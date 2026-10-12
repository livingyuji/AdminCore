/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.rewards;

import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.integration.VaultIntegration;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages reward tables, item payouts, Vault economy rewards, and console command execution.
 *
 * @author LivingYuji
 */
public class RewardManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final VaultIntegration vaultIntegration;
    private final Set<String> claimedRewards = ConcurrentHashMap.newKeySet();

    public RewardManager(JavaPlugin plugin, ConfigManager configManager, VaultIntegration vaultIntegration) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.vaultIntegration = vaultIntegration;
    }

    /**
     * Executes a reward table for a player.
     *
     * @param player recipient player
     * @param tableKey table key in rewards.yml
     * @param transactionId unique transaction id to prevent duplicate claims
     * @return true if awarded successfully
     */
    public boolean award(Player player, String tableKey, String transactionId) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        if (transactionId != null && !claimedRewards.add(transactionId)) {
            // Already claimed
            return false;
        }

        FileConfiguration config = configManager.getRewardsConfig();
        if (config == null || !config.isConfigurationSection("tables." + tableKey)) {
            return false;
        }

        ConfigurationSection section = config.getConfigurationSection("tables." + tableKey);
        if (section == null) return false;

        // Money reward
        double money = section.getDouble("money", 0.0);
        if (money > 0 && vaultIntegration.isAvailable()) {
            vaultIntegration.deposit(player, money);
            player.sendMessage(TextUtil.toComponent("&a+ $" + money + " added to your balance!"));
        }

        // Items reward
        List<String> items = section.getStringList("items");
        for (String itemStr : items) {
            String[] parts = itemStr.split(":");
            try {
                Material mat = Material.valueOf(parts[0].toUpperCase());
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                player.getInventory().addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {
            }
        }

        // Commands reward
        List<String> commands = section.getStringList("commands");
        for (String cmd : commands) {
            String sanitized = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), sanitized);
        }

        String message = section.getString("message", "&aYou received rewards!");
        player.sendMessage(TextUtil.toComponent(message));
        return true;
    }
}
