package com.axion.bot.moderation;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents recent user activity for behavioral analysis
 * Used to track user behavior patterns and detect anomalies
 */
public class RecentActivity {
    private final String userId;
    private final String guildId;
    private final String channelId;
    private final String activityType;
    private final String content;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    
    public RecentActivity(String userId, String guildId, String channelId, 
                         String activityType, String content, Instant timestamp) {
        this.userId = userId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.activityType = activityType;
        this.content = content;
        this.timestamp = timestamp;
        this.metadata = new HashMap<>();
    }
    
    public RecentActivity(String userId, String guildId, String channelId, 
                         String activityType, String content, Instant timestamp,
                         Map<String, Object> metadata) {
        this.userId = userId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.activityType = activityType;
        this.content = content;
        this.timestamp = timestamp;
        this.metadata = new HashMap<>(metadata);
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public String getChannelId() { return channelId; }
    public String getActivityType() { return activityType; }
    public String getContent() { return content; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    
    // Utility methods
    public boolean isMessageActivity() {
        return "MESSAGE".equals(activityType);
    }
    
    public boolean isReactionActivity() {
        return "REACTION".equals(activityType);
    }
    
    public boolean isVoiceActivity() {
        return "VOICE".equals(activityType);
    }
    
    public boolean isWithinTimeframe(Instant cutoff) {
        return timestamp.isAfter(cutoff);
    }
    
    public int getContentLength() {
        return content != null ? content.length() : 0;
    }
    
    public boolean containsLinks() {
        return content != null && content.matches(".*https?://.*");
    }
    
    public boolean containsMentions() {
        return content != null && content.contains("@");
    }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    @Override
    public String toString() {
        return String.format("RecentActivity{userId='%s', guildId='%s', channelId='%s', activityType='%s', timestamp=%s}",
                userId, guildId, channelId, activityType, timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RecentActivity that = (RecentActivity) obj;
        return userId.equals(that.userId) &&
               guildId.equals(that.guildId) &&
               channelId.equals(that.channelId) &&
               activityType.equals(that.activityType) &&
               timestamp.equals(that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, guildId, channelId, activityType, timestamp);
    }
    
    // Static factory methods
    public static RecentActivity messageActivity(String userId, String guildId, String channelId, String content) {
        return new RecentActivity(userId, guildId, channelId, "MESSAGE", content, Instant.now());
    }
    
    public static RecentActivity reactionActivity(String userId, String guildId, String channelId, String emoji) {
        return new RecentActivity(userId, guildId, channelId, "REACTION", emoji, Instant.now());
    }
    
    public static RecentActivity voiceActivity(String userId, String guildId, String channelId, String action) {
        return new RecentActivity(userId, guildId, channelId, "VOICE", action, Instant.now());
    }
}