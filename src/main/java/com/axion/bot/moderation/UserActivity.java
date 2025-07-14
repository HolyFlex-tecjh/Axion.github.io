package com.axion.bot.moderation;

import java.time.Instant;

/**
 * Represents a user activity for behavioral analysis
 */
public class UserActivity {
    private final String userId;
    private final String content;
    private final Instant timestamp;
    private final ActivityType type;
    private final String targetMessageId;
    private double severity = 0.0;

    public UserActivity(String userId, String content, Instant timestamp) {
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = ActivityType.MESSAGE; // Default to MESSAGE type
        this.targetMessageId = null;
    }

    public UserActivity(String userId, String content, Instant timestamp, ActivityType type) {
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.targetMessageId = null;
    }

    public UserActivity(String userId, String content, Instant timestamp, ActivityType type, String targetMessageId) {
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.targetMessageId = targetMessageId;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public Instant getTimestamp() { return timestamp; }
    public ActivityType getType() { return type; }
    public String getTargetMessageId() { return targetMessageId; }
    public double getSeverity() { return severity; }

    // Setter for severity
    public void setSeverity(double severity) {
        this.severity = Math.max(0.0, Math.min(1.0, severity));
    }

    // Analysis methods
    public boolean isViolation() {
        return severity > 0.5; // Consider activities with severity > 0.5 as violations
    }

    public boolean isSpam() {
        // Check if this activity is spam based on content patterns
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Simple spam detection patterns
        String lowerContent = content.toLowerCase();
        
        // Check for excessive repetition
        if (content.length() > 10) {
            char firstChar = content.charAt(0);
            long sameCharCount = content.chars().filter(c -> c == firstChar).count();
            if (sameCharCount > content.length() * 0.7) {
                return true;
            }
        }
        
        // Check for common spam patterns
        return lowerContent.matches(".*(free money|click here|buy now|limited time|act now).*") ||
               lowerContent.matches(".*(www\\.|http|discord\\.gg).*") && severity > 0.3;
    }

    public boolean isToxic() {
        // Check if this activity contains toxic content
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        
        // Simple toxicity detection based on common toxic patterns
        String[] toxicPatterns = {
            "hate", "stupid", "idiot", "kill yourself", "kys", "toxic", 
            "trash", "garbage", "noob", "loser", "pathetic"
        };
        
        for (String pattern : toxicPatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }
        
        // Check severity threshold for toxicity
        return severity > 0.6;
    }

    public double getToxicityLevel() {
        return isToxic() ? severity : 0.0;
    }

    public double getSpamLevel() {
        return isSpam() ? severity : 0.0;
    }

    @Override
    public String toString() {
        return String.format("UserActivity{userId='%s', type=%s, timestamp=%s, severity=%.2f}", 
                           userId, type, timestamp, severity);
    }
}