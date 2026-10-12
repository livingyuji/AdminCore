/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Time utility for timestamp formatting.
 *
 * @author LivingYuji
 */
public final class TimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private TimeUtil() {
    }

    /**
     * Formats an epoch millisecond timestamp into standard date/time string.
     *
     * @param epochMillis millisecond timestamp
     * @return formatted date string
     */
    public static String formatTimestamp(long epochMillis) {
        if (epochMillis <= 0) {
            return "N/A";
        }
        return FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }
}
