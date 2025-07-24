package com.axion.bot.gdpr;

import com.axion.bot.gdpr.GDPRComplianceManager.DataProcessingPurpose;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents data retention policies for different types of data under GDPR
 */
public class DataRetentionPolicy {
    private final String policyId;
    private final String name;
    private final String description;
    private final Map<DataProcessingPurpose, Duration> retentionPeriods;
    private final Set<String> dataTypes;
    private final Instant createdAt;
    private final Instant lastUpdated;
    private final boolean isActive;
    private final String legalBasis;
    private final String createdBy;
    
    public DataRetentionPolicy(String policyId, String name, String description, 
                              Map<DataProcessingPurpose, Duration> retentionPeriods,
                              Set<String> dataTypes, String legalBasis, String createdBy) {
        this.policyId = policyId;
        this.name = name;
        this.description = description;
        this.retentionPeriods = new HashMap<>(retentionPeriods);
        this.dataTypes = new HashSet<>(dataTypes);
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
        this.isActive = true;
        this.legalBasis = legalBasis;
        this.createdBy = createdBy;
    }
    
    // Getters
    public String getPolicyId() {
        return policyId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Map<DataProcessingPurpose, Duration> getRetentionPeriods() {
        return new HashMap<>(retentionPeriods);
    }
    
    public Set<String> getDataTypes() {
        return new HashSet<>(dataTypes);
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public String getLegalBasis() {
        return legalBasis;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    // Policy management methods
    public Duration getRetentionPeriod(DataProcessingPurpose purpose) {
        return retentionPeriods.get(purpose);
    }
    
    /**
     * Sets retention period for a specific data type (in days)
     */
    public void setRetentionPeriod(String dataType, int days) {
        // This is a simplified implementation - in a real system you'd want
        // to map data types to purposes more sophisticatedly
        Duration duration = Duration.ofDays(days);
        
        // Map common data types to purposes
        switch (dataType.toLowerCase()) {
            case "moderation_logs":
                retentionPeriods.put(DataProcessingPurpose.MODERATION, duration);
                break;
            case "user_activities":
            case "behavior_profiles":
                retentionPeriods.put(DataProcessingPurpose.ANALYTICS, duration);
                break;
            case "consent_records":
                retentionPeriods.put(DataProcessingPurpose.LOGGING, duration);
                break;
            default:
                // For unknown types, use LOGGING as default
                retentionPeriods.put(DataProcessingPurpose.LOGGING, duration);
                break;
        }
        
        // Add to data types set
        dataTypes.add(dataType);
    }
    
    /**
     * Performs cleanup of expired data (placeholder implementation)
     * In a real system, this would interact with the database
     */
    public int cleanupExpiredData() {
        // This is a placeholder implementation
        // In a real system, this would:
        // 1. Query the database for expired records
        // 2. Delete or anonymize the expired data
        // 3. Return the count of cleaned records
        
        int cleanedCount = 0;
        
        // Simulate cleanup logic
        for (Map.Entry<DataProcessingPurpose, Duration> entry : retentionPeriods.entrySet()) {
            DataProcessingPurpose purpose = entry.getKey();
            Duration retention = entry.getValue();
            
            // Calculate cutoff time
            Instant cutoffTime = Instant.now().minus(retention);
            
            // In a real implementation, you would:
            // - Query database for records older than cutoffTime for this purpose
            // - Delete or anonymize those records
            // - Add to cleanedCount
            
            // For now, simulate some cleanup
            cleanedCount += 2; // Placeholder
        }
        
        return cleanedCount;
    }
    
    public boolean appliesToDataType(String dataType) {
        return dataTypes.contains(dataType);
    }
    
    public boolean appliesToPurpose(DataProcessingPurpose purpose) {
        return retentionPeriods.containsKey(purpose);
    }
    
    /**
     * Checks if data should be deleted based on creation time and retention policy
     */
    public boolean shouldDeleteData(Instant dataCreationTime, DataProcessingPurpose purpose) {
        Duration retentionPeriod = getRetentionPeriod(purpose);
        if (retentionPeriod == null) {
            return false; // No policy defined, don't delete
        }
        
        Instant expirationTime = dataCreationTime.plus(retentionPeriod);
        return Instant.now().isAfter(expirationTime);
    }
    
    /**
     * Gets the expiration time for data created at a specific time
     */
    public Instant getDataExpirationTime(Instant dataCreationTime, DataProcessingPurpose purpose) {
        Duration retentionPeriod = getRetentionPeriod(purpose);
        if (retentionPeriod == null) {
            return null; // No expiration
        }
        return dataCreationTime.plus(retentionPeriod);
    }
    
    /**
     * Gets the remaining time before data expires
     */
    public Duration getTimeUntilExpiration(Instant dataCreationTime, DataProcessingPurpose purpose) {
        Instant expirationTime = getDataExpirationTime(dataCreationTime, purpose);
        if (expirationTime == null) {
            return null; // No expiration
        }
        
        Duration remaining = Duration.between(Instant.now(), expirationTime);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
    
    /**
     * Creates a default retention policy for Discord bot data
     */
    public static DataRetentionPolicy createDefaultPolicy() {
        Map<DataProcessingPurpose, Duration> defaultPeriods = new HashMap<>();
        
        // Default retention periods based on GDPR best practices
        defaultPeriods.put(DataProcessingPurpose.MODERATION, Duration.ofDays(365 * 2)); // 2 years
        defaultPeriods.put(DataProcessingPurpose.ANALYTICS, Duration.ofDays(365)); // 1 year
        defaultPeriods.put(DataProcessingPurpose.SECURITY, Duration.ofDays(365 * 3)); // 3 years
        defaultPeriods.put(DataProcessingPurpose.LOGGING, Duration.ofDays(365)); // 1 year
        defaultPeriods.put(DataProcessingPurpose.PERSONALIZATION, Duration.ofDays(180)); // 6 months
        
        Set<String> dataTypes = new HashSet<>();
        dataTypes.add("user_messages");
        dataTypes.add("moderation_logs");
        dataTypes.add("user_behavior_data");
        dataTypes.add("analytics_data");
        dataTypes.add("security_logs");
        dataTypes.add("user_preferences");
        
        return new DataRetentionPolicy(
            "default-policy",
            "Default GDPR Retention Policy",
            "Default data retention policy compliant with GDPR requirements",
            defaultPeriods,
            dataTypes,
            "Legitimate interest and legal compliance",
            "system"
        );
    }
    
    /**
     * Creates a minimal retention policy for essential data only
     */
    public static DataRetentionPolicy createMinimalPolicy() {
        Map<DataProcessingPurpose, Duration> minimalPeriods = new HashMap<>();
        
        // Minimal retention periods
        minimalPeriods.put(DataProcessingPurpose.MODERATION, Duration.ofDays(30)); // 30 days
        minimalPeriods.put(DataProcessingPurpose.SECURITY, Duration.ofDays(90)); // 90 days
        minimalPeriods.put(DataProcessingPurpose.LOGGING, Duration.ofDays(365)); // 1 year
        
        Set<String> dataTypes = new HashSet<>();
        dataTypes.add("moderation_logs");
        dataTypes.add("security_logs");
        
        return new DataRetentionPolicy(
            "minimal-policy",
            "Minimal GDPR Retention Policy",
            "Minimal data retention policy for essential data only",
            minimalPeriods,
            dataTypes,
            "Legal compliance and legitimate interest",
            "system"
        );
    }
    
    /**
     * Validates the retention policy for GDPR compliance
     */
    public boolean isGDPRCompliant() {
        // Check if all retention periods are reasonable (not excessive)
        for (Duration period : retentionPeriods.values()) {
            if (period.toDays() > 365 * 10) { // More than 10 years is likely excessive
                return false;
            }
        }
        
        // Check if legal basis is provided
        if (legalBasis == null || legalBasis.trim().isEmpty()) {
            return false;
        }
        
        // Check if policy has a clear purpose
        if (retentionPeriods.isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the compliance rate as a percentage (0.0 to 1.0)
     * This calculates how well the current policy adheres to GDPR best practices
     */
    public double getComplianceRate() {
        int totalChecks = 0;
        int passedChecks = 0;
        
        // Check 1: Reasonable retention periods (not excessive)
        totalChecks++;
        boolean reasonablePeriods = true;
        for (Duration period : retentionPeriods.values()) {
            if (period.toDays() > 365 * 10) { // More than 10 years is excessive
                reasonablePeriods = false;
                break;
            }
        }
        if (reasonablePeriods) passedChecks++;
        
        // Check 2: Legal basis provided
        totalChecks++;
        if (legalBasis != null && !legalBasis.trim().isEmpty()) {
            passedChecks++;
        }
        
        // Check 3: Has defined purposes
        totalChecks++;
        if (!retentionPeriods.isEmpty()) {
            passedChecks++;
        }
        
        // Check 4: Policy is active
        totalChecks++;
        if (isActive) {
            passedChecks++;
        }
        
        // Check 5: Has data types defined
        totalChecks++;
        if (!dataTypes.isEmpty()) {
            passedChecks++;
        }
        
        // Check 6: Retention periods are not too short (minimum 30 days for most purposes)
        totalChecks++;
        boolean adequatePeriods = true;
        for (Map.Entry<DataProcessingPurpose, Duration> entry : retentionPeriods.entrySet()) {
            DataProcessingPurpose purpose = entry.getKey();
            Duration period = entry.getValue();
            
            // Essential purposes should have longer retention
            if (purpose.isEssential() && period.toDays() < 30) {
                adequatePeriods = false;
                break;
            }
            // Non-essential purposes can be shorter but not zero
            if (!purpose.isEssential() && period.toDays() < 1) {
                adequatePeriods = false;
                break;
            }
        }
        if (adequatePeriods) passedChecks++;
        
        return totalChecks > 0 ? (double) passedChecks / totalChecks : 0.0;
    }
    
    /**
     * Converts policy to a human-readable format for documentation
     */
    public String toReadableFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Retention Policy\n");
        sb.append("Policy ID: ").append(policyId).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Legal Basis: ").append(legalBasis).append("\n");
        sb.append("Created: ").append(createdAt).append("\n");
        sb.append("Created By: ").append(createdBy).append("\n");
        sb.append("Active: ").append(isActive).append("\n\n");
        
        sb.append("Retention Periods:\n");
        for (Map.Entry<DataProcessingPurpose, Duration> entry : retentionPeriods.entrySet()) {
            sb.append("  - ").append(entry.getKey().name())
              .append(": ").append(entry.getValue().toDays()).append(" days\n");
        }
        
        sb.append("\nApplicable Data Types:\n");
        for (String dataType : dataTypes) {
            sb.append("  - ").append(dataType).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRetentionPolicy that = (DataRetentionPolicy) o;
        return Objects.equals(policyId, that.policyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(policyId);
    }
    
    /**
     * Gets the retention status for a specific user
     */
    public DataRetentionStatus getRetentionStatus(String userId, String guildId) {
        Map<String, Object> retentionInfo = new HashMap<>();
        
        // Add retention information for each purpose
        for (DataProcessingPurpose purpose : DataProcessingPurpose.values()) {
            Duration retention = getRetentionPeriod(purpose);
            if (retention != null) {
                retentionInfo.put(purpose.name().toLowerCase() + "_retention_days", retention.toDays());
                retentionInfo.put(purpose.name().toLowerCase() + "_essential", purpose.isEssential());
            }
        }
        
        // Add policy metadata
        retentionInfo.put("policy_name", name);
        retentionInfo.put("legal_basis", legalBasis);
        retentionInfo.put("is_active", isActive);
        retentionInfo.put("compliance_rate", getComplianceRate());
        
        return new DataRetentionStatus(userId, guildId, retentionInfo);
    }
    
    @Override
    public String toString() {
        return "DataRetentionPolicy{" +
                "policyId='" + policyId + '\'' +
                ", name='" + name + '\'' +
                ", retentionPeriods=" + retentionPeriods +
                ", dataTypes=" + dataTypes +
                ", isActive=" + isActive +
                '}';
    }
}