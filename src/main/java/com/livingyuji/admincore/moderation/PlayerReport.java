/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.moderation;

import java.util.UUID;

/**
 * Record representing a user report against another player.
 *
 * @author LivingYuji
 */
public class PlayerReport {

    private final long id;
    private final UUID reporterUuid;
    private final String reporterName;
    private final UUID reportedUuid;
    private final String reportedName;
    private final String reason;
    private final long createdAt;
    private String status; // "OPEN", "RESOLVED"

    public PlayerReport(long id, UUID reporterUuid, String reporterName, UUID reportedUuid,
                        String reportedName, String reason, long createdAt, String status) {
        this.id = id;
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.reportedUuid = reportedUuid;
        this.reportedName = reportedName;
        this.reason = reason;
        this.createdAt = createdAt;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public UUID getReporterUuid() {
        return reporterUuid;
    }

    public String getReporterName() {
        return reporterName;
    }

    public UUID getReportedUuid() {
        return reportedUuid;
    }

    public String getReportedName() {
        return reportedName;
    }

    public String getReason() {
        return reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }
}
