/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Safe optional integration hook for Vault economy provider.
 *
 * @author LivingYuji
 */
public class VaultIntegration {

    private final JavaPlugin plugin;
    private Economy economy = null;
    private boolean available = false;

    public VaultIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempts to find and hook Vault economy service.
     */
    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        try {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                available = economy != null;
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            available = false;
        }

        return available;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Deposits money to an offline/online player.
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!available || economy == null || player == null || amount <= 0) {
            return false;
        }
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}
