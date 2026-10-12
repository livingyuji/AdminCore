/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.boss;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BossConfigTest {

    @Test
    @DisplayName("BossConfig should preserve all configured parameters")
    void testBossConfigCreation() {
        BossConfig config = new BossConfig(
                "titan", "Titan of Fire", EntityType.WITHER_SKELETON, 500.0, 15.0, 0.35,
                BarColor.RED, BarStyle.SOLID, null, List.of("give %player% diamond 5"), List.of("knockback", "minions")
        );

        assertEquals("titan", config.getId());
        assertEquals("Titan of Fire", config.getDisplayName());
        assertEquals(EntityType.WITHER_SKELETON, config.getEntityType());
        assertEquals(500.0, config.getMaxHealth());
        assertEquals(15.0, config.getAttackDamage());
        assertEquals(0.35, config.getMovementSpeed());
        assertEquals(BarColor.RED, config.getBarColor());
        assertEquals(BarStyle.SOLID, config.getBarStyle());
        assertTrue(config.getAbilities().contains("knockback"));
        assertTrue(config.getAbilities().contains("minions"));
        assertEquals(1, config.getRewardCommands().size());
    }
}
