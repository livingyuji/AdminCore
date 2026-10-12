/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.permission;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Service for evaluating sender permissions with least-privilege checks
 * and administrative hierarchy handling.
 *
 * @author LivingYuji
 */
public class PermissionService {

    /**
     * Checks if a command sender has the specified permission node.
     * Console always returns true.
     *
     * @param sender command sender
     * @param node permission node
     * @return true if permitted
     */
    public boolean hasPermission(CommandSender sender, PermissionNode node) {
        if (sender == null) {
            return false;
        }
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        if (sender.isOp() || sender.hasPermission(PermissionNode.ADMIN.getNode()) || sender.hasPermission("*")) {
            return true;
        }
        return sender.hasPermission(node.getNode());
    }

    /**
     * Checks if a command sender has the specified string permission.
     *
     * @param sender command sender
     * @param permission permission string
     * @return true if permitted
     */
    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender == null) {
            return false;
        }
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        if (sender.isOp() || sender.hasPermission(PermissionNode.ADMIN.getNode()) || sender.hasPermission("*")) {
            return true;
        }
        return sender.hasPermission(permission);
    }
}
