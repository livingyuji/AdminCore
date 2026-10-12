/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.cosmetics.CosmeticDefinition;
import com.livingyuji.admincore.cosmetics.CosmeticsManager;
import com.livingyuji.admincore.gui.GuiManager;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles player cosmetics subcommands.
 *
 * @author LivingYuji
 */
public class CosmeticsCommandHandler {

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final CosmeticsManager cosmeticsManager;
    private final GuiManager guiManager;

    public CosmeticsCommandHandler(MessageService messageService, PermissionService permissionService,
                                   CosmeticsManager cosmeticsManager, GuiManager guiManager) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.cosmeticsManager = cosmeticsManager;
        this.guiManager = guiManager;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.COSMETICS_USE)) {
            messageService.sendRaw(sender, "&cYou do not have permission for cosmetics.");
            return;
        }

        if (args.length < 2) {
            if (sender instanceof Player player) {
                guiManager.openCosmeticsManager(player);
            } else {
                messageService.sendRaw(sender, "&cConsole usage: /ac cosmetics <give|clear|reload>");
            }
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "trails", "aura", "kill", "join" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cThis command can only be executed by a player.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(player, "&cUsage: /ac cosmetics " + sub + " <id>");
                    return;
                }
                cosmeticsManager.equipCosmetic(player, args[2]);
            }
            case "clear" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cThis command can only be executed by a player.");
                    return;
                }
                cosmeticsManager.clearCosmetics(player);
            }
            case "give" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.COSMETICS_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to give cosmetics.");
                    return;
                }
                if (args.length < 4) {
                    messageService.sendRaw(sender, "&cUsage: /ac cosmetics give <player> <id>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    messageService.sendRaw(sender, "&cPlayer not found: " + args[2]);
                    return;
                }
                boolean equipped = cosmeticsManager.equipCosmetic(target, args[3]);
                messageService.sendRaw(sender, equipped ? "&aGave cosmetic &e" + args[3] + " &ato &f" + target.getName() : "&cCosmetic not found: " + args[3]);
            }
            case "reload" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.COSMETICS_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to reload cosmetics.");
                    return;
                }
                cosmeticsManager.loadCosmetics();
                messageService.sendRaw(sender, "&aCosmetics configurations reloaded.");
            }
            default -> {
                if (sender instanceof Player player) {
                    guiManager.openCosmeticsManager(player);
                } else {
                    messageService.sendRaw(sender, "&cUnknown cosmetics subcommand.");
                }
            }
        }
    }
}
