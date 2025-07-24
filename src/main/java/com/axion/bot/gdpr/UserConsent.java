package com.axion.bot.gdpr;

import com.axion.bot.gdpr.GDPRComplianceManager.DataProcessingPurpose;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents user consent for data processing under GDPR
 */
public class UserConsent {
    private final String userId;
    private final String guildId;
    private final Set<DataProcessingPurpose> purposes;
    private final Instant consentTimestamp;
    private final String consentMethod;
    private final String ipAddress;
    private final String userAgent;
    private boolean isActive;
    private Instant withdrawalTimestamp;
    
    public UserConsent(String userId, String guildId, Set<DataProcessingPurpose> purposes, 
                      Instant consentTimestamp, String consentMethod) {
        this.userId = userId;
        this.guildId = guildId;
        this.purposes = new HashSet<>(purposes);
        this.consentTimestamp = consentTimestamp;
        this.consentMethod = consentMethod;
        this.ipAddress = null; // Discord doesn't provide IP addresses
        this.userAgent = null; // Discord doesn't provide user agents
        this.isActive = true;
        this.withdrawalTimestamp = null;
    }
    
    public UserConsent(String userId, String guildId, Set<DataProcessingPurpose> purposes, 
                      Instant consentTimestamp, String consentMethod, String ipAddress, String userAgent) {
        this.userId = userId;
        this.guildId = guildId;
        this.purposes = new HashSet<>(purposes);
        this.consentTimestamp = consentTimestamp;
        this.consentMethod = consentMethod;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isActive = true;
        this.withdrawalTimestamp = null;
    }
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public Set<DataProcessingPurpose> getPurposes() {
        return new HashSet<>(purposes);
    }
    
    public Instant getConsentTimestamp() {
        return consentTimestamp;
    }
    
    public String getConsentMethod() {
        return consentMethod;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public Instant getWithdrawalTimestamp() {
        return withdrawalTimestamp;
    }
    
    // Methods for consent management
    public boolean hasConsentFor(DataProcessingPurpose purpose) {
        return isActive && purposes.contains(purpose);
    }
    
    public void withdrawConsent() {
        this.isActive = false;
        this.withdrawalTimestamp = Instant.now();
    }
    
    public void withdrawConsentFor(Set<DataProcessingPurpose> purposesToWithdraw) {
        this.purposes.removeAll(purposesToWithdraw);
        if (this.purposes.isEmpty()) {
            withdrawConsent();
        }
    }
    
    public void addConsent(Set<DataProcessingPurpose> newPurposes) {
        this.purposes.addAll(newPurposes);
        this.isActive = true;
        this.withdrawalTimestamp = null;
    }
    
    /**
     * Checks if consent is still valid (not expired)
     * GDPR doesn't specify consent expiration, but best practice is to refresh periodically
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }
        
        // Consider consent valid for 2 years (best practice)
        Instant expirationTime = consentTimestamp.plusSeconds(2 * 365 * 24 * 60 * 60); // 2 years
        return Instant.now().isBefore(expirationTime);
    }
    
    /**
     * Gets the age of the consent in days
     */
    public long getConsentAgeInDays() {
        return java.time.Duration.between(consentTimestamp, Instant.now()).toDays();
    }
    
    /**
     * Creates a copy of this consent with updated purposes
     */
    public UserConsent withUpdatedPurposes(Set<DataProcessingPurpose> newPurposes) {
        UserConsent updated = new UserConsent(userId, guildId, newPurposes, 
                                            Instant.now(), "update", ipAddress, userAgent);
        return updated;
    }
    
    /**
     * Converts consent to a human-readable format for data export
     */
    public String toReadableFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("User Consent Record\n");
        sb.append("User ID: ").append(userId).append("\n");
        sb.append("Guild ID: ").append(guildId).append("\n");
        sb.append("Consent Given: ").append(consentTimestamp).append("\n");
        sb.append("Consent Method: ").append(consentMethod).append("\n");
        sb.append("Active: ").append(isActive).append("\n");
        
        if (withdrawalTimestamp != null) {
            sb.append("Withdrawn: ").append(withdrawalTimestamp).append("\n");
        }
        
        sb.append("Purposes:\n");
        for (DataProcessingPurpose purpose : purposes) {
            sb.append("  - ").append(purpose.name()).append(": ").append(purpose.getDescription()).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConsent that = (UserConsent) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(guildId, that.guildId) &&
               Objects.equals(consentTimestamp, that.consentTimestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, guildId, consentTimestamp);
    }
    
    @Override
    public String toString() {
        return "UserConsent{" +
                "userId='" + userId + '\'' +
                ", guildId='" + guildId + '\'' +
                ", purposes=" + purposes +
                ", consentTimestamp=" + consentTimestamp +
                ", consentMethod='" + consentMethod + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}