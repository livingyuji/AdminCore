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

class TextUtilTest {

    @Test
    @DisplayName("Should translate standard color codes")
    void testColorize() {
        String input = "&aHello &cWorld";
        String colorized = TextUtil.colorize(input);
        assertEquals("§aHello §cWorld", colorized);
    }

    @Test
    @DisplayName("Should escape Discord markdown characters")
    void testEscapeDiscord() {
        String input = "*bold* _italic_ `code` @everyone";
        String escaped = TextUtil.escapeDiscord(input);
        assertEquals("\\*bold\\* \\_italic\\_ \\`code\\` @everyone", escaped);
    }

    @Test
    @DisplayName("Should handle null strings safely")
    void testNullSafe() {
        assertEquals("", TextUtil.colorize(null));
        assertEquals("", TextUtil.escapeDiscord(null));
    }
}
