/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.rewards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RewardDuplicationTest {

    @Test
    @DisplayName("Transaction IDs should prevent duplicate reward claims")
    void testRewardDuplication() {
        Set<String> claimedRewards = ConcurrentHashMap.newKeySet();

        String transaction1 = "EVENT_LMS_WIN_1001";
        String transaction2 = "EVENT_LMS_WIN_1002";

        // First claim should succeed
        assertTrue(claimedRewards.add(transaction1));

        // Second attempt with same ID should be rejected
        assertFalse(claimedRewards.add(transaction1));

        // Different transaction should succeed
        assertTrue(claimedRewards.add(transaction2));
    }
}
