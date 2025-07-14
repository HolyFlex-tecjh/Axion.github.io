package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Result of coordinated behavior analysis
 */
public class CoordinatedBehaviorResult {
    private final String guildId;
    private final List<String> involvedUsers;
    private final List<CoordinatedPattern> detectedPatterns;
    private final CoordinatedPatternType primaryPatternType;
    private final double coordinationScore;
    private final ThreatLevel threatLevel;
    private final Instant detectionTime;
    private final long analysisTimeMs;
    private final Map<String, Object> evidence;
    private final String description;
    private final boolean requiresAction;
    private final List<String> recommendedActions;
    private final Map<String, Double> userInvolvementScores;
    
    private CoordinatedBehaviorResult(Builder builder) {
        this.guildId = Objects.requireNonNull(builder.guildId, "Guild ID cannot be null");
        this.involvedUsers = Collections.unmodifiableList(new ArrayList<>(builder.involvedUsers));
        this.detectedPatterns = Collections.unmodifiableList(new ArrayList<>(builder.detectedPatterns));
        this.primaryPatternType = builder.primaryPatternType;
        this.coordinationScore = Math.max(0.0, Math.min(100.0, builder.coordinationScore));
        this.threatLevel = Objects.requireNonNull(builder.threatLevel, "Threat level cannot be null");
        this.detectionTime = builder.detectionTime != null ? builder.detectionTime : Instant.now();
        this.analysisTimeMs = builder.analysisTimeMs;
        this.evidence = Collections.unmodifiableMap(new HashMap<>(builder.evidence));
        this.description = builder.description != null ? builder.description : "";
        this.requiresAction = builder.requiresAction;
        this.recommendedActions = Collections.unmodifiableList(new ArrayList<>(builder.recommendedActions));
        this.userInvolvementScores = Collections.unmodifiableMap(new HashMap<>(builder.userInvolvementScores));
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder(String guildId) {
        return new Builder(guildId);
    }
    
    /**
     * Create a no-coordination result
     */
    public static CoordinatedBehaviorResult noCoordination(String guildId) {
        return builder(guildId)
            .withThreatLevel(ThreatLevel.NONE)
            .withCoordinationScore(0.0)
            .withDescription("No coordinated behavior detected")
            .build();
    }
    
    /**
     * Create an error result
     */
    public static CoordinatedBehaviorResult error(String guildId, String errorMessage) {
        return builder(guildId)
            .withThreatLevel(ThreatLevel.NONE)
            .withCoordinationScore(0.0)
            .withDescription("Error: " + errorMessage)
            .build();
    }
    
    /**
     * Get the most involved user
     */
    public String getMostInvolvedUser() {
        return userInvolvementScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Get users with involvement above threshold
     */
    public List<String> getHighlyInvolvedUsers(double threshold) {
        return userInvolvementScores.entrySet().stream()
            .filter(entry -> entry.getValue() >= threshold)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Get the strongest pattern
     */
    public CoordinatedPattern getStrongestPattern() {
        return detectedPatterns.stream()
            .max((p1, p2) -> Double.compare(p1.getStrength(), p2.getStrength()))
            .orElse(null);
    }
    
    /**
     * Check if coordination is significant
     */
    public boolean isSignificantCoordination() {
        return coordinationScore >= 50.0 && involvedUsers.size() >= 2;
    }
    
    /**
     * Check if this is a potential raid
     */
    public boolean isPotentialRaid() {
        return primaryPatternType == CoordinatedPatternType.RAID ||
               (coordinationScore >= 70.0 && involvedUsers.size() >= 5);
    }
    
    /**
     * Check if this is spam coordination
     */
    public boolean isSpamCoordination() {
        return primaryPatternType == CoordinatedPatternType.SPAM ||
               detectedPatterns.stream().anyMatch(p -> p.getType() == CoordinatedPatternType.SPAM);
    }
    
    /**
     * Get coordination summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Coordinated Behavior Analysis for Guild %s:\n", guildId));
        summary.append(String.format("- Coordination Score: %.1f/100\n", coordinationScore));
        summary.append(String.format("- Threat Level: %s\n", threatLevel));
        summary.append(String.format("- Involved Users: %d\n", involvedUsers.size()));
        summary.append(String.format("- Primary Pattern: %s\n", primaryPatternType));
        summary.append(String.format("- Patterns Detected: %d\n", detectedPatterns.size()));
        
        if (!description.isEmpty()) {
            summary.append(String.format("- Description: %s\n", description));
        }
        
        if (requiresAction && !recommendedActions.isEmpty()) {
            summary.append("- Recommended Actions:\n");
            for (String action : recommendedActions) {
                summary.append(String.format("  * %s\n", action));
            }
        }
        
        return summary.toString();
    }
    
    /**
     * Get detailed analysis
     */
    public Map<String, Object> getDetailedAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("guildId", guildId);
        analysis.put("involvedUsers", involvedUsers);
        analysis.put("coordinationScore", coordinationScore);
        analysis.put("threatLevel", threatLevel.name());
        analysis.put("primaryPatternType", primaryPatternType != null ? primaryPatternType.name() : null);
        analysis.put("patternsCount", detectedPatterns.size());
        analysis.put("detectionTime", detectionTime);
        analysis.put("analysisTimeMs", analysisTimeMs);
        analysis.put("requiresAction", requiresAction);
        analysis.put("isSignificantCoordination", isSignificantCoordination());
        analysis.put("isPotentialRaid", isPotentialRaid());
        analysis.put("isSpamCoordination", isSpamCoordination());
        analysis.put("mostInvolvedUser", getMostInvolvedUser());
        analysis.put("evidence", evidence);
        analysis.put("userInvolvementScores", userInvolvementScores);
        analysis.put("recommendedActions", recommendedActions);
        return analysis;
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public List<String> getInvolvedUsers() { return involvedUsers; }
    public List<CoordinatedPattern> getDetectedPatterns() { return detectedPatterns; }
    public CoordinatedPatternType getPrimaryPatternType() { return primaryPatternType; }
    public double getCoordinationScore() { return coordinationScore; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public Instant getDetectionTime() { return detectionTime; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public Map<String, Object> getEvidence() { return evidence; }
    public String getDescription() { return description; }
    public boolean requiresAction() { return requiresAction; }
    public List<String> getRecommendedActions() { return recommendedActions; }
    public Map<String, Double> getUserInvolvementScores() { return userInvolvementScores; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CoordinatedBehaviorResult that = (CoordinatedBehaviorResult) obj;
        return Double.compare(that.coordinationScore, coordinationScore) == 0 &&
               analysisTimeMs == that.analysisTimeMs &&
               requiresAction == that.requiresAction &&
               Objects.equals(guildId, that.guildId) &&
               Objects.equals(involvedUsers, that.involvedUsers) &&
               Objects.equals(detectedPatterns, that.detectedPatterns) &&
               primaryPatternType == that.primaryPatternType &&
               threatLevel == that.threatLevel &&
               Objects.equals(detectionTime, that.detectionTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(guildId, involvedUsers, detectedPatterns, primaryPatternType,
                          coordinationScore, threatLevel, detectionTime, analysisTimeMs, requiresAction);
    }
    
    @Override
    public String toString() {
        return String.format("CoordinatedBehaviorResult{guildId='%s', users=%d, score=%.1f, threat=%s, pattern=%s}",
            guildId, involvedUsers.size(), coordinationScore, threatLevel, primaryPatternType);
    }
    
    /**
     * Builder for CoordinatedBehaviorResult
     */
    public static class Builder {
        private final String guildId;
        private final List<String> involvedUsers = new ArrayList<>();
        private final List<CoordinatedPattern> detectedPatterns = new ArrayList<>();
        private CoordinatedPatternType primaryPatternType;
        private double coordinationScore = 0.0;
        private ThreatLevel threatLevel = ThreatLevel.NONE;
        private Instant detectionTime;
        private long analysisTimeMs = 0;
        private final Map<String, Object> evidence = new HashMap<>();
        private String description;
        private boolean requiresAction = false;
        private final List<String> recommendedActions = new ArrayList<>();
        private final Map<String, Double> userInvolvementScores = new HashMap<>();
        
        private Builder(String guildId) {
            this.guildId = guildId;
        }
        
        public Builder addInvolvedUser(String userId) {
            this.involvedUsers.add(userId);
            return this;
        }
        
        public Builder addInvolvedUsers(Collection<String> userIds) {
            this.involvedUsers.addAll(userIds);
            return this;
        }
        
        public Builder addDetectedPattern(CoordinatedPattern pattern) {
            this.detectedPatterns.add(pattern);
            return this;
        }
        
        public Builder withPrimaryPatternType(CoordinatedPatternType patternType) {
            this.primaryPatternType = patternType;
            return this;
        }
        
        public Builder withCoordinationScore(double score) {
            this.coordinationScore = score;
            return this;
        }
        
        public Builder withThreatLevel(ThreatLevel threatLevel) {
            this.threatLevel = threatLevel;
            return this;
        }
        
        public Builder withDetectionTime(Instant detectionTime) {
            this.detectionTime = detectionTime;
            return this;
        }
        
        public Builder withAnalysisTime(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }
        
        public Builder addEvidence(String key, Object value) {
            this.evidence.put(key, value);
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder withRequiresAction(boolean requiresAction) {
            this.requiresAction = requiresAction;
            return this;
        }
        
        public Builder addRecommendedAction(String action) {
            this.recommendedActions.add(action);
            return this;
        }
        
        public Builder addUserInvolvement(String userId, double score) {
            this.userInvolvementScores.put(userId, score);
            return this;
        }
        
        public CoordinatedBehaviorResult build() {
            return new CoordinatedBehaviorResult(this);
        }
    }
}