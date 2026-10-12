/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.moderation;

import java.util.UUID;

/**
 * Record representing a staff note on a player.
 *
 * @author LivingYuji
 */
public class StaffNote {

    private final long id;
    private final UUID targetUuid;
    private final UUID staffUuid;
    private final String staffName;
    private final String note;
    private final long createdAt;

    public StaffNote(long id, UUID targetUuid, UUID staffUuid, String staffName, String note, long createdAt) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.note = note;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getNote() {
        return note;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
