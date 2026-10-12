/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.moderation;

import com.livingyuji.admincore.util.DurationParser;
import com.livingyuji.admincore.util.TextUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

/**
 * Event listener enforcing active bans and mutes.
 *
 * @author LivingYuji
 */
public class ModerationListener implements Listener {

    private final ModerationManager moderationManager;

    public ModerationListener(ModerationManager moderationManager) {
        this.moderationManager = moderationManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        Punishment ban = moderationManager.getActiveBan(uuid);
        if (ban != null) {
            String duration = ban.isPermanent() ? "Permanent" : DurationParser.formatDuration(ban.getRemainingMillis());
            String kickMessage = TextUtil.colorize("&c&lYou are banned from this server!\n\n" +
                    "&eReason: &f" + ban.getReason() + "\n" +
                    "&eDuration: &f" + duration + "\n" +
                    "&eBanned by: &f" + ban.getStaffName());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, TextUtil.toComponent(kickMessage));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Punishment mute = moderationManager.getActiveMute(uuid);
        if (mute != null) {
            event.setCancelled(true);
            String duration = mute.isPermanent() ? "Permanent" : DurationParser.formatDuration(mute.getRemainingMillis());
            event.getPlayer().sendMessage(TextUtil.toComponent(
                    "&cYou cannot speak! You are currently muted.\n" +
                    "&eRemaining: &f" + duration + " &8| &eReason: &f" + mute.getReason()
            ));
        }
    }
}
