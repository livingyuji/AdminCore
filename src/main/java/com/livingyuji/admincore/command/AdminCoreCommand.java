/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.gui.GuiManager;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command dispatcher and tab completer for /ac and /admincore.
 *
 * @author LivingYuji
 */
public class AdminCoreCommand implements CommandExecutor, TabCompleter {

    private final PermissionService permissionService;
    private final GuiManager guiManager;
    private final GeneralCommandHandler generalHandler;
    private final PlayerCommandHandler playerHandler;
    private final TrollCommandHandler trollHandler;
    private final BossCommandHandler bossHandler;
    private final EventCommandHandler eventHandler;
    private final CosmeticsCommandHandler cosmeticsHandler;
    private final ModerationCommandHandler moderationHandler;

    private static final List<String> ALL_SUBCOMMANDS = Arrays.asList(
            "help", "gui", "version", "status", "modules", "reload", "debug",
            "info", "invsee", "endersee", "tp", "tphere", "tpall", "back",
            "heal", "feed", "fly", "god", "speed", "vanish", "freeze", "spectate", "effect", "staffmode",
            "troll", "boss", "event", "cosmetics",
            "warn", "mute", "unmute", "kick", "ban", "tempban", "unban", "history", "note", "report", "reports",
            "logs", "stats", "discord"
    );

    public AdminCoreCommand(PermissionService permissionService, GuiManager guiManager,
                            GeneralCommandHandler generalHandler, PlayerCommandHandler playerHandler,
                            TrollCommandHandler trollHandler, BossCommandHandler bossHandler,
                            EventCommandHandler eventHandler, CosmeticsCommandHandler cosmeticsHandler,
                            ModerationCommandHandler moderationHandler) {
        this.permissionService = permissionService;
        this.guiManager = guiManager;
        this.generalHandler = generalHandler;
        this.playerHandler = playerHandler;
        this.trollHandler = trollHandler;
        this.bossHandler = bossHandler;
        this.eventHandler = eventHandler;
        this.cosmeticsHandler = cosmeticsHandler;
        this.moderationHandler = moderationHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                guiManager.openMainDashboard(player);
            } else {
                generalHandler.handleHelp(sender, args);
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            // General
            case "help" -> generalHandler.handleHelp(sender, args);
            case "gui" -> {
                if (sender instanceof Player p) guiManager.openMainDashboard(p);
                else sender.sendMessage(TextUtil.toComponent("&cOnly in-game players can open the GUI dashboard."));
            }
            case "version" -> generalHandler.handleVersion(sender);
            case "status", "modules" -> generalHandler.handleStatus(sender);
            case "reload" -> generalHandler.handleReload(sender);
            case "debug" -> generalHandler.handleDebug(sender);
            case "logs" -> generalHandler.handleLogs(sender, args);
            case "discord" -> generalHandler.handleDiscord(sender, args);

            // Player utilities
            case "info" -> playerHandler.handleInfo(sender, args);
            case "invsee" -> playerHandler.handleInvsee(sender, args);
            case "endersee" -> playerHandler.handleEndersee(sender, args);
            case "tp" -> playerHandler.handleTp(sender, args);
            case "tphere" -> playerHandler.handleTphere(sender, args);
            case "tpall" -> playerHandler.handleTpall(sender);
            case "back" -> playerHandler.handleBack(sender);
            case "heal" -> playerHandler.handleHeal(sender, args);
            case "feed" -> playerHandler.handleFeed(sender, args);
            case "fly" -> playerHandler.handleFly(sender, args);
            case "god" -> playerHandler.handleGod(sender, args);
            case "speed" -> playerHandler.handleSpeed(sender, args);
            case "vanish" -> playerHandler.handleVanish(sender, args);
            case "freeze" -> playerHandler.handleFreeze(sender, args);
            case "spectate" -> playerHandler.handleSpectate(sender, args);
            case "effect" -> playerHandler.handleEffect(sender, args);
            case "staffmode" -> playerHandler.handleStaffMode(sender);

            // Submodules
            case "troll" -> trollHandler.handle(sender, args);
            case "boss" -> bossHandler.handle(sender, args);
            case "event" -> eventHandler.handle(sender, args);
            case "cosmetics" -> cosmeticsHandler.handle(sender, args);

            // Moderation
            case "warn" -> moderationHandler.handleWarn(sender, args);
            case "mute" -> moderationHandler.handleMute(sender, args);
            case "unmute" -> moderationHandler.handleUnmute(sender, args);
            case "kick" -> moderationHandler.handleKick(sender, args);
            case "ban" -> moderationHandler.handleBan(sender, args);
            case "tempban" -> moderationHandler.handleTempban(sender, args);
            case "unban" -> moderationHandler.handleUnban(sender, args);
            case "history" -> moderationHandler.handleHistory(sender, args);
            case "note" -> moderationHandler.handleNote(sender, args);
            case "report" -> moderationHandler.handleReport(sender, args);
            case "reports" -> moderationHandler.handleReports(sender);

            default -> {
                sender.sendMessage(TextUtil.toComponent("&cUnknown command: &f" + sub + "&c. Type &e/ac help &cfor assistance."));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return ALL_SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "help" -> {
                    return filter(List.of("general", "player", "moderation", "boss", "event", "all"), args[1]);
                }
                case "troll" -> {
                    return filter(List.of("help", "fakejoin", "fakeleave", "lightning", "launch", "spin", "sound", "title", "particles", "stop", "clear"), args[1]);
                }
                case "boss" -> {
                    return filter(List.of("list", "info", "spawn", "remove", "setspawn", "reload", "killall"), args[1]);
                }
                case "event" -> {
                    return filter(List.of("list", "create", "start", "stop", "join", "leave", "setspawn", "setarena", "schedule", "reward", "leaderboard", "delete"), args[1]);
                }
                case "cosmetics" -> {
                    return filter(List.of("trails", "aura", "kill", "join", "clear", "give", "reload"), args[1]);
                }
                case "discord" -> {
                    return filter(List.of("test", "status"), args[1]);
                }
                case "logs" -> {
                    return filter(List.of("search", "export"), args[1]);
                }
                case "info", "invsee", "endersee", "tp", "tphere", "heal", "feed", "fly", "god", "vanish", "freeze",
                        "spectate", "warn", "mute", "unmute", "kick", "ban", "tempban", "unban", "history", "note", "report" -> {
                    return onlinePlayerNames(args[1]);
                }
                case "speed" -> {
                    return filter(List.of("1", "2", "3", "5", "10"), args[1]);
                }
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("event") && args[1].equalsIgnoreCase("create")) {
                return filter(List.of("LMS", "KOTH", "SUMO", "PARKOUR", "TREASURE_HUNT", "SUPPLY_DROP", "MOB_ARENA"), args[2]);
            }
            if (sub.equals("mute") || sub.equals("tempban")) {
                return filter(List.of("10m", "1h", "1d", "7d", "30d"), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> onlinePlayerNames(String prefix) {
        String p = prefix.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(p))
                .collect(Collectors.toList());
    }

    private List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase();
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(p))
                .collect(Collectors.toList());
    }
}
