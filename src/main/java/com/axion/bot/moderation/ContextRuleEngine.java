package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Engine for evaluating context-based moderation rules
 */
public class ContextRuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(ContextRuleEngine.class);
    
    private final ContextRuleConfig config;
    
    public ContextRuleEngine(ContextRuleConfig config) {
        this.config = config;
    }
    
    /**
     * Evaluate context-based rules
     */
    public ConditionResult evaluateContext(ModerationContext context) {
        List<String> contextFactors = new ArrayList<>();
        double maxConfidence = 0.0;
        
        try {
            // Evaluate channel type context
            if (config.isConsiderChannelType()) {
                ChannelContextResult channelResult = evaluateChannelContext(context);
                if (channelResult.hasViolation()) {
                    contextFactors.addAll(channelResult.getFactors());
                    maxConfidence = Math.max(maxConfidence, channelResult.getConfidence());
                }
            }
            
            // Evaluate user roles context
            if (config.isConsiderUserRoles()) {
                RoleContextResult roleResult = evaluateRoleContext(context);
                if (roleResult.hasViolation()) {
                    contextFactors.addAll(roleResult.getFactors());
                    maxConfidence = Math.max(maxConfidence, roleResult.getConfidence());
                }
            }
            
            // Evaluate time of day context
            if (config.isConsiderTimeOfDay()) {
                TimeContextResult timeResult = evaluateTimeContext(context);
                if (timeResult.hasViolation()) {
                    contextFactors.addAll(timeResult.getFactors());
                    maxConfidence = Math.max(maxConfidence, timeResult.getConfidence());
                }
            }
            
            return new ConditionResult(!contextFactors.isEmpty(), contextFactors, maxConfidence);
            
        } catch (Exception e) {
            logger.error("Error evaluating context rules", e);
            return ConditionResult.error("Context evaluation failed: " + e.getMessage());
        }
    }
    
    private ChannelContextResult evaluateChannelContext(ModerationContext context) {
        List<String> factors = new ArrayList<>();
        double confidence = 0.0;
        
        String channelType = context.getChannelType();
        
        // Higher scrutiny in public channels
        if ("TEXT".equals(channelType)) {
            factors.add("Public text channel - standard moderation");
            confidence = 0.5;
        }
        
        // Different rules for voice channels
        if ("VOICE".equals(channelType)) {
            factors.add("Voice channel context");
            confidence = 0.3;
        }
        
        // Special handling for announcement channels
        if (context.getChannelName() != null && 
            (context.getChannelName().contains("announcement") || 
             context.getChannelName().contains("news"))) {
            factors.add("Announcement channel - elevated scrutiny");
            confidence = 0.8;
        }
        
        return new ChannelContextResult(!factors.isEmpty(), factors, confidence);
    }
    
    private RoleContextResult evaluateRoleContext(ModerationContext context) {
        List<String> factors = new ArrayList<>();
        double confidence = 0.0;
        
        Set<String> userRoles = context.getUserRoles();
        
        // Check for staff roles (should have reduced scrutiny)
        boolean hasStaffRole = userRoles.stream()
            .anyMatch(role -> role.toLowerCase().contains("mod") || 
                            role.toLowerCase().contains("admin") ||
                            role.toLowerCase().contains("staff"));
        
        if (hasStaffRole) {
            factors.add("User has staff role - reduced scrutiny");
            confidence = 0.2; // Lower confidence means less likely to trigger
        }
        
        // Check for new member role (should have increased scrutiny)
        boolean hasNewMemberRole = userRoles.stream()
            .anyMatch(role -> role.toLowerCase().contains("new") ||
                            role.toLowerCase().contains("member"));
        
        if (hasNewMemberRole) {
            factors.add("New member - increased scrutiny");
            confidence = 0.7;
        }
        
        // Check for muted role
        boolean hasMutedRole = userRoles.stream()
            .anyMatch(role -> role.toLowerCase().contains("muted"));
        
        if (hasMutedRole) {
            factors.add("User is muted - high scrutiny");
            confidence = 0.9;
        }
        
        return new RoleContextResult(!factors.isEmpty(), factors, confidence);
    }
    
    private TimeContextResult evaluateTimeContext(ModerationContext context) {
        List<String> factors = new ArrayList<>();
        double confidence = 0.0;
        
        try {
            LocalTime currentTime = LocalTime.now(ZoneId.of(context.getTimezone()));
            
            // Night hours (11 PM - 6 AM) might have different moderation needs
            if (currentTime.isAfter(LocalTime.of(23, 0)) || 
                currentTime.isBefore(LocalTime.of(6, 0))) {
                factors.add("Night hours - potential for different behavior patterns");
                confidence = 0.6;
            }
            
            // Peak hours (6 PM - 10 PM) might have higher activity
            if (currentTime.isAfter(LocalTime.of(18, 0)) && 
                currentTime.isBefore(LocalTime.of(22, 0))) {
                factors.add("Peak hours - high activity period");
                confidence = 0.4;
            }
            
        } catch (Exception e) {
            logger.warn("Could not evaluate time context: {}", e.getMessage());
        }
        
        return new TimeContextResult(!factors.isEmpty(), factors, confidence);
    }
    
    // Helper classes for context evaluation results
    private static class ChannelContextResult {
        private final boolean hasViolation;
        private final List<String> factors;
        private final double confidence;
        
        public ChannelContextResult(boolean hasViolation, List<String> factors, double confidence) {
            this.hasViolation = hasViolation;
            this.factors = factors;
            this.confidence = confidence;
        }
        
        public boolean hasViolation() { return hasViolation; }
        public List<String> getFactors() { return factors; }
        public double getConfidence() { return confidence; }
    }
    
    private static class RoleContextResult {
        private final boolean hasViolation;
        private final List<String> factors;
        private final double confidence;
        
        public RoleContextResult(boolean hasViolation, List<String> factors, double confidence) {
            this.hasViolation = hasViolation;
            this.factors = factors;
            this.confidence = confidence;
        }
        
        public boolean hasViolation() { return hasViolation; }
        public List<String> getFactors() { return factors; }
        public double getConfidence() { return confidence; }
    }
    
    private static class TimeContextResult {
        private final boolean hasViolation;
        private final List<String> factors;
        private final double confidence;
        
        public TimeContextResult(boolean hasViolation, List<String> factors, double confidence) {
            this.hasViolation = hasViolation;
            this.factors = factors;
            this.confidence = confidence;
        }
        
        public boolean hasViolation() { return hasViolation; }
        public List<String> getFactors() { return factors; }
        public double getConfidence() { return confidence; }
    }
}