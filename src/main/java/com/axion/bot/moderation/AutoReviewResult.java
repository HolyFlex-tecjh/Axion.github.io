package com.axion.bot.moderation;

import java.util.*;

/**
 * Model class representing the result of an automatic review
 */
public class AutoReviewResult {
    private final ReviewDecision decision;
    private final double confidence;
    private final String reason;
    private final List<String> factors;
    
    public AutoReviewResult(ReviewDecision decision, double confidence, String reason, List<String> factors) {
        this.decision = decision;
        this.confidence = confidence;
        this.reason = reason;
        this.factors = new ArrayList<>(factors);
    }
    
    // Getters
    public ReviewDecision getDecision() { return decision; }
    public double getConfidence() { return confidence; }
    public String getReason() { return reason; }
    public List<String> getFactors() { return new ArrayList<>(factors); }
}