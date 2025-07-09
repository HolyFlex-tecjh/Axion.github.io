package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive user moderation profile
 * Tracks user behavior, violations, and moderation history
 * Used by the advanced moderation system for intelligent decision making
 */
public class UserModerationProfile {
    private final String userId;
    private final String guildId;
    private final Instant createdAt;
    
    // Violation tracking
    private int totalViolations = 0;
    private int warningCount = 0;
    private int timeoutCount = 0;
    private int kickCount = 0;
    private int banCount = 0;
    
    // Behavior scoring
    private int trustScore = 100; // Starts at 100, decreases with violations
    private int suspicionPoints = 0;
    private double riskLevel = 0.0;
    
    // Activity tracking
    private int messageCount = 0;
    private Instant lastActivity;
    private Instant firstSeen;
    private Duration totalActiveTime = Duration.ZERO;
    
    // Violation history
    private final List<ViolationRecord> violationHistory = new ArrayList<>();
    private final Map<ModerationAction, Integer> actionCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> violationTypes = new ConcurrentHashMap<>();
    private final Map<String, List<Instant>> ruleViolations = new ConcurrentHashMap<>();
    
    // Temporary states
    private boolean isCurrentlyTimedOut = false;
    private Instant timeoutExpiry;
    private boolean isUnderInvestigation = false;
    private String investigationReason;
    
    // Behavioral patterns
    private final List<Instant> recentMessages = new ArrayList<>();
    private final Set<String> channelsUsed = new HashSet<>();
    private final Map<String, Integer> contentPatterns = new HashMap<>();
    
    public UserModerationProfile(String userId, String guildId) {
        this.userId = userId;
        this.guildId = guildId;
        this.createdAt = Instant.now();
        this.firstSeen = Instant.now();
        this.lastActivity = Instant.now();
    }
    
    /**
     * Record a new violation
     */
    public void recordViolation(ModerationAction action, String reason, ModerationSeverity severity, boolean automated) {
        totalViolations++;
        
        // Update action counts
        actionCounts.merge(action, 1, Integer::sum);
        
        // Update specific violation counts
        switch (action) {
            case WARN_USER:
            case DELETE_AND_WARN:
                warningCount++;
                break;
            case TIMEOUT:
            case DELETE_AND_TIMEOUT:
                timeoutCount++;
                isCurrentlyTimedOut = true;
                timeoutExpiry = Instant.now().plus(Duration.ofMinutes(10)); // Default timeout
                break;
            case KICK:
                kickCount++;
                break;
            case BAN:
                banCount++;
                break;
            case SYSTEM_ACTION:
            case DELETE_MESSAGE:
            case LOG_ONLY:
            case NONE:
                // No specific count tracking needed for these actions
                break;
        }
        
        // Create violation record
        ViolationRecord record = new ViolationRecord(
            action, reason, severity, automated, Instant.now()
        );
        violationHistory.add(record);
        
        // Update trust score
        updateTrustScore(severity, automated);
        
        // Update risk level
        calculateRiskLevel();
        
        // Clean old violation history (keep last 100)
        if (violationHistory.size() > 100) {
            violationHistory.subList(0, violationHistory.size() - 100).clear();
        }
    }
    
    /**
     * Record user activity
     */
    public void recordActivity(String channelId, String messageContent) {
        messageCount++;
        lastActivity = Instant.now();
        
        // Track channels used
        channelsUsed.add(channelId);
        
        // Track recent messages for spam detection
        recentMessages.add(Instant.now());
        
        // Clean old message timestamps (keep last hour)
        Instant oneHourAgo = Instant.now().minus(Duration.ofHours(1));
        recentMessages.removeIf(timestamp -> timestamp.isBefore(oneHourAgo));
        
        // Analyze content patterns
        if (messageContent != null && !messageContent.trim().isEmpty()) {
            analyzeContentPattern(messageContent);
        }
        
        // Update total active time
        if (firstSeen != null) {
            totalActiveTime = Duration.between(firstSeen, Instant.now());
        }
    }
    
    /**
     * Record a rule violation
     */
    public void recordRuleViolation(String ruleId) {
        ruleViolations.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(Instant.now());
        
        // Clean old violations (keep last 30 days)
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        ruleViolations.get(ruleId).removeIf(timestamp -> timestamp.isBefore(thirtyDaysAgo));
    }
    
    /**
     * Get rule violation count within timeframe
     */
    public int getRuleViolationCount(String ruleId, Duration timeframe) {
        List<Instant> violations = ruleViolations.get(ruleId);
        if (violations == null) return 0;
        
        Instant cutoff = Instant.now().minus(timeframe);
        return (int) violations.stream()
            .filter(timestamp -> timestamp.isAfter(cutoff))
            .count();
    }
    
    /**
     * Add suspicion points
     */
    public void addSuspicionPoints(int points) {
        suspicionPoints += points;
        calculateRiskLevel();
    }
    
    /**
     * Remove suspicion points (for good behavior)
     */
    public void removeSuspicionPoints(int points) {
        suspicionPoints = Math.max(0, suspicionPoints - points);
        calculateRiskLevel();
    }
    
    /**
     * Update trust score based on violation
     */
    private void updateTrustScore(ModerationSeverity severity, boolean automated) {
        int penalty;
        switch (severity) {
            case LOW:
                penalty = automated ? 2 : 5;
                break;
            case MEDIUM:
                penalty = automated ? 5 : 10;
                break;
            case HIGH:
                penalty = automated ? 10 : 20;
                break;
            case VERY_HIGH:
                penalty = automated ? 20 : 30;
                break;
            default:
                penalty = 1;
                break;
        }
        
        trustScore = Math.max(0, trustScore - penalty);
    }
    
    /**
     * Calculate risk level based on various factors
     */
    private void calculateRiskLevel() {
        double risk = 0.0;
        
        // Base risk from trust score
        risk += (100 - trustScore) / 100.0 * 0.4;
        
        // Risk from suspicion points
        risk += Math.min(suspicionPoints / 20.0, 1.0) * 0.3;
        
        // Risk from recent violations
        long recentViolations = violationHistory.stream()
            .filter(v -> Duration.between(v.getTimestamp(), Instant.now()).toDays() <= 7)
            .count();
        risk += Math.min(recentViolations / 5.0, 1.0) * 0.2;
        
        // Risk from violation frequency
        if (totalViolations > 0 && totalActiveTime.toDays() > 0) {
            double violationRate = (double) totalViolations / totalActiveTime.toDays();
            risk += Math.min(violationRate, 1.0) * 0.1;
        }
        
        riskLevel = Math.min(risk, 1.0);
    }
    
    /**
     * Analyze content patterns for behavioral insights
     */
    private void analyzeContentPattern(String content) {
        // Simple pattern analysis
        String normalized = content.toLowerCase().trim();
        
        if (normalized.length() < 5) {
            contentPatterns.merge("short_messages", 1, Integer::sum);
        }
        
        if (normalized.matches(".*[!?]{3,}.*")) {
            contentPatterns.merge("excessive_punctuation", 1, Integer::sum);
        }
        
        if (normalized.matches(".*[A-Z]{5,}.*")) {
            contentPatterns.merge("excessive_caps", 1, Integer::sum);
        }
        
        if (normalized.matches(".*https?://.*")) {
            contentPatterns.merge("contains_links", 1, Integer::sum);
        }
    }
    
    /**
     * Check if user is currently timed out
     */
    public boolean isCurrentlyTimedOut() {
        if (isCurrentlyTimedOut && timeoutExpiry != null) {
            if (Instant.now().isAfter(timeoutExpiry)) {
                isCurrentlyTimedOut = false;
                timeoutExpiry = null;
            }
        }
        return isCurrentlyTimedOut;
    }
    
    /**
     * Set timeout status
     */
    public void setTimeoutStatus(boolean timedOut, Duration duration) {
        this.isCurrentlyTimedOut = timedOut;
        if (timedOut && duration != null) {
            this.timeoutExpiry = Instant.now().plus(duration);
        } else {
            this.timeoutExpiry = null;
        }
    }
    
    /**
     * Check if user should be considered high risk
     */
    public boolean isHighRisk() {
        return riskLevel >= 0.7 || suspicionPoints >= 15 || trustScore <= 20;
    }
    
    /**
     * Check if user should be considered low risk
     */
    public boolean isLowRisk() {
        return riskLevel <= 0.2 && suspicionPoints <= 3 && trustScore >= 80 && totalViolations <= 1;
    }
    
    /**
     * Get recent violation count (last 7 days)
     */
    public long getRecentViolationCount() {
        Instant weekAgo = Instant.now().minus(Duration.ofDays(7));
        return violationHistory.stream()
            .filter(v -> v.getTimestamp().isAfter(weekAgo))
            .count();
    }
    
    /**
     * Get violations by type
     */
    public Map<String, Integer> getViolationsByType() {
        return new HashMap<>(violationTypes);
    }
    
    /**
     * Get recent message rate (messages per hour)
     */
    public double getRecentMessageRate() {
        if (recentMessages.isEmpty()) return 0.0;
        
        Instant oldestMessage = recentMessages.get(0);
        Duration timeSpan = Duration.between(oldestMessage, Instant.now());
        
        if (timeSpan.toMinutes() == 0) return recentMessages.size();
        
        return (double) recentMessages.size() / (timeSpan.toMinutes() / 60.0);
    }
    
    /**
     * Reset suspicion points (for good behavior over time)
     */
    public void resetSuspicionPoints() {
        suspicionPoints = 0;
        calculateRiskLevel();
    }
    
    /**
     * Improve trust score (for consistent good behavior)
     */
    public void improveTrustScore(int points) {
        trustScore = Math.min(100, trustScore + points);
        calculateRiskLevel();
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public Instant getCreatedAt() { return createdAt; }
    public int getTotalViolations() { return totalViolations; }
    public int getWarningCount() { return warningCount; }
    public int getTimeoutCount() { return timeoutCount; }
    public int getKickCount() { return kickCount; }
    public int getBanCount() { return banCount; }
    public int getTrustScore() { return trustScore; }
    public int getSuspicionPoints() { return suspicionPoints; }
    public double getRiskLevel() { return riskLevel; }
    public int getMessageCount() { return messageCount; }
    public Instant getLastActivity() { return lastActivity; }
    public Instant getFirstSeen() { return firstSeen; }
    public Duration getTotalActiveTime() { return totalActiveTime; }
    public List<ViolationRecord> getViolationHistory() { return new ArrayList<>(violationHistory); }
    public Map<ModerationAction, Integer> getActionCounts() { return new HashMap<>(actionCounts); }
    public boolean isUnderInvestigation() { return isUnderInvestigation; }
    public String getInvestigationReason() { return investigationReason; }
    public Set<String> getChannelsUsed() { return new HashSet<>(channelsUsed); }
    public Map<String, Integer> getContentPatterns() { return new HashMap<>(contentPatterns); }
    public Instant getTimeoutExpiry() { return timeoutExpiry; }
    
    // Setters for investigation
    public void setUnderInvestigation(boolean underInvestigation, String reason) {
        this.isUnderInvestigation = underInvestigation;
        this.investigationReason = reason;
    }
    
    /**
     * Get trust level based on trust score
     */
    public TrustLevel getTrustLevel() {
        if (trustScore >= 80) return TrustLevel.HIGH;
        if (trustScore >= 60) return TrustLevel.MEDIUM;
        if (trustScore >= 40) return TrustLevel.LOW;
        return TrustLevel.VERY_LOW;
    }
    
    /**
     * Check if user is currently punished (timed out, banned, etc.)
     */
    public boolean isCurrentlyPunished() {
        return isCurrentlyTimedOut();
    }
    
    /**
     * Increment good behavior counter
     */
    public void incrementGoodBehavior() {
        removeSuspicionPoints(1);
        if (trustScore < 100) {
            improveTrustScore(1);
        }
    }
    
    /**
     * Record a join event
     */
    public void recordJoinEvent(Instant joinTime) {
        if (firstSeen == null || joinTime.isBefore(firstSeen)) {
            firstSeen = joinTime;
        }
        lastActivity = joinTime;
    }
    
    /**
     * Add a punishment record
     */
    public void addPunishment(ModerationAction action, Duration duration, String reason) {
        recordViolation(action, reason, ModerationSeverity.MEDIUM, false);
        if (action == ModerationAction.TIMEOUT && duration != null) {
            setTimeoutStatus(true, duration);
        }
    }
    
    /**
     * Get violation count
     */
    public int getViolationCount() {
        return totalViolations;
    }
    
    /**
     * Check if user has recent duplicate message
     */
    public boolean hasRecentDuplicateMessage(String content, long timeframeMs) {
        if (content == null || content.trim().isEmpty()) return false;
        
        Instant cutoff = Instant.now().minus(Duration.ofMillis(timeframeMs));
        
        // Check if we've seen this exact content recently
        // This is a simplified implementation - in practice you'd store recent message content
        return contentPatterns.getOrDefault("duplicate_" + content.hashCode(), 0) > 1;
    }
    
    /**
     * Get recent message count within timeframe
     */
    public int getRecentMessageCount(long timeframeMs) {
        Instant cutoff = Instant.now().minus(Duration.ofMillis(timeframeMs));
        return (int) recentMessages.stream()
            .filter(timestamp -> timestamp.isAfter(cutoff))
            .count();
    }
    
    /**
     * Add a violation with reason
     */
    public void addViolation(String reason) {
        recordViolation(ModerationAction.WARN_USER, reason, ModerationSeverity.LOW, true);
    }
    
    /**
     * Update trust level (called by scheduled tasks)
     */
    public void updateTrustLevel() {
        // Gradually improve trust over time if no recent violations
        long daysSinceLastViolation = violationHistory.isEmpty() ? 30 : 
            Duration.between(violationHistory.get(violationHistory.size() - 1).getTimestamp(), Instant.now()).toDays();
        
        if (daysSinceLastViolation >= 7) {
            improveTrustScore(1);
        }
        if (daysSinceLastViolation >= 30) {
            resetSuspicionPoints();
        }
    }
    
    /**
     * Get a summary of the user's moderation status
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("User: %s | Trust: %d | Risk: %.2f | Violations: %d\n", 
            userId, trustScore, riskLevel, totalViolations));
        summary.append(String.format("Warnings: %d | Timeouts: %d | Kicks: %d | Bans: %d\n", 
            warningCount, timeoutCount, kickCount, banCount));
        summary.append(String.format("Messages: %d | Active Time: %d days | Recent Rate: %.1f msg/hr", 
            messageCount, totalActiveTime.toDays(), getRecentMessageRate()));
        
        return summary.toString();
    }
    
    /**
     * Violation record inner class
     */
    /**
     * Trust level enum
     */
    public enum TrustLevel {
        VERY_LOW, LOW, MEDIUM, HIGH
    }
    
    /**
     * Violation record inner class
     */
    public static class ViolationRecord {
        private final ModerationAction action;
        private final String reason;
        private final ModerationSeverity severity;
        private final boolean automated;
        private final Instant timestamp;
        
        public ViolationRecord(ModerationAction action, String reason, ModerationSeverity severity, 
                             boolean automated, Instant timestamp) {
            this.action = action;
            this.reason = reason;
            this.severity = severity;
            this.automated = automated;
            this.timestamp = timestamp;
        }
        
        // Getters
        public ModerationAction getAction() { return action; }
        public String getReason() { return reason; }
        public ModerationSeverity getSeverity() { return severity; }
        public boolean isAutomated() { return automated; }
        public Instant getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("%s: %s (%s) - %s", 
                action, reason, severity, automated ? "Auto" : "Manual");
        }
    }
}