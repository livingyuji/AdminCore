/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages loading, caching, reloading, and validation of all AdminCore configuration files.
 *
 * @author LivingYuji
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();

    private static final String[] CONFIG_FILES = {
            "config.yml",
            "messages.yml",
            "gui.yml",
            "bosses.yml",
            "events.yml",
            "cosmetics.yml",
            "rewards.yml"
    };

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads or reloads all configuration files from disk or resource defaults.
     */
    public void loadAll() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        for (String fileName : CONFIG_FILES) {
            loadFile(fileName);
        }
    }

    /**
     * Loads a specific configuration file.
     *
     * @param fileName configuration file name
     */
    public void loadFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        // Merge defaults if updated
        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            config.setDefaults(defaultConfig);
        }

        configs.put(fileName, config);
        files.put(fileName, file);
    }

    /**
     * Saves a specific configuration file to disk.
     *
     * @param fileName configuration file name
     */
    public void saveFile(String fileName) {
        FileConfiguration config = configs.get(fileName);
        File file = files.get(fileName);
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save configuration: " + fileName, e);
            }
        }
    }

    /**
     * Reloads all configuration files.
     */
    public void reloadAll() {
        configs.clear();
        files.clear();
        loadAll();
    }

    public FileConfiguration getMainConfig() {
        return configs.get("config.yml");
    }

    public FileConfiguration getMessagesConfig() {
        return configs.get("messages.yml");
    }

    public FileConfiguration getGuiConfig() {
        return configs.get("gui.yml");
    }

    public FileConfiguration getBossesConfig() {
        return configs.get("bosses.yml");
    }

    public FileConfiguration getEventsConfig() {
        return configs.get("events.yml");
    }

    public FileConfiguration getCosmeticsConfig() {
        return configs.get("cosmetics.yml");
    }

    public FileConfiguration getRewardsConfig() {
        return configs.get("rewards.yml");
    }

    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }

    public boolean isModuleEnabled(String moduleName) {
        FileConfiguration config = getMainConfig();
        if (config == null) {
            return true;
        }
        return config.getBoolean("modules." + moduleName, true);
    }

    public boolean isDebugEnabled() {
        FileConfiguration config = getMainConfig();
        return config != null && config.getBoolean("debug-mode", false);
    }
}
