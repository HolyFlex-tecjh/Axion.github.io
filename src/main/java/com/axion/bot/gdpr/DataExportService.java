package com.axion.bot.gdpr;

import com.axion.bot.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Service for exporting user data in compliance with GDPR Article 20 (Right to data portability)
 */
public class DataExportService {
    private static final Logger logger = LoggerFactory.getLogger(DataExportService.class);
    
    private final DatabaseService databaseService;
    
    public DataExportService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    /**
     * Exports all user data in the specified format
     */
    public UserDataExport exportUserData(String userId, String guildId) {
        logger.info("Starting data export for user {} in guild {}", userId, guildId);
        
        UserDataExport export = new UserDataExport(userId, guildId);
        
        // Export consent data
        exportConsentData(export, userId, guildId);
        
        // Export moderation data
        exportModerationData(export, userId, guildId);
        
        // Export user activity data
        exportActivityData(export, userId, guildId);
        
        // Export user preferences
        exportPreferencesData(export, userId, guildId);
        
        logger.info("Completed data export for user {} in guild {}", userId, guildId);
        return export;
    }
    
    /**
     * Exports user data as a formatted string
     */
    public String exportUserDataAsString(String userId, String guildId, String format) {
        UserDataExport export = exportUserData(userId, guildId);
        
        switch (format.toLowerCase()) {
            case "json":
                return export.toJson();
            case "readable":
                return export.toReadableFormat();
            case "csv":
                return export.toCsv();
            default:
                return export.toReadableFormat();
        }
    }
    
    /**
     * Exports user consent data
     */
    private void exportConsentData(UserDataExport export, String userId, String guildId) {
        String sql = "SELECT purposes, consent_timestamp, consent_method, is_active, withdrawal_timestamp " +
                    "FROM gdpr_user_consent WHERE user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> consentData = new HashMap<>();
                    consentData.put("purposes", rs.getString("purposes"));
                    consentData.put("consent_timestamp", formatTimestamp(rs.getLong("consent_timestamp")));
                    consentData.put("consent_method", rs.getString("consent_method"));
                    consentData.put("is_active", rs.getBoolean("is_active"));
                    
                    long withdrawalTs = rs.getLong("withdrawal_timestamp");
                    if (withdrawalTs > 0) {
                        consentData.put("withdrawal_timestamp", formatTimestamp(withdrawalTs));
                    }
                    
                    export.addConsentRecord(consentData);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to export consent data for user {}", userId, e);
        }
    }
    
    /**
     * Exports user moderation data
     */
    private void exportModerationData(UserDataExport export, String userId, String guildId) {
        String sql = "SELECT action_type, reason, moderator_id, timestamp, duration " +
                    "FROM moderation_logs WHERE user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> moderationData = new HashMap<>();
                    moderationData.put("action_type", rs.getString("action_type"));
                    moderationData.put("reason", rs.getString("reason"));
                    moderationData.put("moderator_id", rs.getString("moderator_id"));
                    moderationData.put("timestamp", formatTimestamp(rs.getLong("timestamp")));
                    moderationData.put("duration", rs.getLong("duration"));
                    
                    export.addModerationRecord(moderationData);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to export moderation data for user {}", userId, e);
        }
    }
    
    /**
     * Exports user activity data
     */
    private void exportActivityData(UserDataExport export, String userId, String guildId) {
        String sql = "SELECT activity_type, content, timestamp, channel_id " +
                    "FROM user_activity WHERE user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> activityData = new HashMap<>();
                    activityData.put("activity_type", rs.getString("activity_type"));
                    activityData.put("content", rs.getString("content"));
                    activityData.put("timestamp", formatTimestamp(rs.getLong("timestamp")));
                    activityData.put("channel_id", rs.getString("channel_id"));
                    
                    export.addActivityRecord(activityData);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to export activity data for user {}", userId, e);
        }
    }
    
    /**
     * Exports user preferences data
     */
    private void exportPreferencesData(UserDataExport export, String userId, String guildId) {
        String sql = "SELECT preference_key, preference_value, updated_timestamp " +
                    "FROM user_preferences WHERE user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> preferenceData = new HashMap<>();
                    preferenceData.put("preference_key", rs.getString("preference_key"));
                    preferenceData.put("preference_value", rs.getString("preference_value"));
                    preferenceData.put("updated_timestamp", formatTimestamp(rs.getLong("updated_timestamp")));
                    
                    export.addPreferenceRecord(preferenceData);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to export preferences data for user {}", userId, e);
        }
    }
    
    /**
     * Formats timestamp for export
     */
    private String formatTimestamp(long epochSecond) {
        if (epochSecond <= 0) {
            return null;
        }
        return Instant.ofEpochSecond(epochSecond)
                     .atZone(java.time.ZoneOffset.UTC)
                     .format(DateTimeFormatter.ISO_INSTANT);
    }
}