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
 * Engine for handling escalation-based moderation rules
 */
public class EscalationEngine {
    private static final Logger logger = LoggerFactory.getLogger(EscalationEngine.class);
    
    private final EscalationConfig config;
    private final Map<String, List<ViolationEvent>> userViolationHistory = new ConcurrentHashMap<>();
    
    public EscalationEngine(EscalationConfig config) {
        this.config = config;
    }
    
    /**
     * Evaluate escalation rules and determine if escalation is needed
     */
    public EscalationResult evaluateEscalation(ModerationContext context, double baseConfidence) {
        if (!config.isEnableEscalation()) {
            return new EscalationResult(false, baseConfidence, new ArrayList<>());
        }
        
        try {
            String userId = context.getUserId();
            
            // Get user's violation history
            List<ViolationEvent> userHistory = userViolationHistory.computeIfAbsent(
                userId, k -> new ArrayList<>()
            );
            
            // Clean old violations outside escalation window
            Instant cutoff = Instant.now().minus(config.getEscalationWindow());
            userHistory.removeIf(event -> event.getTimestamp().isBefore(cutoff));
            
            // Add current violation
            ViolationEvent currentViolation = new ViolationEvent(
                Instant.now(),
                context.getGuildId(),
                context.getChannelId(),
                baseConfidence
            );
            userHistory.add(currentViolation);
            
            // Evaluate escalation
            List<String> escalationFactors = new ArrayList<>();
            double escalatedConfidence = baseConfidence;
            boolean shouldEscalate = false;
            
            // Check violation count threshold
            if (userHistory.size() >= config.getEscalationThreshold()) {
                shouldEscalate = true;
                escalationFactors.add("Violation threshold exceeded: " + userHistory.size() + 
                                    " violations in " + config.getEscalationWindow().toDays() + " days");
                
                // Apply escalation multiplier
                double escalationFactor = calculateEscalationFactor(userHistory.size());
                escalatedConfidence = Math.min(1.0, baseConfidence * escalationFactor);
            }
            
            // Check for pattern escalation
            if (detectEscalatingPattern(userHistory)) {
                shouldEscalate = true;
                escalationFactors.add("Escalating violation pattern detected");
                escalatedConfidence = Math.min(1.0, escalatedConfidence * 1.2);
            }
            
            // Check for cross-guild violations
            if (detectCrossGuildViolations(userHistory, context.getGuildId())) {
                shouldEscalate = true;
                escalationFactors.add("Cross-guild violation pattern detected");
                escalatedConfidence = Math.min(1.0, escalatedConfidence * 1.3);
            }
            
            // Check for rapid violations
            if (detectRapidViolations(userHistory)) {
                shouldEscalate = true;
                escalationFactors.add("Rapid violation pattern detected");
                escalatedConfidence = Math.min(1.0, escalatedConfidence * 1.1);
            }
            
            return new EscalationResult(shouldEscalate, escalatedConfidence, escalationFactors);
            
        } catch (Exception e) {
            logger.error("Error evaluating escalation for user {}", context.getUserId(), e);
            return new EscalationResult(false, baseConfidence, 
                List.of("Escalation evaluation failed: " + e.getMessage()));
        }
    }
    
    private double calculateEscalationFactor(int violationCount) {
        // Progressive escalation based on violation count
        if (violationCount >= config.getEscalationThreshold()) {
            int excessViolations = violationCount - config.getEscalationThreshold();
            return Math.pow(config.getEscalationMultiplier(), excessViolations + 1);
        }
        return 1.0;
    }
    
    private boolean detectEscalatingPattern(List<ViolationEvent> history) {
        if (history.size() < 3) return false;
        
        // Check if violation confidence is increasing over time
        for (int i = 1; i < history.size(); i++) {
            if (history.get(i).getConfidence() <= history.get(i - 1).getConfidence()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean detectCrossGuildViolations(List<ViolationEvent> history, String currentGuildId) {
        if (history.size() < 2) return false;
        
        long differentGuilds = history.stream()
            .map(ViolationEvent::getGuildId)
            .distinct()
            .count();
            
        return differentGuilds > 1;
    }
    
    private boolean detectRapidViolations(List<ViolationEvent> history) {
        if (history.size() < 3) return false;
        
        // Check if last 3 violations occurred within 1 hour
        List<ViolationEvent> recent = history.subList(Math.max(0, history.size() - 3), history.size());
        Instant earliest = recent.get(0).getTimestamp();
        Instant latest = recent.get(recent.size() - 1).getTimestamp();

        // Example usage of channelId: check if all violations happened in the same channel
        boolean sameChannel = recent.stream()
            .map(ViolationEvent::getChannelId)
            .distinct()
            .count() == 1;
        
        return Duration.between(earliest, latest).toHours() < 1 && sameChannel;
    }
    
    /**
     * Clean up old violation history to prevent memory leaks
     */
    public void cleanupOldHistory() {
        Instant cutoff = Instant.now().minus(config.getEscalationWindow().multipliedBy(2));
        
        userViolationHistory.entrySet().removeIf(entry -> {
            List<ViolationEvent> history = entry.getValue();
            history.removeIf(event -> event.getTimestamp().isBefore(cutoff));
            return history.isEmpty();
        });
    }
    
    /**
     * Get escalation statistics for a user
     */
    public EscalationStats getEscalationStats(String userId) {
        List<ViolationEvent> history = userViolationHistory.getOrDefault(userId, new ArrayList<>());
        
        // Clean old violations
        Instant cutoff = Instant.now().minus(config.getEscalationWindow());
        history.removeIf(event -> event.getTimestamp().isBefore(cutoff));
        
        int violationCount = history.size();
        boolean isEscalated = violationCount >= config.getEscalationThreshold();
        double escalationLevel = isEscalated ? calculateEscalationFactor(violationCount) : 1.0;
        
        return new EscalationStats(violationCount, isEscalated, escalationLevel);
    }

    /**
     * Represents a violation event for escalation tracking
     */
    private static class ViolationEvent {
        private final Instant timestamp;
        private final String guildId;
        private final String channelId;
        private final double confidence;

        public ViolationEvent(Instant timestamp, String guildId, String channelId, double confidence) {
            this.timestamp = timestamp;
            this.guildId = guildId;
            this.channelId = channelId;
            this.confidence = confidence;
        }

        public Instant getTimestamp() { return timestamp; }
        public String getGuildId() { return guildId; }
        public String getChannelId() { return channelId; }
        public double getConfidence() { return confidence; }
    }
    
    /**
     * Result of escalation evaluation
     */
    public static class EscalationResult {
        private final boolean shouldEscalate;
        private final double escalatedConfidence;
        private final List<String> escalationFactors;
        
        public EscalationResult(boolean shouldEscalate, double escalatedConfidence, List<String> escalationFactors) {
            this.shouldEscalate = shouldEscalate;
            this.escalatedConfidence = escalatedConfidence;
            this.escalationFactors = escalationFactors;
        }
        
        public boolean shouldEscalate() { return shouldEscalate; }
        public double getEscalatedConfidence() { return escalatedConfidence; }
        public List<String> getEscalationFactors() { return escalationFactors; }
    }
    
    /**
     * Escalation statistics for a user
     */
    public static class EscalationStats {
        private final int violationCount;
        private final boolean isEscalated;
        private final double escalationLevel;
        
        public EscalationStats(int violationCount, boolean isEscalated, double escalationLevel) {
            this.violationCount = violationCount;
            this.isEscalated = isEscalated;
            this.escalationLevel = escalationLevel;
        }
        
        public int getViolationCount() { return violationCount; }
        public boolean isEscalated() { return isEscalated; }
        public double getEscalationLevel() { return escalationLevel; }
    }
}