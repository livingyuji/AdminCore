/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DurationParserTest {

    @Test
    @DisplayName("Should parse standard duration strings correctly")
    void testStandardDurations() {
        assertEquals(30_000L, DurationParser.parseDurationMillis("30s"));
        assertEquals(600_000L, DurationParser.parseDurationMillis("10m"));
        assertEquals(7_200_000L, DurationParser.parseDurationMillis("2h"));
        assertEquals(86_400_000L, DurationParser.parseDurationMillis("1d"));
        assertEquals(7 * 86_400_000L, DurationParser.parseDurationMillis("1w"));
    }

    @Test
    @DisplayName("Should parse combined duration strings")
    void testCombinedDurations() {
        long expected = 86_400_000L + 2 * 3_600_000L + 30 * 60_000L;
        assertEquals(expected, DurationParser.parseDurationMillis("1d2h30m"));
    }

    @Test
    @DisplayName("Should return -1 for permanent or invalid tokens")
    void testPermanentAndInvalid() {
        assertEquals(-1L, DurationParser.parseDurationMillis("permanent"));
        assertEquals(-1L, DurationParser.parseDurationMillis("perm"));
        assertEquals(-1L, DurationParser.parseDurationMillis("-1"));
        assertEquals(-1L, DurationParser.parseDurationMillis("invalid"));
        assertEquals(-1L, DurationParser.parseDurationMillis(""));
        assertEquals(-1L, DurationParser.parseDurationMillis(null));
    }

    @Test
    @DisplayName("Should format durations into human readable strings")
    void testFormatDuration() {
        assertEquals("Permanent", DurationParser.formatDuration(-1L));
        assertEquals("Permanent", DurationParser.formatDuration(0L));
        assertEquals("30s", DurationParser.formatDuration(30_000L));
        assertEquals("10m 0s", DurationParser.formatDuration(600_000L));
        assertEquals("1d 2h 0m 0s", DurationParser.formatDuration(86_400_000L + 7_200_000L));
    }
}
