/*
 * AdminCore - All-in-One Minecraft Server Administration Plugin
 * Copyright (C) 2026 LivingYuji. All rights reserved.
 *
 * Developed and credited entirely to LivingYuji.
 */
package com.livingyuji.admincore.moderation;

import com.livingyuji.admincore.storage.DatabaseService;
import com.livingyuji.admincore.util.DurationParser;
import com.livingyuji.admincore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages disciplinary actions, active mutes/bans caches, notes, and player reports.
 *
 * @author LivingYuji
 */
public class ModerationManager {

    private final JavaPlugin plugin;
    private final DatabaseService databaseService;

    // Fast in-memory caches for active punishments
    private final Map<UUID, Punishment> activeMutes = new ConcurrentHashMap<>();
    private final Map<UUID, Punishment> activeBans = new ConcurrentHashMap<>();

    public ModerationManager(JavaPlugin plugin, DatabaseService databaseService) {
        this.plugin = plugin;
        this.databaseService = databaseService;
    }

    /**
     * Loads active punishments from the database into in-memory cache.
     */
    public void loadActivePunishments() {
        activeMutes.clear();
        activeBans.clear();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM ac_punishments WHERE active = 1 AND (expires_at = -1 OR expires_at > ?)"
             )) {
            stmt.setLong(1, System.currentTimeMillis());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Punishment punishment = mapPunishment(rs);
                    if (punishment.getType() == PunishmentType.MUTE) {
                        activeMutes.put(punishment.getTargetUuid(), punishment);
                    } else if (punishment.getType() == PunishmentType.BAN || punishment.getType() == PunishmentType.TEMPBAN) {
                        activeBans.put(punishment.getTargetUuid(), punishment);
                    }
                }
            }
            plugin.getLogger().info("Loaded " + activeMutes.size() + " active mutes and " + activeBans.size() + " active bans.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load active punishments from database", e);
        }
    }

    private Punishment mapPunishment(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        UUID targetUuid = UUID.fromString(rs.getString("target_uuid"));
        String targetName = rs.getString("target_name");
        String staffUuidStr = rs.getString("staff_uuid");
        UUID staffUuid = (staffUuidStr == null || staffUuidStr.isEmpty() || staffUuidStr.equals("CONSOLE"))
                ? null : UUID.fromString(staffUuidStr);
        String staffName = rs.getString("staff_name");
        PunishmentType type = PunishmentType.valueOf(rs.getString("type"));
        String reason = rs.getString("reason");
        long createdAt = rs.getLong("created_at");
        long expiresAt = rs.getLong("expires_at");
        boolean active = rs.getInt("active") == 1;

        return new Punishment(id, targetUuid, targetName, staffUuid, staffName, type, reason, createdAt, expiresAt, active);
    }

    /**
     * Warns an online or offline player.
     */
    public void warn(UUID targetUuid, String targetName, UUID staffUuid, String staffName, String reason) {
        long now = System.currentTimeMillis();
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_punishments (target_uuid, target_name, staff_uuid, staff_name, type, reason, created_at, expires_at, active) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                 )) {
                stmt.setString(1, targetUuid.toString());
                stmt.setString(2, targetName);
                stmt.setString(3, staffUuid != null ? staffUuid.toString() : "CONSOLE");
                stmt.setString(4, staffName);
                stmt.setString(5, PunishmentType.WARN.name());
                stmt.setString(6, reason);
                stmt.setLong(7, now);
                stmt.setLong(8, -1);
                stmt.setInt(9, 1);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist warning", e);
            }
        });

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(TextUtil.toComponent("&c&l[WARNING]&r &eYou have been warned by &f" + staffName + "&e for: &f" + reason));
        }
    }

    /**
     * Mutes a player temporarily or permanently.
     */
    public void mute(UUID targetUuid, String targetName, UUID staffUuid, String staffName, String reason, long durationMillis) {
        long now = System.currentTimeMillis();
        long expiresAt = durationMillis > 0 ? now + durationMillis : -1;
        Punishment punishment = new Punishment(0, targetUuid, targetName, staffUuid, staffName, PunishmentType.MUTE, reason, now, expiresAt, true);

        activeMutes.put(targetUuid, punishment);

        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection()) {
                // Deactivate any previous active mutes for target
                try (PreparedStatement deactivate = conn.prepareStatement(
                        "UPDATE ac_punishments SET active = 0 WHERE target_uuid = ? AND type = 'MUTE' AND active = 1")) {
                    deactivate.setString(1, targetUuid.toString());
                    deactivate.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO ac_punishments (target_uuid, target_name, staff_uuid, staff_name, type, reason, created_at, expires_at, active) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )) {
                    stmt.setString(1, targetUuid.toString());
                    stmt.setString(2, targetName);
                    stmt.setString(3, staffUuid != null ? staffUuid.toString() : "CONSOLE");
                    stmt.setString(4, staffName);
                    stmt.setString(5, PunishmentType.MUTE.name());
                    stmt.setString(6, reason);
                    stmt.setLong(7, now);
                    stmt.setLong(8, expiresAt);
                    stmt.setInt(9, 1);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist mute", e);
            }
        });

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            String durationStr = durationMillis > 0 ? DurationParser.formatDuration(durationMillis) : "Permanent";
            onlineTarget.sendMessage(TextUtil.toComponent("&c&l[MUTED]&r &eYou were muted by &f" + staffName +
                    "&e for &f" + durationStr + "&e. Reason: &f" + reason));
        }
    }

    /**
     * Unmutes a player.
     */
    public boolean unmute(UUID targetUuid, String staffName) {
        Punishment existing = activeMutes.remove(targetUuid);
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE ac_punishments SET active = 0 WHERE target_uuid = ? AND type = 'MUTE' AND active = 1")) {
                stmt.setString(1, targetUuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to deactivate mute", e);
            }
        });

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(TextUtil.toComponent("&a&l[UNMUTED]&r &aYou have been unmuted by &f" + staffName));
        }
        return existing != null;
    }

    /**
     * Kicks a player immediately.
     */
    public void kick(Player player, String staffName, String reason) {
        long now = System.currentTimeMillis();
        UUID targetUuid = player.getUniqueId();
        String targetName = player.getName();

        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_punishments (target_uuid, target_name, staff_uuid, staff_name, type, reason, created_at, expires_at, active) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                 )) {
                stmt.setString(1, targetUuid.toString());
                stmt.setString(2, targetName);
                stmt.setString(3, "CONSOLE");
                stmt.setString(4, staffName);
                stmt.setString(5, PunishmentType.KICK.name());
                stmt.setString(6, reason);
                stmt.setLong(7, now);
                stmt.setLong(8, -1);
                stmt.setInt(9, 0); // Kicks are instantaneous
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist kick", e);
            }
        });

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.kick(TextUtil.toComponent("&c&lKicked by " + staffName + "\n\n&eReason: &f" + reason));
        });
    }

    /**
     * Bans a player permanently or temporarily.
     */
    public void ban(UUID targetUuid, String targetName, UUID staffUuid, String staffName, String reason, long durationMillis) {
        long now = System.currentTimeMillis();
        long expiresAt = durationMillis > 0 ? now + durationMillis : -1;
        PunishmentType type = durationMillis > 0 ? PunishmentType.TEMPBAN : PunishmentType.BAN;
        Punishment punishment = new Punishment(0, targetUuid, targetName, staffUuid, staffName, type, reason, now, expiresAt, true);

        activeBans.put(targetUuid, punishment);

        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection()) {
                try (PreparedStatement deactivate = conn.prepareStatement(
                        "UPDATE ac_punishments SET active = 0 WHERE target_uuid = ? AND (type = 'BAN' OR type = 'TEMPBAN') AND active = 1")) {
                    deactivate.setString(1, targetUuid.toString());
                    deactivate.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO ac_punishments (target_uuid, target_name, staff_uuid, staff_name, type, reason, created_at, expires_at, active) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )) {
                    stmt.setString(1, targetUuid.toString());
                    stmt.setString(2, targetName);
                    stmt.setString(3, staffUuid != null ? staffUuid.toString() : "CONSOLE");
                    stmt.setString(4, staffName);
                    stmt.setString(5, type.name());
                    stmt.setString(6, reason);
                    stmt.setLong(7, now);
                    stmt.setLong(8, expiresAt);
                    stmt.setInt(9, 1);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist ban", e);
            }
        });

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            String durationStr = durationMillis > 0 ? DurationParser.formatDuration(durationMillis) : "Permanent";
            Bukkit.getScheduler().runTask(plugin, () -> {
                onlineTarget.kick(TextUtil.toComponent("&c&lBanned from server\n\n&eBy: &f" + staffName +
                        "\n&eDuration: &f" + durationStr + "\n&eReason: &f" + reason));
            });
        }
    }

    /**
     * Unbans a player.
     */
    public boolean unban(UUID targetUuid, String staffName) {
        Punishment existing = activeBans.remove(targetUuid);
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE ac_punishments SET active = 0 WHERE target_uuid = ? AND (type = 'BAN' OR type = 'TEMPBAN') AND active = 1")) {
                stmt.setString(1, targetUuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to deactivate ban", e);
            }
        });
        return existing != null;
    }

    public Punishment getActiveMute(UUID targetUuid) {
        Punishment mute = activeMutes.get(targetUuid);
        if (mute != null && mute.isExpired()) {
            activeMutes.remove(targetUuid);
            unmute(targetUuid, "SYSTEM_EXPIRED");
            return null;
        }
        return mute;
    }

    public Punishment getActiveBan(UUID targetUuid) {
        Punishment ban = activeBans.get(targetUuid);
        if (ban != null && ban.isExpired()) {
            activeBans.remove(targetUuid);
            unban(targetUuid, "SYSTEM_EXPIRED");
            return null;
        }
        return ban;
    }

    /**
     * Adds a staff note.
     */
    public void addNote(UUID targetUuid, UUID staffUuid, String staffName, String note) {
        long now = System.currentTimeMillis();
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_staff_notes (target_uuid, staff_uuid, note, created_at) VALUES (?, ?, ?, ?)"
                 )) {
                stmt.setString(1, targetUuid.toString());
                stmt.setString(2, staffUuid != null ? staffUuid.toString() : "CONSOLE");
                stmt.setString(3, note);
                stmt.setLong(4, now);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to persist staff note", e);
            }
        });
    }

    /**
     * Retrieves all punishment history for a player synchronously (safe for background workers).
     */
    public List<Punishment> getHistory(UUID targetUuid) {
        List<Punishment> list = new ArrayList<>();
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM ac_punishments WHERE target_uuid = ? ORDER BY created_at DESC"
             )) {
            stmt.setString(1, targetUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPunishment(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load punishment history", e);
        }
        return list;
    }

    /**
     * Retrieves all staff notes for a player.
     */
    public List<StaffNote> getNotes(UUID targetUuid) {
        List<StaffNote> list = new ArrayList<>();
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM ac_staff_notes WHERE target_uuid = ? ORDER BY created_at DESC"
             )) {
            stmt.setString(1, targetUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String sUuid = rs.getString("staff_uuid");
                    UUID staffUuid = (sUuid == null || sUuid.equals("CONSOLE")) ? null : UUID.fromString(sUuid);
                    String note = rs.getString("note");
                    long createdAt = rs.getLong("created_at");
                    list.add(new StaffNote(id, targetUuid, staffUuid, sUuid, note, createdAt));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load staff notes", e);
        }
        return list;
    }

    /**
     * Submits a player report.
     */
    public void submitReport(UUID reporterUuid, String reporterName, UUID reportedUuid, String reportedName, String reason) {
        long now = System.currentTimeMillis();
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ac_player_reports (reporter_uuid, reporter_name, reported_uuid, reported_name, reason, created_at, status) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?)"
                 )) {
                stmt.setString(1, reporterUuid.toString());
                stmt.setString(2, reporterName);
                stmt.setString(3, reportedUuid.toString());
                stmt.setString(4, reportedName);
                stmt.setString(5, reason);
                stmt.setLong(6, now);
                stmt.setString(7, "OPEN");
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to submit player report", e);
            }
        });
    }

    /**
     * Retrieves all reports with a specific status.
     */
    public List<PlayerReport> getReports(String statusFilter) {
        List<PlayerReport> list = new ArrayList<>();
        String query = statusFilter == null ?
                "SELECT * FROM ac_player_reports ORDER BY created_at DESC" :
                "SELECT * FROM ac_player_reports WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (statusFilter != null) {
                stmt.setString(1, statusFilter.toUpperCase());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PlayerReport(
                            rs.getLong("id"),
                            UUID.fromString(rs.getString("reporter_uuid")),
                            rs.getString("reporter_name"),
                            UUID.fromString(rs.getString("reported_uuid")),
                            rs.getString("reported_name"),
                            rs.getString("reason"),
                            rs.getLong("created_at"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query player reports", e);
        }
        return list;
    }

    /**
     * Resolves a report by ID.
     */
    public void resolveReport(long reportId) {
        databaseService.executeAsync(() -> {
            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE ac_player_reports SET status = 'RESOLVED' WHERE id = ?"
                 )) {
                stmt.setLong(1, reportId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to resolve report", e);
            }
        });
    }
}
