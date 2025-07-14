package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks behavioral metrics for a user
 */
public class UserBehaviorMetrics {
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalViolations = new AtomicLong(0);
    private final AtomicInteger consecutiveViolations = new AtomicInteger(0);
    private final Map<ActivityType, AtomicLong> activityCounts = new HashMap<>();
    private final Map<PatternType, AtomicLong> patternCounts = new HashMap<>();
    
    private volatile double averageMessageLength = 0.0;
    private volatile double toxicityScore = 0.0;
    private volatile double spamScore = 0.0;
    private volatile Instant lastActivity = Instant.now();
    private volatile Instant firstActivity = Instant.now();
    
    // Message timing metrics
    private volatile double averageMessageInterval = 0.0;
    private volatile double messageIntervalVariance = 0.0;
    
    // Trust and reputation metrics
    private volatile double trustScore = 1.0;
    private volatile double reputationScore = 0.5;
    
    public UserBehaviorMetrics() {
        // Initialize activity counters
        for (ActivityType type : ActivityType.values()) {
            activityCounts.put(type, new AtomicLong(0));
        }
        
        // Initialize pattern counters
        for (PatternType type : PatternType.values()) {
            patternCounts.put(type, new AtomicLong(0));
        }
    }
    
    /**
     * Update metrics with a new activity
     */
    public void updateWithActivity(UserActivity activity) {
        // Update activity counts
        activityCounts.get(activity.getType()).incrementAndGet();
        
        // Update timing
        Instant now = activity.getTimestamp();
        if (lastActivity != null) {
            double interval = Duration.between(lastActivity, now).toMillis() / 1000.0;
            updateMessageInterval(interval);
        }
        lastActivity = now;
        
        // Update message-specific metrics
        if (activity.getType() == ActivityType.MESSAGE) {
            totalMessages.incrementAndGet();
            
            if (activity.getContent() != null) {
                updateAverageMessageLength(activity.getContent().length());
            }
            
            // Update toxicity and spam scores
            if (activity.isToxic()) {
                updateToxicityScore(activity.getToxicityLevel());
            }
            
            if (activity.isSpam()) {
                updateSpamScore(activity.getSpamLevel());
            }
        }
        
        // Update violation tracking
        if (activity.isViolation()) {
            totalViolations.incrementAndGet();
            consecutiveViolations.incrementAndGet();
        } else {
            consecutiveViolations.set(0);
        }
        
        // Update trust and reputation
        updateTrustScore(activity);
        updateReputationScore(activity);
    }
    
    /**
     * Update metrics with detected pattern
     */
    public void updateWithPattern(BehaviorPattern pattern) {
        patternCounts.get(pattern.getPatternType()).incrementAndGet();
        
        // Adjust trust score based on pattern severity
        double impact = pattern.getRiskWeight() * 0.1;
        trustScore = Math.max(0.0, trustScore - impact);
    }
    
    private void updateAverageMessageLength(int messageLength) {
        long messageCount = totalMessages.get();
        if (messageCount == 1) {
            averageMessageLength = messageLength;
        } else {
            averageMessageLength = ((averageMessageLength * (messageCount - 1)) + messageLength) / messageCount;
        }
    }
    
    private void updateMessageInterval(double interval) {
        long messageCount = totalMessages.get();
        if (messageCount == 1) {
            averageMessageInterval = interval;
            messageIntervalVariance = 0.0;
        } else {
            double oldAverage = averageMessageInterval;
            averageMessageInterval = ((averageMessageInterval * (messageCount - 1)) + interval) / messageCount;
            
            // Update variance (simplified calculation)
            double variance = Math.pow(interval - oldAverage, 2);
            messageIntervalVariance = ((messageIntervalVariance * (messageCount - 1)) + variance) / messageCount;
        }
    }
    
    private void updateToxicityScore(double toxicityLevel) {
        // Exponential moving average with decay factor
        double decayFactor = 0.1;
        toxicityScore = (1 - decayFactor) * toxicityScore + decayFactor * toxicityLevel;
    }
    
    private void updateSpamScore(double spamLevel) {
        // Exponential moving average with decay factor
        double decayFactor = 0.1;
        spamScore = (1 - decayFactor) * spamScore + decayFactor * spamLevel;
    }
    
    private void updateTrustScore(UserActivity activity) {
        if (activity.isViolation()) {
            // Decrease trust for violations
            double decrease = 0.05 * activity.getSeverity();
            trustScore = Math.max(0.0, trustScore - decrease);
        } else {
            // Slowly increase trust for good behavior
            double increase = 0.001;
            trustScore = Math.min(1.0, trustScore + increase);
        }
    }
    
    private void updateReputationScore(UserActivity activity) {
        if (activity.isViolation()) {
            // Decrease reputation for violations
            double decrease = 0.02 * activity.getSeverity();
            reputationScore = Math.max(0.0, reputationScore - decrease);
        } else if (activity.getType() == ActivityType.MESSAGE && !activity.isSpam() && !activity.isToxic()) {
            // Increase reputation for good messages
            double increase = 0.0005;
            reputationScore = Math.min(1.0, reputationScore + increase);
        }
    }
    
    // Getters
    public long getTotalMessages() { return totalMessages.get(); }
    public long getTotalViolations() { return totalViolations.get(); }
    public int getConsecutiveViolations() { return consecutiveViolations.get(); }
    public double getAverageMessageLength() { return averageMessageLength; }
    public double getToxicityScore() { return toxicityScore; }
    public double getSpamScore() { return spamScore; }
    public Instant getLastActivity() { return lastActivity; }
    public Instant getFirstActivity() { return firstActivity; }
    public double getAverageMessageInterval() { return averageMessageInterval; }
    public double getMessageIntervalVariance() { return messageIntervalVariance; }
    public double getTrustScore() { return trustScore; }
    public double getReputationScore() { return reputationScore; }
    
    public long getActivityCount(ActivityType type) {
        return activityCounts.get(type).get();
    }
    
    public long getPatternCount(PatternType type) {
        return patternCounts.get(type).get();
    }
    
    public double getViolationRate() {
        long total = activityCounts.values().stream().mapToLong(AtomicLong::get).sum();
        return total == 0 ? 0.0 : (double) totalViolations.get() / total;
    }
    
    public double getActivityRate() {
        if (firstActivity.equals(lastActivity)) {
            return 0.0;
        }
        
        long totalActivities = activityCounts.values().stream().mapToLong(AtomicLong::get).sum();
        double hoursActive = Duration.between(firstActivity, lastActivity).toMinutes() / 60.0;
        
        return hoursActive == 0 ? 0.0 : totalActivities / hoursActive;
    }
    
    /**
     * Increment total message count
     */
    public void incrementTotalMessages() {
        totalMessages.incrementAndGet();
    }
    
    /**
     * Increment activity count by activity name
     */
    public void incrementActivityCount(String activityName) {
        // This is a simplified implementation that maps string names to ActivityType
        // In a real implementation, you might want a more sophisticated mapping
        switch (activityName.toLowerCase()) {
            case "reactions":
                activityCounts.get(ActivityType.REACTION).incrementAndGet();
                break;
            case "voice_joins":
                activityCounts.get(ActivityType.VOICE_JOIN).incrementAndGet();
                break;
            case "joins":
                activityCounts.get(ActivityType.JOIN).incrementAndGet();
                break;
            default:
                // For unknown activity types, we could log a warning or handle differently
                break;
        }
    }
    
    /**
     * Increment violation count
     */
    public void incrementViolations() {
        totalViolations.incrementAndGet();
        consecutiveViolations.incrementAndGet();
    }
    
    /**
     * Increment pattern count by pattern name
     */
    public void incrementPatternCount(String patternName) {
        // Map string pattern names to PatternType enum values
        try {
            PatternType patternType = PatternType.valueOf(patternName.toUpperCase());
            patternCounts.get(patternType).incrementAndGet();
        } catch (IllegalArgumentException e) {
            // Handle unknown pattern types - could log a warning
            // For now, we'll silently ignore unknown pattern types
        }
    }
    
    /**
     * Set trust score
     */
    public void setTrustScore(double score) {
        this.trustScore = Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Set reputation score
     */
    public void setReputationScore(double score) {
        this.reputationScore = Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Calculate overall risk score based on all metrics
     */
    public double calculateRiskScore() {
        double riskScore = 0.0;
        
        // Violation rate factor (0-0.3)
        riskScore += Math.min(0.3, getViolationRate() * 0.3);
        
        // Toxicity factor (0-0.25)
        riskScore += Math.min(0.25, toxicityScore * 0.25);
        
        // Spam factor (0-0.2)
        riskScore += Math.min(0.2, spamScore * 0.2);
        
        // Trust factor (inverse, 0-0.15)
        riskScore += (1.0 - trustScore) * 0.15;
        
        // Consecutive violations factor (0-0.1)
        riskScore += Math.min(0.1, consecutiveViolations.get() * 0.02);
        
        return Math.min(1.0, riskScore);
    }
}