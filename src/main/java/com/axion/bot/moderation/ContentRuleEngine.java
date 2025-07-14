package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Engine for evaluating content-based moderation rules
 */
public class ContentRuleEngine {
    private static final Logger logger = LoggerFactory.getLogger(ContentRuleEngine.class);
    
    private final ContentRuleConfig config;
    private final Pattern urlPattern;
    private final Pattern mentionPattern;
    
    public ContentRuleEngine(ContentRuleConfig config) {
        this.config = config;
        this.urlPattern = Pattern.compile("https?://[\\w\\-._~:/?#[\\]@!$&'()*+,;=%]+");
        this.mentionPattern = Pattern.compile("<@[!&]?\\d+>");
    }
    
    /**
     * Evaluate content against content-based rules
     */
    public ConditionResult evaluateContent(String content, ModerationContext context) {
        List<String> violations = new ArrayList<>();
        double maxConfidence = 0.0;
        
        try {
            // Check for excessive caps
            double capsPercentage = calculateCapsPercentage(content);
            if (capsPercentage > config.getCapsPercentageThreshold()) {
                violations.add("Excessive caps usage: " + String.format("%.1f%%", capsPercentage));
                maxConfidence = Math.max(maxConfidence, 0.8);
            }
            
            // Check for excessive links
            int linkCount = countLinks(content);
            if (linkCount > config.getMaxLinksPerMessage()) {
                violations.add("Excessive links: " + linkCount + " (max: " + config.getMaxLinksPerMessage() + ")");
                maxConfidence = Math.max(maxConfidence, 0.9);
            }
            
            // Check for spam patterns
            if (isSpamPattern(content)) {
                violations.add("Spam pattern detected");
                maxConfidence = Math.max(maxConfidence, 0.85);
            }
            
            // Check for excessive mentions
            int mentionCount = countMentions(content);
            if (mentionCount > 5) {
                violations.add("Excessive mentions: " + mentionCount);
                maxConfidence = Math.max(maxConfidence, 0.7);
            }
            
            return new ConditionResult(!violations.isEmpty(), violations, maxConfidence);
            
        } catch (Exception e) {
            logger.error("Error evaluating content rules", e);
            return ConditionResult.error("Content evaluation failed: " + e.getMessage());
        }
    }
    
    private double calculateCapsPercentage(String content) {
        if (content.length() == 0) return 0.0;
        
        long capsCount = content.chars()
            .filter(Character::isUpperCase)
            .count();
            
        long letterCount = content.chars()
            .filter(Character::isLetter)
            .count();
            
        return letterCount > 0 ? (capsCount * 100.0) / letterCount : 0.0;
    }
    
    private int countLinks(String content) {
        Matcher matcher = urlPattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    private int countMentions(String content) {
        Matcher matcher = mentionPattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    private boolean isSpamPattern(String content) {
        // Check for repeated characters
        if (content.matches(".*(..)\\1{3,}.*")) {
            return true;
        }
        
        // Check for excessive repetition of words
        String[] words = content.toLowerCase().split("\\s+");
        if (words.length > 3) {
            String firstWord = words[0];
            int repetitions = 0;
            for (String word : words) {
                if (word.equals(firstWord)) {
                    repetitions++;
                }
            }
            if (repetitions > words.length / 2) {
                return true;
            }
        }
        
        return false;
    }
}

/**
 * Result of a condition evaluation
 */
class ConditionResult {
    private final boolean match;
    private final List<String> reasons;
    private final double confidence;
    private final String error;
    
    public ConditionResult(boolean match, List<String> reasons, double confidence) {
        this.match = match;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.confidence = confidence;
        this.error = null;
    }
    
    private ConditionResult(String error) {
        this.match = false;
        this.reasons = new ArrayList<>();
        this.confidence = 0.0;
        this.error = error;
    }
    
    public static ConditionResult error(String message) {
        return new ConditionResult(message);
    }
    
    public static ConditionResult match(String reason, double confidence) {
        List<String> reasons = new ArrayList<>();
        reasons.add(reason);
        return new ConditionResult(true, reasons, confidence);
    }
    
    public static ConditionResult noMatch(String reason) {
        List<String> reasons = new ArrayList<>();
        reasons.add(reason);
        return new ConditionResult(false, reasons, 0.0);
    }
    
    public boolean isMatch() { return match; }
    public List<String> getReasons() { return reasons; }
    public double getConfidence() { return confidence; }
    public String getError() { return error; }
    public boolean hasError() { return error != null; }
}