package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine for evaluating behavior-based moderation rules
 */
public class BehaviorRuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorRuleEngine.class);
    
    private final BehaviorRuleConfig config;
    private final Map<String, List<MessageEvent>> userMessageHistory = new ConcurrentHashMap<>();
    
    public BehaviorRuleEngine(BehaviorRuleConfig config) {
        this.config = config;
    }
    
    /**
     * Evaluate behavior-based rules for a user
     */
    public ConditionResult evaluateBehavior(ModerationContext context) {
        List<String> violations = new ArrayList<>();
        double maxConfidence = 0.0;
        
        try {
            String userId = context.getUserId();
            
            // Track current message
            MessageEvent currentEvent = new MessageEvent(
                context.getContent(), 
                Instant.now(), 
                context.getChannelId()
            );
            
            List<MessageEvent> userHistory = userMessageHistory.computeIfAbsent(
                userId, k -> new ArrayList<>()
            );
            
            // Clean old messages outside analysis window
            Instant cutoff = Instant.now().minus(config.getBehaviorAnalysisWindow());
            userHistory.removeIf(event -> event.getTimestamp().isBefore(cutoff));
            
            // Add current message
            userHistory.add(currentEvent);
            
            // Check for spam frequency
            if (userHistory.size() > config.getSpamFrequencyThreshold()) {
                violations.add("Spam frequency exceeded: " + userHistory.size() + " messages in " + 
                             config.getBehaviorAnalysisWindow().toMinutes() + " minutes");
                maxConfidence = Math.max(maxConfidence, 0.9);
            }
            
            // Check for duplicate messages
            int duplicateCount = countDuplicateMessages(userHistory, context.getContent());
            if (duplicateCount >= config.getDuplicateMessageThreshold()) {
                violations.add("Duplicate message spam: " + duplicateCount + " identical messages");
                maxConfidence = Math.max(maxConfidence, 0.85);
            }
            
            // Check for rapid posting pattern
            if (detectRapidPosting(userHistory)) {
                violations.add("Rapid posting pattern detected");
                maxConfidence = Math.max(maxConfidence, 0.8);
            }
            
            // Check for channel flooding
            if (detectChannelFlooding(userHistory, context.getChannelId())) {
                violations.add("Channel flooding detected");
                maxConfidence = Math.max(maxConfidence, 0.75);
            }
            
            return new ConditionResult(!violations.isEmpty(), violations, maxConfidence);
            
        } catch (Exception e) {
            logger.error("Error evaluating behavior rules for user {}", context.getUserId(), e);
            return ConditionResult.error("Behavior evaluation failed: " + e.getMessage());
        }
    }
    
    private int countDuplicateMessages(List<MessageEvent> history, String currentContent) {
        return (int) history.stream()
            .map(MessageEvent::getContent)
            .filter(content -> content.equals(currentContent))
            .count();
    }
    
    private boolean detectRapidPosting(List<MessageEvent> history) {
        if (history.size() < 3) return false;
        
        // Check if last 3 messages were posted within 10 seconds
        List<MessageEvent> recent = history.subList(Math.max(0, history.size() - 3), history.size());
        Instant earliest = recent.get(0).getTimestamp();
        Instant latest = recent.get(recent.size() - 1).getTimestamp();
        
        return Duration.between(earliest, latest).getSeconds() < 10;
    }
    
    private boolean detectChannelFlooding(List<MessageEvent> history, String channelId) {
        long messagesInChannel = history.stream()
            .filter(event -> event.getChannelId().equals(channelId))
            .count();
            
        // Consider flooding if more than 70% of recent messages are in the same channel
        return history.size() > 5 && (messagesInChannel * 100.0 / history.size()) > 70;
    }
    
    /**
     * Clean up old message history to prevent memory leaks
     */
    public void cleanupOldHistory() {
        Instant cutoff = Instant.now().minus(config.getBehaviorAnalysisWindow().multipliedBy(2));
        
        userMessageHistory.entrySet().removeIf(entry -> {
            List<MessageEvent> history = entry.getValue();
            history.removeIf(event -> event.getTimestamp().isBefore(cutoff));
            return history.isEmpty();
        });
    }
    
    /**
     * Represents a message event for behavior tracking
     */
    private static class MessageEvent {
        private final String content;
        private final Instant timestamp;
        private final String channelId;
        
        public MessageEvent(String content, Instant timestamp, String channelId) {
            this.content = content;
            this.timestamp = timestamp;
            this.channelId = channelId;
        }
        
        public String getContent() { return content; }
        public Instant getTimestamp() { return timestamp; }
        public String getChannelId() { return channelId; }
    }
}