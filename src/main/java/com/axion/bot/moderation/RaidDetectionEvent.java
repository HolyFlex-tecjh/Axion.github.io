package com.axion.bot.moderation;

import java.time.Instant;

/**
 * Represents a raid detection event for tracking suspicious activity patterns
 */
public class RaidDetectionEvent {
    private final String userId;
    private final String guildId;
    private final Instant timestamp;
    private final RaidEventType eventType;
    private final String details;
    private final String channelId;
    private final int suspicionLevel;
    
    public enum RaidEventType {
        RAPID_JOIN,
        MASS_MESSAGE,
        SUSPICIOUS_USERNAME,
        NEW_ACCOUNT_JOIN,
        COORDINATED_ACTIVITY,
        SPAM_PATTERN,
        INVITE_SPAM
    }
    
    public RaidDetectionEvent(String userId, String guildId, RaidEventType eventType, String details) {
        this(userId, guildId, eventType, details, null, 1);
    }
    
    public RaidDetectionEvent(String userId, String guildId, RaidEventType eventType, String details, String channelId) {
        this(userId, guildId, eventType, details, channelId, 1);
    }
    
    public RaidDetectionEvent(String userId, String guildId, RaidEventType eventType, String details, String channelId, int suspicionLevel) {
        this.userId = userId;
        this.guildId = guildId;
        this.timestamp = Instant.now();
        this.eventType = eventType;
        this.details = details;
        this.channelId = channelId;
        this.suspicionLevel = Math.max(1, Math.min(10, suspicionLevel));
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public Instant getTimestamp() { return timestamp; }
    public RaidEventType getEventType() { return eventType; }
    public String getDetails() { return details; }
    public String getChannelId() { return channelId; }
    public int getSuspicionLevel() { return suspicionLevel; }
    
    /**
     * Check if this event is recent (within the specified timeframe)
     */
    public boolean isRecent(long timeframeMs) {
        return Instant.now().toEpochMilli() - timestamp.toEpochMilli() <= timeframeMs;
    }
    
    /**
     * Get the age of this event in milliseconds
     */
    public long getAgeMs() {
        return Instant.now().toEpochMilli() - timestamp.toEpochMilli();
    }
    
    /**
     * Check if this event matches another event for correlation
     */
    public boolean correlatesWith(RaidDetectionEvent other, long maxTimeDifferenceMs) {
        if (other == null || !this.guildId.equals(other.guildId)) {
            return false;
        }
        
        long timeDiff = Math.abs(this.timestamp.toEpochMilli() - other.timestamp.toEpochMilli());
        return timeDiff <= maxTimeDifferenceMs;
    }
    
    /**
     * Get the severity weight of this event type
     */
    public int getSeverityWeight() {
        switch (eventType) {
            case RAPID_JOIN:
                return 3;
            case MASS_MESSAGE:
                return 4;
            case SUSPICIOUS_USERNAME:
                return 2;
            case NEW_ACCOUNT_JOIN:
                return 2;
            case COORDINATED_ACTIVITY:
                return 5;
            case SPAM_PATTERN:
                return 3;
            case INVITE_SPAM:
                return 4;
            default:
                return 1;
        }
    }
    
    /**
     * Calculate the total threat score for this event
     */
    public int getThreatScore() {
        return getSeverityWeight() * suspicionLevel;
    }
    
    @Override
    public String toString() {
        return String.format("RaidEvent[%s:%s:%s:%d] %s", 
            eventType, userId, timestamp, suspicionLevel, details);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RaidDetectionEvent that = (RaidDetectionEvent) obj;
        return userId.equals(that.userId) && 
               guildId.equals(that.guildId) && 
               timestamp.equals(that.timestamp) && 
               eventType == that.eventType;
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode() + guildId.hashCode() + timestamp.hashCode() + eventType.hashCode();
    }
}