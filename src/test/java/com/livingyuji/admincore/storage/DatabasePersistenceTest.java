/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DatabasePersistenceTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_punishments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid TEXT NOT NULL,
                    target_name TEXT NOT NULL,
                    staff_uuid TEXT NOT NULL,
                    staff_name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    actor TEXT NOT NULL,
                    action TEXT NOT NULL,
                    details TEXT NOT NULL,
                    timestamp INTEGER NOT NULL
                );
            """);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Should successfully insert and query punishment records")
    void testInsertAndQueryPunishment() throws SQLException {
        UUID target = UUID.randomUUID();
        long now = System.currentTimeMillis();

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO ac_punishments (target_uuid, target_name, staff_uuid, staff_name, type, reason, created_at, expires_at, active) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)")) {
            stmt.setString(1, target.toString());
            stmt.setString(2, "TestPlayer");
            stmt.setString(3, "CONSOLE");
            stmt.setString(4, "CONSOLE");
            stmt.setString(5, "BAN");
            stmt.setString(6, "Hacking test");
            stmt.setLong(7, now);
            stmt.setLong(8, -1);
            assertEquals(1, stmt.executeUpdate());
        }

        try (PreparedStatement query = connection.prepareStatement(
                "SELECT * FROM ac_punishments WHERE target_uuid = ? AND active = 1")) {
            query.setString(1, target.toString());
            try (ResultSet rs = query.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("TestPlayer", rs.getString("target_name"));
                assertEquals("BAN", rs.getString("type"));
                assertEquals("Hacking test", rs.getString("reason"));
                assertEquals(1, rs.getInt("active"));
            }
        }
    }

    @Test
    @DisplayName("Should successfully record audit logs")
    void testAuditLogs() throws SQLException {
        long now = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO ac_audit_logs (actor, action, details, timestamp) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, "AdminUser");
            stmt.setString(2, "RELOAD");
            stmt.setString(3, "Reloaded config.yml");
            stmt.setLong(4, now);
            assertEquals(1, stmt.executeUpdate());
        }

        try (Statement query = connection.createStatement();
             ResultSet rs = query.executeQuery("SELECT COUNT(*) FROM ac_audit_logs")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }
}
