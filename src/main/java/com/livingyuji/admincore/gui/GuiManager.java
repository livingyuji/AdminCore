/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.gui;

import com.livingyuji.admincore.boss.BossConfig;
import com.livingyuji.admincore.boss.BossManager;
import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.cosmetics.CosmeticDefinition;
import com.livingyuji.admincore.cosmetics.CosmeticsManager;
import com.livingyuji.admincore.event.AdminEvent;
import com.livingyuji.admincore.event.EventManager;
import com.livingyuji.admincore.logging.AuditLogger;
import com.livingyuji.admincore.moderation.ModerationManager;
import com.livingyuji.admincore.permission.PermissionNode;
import com.livingyuji.admincore.permission.PermissionService;
import com.livingyuji.admincore.player.PlayerStateManager;
import com.livingyuji.admincore.troll.TrollManager;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Robust Inventory GUI engine powering the Admin Control Center dashboard.
 *
 * @author LivingYuji
 */
public class GuiManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final PermissionService permissionService;
    private final PlayerStateManager playerStateManager;
    private final ModerationManager moderationManager;
    private final TrollManager trollManager;
    private final BossManager bossManager;
    private final EventManager eventManager;
    private final CosmeticsManager cosmeticsManager;
    private final AuditLogger auditLogger;

    public GuiManager(JavaPlugin plugin, ConfigManager configManager, PermissionService permissionService,
                      PlayerStateManager playerStateManager, ModerationManager moderationManager,
                      TrollManager trollManager, BossManager bossManager, EventManager eventManager,
                      CosmeticsManager cosmeticsManager, AuditLogger auditLogger) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.permissionService = permissionService;
        this.playerStateManager = playerStateManager;
        this.moderationManager = moderationManager;
        this.trollManager = trollManager;
        this.bossManager = bossManager;
        this.eventManager = eventManager;
        this.cosmeticsManager = cosmeticsManager;
        this.auditLogger = auditLogger;
    }

    /**
     * Opens the main 10-section Admin Control Center.
     */
    public void openMainDashboard(Player player) {
        if (!permissionService.hasPermission(player, PermissionNode.GUI)) {
            player.sendMessage(TextUtil.toComponent("&cYou do not have permission to access the Admin Control Center."));
            return;
        }

        AdminCoreHolder holder = new AdminCoreHolder(MenuType.MAIN_DASHBOARD);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("&8&lAdminCore Dashboard"));
        holder.setInventory(inv);

        fillBorder(inv);

        inv.setItem(10, createItem(Material.COMPASS, "&b&lPlayer Management", List.of("&7Inspect online players, inventories,", "&7teleportation, and player states.")));
        inv.setItem(12, createItem(Material.ANVIL, "&c&lModeration Center", List.of("&7Manage warnings, mutes, kicks, bans,", "&7notes, and player reports.")));
        inv.setItem(14, createItem(Material.FEATHER, "&d&lFun & Troll Tools", List.of("&7Harmless prank actions, fake events,", "&7visual lightning, launches, and spins.")));
        inv.setItem(16, createItem(Material.NETHERITE_SWORD, "&4&lBoss Manager", List.of("&7Spawn and manage custom bosses,", "&7configure abilities and defeat loot.")));
        inv.setItem(28, createItem(Material.BEACON, "&6&lEvent Manager", List.of("&7Host LMS, KOTH, Sumo, Parkour,", "&7Supply Drops, and Mob Arenas.")));
        inv.setItem(30, createItem(Material.BLAZE_POWDER, "&e&lCosmetics Manager", List.of("&7Select particle trails, ambient auras,", "&7and custom victory celebrations.")));
        inv.setItem(32, createItem(Material.CHEST, "&a&lRewards & Economy", List.of("&7Review configured reward tables,", "&7Vault payouts, and item bundles.")));
        inv.setItem(34, createItem(Material.REPEATER, "&f&lServer Tools", List.of("&7Configuration reload, status checks,", "&7memory diagnostics, and debug mode.")));
        inv.setItem(46, createItem(Material.BOOK, "&9&lLogs & Statistics", List.of("&7View and search staff audit logs,", "&7or export records to disk.")));
        inv.setItem(52, createItem(Material.NETHER_STAR, "&e&lPlugin Information", List.of("&7AdminCore v1.0.0", "&7Developed by &bLivingYuji", "&8Original Minecraft Administration")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
    }

    /**
     * Opens Player Management screen showing online players.
     */
    public void openPlayerManagement(Player staff, int page) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.PLAYER_MANAGEMENT, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("&8&lPlayer Management - Page " + (page + 1)));
        holder.setInventory(inv);

        fillBorder(inv);

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int start = page * 28;
        int end = Math.min(start + 28, players.size());

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = start; i < end; i++) {
            Player target = players.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.displayName(TextUtil.toComponent("&e&l" + target.getName()));
                meta.lore(List.of(
                        TextUtil.toComponent("&7Ping: &f" + target.getPing() + "ms"),
                        TextUtil.toComponent("&7Health: &c" + (int) target.getHealth() + "/" + (int) target.getMaxHealth()),
                        TextUtil.toComponent("&7Gamemode: &f" + target.getGameMode().name()),
                        TextUtil.toComponent("&bClick to manage this player")
                ));
                head.setItemMeta(meta);
            }
            inv.setItem(slots[i - start], head);
        }

        inv.setItem(48, createItem(Material.ARROW, "&ePrevious Page", List.of()));
        inv.setItem(49, createItem(Material.BARRIER, "&cBack to Main Menu", List.of()));
        inv.setItem(50, createItem(Material.ARROW, "&eNext Page", List.of()));

        staff.openInventory(inv);
        staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
    }

    /**
     * Opens Player Action menu for a specific target player.
     */
    public void openPlayerActions(Player staff, Player target) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.PLAYER_ACTIONS, 0, target);
        Inventory inv = Bukkit.createInventory(holder, 36, TextUtil.toComponent("&8&lManage: &e" + target.getName()));
        holder.setInventory(inv);

        fillBorder(inv);

        inv.setItem(10, createItem(Material.GOLDEN_APPLE, "&aHeal & Feed", List.of("&7Restore full health and food.")));
        inv.setItem(11, createItem(Material.FEATHER, "&bToggle Flight", List.of("&7Current: &f" + target.getAllowFlight())));
        inv.setItem(12, createItem(Material.SHIELD, "&6Toggle God Mode", List.of("&7Current: &f" + playerStateManager.isGod(target.getUniqueId()))));
        inv.setItem(13, createItem(Material.ENDER_PEARL, "&5Teleport To", List.of("&7Teleport to this player.")));
        inv.setItem(14, createItem(Material.ENDER_EYE, "&dTeleport Here", List.of("&7Teleport player to you.")));
        inv.setItem(15, createItem(Material.PACKED_ICE, "&3Toggle Freeze", List.of("&7Current: &f" + playerStateManager.isFrozen(target.getUniqueId()))));
        inv.setItem(16, createItem(Material.CHEST, "&eInspect Inventory", List.of("&7View player's live inventory.")));

        inv.setItem(19, createItem(Material.ENDER_CHEST, "&5Inspect Ender Chest", List.of("&7View player's ender chest.")));
        inv.setItem(20, createItem(Material.POTION, "&dApply Speed Boost", List.of("&7Grant temporary speed.")));
        inv.setItem(21, createItem(Material.REDSTONE, "&cKick Player", List.of("&7Kick player with default reason.")));
        inv.setItem(22, createItem(Material.BARRIER, "&4Ban Player", List.of("&7Permanently ban this player.")));

        inv.setItem(31, createItem(Material.ARROW, "&cBack to Players", List.of()));

        staff.openInventory(inv);
    }

    /**
     * Opens Moderation Center.
     */
    public void openModerationCenter(Player staff) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.MODERATION_CENTER);
        Inventory inv = Bukkit.createInventory(holder, 36, TextUtil.toComponent("&8&lModeration Center"));
        holder.setInventory(inv);

        fillBorder(inv);

        inv.setItem(11, createItem(Material.WRITABLE_BOOK, "&ePlayer Reports Queue", List.of("&7Review submitted player reports.")));
        inv.setItem(13, createItem(Material.IRON_SWORD, "&cDisciplinary Tools", List.of("&7Quick punish shortcuts & history.")));
        inv.setItem(15, createItem(Material.NETHERITE_CHESTPLATE, "&bStaff Mode Duty", List.of("&7Toggle active staff duty mode.")));

        inv.setItem(31, createItem(Material.ARROW, "&cBack to Main Menu", List.of()));

        staff.openInventory(inv);
    }

    /**
     * Opens Troll Tools GUI.
     */
    public void openTrollTools(Player staff) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.TROLL_TOOLS);
        Inventory inv = Bukkit.createInventory(holder, 36, TextUtil.toComponent("&8&lFun & Troll Pranks"));
        holder.setInventory(inv);

        fillBorder(inv);

        inv.setItem(10, createItem(Material.LIME_DYE, "&aFake Join Announcement", List.of("&7Broadcasts harmless join message.")));
        inv.setItem(12, createItem(Material.GRAY_DYE, "&7Fake Leave Announcement", List.of("&7Broadcasts harmless leave message.")));
        inv.setItem(14, createItem(Material.FIREWORK_ROCKET, "&eLaunch Harmlessly", List.of("&7Launches target with fall immunity.")));
        inv.setItem(16, createItem(Material.LIGHTNING_ROD, "&6Visual Lightning", List.of("&7Strikes harmless visual bolt.")));
        inv.setItem(22, createItem(Material.BARRIER, "&cStop All Active Pranks", List.of("&7Immediately halt running spins/effects.")));

        inv.setItem(31, createItem(Material.ARROW, "&cBack to Main Menu", List.of()));

        staff.openInventory(inv);
    }

    /**
     * Opens Boss Manager GUI.
     */
    public void openBossManager(Player staff) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.BOSS_MANAGER);
        Inventory inv = Bukkit.createInventory(holder, 36, TextUtil.toComponent("&8&lCustom Boss Manager"));
        holder.setInventory(inv);

        fillBorder(inv);

        int slot = 10;
        for (BossConfig boss : bossManager.getBossConfigs().values()) {
            if (slot > 16) break;
            inv.setItem(slot++, createItem(Material.WITHER_SKELETON_SKULL, "&4&l" + boss.getDisplayName(),
                    List.of("&7ID: &f" + boss.getId(),
                            "&7Health: &c" + boss.getMaxHealth(),
                            "&7Damage: &e" + boss.getAttackDamage(),
                            "&aClick to spawn at your location")));
        }

        inv.setItem(22, createItem(Material.TNT, "&cKill All Active Bosses", List.of("&7Removes all active plugin bosses.")));
        inv.setItem(31, createItem(Material.ARROW, "&cBack to Main Menu", List.of()));

        staff.openInventory(inv);
    }

    /**
     * Opens Event Manager GUI.
     */
    public void openEventManager(Player staff) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.EVENT_MANAGER);
        Inventory inv = Bukkit.createInventory(holder, 36, TextUtil.toComponent("&8&lServer Event Manager"));
        holder.setInventory(inv);

        fillBorder(inv);

        int slot = 10;
        for (AdminEvent event : eventManager.getRegisteredEvents().values()) {
            if (slot > 16) break;
            inv.setItem(slot++, createItem(Material.GOLDEN_APPLE, "&6&l" + event.getDisplayName(),
                    List.of("&7Type: &f" + event.getType().getFriendlyName(),
                            "&7Min Players: &f" + event.getMinPlayers(),
                            "&7Status: &e" + event.getState().name(),
                            "&aClick to start this event")));
        }

        inv.setItem(22, createItem(Material.BARRIER, "&cStop Active Event", List.of("&7Cancels any running event match.")));
        inv.setItem(31, createItem(Material.ARROW, "&cBack to Main Menu", List.of()));

        staff.openInventory(inv);
    }

    /**
     * Opens Cosmetics Selector GUI.
     */
    public void openCosmeticsManager(Player player) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.COSMETICS_MANAGER);
        Inventory inv = Bukkit.createInventory(holder, 36, TextUtil.toComponent("&8&lCosmetics Selector"));
        holder.setInventory(inv);

        fillBorder(inv);

        int slot = 10;
        for (CosmeticDefinition def : cosmeticsManager.getDefinitions().values()) {
            if (slot > 16) break;
            inv.setItem(slot++, createItem(def.getIconMaterial(), "&b&l" + def.getName(),
                    List.of("&7Category: &f" + def.getType().name(),
                            "&7Particle: &e" + (def.getParticle() != null ? def.getParticle().name() : "None"),
                            "&aClick to equip")));
        }

        inv.setItem(22, createItem(Material.LAVA_BUCKET, "&cClear Active Cosmetics", List.of("&7Remove your equipped effects.")));
        inv.setItem(31, createItem(Material.ARROW, "&cBack to Main Menu", List.of()));

        player.openInventory(inv);
    }

    /**
     * Opens Server Tools GUI.
     */
    public void openServerTools(Player staff) {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.SERVER_TOOLS);
        Inventory inv = Bukkit.createInventory(holder, 27, TextUtil.toComponent("&8&lServer & Plugin Tools"));
        holder.setInventory(inv);

        fillBorder(inv);

        inv.setItem(11, createItem(Material.EMERALD, "&aReload Configurations", List.of("&7Safely reload YAML configs.")));
        inv.setItem(13, createItem(Material.REDSTONE_TORCH, "&eToggle Debug Mode", List.of("&7Verbose console logging.")));
        inv.setItem(15, createItem(Material.CLOCK, "&bServer Performance", List.of("&7Memory: &f" +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "MB / " +
                (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB")));

        inv.setItem(22, createItem(Material.ARROW, "&cBack to Main Menu", List.of()));

        staff.openInventory(inv);
    }

    private void fillBorder(Inventory inv) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        int size = inv.getSize();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, filler);
            inv.setItem(size - 9 + i, filler);
        }
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, filler);
            inv.setItem(i + 8, filler);
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.toComponent(name));
            List<net.kyori.adventure.text.Component> loreComps = new ArrayList<>();
            for (String line : lore) {
                loreComps.add(TextUtil.toComponent(line));
            }
            meta.lore(loreComps);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminCoreHolder holder)) {
            return;
        }

        // Prevent taking, moving, dragging, or duplicating any items
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);

        switch (holder.getMenuType()) {
            case MAIN_DASHBOARD -> handleMainDashboardClick(player, event.getSlot());
            case PLAYER_MANAGEMENT -> handlePlayerManagementClick(player, holder, event.getSlot(), clicked);
            case PLAYER_ACTIONS -> handlePlayerActionsClick(player, holder, event.getSlot());
            case MODERATION_CENTER -> handleModerationClick(player, event.getSlot());
            case TROLL_TOOLS -> handleTrollClick(player, event.getSlot());
            case BOSS_MANAGER -> handleBossClick(player, event.getSlot(), clicked);
            case EVENT_MANAGER -> handleEventClick(player, event.getSlot(), clicked);
            case COSMETICS_MANAGER -> handleCosmeticsClick(player, clicked);
            case SERVER_TOOLS -> handleServerToolsClick(player, event.getSlot());
        }
    }

    private void handleMainDashboardClick(Player player, int slot) {
        switch (slot) {
            case 10 -> openPlayerManagement(player, 0);
            case 12 -> openModerationCenter(player);
            case 14 -> openTrollTools(player);
            case 16 -> openBossManager(player);
            case 28 -> openEventManager(player);
            case 30 -> openCosmeticsManager(player);
            case 32 -> player.sendMessage(TextUtil.toComponent("&aConfigured rewards available via &f/ac event reward &aor &f/ac boss!"));
            case 34 -> openServerTools(player);
            case 46 -> {
                player.sendMessage(TextUtil.toComponent("&eRecent audit logs:"));
                for (String line : auditLogger.searchLogs("", 5)) {
                    player.sendMessage(TextUtil.toComponent("&7- " + line));
                }
            }
            case 52 -> player.sendMessage(TextUtil.toComponent("&bAdminCore v1.0.0 &7| Developed and credited entirely to &eLivingYuji"));
        }
    }

    private void handlePlayerManagementClick(Player staff, AdminCoreHolder holder, int slot, ItemStack clicked) {
        if (slot == 49) {
            openMainDashboard(staff);
            return;
        }
        if (slot == 48 && holder.getPage() > 0) {
            openPlayerManagement(staff, holder.getPage() - 1);
            return;
        }
        if (slot == 50) {
            openPlayerManagement(staff, holder.getPage() + 1);
            return;
        }

        if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta() instanceof SkullMeta sm) {
            if (sm.getOwningPlayer() != null && sm.getOwningPlayer().isOnline()) {
                Player target = sm.getOwningPlayer().getPlayer();
                if (target != null) {
                    openPlayerActions(staff, target);
                }
            }
        }
    }

    private void handlePlayerActionsClick(Player staff, AdminCoreHolder holder, int slot) {
        if (!(holder.getContext() instanceof Player target) || !target.isOnline()) {
            staff.sendMessage(TextUtil.toComponent("&cTarget player is no longer online."));
            staff.closeInventory();
            return;
        }

        switch (slot) {
            case 10 -> {
                target.setHealth(target.getMaxHealth());
                target.setFoodLevel(20);
                staff.sendMessage(TextUtil.toComponent("&aHealed and fed &e" + target.getName()));
            }
            case 11 -> {
                boolean flight = !target.getAllowFlight();
                target.setAllowFlight(flight);
                staff.sendMessage(TextUtil.toComponent("&eFlight for " + target.getName() + " set to &f" + flight));
            }
            case 12 -> {
                boolean god = !playerStateManager.isGod(target.getUniqueId());
                playerStateManager.setGod(target, god);
                staff.sendMessage(TextUtil.toComponent("&eGod mode for " + target.getName() + " set to &f" + god));
            }
            case 13 -> {
                staff.teleport(target.getLocation());
                staff.sendMessage(TextUtil.toComponent("&aTeleported to &e" + target.getName()));
            }
            case 14 -> {
                target.teleport(staff.getLocation());
                staff.sendMessage(TextUtil.toComponent("&aTeleported &e" + target.getName() + " &ato you."));
            }
            case 15 -> {
                boolean frozen = !playerStateManager.isFrozen(target.getUniqueId());
                playerStateManager.setFrozen(target, frozen, staff.getName());
                staff.sendMessage(TextUtil.toComponent((frozen ? "&aFrozen " : "&cUnfrozen ") + target.getName()));
            }
            case 16 -> staff.openInventory(target.getInventory());
            case 19 -> staff.openInventory(target.getEnderChest());
            case 21 -> moderationManager.kick(target, staff.getName(), "Kicked by staff via Control Center");
            case 22 -> moderationManager.ban(target.getUniqueId(), target.getName(), staff.getUniqueId(), staff.getName(), "Banned via Control Center", -1);
            case 31 -> openPlayerManagement(staff, 0);
        }
    }

    private void handleModerationClick(Player staff, int slot) {
        switch (slot) {
            case 11 -> {
                staff.sendMessage(TextUtil.toComponent("&eRecent Open Reports:"));
                var reports = moderationManager.getReports("OPEN");
                if (reports.isEmpty()) {
                    staff.sendMessage(TextUtil.toComponent("&7No open reports found!"));
                } else {
                    for (var r : reports) {
                        staff.sendMessage(TextUtil.toComponent("&8[&c#" + r.getId() + "&8] &e" + r.getReporterName() +
                                " &7reported &f" + r.getReportedName() + " &7for &e" + r.getReason()));
                    }
                }
            }
            case 15 -> playerStateManager.toggleStaffMode(staff);
            case 31 -> openMainDashboard(staff);
        }
    }

    private void handleTrollClick(Player staff, int slot) {
        switch (slot) {
            case 10 -> trollManager.fakeJoin(staff.getName());
            case 12 -> trollManager.fakeLeave(staff.getName());
            case 14 -> trollManager.launch(staff);
            case 16 -> trollManager.visualLightning(staff);
            case 22 -> {
                trollManager.clearAll();
                staff.sendMessage(TextUtil.toComponent("&aCleared all active troll pranks!"));
            }
            case 31 -> openMainDashboard(staff);
        }
    }

    private void handleBossClick(Player staff, int slot, ItemStack clicked) {
        if (slot == 22) {
            int killed = bossManager.killAll();
            staff.sendMessage(TextUtil.toComponent("&cRemoved " + killed + " active bosses."));
            return;
        }
        if (slot == 31) {
            openMainDashboard(staff);
            return;
        }
        for (BossConfig boss : bossManager.getBossConfigs().values()) {
            if (clicked.getItemMeta() != null && clicked.getItemMeta().displayName() != null &&
                    TextUtil.stripColor(clicked.getItemMeta().getDisplayName()).contains(boss.getDisplayName())) {
                boolean spawned = bossManager.spawnBoss(boss.getId(), staff.getLocation());
                staff.sendMessage(TextUtil.toComponent(spawned ? "&aSpawned boss &b" + boss.getDisplayName() : "&cFailed to spawn boss."));
                return;
            }
        }
    }

    private void handleEventClick(Player staff, int slot, ItemStack clicked) {
        if (slot == 22) {
            eventManager.stopActiveEvent("Stopped via GUI");
            staff.sendMessage(TextUtil.toComponent("&cStopped active event."));
            return;
        }
        if (slot == 31) {
            openMainDashboard(staff);
            return;
        }
        for (AdminEvent ev : eventManager.getRegisteredEvents().values()) {
            if (clicked.getItemMeta() != null && clicked.getItemMeta().displayName() != null &&
                    TextUtil.stripColor(clicked.getItemMeta().getDisplayName()).contains(ev.getDisplayName())) {
                boolean started = eventManager.startEvent(ev.getId());
                staff.sendMessage(TextUtil.toComponent(started ? "&aStarted event &b" + ev.getDisplayName() : "&cCannot start event (already active or missing arena)."));
                return;
            }
        }
    }

    private void handleCosmeticsClick(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.LAVA_BUCKET) {
            cosmeticsManager.clearCosmetics(player);
            return;
        }
        if (clicked.getType() == Material.ARROW) {
            openMainDashboard(player);
            return;
        }
        for (CosmeticDefinition def : cosmeticsManager.getDefinitions().values()) {
            if (clicked.getType() == def.getIconMaterial()) {
                cosmeticsManager.equipCosmetic(player, def.getId());
                return;
            }
        }
    }

    private void handleServerToolsClick(Player staff, int slot) {
        switch (slot) {
            case 11 -> {
                configManager.reloadAll();
                staff.sendMessage(TextUtil.toComponent("&aAdminCore configurations reloaded successfully."));
            }
            case 13 -> {
                boolean debug = !configManager.isDebugEnabled();
                configManager.getMainConfig().set("debug-mode", debug);
                configManager.saveFile("config.yml");
                staff.sendMessage(TextUtil.toComponent("&eDebug mode set to: &f" + debug));
            }
            case 22 -> openMainDashboard(staff);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof AdminCoreHolder) {
            event.setCancelled(true);
        }
    }
}
