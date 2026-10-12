/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.permission;

/**
 * Standard permission node definitions for AdminCore.
 *
 * @author LivingYuji
 */
public enum PermissionNode {

    // General & Admin
    ADMIN("admincore.admin", "Full administrative access to AdminCore"),
    GUI("admincore.gui", "Access to AdminCore interactive dashboard GUI"),
    RELOAD("admincore.reload", "Permission to reload configurations"),
    DEBUG("admincore.debug", "Permission to toggle debug mode"),
    STATUS("admincore.status", "Permission to view plugin and module status"),

    // Player Utilities
    PLAYER_INFO("admincore.player.info", "View detailed player metadata and diagnostics"),
    PLAYER_INVENTORY("admincore.player.inventory", "Inspect player inventories and ender chests"),
    PLAYER_TELEPORT("admincore.player.teleport", "Teleport commands (tp, tphere, tpall, back)"),
    PLAYER_HEAL("admincore.player.heal", "Heal and feed players"),
    PLAYER_FLY("admincore.player.fly", "Toggle player flight mode"),
    PLAYER_GOD("admincore.player.god", "Toggle player god mode"),
    PLAYER_VANISH("admincore.player.vanish", "Toggle vanish invisibility mode"),
    PLAYER_FREEZE("admincore.player.freeze", "Freeze and unfreeze players"),
    PLAYER_SPECTATE("admincore.player.spectate", "Spectate other players"),
    PLAYER_EFFECT("admincore.player.effect", "Apply potion effects to players"),
    PLAYER_SPEED("admincore.player.speed", "Change walking and flying speed"),
    PLAYER_STAFFMODE("admincore.player.staffmode", "Toggle staff duty mode"),

    // Fun and Troll Tools
    TROLL_USE("admincore.troll.use", "Access to standard harmless troll commands"),
    TROLL_ADVANCED("admincore.troll.advanced", "Access to advanced prank combinations"),

    // Boss System
    BOSS_VIEW("admincore.boss.view", "View active bosses and boss info"),
    BOSS_MANAGE("admincore.boss.manage", "Spawn, remove, and configure custom bosses"),

    // Event System
    EVENT_JOIN("admincore.event.join", "Join scheduled server events"),
    EVENT_MANAGE("admincore.event.manage", "Create, start, stop, and schedule server events"),

    // Cosmetics
    COSMETICS_USE("admincore.cosmetics.use", "Equip and view unlocked cosmetics"),
    COSMETICS_MANAGE("admincore.cosmetics.manage", "Give cosmetics and reload cosmetic configs"),

    // Moderation
    MODERATION_WARN("admincore.moderation.warn", "Warn players"),
    MODERATION_MUTE("admincore.moderation.mute", "Mute and unmute players"),
    MODERATION_KICK("admincore.moderation.kick", "Kick players from the server"),
    MODERATION_BAN("admincore.moderation.ban", "Ban, tempban, and unban players"),
    MODERATION_HISTORY("admincore.moderation.history", "View punishment histories"),
    MODERATION_NOTES("admincore.moderation.notes", "View and add staff notes on players"),
    MODERATION_REPORTS("admincore.moderation.reports", "Manage and view player reports"),

    // Logs & Stats
    LOGS_VIEW("admincore.logs.view", "View staff action and audit logs"),
    LOGS_EXPORT("admincore.logs.export", "Export audit logs to disk"),

    // Discord Integration
    DISCORD_MANAGE("admincore.discord.manage", "Manage and test Discord webhook notifications");

    private final String node;
    private final String description;

    PermissionNode(String node, String description) {
        this.node = node;
        this.description = description;
    }

    public String getNode() {
        return node;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return node;
    }
}
