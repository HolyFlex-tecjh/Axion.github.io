package com.axion.bot.gdpr;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a response to a data subject access request under GDPR Article 15
 */
public class DataSubjectAccessResponse {
    private final String userId;
    private final String guildId;
    private final UserConsent consent;
    private final DataRetentionStatus retentionStatus;
    private final UserDataExport dataExport;
    private final Instant responseTimestamp;
    private final Map<String, Object> additionalInfo;
    
    public DataSubjectAccessResponse(String userId, String guildId, UserConsent consent, 
                                   DataRetentionStatus retentionStatus, UserDataExport dataExport) {
        this.userId = userId;
        this.guildId = guildId;
        this.consent = consent;
        this.retentionStatus = retentionStatus;
        this.dataExport = dataExport;
        this.responseTimestamp = Instant.now();
        this.additionalInfo = new HashMap<>();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public UserConsent getConsent() {
        return consent;
    }
    
    public DataRetentionStatus getRetentionStatus() {
        return retentionStatus;
    }
    
    public UserDataExport getDataExport() {
        return dataExport;
    }
    
    public Instant getResponseTimestamp() {
        return responseTimestamp;
    }
    
    public Map<String, Object> getAdditionalInfo() {
        return new HashMap<>(additionalInfo);
    }
    
    public void addAdditionalInfo(String key, Object value) {
        additionalInfo.put(key, value);
    }
    
    public Object getAdditionalInfo(String key) {
        return additionalInfo.get(key);
    }
    
    public boolean hasAdditionalInfo(String key) {
        return additionalInfo.containsKey(key);
    }
}