/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.util;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing and formatting time durations.
 *
 * @author LivingYuji
 */
public final class DurationParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)");

    private DurationParser() {
        // Utility class
    }

    /**
     * Parses a string representation of duration into milliseconds.
     * Supported units: s (seconds), m (minutes), h (hours), d (days), w (weeks), mo (months=30d), y (years=365d).
     *
     * @param input the input string, e.g., "10m", "2h30m", "7d"
     * @return duration in milliseconds, or -1 if invalid/permanent
     */
    public static long parseDurationMillis(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1;
        }

        String cleaned = input.trim().toLowerCase();
        if (cleaned.equals("permanent") || cleaned.equals("perm") || cleaned.equals("forever") || cleaned.equals("-1")) {
            return -1;
        }

        Matcher matcher = DURATION_PATTERN.matcher(cleaned);
        long totalMillis = 0;
        boolean found = false;

        while (matcher.find()) {
            found = true;
            try {
                long amount = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2);

                long multiplier = switch (unit) {
                    case "s", "sec", "second", "seconds" -> 1000L;
                    case "m", "min", "minute", "minutes" -> 60_000L;
                    case "h", "hr", "hour", "hours" -> 3_600_000L;
                    case "d", "day", "days" -> 86_400_000L;
                    case "w", "week", "weeks" -> 7 * 86_400_000L;
                    case "mo", "month", "months" -> 30L * 86_400_000L;
                    case "y", "yr", "year", "years" -> 365L * 86_400_000L;
                    default -> -1L;
                };

                if (multiplier == -1L) {
                    return -1L; // Unknown unit
                }

                totalMillis += amount * multiplier;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return found ? totalMillis : -1;
    }

    /**
     * Formats remaining milliseconds into a user-friendly string.
     *
     * @param millis duration in milliseconds
     * @return formatted duration string (e.g. "2h 30m 10s" or "Permanent")
     */
    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "Permanent";
        }

        Duration duration = Duration.ofMillis(millis);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}
