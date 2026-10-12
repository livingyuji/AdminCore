/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.permission;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
    }

    @Test
    @DisplayName("Console should always have all permissions")
    void testConsolePermissions() {
        ConsoleCommandSender console = mock(ConsoleCommandSender.class);
        assertTrue(permissionService.hasPermission(console, PermissionNode.ADMIN));
        assertTrue(permissionService.hasPermission(console, PermissionNode.MODERATION_BAN));
        assertTrue(permissionService.hasPermission(console, "admincore.custom.perm"));
    }

    @Test
    @DisplayName("Player with admincore.admin node should bypass other checks")
    void testAdminBypass() {
        Player player = mock(Player.class);
        when(player.hasPermission("admincore.admin")).thenReturn(true);

        assertTrue(permissionService.hasPermission(player, PermissionNode.MODERATION_WARN));
        assertTrue(permissionService.hasPermission(player, PermissionNode.PLAYER_FLY));
    }

    @Test
    @DisplayName("Regular player without permission should be denied")
    void testDeniedPermission() {
        Player player = mock(Player.class);
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission(Mockito.anyString())).thenReturn(false);

        assertFalse(permissionService.hasPermission(player, PermissionNode.MODERATION_BAN));
    }

    @Test
    @DisplayName("Player with exact permission should be granted")
    void testExactPermission() {
        Player player = mock(Player.class);
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission(PermissionNode.PLAYER_FLY.getNode())).thenReturn(true);

        assertTrue(permissionService.hasPermission(player, PermissionNode.PLAYER_FLY));
        assertFalse(permissionService.hasPermission(player, PermissionNode.MODERATION_BAN));
    }
}
