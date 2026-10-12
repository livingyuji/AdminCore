/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.boss;

import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Configuration definition for a custom boss template.
 *
 * @author LivingYuji
 */
public class BossConfig {

    private final String id;
    private final String displayName;
    private final EntityType entityType;
    private final double maxHealth;
    private final double attackDamage;
    private final double movementSpeed;
    private final BarColor barColor;
    private final BarStyle barStyle;
    private Location spawnLocation;
    private final List<String> rewardCommands;
    private final List<String> abilities;

    public BossConfig(String id, String displayName, EntityType entityType, double maxHealth,
                      double attackDamage, double movementSpeed, BarColor barColor, BarStyle barStyle,
                      Location spawnLocation, List<String> rewardCommands, List<String> abilities) {
        this.id = id;
        this.displayName = displayName;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.barColor = barColor;
        this.barStyle = barStyle;
        this.spawnLocation = spawnLocation;
        this.rewardCommands = rewardCommands != null ? rewardCommands : List.of();
        this.abilities = abilities != null ? abilities : List.of();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    public List<String> getAbilities() {
        return abilities;
    }
}
