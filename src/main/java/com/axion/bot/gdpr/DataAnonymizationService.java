package com.axion.bot.gdpr;

import com.axion.bot.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for anonymizing user data in compliance with GDPR Article 17 (Right to erasure)
 */
public class DataAnonymizationService {
    private static final Logger logger = LoggerFactory.getLogger(DataAnonymizationService.class);
    
    private final DatabaseService databaseService;
    private final SecureRandom secureRandom;
    
    public DataAnonymizationService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Anonymizes all user data while preserving statistical value
     */
    public boolean anonymizeUserData(String userId, String guildId) {
        logger.info("Starting data anonymization for user {} in guild {}", userId, guildId);
        
        try (Connection conn = databaseService.getConnection()) {
            conn.setAutoCommit(false);
            
            // Generate anonymous identifier
            String anonymousId = generateAnonymousId(userId);
            
            // Anonymize consent records
            anonymizeConsentData(conn, userId, guildId, anonymousId);
            
            // Anonymize moderation records
            anonymizeModerationData(conn, userId, guildId, anonymousId);
            
            // Anonymize activity records
            anonymizeActivityData(conn, userId, guildId, anonymousId);
            
            // Anonymize preference records
            anonymizePreferenceData(conn, userId, guildId, anonymousId);
            
            // Record anonymization event
            recordAnonymizationEvent(conn, userId, guildId, anonymousId);
            
            conn.commit();
            logger.info("Successfully anonymized data for user {} in guild {}", userId, guildId);
            return true;
            
        } catch (SQLException e) {
            logger.error("Failed to anonymize data for user {} in guild {}", userId, guildId, e);
            return false;
        }
    }
    
    /**
     * Anonymizes consent data
     */
    private void anonymizeConsentData(Connection conn, String userId, String guildId, String anonymousId) 
            throws SQLException {
        String sql = "UPDATE gdpr_user_consent SET user_id = ?, anonymized = true, anonymization_timestamp = ? " +
                    "WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, anonymousId);
            stmt.setLong(2, Instant.now().getEpochSecond());
            stmt.setString(3, userId);
            stmt.setString(4, guildId);
            
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Anonymized {} consent records for user {}", rowsAffected, userId);
        }
    }
    
    /**
     * Anonymizes moderation data
     */
    private void anonymizeModerationData(Connection conn, String userId, String guildId, String anonymousId) 
            throws SQLException {
        String sql = "UPDATE moderation_logs SET user_id = ?, reason = ?, anonymized = true, anonymization_timestamp = ? " +
                    "WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, anonymousId);
            stmt.setString(2, "[ANONYMIZED]"); // Replace reason with generic text
            stmt.setLong(3, Instant.now().getEpochSecond());
            stmt.setString(4, userId);
            stmt.setString(5, guildId);
            
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Anonymized {} moderation records for user {}", rowsAffected, userId);
        }
    }
    
    /**
     * Anonymizes activity data
     */
    private void anonymizeActivityData(Connection conn, String userId, String guildId, String anonymousId) 
            throws SQLException {
        String sql = "UPDATE user_activity SET user_id = ?, content = ?, anonymized = true, anonymization_timestamp = ? " +
                    "WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, anonymousId);
            stmt.setString(2, anonymizeContent("[ANONYMIZED_CONTENT]"));
            stmt.setLong(3, Instant.now().getEpochSecond());
            stmt.setString(4, userId);
            stmt.setString(5, guildId);
            
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Anonymized {} activity records for user {}", rowsAffected, userId);
        }
    }
    
    /**
     * Anonymizes preference data
     */
    private void anonymizePreferenceData(Connection conn, String userId, String guildId, String anonymousId) 
            throws SQLException {
        String sql = "UPDATE user_preferences SET user_id = ?, preference_value = ?, anonymized = true, anonymization_timestamp = ? " +
                    "WHERE user_id = ? AND guild_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, anonymousId);
            stmt.setString(2, "[ANONYMIZED]");
            stmt.setLong(3, Instant.now().getEpochSecond());
            stmt.setString(4, userId);
            stmt.setString(5, guildId);
            
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Anonymized {} preference records for user {}", rowsAffected, userId);
        }
    }
    
    /**
     * Records the anonymization event for audit purposes
     */
    private void recordAnonymizationEvent(Connection conn, String userId, String guildId, String anonymousId) 
            throws SQLException {
        String sql = "INSERT INTO gdpr_anonymization_log (original_user_id, anonymous_id, guild_id, anonymization_timestamp, method) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashUserId(userId)); // Store hashed version for audit
            stmt.setString(2, anonymousId);
            stmt.setString(3, guildId);
            stmt.setLong(4, Instant.now().getEpochSecond());
            stmt.setString(5, "automated_anonymization");
            
            stmt.executeUpdate();
            logger.debug("Recorded anonymization event for user {}", userId);
        }
    }
    
    /**
     * Generates a cryptographically secure anonymous identifier
     */
    private String generateAnonymousId(String originalUserId) {
        try {
            // Create a deterministic but irreversible anonymous ID
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Add salt to prevent rainbow table attacks
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            
            digest.update(salt);
            digest.update(originalUserId.getBytes());
            digest.update(UUID.randomUUID().toString().getBytes());
            
            byte[] hash = digest.digest();
            return "anon_" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to generate anonymous ID", e);
            return "anon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
    
    /**
     * Hashes user ID for audit logging
     */
    private String hashUserId(String userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(userId.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to hash user ID", e);
            return "hash_error";
        }
    }
    
    /**
     * Anonymizes content while preserving some statistical properties
     */
    private String anonymizeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "[EMPTY]";
        }
        
        // Preserve length and basic structure for analytics
        int length = content.length();
        if (length <= 10) {
            return "[SHORT_" + length + "]"; 
        } else if (length <= 50) {
            return "[MEDIUM_" + length + "]";
        } else {
            return "[LONG_" + length + "]";
        }
    }
    
    /**
     * Checks if user data has been anonymized
     */
    public boolean isUserDataAnonymized(String userId, String guildId) {
        String sql = "SELECT COUNT(*) FROM gdpr_anonymization_log WHERE original_user_id = ? AND guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hashUserId(userId));
            stmt.setString(2, guildId);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to check anonymization status for user {}", userId, e);
        }
        
        return false;
    }
    
    /**
     * Gets anonymization statistics
     */
    public AnonymizationStats getAnonymizationStats(String guildId) {
        String sql = "SELECT COUNT(*) as total_anonymized, " +
                    "COUNT(CASE WHEN anonymization_timestamp > ? THEN 1 END) as recent_anonymized " +
                    "FROM gdpr_anonymization_log WHERE guild_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Recent = last 30 days
            long thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60).getEpochSecond();
            stmt.setLong(1, thirtyDaysAgo);
            stmt.setString(2, guildId);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AnonymizationStats(
                        rs.getInt("total_anonymized"),
                        rs.getInt("recent_anonymized")
                    );
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get anonymization stats for guild {}", guildId, e);
        }
        
        return new AnonymizationStats(0, 0);
    }
    
    /**
     * Statistics for anonymization operations
     */
    public static class AnonymizationStats {
        private final int totalAnonymized;
        private final int recentAnonymized;
        
        public AnonymizationStats(int totalAnonymized, int recentAnonymized) {
            this.totalAnonymized = totalAnonymized;
            this.recentAnonymized = recentAnonymized;
        }
        
        public int getTotalAnonymized() {
            return totalAnonymized;
        }
        
        public int getRecentAnonymized() {
            return recentAnonymized;
        }
    }
}