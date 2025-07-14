package com.axion.bot.moderation;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Enhanced pattern matching for moderation with context awareness
 */
public class EnhancedPattern {
    private final String name;
    private final Pattern pattern;
    private final ModerationSeverity severity;
    private final boolean requiresContext;
    private final double confidenceThreshold;
    
    public EnhancedPattern(String name, Pattern pattern, ModerationSeverity severity, boolean requiresContext) {
        this(name, pattern, severity, requiresContext, 0.7);
    }
    
    public EnhancedPattern(String name, Pattern pattern, ModerationSeverity severity, 
                          boolean requiresContext, double confidenceThreshold) {
        this.name = name;
        this.pattern = pattern;
        this.severity = severity;
        this.requiresContext = requiresContext;
        this.confidenceThreshold = confidenceThreshold;
    }
    
    /**
     * Check if the pattern matches the content
     */
    public boolean matches(String content) {
        return pattern.matcher(content).find();
    }
    
    /**
     * Check if the pattern matches with contextual data
     */
    public boolean matches(String content, Map<String, Object> contextualData) {
        if (!matches(content)) {
            return false;
        }
        
        if (!requiresContext) {
            return true;
        }
        
        // Apply contextual filtering
        return evaluateContext(content, contextualData);
    }
    
    /**
     * Evaluate contextual factors to reduce false positives
     */
    private boolean evaluateContext(String content, Map<String, Object> contextualData) {
        if (contextualData == null || contextualData.isEmpty()) {
            return !requiresContext;
        }
        
        // Check user trust level
        Object trustScore = contextualData.get("trustScore");
        if (trustScore instanceof Integer && (Integer) trustScore > 80) {
            // High trust users get benefit of doubt for lower severity patterns
            if (severity == ModerationSeverity.LOW || severity == ModerationSeverity.MEDIUM) {
                return false;
            }
        }
        
        // Check message context
        Object messageCount = contextualData.get("recentMessageCount");
        if (messageCount instanceof Integer && (Integer) messageCount > 10) {
            // Rapid messaging might indicate spam context
            return true;
        }
        
        // Check channel context
        Object channelType = contextualData.get("channelType");
        if ("PRIVATE".equals(channelType)) {
            // More lenient in private channels
            return severity == ModerationSeverity.HIGH || severity == ModerationSeverity.VERY_HIGH;
        }
        
        return true;
    }
    
    /**
     * Get the confidence score for this match
     */
    public double getConfidence(String content, Map<String, Object> contextualData) {
        if (!matches(content)) {
            return 0.0;
        }
        
        double baseConfidence = 0.8; // Base confidence for pattern match
        
        // Adjust based on context
        if (contextualData != null) {
            Object trustScore = contextualData.get("trustScore");
            if (trustScore instanceof Integer) {
                int trust = (Integer) trustScore;
                if (trust > 80) {
                    baseConfidence *= 0.7; // Reduce confidence for trusted users
                } else if (trust < 30) {
                    baseConfidence *= 1.2; // Increase confidence for untrusted users
                }
            }
        }
        
        return Math.min(1.0, baseConfidence);
    }
    
    // Getters
    public String getName() { return name; }
    public Pattern getPattern() { return pattern; }
    public ModerationSeverity getSeverity() { return severity; }
    public boolean isRequiresContext() { return requiresContext; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    
    @Override
    public String toString() {
        return String.format("EnhancedPattern{name='%s', severity=%s, requiresContext=%s}",
            name, severity, requiresContext);
    }
}