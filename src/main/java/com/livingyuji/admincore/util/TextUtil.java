/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text formatting, colorization, and sanitization utility.
 *
 * @author LivingYuji
 */
public final class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private TextUtil() {
    }

    /**
     * Translates alternate color codes ('&') including hex (&#RRGGBB) to colorized text.
     *
     * @param message input string
     * @return colorized string
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString().replace('&', '§');
    }

    /**
     * Converts a string with color codes to an Adventure Component.
     *
     * @param message input string
     * @return Adventure Component
     */
    public static Component toComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacySection().deserialize(colorize(message));
    }

    /**
     * Strips all formatting and color codes.
     *
     * @param message input text
     * @return clean plain text
     */
    public static String stripColor(String message) {
        if (message == null) {
            return "";
        }
        Component comp = LegacyComponentSerializer.legacySection().deserialize(colorize(message));
        return PlainTextComponentSerializer.plainText().serialize(comp);
    }

    /**
     * Escapes Discord markdown characters to avoid accidental formatting in webhooks.
     *
     * @param text input text
     * @return sanitized text
     */
    public static String escapeDiscord(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("|", "\\|");
    }
}
