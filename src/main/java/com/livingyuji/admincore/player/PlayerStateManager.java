/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.player;

import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player states such as Vanish, GodMode, Freeze, StaffMode, and Teleport Back histories.
 *
 * @author LivingYuji
 */
public class PlayerStateManager {

    private final JavaPlugin plugin;
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> godPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();
    private final Map<UUID, StaffModeSession> staffSessions = new ConcurrentHashMap<>();

    public PlayerStateManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setBackLocation(UUID uuid, Location location) {
        if (uuid != null && location != null) {
            backLocations.put(uuid, location.clone());
        }
    }

    public Location getBackLocation(UUID uuid) {
        return backLocations.get(uuid);
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public void setVanished(Player player, boolean vanished) {
        UUID uuid = player.getUniqueId();
        if (vanished) {
            vanishedPlayers.add(uuid);
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.hasPermission(PermissionNode.PLAYER_VANISH.getNode()) && !other.isOp()) {
                    other.hidePlayer(plugin, player);
                }
            }
            player.setAllowFlight(true);
            player.sendMessage(TextUtil.toComponent("&aVanish enabled. You are now invisible to regular players."));
        } else {
            vanishedPlayers.remove(uuid);
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, player);
            }
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            player.sendMessage(TextUtil.toComponent("&cVanish disabled. You are now visible to everyone."));
        }
    }

    public boolean isGod(UUID uuid) {
        return godPlayers.contains(uuid);
    }

    public void setGod(Player player, boolean god) {
        UUID uuid = player.getUniqueId();
        if (god) {
            godPlayers.add(uuid);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.sendMessage(TextUtil.toComponent("&aGod mode enabled. You are invulnerable to damage."));
        } else {
            godPlayers.remove(uuid);
            player.sendMessage(TextUtil.toComponent("&cGod mode disabled."));
        }
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    public void setFrozen(Player target, boolean frozen, String staffName) {
        UUID uuid = target.getUniqueId();
        if (frozen) {
            frozenPlayers.add(uuid);
            target.sendMessage(TextUtil.toComponent("&c&lYOU HAVE BEEN FROZEN BY STAFF! &r&cDo not log out."));
        } else {
            frozenPlayers.remove(uuid);
            target.sendMessage(TextUtil.toComponent("&aYou have been unfrozen by &f" + staffName + "&a."));
        }
    }

    public boolean isInStaffMode(UUID uuid) {
        return staffSessions.containsKey(uuid);
    }

    public void toggleStaffMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInStaffMode(uuid)) {
            disableStaffMode(player);
        } else {
            enableStaffMode(player);
        }
    }

    public void enableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();
        StaffModeSession session = new StaffModeSession(
                player.getGameMode(),
                player.getInventory().getContents(),
                player.getInventory().getArmorContents(),
                player.getLocation(),
                player.isFlying(),
                player.getAllowFlight()
        );
        staffSessions.put(uuid, session);

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        setVanished(player, true);

        // Equip Staff Mode tools
        player.getInventory().setItem(0, createTool(Material.COMPASS, "&b&lTeleport Compass", "&7Right-click to jump to target block"));
        player.getInventory().setItem(1, createTool(Material.BOOK, "&e&lPlayer Inspector", "&7Right-click player to inspect info"));
        player.getInventory().setItem(2, createTool(Material.PACKED_ICE, "&3&lFreeze Wand", "&7Right-click player to freeze/unfreeze"));
        player.getInventory().setItem(4, createTool(Material.CHEST, "&6&lAdmin Dashboard", "&7Right-click to open Admin GUI"));
        player.getInventory().setItem(7, createTool(Material.FEATHER, "&f&lFly Speed Boost", "&7Right-click to toggle flight speed"));
        player.getInventory().setItem(8, createTool(Material.BARRIER, "&c&lExit Staff Mode", "&7Right-click to exit staff mode"));

        player.sendMessage(TextUtil.toComponent("&a&lStaff Mode enabled! &r&7Original inventory and state backed up."));
    }

    public void disableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();
        StaffModeSession session = staffSessions.remove(uuid);
        if (session != null) {
            player.getInventory().clear();
            player.getInventory().setContents(session.getOriginalInventory());
            player.getInventory().setArmorContents(session.getOriginalArmor());
            player.setGameMode(session.getOriginalGameMode());
            player.setAllowFlight(session.wasAllowFlight());
            player.setFlying(session.wasFlying());
            setVanished(player, false);
            player.sendMessage(TextUtil.toComponent("&c&lStaff Mode disabled! &r&7Your original state has been restored."));
        }
    }

    private ItemStack createTool(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.toComponent(name));
            meta.lore(List.of(TextUtil.toComponent(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Restores all staff mode sessions and cleans up during plugin shutdown.
     */
    public void restoreAll() {
        for (UUID uuid : staffSessions.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                disableStaffMode(player);
            }
        }
        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    other.showPlayer(plugin, player);
                }
            }
        }
        vanishedPlayers.clear();
        godPlayers.clear();
        frozenPlayers.clear();
        backLocations.clear();
    }
}
