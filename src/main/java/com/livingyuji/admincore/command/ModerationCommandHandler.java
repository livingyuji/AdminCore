/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.discord.DiscordWebhookService;
import com.livingyuji.admincore.logging.AuditLogger;
import com.livingyuji.admincore.moderation.ModerationManager;
import com.livingyuji.admincore.moderation.PlayerReport;
import com.livingyuji.admincore.moderation.Punishment;
import com.livingyuji.admincore.moderation.StaffNote;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.util.DurationParser;
import com.livingyuji.admincore.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Handles moderation, punishment, and reporting commands.
 *
 * @author LivingYuji
 */
public class ModerationCommandHandler {

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final ModerationManager moderationManager;
    private final AuditLogger auditLogger;
    private final DiscordWebhookService discordService;

    public ModerationCommandHandler(MessageService messageService, PermissionService permissionService,
                                    ModerationManager moderationManager, AuditLogger auditLogger,
                                    DiscordWebhookService discordService) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.moderationManager = moderationManager;
        this.auditLogger = auditLogger;
        this.discordService = discordService;
    }

    public void handleWarn(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_WARN)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac warn.");
            return;
        }
        if (args.length < 3) {
            messageService.sendRaw(sender, "&cUsage: /ac warn <player> <reason>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;

        moderationManager.warn(target.getUniqueId(), target.getName() != null ? target.getName() : args[1],
                staffUuid, sender.getName(), reason);

        auditLogger.log(sender.getName(), "WARN", "Warned " + target.getName() + " for: " + reason);
        discordService.sendEmbed("punishments", "Player Warned",
                "Target: " + target.getName() + "\nStaff: " + sender.getName() + "\nReason: " + reason, 0xFFA500);

        messageService.sendRaw(sender, "&aWarned player &e" + target.getName() + " &afor: &f" + reason);
    }

    public void handleMute(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_MUTE)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac mute.");
            return;
        }
        if (args.length < 4) {
            messageService.sendRaw(sender, "&cUsage: /ac mute <player> <duration> <reason>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long duration = DurationParser.parseDurationMillis(args[2]);
        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;

        moderationManager.mute(target.getUniqueId(), target.getName() != null ? target.getName() : args[1],
                staffUuid, sender.getName(), reason, duration);

        String durStr = duration > 0 ? DurationParser.formatDuration(duration) : "Permanent";
        auditLogger.log(sender.getName(), "MUTE", "Muted " + target.getName() + " (" + durStr + "): " + reason);
        discordService.sendEmbed("punishments", "Player Muted",
                "Target: " + target.getName() + "\nStaff: " + sender.getName() + "\nDuration: " + durStr + "\nReason: " + reason, 0xFF5555);

        messageService.sendRaw(sender, "&aMuted player &e" + target.getName() + " &afor &f" + durStr + "&a. Reason: &f" + reason);
    }

    public void handleUnmute(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_MUTE)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac unmute.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(sender, "&cUsage: /ac unmute <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean unmuted = moderationManager.unmute(target.getUniqueId(), sender.getName());
        auditLogger.log(sender.getName(), "UNMUTE", "Unmuted " + target.getName());
        messageService.sendRaw(sender, unmuted ? "&aUnmuted player &e" + target.getName() : "&ePlayer had no active mute or was already unmuted.");
    }

    public void handleKick(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_KICK)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac kick.");
            return;
        }
        if (args.length < 3) {
            messageService.sendRaw(sender, "&cUsage: /ac kick <player> <reason>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(sender, "&cPlayer not found: " + args[1]);
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        moderationManager.kick(target, sender.getName(), reason);

        auditLogger.log(sender.getName(), "KICK", "Kicked " + target.getName() + " for: " + reason);
        discordService.sendEmbed("punishments", "Player Kicked",
                "Target: " + target.getName() + "\nStaff: " + sender.getName() + "\nReason: " + reason, 0xFF3333);

        messageService.sendRaw(sender, "&aKicked &e" + target.getName() + " &afor: &f" + reason);
    }

    public void handleBan(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_BAN)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac ban.");
            return;
        }
        if (args.length < 3) {
            messageService.sendRaw(sender, "&cUsage: /ac ban <player> <reason>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;

        moderationManager.ban(target.getUniqueId(), target.getName() != null ? target.getName() : args[1],
                staffUuid, sender.getName(), reason, -1);

        auditLogger.log(sender.getName(), "BAN", "Permanently banned " + target.getName() + ": " + reason);
        discordService.sendEmbed("punishments", "Player Permanently Banned",
                "Target: " + target.getName() + "\nStaff: " + sender.getName() + "\nReason: " + reason, 0xCC0000);

        messageService.sendRaw(sender, "&aPermanently banned player &e" + target.getName() + " &afor: &f" + reason);
    }

    public void handleTempban(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_BAN)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac tempban.");
            return;
        }
        if (args.length < 4) {
            messageService.sendRaw(sender, "&cUsage: /ac tempban <player> <duration> <reason>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long duration = DurationParser.parseDurationMillis(args[2]);
        if (duration <= 0) {
            messageService.sendRaw(sender, "&cInvalid duration specified. Example: 1d, 7d, 30d");
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;

        moderationManager.ban(target.getUniqueId(), target.getName() != null ? target.getName() : args[1],
                staffUuid, sender.getName(), reason, duration);

        String durStr = DurationParser.formatDuration(duration);
        auditLogger.log(sender.getName(), "TEMPBAN", "Tempbanned " + target.getName() + " (" + durStr + "): " + reason);
        discordService.sendEmbed("punishments", "Player Temporarily Banned",
                "Target: " + target.getName() + "\nStaff: " + sender.getName() + "\nDuration: " + durStr + "\nReason: " + reason, 0xAA0000);

        messageService.sendRaw(sender, "&aTemporarily banned player &e" + target.getName() + " &afor &f" + durStr + "&a. Reason: &f" + reason);
    }

    public void handleUnban(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_BAN)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac unban.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(sender, "&cUsage: /ac unban <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        boolean unbanned = moderationManager.unban(target.getUniqueId(), sender.getName());
        auditLogger.log(sender.getName(), "UNBAN", "Unbanned " + target.getName());
        messageService.sendRaw(sender, unbanned ? "&aUnbanned player &e" + target.getName() : "&ePlayer had no active ban or was already unbanned.");
    }

    public void handleHistory(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_HISTORY)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac history.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(sender, "&cUsage: /ac history <player>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        List<Punishment> history = moderationManager.getHistory(target.getUniqueId());

        messageService.sendRaw(sender, "&8&m----------------&r &c&lPunishment History: " + target.getName() + " &8&m----------------");
        if (history.isEmpty()) {
            messageService.sendRaw(sender, "&aClean record! No punishments found.");
        } else {
            for (Punishment p : history) {
                String dur = p.isPermanent() ? "Permanent" : (p.getExpiresAt() > 0 ? DurationParser.formatDuration(p.getExpiresAt() - p.getCreatedAt()) : "N/A");
                messageService.sendRaw(sender, String.format("&8[%s] &e%s &7by &f%s &7| &e%s &7| &f%s",
                        TimeUtil.formatTimestamp(p.getCreatedAt()), p.getType().name(), p.getStaffName(), dur, p.getReason()));
            }
        }
        messageService.sendRaw(sender, "&8&m------------------------------------------------------------");
    }

    public void handleNote(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_NOTES)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac note.");
            return;
        }
        if (args.length < 3) {
            messageService.sendRaw(sender, "&cUsage: /ac note <player> <text>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String note = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        UUID staffUuid = sender instanceof Player p ? p.getUniqueId() : null;

        moderationManager.addNote(target.getUniqueId(), staffUuid, sender.getName(), note);
        messageService.sendRaw(sender, "&aAdded staff note for &e" + target.getName() + "&a: &f" + note);
    }

    public void handleReport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player reporter)) {
            messageService.sendRaw(sender, "&cOnly online players can submit reports.");
            return;
        }
        if (args.length < 3) {
            messageService.sendRaw(sender, "&cUsage: /ac report <player> <reason>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(sender, "&cPlayer not found: " + args[1]);
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        moderationManager.submitReport(reporter.getUniqueId(), reporter.getName(), target.getUniqueId(), target.getName(), reason);

        // Notify online staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (permissionService.hasPermission(staff, PermissionNode.MODERATION_REPORTS)) {
                messageService.sendRaw(staff, "&c&l[NEW REPORT]&r &e" + reporter.getName() + " &7reported &f" +
                        target.getName() + " &7for: &e" + reason);
            }
        }

        messageService.sendRaw(reporter, "&aYour report against &e" + target.getName() + " &ahas been submitted to staff.");
    }

    public void handleReports(CommandSender sender) {
        if (!permissionService.hasPermission(sender, PermissionNode.MODERATION_REPORTS)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac reports.");
            return;
        }

        List<PlayerReport> reports = moderationManager.getReports("OPEN");
        messageService.sendRaw(sender, "&8&m----------------&r &c&lOpen Player Reports &8&m----------------");
        if (reports.isEmpty()) {
            messageService.sendRaw(sender, "&aNo open reports waiting in queue.");
        } else {
            for (PlayerReport r : reports) {
                messageService.sendRaw(sender, String.format("&8[#%d] &e%s &7reported &f%s &7for: &e%s",
                        r.getId(), r.getReporterName(), r.getReportedName(), r.getReason()));
            }
        }
        messageService.sendRaw(sender, "&8&m------------------------------------------------------");
    }
}
