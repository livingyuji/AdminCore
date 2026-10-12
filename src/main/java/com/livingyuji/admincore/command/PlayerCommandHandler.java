/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.command;

import com.livingyuji.admincore.config.MessageService;
import com.livingyuji.admincore.logging.AuditLogger;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.player.PlayerStateManager;
import com.livingyuji.admincore.util.DurationParser;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles player utility subcommands (teleport, heal, vanish, freeze, etc.).
 *
 * @author LivingYuji
 */
public class PlayerCommandHandler {

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final PlayerStateManager playerStateManager;
    private final AuditLogger auditLogger;

    public PlayerCommandHandler(MessageService messageService, PermissionService permissionService,
                                PlayerStateManager playerStateManager, AuditLogger auditLogger) {
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.playerStateManager = playerStateManager;
        this.auditLogger = auditLogger;
    }

    public void handleInfo(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_INFO)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac info.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(sender, "&cUsage: /ac info <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(sender, "&cPlayer not found or offline: " + args[1]);
            return;
        }

        messageService.sendRaw(sender, "&8&m----------------&r &b&lPlayer Info: " + target.getName() + " &8&m----------------");
        messageService.sendRaw(sender, "&7UUID: &f" + target.getUniqueId());
        messageService.sendRaw(sender, "&7Ping: &f" + target.getPing() + "ms &8| &7Gamemode: &f" + target.getGameMode().name());
        messageService.sendRaw(sender, "&7Health: &c" + (int) target.getHealth() + "/" + (int) target.getMaxHealth() +
                " &8| &7Food: &e" + target.getFoodLevel() + "/20");
        messageService.sendRaw(sender, "&7Flight: &f" + target.getAllowFlight() + " &8| &7God Mode: &f" + playerStateManager.isGod(target.getUniqueId()));
        messageService.sendRaw(sender, "&7Frozen: &f" + playerStateManager.isFrozen(target.getUniqueId()) +
                " &8| &7Vanished: &f" + playerStateManager.isVanished(target.getUniqueId()));
        Location loc = target.getLocation();
        messageService.sendRaw(sender, String.format("&7Location: &f%s &8(%.1f, %.1f, %.1f)", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
        messageService.sendRaw(sender, "&8&m-------------------------------------------------------");
    }

    public void handleInvsee(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_INVENTORY)) {
            messageService.sendRaw(staff, "&cYou do not have permission for /ac invsee.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(staff, "&cUsage: /ac invsee <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(staff, "&cPlayer not found: " + args[1]);
            return;
        }

        staff.openInventory(target.getInventory());
        messageService.sendRaw(staff, "&aOpened inventory of &e" + target.getName());
    }

    public void handleEndersee(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_INVENTORY)) {
            messageService.sendRaw(staff, "&cYou do not have permission for /ac endersee.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(staff, "&cUsage: /ac endersee <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(staff, "&cPlayer not found: " + args[1]);
            return;
        }

        staff.openInventory(target.getEnderChest());
        messageService.sendRaw(staff, "&aOpened ender chest of &e" + target.getName());
    }

    public void handleTp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_TELEPORT)) {
            messageService.sendRaw(staff, "&cYou do not have permission for teleportation.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(staff, "&cUsage: /ac tp <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(staff, "&cPlayer not found: " + args[1]);
            return;
        }

        staff.teleport(target.getLocation());
        messageService.sendRaw(staff, "&aTeleported to &e" + target.getName());
    }

    public void handleTphere(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_TELEPORT)) {
            messageService.sendRaw(staff, "&cYou do not have permission for teleportation.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(staff, "&cUsage: /ac tphere <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(staff, "&cPlayer not found: " + args[1]);
            return;
        }

        target.teleport(staff.getLocation());
        messageService.sendRaw(staff, "&aTeleported &e" + target.getName() + " &ato you.");
    }

    public void handleTpall(CommandSender sender) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_TELEPORT)) {
            messageService.sendRaw(staff, "&cYou do not have permission for /ac tpall.");
            return;
        }

        int count = 0;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(staff)) {
                other.teleport(staff.getLocation());
                count++;
            }
        }
        messageService.sendRaw(staff, "&aTeleported &e" + count + " &aplayers to your location.");
    }

    public void handleBack(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(player, PermissionNode.PLAYER_TELEPORT)) {
            messageService.sendRaw(player, "&cYou do not have permission for /ac back.");
            return;
        }

        Location backLoc = playerStateManager.getBackLocation(player.getUniqueId());
        if (backLoc == null) {
            messageService.sendRaw(player, "&cNo previous location recorded to return to.");
            return;
        }

        player.teleport(backLoc);
        messageService.sendRaw(player, "&aReturned to previous location.");
    }

    public void handleHeal(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_HEAL)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac heal.");
            return;
        }

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messageService.sendRaw(sender, "&cSpecify an online player: /ac heal <player>");
            return;
        }

        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setFireTicks(0);
        messageService.sendRaw(sender, "&aHealed &e" + target.getName());
    }

    public void handleFeed(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_HEAL)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac feed.");
            return;
        }

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messageService.sendRaw(sender, "&cSpecify an online player: /ac feed <player>");
            return;
        }

        target.setFoodLevel(20);
        messageService.sendRaw(sender, "&aFed &e" + target.getName());
    }

    public void handleFly(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_FLY)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac fly.");
            return;
        }

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messageService.sendRaw(sender, "&cSpecify an online player: /ac fly <player>");
            return;
        }

        boolean allow = !target.getAllowFlight();
        target.setAllowFlight(allow);
        if (!allow) target.setFlying(false);
        messageService.sendRaw(sender, "&eFlight for &f" + target.getName() + " &eset to: &f" + (allow ? "&aEnabled" : "&cDisabled"));
    }

    public void handleGod(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_GOD)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac god.");
            return;
        }

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messageService.sendRaw(sender, "&cSpecify an online player: /ac god <player>");
            return;
        }

        boolean current = playerStateManager.isGod(target.getUniqueId());
        playerStateManager.setGod(target, !current);
        messageService.sendRaw(sender, "&eGod mode for &f" + target.getName() + " &eset to: &f" + (!current ? "&aEnabled" : "&cDisabled"));
    }

    public void handleSpeed(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_SPEED)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac speed.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(sender, "&cUsage: /ac speed <1-10> [player]");
            return;
        }

        int speedVal;
        try {
            speedVal = Integer.parseInt(args[1]);
            if (speedVal < 1 || speedVal > 10) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            messageService.sendRaw(sender, "&cSpeed value must be between 1 and 10.");
            return;
        }

        Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messageService.sendRaw(sender, "&cSpecify an online player: /ac speed <1-10> [player]");
            return;
        }

        float bukkitSpeed = speedVal / 10.0f * 0.2f;
        if (target.isFlying()) {
            target.setFlySpeed(bukkitSpeed);
            messageService.sendRaw(sender, "&eFly speed of &f" + target.getName() + " &eset to: &f" + speedVal);
        } else {
            target.setWalkSpeed(bukkitSpeed);
            messageService.sendRaw(sender, "&eWalk speed of &f" + target.getName() + " &eset to: &f" + speedVal);
        }
    }

    public void handleVanish(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_VANISH)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac vanish.");
            return;
        }

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            messageService.sendRaw(sender, "&cSpecify an online player: /ac vanish <player>");
            return;
        }

        boolean vanished = !playerStateManager.isVanished(target.getUniqueId());
        playerStateManager.setVanished(target, vanished);
        messageService.sendRaw(sender, "&eVanish for &f" + target.getName() + " &eset to: &f" + (vanished ? "&aEnabled" : "&cDisabled"));
    }

    public void handleFreeze(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_FREEZE)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac freeze.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(sender, "&cUsage: /ac freeze <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(sender, "&cPlayer not found: " + args[1]);
            return;
        }

        boolean frozen = !playerStateManager.isFrozen(target.getUniqueId());
        playerStateManager.setFrozen(target, frozen, sender.getName());
        messageService.sendRaw(sender, "&eFreeze state of &f" + target.getName() + " &eset to: &f" + (frozen ? "&aFrozen" : "&cUnfrozen"));
    }

    public void handleSpectate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_SPECTATE)) {
            messageService.sendRaw(staff, "&cYou do not have permission for /ac spectate.");
            return;
        }
        if (args.length < 2) {
            messageService.sendRaw(staff, "&cUsage: /ac spectate <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(staff, "&cPlayer not found: " + args[1]);
            return;
        }

        staff.setGameMode(GameMode.SPECTATOR);
        staff.setSpectatorTarget(target);
        messageService.sendRaw(staff, "&aNow spectating &e" + target.getName());
    }

    public void handleEffect(CommandSender sender, String[] args) {
        if (!permissionService.hasPermission(sender, PermissionNode.PLAYER_EFFECT)) {
            messageService.sendRaw(sender, "&cYou do not have permission for /ac effect.");
            return;
        }
        if (args.length < 4) {
            messageService.sendRaw(sender, "&cUsage: /ac effect <player> <effect> <duration>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messageService.sendRaw(sender, "&cPlayer not found: " + args[1]);
            return;
        }

        PotionEffectType type = PotionEffectType.getByName(args[2].toUpperCase());
        if (type == null) {
            messageService.sendRaw(sender, "&cInvalid potion effect: " + args[2]);
            return;
        }

        long millis = DurationParser.parseDurationMillis(args[3]);
        int ticks = millis > 0 ? (int) (millis / 50L) : 600;

        target.addPotionEffect(new PotionEffect(type, ticks, 1));
        messageService.sendRaw(sender, "&aApplied &e" + type.getName() + " &ato &f" + target.getName());
    }

    public void handleStaffMode(CommandSender sender) {
        if (!(sender instanceof Player staff)) {
            messageService.sendRaw(sender, "&cThis command can only be run by a player.");
            return;
        }
        if (!permissionService.hasPermission(staff, PermissionNode.PLAYER_STAFFMODE)) {
            messageService.sendRaw(staff, "&cYou do not have permission for staff mode.");
            return;
        }

        playerStateManager.toggleStaffMode(staff);
    }
}
