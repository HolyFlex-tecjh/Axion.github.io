package com.axion.bot.gdpr;

import com.axion.bot.database.DatabaseService;
import com.axion.bot.gdpr.GDPRComplianceManager.DataProcessingPurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Manages user consent records in the database
 */
public class ConsentManager {
    private static final Logger logger = LoggerFactory.getLogger(ConsentManager.class);
    
    private final DatabaseService databaseService;
    
    public ConsentManager(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    /**
     * Stores user consent in the database
     */
    public boolean storeConsent(UserConsent consent) {
        String sql = "INSERT INTO gdpr_user_consent (user_id, guild_id, purposes, consent_timestamp, consent_method, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "purposes = VALUES(purposes), consent_timestamp = VALUES(consent_timestamp), " +
                    "consent_method = VALUES(consent_method), is_active = VALUES(is_active)";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, consent.getUserId());
            stmt.setString(2, consent.getGuildId());
            stmt.setString(3, purposesToString(consent.getPurposes()));
            stmt.setLong(4, consent.getConsentTimestamp().getEpochSecond());
            stmt.setString(5, consent.getConsentMethod());
            stmt.setBoolean(6, consent.isActive());
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Stored consent for user {} in guild {}: {} purposes", 
                       consent.getUserId(), consent.getGuildId(), consent.getPurposes().size());
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Failed to store consent for user {}", consent.getUserId(), e);
            return false;
        }
    }
    
    /**
     * Retrieves user consent from the database
     */
    public UserConsent getConsent(String userId, String guildId) {
        String sql = "SELECT purposes, consent_timestamp, consent_method, is_active " +
                    "FROM gdpr_user_consent WHERE user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Set<DataProcessingPurpose> purposes = stringToPurposes(rs.getString("purposes"));
                    Instant consentTimestamp = Instant.ofEpochSecond(rs.getLong("consent_timestamp"));
                    String consentMethod = rs.getString("consent_method");
                    boolean isActive = rs.getBoolean("is_active");
                    
                    UserConsent consent = new UserConsent(userId, guildId, purposes, consentTimestamp, consentMethod);
                    if (!isActive) {
                        consent.withdrawConsent();
                    }
                    return consent;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to retrieve consent for user {} in guild {}", userId, guildId, e);
        }
        
        return null;
    }
    
    /**
     * Updates user consent
     */
    public boolean updateConsent(UserConsent consent) {
        return storeConsent(consent);
    }
    
    /**
     * Withdraws user consent
     */
    public boolean withdrawConsent(String userId, String guildId) {
        String sql = "UPDATE gdpr_user_consent SET is_active = false, withdrawal_timestamp = ? " +
                    "WHERE user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, Instant.now().getEpochSecond());
            stmt.setString(2, userId);
            stmt.setString(3, guildId);
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Withdrew consent for user {} in guild {}", userId, guildId);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Failed to withdraw consent for user {} in guild {}", userId, guildId, e);
            return false;
        }
    }
    
    /**
     * Deletes expired consent records
     */
    public boolean deleteExpiredConsent(String userId, String guildId) {
        String sql = "DELETE FROM gdpr_user_consent WHERE user_id = ? AND guild_id = ? AND " +
                    "withdrawal_timestamp IS NOT NULL AND withdrawal_timestamp < ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Delete consent records older than 7 years (legal retention period)
            long sevenYearsAgo = Instant.now().minusSeconds(7 * 365 * 24 * 60 * 60).getEpochSecond();
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setLong(3, sevenYearsAgo);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Deleted {} expired consent records for user {} in guild {}", 
                           rowsAffected, userId, guildId);
            }
            return true;
            
        } catch (SQLException e) {
            logger.error("Failed to delete expired consent for user {} in guild {}", userId, guildId, e);
            return false;
        }
    }
    
    /**
     * Gets the total number of consent records
     */
    public int getTotalConsents() {
        String sql = "SELECT COUNT(*) FROM gdpr_user_consent";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get total consents count", e);
        }
        
        return 0;
    }
    
    /**
     * Gets the number of active consent records
     */
    public int getActiveConsents() {
        String sql = "SELECT COUNT(*) FROM gdpr_user_consent WHERE is_active = true";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get active consents count", e);
        }
        
        return 0;
    }
    
    /**
     * Gets the number of withdrawn consent records
     */
    public int getWithdrawnConsents() {
        String sql = "SELECT COUNT(*) FROM gdpr_user_consent WHERE is_active = false";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get withdrawn consents count", e);
        }
        
        return 0;
    }
    
    /**
     * Converts purposes set to string for database storage
     */
    private String purposesToString(Set<DataProcessingPurpose> purposes) {
        return purposes.stream()
                      .map(Enum::name)
                      .reduce((a, b) -> a + "," + b)
                      .orElse("");
    }
    
    /**
     * Converts string from database to purposes set
     */
    private Set<DataProcessingPurpose> stringToPurposes(String purposesStr) {
        Set<DataProcessingPurpose> purposes = new HashSet<>();
        if (purposesStr != null && !purposesStr.trim().isEmpty()) {
            Arrays.stream(purposesStr.split(","))
                  .map(String::trim)
                  .forEach(purposeName -> {
                      try {
                          purposes.add(DataProcessingPurpose.valueOf(purposeName));
                      } catch (IllegalArgumentException e) {
                          logger.warn("Unknown purpose: {}", purposeName);
                      }
                  });
        }
        return purposes;
    }
}