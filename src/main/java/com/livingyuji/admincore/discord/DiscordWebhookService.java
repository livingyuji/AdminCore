/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.discord;

import com.livingyuji.admincore.config.ConfigManager;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Asynchronous Discord webhook notification service.
 *
 * @author LivingYuji
 */
public class DiscordWebhookService {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ExecutorService executorService;

    public DiscordWebhookService(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AdminCore-Discord-Webhook");
            t.setDaemon(true);
            return t;
        });
    }

    public boolean isEnabled() {
        FileConfiguration config = configManager.getMainConfig();
        return config != null && config.getBoolean("discord.enabled", false);
    }

    public String getWebhookUrl() {
        FileConfiguration config = configManager.getMainConfig();
        if (config == null) return "";
        return config.getString("discord.webhook-url", "");
    }

    public boolean isNotificationEnabled(String category) {
        if (!isEnabled()) return false;
        FileConfiguration config = configManager.getMainConfig();
        if (config == null) return false;
        return config.getBoolean("discord.notifications." + category, true);
    }

    /**
     * Sends an embed notification asynchronously to the configured webhook.
     *
     * @param category category toggle (e.g., "punishments", "events", "bosses", "staff-actions")
     * @param title embed title
     * @param description embed description
     * @param colorHex hex integer color (e.g. 0xFF0000)
     */
    public void sendEmbed(String category, String title, String description, int colorHex) {
        if (!isNotificationEnabled(category)) {
            return;
        }

        String webhookUrl = getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty() || !webhookUrl.startsWith("http")) {
            return;
        }

        executorService.submit(() -> {
            try {
                String safeTitle = TextUtil.escapeDiscord(TextUtil.stripColor(title));
                String safeDesc = TextUtil.escapeDiscord(TextUtil.stripColor(description));

                String json = String.format("""
                    {
                        "embeds": [{
                            "title": "%s",
                            "description": "%s",
                            "color": %d,
                            "footer": {
                                "text": "AdminCore by LivingYuji"
                            },
                            "timestamp": "%s"
                        }]
                    }
                    """,
                        escapeJson(safeTitle),
                        escapeJson(safeDesc),
                        colorHex,
                        Instant.now().toString()
                );

                URL url = URI.create(webhookUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "AdminCore-Minecraft-Plugin");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    plugin.getLogger().warning("Discord webhook returned HTTP status code: " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to deliver Discord webhook notification", e);
            }
        });
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
