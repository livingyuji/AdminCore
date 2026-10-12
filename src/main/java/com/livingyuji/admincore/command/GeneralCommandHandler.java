/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.discord.DiscordWebhookService;
import com.livingyuji.admincore.gui.GuiManager;
import com.livingyuji.admincore.logging.AuditLogger;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Handles general plugin administration subcommands.
 *
 * @author LivingYuji
 */
public class GeneralCommandHandler {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageService messageService;
    private final PermissionService permissionService;
    private final GuiManager guiManager;
    private final AuditLogger auditLogger;
    private final DiscordWebhookService discordService;

    public GeneralCommandHandler(JavaPlugin plugin, ConfigManager configManager, MessageService messageService,
                                 PermissionService permissionService, GuiManager guiManager,
                                 AuditLogger auditLogger, DiscordWebhookService discordService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.guiManager = guiManager;
        this.auditLogger = auditLogger;
        this.discordService = discordService;
    }

    public void handleHelp(CommandSender sender, String[] args) {
        String category = args.length > 1 ? args[1].toLowerCase() : "all";

        messageService.sendRaw(sender, "&8&m====================&r &b&lAdminCore Help &8&m====================");
        messageService.sendRaw(sender, "&7Developed and credited to &eLivingYuji &7| Version &f1.0.0");

        if (category.equals("all") || category.equals("general")) {
            messageService.sendRaw(sender, "&b/ac &7- Open main admin control GUI");
            messageService.sendRaw(sender, "&b/ac help [category] &7- Display help categories");
            messageService.sendRaw(sender, "&b/ac reload &7- Reload plugin configuration files");
            messageService.sendRaw(sender, "&b/ac version &7- View plugin version & credits");
            messageService.sendRaw(sender, "&b/ac status &7- View active modules & performance");
        }

        if (category.equals("all") || category.equals("player")) {
            messageService.sendRaw(sender, "&e--- Player Utilities ---");
            messageService.sendRaw(sender, "&b/ac info <player> &7- Inspect player details");
            messageService.sendRaw(sender, "&b/ac invsee <player> &7- Inspect player inventory");
            messageService.sendRaw(sender, "&b/ac tp / tphere / tpall / back &7- Teleport utilities");
            messageService.sendRaw(sender, "&b/ac heal / feed / fly / god / vanish / freeze / staffmode");
        }

        if (category.equals("all") || category.equals("moderation")) {
            messageService.sendRaw(sender, "&e--- Moderation ---");
            messageService.sendRaw(sender, "&b/ac warn / mute / unmute / kick / ban / tempban / unban");
            messageService.sendRaw(sender, "&b/ac history <player> &7- View punishments");
            messageService.sendRaw(sender, "&b/ac report / reports &7- Submit & review reports");
        }

        if (category.equals("all") || category.equals("boss") || category.equals("event")) {
            messageService.sendRaw(sender, "&e--- Bosses & Events ---");
            messageService.sendRaw(sender, "&b/ac boss list / spawn / remove / killall");
            messageService.sendRaw(sender, "&b/ac event list / create / start / stop / join / leave");
        }

        messageService.sendRaw(sender, "&8&m=====================================================");
    }

    public void handleVersion(CommandSender sender) {
        messageService.sendRaw(sender, "&b&lAdminCore &fv1.0.0");
        messageService.sendRaw(sender, "&7All-in-one administration, events, moderation, and player utilities.");
        messageService.sendRaw(sender, "&7Developed and credited entirely to: &eLivingYuji");
        messageService.sendRaw(sender, "&7Platform: &fPaper 1.21.1 &8| &7Java: &fJDK 21");
    }

    public void handleStatus(CommandSender sender) {
        if (!permissionService.hasPermission(sender, PermissionNode.STATUS)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac status.");
            return;
        }

        messageService.sendRaw(sender, "&8&m----------------&r &b&lAdminCore Status &8&m----------------");
        messageService.sendRaw(sender, "&7Players Online: &f" + Bukkit.getOnlinePlayers().size());
        messageService.sendRaw(sender, "&7Allocated Memory: &f" + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB");
        messageService.sendRaw(sender, "&7Free Memory: &f" + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB");
        messageService.sendRaw(sender, "&7Discord Webhook: &f" + (discordService.isEnabled() ? "&aEnabled" : "&cDisabled"));
        messageService.sendRaw(sender, "&7Debug Mode: &f" + (configManager.isDebugEnabled() ? "&aActive" : "&cInactive"));
        messageService.sendRaw(sender, "&8&m------------------------------------------------");
    }

    public void handleReload(CommandSender sender) {
        if (!permissionService.hasPermission(sender, PermissionNode.RELOAD)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac reload.");
            return;
        }

        configManager.reloadAll();
        auditLogger.log(sender.getName(), "RELOAD", "Reloaded AdminCore configuration files");
        messageService.sendRaw(sender, "&aAdminCore configurations have been reloaded successfully.");
    }

    public void handleDebug(CommandSender sender) {
        if (!permissionService.hasPermission(sender, PermissionNode.DEBUG)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac debug.");
            return;
        }

        boolean current = configManager.isDebugEnabled();
        configManager.getMainConfig().set("debug-mode", !current);
        configManager.saveFile("config.yml");
        messageService.sendRaw(sender, "&eDebug mode is now: &f" + (!current ? "&aEnabled" : "&cDisabled"));
    }

    public void handleLogs(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.LOGS_VIEW)) {
            messageService.sendRaw(sender, "&cYou do not have permission to view audit logs.");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("export")) {
            if (!permissionService.hasPermission(sender, PermissionNode.LOGS_EXPORT)) {
                messageService.sendRaw(sender, "&cYou do not have permission to export logs.");
                return;
            }
            try {
                File file = auditLogger.exportLogs();
                messageService.sendRaw(sender, "&aAudit logs successfully exported to: &f" + file.getName());
            } catch (Exception e) {
                messageService.sendRaw(sender, "&cFailed to export logs: " + e.getMessage());
            }
            return;
        }

        String filter = args.length > 2 && args[1].equalsIgnoreCase("search") ? args[2] : (args.length > 1 ? args[1] : "");
        List<String> logs = auditLogger.searchLogs(filter, 8);
        messageService.sendRaw(sender, "&8&m----------------&r &b&lAudit Logs &8&m----------------");
        if (logs.isEmpty()) {
            messageService.sendRaw(sender, "&7No audit records found matching query.");
        } else {
            for (String line : logs) {
                messageService.sendRaw(sender, "&7" + line);
            }
        }
        messageService.sendRaw(sender, "&8&m-----------------------------------------");
    }

    public void handleDiscord(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.DISCORD_MANAGE)) {
            messageService.sendRaw(sender, "&cYou do not have permission for Discord management.");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
            if (!discordService.isEnabled()) {
                messageService.sendRaw(sender, "&cDiscord integration is currently disabled in config.yml.");
                return;
            }
            discordService.sendEmbed("system", "AdminCore Test Webhook",
                    "Discord webhook integration test triggered by " + sender.getName(), 0x00FF00);
            messageService.sendRaw(sender, "&aDiscord test notification dispatched.");
            return;
        }

        messageService.sendRaw(sender, "&bDiscord Status: &f" + (discordService.isEnabled() ? "&aEnabled" : "&cDisabled"));
    }
}
