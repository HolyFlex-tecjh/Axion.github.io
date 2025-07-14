package com.axion.bot.moderation;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Detects spam patterns in user behavior
 */
public class SpamPatternDetector {
    private final SpamDetectionConfig config;
    
    public SpamPatternDetector(SpamDetectionConfig config) {
        this.config = config;
    }
    
    public Optional<BehaviorPattern> detectPattern(UserBehaviorProfile profile, UserActivity activity) {
        if (activity.getType() != ActivityType.MESSAGE) {
            return Optional.empty();
        }
        
        // Check message frequency
        List<UserActivity> recentMessages = profile.getRecentActivities(Duration.ofMinutes(1))
            .stream()
            .filter(a -> a.getType() == ActivityType.MESSAGE)
            .collect(Collectors.toList());
        
        if (recentMessages.size() >= config.getMessageFrequencyThreshold()) {
            return Optional.of(new BehaviorPattern(
                PatternType.SPAM,
                0.7,
                "High message frequency detected: " + recentMessages.size() + " messages per minute"
            ));
        }
        
        // Check for duplicate messages
        String currentContent = normalizeContent(activity.getContent());
        long duplicateCount = recentMessages.stream()
            .filter(a -> normalizeContent(a.getContent()).equals(currentContent))
            .count();
        
        if (duplicateCount >= config.getDuplicateMessageThreshold()) {
            return Optional.of(new BehaviorPattern(
                PatternType.SPAM,
                0.8,
                "Duplicate message spam detected: " + duplicateCount + " identical messages"
            ));
        }
        
        // Check for similar messages
        long similarCount = recentMessages.stream()
            .filter(a -> calculateSimilarity(currentContent, normalizeContent(a.getContent())) >= config.getSimilarityThreshold())
            .count();
        
        if (similarCount >= config.getDuplicateMessageThreshold()) {
            return Optional.of(new BehaviorPattern(
                PatternType.SPAM,
                0.6,
                "Similar message spam detected: " + similarCount + " similar messages"
            ));
        }
        
        // Check for link spam
        if (isLinkSpam(activity, recentMessages)) {
            return Optional.of(new BehaviorPattern(
                PatternType.SPAM,
                0.7,
                "Link spam detected"
            ));
        }
        
        // Check for mention spam
        if (isMentionSpam(activity)) {
            return Optional.of(new BehaviorPattern(
                PatternType.SPAM,
                0.6,
                "Mention spam detected"
            ));
        }
        
        return Optional.empty();
    }
    
    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        
        return content.toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim();
    }
    
    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        if (text1.equals(text2)) {
            return 1.0;
        }
        
        // Simple Jaccard similarity
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        java.util.Set<String> set1 = java.util.Set.of(words1);
        java.util.Set<String> set2 = java.util.Set.of(words2);
        
        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);
        
        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private boolean isLinkSpam(UserActivity activity, List<UserActivity> recentMessages) {
        String content = activity.getContent();
        if (content == null) {
            return false;
        }
        
        // Count links in current message
        long currentLinks = content.split("https?://").length - 1;
        
        if (currentLinks > 3) {
            return true;
        }
        
        // Count total links in recent messages
        long totalLinks = recentMessages.stream()
            .mapToLong(a -> {
                String msgContent = a.getContent();
                return msgContent != null ? msgContent.split("https?://").length - 1 : 0;
            })
            .sum();
        
        return totalLinks > 5;
    }
    
    private boolean isMentionSpam(UserActivity activity) {
        String content = activity.getContent();
        if (content == null) {
            return false;
        }
        
        // Count mentions (@user or <@userid>)
        long mentionCount = content.split("@").length - 1;
        long discordMentionCount = content.split("<@").length - 1;
        
        return (mentionCount + discordMentionCount) > 5;
    }
}