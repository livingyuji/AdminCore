/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.troll;

import com.livingyuji.admincore.util.TextUtil;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages harmless fun and prank tools with safety controls, bounds, and immediate cancellation.
 *
 * @author LivingYuji
 */
public class TrollManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, List<BukkitTask>> activeTrollTasks = new ConcurrentHashMap<>();
    private final Set<UUID> launchImmunity = ConcurrentHashMap.newKeySet();

    public TrollManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Broadcasts a fake join message.
     */
    public void fakeJoin(String playerName) {
        Bukkit.broadcast(TextUtil.toComponent("&e" + playerName + " joined the game"));
    }

    /**
     * Broadcasts a fake leave message.
     */
    public void fakeLeave(String playerName) {
        Bukkit.broadcast(TextUtil.toComponent("&e" + playerName + " left the game"));
    }

    /**
     * Strikes harmless visual lightning at the player's location.
     */
    public void visualLightning(Player target) {
        target.getWorld().strikeLightningEffect(target.getLocation());
    }

    /**
     * Safely launches the player upwards without causing fall damage.
     */
    public void launch(Player target) {
        UUID uuid = target.getUniqueId();
        launchImmunity.add(uuid);
        target.setVelocity(new Vector(0, 1.8, 0));

        // Remove immunity after 8 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> launchImmunity.remove(uuid), 160L);
    }

    /**
     * Harmlessly spins the player for 5 seconds.
     */
    public void spin(Player target) {
        UUID uuid = target.getUniqueId();
        stopPlayerTasks(uuid);

        List<BukkitTask> tasks = activeTrollTasks.computeIfAbsent(uuid, k -> new ArrayList<>());

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!target.isOnline() || ticks++ >= 100) { // 5 seconds (100 ticks)
                    stopPlayerTasks(uuid);
                    return;
                }
                Location loc = target.getLocation();
                loc.setYaw((loc.getYaw() + 30f) % 360f);
                target.teleport(loc);
            }
        }, 1L, 1L);

        tasks.add(task);
    }

    /**
     * Plays a specific sound at player's location.
     */
    public boolean playSound(Player target, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Spawns harmless particles around the player.
     */
    public boolean spawnParticles(Player target, String particleName) {
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            target.getWorld().spawnParticle(particle, target.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.05);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Sends a custom title to the player.
     */
    public void sendTitle(Player target, String titleText) {
        Title title = Title.title(
                TextUtil.toComponent(titleText),
                TextUtil.toComponent("&7- AdminCore -"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
        );
        target.showTitle(title);
    }

    /**
     * Stops all active troll effects on a specific player.
     */
    public void stopPlayerTasks(UUID targetUuid) {
        List<BukkitTask> tasks = activeTrollTasks.remove(targetUuid);
        if (tasks != null) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
    }

    /**
     * Clears all running troll tasks on the entire server.
     */
    public void clearAll() {
        for (List<BukkitTask> tasks : activeTrollTasks.values()) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
        activeTrollTasks.clear();
        launchImmunity.clear();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && launchImmunity.contains(player.getUniqueId())) {
                event.setCancelled(true);
                launchImmunity.remove(player.getUniqueId());
            }
        }
    }
}
