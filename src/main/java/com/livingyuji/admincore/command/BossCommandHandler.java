/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.boss.BossConfig;
import com.livingyuji.admincore.boss.BossInstance;
import com.livingyuji.admincore.boss.BossManager;
import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles custom boss administration subcommands.
 *
 * @author LivingYuji
 */
public class BossCommandHandler {

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final BossManager bossManager;

    public BossCommandHandler(MessageService messageService, PermissionService permissionService, BossManager bossManager) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.bossManager = bossManager;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.BOSS_VIEW)) {
            messageService.sendRaw(sender, "&cYou do not have permission for boss commands.");
            return;
        }

        if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
            messageService.sendRaw(sender, "&8&m----------------&r &4&lAdminCore Bosses &8&m----------------");
            if (bossManager.getBossConfigs().isEmpty()) {
                messageService.sendRaw(sender, "&7No boss templates configured in bosses.yml.");
            } else {
                for (BossConfig boss : bossManager.getBossConfigs().values()) {
                    messageService.sendRaw(sender, "&8- &b" + boss.getId() + " &7(" + boss.getDisplayName() + "&7) &8| &c" +
                            boss.getMaxHealth() + " HP &8| &eType: &f" + boss.getEntityType().name());
                }
            }
            messageService.sendRaw(sender, "&7Active spawned bosses: &e" + bossManager.getActiveBosses().size());
            messageService.sendRaw(sender, "&8&m---------------------------------------------------");
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "info" -> {
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac boss info <id>");
                    return;
                }
                BossConfig boss = bossManager.getBossConfigs().get(args[2].toLowerCase());
                if (boss == null) {
                    messageService.sendRaw(sender, "&cBoss not found: " + args[2]);
                    return;
                }
                messageService.sendRaw(sender, "&8&m----------------&r &4&lBoss Info: " + boss.getDisplayName() + " &8&m----------------");
                messageService.sendRaw(sender, "&7ID: &f" + boss.getId() + " &8| &7Entity: &f" + boss.getEntityType().name());
                messageService.sendRaw(sender, "&7Max Health: &c" + boss.getMaxHealth() + " &8| &7Damage: &e" + boss.getAttackDamage());
                messageService.sendRaw(sender, "&7Abilities: &b" + String.join(", ", boss.getAbilities()));
                messageService.sendRaw(sender, "&7Rewards: &a" + boss.getRewardCommands().size() + " commands configured");
                messageService.sendRaw(sender, "&8&m--------------------------------------------------------");
            }
            case "spawn" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.BOSS_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to spawn bosses.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac boss spawn <id>");
                    return;
                }
                Player player = sender instanceof Player p ? p : null;
                boolean spawned = bossManager.spawnBoss(args[2], player != null ? player.getLocation() : null);
                messageService.sendRaw(sender, spawned ? "&aSpawned boss &e" + args[2] : "&cFailed to spawn boss (check ID or spawn location).");
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cThis command can only be run by a player.");
                    return;
                }
                if (!permissionService.hasPermission(sender, PermissionNode.BOSS_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to configure bosses.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac boss setspawn <id>");
                    return;
                }
                boolean set = bossManager.setBossSpawn(args[2], player.getLocation());
                messageService.sendRaw(sender, set ? "&aSet spawn location for boss &e" + args[2] : "&cBoss not found: " + args[2]);
            }
            case "killall", "remove" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.BOSS_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to remove bosses.");
                    return;
                }
                int count = bossManager.killAll();
                messageService.sendRaw(sender, "&cRemoved &e" + count + " &cactive plugin bosses.");
            }
            case "reload" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.BOSS_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to reload bosses.");
                    return;
                }
                bossManager.loadConfigs();
                messageService.sendRaw(sender, "&aBoss configurations reloaded.");
            }
            default -> messageService.sendRaw(sender, "&cUnknown subcommand. Use /ac boss list");
        }
    }
}
