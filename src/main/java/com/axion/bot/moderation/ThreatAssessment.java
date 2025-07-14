package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;

/**
 * Comprehensive threat assessment for users and content
 */
public class ThreatAssessment {
    private final String targetId;
    private final String guildId;
    private final ThreatType threatType;
    private final ThreatLevel threatLevel;
    private final double threatScore;
    private final double confidence;
    private final List<String> threatIndicators;
    private final Map<String, Double> threatFactors;
    private final List<String> evidenceItems;
    private final String description;
    private final Instant assessmentTime;
    private final long assessmentDurationMs;
    private final boolean requiresImmediateAction;
    private final List<String> recommendedActions;
    private final Map<String, Object> metadata;
    private final String assessmentSource;
    private final boolean isActive;
    
    private ThreatAssessment(Builder builder) {
        this.targetId = Objects.requireNonNull(builder.targetId, "Target ID cannot be null");
        this.guildId = Objects.requireNonNull(builder.guildId, "Guild ID cannot be null");
        this.threatType = Objects.requireNonNull(builder.threatType, "Threat type cannot be null");
        this.threatLevel = Objects.requireNonNull(builder.threatLevel, "Threat level cannot be null");
        this.threatScore = Math.max(0.0, Math.min(100.0, builder.threatScore));
        this.confidence = Math.max(0.0, Math.min(100.0, builder.confidence));
        this.threatIndicators = Collections.unmodifiableList(new ArrayList<>(builder.threatIndicators));
        this.threatFactors = Collections.unmodifiableMap(new HashMap<>(builder.threatFactors));
        this.evidenceItems = Collections.unmodifiableList(new ArrayList<>(builder.evidenceItems));
        this.description = builder.description != null ? builder.description : "";
        this.assessmentTime = builder.assessmentTime != null ? builder.assessmentTime : Instant.now();
        this.assessmentDurationMs = builder.assessmentDurationMs;
        this.requiresImmediateAction = builder.requiresImmediateAction;
        this.recommendedActions = Collections.unmodifiableList(new ArrayList<>(builder.recommendedActions));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.assessmentSource = builder.assessmentSource != null ? builder.assessmentSource : "Unknown";
        this.isActive = builder.isActive;
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder(String targetId, String guildId, ThreatType threatType) {
        return new Builder(targetId, guildId, threatType);
    }
    
    /**
     * Create a no-threat assessment
     */
    public static ThreatAssessment noThreat(String targetId, String guildId) {
        return builder(targetId, guildId, ThreatType.NONE)
            .withThreatLevel(ThreatLevel.NONE)
            .withThreatScore(0.0)
            .withConfidence(95.0)
            .withDescription("No threats detected")
            .build();
    }
    
    /**
     * Create a spam threat assessment
     */
    public static ThreatAssessment spamThreat(String targetId, String guildId, double score, String evidence) {
        return builder(targetId, guildId, ThreatType.SPAM)
            .withThreatLevel(determineThreatLevel(score))
            .withThreatScore(score)
            .withConfidence(Math.min(95.0, score * 0.9))
            .addEvidence(evidence)
            .withDescription("Spam behavior detected")
            .withRequiresImmediateAction(score >= 70.0)
            .build();
    }
    
    /**
     * Create a toxicity threat assessment
     */
    public static ThreatAssessment toxicityThreat(String targetId, String guildId, double score, String evidence) {
        return builder(targetId, guildId, ThreatType.TOXICITY)
            .withThreatLevel(determineThreatLevel(score))
            .withThreatScore(score)
            .withConfidence(Math.min(90.0, score * 0.85))
            .addEvidence(evidence)
            .withDescription("Toxic behavior detected")
            .withRequiresImmediateAction(score >= 60.0)
            .build();
    }
    
    /**
     * Create a harassment threat assessment
     */
    public static ThreatAssessment harassmentThreat(String targetId, String guildId, double score, String evidence) {
        return builder(targetId, guildId, ThreatType.HARASSMENT)
            .withThreatLevel(determineThreatLevel(score))
            .withThreatScore(score)
            .withConfidence(Math.min(92.0, score * 0.88))
            .addEvidence(evidence)
            .withDescription("Harassment behavior detected")
            .withRequiresImmediateAction(score >= 50.0)
            .build();
    }
    
    /**
     * Create a doxxing threat assessment
     */
    public static ThreatAssessment doxxingThreat(String targetId, String guildId, double score, String evidence) {
        return builder(targetId, guildId, ThreatType.DOXXING)
            .withThreatLevel(ThreatLevel.CRITICAL) // Doxxing is always critical
            .withThreatScore(Math.max(80.0, score))
            .withConfidence(Math.min(98.0, score * 0.95))
            .addEvidence(evidence)
            .withDescription("Doxxing attempt detected")
            .withRequiresImmediateAction(true)
            .build();
    }
    
    /**
     * Create a detected threat assessment with comprehensive details
     */
    public static ThreatAssessment detectedThreat(String targetId, String guildId, ThreatLevel threatLevel, 
                                                 List<String> detectedThreats, double confidence, 
                                                 List<String> indicators, List<String> evidence, 
                                                 String description, List<String> recommendations, 
                                                 Instant analysisTime) {
        Builder builder = builder(targetId, guildId, ThreatType.MULTIPLE)
            .withThreatLevel(threatLevel)
            .withThreatScore(threatLevel.ordinal() * 20.0) // Convert level to score
            .withConfidence(confidence)
            .withDescription(description != null ? description : "Multiple threats detected")
            .withRequiresImmediateAction(threatLevel.ordinal() >= ThreatLevel.HIGH.ordinal());
        
        if (analysisTime != null) {
            builder.withAssessmentTime(analysisTime);
        }
        
        if (detectedThreats != null) {
            detectedThreats.forEach(builder::addThreatIndicator);
        }
        
        if (indicators != null) {
            indicators.forEach(builder::addThreatIndicator);
        }
        
        if (evidence != null) {
            evidence.forEach(builder::addEvidence);
        }
        
        if (recommendations != null) {
            recommendations.forEach(builder::addRecommendedAction);
        }
        
        return builder.build();
    }
    
    /**
     * Determine threat level based on score
     */
    private static ThreatLevel determineThreatLevel(double score) {
        if (score >= 80.0) return ThreatLevel.CRITICAL;
        if (score >= 60.0) return ThreatLevel.HIGH;
        if (score >= 40.0) return ThreatLevel.MEDIUM;
        if (score >= 20.0) return ThreatLevel.LOW;
        return ThreatLevel.NONE;
    }
    
    /**
     * Get the primary threat factor
     */
    public String getPrimaryThreatFactor() {
        return threatFactors.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    /**
     * Check if threat is severe
     */
    public boolean isSevere() {
        return threatLevel.ordinal() >= ThreatLevel.HIGH.ordinal() && threatScore >= 70.0;
    }
    
    /**
     * Check if threat is critical
     */
    public boolean isCritical() {
        return threatLevel == ThreatLevel.CRITICAL || threatScore >= 85.0;
    }
    
    /**
     * Get threat severity category
     */
    public String getSeverityCategory() {
        if (isCritical()) return "Critical";
        if (isSevere()) return "Severe";
        if (threatLevel == ThreatLevel.MEDIUM) return "Moderate";
        if (threatLevel == ThreatLevel.LOW) return "Minor";
        return "None";
    }
    
    /**
     * Get confidence level category
     */
    public String getConfidenceCategory() {
        if (confidence >= 90.0) return "Very High";
        if (confidence >= 75.0) return "High";
        if (confidence >= 60.0) return "Medium";
        if (confidence >= 40.0) return "Low";
        return "Very Low";
    }
    
    /**
     * Check if assessment is reliable
     */
    public boolean isReliable() {
        return confidence >= 60.0 && !evidenceItems.isEmpty();
    }
    
    /**
     * Get assessment age in milliseconds
     */
    public long getAgeMs() {
        return Instant.now().toEpochMilli() - assessmentTime.toEpochMilli();
    }
    
    /**
     * Check if assessment is recent
     */
    public boolean isRecent(long thresholdMs) {
        return getAgeMs() <= thresholdMs;
    }
    
    /**
     * Get threat summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Threat Assessment for %s:\n", targetId));
        summary.append(String.format("- Type: %s\n", threatType));
        summary.append(String.format("- Level: %s (Score: %.1f/100)\n", threatLevel, threatScore));
        summary.append(String.format("- Severity: %s\n", getSeverityCategory()));
        summary.append(String.format("- Confidence: %s (%.1f%%)\n", getConfidenceCategory(), confidence));
        
        if (!description.isEmpty()) {
            summary.append(String.format("- Description: %s\n", description));
        }
        
        if (!threatIndicators.isEmpty()) {
            summary.append(String.format("- Indicators: %d detected\n", threatIndicators.size()));
        }
        
        if (requiresImmediateAction) {
            summary.append("- Status: IMMEDIATE ACTION REQUIRED\n");
        }
        
        if (!recommendedActions.isEmpty()) {
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
        analysis.put("targetId", targetId);
        analysis.put("guildId", guildId);
        analysis.put("threatType", threatType.name());
        analysis.put("threatLevel", threatLevel.name());
        analysis.put("threatScore", threatScore);
        analysis.put("confidence", confidence);
        analysis.put("severityCategory", getSeverityCategory());
        analysis.put("confidenceCategory", getConfidenceCategory());
        analysis.put("isSevere", isSevere());
        analysis.put("isCritical", isCritical());
        analysis.put("isReliable", isReliable());
        analysis.put("requiresImmediateAction", requiresImmediateAction);
        analysis.put("primaryThreatFactor", getPrimaryThreatFactor());
        analysis.put("threatIndicators", threatIndicators);
        analysis.put("threatFactors", threatFactors);
        analysis.put("evidenceItems", evidenceItems);
        analysis.put("recommendedActions", recommendedActions);
        analysis.put("assessmentTime", assessmentTime);
        analysis.put("assessmentDurationMs", assessmentDurationMs);
        analysis.put("assessmentSource", assessmentSource);
        analysis.put("isActive", isActive);
        analysis.put("ageMs", getAgeMs());
        analysis.put("metadata", metadata);
        return analysis;
    }
    
    /**
     * Merge with another threat assessment
     */
    public ThreatAssessment mergeWith(ThreatAssessment other) {
        if (!this.targetId.equals(other.targetId) || !this.guildId.equals(other.guildId)) {
            throw new IllegalArgumentException("Cannot merge assessments for different targets");
        }
        
        Builder builder = builder(targetId, guildId, threatType)
            .withThreatLevel(threatLevel.ordinal() > other.threatLevel.ordinal() ? threatLevel : other.threatLevel)
            .withThreatScore(Math.max(threatScore, other.threatScore))
            .withConfidence((confidence + other.confidence) / 2.0)
            .withDescription(description + "; " + other.description)
            .withRequiresImmediateAction(requiresImmediateAction || other.requiresImmediateAction);
        
        // Merge indicators
        Set<String> allIndicators = new HashSet<>(threatIndicators);
        allIndicators.addAll(other.threatIndicators);
        allIndicators.forEach(builder::addThreatIndicator);
        
        // Merge evidence
        Set<String> allEvidence = new HashSet<>(evidenceItems);
        allEvidence.addAll(other.evidenceItems);
        allEvidence.forEach(builder::addEvidence);
        
        // Merge actions
        Set<String> allActions = new HashSet<>(recommendedActions);
        allActions.addAll(other.recommendedActions);
        allActions.forEach(builder::addRecommendedAction);
        
        return builder.build();
    }
    
    // Getters
    public String getTargetId() { return targetId; }
    public String getGuildId() { return guildId; }
    public ThreatType getThreatType() { return threatType; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public double getThreatScore() { return threatScore; }
    public double getConfidence() { return confidence; }
    public List<String> getThreatIndicators() { return threatIndicators; }
    public Map<String, Double> getThreatFactors() { return threatFactors; }
    public List<String> getEvidenceItems() { return evidenceItems; }
    public String getDescription() { return description; }
    public Instant getAssessmentTime() { return assessmentTime; }
    public long getAssessmentDurationMs() { return assessmentDurationMs; }
    public boolean requiresImmediateAction() { return requiresImmediateAction; }
    public List<String> getRecommendedActions() { return recommendedActions; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getAssessmentSource() { return assessmentSource; }
    public boolean isActive() { return isActive; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ThreatAssessment that = (ThreatAssessment) obj;
        return Objects.equals(targetId, that.targetId) &&
               Objects.equals(guildId, that.guildId) &&
               threatType == that.threatType &&
               Objects.equals(assessmentTime, that.assessmentTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(targetId, guildId, threatType, assessmentTime);
    }
    
    @Override
    public String toString() {
        return String.format("ThreatAssessment{target='%s', type=%s, level=%s, score=%.1f, confidence=%.1f}",
            targetId, threatType, threatLevel, threatScore, confidence);
    }
    
    /**
     * Builder for ThreatAssessment
     */
    public static class Builder {
        private final String targetId;
        private final String guildId;
        private final ThreatType threatType;
        private ThreatLevel threatLevel = ThreatLevel.NONE;
        private double threatScore = 0.0;
        private double confidence = 0.0;
        private final List<String> threatIndicators = new ArrayList<>();
        private final Map<String, Double> threatFactors = new HashMap<>();
        private final List<String> evidenceItems = new ArrayList<>();
        private String description;
        private Instant assessmentTime;
        private long assessmentDurationMs = 0;
        private boolean requiresImmediateAction = false;
        private final List<String> recommendedActions = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private String assessmentSource;
        private boolean isActive = true;
        
        private Builder(String targetId, String guildId, ThreatType threatType) {
            this.targetId = targetId;
            this.guildId = guildId;
            this.threatType = threatType;
        }
        
        public Builder withThreatLevel(ThreatLevel threatLevel) {
            this.threatLevel = threatLevel;
            return this;
        }
        
        public Builder withThreatScore(double threatScore) {
            this.threatScore = threatScore;
            return this;
        }
        
        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public Builder addThreatIndicator(String indicator) {
            this.threatIndicators.add(indicator);
            return this;
        }
        
        public Builder addThreatFactor(String factor, double weight) {
            this.threatFactors.put(factor, weight);
            return this;
        }
        
        public Builder addEvidence(String evidence) {
            this.evidenceItems.add(evidence);
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder withAssessmentTime(Instant assessmentTime) {
            this.assessmentTime = assessmentTime;
            return this;
        }
        
        public Builder withAssessmentDuration(long durationMs) {
            this.assessmentDurationMs = durationMs;
            return this;
        }
        
        public Builder withRequiresImmediateAction(boolean requiresImmediateAction) {
            this.requiresImmediateAction = requiresImmediateAction;
            return this;
        }
        
        public Builder addRecommendedAction(String action) {
            this.recommendedActions.add(action);
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder withAssessmentSource(String source) {
            this.assessmentSource = source;
            return this;
        }
        
        public Builder withIsActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public ThreatAssessment build() {
            return new ThreatAssessment(this);
        }
    }
}