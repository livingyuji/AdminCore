/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.moderation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PunishmentTest {

    @Test
    @DisplayName("Permanent punishment should never expire")
    void testPermanentPunishment() {
        Punishment p = new Punishment(
                1L, UUID.randomUUID(), "Player1", null, "CONSOLE",
                PunishmentType.BAN, "Cheating", System.currentTimeMillis(), -1, true
        );

        assertTrue(p.isPermanent());
        assertFalse(p.isExpired());
        assertEquals(-1L, p.getRemainingMillis());
    }

    @Test
    @DisplayName("Temporary punishment should detect expiry correctly")
    void testTemporaryPunishment() {
        long now = System.currentTimeMillis();
        // Expired 5 seconds ago
        Punishment expired = new Punishment(
                2L, UUID.randomUUID(), "Player2", null, "CONSOLE",
                PunishmentType.TEMPBAN, "Griefing", now - 10_000, now - 5_000, true
        );

        assertFalse(expired.isPermanent());
        assertTrue(expired.isExpired());
        assertEquals(0L, expired.getRemainingMillis());

        // Active for 1 hour
        Punishment active = new Punishment(
                3L, UUID.randomUUID(), "Player3", null, "CONSOLE",
                PunishmentType.MUTE, "Spam", now, now + 3_600_000, true
        );

        assertFalse(active.isPermanent());
        assertFalse(active.isExpired());
        assertTrue(active.getRemainingMillis() > 0);
    }
}
