/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility for serializing and deserializing Bukkit locations.
 *
 * @author LivingYuji
 */
public final class LocationUtil {

    private LocationUtil() {
    }

    /**
     * Serializes a Location into a formatted string: "world,x,y,z,yaw,pitch".
     *
     * @param loc location to serialize
     * @return serialized string
     */
    public static String serialize(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        return String.format(java.util.Locale.US, "%s,%.3f,%.3f,%.3f,%.2f,%.2f",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }

    /**
     * Deserializes a string formatted as "world,x,y,z,yaw,pitch" into a Bukkit Location.
     *
     * @param str serialized location string
     * @return parsed Location or null if invalid or world not found
     */
    public static Location deserialize(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        String[] parts = str.split(",");
        if (parts.length < 4) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0].trim());
        if (world == null) {
            return null;
        }

        try {
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
