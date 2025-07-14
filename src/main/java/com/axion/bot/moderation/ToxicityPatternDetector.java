package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Detects toxicity patterns in user behavior
 */
public class ToxicityPatternDetector {
    private final ToxicityDetectionConfig config;
    
    public ToxicityPatternDetector(ToxicityDetectionConfig config) {
        this.config = config;
    }
    
    public Optional<BehaviorPattern> detectPattern(UserBehaviorProfile profile, UserActivity activity) {
        if (activity.getType() != ActivityType.MESSAGE) {
            return Optional.empty();
        }
        
        // Check for toxic content
        double toxicityScore = calculateToxicityScore(activity.getContent());
        
        if (toxicityScore >= config.getToxicityThreshold()) {
            // Check for escalation pattern
            List<UserActivity> recentActivities = profile.getRecentActivities(Duration.ofMinutes(30));
            long consecutiveToxic = recentActivities.stream()
                .filter(a -> a.isToxic())
                .count();
            
            if (consecutiveToxic >= config.getConsecutiveToxicThreshold()) {
                return Optional.of(new BehaviorPattern(
                    PatternType.TOXICITY,
                    Math.min(0.9, toxicityScore + (consecutiveToxic * 0.1)),
                    "Consecutive toxic messages detected"
                ));
            }
            
            return Optional.of(new BehaviorPattern(
                PatternType.TOXICITY,
                toxicityScore,
                "Toxic content detected"
            ));
        }
        
        return Optional.empty();
    }
    
    private double calculateToxicityScore(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }
        
        String lowerContent = content.toLowerCase();
        double score = 0.0;
        
        // Check for profanity
        if (containsProfanity(lowerContent)) {
            score += 0.4;
        }
        
        // Check for hate speech indicators
        if (containsHateSpeech(lowerContent)) {
            score += 0.6;
        }
        
        // Check for threats
        if (containsThreats(lowerContent)) {
            score += 0.8;
        }
        
        // Check for harassment patterns
        if (containsHarassment(lowerContent)) {
            score += 0.5;
        }
        
        return Math.min(1.0, score);
    }
    
    private boolean containsProfanity(String content) {
        String[] profanityWords = {"damn", "hell", "crap", "stupid", "idiot"};
        for (String word : profanityWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsHateSpeech(String content) {
        return content.matches(".*(racist|sexist|homophobic|transphobic).*");
    }
    
    private boolean containsThreats(String content) {
        return content.matches(".*(kill|hurt|harm|attack|destroy).*you.*") ||
               content.matches(".*(gonna|going to|will).*(kill|hurt|harm|attack).*");
    }
    
    private boolean containsHarassment(String content) {
        return content.matches(".*(shut up|go away|leave|nobody likes you).*");
    }
}