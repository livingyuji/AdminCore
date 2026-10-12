/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.boss;

import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.util.LocationUtil;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages custom boss definitions, spawning, ticking AI abilities, and defeat rewards.
 *
 * @author LivingYuji
 */
public class BossManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, BossConfig> bossConfigs = new HashMap<>();
    private final Map<UUID, BossInstance> activeBosses = new ConcurrentHashMap<>();
    private BukkitTask tickingTask;

    public BossManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Loads boss configurations from bosses.yml.
     */
    public void loadConfigs() {
        bossConfigs.clear();
        FileConfiguration config = configManager.getBossesConfig();
        if (config == null || !config.isConfigurationSection("bosses")) {
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("bosses");
        for (String id : section.getKeys(false)) {
            ConfigurationSection bs = section.getConfigurationSection(id);
            if (bs == null) continue;

            String displayName = bs.getString("display-name", id);
            String entityTypeStr = bs.getString("entity-type", "ZOMBIE");
            EntityType type;
            try {
                type = EntityType.valueOf(entityTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                type = EntityType.ZOMBIE;
            }

            double health = bs.getDouble("health", 300.0);
            double damage = bs.getDouble("damage", 10.0);
            double speed = bs.getDouble("speed", 0.3);

            String colorStr = bs.getString("bar-color", "RED");
            BarColor color;
            try {
                color = BarColor.valueOf(colorStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                color = BarColor.RED;
            }

            String styleStr = bs.getString("bar-style", "SOLID");
            BarStyle style;
            try {
                style = BarStyle.valueOf(styleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                style = BarStyle.SOLID;
            }

            Location spawnLoc = LocationUtil.deserialize(bs.getString("spawn-location"));
            List<String> rewards = bs.getStringList("reward-commands");
            List<String> abilities = bs.getStringList("abilities");

            bossConfigs.put(id.toLowerCase(), new BossConfig(
                    id, displayName, type, health, damage, speed, color, style, spawnLoc, rewards, abilities
            ));
        }

        plugin.getLogger().info("Loaded " + bossConfigs.size() + " custom boss configurations.");
    }

    /**
     * Starts the boss ticking loop for boss bars and abilities.
     */
    public void startTicking() {
        if (tickingTask != null) {
            tickingTask.cancel();
        }

        tickingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (BossInstance instance : new ArrayList<>(activeBosses.values())) {
                if (instance.isDead()) {
                    instance.cleanup();
                    activeBosses.remove(instance.getEntityId());
                    continue;
                }

                LivingEntity entity = instance.getEntity();
                BossBar bar = instance.getBossBar();

                // Update health bar progress
                double currentHealth = entity.getHealth();
                double maxHealth = instance.getConfig().getMaxHealth();
                bar.setProgress(Math.max(0.0, Math.min(1.0, currentHealth / maxHealth)));

                // Update players seeing boss bar (within 40 blocks)
                List<Player> nearby = new ArrayList<>();
                for (Player player : entity.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(entity.getLocation()) <= 1600) { // 40^2
                        bar.addPlayer(player);
                        nearby.add(player);
                    } else {
                        bar.removePlayer(player);
                    }
                }

                if (!nearby.isEmpty()) {
                    executeAbilities(instance, nearby);
                }
            }
        }, 20L, 20L); // Tick once per second
    }

    private void executeAbilities(BossInstance instance, List<Player> nearbyPlayers) {
        LivingEntity boss = instance.getEntity();
        List<String> abilities = instance.getConfig().getAbilities();

        // 1. Knockback ability (every 10s)
        if (abilities.contains("knockback") && instance.canUseAbility("knockback", 10_000L)) {
            for (Player player : nearbyPlayers) {
                if (player.getLocation().distanceSquared(boss.getLocation()) <= 64) { // 8 blocks
                    Vector dir = player.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
                    dir.setY(0.5);
                    player.setVelocity(dir.multiply(1.5));
                    player.sendMessage(TextUtil.toComponent("&c&l" + instance.getConfig().getDisplayName() + "&r &cuses Shockwave!"));
                }
            }
            boss.getWorld().spawnParticle(Particle.EXPLOSION, boss.getLocation().add(0, 1, 0), 3);
            instance.recordAbilityUse("knockback");
        }

        // 2. Minions summon ability (every 25s)
        if (abilities.contains("minions") && instance.canUseAbility("minions", 25_000L)) {
            for (int i = 0; i < 2; i++) {
                Location spawn = boss.getLocation().add((Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4);
                Zombie minion = (Zombie) boss.getWorld().spawnEntity(spawn, EntityType.ZOMBIE);
                minion.setBaby(true);
                minion.customName(TextUtil.toComponent("&cMinion of " + instance.getConfig().getDisplayName()));
                minion.setCustomNameVisible(true);
            }
            instance.recordAbilityUse("minions");
        }

        // 3. Ground strike ability (every 15s)
        if (abilities.contains("strike") && instance.canUseAbility("strike", 15_000L)) {
            for (Player player : nearbyPlayers) {
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.damage(4.0, boss);
                player.sendMessage(TextUtil.toComponent("&c&l" + instance.getConfig().getDisplayName() + "&r &ccalled down lightning strikes!"));
            }
            instance.recordAbilityUse("strike");
        }
    }

    /**
     * Spawns a custom boss at a target location.
     */
    public boolean spawnBoss(String id, Location location) {
        BossConfig config = bossConfigs.get(id.toLowerCase());
        if (config == null) {
            return false;
        }

        Location spawnLoc = location != null ? location : config.getSpawnLocation();
        if (spawnLoc == null || spawnLoc.getWorld() == null) {
            return false;
        }

        Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, config.getEntityType());
        if (!(entity instanceof LivingEntity living)) {
            entity.remove();
            return false;
        }

        living.customName(TextUtil.toComponent(config.getDisplayName()));
        living.setCustomNameVisible(true);
        living.setRemoveWhenFarAway(false);

        // Configure attributes
        AttributeInstance maxHealthAttr = living.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(config.getMaxHealth());
        }
        living.setHealth(config.getMaxHealth());

        AttributeInstance damageAttr = living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(config.getAttackDamage());
        }

        AttributeInstance speedAttr = living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(config.getMovementSpeed());
        }

        BossBar bar = Bukkit.createBossBar(
                TextUtil.colorize(config.getDisplayName()),
                config.getBarColor(),
                config.getBarStyle()
        );

        BossInstance instance = new BossInstance(config.getId(), living, bar, config);
        activeBosses.put(living.getUniqueId(), instance);

        Bukkit.broadcast(TextUtil.toComponent("&4&l[BOSS SPAWN]&r &e" + config.getDisplayName() + " &ehas appeared in the world!"));
        return true;
    }

    /**
     * Sets the configured spawn location for a boss.
     */
    public boolean setBossSpawn(String id, Location location) {
        BossConfig config = bossConfigs.get(id.toLowerCase());
        if (config == null) {
            return false;
        }
        config.setSpawnLocation(location);

        FileConfiguration fConfig = configManager.getBossesConfig();
        if (fConfig != null) {
            fConfig.set("bosses." + config.getId() + ".spawn-location", LocationUtil.serialize(location));
            configManager.saveFile("bosses.yml");
        }
        return true;
    }

    /**
     * Removes all active bosses.
     */
    public int killAll() {
        int count = activeBosses.size();
        for (BossInstance instance : activeBosses.values()) {
            instance.cleanup();
        }
        activeBosses.clear();
        return count;
    }

    /**
     * Cleans up all active bosses and cancels the ticking task on shutdown.
     */
    public void shutdown() {
        if (tickingTask != null) {
            tickingTask.cancel();
        }
        killAll();
    }

    public Map<String, BossConfig> getBossConfigs() {
        return Collections.unmodifiableMap(bossConfigs);
    }

    public Map<UUID, BossInstance> getActiveBosses() {
        return Collections.unmodifiableMap(activeBosses);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        BossInstance instance = activeBosses.remove(event.getEntity().getUniqueId());
        if (instance != null) {
            instance.setDead(true);
            instance.getBossBar().removeAll();

            Player killer = event.getEntity().getKiller();
            String killerName = killer != null ? killer.getName() : "Unknown";

            Bukkit.broadcast(TextUtil.toComponent("&6&l[BOSS DEFEATED]&r &e" + instance.getConfig().getDisplayName() +
                    " &ehas been slain by &f" + killerName + "&e!"));

            // Execute reward commands
            if (killer != null) {
                for (String cmd : instance.getConfig().getRewardCommands()) {
                    String formatted = cmd.replace("%player%", killer.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
                }
            }
        }
    }
}
