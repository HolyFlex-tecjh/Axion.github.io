package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a detected coordinated behavior pattern
 */
public class CoordinatedPattern {
    private final CoordinatedPatternType type;
    private final List<String> involvedUsers;
    private final double strength;
    private final double confidence;
    private final Instant detectedAt;
    private final long timeWindowMs;
    private final Map<String, Object> patternData;
    private final String description;
    private final List<String> evidenceItems;
    private final Map<String, Double> userContributions;
    private final boolean isActive;
    private final Instant lastActivity;
    
    private CoordinatedPattern(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "Pattern type cannot be null");
        this.involvedUsers = Collections.unmodifiableList(new ArrayList<>(builder.involvedUsers));
        this.strength = Math.max(0.0, Math.min(100.0, builder.strength));
        this.confidence = Math.max(0.0, Math.min(100.0, builder.confidence));
        this.detectedAt = builder.detectedAt != null ? builder.detectedAt : Instant.now();
        this.timeWindowMs = builder.timeWindowMs;
        this.patternData = Collections.unmodifiableMap(new HashMap<>(builder.patternData));
        this.description = builder.description != null ? builder.description : "";
        this.evidenceItems = Collections.unmodifiableList(new ArrayList<>(builder.evidenceItems));
        this.userContributions = Collections.unmodifiableMap(new HashMap<>(builder.userContributions));
        this.isActive = builder.isActive;
        this.lastActivity = builder.lastActivity != null ? builder.lastActivity : Instant.now();
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder(CoordinatedPatternType type) {
        return new Builder(type);
    }
    
    /**
     * Create a spam coordination pattern
     */
    public static CoordinatedPattern spamPattern(List<String> users, double strength, String evidence) {
        return builder(CoordinatedPatternType.SPAM)
            .addInvolvedUsers(users)
            .withStrength(strength)
            .withConfidence(strength * 0.8) // Confidence slightly lower than strength
            .withDescription("Coordinated spam activity detected")
            .addEvidence(evidence)
            .build();
    }
    
    /**
     * Create a raid pattern
     */
    public static CoordinatedPattern raidPattern(List<String> users, double strength, long timeWindow) {
        return builder(CoordinatedPatternType.RAID)
            .addInvolvedUsers(users)
            .withStrength(strength)
            .withConfidence(Math.min(95.0, strength * 1.1))
            .withTimeWindow(timeWindow)
            .withDescription(String.format("Potential raid detected with %d users", users.size()))
            .build();
    }
    
    /**
     * Create a toxicity coordination pattern
     */
    public static CoordinatedPattern toxicityPattern(List<String> users, double strength) {
        return builder(CoordinatedPatternType.TOXICITY)
            .addInvolvedUsers(users)
            .withStrength(strength)
            .withConfidence(strength * 0.9)
            .withDescription("Coordinated toxic behavior detected")
            .build();
    }
    
    /**
     * Get the primary contributor
     */
    public String getPrimaryContributor() {
        return userContributions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(involvedUsers.isEmpty() ? null : involvedUsers.get(0));
    }
    
    /**
     * Get users with contribution above threshold
     */
    public List<String> getHighContributors(double threshold) {
        return userContributions.entrySet().stream()
            .filter(entry -> entry.getValue() >= threshold)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if pattern is strong
     */
    public boolean isStrong() {
        return strength >= 70.0 && confidence >= 60.0;
    }
    
    /**
     * Check if pattern is weak
     */
    public boolean isWeak() {
        return strength < 30.0 || confidence < 25.0;
    }
    
    /**
     * Check if pattern requires immediate action
     */
    public boolean requiresImmediateAction() {
        return isStrong() && (type == CoordinatedPatternType.RAID || 
                             type == CoordinatedPatternType.HARASSMENT ||
                             (type == CoordinatedPatternType.SPAM && strength >= 85.0));
    }
    
    /**
     * Get pattern age in milliseconds
     */
    public long getAgeMs() {
        return Instant.now().toEpochMilli() - detectedAt.toEpochMilli();
    }
    
    /**
     * Check if pattern is recent
     */
    public boolean isRecent(long thresholdMs) {
        return getAgeMs() <= thresholdMs;
    }
    
    /**
     * Get pattern severity level
     */
    public SeverityLevel getSeverityLevel() {
        if (strength >= 80.0 && confidence >= 70.0) {
            return SeverityLevel.CRITICAL;
        } else if (strength >= 60.0 && confidence >= 50.0) {
            return SeverityLevel.HIGH;
        } else if (strength >= 40.0 && confidence >= 30.0) {
            return SeverityLevel.MEDIUM;
        } else {
            return SeverityLevel.LOW;
        }
    }
    
    /**
     * Get pattern summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%s Pattern:\n", type.name()));
        summary.append(String.format("- Strength: %.1f%% (Confidence: %.1f%%)\n", strength, confidence));
        summary.append(String.format("- Severity: %s\n", getSeverityLevel()));
        summary.append(String.format("- Involved Users: %d\n", involvedUsers.size()));
        summary.append(String.format("- Detected: %s\n", detectedAt));
        
        if (!description.isEmpty()) {
            summary.append(String.format("- Description: %s\n", description));
        }
        
        if (!evidenceItems.isEmpty()) {
            summary.append(String.format("- Evidence Items: %d\n", evidenceItems.size()));
        }
        
        return summary.toString();
    }
    
    /**
     * Get detailed analysis
     */
    public Map<String, Object> getDetailedAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("type", type.name());
        analysis.put("strength", strength);
        analysis.put("confidence", confidence);
        analysis.put("severityLevel", getSeverityLevel().name());
        analysis.put("involvedUsers", involvedUsers);
        analysis.put("userCount", involvedUsers.size());
        analysis.put("detectedAt", detectedAt);
        analysis.put("ageMs", getAgeMs());
        analysis.put("timeWindowMs", timeWindowMs);
        analysis.put("isActive", isActive);
        analysis.put("lastActivity", lastActivity);
        analysis.put("isStrong", isStrong());
        analysis.put("requiresImmediateAction", requiresImmediateAction());
        analysis.put("primaryContributor", getPrimaryContributor());
        analysis.put("userContributions", userContributions);
        analysis.put("evidenceCount", evidenceItems.size());
        analysis.put("patternData", patternData);
        return analysis;
    }
    
    // Getters
    public CoordinatedPatternType getType() { return type; }
    public List<String> getInvolvedUsers() { return involvedUsers; }
    public double getStrength() { return strength; }
    public double getConfidence() { return confidence; }
    public Instant getDetectedAt() { return detectedAt; }
    public long getTimeWindowMs() { return timeWindowMs; }
    public Map<String, Object> getPatternData() { return patternData; }
    public String getDescription() { return description; }
    public List<String> getEvidenceItems() { return evidenceItems; }
    public Map<String, Double> getUserContributions() { return userContributions; }
    public boolean isActive() { return isActive; }
    public Instant getLastActivity() { return lastActivity; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CoordinatedPattern that = (CoordinatedPattern) obj;
        return Double.compare(that.strength, strength) == 0 &&
               Double.compare(that.confidence, confidence) == 0 &&
               timeWindowMs == that.timeWindowMs &&
               isActive == that.isActive &&
               type == that.type &&
               Objects.equals(involvedUsers, that.involvedUsers) &&
               Objects.equals(detectedAt, that.detectedAt) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, involvedUsers, strength, confidence, detectedAt, 
                          timeWindowMs, description, isActive);
    }
    
    @Override
    public String toString() {
        return String.format("CoordinatedPattern{type=%s, users=%d, strength=%.1f, confidence=%.1f, severity=%s}",
            type, involvedUsers.size(), strength, confidence, getSeverityLevel());
    }
    
    /**
     * Severity levels for coordinated patterns
     */
    public enum SeverityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Builder for CoordinatedPattern
     */
    public static class Builder {
        private final CoordinatedPatternType type;
        private final List<String> involvedUsers = new ArrayList<>();
        private double strength = 0.0;
        private double confidence = 0.0;
        private Instant detectedAt;
        private long timeWindowMs = 0;
        private final Map<String, Object> patternData = new HashMap<>();
        private String description;
        private final List<String> evidenceItems = new ArrayList<>();
        private final Map<String, Double> userContributions = new HashMap<>();
        private boolean isActive = true;
        private Instant lastActivity;
        
        private Builder(CoordinatedPatternType type) {
            this.type = type;
        }
        
        public Builder addInvolvedUser(String userId) {
            this.involvedUsers.add(userId);
            return this;
        }
        
        public Builder addInvolvedUsers(Collection<String> userIds) {
            this.involvedUsers.addAll(userIds);
            return this;
        }
        
        public Builder withStrength(double strength) {
            this.strength = strength;
            return this;
        }
        
        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public Builder withDetectedAt(Instant detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }
        
        public Builder withTimeWindow(long timeWindowMs) {
            this.timeWindowMs = timeWindowMs;
            return this;
        }
        
        public Builder addPatternData(String key, Object value) {
            this.patternData.put(key, value);
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder addEvidence(String evidence) {
            this.evidenceItems.add(evidence);
            return this;
        }
        
        public Builder addUserContribution(String userId, double contribution) {
            this.userContributions.put(userId, contribution);
            return this;
        }
        
        public Builder withIsActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder withLastActivity(Instant lastActivity) {
            this.lastActivity = lastActivity;
            return this;
        }
        
        public CoordinatedPattern build() {
            return new CoordinatedPattern(this);
        }
    }
}