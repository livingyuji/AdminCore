/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.troll.TrollManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Handles troll and fun pranks with safety boundaries and permission checks.
 *
 * @author LivingYuji
 */
public class TrollCommandHandler {

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final TrollManager trollManager;

    public TrollCommandHandler(MessageService messageService, PermissionService permissionService, TrollManager trollManager) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.trollManager = trollManager;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.TROLL_USE)) {
            messageService.sendRaw(sender, "&cYou do not have permission for troll commands.");
            return;
        }

        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "fakejoin" -> {
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac troll fakejoin <player>");
                    return;
                }
                trollManager.fakeJoin(args[2]);
            }
            case "fakeleave" -> {
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac troll fakeleave <player>");
                    return;
                }
                trollManager.fakeLeave(args[2]);
            }
            case "lightning" -> {
                Player target = getTarget(sender, args, 2);
                if (target != null) {
                    trollManager.visualLightning(target);
                    messageService.sendRaw(sender, "&aStruck visual lightning at &e" + target.getName());
                }
            }
            case "launch" -> {
                Player target = getTarget(sender, args, 2);
                if (target != null) {
                    trollManager.launch(target);
                    messageService.sendRaw(sender, "&aSafely launched &e" + target.getName());
                }
            }
            case "spin" -> {
                Player target = getTarget(sender, args, 2);
                if (target != null) {
                    trollManager.spin(target);
                    messageService.sendRaw(sender, "&aSpinning &e" + target.getName() + " &afor 5 seconds.");
                }
            }
            case "sound" -> {
                if (args.length < 4) {
                    messageService.sendRaw(sender, "&cUsage: /ac troll sound <player> <sound>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    messageService.sendRaw(sender, "&cPlayer not found: " + args[2]);
                    return;
                }
                boolean played = trollManager.playSound(target, args[3]);
                messageService.sendRaw(sender, played ? "&aPlayed sound &e" + args[3] + " &ato &f" + target.getName() : "&cInvalid sound name: " + args[3]);
            }
            case "title" -> {
                if (args.length < 4) {
                    messageService.sendRaw(sender, "&cUsage: /ac troll title <player> <text>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    messageService.sendRaw(sender, "&cPlayer not found: " + args[2]);
                    return;
                }
                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                trollManager.sendTitle(target, text);
                messageService.sendRaw(sender, "&aSent title to &e" + target.getName());
            }
            case "particles" -> {
                if (args.length < 4) {
                    messageService.sendRaw(sender, "&cUsage: /ac troll particles <player> <effect>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    messageService.sendRaw(sender, "&cPlayer not found: " + args[2]);
                    return;
                }
                boolean spawned = trollManager.spawnParticles(target, args[3]);
                messageService.sendRaw(sender, spawned ? "&aSpawned particles around &e" + target.getName() : "&cInvalid particle name: " + args[3]);
            }
            case "stop" -> {
                Player target = getTarget(sender, args, 2);
                if (target != null) {
                    trollManager.stopPlayerTasks(target.getUniqueId());
                    messageService.sendRaw(sender, "&aStopped active troll pranks on &e" + target.getName());
                }
            }
            case "clear" -> {
                trollManager.clearAll();
                messageService.sendRaw(sender, "&aCleared all active troll pranks across the server.");
            }
            default -> sendHelp(sender);
        }
    }

    private Player getTarget(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            messageService.sendRaw(sender, "&cPlease specify a target player.");
            return null;
        }
        Player target = Bukkit.getPlayer(args[index]);
        if (target == null) {
            messageService.sendRaw(sender, "&cPlayer not found: " + args[index]);
            return null;
        }
        return target;
    }

    private void sendHelp(CommandSender sender) {
        messageService.sendRaw(sender, "&8&m----------------&r &d&lAdminCore Troll Help &8&m----------------");
        messageService.sendRaw(sender, "&b/ac troll fakejoin <player> &7- Fake join announcement");
        messageService.sendRaw(sender, "&b/ac troll fakeleave <player> &7- Fake leave announcement");
        messageService.sendRaw(sender, "&b/ac troll lightning <player> &7- Harmless visual lightning bolt");
        messageService.sendRaw(sender, "&b/ac troll launch <player> &7- Launch upwards with fall protection");
        messageService.sendRaw(sender, "&b/ac troll spin <player> &7- Harmless 5-second rapid spin");
        messageService.sendRaw(sender, "&b/ac troll sound <player> <sound> &7- Play sound effect");
        messageService.sendRaw(sender, "&b/ac troll title <player> <text> &7- Display custom title");
        messageService.sendRaw(sender, "&b/ac troll particles <player> <effect> &7- Display particle burst");
        messageService.sendRaw(sender, "&b/ac troll stop <player> &7- Stop pranks on player");
        messageService.sendRaw(sender, "&b/ac troll clear &7- Immediately cancel all active pranks");
        messageService.sendRaw(sender, "&8&m--------------------------------------------------------");
    }
}
