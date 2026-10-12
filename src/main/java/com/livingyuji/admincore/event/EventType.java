/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.event;

/**
 * Types of structured server events managed by AdminCore.
 *
 * @author LivingYuji
 */
public enum EventType {
    LMS("Last Man Standing"),
    KOTH("King of the Hill"),
    SUMO("Sumo Knockout"),
    PARKOUR("Parkour Race"),
    TREASURE_HUNT("Treasure Hunt"),
    SUPPLY_DROP("Supply Drop"),
    MOB_ARENA("Mob Wave Arena");

    private final String friendlyName;

    EventType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
