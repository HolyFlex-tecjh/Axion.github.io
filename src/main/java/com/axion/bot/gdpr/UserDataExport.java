package com.axion.bot.gdpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a complete export of user data for GDPR compliance
 */
public class UserDataExport {
    private static final Logger logger = LoggerFactory.getLogger(UserDataExport.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    private final String userId;
    private final String guildId;
    private final Instant exportTimestamp;
    private final List<Map<String, Object>> consentRecords;
    private final List<Map<String, Object>> moderationRecords;
    private final List<Map<String, Object>> activityRecords;
    private final List<Map<String, Object>> preferenceRecords;
    
    public UserDataExport(String userId, String guildId) {
        this.userId = userId;
        this.guildId = guildId;
        this.exportTimestamp = Instant.now();
        this.consentRecords = new ArrayList<>();
        this.moderationRecords = new ArrayList<>();
        this.activityRecords = new ArrayList<>();
        this.preferenceRecords = new ArrayList<>();
    }
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public Instant getExportTimestamp() {
        return exportTimestamp;
    }
    
    public List<Map<String, Object>> getConsentRecords() {
        return new ArrayList<>(consentRecords);
    }
    
    public List<Map<String, Object>> getModerationRecords() {
        return new ArrayList<>(moderationRecords);
    }
    
    public List<Map<String, Object>> getActivityRecords() {
        return new ArrayList<>(activityRecords);
    }
    
    public List<Map<String, Object>> getPreferenceRecords() {
        return new ArrayList<>(preferenceRecords);
    }
    
    // Add methods
    public void addConsentRecord(Map<String, Object> record) {
        consentRecords.add(new HashMap<>(record));
    }
    
    public void addModerationRecord(Map<String, Object> record) {
        moderationRecords.add(new HashMap<>(record));
    }
    
    public void addActivityRecord(Map<String, Object> record) {
        activityRecords.add(new HashMap<>(record));
    }
    
    public void addPreferenceRecord(Map<String, Object> record) {
        preferenceRecords.add(new HashMap<>(record));
    }
    
    /**
     * Exports data as JSON string
     */
    public String toJson() {
        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("user_id", userId);
            exportData.put("guild_id", guildId);
            exportData.put("export_timestamp", exportTimestamp.toString());
            exportData.put("consent_records", consentRecords);
            exportData.put("moderation_records", moderationRecords);
            exportData.put("activity_records", activityRecords);
            exportData.put("preference_records", preferenceRecords);
            
            return objectMapper.writeValueAsString(exportData);
        } catch (Exception e) {
            logger.error("Failed to convert export to JSON", e);
            return "Error generating JSON export";
        }
    }
    
    /**
     * Exports data in human-readable format
     */
    public String toReadableFormat() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== GDPR Data Export ===").append("\n");
        sb.append("User ID: ").append(userId).append("\n");
        sb.append("Guild ID: ").append(guildId).append("\n");
        sb.append("Export Date: ").append(exportTimestamp.toString()).append("\n\n");
        
        // Consent Records
        sb.append("--- Consent Records ---\n");
        if (consentRecords.isEmpty()) {
            sb.append("No consent records found.\n");
        } else {
            for (Map<String, Object> record : consentRecords) {
                sb.append("Purposes: ").append(record.get("purposes")).append("\n");
                sb.append("Consent Date: ").append(record.get("consent_timestamp")).append("\n");
                sb.append("Method: ").append(record.get("consent_method")).append("\n");
                sb.append("Active: ").append(record.get("is_active")).append("\n");
                if (record.containsKey("withdrawal_timestamp")) {
                    sb.append("Withdrawn: ").append(record.get("withdrawal_timestamp")).append("\n");
                }
                sb.append("\n");
            }
        }
        
        // Moderation Records
        sb.append("--- Moderation Records ---\n");
        if (moderationRecords.isEmpty()) {
            sb.append("No moderation records found.\n");
        } else {
            for (Map<String, Object> record : moderationRecords) {
                sb.append("Action: ").append(record.get("action_type")).append("\n");
                sb.append("Reason: ").append(record.get("reason")).append("\n");
                sb.append("Moderator: ").append(record.get("moderator_id")).append("\n");
                sb.append("Date: ").append(record.get("timestamp")).append("\n");
                sb.append("Duration: ").append(record.get("duration")).append("\n\n");
            }
        }
        
        // Activity Records
        sb.append("--- Activity Records ---\n");
        if (activityRecords.isEmpty()) {
            sb.append("No activity records found.\n");
        } else {
            for (Map<String, Object> record : activityRecords) {
                sb.append("Type: ").append(record.get("activity_type")).append("\n");
                sb.append("Content: ").append(record.get("content")).append("\n");
                sb.append("Date: ").append(record.get("timestamp")).append("\n");
                sb.append("Channel: ").append(record.get("channel_id")).append("\n\n");
            }
        }
        
        // Preference Records
        sb.append("--- Preference Records ---\n");
        if (preferenceRecords.isEmpty()) {
            sb.append("No preference records found.\n");
        } else {
            for (Map<String, Object> record : preferenceRecords) {
                sb.append("Key: ").append(record.get("preference_key")).append("\n");
                sb.append("Value: ").append(record.get("preference_value")).append("\n");
                sb.append("Updated: ").append(record.get("updated_timestamp")).append("\n\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Exports data in CSV format
     */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        
        // Export metadata
        sb.append("Export Metadata\n");
        sb.append("User ID,Guild ID,Export Timestamp\n");
        sb.append(userId).append(",").append(guildId).append(",").append(exportTimestamp).append("\n\n");
        
        // Consent records
        sb.append("Consent Records\n");
        sb.append("Purposes,Consent Timestamp,Method,Active,Withdrawal Timestamp\n");
        for (Map<String, Object> record : consentRecords) {
            sb.append(csvEscape(record.get("purposes"))).append(",")
              .append(csvEscape(record.get("consent_timestamp"))).append(",")
              .append(csvEscape(record.get("consent_method"))).append(",")
              .append(record.get("is_active")).append(",")
              .append(csvEscape(record.get("withdrawal_timestamp"))).append("\n");
        }
        sb.append("\n");
        
        // Moderation records
        sb.append("Moderation Records\n");
        sb.append("Action Type,Reason,Moderator ID,Timestamp,Duration\n");
        for (Map<String, Object> record : moderationRecords) {
            sb.append(csvEscape(record.get("action_type"))).append(",")
              .append(csvEscape(record.get("reason"))).append(",")
              .append(csvEscape(record.get("moderator_id"))).append(",")
              .append(csvEscape(record.get("timestamp"))).append(",")
              .append(record.get("duration")).append("\n");
        }
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Escapes CSV values
     */
    private String csvEscape(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
    
    /**
     * Gets the total number of records in this export
     */
    public int getTotalRecords() {
        return consentRecords.size() + moderationRecords.size() + 
               activityRecords.size() + preferenceRecords.size();
    }
    
    /**
     * Checks if the export contains any data
     */
    public boolean hasData() {
        return getTotalRecords() > 0;
    }
}