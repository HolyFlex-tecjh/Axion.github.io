package com.axion.bot.gdpr;

import com.axion.bot.gdpr.GDPRComplianceManager.DataProcessingPurpose;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a data processing activity as required by GDPR Article 30
 * (Records of processing activities)
 */
public class DataProcessingActivity {
    private final String activityId;
    private final String name;
    private final String description;
    private final DataProcessingPurpose purpose;
    private final Set<String> dataCategories;
    private final Set<String> dataSubjects;
    private final String legalBasis;
    private final Set<String> recipients;
    private final String retentionPeriod;
    private final String securityMeasures;
    private final Instant createdAt;
    private final Instant lastUpdated;
    private final String controller;
    private final String processor;
    private final boolean isActive;
    private final Map<String, String> additionalInfo;
    private final List<String> dataTransfers;
    
    public DataProcessingActivity(String activityId, String name, String description,
                                 DataProcessingPurpose purpose, Set<String> dataCategories,
                                 Set<String> dataSubjects, String legalBasis,
                                 Set<String> recipients, String retentionPeriod,
                                 String securityMeasures, String controller, String processor) {
        this.activityId = activityId;
        this.name = name;
        this.description = description;
        this.purpose = purpose;
        this.dataCategories = new HashSet<>(dataCategories);
        this.dataSubjects = new HashSet<>(dataSubjects);
        this.legalBasis = legalBasis;
        this.recipients = new HashSet<>(recipients);
        this.retentionPeriod = retentionPeriod;
        this.securityMeasures = securityMeasures;
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
        this.controller = controller;
        this.processor = processor;
        this.isActive = true;
        this.additionalInfo = new HashMap<>();
        this.dataTransfers = new ArrayList<>();
    }
    
    // Simplified constructor for basic data processing activities
    public DataProcessingActivity(String userId, String guildId, DataProcessingPurpose purpose, 
                                 String activity, String legalBasis, Instant timestamp) {
        this.activityId = userId + ":" + guildId + ":" + System.currentTimeMillis();
        this.name = activity;
        this.description = "Data processing activity: " + activity;
        this.purpose = purpose;
        this.dataCategories = new HashSet<>();
        this.dataSubjects = new HashSet<>();
        this.legalBasis = legalBasis;
        this.recipients = new HashSet<>();
        this.retentionPeriod = "As per retention policy";
        this.securityMeasures = "Standard security measures";
        this.createdAt = timestamp;
        this.lastUpdated = timestamp;
        this.controller = "Discord Bot";
        this.processor = "Discord Bot";
        this.isActive = true;
        this.additionalInfo = new HashMap<>();
        this.dataTransfers = new ArrayList<>();
    }
    
    // Constructor for test purposes
    public DataProcessingActivity(String activityId, String name, String description, DataProcessingPurpose purpose) {
        this.activityId = activityId;
        this.name = name;
        this.description = description;
        this.purpose = purpose;
        this.dataCategories = new HashSet<>();
        this.dataSubjects = new HashSet<>();
        this.legalBasis = "Test legal basis";
        this.recipients = new HashSet<>();
        this.retentionPeriod = "Test retention period";
        this.securityMeasures = "Test security measures";
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
        this.controller = "Test Controller";
        this.processor = "Test Processor";
        this.isActive = true;
        this.additionalInfo = new HashMap<>();
        this.dataTransfers = new ArrayList<>();
    }
    
    // Getters
    public String getActivityId() {
        return activityId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public DataProcessingPurpose getPurpose() {
        return purpose;
    }
    
    public Set<String> getDataCategories() {
        return new HashSet<>(dataCategories);
    }
    
    public Set<String> getDataSubjects() {
        return new HashSet<>(dataSubjects);
    }
    
    public String getLegalBasis() {
        return legalBasis;
    }
    
    public Set<String> getRecipients() {
        return new HashSet<>(recipients);
    }
    
    public String getRetentionPeriod() {
        return retentionPeriod;
    }
    
    public String getSecurityMeasures() {
        return securityMeasures;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public String getController() {
        return controller;
    }
    
    public String getProcessor() {
        return processor;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public Map<String, String> getAdditionalInfo() {
        return new HashMap<>(additionalInfo);
    }
    
    public List<String> getDataTransfers() {
        return new ArrayList<>(dataTransfers);
    }
    
    // Activity management methods
    public void addDataCategory(String category) {
        this.dataCategories.add(category);
    }
    
    public void removeDataCategory(String category) {
        this.dataCategories.remove(category);
    }
    
    public void addDataSubject(String subject) {
        this.dataSubjects.add(subject);
    }
    
    public void removeDataSubject(String subject) {
        this.dataSubjects.remove(subject);
    }
    
    public void addRecipient(String recipient) {
        this.recipients.add(recipient);
    }
    
    public void removeRecipient(String recipient) {
        this.recipients.remove(recipient);
    }
    
    public void addDataTransfer(String transferDetails) {
        this.dataTransfers.add(transferDetails);
    }
    
    public void addAdditionalInfo(String key, String value) {
        this.additionalInfo.put(key, value);
    }
    
    public void removeAdditionalInfo(String key) {
        this.additionalInfo.remove(key);
    }
    
    /**
     * Checks if this activity processes a specific type of data
     */
    public boolean processesDataCategory(String category) {
        return dataCategories.contains(category);
    }
    
    /**
     * Checks if this activity affects a specific type of data subject
     */
    public boolean affectsDataSubject(String subject) {
        return dataSubjects.contains(subject);
    }
    
    /**
     * Checks if this activity shares data with a specific recipient
     */
    public boolean sharesDataWith(String recipient) {
        return recipients.contains(recipient);
    }
    
    /**
     * Validates the activity for GDPR compliance
     */
    public boolean isGDPRCompliant() {
        // Check required fields
        if (name == null || name.trim().isEmpty()) return false;
        if (purpose == null) return false;
        if (legalBasis == null || legalBasis.trim().isEmpty()) return false;
        if (dataCategories.isEmpty()) return false;
        if (dataSubjects.isEmpty()) return false;
        if (retentionPeriod == null || retentionPeriod.trim().isEmpty()) return false;
        if (securityMeasures == null || securityMeasures.trim().isEmpty()) return false;
        if (controller == null || controller.trim().isEmpty()) return false;
        
        return true;
    }
    
    /**
     * Creates default processing activities for a Discord bot
     */
    public static List<DataProcessingActivity> createDefaultActivities() {
        List<DataProcessingActivity> activities = new ArrayList<>();
        
        // Moderation Activity
        Set<String> moderationData = new HashSet<>();
        moderationData.add("User messages");
        moderationData.add("Moderation actions");
        moderationData.add("Warning records");
        moderationData.add("Ban/kick records");
        
        Set<String> discordUsers = new HashSet<>();
        discordUsers.add("Discord server members");
        discordUsers.add("Discord server moderators");
        
        Set<String> moderationRecipients = new HashSet<>();
        moderationRecipients.add("Server administrators");
        moderationRecipients.add("Moderation team");
        
        activities.add(new DataProcessingActivity(
            "moderation-001",
            "Server Moderation",
            "Processing user data for server moderation purposes including warnings, bans, and content filtering",
            DataProcessingPurpose.MODERATION,
            moderationData,
            discordUsers,
            "Legitimate interest (maintaining server safety and order)",
            moderationRecipients,
            "2 years from last moderation action",
            "Encrypted storage, access controls, audit logging",
            "Server Owner",
            "Axion Bot"
        ));
        
        // Analytics Activity
        Set<String> analyticsData = new HashSet<>();
        analyticsData.add("Message statistics");
        analyticsData.add("User activity patterns");
        analyticsData.add("Command usage statistics");
        
        Set<String> analyticsRecipients = new HashSet<>();
        analyticsRecipients.add("Server administrators");
        
        activities.add(new DataProcessingActivity(
            "analytics-001",
            "Server Analytics",
            "Processing aggregated user data for server analytics and insights",
            DataProcessingPurpose.ANALYTICS,
            analyticsData,
            discordUsers,
            "Legitimate interest (server optimization and management)",
            analyticsRecipients,
            "1 year from collection",
            "Data anonymization, encrypted storage, access controls",
            "Server Owner",
            "Axion Bot"
        ));
        
        // Security Activity
        Set<String> securityData = new HashSet<>();
        securityData.add("Security logs");
        securityData.add("Suspicious activity records");
        securityData.add("Anti-spam data");
        
        Set<String> securityRecipients = new HashSet<>();
        securityRecipients.add("Security team");
        securityRecipients.add("Server administrators");
        
        activities.add(new DataProcessingActivity(
            "security-001",
            "Security Monitoring",
            "Processing user data for security monitoring and threat detection",
            DataProcessingPurpose.SECURITY,
            securityData,
            discordUsers,
            "Legitimate interest (protecting server and users from security threats)",
            securityRecipients,
            "3 years from incident",
            "Encrypted storage, access controls, audit logging, data minimization",
            "Server Owner",
            "Axion Bot"
        ));
        
        return activities;
    }
    
    /**
     * Converts activity to GDPR Article 30 compliant format
     */
    public String toGDPRFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GDPR Article 30 - Record of Processing Activity ===\n\n");
        
        sb.append("1. Name and contact details of the controller:\n");
        sb.append("   ").append(controller).append("\n\n");
        
        sb.append("2. Name and contact details of the processor:\n");
        sb.append("   ").append(processor).append("\n\n");
        
        sb.append("3. Purposes of the processing:\n");
        sb.append("   ").append(purpose.name()).append(" - ").append(purpose.getDescription()).append("\n\n");
        
        sb.append("4. Description of the categories of data subjects:\n");
        for (String subject : dataSubjects) {
            sb.append("   - ").append(subject).append("\n");
        }
        sb.append("\n");
        
        sb.append("5. Categories of personal data:\n");
        for (String category : dataCategories) {
            sb.append("   - ").append(category).append("\n");
        }
        sb.append("\n");
        
        sb.append("6. Categories of recipients:\n");
        for (String recipient : recipients) {
            sb.append("   - ").append(recipient).append("\n");
        }
        sb.append("\n");
        
        sb.append("7. Transfers to third countries:\n");
        if (dataTransfers.isEmpty()) {
            sb.append("   No transfers to third countries\n");
        } else {
            for (String transfer : dataTransfers) {
                sb.append("   - ").append(transfer).append("\n");
            }
        }
        sb.append("\n");
        
        sb.append("8. Time limits for erasure:\n");
        sb.append("   ").append(retentionPeriod).append("\n\n");
        
        sb.append("9. Technical and organizational security measures:\n");
        sb.append("   ").append(securityMeasures).append("\n\n");
        
        sb.append("10. Legal basis for processing:\n");
        sb.append("    ").append(legalBasis).append("\n\n");
        
        sb.append("Activity Details:\n");
        sb.append("- Activity ID: ").append(activityId).append("\n");
        sb.append("- Name: ").append(name).append("\n");
        sb.append("- Description: ").append(description).append("\n");
        sb.append("- Created: ").append(createdAt).append("\n");
        sb.append("- Last Updated: ").append(lastUpdated).append("\n");
        sb.append("- Status: ").append(isActive ? "Active" : "Inactive").append("\n");
        
        if (!additionalInfo.isEmpty()) {
            sb.append("\nAdditional Information:\n");
            for (Map.Entry<String, String> entry : additionalInfo.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataProcessingActivity that = (DataProcessingActivity) o;
        return Objects.equals(activityId, that.activityId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(activityId);
    }
    
    @Override
    public String toString() {
        return "DataProcessingActivity{" +
                "activityId='" + activityId + '\'' +
                ", name='" + name + '\'' +
                ", purpose=" + purpose +
                ", dataCategories=" + dataCategories +
                ", legalBasis='" + legalBasis + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}