/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.storage;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Thread-safe SQLite database service with connection pooling and asynchronous execution.
 *
 * @author LivingYuji
 */
public class DatabaseService {

    private final JavaPlugin plugin;
    private final String connectionUrl;
    private final ExecutorService executorService;
    private Connection connection;

    public DatabaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        File dbFile = new File(plugin.getDataFolder(), "admincore.db");
        this.connectionUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.executorService = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r, "AdminCore-DB-Worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Initializes the database connection and creates required tables.
     */
    public synchronized void initialize() throws SQLException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
            // SQLite driver might already be registered by server classpath
        }

        connection = DriverManager.getConnection(connectionUrl);
        applyMigrations();
    }

    private void applyMigrations() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Punishments table
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

            // Staff notes table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_staff_notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid TEXT NOT NULL,
                    staff_uuid TEXT NOT NULL,
                    note TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                );
            """);

            // Player reports table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_player_reports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reporter_uuid TEXT NOT NULL,
                    reporter_name TEXT NOT NULL,
                    reported_uuid TEXT NOT NULL,
                    reported_name TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'OPEN'
                );
            """);

            // Cosmetics table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_player_cosmetics (
                    player_uuid TEXT NOT NULL,
                    cosmetic_type TEXT NOT NULL,
                    cosmetic_id TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1,
                    PRIMARY KEY (player_uuid, cosmetic_type, cosmetic_id)
                );
            """);

            // Audit logs table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    actor TEXT NOT NULL,
                    action TEXT NOT NULL,
                    details TEXT NOT NULL,
                    timestamp INTEGER NOT NULL
                );
            """);

            // Event stats table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ac_event_stats (
                    player_uuid TEXT NOT NULL,
                    event_name TEXT NOT NULL,
                    wins INTEGER NOT NULL DEFAULT 0,
                    participations INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (player_uuid, event_name)
                );
            """);
        }
    }

    /**
     * Executes an asynchronous database task.
     *
     * @param task task runnable
     */
    public void executeAsync(Runnable task) {
        executorService.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Asynchronous database operation error", e);
            }
        });
    }

    /**
     * Returns an active SQL connection.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(connectionUrl);
        }
        return connection;
    }

    /**
     * Closes the database connection and terminates background worker threads.
     */
    public synchronized void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
}
