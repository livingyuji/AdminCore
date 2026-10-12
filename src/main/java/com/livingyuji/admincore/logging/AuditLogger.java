/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.logging;

import com.livingyuji.admincore.storage.DatabaseService;
import com.livingyuji.admincore.util.TimeUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages administrative audit logging, searching, and disk export.
 *
 * @author LivingYuji
 */
public class AuditLogger {

    private final JavaPlugin plugin;
    private final DatabaseService databaseService;

    public AuditLogger(JavaPlugin plugin, DatabaseService databaseService) {
        this.plugin = plugin;
        this.databaseService = databaseService;
    }

    /**
     * Records an administrative action in the audit log asynchronously.
     */
    public void log(String actor, String action, String details) {
        long now = System.currentTimeMillis();
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_audit_logs (actor, action, details, timestamp) VALUES (?, ?, ?, ?)"
                 )) {
                stmt.setString(1, actor);
                stmt.setString(2, action);
                stmt.setString(3, details);
                stmt.setLong(4, now);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to record audit log entry", e);
            }
        });
    }

    /**
     * Searches recent audit logs matching a keyword or player name.
     */
    public List<String> searchLogs(String filter, int limit) {
        List<String> results = new ArrayList<>();
        String sql = (filter == null || filter.trim().isEmpty())
                ? "SELECT * FROM ac_audit_logs ORDER BY timestamp DESC LIMIT ?"
                : "SELECT * FROM ac_audit_logs WHERE actor LIKE ? OR action LIKE ? OR details LIKE ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filter == null || filter.trim().isEmpty()) {
                stmt.setInt(1, limit);
            } else {
                String pattern = "%" + filter + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
                stmt.setInt(4, limit);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String formatted = String.format("[%s] %s executed %s: %s",
                            TimeUtil.formatTimestamp(rs.getLong("timestamp")),
                            rs.getString("actor"),
                            rs.getString("action"),
                            rs.getString("details")
                    );
                    results.add(formatted);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query audit logs", e);
        }

        return results;
    }

    /**
     * Exports all audit logs to a text file in plugin data folder.
     */
    public File exportLogs() throws IOException, SQLException {
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        File exportFile = new File(logsDir, "audit-export-" + System.currentTimeMillis() + ".txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile));
             Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ac_audit_logs ORDER BY timestamp ASC");
             ResultSet rs = stmt.executeQuery()) {

            writer.println("# AdminCore Audit Log Export");
            writer.println("# Generated at: " + TimeUtil.formatTimestamp(System.currentTimeMillis()));
            writer.println("# Developed by LivingYuji");
            writer.println("----------------------------------------------------------------------");

            while (rs.next()) {
                writer.println(String.format("[%s] Actor: %s | Action: %s | Details: %s",
                        TimeUtil.formatTimestamp(rs.getLong("timestamp")),
                        rs.getString("actor"),
                        rs.getString("action"),
                        rs.getString("details")
                ));
            }
        }

        return exportFile;
    }
}
