/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.cosmetics;

import org.bukkit.Material;
import org.bukkit.Particle;

/**
 * Definition of a single cosmetic item/effect.
 *
 * @author LivingYuji
 */
public class CosmeticDefinition {

    private final String id;
    private final String name;
    private final CosmeticType type;
    private final Particle particle;
    private final Material iconMaterial;
    private final String permission;

    public CosmeticDefinition(String id, String name, CosmeticType type, Particle particle, Material iconMaterial, String permission) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.particle = particle;
        this.iconMaterial = iconMaterial;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CosmeticType getType() {
        return type;
    }

    public Particle getParticle() {
        return particle;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public String getPermission() {
        return permission;
    }
}
