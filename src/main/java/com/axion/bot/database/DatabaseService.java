package com.axion.bot.database;

import com.axion.bot.moderation.ModerationLog;
import com.axion.bot.moderation.ModerationAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service klasse til database operationer
 * Håndterer alle CRUD operationer for bot data
 */
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private final DatabaseManager databaseManager;

    public DatabaseService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Henter database forbindelse
     */
    public Connection getConnection() {
        return databaseManager.getConnection();
    }

    // ==================== WARNING OPERATIONS ====================

    /**
     * Tilføjer en advarsel til databasen
     */
    public void addWarning(String userId, String guildId, String reason, String moderatorId) {
        String sql = "INSERT INTO warnings (user_id, guild_id, reason, moderator_id) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setString(3, reason);
            stmt.setString(4, moderatorId);
            stmt.executeUpdate();
            
            logger.info("Advarsel tilføjet til database for bruger: {} i guild: {}", userId, guildId);
        } catch (SQLException e) {
            logger.error("Fejl ved tilføjelse af advarsel", e);
        }
    }

    /**
     * Får antal advarsler for en bruger i en guild
     */
    public int getWarningCount(String userId, String guildId) {
        String sql = "SELECT COUNT(*) FROM warnings WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af advarsel antal", e);
        }
        return 0;
    }

    /**
     * Fjerner alle advarsler for en bruger i en guild
     */
    public void clearWarnings(String userId, String guildId) {
        String sql = "DELETE FROM warnings WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            int deleted = stmt.executeUpdate();
            
            logger.info("Fjernede {} advarsler for bruger: {} i guild: {}", deleted, userId, guildId);
        } catch (SQLException e) {
            logger.error("Fejl ved fjernelse af advarsler", e);
        }
    }

    // ==================== MODERATION LOG OPERATIONS ====================

    /**
     * Logger en moderation handling
     */
    public void logModerationAction(String userId, String username, String moderatorId, String moderatorName,
                                   String action, String reason, String guildId, String channelId,
                                   String messageId, int severity, boolean automated) {
        String sql = "INSERT INTO moderation_logs " +
                     "(user_id, username, moderator_id, moderator_name, action, reason, " +
                     "guild_id, channel_id, message_id, severity, automated) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection connection = databaseManager.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null. Kan ikke logge moderation handling.");
            return;
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, moderatorId);
            stmt.setString(4, moderatorName);
            stmt.setString(5, action);
            stmt.setString(6, reason);
            stmt.setString(7, guildId);
            stmt.setString(8, channelId);
            stmt.setString(9, messageId);
            stmt.setInt(10, severity);
            stmt.setBoolean(11, automated);
            stmt.executeUpdate();
            
            logger.info("Moderation handling logget: {} - {} - {}", username, action, reason);
        } catch (SQLException e) {
            logger.error("Fejl ved logging af moderation handling", e);
        }
    }

    /**
     * Henter moderation logs for en bruger
     */
    public List<ModerationLog> getModerationLogs(String userId, String guildId, int limit) {
        List<ModerationLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM moderation_logs " +
                     "WHERE user_id = ? AND guild_id = ? " +
                     "ORDER BY timestamp DESC LIMIT ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setInt(3, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String actionString = rs.getString("action");
                    ModerationAction action = convertStringToModerationAction(actionString);
                    
                    ModerationLog log = new ModerationLog(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("moderator_id"),
                        rs.getString("moderator_name"),
                        action,
                        rs.getString("reason"),
                        rs.getString("guild_id"),
                        rs.getString("channel_id"),
                        rs.getString("message_id"),
                        rs.getInt("severity"),
                        rs.getBoolean("automated"),
                        rs.getTimestamp("timestamp").toInstant()
                    );
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af moderation logs", e);
        }
        return logs;
    }

    // ==================== USER VIOLATION OPERATIONS ====================

    /**
     * Øger violation count for en bruger
     */
    public void incrementViolationCount(String userId, String guildId) {
        String sql = "INSERT INTO user_violations (user_id, guild_id, violation_count, last_violation) " +
                     "VALUES (?, ?, 1, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT(user_id) DO UPDATE SET " +
                     "violation_count = violation_count + 1, " +
                     "last_violation = CURRENT_TIMESTAMP";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.executeUpdate();
            
            logger.info("Violation count øget for bruger: {} i guild: {}", userId, guildId);
        } catch (SQLException e) {
            logger.error("Fejl ved øgning af violation count", e);
        }
    }

    /**
     * Får violation count for en bruger
     */
    public int getViolationCount(String userId, String guildId) {
        String sql = "SELECT violation_count FROM user_violations WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("violation_count");
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af violation count", e);
        }
        return 0;
    }

    /**
     * Nulstiller violation count for en bruger
     */
    public void resetViolationCount(String userId, String guildId) {
        String sql = "DELETE FROM user_violations WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.executeUpdate();
            
            logger.info("Violation count nulstillet for bruger: {} i guild: {}", userId, guildId);
        } catch (SQLException e) {
            logger.error("Fejl ved nulstilling af violation count", e);
        }
    }

    // ==================== TEMP BAN OPERATIONS ====================

    /**
     * Tilføjer en temp ban
     */
    public void addTempBan(String userId, String guildId, Instant expiresAt, String reason) {
        String sql = "INSERT INTO temp_bans (user_id, guild_id, expires_at, reason) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT(user_id, guild_id) DO UPDATE SET " +
                     "expires_at = ?, reason = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setTimestamp(3, Timestamp.from(expiresAt));
            stmt.setString(4, reason);
            stmt.setTimestamp(5, Timestamp.from(expiresAt));
            stmt.setString(6, reason);
            stmt.executeUpdate();
            
            logger.info("Temp ban tilføjet for bruger: {} i guild: {} indtil: {}", userId, guildId, expiresAt);
        } catch (SQLException e) {
            logger.error("Fejl ved tilføjelse af temp ban", e);
        }
    }

    /**
     * Tjekker om en bruger er temp banned
     */
    public boolean isTempBanned(String userId, String guildId) {
        String sql = "SELECT expires_at FROM temp_bans WHERE user_id = ? AND guild_id = ? AND expires_at > CURRENT_TIMESTAMP";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Fejl ved tjek af temp ban status", e);
        }
        return false;
    }

    /**
     * Fjerner temp ban for en bruger
     */
    public void removeTempBan(String userId, String guildId) {
        String sql = "DELETE FROM temp_bans WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.executeUpdate();
            
            logger.info("Temp ban fjernet for bruger: {} i guild: {}", userId, guildId);
        } catch (SQLException e) {
            logger.error("Fejl ved fjernelse af temp ban", e);
        }
    }

    /**
     * Henter alle aktive temp bans
     */
    public Map<String, Instant> getActiveTempBans() {
        Map<String, Instant> activeBans = new HashMap<>();
        String sql = "SELECT user_id, expires_at FROM temp_bans WHERE expires_at > CURRENT_TIMESTAMP";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    Instant expiresAt = rs.getTimestamp("expires_at").toInstant();
                    activeBans.put(userId, expiresAt);
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af aktive temp bans", e);
        }
        return activeBans;
    }

    /**
     * Rydder op i udløbne temp bans
     */
    public void cleanupExpiredTempBans() {
        String sql = "DELETE FROM temp_bans WHERE expires_at <= CURRENT_TIMESTAMP";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                logger.info("Ryddede op i {} udløbne temp bans", deleted);
            }
        } catch (SQLException e) {
            logger.error("Fejl ved oprydning af udløbne temp bans", e);
        }
    }
    
    /**
     * Konverterer en string til ModerationAction enum
     */
    private ModerationAction convertStringToModerationAction(String actionString) {
        if (actionString == null) {
            return ModerationAction.NONE;
        }
        
        try {
            // Prøv at matche direkte med enum navn
            return ModerationAction.valueOf(actionString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Hvis direkte match fejler, prøv at matche baseret på beskrivelse
            switch (actionString.toLowerCase()) {
                 case "warn":
                 case "warning":
                 case "advarsel":
                     return ModerationAction.WARN_USER;
                 case "kick":
                     return ModerationAction.KICK;
                 case "ban":
                     return ModerationAction.BAN;
                 case "timeout":
                 case "mute":
                     return ModerationAction.TIMEOUT;
                 case "delete":
                 case "delete_message":
                     return ModerationAction.DELETE_MESSAGE;
                 default:
                     logger.warn("Ukendt moderation action: {}, bruger NONE", actionString);
                     return ModerationAction.NONE;
             }
        }
    }

    // ==================== SERVER CONFIG OPERATIONS ====================

    /**
     * Henter server konfiguration
     */
    public Map<String, Object> getServerConfig(String guildId) {
        Map<String, Object> config = new HashMap<>();
        String sql = "SELECT * FROM server_config WHERE guild_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    config.put("moderation_level", rs.getString("moderation_level"));
                    config.put("auto_moderation", rs.getBoolean("auto_moderation"));
                    config.put("spam_protection", rs.getBoolean("spam_protection"));
                    config.put("toxic_detection", rs.getBoolean("toxic_detection"));
                    config.put("link_protection", rs.getBoolean("link_protection"));
                    config.put("max_messages_per_minute", rs.getInt("max_messages_per_minute"));
                    config.put("max_links_per_message", rs.getInt("max_links_per_message"));
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af server konfiguration", e);
        }
        return config;
    }

    /**
     * Opdaterer server konfiguration
     */
    public void updateServerConfig(String guildId, Map<String, Object> config) {
        String sql = "INSERT INTO server_config " +
                     "(guild_id, moderation_level, auto_moderation, spam_protection, " +
                     "toxic_detection, link_protection, max_messages_per_minute, max_links_per_message) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT(guild_id) DO UPDATE SET " +
                     "moderation_level = ?, auto_moderation = ?, spam_protection = ?, " +
                     "toxic_detection = ?, link_protection = ?, max_messages_per_minute = ?, " +
                     "max_links_per_message = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            // Insert values
            stmt.setString(1, guildId);
            stmt.setString(2, (String) config.getOrDefault("moderation_level", "standard"));
            stmt.setBoolean(3, (Boolean) config.getOrDefault("auto_moderation", true));
            stmt.setBoolean(4, (Boolean) config.getOrDefault("spam_protection", true));
            stmt.setBoolean(5, (Boolean) config.getOrDefault("toxic_detection", true));
            stmt.setBoolean(6, (Boolean) config.getOrDefault("link_protection", false));
            stmt.setInt(7, (Integer) config.getOrDefault("max_messages_per_minute", 10));
            stmt.setInt(8, (Integer) config.getOrDefault("max_links_per_message", 3));
            
            // Update values
            stmt.setString(9, (String) config.getOrDefault("moderation_level", "standard"));
            stmt.setBoolean(10, (Boolean) config.getOrDefault("auto_moderation", true));
            stmt.setBoolean(11, (Boolean) config.getOrDefault("spam_protection", true));
            stmt.setBoolean(12, (Boolean) config.getOrDefault("toxic_detection", true));
            stmt.setBoolean(13, (Boolean) config.getOrDefault("link_protection", false));
            stmt.setInt(14, (Integer) config.getOrDefault("max_messages_per_minute", 10));
            stmt.setInt(15, (Integer) config.getOrDefault("max_links_per_message", 3));
            
            stmt.executeUpdate();
            logger.info("Server konfiguration opdateret for guild: {}", guildId);
        } catch (SQLException e) {
            logger.error("Fejl ved opdatering af server konfiguration", e);
        }
    }

    // ==================== USER LANGUAGE OPERATIONS ====================

    /**
     * Sætter bruger sprog
     */
    public void setUserLanguage(String userId, String languageCode) {
        String sql = "INSERT INTO user_languages (user_id, language_code) " +
                     "VALUES (?, ?) " +
                     "ON CONFLICT(user_id) DO UPDATE SET " +
                     "language_code = ?, updated_at = CURRENT_TIMESTAMP";
        
        Connection connection = databaseManager.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null. Kan ikke sætte bruger sprog.");
            return;
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, languageCode);
            stmt.setString(3, languageCode);
            stmt.executeUpdate();
            
            logger.info("Bruger sprog sat til {} for bruger: {}", languageCode, userId);
        } catch (SQLException e) {
            logger.error("Fejl ved sætning af bruger sprog", e);
        }
    }

    /**
     * Henter bruger sprog
     */
    public String getUserLanguage(String userId) {
        String sql = "SELECT language_code FROM user_languages WHERE user_id = ?";
        
        Connection connection = databaseManager.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null. Returnerer standard sprog.");
            return "en"; // Default to English
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("language_code");
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af bruger sprog for bruger: " + userId, e);
        }
        
        return "en"; // Default to English if not found or error
    }

    /**
     * Removes user language setting (resets to default)
     */
    public void removeUserLanguage(String userId) {
        String sql = "DELETE FROM user_languages WHERE user_id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("Error removing user language for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Henter alle brugerspecifikke sprogindstillinger
     */
    public Map<String, String> getAllUserLanguages() {
        Map<String, String> languages = new HashMap<>();
        String sql = "SELECT user_id, language_code FROM user_languages";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    languages.put(rs.getString("user_id"), rs.getString("language_code"));
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af alle bruger sprog", e);
        }
        return languages;
    }
}