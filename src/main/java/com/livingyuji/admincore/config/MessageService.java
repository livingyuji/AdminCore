/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.config;

import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Message translation and dispatch service.
 *
 * @author LivingYuji
 */
public class MessageService {

    private final ConfigManager configManager;

    public MessageService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Retrieves the configured prefix.
     */
    public String getPrefix() {
        FileConfiguration config = configManager.getMessagesConfig();
        if (config == null) {
            return "&8[&bAdminCore&8]&r ";
        }
        return config.getString("prefix", "&8[&bAdminCore&8]&r ");
    }

    /**
     * Gets a raw message string from messages.yml with fallback.
     */
    public String getRaw(String key, String fallback) {
        FileConfiguration config = configManager.getMessagesConfig();
        if (config == null) {
            return fallback;
        }
        return config.getString(key, fallback);
    }

    /**
     * Formats a message with prefix and placeholder replacements.
     */
    public String format(String key, String fallback, Map<String, String> placeholders) {
        String template = getRaw(key, fallback);
        String prefix = getPrefix();
        String formatted = template.replace("%prefix%", prefix);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                formatted = formatted.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return TextUtil.colorize(formatted);
    }

    /**
     * Formats a message without extra placeholders.
     */
    public String format(String key, String fallback) {
        return format(key, fallback, null);
    }

    /**
     * Sends a formatted message to the command sender.
     */
    public void send(CommandSender sender, String key, String fallback, Map<String, String> placeholders) {
        if (sender == null) {
            return;
        }
        String msg = format(key, fallback, placeholders);
        if (!msg.isEmpty()) {
            sender.sendMessage(TextUtil.toComponent(msg));
        }
    }

    /**
     * Sends a formatted message to the command sender.
     */
    public void send(CommandSender sender, String key, String fallback) {
        send(sender, key, fallback, null);
    }

    /**
     * Sends a raw colorized string directly.
     */
    public void sendRaw(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(TextUtil.toComponent(TextUtil.colorize(message)));
        }
    }
}
