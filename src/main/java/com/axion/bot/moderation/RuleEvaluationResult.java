package com.axion.bot.moderation;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Result of evaluating a moderation rule
 */
public class RuleEvaluationResult {
    private final boolean match;
    private final List<RuleMatch> ruleMatches;
    private final double confidence;
    private final long evaluationTimeMs;
    private final Instant timestamp;
    private final String error;
    private final long cacheExpiryMs;
    
    public RuleEvaluationResult(boolean match, List<RuleMatch> ruleMatches, double confidence, long evaluationTimeMs) {
        this.match = match;
        this.ruleMatches = ruleMatches != null ? new ArrayList<>(ruleMatches) : new ArrayList<>();
        this.confidence = confidence;
        this.evaluationTimeMs = evaluationTimeMs;
        this.timestamp = Instant.now();
        this.error = null;
        this.cacheExpiryMs = 300000; // 5 minutes default
    }
    
    private RuleEvaluationResult(String error) {
        this.match = false;
        this.ruleMatches = new ArrayList<>();
        this.confidence = 0.0;
        this.evaluationTimeMs = 0;
        this.timestamp = Instant.now();
        this.error = error;
        this.cacheExpiryMs = 60000; // 1 minute for errors
    }
    
    public static RuleEvaluationResult error(String message) {
        return new RuleEvaluationResult(message);
    }
    
    public static RuleEvaluationResult noMatch() {
        return new RuleEvaluationResult(false, new ArrayList<>(), 0.0, 0);
    }
    
    public static RuleEvaluationResult match(double confidence, long evaluationTimeMs) {
        return new RuleEvaluationResult(true, new ArrayList<>(), confidence, evaluationTimeMs);
    }
    
    // Getters
    public boolean isMatch() { return match; }
    public List<RuleMatch> getRuleMatches() { return ruleMatches; }
    public double getConfidence() { return confidence; }
    public long getEvaluationTimeMs() { return evaluationTimeMs; }
    public Instant getTimestamp() { return timestamp; }
    public String getError() { return error; }
    public boolean hasError() { return error != null; }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp.toEpochMilli() > cacheExpiryMs;
    }
    
    public boolean isExpired(long customExpiryMs) {
        return System.currentTimeMillis() - timestamp.toEpochMilli() > customExpiryMs;
    }
    
    /**
     * Get the highest severity from all matched rules
     */
    public ModerationSeverity getHighestSeverity() {
        return ruleMatches.stream()
            .map(match -> match.getRule().getSeverity())
            .max(Enum::compareTo)
            .orElse(ModerationSeverity.LOW);
    }
    
    /**
     * Get all violated rule names
     */
    public List<String> getViolatedRules() {
        return ruleMatches.stream()
            .map(match -> match.getRule().getName())
            .toList();
    }
    
    /**
     * Get combined match reasons from all rules
     */
    public List<String> getAllReasons() {
        List<String> allReasons = new ArrayList<>();
        for (RuleMatch match : ruleMatches) {
            allReasons.addAll(match.getReasons());
        }
        return allReasons;
    }
    
    /**
     * Create a summary of the evaluation result
     */
    public String getSummary() {
        if (hasError()) {
            return "Error: " + error;
        }
        
        if (!match) {
            return "No violations detected";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Violations detected: ");
        summary.append(ruleMatches.size()).append(" rule(s) triggered");
        summary.append(", confidence: ").append(String.format("%.2f", confidence));
        summary.append(", severity: ").append(getHighestSeverity());
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "RuleEvaluationResult{" +
                "match=" + match +
                ", confidence=" + confidence +
                ", ruleMatches=" + ruleMatches.size() +
                ", evaluationTimeMs=" + evaluationTimeMs +
                ", error='" + error + '\'' +
                '}';
    }
}

/**
 * Represents a match between a rule and content
 */
class RuleMatch {
    private final ModerationRule rule;
    private final RuleEvaluationResult result;
    private final List<String> reasons;
    private final double confidence;
    
    public RuleMatch(ModerationRule rule, RuleEvaluationResult result) {
        this.rule = rule;
        this.result = result;
        this.reasons = result.getAllReasons();
        this.confidence = result.getConfidence();
    }
    
    public RuleMatch(ModerationRule rule, List<String> reasons, double confidence) {
        this.rule = rule;
        this.result = null;
        this.reasons = new ArrayList<>(reasons);
        this.confidence = confidence;
    }
    
    public ModerationRule getRule() { return rule; }
    public RuleEvaluationResult getResult() { return result; }
    public List<String> getReasons() { return reasons; }
    public double getConfidence() { return confidence; }
    
    @Override
    public String toString() {
        return "RuleMatch{" +
                "rule=" + rule.getName() +
                ", confidence=" + confidence +
                ", reasons=" + reasons.size() +
                '}';
    }
}