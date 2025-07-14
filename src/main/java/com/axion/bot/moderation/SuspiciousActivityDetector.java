package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Detects suspicious activity patterns in user behavior
 */
public class SuspiciousActivityDetector {
    private final SuspiciousActivityConfig config;
    
    public SuspiciousActivityDetector(SuspiciousActivityConfig config) {
        this.config = config;
    }
    
    public Optional<BehaviorPattern> detectPattern(UserBehaviorProfile profile, UserActivity activity) {
        // Check for rapid actions
        List<UserActivity> recentActivities = profile.getRecentActivities(config.getRapidActionWindow());
        
        if (recentActivities.size() >= config.getRapidActionThreshold()) {
            return Optional.of(new BehaviorPattern(
                PatternType.SUSPICIOUS_ACTIVITY,
                0.6,
                "Rapid action pattern detected: " + recentActivities.size() + " actions in " + 
                config.getRapidActionWindow().toMinutes() + " minutes"
            ));
        }
        
        // Check for suspicious timing patterns
        if (isSuspiciousTiming(activity)) {
            return Optional.of(new BehaviorPattern(
                PatternType.SUSPICIOUS_TIMING,
                0.4,
                "Activity at suspicious time detected"
            ));
        }
        
        // Check for bot-like behavior
        if (isBotLikeBehavior(profile, activity)) {
            return Optional.of(new BehaviorPattern(
                PatternType.SUSPICIOUS_ACTIVITY,
                0.7,
                "Bot-like behavior pattern detected"
            ));
        }
        
        // Check for evasion attempts
        if (isEvasionAttempt(activity)) {
            return Optional.of(new BehaviorPattern(
                PatternType.EVASION,
                0.5,
                "Potential filter evasion detected"
            ));
        }
        
        return Optional.empty();
    }
    
    private boolean isSuspiciousTiming(UserActivity activity) {
        // Check if activity occurs at unusual hours (2-6 AM UTC)
        int hour = activity.getTimestamp().atZone(java.time.ZoneOffset.UTC).getHour();
        return hour >= 2 && hour <= 6;
    }
    
    private boolean isBotLikeBehavior(UserBehaviorProfile profile, UserActivity activity) {
        List<UserActivity> recentActivities = profile.getRecentActivities(Duration.ofMinutes(10));
        
        if (recentActivities.size() < 5) {
            return false;
        }
        
        // Check for perfectly timed intervals
        boolean hasRegularIntervals = true;
        long expectedInterval = 0;
        
        for (int i = 1; i < Math.min(recentActivities.size(), 5); i++) {
            long interval = Duration.between(
                recentActivities.get(i-1).getTimestamp(),
                recentActivities.get(i).getTimestamp()
            ).toSeconds();
            
            if (i == 1) {
                expectedInterval = interval;
            } else if (Math.abs(interval - expectedInterval) > 2) { // Allow 2 second variance
                hasRegularIntervals = false;
                break;
            }
        }
        
        return hasRegularIntervals && expectedInterval > 0 && expectedInterval < 60;
    }
    
    private boolean isEvasionAttempt(UserActivity activity) {
        if (activity.getType() != ActivityType.MESSAGE) {
            return false;
        }
        
        String content = activity.getContent();
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Check for character substitution
        boolean hasSubstitution = content.matches(".*[4@][3€][1!|][0°][5$].*");
        
        // Check for excessive spacing
        boolean hasSpacing = content.matches(".*\\w\\s+\\w\\s+\\w.*");
        
        // Check for unicode abuse
        boolean hasUnicodeAbuse = content.matches(".*[\u0300-\u036F]{3,}.*");
        
        // Check for zero-width characters
        boolean hasZeroWidth = content.matches(".*[\u200B\u200C\u200D\uFEFF].*");
        
        return hasSubstitution || hasSpacing || hasUnicodeAbuse || hasZeroWidth;
    }
}