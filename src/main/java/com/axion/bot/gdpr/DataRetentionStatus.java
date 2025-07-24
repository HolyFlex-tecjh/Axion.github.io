package com.axion.bot.gdpr;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the data retention status for a user
 */
public class DataRetentionStatus {
    private final String userId;
    private final String guildId;
    private final Map<String, Object> retentionInfo;
    private final Instant lastChecked;
    
    public DataRetentionStatus(String userId, String guildId, Map<String, Object> retentionInfo) {
        this.userId = userId;
        this.guildId = guildId;
        this.retentionInfo = new HashMap<>(retentionInfo);
        this.lastChecked = Instant.now();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public Map<String, Object> getRetentionInfo() {
        return new HashMap<>(retentionInfo);
    }
    
    public Instant getLastChecked() {
        return lastChecked;
    }
    
    public Object getRetentionValue(String key) {
        return retentionInfo.get(key);
    }
    
    public boolean hasRetentionInfo(String key) {
        return retentionInfo.containsKey(key);
    }
}