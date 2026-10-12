/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Safe optional integration hook for LuckPerms.
 *
 * @author LivingYuji
 */
public class LuckPermsIntegration {

    private final JavaPlugin plugin;
    private LuckPerms luckPerms = null;
    private boolean available = false;

    public LuckPermsIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempts to find and hook LuckPerms.
     */
    public boolean setupLuckPerms() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }

        try {
            luckPerms = LuckPermsProvider.get();
            available = luckPerms != null;
        } catch (NoClassDefFoundError | IllegalStateException ignored) {
            available = false;
        }

        return available;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getPrimaryGroup(UUID uuid) {
        if (!available || luckPerms == null) {
            return "default";
        }
        try {
            var user = luckPerms.getUserManager().getUser(uuid);
            if (user != null) {
                return user.getPrimaryGroup();
            }
        } catch (Exception ignored) {
        }
        return "default";
    }
}
