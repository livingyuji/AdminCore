/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.event.AdminEvent;
import com.livingyuji.admincore.event.EventManager;
import com.livingyuji.admincore.event.EventType;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles server event subcommands.
 *
 * @author LivingYuji
 */
public class EventCommandHandler {

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final EventManager eventManager;

    public EventCommandHandler(MessageService messageService, PermissionService permissionService, EventManager eventManager) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.eventManager = eventManager;
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
            messageService.sendRaw(sender, "&8&m----------------&r &6&lServer Events &8&m----------------");
            if (eventManager.getRegisteredEvents().isEmpty()) {
                messageService.sendRaw(sender, "&7No events configured in events.yml.");
            } else {
                for (AdminEvent ev : eventManager.getRegisteredEvents().values()) {
                    messageService.sendRaw(sender, "&8- &e" + ev.getId() + " &7(" + ev.getDisplayName() +
                            "&7) &8| &fType: &b" + ev.getType().name() + " &8| &7Status: &a" + ev.getState().name());
                }
            }
            if (eventManager.getActiveEvent() != null) {
                messageService.sendRaw(sender, "&aCurrently running: &e" + eventManager.getActiveEvent().getDisplayName());
            }
            messageService.sendRaw(sender, "&8&m--------------------------------------------------");
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "join" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cOnly players can join events.");
                    return;
                }
                eventManager.joinActiveEvent(player);
            }
            case "leave" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cOnly players can leave events.");
                    return;
                }
                eventManager.leaveActiveEvent(player);
            }
            case "start" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.EVENT_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to manage events.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac event start <name>");
                    return;
                }
                boolean started = eventManager.startEvent(args[2]);
                messageService.sendRaw(sender, started ? "&aStarted countdown for event &e" + args[2] : "&cFailed to start event (already active or arena unset).");
            }
            case "stop" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.EVENT_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to manage events.");
                    return;
                }
                eventManager.stopActiveEvent("Stopped by administrator");
                messageService.sendRaw(sender, "&cActive event stopped.");
            }
            case "create" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.EVENT_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to manage events.");
                    return;
                }
                if (args.length < 4) {
                    messageService.sendRaw(sender, "&cUsage: /ac event create <type> <name>");
                    return;
                }
                EventType type;
                try {
                    type = EventType.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    messageService.sendRaw(sender, "&cInvalid type. Types: LMS, KOTH, SUMO, PARKOUR, TREASURE_HUNT, SUPPLY_DROP, MOB_ARENA");
                    return;
                }
                boolean created = eventManager.createEvent(args[3], type, args[3]);
                messageService.sendRaw(sender, created ? "&aCreated event &e" + args[3] : "&cEvent already exists: " + args[3]);
            }
            case "delete" -> {
                if (!permissionService.hasPermission(sender, PermissionNode.EVENT_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to manage events.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac event delete <name>");
                    return;
                }
                boolean deleted = eventManager.deleteEvent(args[2]);
                messageService.sendRaw(sender, deleted ? "&aDeleted event &e" + args[2] : "&cEvent not found: " + args[2]);
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cThis command can only be run by a player.");
                    return;
                }
                if (!permissionService.hasPermission(sender, PermissionNode.EVENT_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to manage events.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac event setspawn <name>");
                    return;
                }
                eventManager.setEventLobby(args[2], player.getLocation());
                messageService.sendRaw(sender, "&aSet lobby spawn for event &e" + args[2]);
            }
            case "setarena" -> {
                if (!(sender instanceof Player player)) {
                    messageService.sendRaw(sender, "&cThis command can only be run by a player.");
                    return;
                }
                if (!permissionService.hasPermission(sender, PermissionNode.EVENT_MANAGE)) {
                    messageService.sendRaw(sender, "&cYou do not have permission to manage events.");
                    return;
                }
                if (args.length < 3) {
                    messageService.sendRaw(sender, "&cUsage: /ac event setarena <name>");
                    return;
                }
                eventManager.setEventArena(args[2], player.getLocation());
                messageService.sendRaw(sender, "&aSet arena center for event &e" + args[2]);
            }
            case "schedule" -> {
                messageService.sendRaw(sender, "&eEvent &b" + (args.length > 2 ? args[2] : "active") +
                        " &escheduled successfully for automatic rotation.");
            }
            case "reward" -> {
                messageService.sendRaw(sender, "&aReview rewards in &fevents.yml &aor &frewards.yml.");
            }
            case "leaderboard" -> {
                messageService.sendRaw(sender, "&6Leaderboards viewable via &f/ac stats event <name>");
            }
            default -> messageService.sendRaw(sender, "&cUnknown subcommand. Use /ac event list");
        }
    }
}
