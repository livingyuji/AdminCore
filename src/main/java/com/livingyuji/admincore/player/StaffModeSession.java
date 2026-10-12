/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Stores pre-staff-mode player state to allow clean restoration.
 *
 * @author LivingYuji
 */
public class StaffModeSession {

    private final GameMode originalGameMode;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;
    private final Location originalLocation;
    private final boolean wasFlying;
    private final boolean wasAllowFlight;

    public StaffModeSession(GameMode originalGameMode, ItemStack[] originalInventory,
                            ItemStack[] originalArmor, Location originalLocation,
                            boolean wasFlying, boolean wasAllowFlight) {
        this.originalGameMode = originalGameMode;
        this.originalInventory = originalInventory;
        this.originalArmor = originalArmor;
        this.originalLocation = originalLocation;
        this.wasFlying = wasFlying;
        this.wasAllowFlight = wasAllowFlight;
    }

    public GameMode getOriginalGameMode() {
        return originalGameMode;
    }

    public ItemStack[] getOriginalInventory() {
        return originalInventory;
    }

    public ItemStack[] getOriginalArmor() {
        return originalArmor;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public boolean wasFlying() {
        return wasFlying;
    }

    public boolean wasAllowFlight() {
        return wasAllowFlight;
    }
}
