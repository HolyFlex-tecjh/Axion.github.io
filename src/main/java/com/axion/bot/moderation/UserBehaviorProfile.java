package com.axion.bot.moderation;

import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Comprehensive user behavior profile for tracking patterns and metrics
 */
public class UserBehaviorProfile {
    private final String userId;
    private final String guildId;
    private final UserBehaviorMetrics metrics;
    private final Map<PatternType, Integer> patternCounts;
    private final Map<ActivityType, List<Instant>> activityHistory;
    private final Set<String> flaggedContent;
    private final Map<String, Object> customAttributes;
    private Instant lastUpdated;
    private Instant createdAt;
    private double trustScore;
    private double reputationScore;
    private RiskLevel currentRiskLevel;
    private boolean isMonitored;
    private String notes;
    
    public UserBehaviorProfile(String userId, String guildId) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.guildId = Objects.requireNonNull(guildId, "Guild ID cannot be null");
        this.metrics = new UserBehaviorMetrics();
        this.patternCounts = new ConcurrentHashMap<>();
        this.activityHistory = new ConcurrentHashMap<>();
        this.flaggedContent = ConcurrentHashMap.newKeySet();
        this.customAttributes = new ConcurrentHashMap<>();
        this.lastUpdated = Instant.now();
        this.createdAt = Instant.now();
        this.trustScore = 50.0; // Start with neutral trust
        this.reputationScore = 50.0; // Start with neutral reputation
        this.currentRiskLevel = RiskLevel.LOW;
        this.isMonitored = false;
        this.notes = "";
        
        // Initialize pattern counts
        for (PatternType type : PatternType.values()) {
            patternCounts.put(type, 0);
        }
        
        // Initialize activity history
        for (ActivityType type : ActivityType.values()) {
            activityHistory.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Update the profile with a new activity
     */
    public synchronized void recordActivity(ActivityType activityType, Instant timestamp) {
        activityHistory.computeIfAbsent(activityType, k -> new ArrayList<>()).add(timestamp);
        lastUpdated = Instant.now();
        
        // Update metrics based on activity type
        switch (activityType) {
            case MESSAGE:
                metrics.incrementTotalMessages();
                break;
            case REACTION:
                metrics.incrementActivityCount("reactions");
                break;
            case VOICE_JOIN:
                metrics.incrementActivityCount("voice_joins");
                break;
            case JOIN:
                metrics.incrementActivityCount("joins");
                break;
            case EDIT:
                // No specific metric for EDIT
                break;
            case DELETE:
                // No specific metric for DELETE
                break;
            case VOICE_LEAVE:
                // No specific metric for VOICE_LEAVE
                break;
            case LEAVE:
                // No specific metric for LEAVE
                break;
        }
        
        // Clean old activity history (keep last 30 days)
        cleanOldActivities();
    }
    
    /**
     * Record a detected pattern
     */
    public synchronized void recordPattern(BehaviorPattern pattern) {
        PatternType type = pattern.getPatternType();
        patternCounts.merge(type, 1, Integer::sum);
        metrics.incrementPatternCount(type.name().toLowerCase());
        
        // Update risk assessment based on pattern
        updateRiskLevel(pattern);
        lastUpdated = Instant.now();
    }
    
    /**
     * Record a violation
     */
    public synchronized void recordViolation(String violationType, String content) {
        metrics.incrementViolations();
        if (content != null && !content.trim().isEmpty()) {
            flaggedContent.add(content.trim());
        }
        
        // Adjust trust and reputation scores
        adjustTrustScore(-5.0);
        adjustReputationScore(-3.0);
        
        lastUpdated = Instant.now();
    }
    
    /**
     * Update trust score
     */
    public synchronized void adjustTrustScore(double adjustment) {
        trustScore = Math.max(0.0, Math.min(100.0, trustScore + adjustment));
        metrics.setTrustScore(trustScore);
        lastUpdated = Instant.now();
    }
    
    /**
     * Update reputation score
     */
    public synchronized void adjustReputationScore(double adjustment) {
        reputationScore = Math.max(0.0, Math.min(100.0, reputationScore + adjustment));
        metrics.setReputationScore(reputationScore);
        lastUpdated = Instant.now();
    }
    
    /**
     * Update risk level based on detected pattern
     */
    private void updateRiskLevel(BehaviorPattern pattern) {
        if (pattern.isHighRisk()) {
            if (currentRiskLevel.ordinal() < RiskLevel.HIGH.ordinal()) {
                currentRiskLevel = RiskLevel.HIGH;
            }
        } else if (pattern.getSeverityLevel() == BehaviorPattern.SeverityLevel.MEDIUM) {
            if (currentRiskLevel.ordinal() < RiskLevel.MEDIUM.ordinal()) {
                currentRiskLevel = RiskLevel.MEDIUM;
            }
        }
    }
    
    /**
     * Calculate activity frequency for a specific type
     */
    public double getActivityFrequency(ActivityType activityType, long timeWindowMs) {
        List<Instant> activities = activityHistory.get(activityType);
        if (activities == null || activities.isEmpty()) {
            return 0.0;
        }
        
        Instant cutoff = Instant.now().minusMillis(timeWindowMs);
        long recentActivities = activities.stream()
            .mapToLong(instant -> instant.isAfter(cutoff) ? 1 : 0)
            .sum();
        
        return (double) recentActivities / (timeWindowMs / 1000.0); // Activities per second
    }
    
    /**
     * Get recent activities within time window
     */
    public List<Instant> getRecentActivities(ActivityType activityType, long timeWindowMs) {
        List<Instant> activities = activityHistory.get(activityType);
        if (activities == null) {
            return Collections.emptyList();
        }
        
        Instant cutoff = Instant.now().minusMillis(timeWindowMs);
        return activities.stream()
            .filter(instant -> instant.isAfter(cutoff))
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent activities within duration window (all activity types)
     */
    public List<UserActivity> getRecentActivities(Duration duration) {
        List<UserActivity> recentActivities = new ArrayList<>();
        Instant cutoff = Instant.now().minus(duration);
        
        for (Map.Entry<ActivityType, List<Instant>> entry : activityHistory.entrySet()) {
            ActivityType type = entry.getKey();
            List<Instant> activities = entry.getValue();
            
            for (Instant timestamp : activities) {
                if (timestamp.isAfter(cutoff)) {
                    // Create a UserActivity object for each recent activity
                    UserActivity activity = new UserActivity(userId, "", timestamp, type);
                    recentActivities.add(activity);
                }
            }
        }
        
        // Sort by timestamp
        recentActivities.sort(Comparator.comparing(UserActivity::getTimestamp));
        return recentActivities;
    }
    
    /**
     * Check if user is exhibiting suspicious behavior
     */
    public boolean isSuspicious() {
        return currentRiskLevel.ordinal() >= RiskLevel.MEDIUM.ordinal() ||
               trustScore < 30.0 ||
               reputationScore < 25.0 ||
               getTotalPatternCount() > 10;
    }
    
    /**
     * Check if user requires monitoring
     */
    public boolean requiresMonitoring() {
        return isMonitored ||
               currentRiskLevel.ordinal() >= RiskLevel.HIGH.ordinal() ||
               trustScore < 20.0 ||
               getTotalPatternCount() > 15;
    }
    
    /**
     * Check if user is high risk
     */
    public boolean isHighRisk() {
        return currentRiskLevel.ordinal() >= RiskLevel.HIGH.ordinal() ||
               trustScore < 25.0 ||
               reputationScore < 20.0 ||
               getTotalPatternCount() > 12;
    }
    
    /**
     * Get total pattern count across all types
     */
    public int getTotalPatternCount() {
        return patternCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Get pattern count for specific type
     */
    public int getPatternCount(PatternType type) {
        return patternCounts.getOrDefault(type, 0);
    }
    
    /**
     * Clean old activities (keep last 30 days)
     */
    private void cleanOldActivities() {
        Instant cutoff = Instant.now().minusSeconds(30 * 24 * 60 * 60); // 30 days
        
        for (List<Instant> activities : activityHistory.values()) {
            activities.removeIf(instant -> instant.isBefore(cutoff));
        }
        
        // Limit flagged content size
        if (flaggedContent.size() > 100) {
            Iterator<String> iterator = flaggedContent.iterator();
            for (int i = 0; i < 20 && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
        }
    }
    
    /**
     * Set custom attribute
     */
    public void setAttribute(String key, Object value) {
        customAttributes.put(key, value);
        lastUpdated = Instant.now();
    }
    
    /**
     * Get custom attribute
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = customAttributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Add activity to the profile
     */
    public void addActivity(ActivityType activityType) {
        recordActivity(activityType, Instant.now());
    }
    
    /**
     * Get account creation time
     */
    public Instant getAccountCreated() {
        return createdAt;
    }
    
    /**
     * Get activity rate (activities per hour)
     */
    public double getActivityRate() {
        long totalActivities = activityHistory.values().stream()
            .mapToLong(List::size)
            .sum();
        
        long hoursActive = java.time.Duration.between(createdAt, Instant.now()).toHours();
        if (hoursActive == 0) hoursActive = 1; // Avoid division by zero
        
        return (double) totalActivities / hoursActive;
    }
    
    /**
     * Get violation history
     */
    public List<String> getViolationHistory() {
        return new ArrayList<>(flaggedContent);
    }

    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public UserBehaviorMetrics getMetrics() { return metrics; }
    public Map<PatternType, Integer> getPatternCounts() { return Collections.unmodifiableMap(patternCounts); }
    public Map<ActivityType, List<Instant>> getActivityHistory() { return Collections.unmodifiableMap(activityHistory); }
    public Set<String> getFlaggedContent() { return Collections.unmodifiableSet(flaggedContent); }
    public Map<String, Object> getCustomAttributes() { return Collections.unmodifiableMap(customAttributes); }
    public Instant getLastUpdated() { return lastUpdated; }
    public Instant getCreatedAt() { return createdAt; }
    public double getTrustScore() { return trustScore; }
    public double getReputationScore() { return reputationScore; }
    public RiskLevel getCurrentRiskLevel() { return currentRiskLevel; }
    public boolean isMonitored() { return isMonitored; }
    public String getNotes() { return notes; }
    
    // Setters
    public void setMonitored(boolean monitored) {
        this.isMonitored = monitored;
        this.lastUpdated = Instant.now();
    }
    
    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
        this.lastUpdated = Instant.now();
    }
    
    public void setCurrentRiskLevel(RiskLevel riskLevel) {
        this.currentRiskLevel = Objects.requireNonNull(riskLevel);
        this.lastUpdated = Instant.now();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserBehaviorProfile that = (UserBehaviorProfile) obj;
        return Objects.equals(userId, that.userId) && Objects.equals(guildId, that.guildId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, guildId);
    }
    
    @Override
    public String toString() {
        return String.format("UserBehaviorProfile{userId='%s', guildId='%s', riskLevel=%s, trustScore=%.1f, patterns=%d}",
            userId, guildId, currentRiskLevel, trustScore, getTotalPatternCount());
    }
}