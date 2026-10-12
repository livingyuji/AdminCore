/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.event;

import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.storage.DatabaseService;
import com.livingyuji.admincore.util.LocationUtil;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages the lifecycle, state machines, rules, and rewards of server events.
 *
 * @author LivingYuji
 */
public class EventManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseService databaseService;

    private final Map<String, AdminEvent> registeredEvents = new HashMap<>();
    private AdminEvent activeEvent = null;
    private BukkitTask countdownTask = null;
    private BukkitTask gameLoopTask = null;

    public EventManager(JavaPlugin plugin, ConfigManager configManager, DatabaseService databaseService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseService = databaseService;
    }

    /**
     * Loads events from events.yml.
     */
    public void loadEvents() {
        registeredEvents.clear();
        FileConfiguration config = configManager.getEventsConfig();
        if (config == null || !config.isConfigurationSection("events")) {
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("events");
        for (String id : section.getKeys(false)) {
            ConfigurationSection es = section.getConfigurationSection(id);
            if (es == null) continue;

            String typeStr = es.getString("type", "LMS");
            EventType type;
            try {
                type = EventType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                type = EventType.LMS;
            }

            String displayName = es.getString("display-name", id);
            Location lobby = LocationUtil.deserialize(es.getString("lobby-location"));
            Location arena = LocationUtil.deserialize(es.getString("arena-location"));
            int minPlayers = es.getInt("min-players", 2);
            int maxPlayers = es.getInt("max-players", 50);
            List<String> rewards = es.getStringList("reward-commands");

            AdminEvent event = new AdminEvent(id, type, displayName, lobby, arena, minPlayers, maxPlayers, rewards);
            event.setState(EventState.WAITING);
            registeredEvents.put(id.toLowerCase(), event);
        }

        plugin.getLogger().info("Loaded " + registeredEvents.size() + " server events.");
    }

    /**
     * Creates a new event.
     */
    public boolean createEvent(String id, EventType type, String displayName) {
        String key = id.toLowerCase();
        if (registeredEvents.containsKey(key)) {
            return false;
        }

        AdminEvent event = new AdminEvent(id, type, displayName, null, null, 2, 50, new ArrayList<>());
        event.setState(EventState.WAITING);
        registeredEvents.put(key, event);

        saveEventToConfig(event);
        return true;
    }

    public boolean deleteEvent(String id) {
        String key = id.toLowerCase();
        AdminEvent event = registeredEvents.remove(key);
        if (event != null) {
            if (activeEvent == event) {
                stopActiveEvent("Event deleted by administrator");
            }
            FileConfiguration config = configManager.getEventsConfig();
            if (config != null) {
                config.set("events." + event.getId(), null);
                configManager.saveFile("events.yml");
            }
            return true;
        }
        return false;
    }

    private void saveEventToConfig(AdminEvent event) {
        FileConfiguration config = configManager.getEventsConfig();
        if (config != null) {
            String path = "events." + event.getId();
            config.set(path + ".type", event.getType().name());
            config.set(path + ".display-name", event.getDisplayName());
            config.set(path + ".min-players", event.getMinPlayers());
            config.set(path + ".max-players", event.getMaxPlayers());
            if (event.getLobbyLocation() != null) {
                config.set(path + ".lobby-location", LocationUtil.serialize(event.getLobbyLocation()));
            }
            if (event.getArenaLocation() != null) {
                config.set(path + ".arena-location", LocationUtil.serialize(event.getArenaLocation()));
            }
            config.set(path + ".reward-commands", event.getRewardCommands());
            configManager.saveFile("events.yml");
        }
    }

    public boolean startEvent(String id) {
        if (activeEvent != null) {
            return false;
        }
        AdminEvent event = registeredEvents.get(id.toLowerCase());
        if (event == null || event.getArenaLocation() == null) {
            return false;
        }

        activeEvent = event;
        activeEvent.reset();
        activeEvent.setState(EventState.WAITING);

        Bukkit.broadcast(TextUtil.toComponent("&6&l[EVENT ANNOUNCEMENT]&r &eEvent &b" + event.getDisplayName() +
                " &ehas opened! Type &a/ac event join &eto participate! Starting in 30 seconds!"));

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int remaining = activeEvent.decrementCountdown();
            if (remaining == 20 || remaining == 10 || (remaining <= 5 && remaining > 0)) {
                Bukkit.broadcast(TextUtil.toComponent("&eEvent &b" + activeEvent.getDisplayName() +
                        " &estarts in &c" + remaining + " &eseconds!"));
            } else if (remaining <= 0) {
                countdownTask.cancel();
                countdownTask = null;
                beginEventMatch();
            }
        }, 20L, 20L);

        return true;
    }

    private void beginEventMatch() {
        if (activeEvent == null) return;

        if (activeEvent.getParticipants().size() < activeEvent.getMinPlayers()) {
            Bukkit.broadcast(TextUtil.toComponent("&cEvent cancelled: Not enough players joined (minimum " +
                    activeEvent.getMinPlayers() + ")."));
            stopActiveEvent("Not enough players");
            return;
        }

        activeEvent.setState(EventState.RUNNING);
        Bukkit.broadcast(TextUtil.toComponent("&a&l[EVENT STARTED]&r &e" + activeEvent.getDisplayName() + " &ehas begun!"));

        // Teleport participants to arena
        for (UUID uuid : activeEvent.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.teleport(activeEvent.getArenaLocation());
                player.sendMessage(TextUtil.toComponent("&aTeleported to event arena! Good luck!"));
            }
        }

        startGameLoop();
    }

    private void startGameLoop() {
        gameLoopTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeEvent == null || activeEvent.getState() != EventState.RUNNING) {
                if (gameLoopTask != null) gameLoopTask.cancel();
                return;
            }

            // LMS / Sumo win condition
            if (activeEvent.getType() == EventType.LMS || activeEvent.getType() == EventType.SUMO) {
                if (activeEvent.getAliveParticipants().size() <= 1) {
                    UUID winnerUuid = activeEvent.getAliveParticipants().stream().findFirst().orElse(null);
                    endEventWithWinner(winnerUuid);
                    return;
                }

                // Sumo boundary check
                if (activeEvent.getType() == EventType.SUMO) {
                    Location arena = activeEvent.getArenaLocation();
                    for (UUID uuid : new ArrayList<>(activeEvent.getAliveParticipants())) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            if (player.getLocation().distanceSquared(arena) > 225 || player.getLocation().getY() < arena.getY() - 1) { // 15 blocks radius
                                activeEvent.eliminate(uuid);
                                player.sendMessage(TextUtil.toComponent("&cYou fell out of the sumo ring and were eliminated!"));
                                Bukkit.broadcast(TextUtil.toComponent("&e" + player.getName() + " was eliminated from Sumo! (" +
                                        activeEvent.getAliveParticipants().size() + " remaining)"));
                            }
                        }
                    }
                }
            }

            // KOTH capture check
            if (activeEvent.getType() == EventType.KOTH) {
                Location hill = activeEvent.getArenaLocation();
                for (UUID uuid : activeEvent.getAliveParticipants()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        if (player.getLocation().distanceSquared(hill) <= 25) { // 5 blocks radius
                            activeEvent.addScore(uuid, 1);
                            int score = activeEvent.getScores().getOrDefault(uuid, 0);
                            player.sendActionBar(TextUtil.toComponent("&6Hill Control: &e" + score + "/60 points"));
                            if (score >= 60) {
                                endEventWithWinner(uuid);
                                return;
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    public void endEventWithWinner(UUID winnerUuid) {
        if (activeEvent == null) return;
        activeEvent.setState(EventState.ENDING);

        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }

        String winnerName = "None";
        if (winnerUuid != null) {
            Player winner = Bukkit.getPlayer(winnerUuid);
            if (winner != null) {
                winnerName = winner.getName();
                for (String cmd : activeEvent.getRewardCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", winnerName));
                }
            }
            recordWin(winnerUuid, activeEvent.getId());
        }

        Bukkit.broadcast(TextUtil.toComponent("&6&l[EVENT WINNER]&r &e" + winnerName + " &ehas won &b" +
                activeEvent.getDisplayName() + "&e! Congratulations!"));

        // Record participations
        for (UUID uuid : activeEvent.getParticipants()) {
            recordParticipation(uuid, activeEvent.getId());
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && activeEvent.getLobbyLocation() != null) {
                player.teleport(activeEvent.getLobbyLocation());
            }
        }

        activeEvent.setState(EventState.FINISHED);
        activeEvent = null;
    }

    public void stopActiveEvent(String reason) {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }
        if (activeEvent != null) {
            Bukkit.broadcast(TextUtil.toComponent("&c&l[EVENT STOPPED]&r &cEvent &e" + activeEvent.getDisplayName() +
                    " &cwas cancelled: &f" + reason));
            for (UUID uuid : activeEvent.getParticipants()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && activeEvent.getLobbyLocation() != null) {
                    player.teleport(activeEvent.getLobbyLocation());
                }
            }
            activeEvent.setState(EventState.FINISHED);
            activeEvent = null;
        }
    }

    public boolean joinActiveEvent(Player player) {
        if (activeEvent == null) {
            player.sendMessage(TextUtil.toComponent("&cThere is no active event currently accepting players."));
            return false;
        }
        if (activeEvent.getState() != EventState.WAITING) {
            player.sendMessage(TextUtil.toComponent("&cThis event has already started or is in progress."));
            return false;
        }
        if (activeEvent.getParticipants().contains(player.getUniqueId())) {
            player.sendMessage(TextUtil.toComponent("&cYou have already joined this event."));
            return false;
        }

        boolean added = activeEvent.addParticipant(player.getUniqueId());
        if (added) {
            if (activeEvent.getLobbyLocation() != null) {
                player.teleport(activeEvent.getLobbyLocation());
            }
            Bukkit.broadcast(TextUtil.toComponent("&a" + player.getName() + " joined event &b" +
                    activeEvent.getDisplayName() + " &7(" + activeEvent.getParticipants().size() + "/" +
                    activeEvent.getMaxPlayers() + ")"));
            return true;
        } else {
            player.sendMessage(TextUtil.toComponent("&cEvent is currently full!"));
            return false;
        }
    }

    public boolean leaveActiveEvent(Player player) {
        if (activeEvent == null || !activeEvent.getParticipants().contains(player.getUniqueId())) {
            return false;
        }
        activeEvent.removeParticipant(player.getUniqueId());
        if (activeEvent.getLobbyLocation() != null) {
            player.teleport(activeEvent.getLobbyLocation());
        }
        player.sendMessage(TextUtil.toComponent("&eYou left the active event."));
        return true;
    }

    private void recordWin(UUID uuid, String eventName) {
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_event_stats (player_uuid, event_name, wins, participations) " +
                                 "VALUES (?, ?, 1, 1) " +
                                 "ON CONFLICT(player_uuid, event_name) DO UPDATE SET wins = wins + 1, participations = participations + 1")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, eventName);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to record event win", e);
            }
        });
    }

    private void recordParticipation(UUID uuid, String eventName) {
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_event_stats (player_uuid, event_name, wins, participations) " +
                                 "VALUES (?, ?, 0, 1) " +
                                 "ON CONFLICT(player_uuid, event_name) DO UPDATE SET participations = participations + 1")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, eventName);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to record event participation", e);
            }
        });
    }

    public AdminEvent getActiveEvent() {
        return activeEvent;
    }

    public Map<String, AdminEvent> getRegisteredEvents() {
        return Collections.unmodifiableMap(registeredEvents);
    }

    public void setEventLobby(String id, Location loc) {
        AdminEvent event = registeredEvents.get(id.toLowerCase());
        if (event != null) {
            event.setLobbyLocation(loc);
            saveEventToConfig(event);
        }
    }

    public void setEventArena(String id, Location loc) {
        AdminEvent event = registeredEvents.get(id.toLowerCase());
        if (event != null) {
            event.setArenaLocation(loc);
            saveEventToConfig(event);
        }
    }

    public void shutdown() {
        stopActiveEvent("Plugin disabled / server shutdown");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (activeEvent != null && activeEvent.getState() == EventState.RUNNING) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (activeEvent.getAliveParticipants().contains(uuid)) {
                activeEvent.eliminate(uuid);
                Bukkit.broadcast(TextUtil.toComponent("&e" + event.getPlayer().getName() + " was eliminated from &b" +
                        activeEvent.getDisplayName() + "! (" + activeEvent.getAliveParticipants().size() + " alive)"));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activeEvent != null) {
            activeEvent.removeParticipant(event.getPlayer().getUniqueId());
        }
    }
}
