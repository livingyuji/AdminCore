/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.moderation;

import java.util.UUID;

/**
 * Immutable punishment record representation.
 *
 * @author LivingYuji
 */
public class Punishment {

    private final long id;
    private final UUID targetUuid;
    private final String targetName;
    private final UUID staffUuid; // null for console
    private final String staffName;
    private final PunishmentType type;
    private final String reason;
    private final long createdAt;
    private final long expiresAt; // -1 for permanent
    private final boolean active;

    public Punishment(long id, UUID targetUuid, String targetName, UUID staffUuid, String staffName,
                      PunishmentType type, String reason, long createdAt, long expiresAt, boolean active) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPermanent() {
        return expiresAt <= 0;
    }

    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() >= expiresAt;
    }

    public long getRemainingMillis() {
        if (isPermanent()) {
            return -1;
        }
        long diff = expiresAt - System.currentTimeMillis();
        return Math.max(0, diff);
    }
}
