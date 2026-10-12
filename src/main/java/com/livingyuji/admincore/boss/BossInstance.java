/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.boss;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an active spawned boss in the server world.
 *
 * @author LivingYuji
 */
public class BossInstance {

    private final String bossId;
    private final LivingEntity entity;
    private final BossBar bossBar;
    private final BossConfig config;
    private final Map<String, Long> lastAbilityTimes = new HashMap<>();
    private boolean dead = false;

    public BossInstance(String bossId, LivingEntity entity, BossBar bossBar, BossConfig config) {
        this.bossId = bossId;
        this.entity = entity;
        this.bossBar = bossBar;
        this.config = config;
    }

    public String getBossId() {
        return bossId;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public UUID getEntityId() {
        return entity.getUniqueId();
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public BossConfig getConfig() {
        return config;
    }

    public boolean isDead() {
        return dead || !entity.isValid() || entity.isDead();
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean canUseAbility(String abilityName, long cooldownMillis) {
        long last = lastAbilityTimes.getOrDefault(abilityName, 0L);
        return System.currentTimeMillis() - last >= cooldownMillis;
    }

    public void recordAbilityUse(String abilityName) {
        lastAbilityTimes.put(abilityName, System.currentTimeMillis());
    }

    public void cleanup() {
        bossBar.removeAll();
        if (entity.isValid()) {
            entity.remove();
        }
    }
}
