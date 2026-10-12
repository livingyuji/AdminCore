/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.player;

import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Event listener enforcing frozen restrictions, god mode protection, vanish hiding,
 * staff mode item shortcuts, and location history tracking.
 *
 * @author LivingYuji
 */
public class PlayerListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerStateManager playerStateManager;

    public PlayerListener(JavaPlugin plugin, PlayerStateManager playerStateManager) {
        this.plugin = plugin;
        this.playerStateManager = playerStateManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        playerStateManager.setBackLocation(player.getUniqueId(), player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        // Record prior location for /back
        playerStateManager.setBackLocation(player.getUniqueId(), event.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (playerStateManager.isFrozen(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setTo(from.setDirection(to.getDirection()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if (playerStateManager.isGod(uuid) || playerStateManager.isInStaffMode(uuid) || playerStateManager.isFrozen(uuid)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if (playerStateManager.isFrozen(uuid)) {
                event.setCancelled(true);
                player.sendMessage(TextUtil.toComponent("&cYou cannot attack while frozen!"));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        // Hide vanished players from joining players who lack permission
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (playerStateManager.isVanished(online.getUniqueId())) {
                if (!joined.hasPermission(PermissionNode.PLAYER_VANISH.getNode()) && !joined.isOp()) {
                    joined.hidePlayer(plugin, online);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerStateManager.isInStaffMode(player.getUniqueId())) {
            playerStateManager.disableStaffMode(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!playerStateManager.isInStaffMode(player.getUniqueId())) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.COMPASS) {
                Block targetBlock = player.getTargetBlockExact(100);
                if (targetBlock != null) {
                    player.teleport(targetBlock.getLocation().add(0.5, 1.0, 0.5));
                    player.sendMessage(TextUtil.toComponent("&aTeleported to target block!"));
                }
            } else if (item.getType() == Material.FEATHER) {
                float currentSpeed = player.getFlySpeed();
                float nextSpeed = currentSpeed >= 0.3f ? 0.1f : 0.4f;
                player.setFlySpeed(nextSpeed);
                player.sendMessage(TextUtil.toComponent("&eFly speed set to: &f" + (nextSpeed == 0.1f ? "Normal (1)" : "Fast (4)")));
            } else if (item.getType() == Material.BARRIER) {
                playerStateManager.disableStaffMode(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player staff = event.getPlayer();
        if (!playerStateManager.isInStaffMode(staff.getUniqueId())) {
            return;
        }

        if (event.getRightClicked() instanceof Player target) {
            ItemStack item = staff.getInventory().getItemInMainHand();
            if (item.getType() == Material.PACKED_ICE) {
                boolean wasFrozen = playerStateManager.isFrozen(target.getUniqueId());
                playerStateManager.setFrozen(target, !wasFrozen, staff.getName());
                staff.sendMessage(TextUtil.toComponent((!wasFrozen ? "&aFrozen " : "&cUnfrozen ") + target.getName()));
                event.setCancelled(true);
            } else if (item.getType() == Material.BOOK) {
                staff.performCommand("ac info " + target.getName());
                event.setCancelled(true);
            }
        }
    }
}
