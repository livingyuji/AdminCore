/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.gui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminCoreHolderTest {

    @Test
    @DisplayName("AdminCoreHolder should properly retain menu metadata and pagination")
    void testHolderProperties() {
        AdminCoreHolder holder = new AdminCoreHolder(MenuType.PLAYER_MANAGEMENT, 2, "ContextData");

        assertEquals(MenuType.PLAYER_MANAGEMENT, holder.getMenuType());
        assertEquals(2, holder.getPage());
        assertEquals("ContextData", holder.getContext());
        assertNull(holder.getInventory());
    }
}
