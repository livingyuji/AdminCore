/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.cosmetics;

import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.storage.DatabaseService;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player cosmetics, particle animations, selection GUIs, and persistence.
 *
 * @author LivingYuji
 */
public class CosmeticsManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseService databaseService;

    private final Map<String, CosmeticDefinition> definitions = new HashMap<>();
    private final Map<UUID, Map<CosmeticType, CosmeticDefinition>> activeCosmetics = new ConcurrentHashMap<>();
    private BukkitTask particleTask;
    private double auraAngle = 0;

    public CosmeticsManager(JavaPlugin plugin, ConfigManager configManager, DatabaseService databaseService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseService = databaseService;
    }

    /**
     * Loads cosmetics from cosmetics.yml.
     */
    public void loadCosmetics() {
        definitions.clear();
        FileConfiguration config = configManager.getCosmeticsConfig();
        if (config == null || !config.isConfigurationSection("cosmetics")) {
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("cosmetics");
        for (String id : section.getKeys(false)) {
            ConfigurationSection cs = section.getConfigurationSection(id);
            if (cs == null) continue;

            String name = cs.getString("name", id);
            String typeStr = cs.getString("type", "TRAIL");
            CosmeticType type;
            try {
                type = CosmeticType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                type = CosmeticType.TRAIL;
            }

            String particleStr = cs.getString("particle", "FLAME");
            Particle particle;
            try {
                particle = Particle.valueOf(particleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                particle = Particle.FLAME;
            }

            String matStr = cs.getString("icon", "BLAZE_POWDER");
            Material icon;
            try {
                icon = Material.valueOf(matStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                icon = Material.BLAZE_POWDER;
            }

            String permission = cs.getString("permission", "admincore.cosmetics." + id);

            definitions.put(id.toLowerCase(), new CosmeticDefinition(id, name, type, particle, icon, permission));
        }

        plugin.getLogger().info("Loaded " + definitions.size() + " cosmetic effects.");
    }

    /**
     * Starts ticking particle task for active trails and auras.
     */
    public void startParticleTask() {
        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            auraAngle = (auraAngle + 0.3) % (Math.PI * 2);

            for (Map.Entry<UUID, Map<CosmeticType, CosmeticDefinition>> entry : activeCosmetics.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    continue;
                }

                Map<CosmeticType, CosmeticDefinition> playerCosmetics = entry.getValue();

                // 1. Trail effect (spawns behind feet)
                CosmeticDefinition trail = playerCosmetics.get(CosmeticType.TRAIL);
                if (trail != null && trail.getParticle() != null) {
                    Location loc = player.getLocation().add(0, 0.2, 0);
                    player.getWorld().spawnParticle(trail.getParticle(), loc, 3, 0.2, 0.1, 0.2, 0.01);
                }

                // 2. Aura effect (orbits around player head/body)
                CosmeticDefinition aura = playerCosmetics.get(CosmeticType.AURA);
                if (aura != null && aura.getParticle() != null) {
                    Location center = player.getLocation().add(0, 1.2, 0);
                    double x = Math.cos(auraAngle) * 0.9;
                    double z = Math.sin(auraAngle) * 0.9;
                    center.add(x, 0, z);
                    player.getWorld().spawnParticle(aura.getParticle(), center, 1, 0, 0, 0, 0);
                }
            }
        }, 4L, 4L); // Every 4 ticks (~200ms)
    }

    public boolean equipCosmetic(Player player, String id) {
        CosmeticDefinition def = definitions.get(id.toLowerCase());
        if (def == null) {
            return false;
        }

        if (!player.hasPermission(def.getPermission()) && !player.isOp() && !player.hasPermission("admincore.admin")) {
            player.sendMessage(TextUtil.toComponent("&cYou do not have permission for cosmetic &e" + def.getName()));
            return false;
        }

        Map<CosmeticType, CosmeticDefinition> map = activeCosmetics.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        map.put(def.getType(), def);

        // Persist to DB
        saveCosmeticToDb(player.getUniqueId(), def.getType().name(), def.getId());

        player.sendMessage(TextUtil.toComponent("&aEquipped cosmetic &b" + def.getName() + "&a!"));
        return true;
    }

    public void clearCosmetics(Player player) {
        activeCosmetics.remove(player.getUniqueId());
        removeCosmeticsFromDb(player.getUniqueId());
        player.sendMessage(TextUtil.toComponent("&cCleared all active cosmetics."));
    }

    private void saveCosmeticToDb(UUID uuid, String type, String cosmeticId) {
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_player_cosmetics (player_uuid, cosmetic_type, cosmetic_id, active) " +
                                 "VALUES (?, ?, ?, 1) " +
                                 "ON CONFLICT(player_uuid, cosmetic_type, cosmetic_id) DO UPDATE SET active = 1")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, type);
                stmt.setString(3, cosmeticId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist cosmetic equip", e);
            }
        });
    }

    private void removeCosmeticsFromDb(UUID uuid) {
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM ac_player_cosmetics WHERE player_uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to clear cosmetics in database", e);
            }
        });
    }

    public Map<String, CosmeticDefinition> getDefinitions() {
        return Collections.unmodifiableMap(definitions);
    }

    public void shutdown() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        activeCosmetics.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Load active cosmetics from database
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM ac_player_cosmetics WHERE player_uuid = ? AND active = 1")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("cosmetic_id");
                        CosmeticDefinition def = definitions.get(id.toLowerCase());
                        if (def != null) {
                            activeCosmetics.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(def.getType(), def);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player cosmetics on join", e);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeCosmetics.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            Map<CosmeticType, CosmeticDefinition> map = activeCosmetics.get(killer.getUniqueId());
            if (map != null && map.containsKey(CosmeticType.KILL_EFFECT)) {
                CosmeticDefinition def = map.get(CosmeticType.KILL_EFFECT);
                Location loc = event.getEntity().getLocation();
                loc.getWorld().strikeLightningEffect(loc);
                loc.getWorld().spawnParticle(def.getParticle(), loc.add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }
}
