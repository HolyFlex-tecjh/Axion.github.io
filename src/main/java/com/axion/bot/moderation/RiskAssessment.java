package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Risk assessment for user behavior analysis
 */
public class RiskAssessment {
    private final String userId;
    private final String guildId;
    private final RiskLevel riskLevel;
    private final double riskScore;
    private final Map<String, Double> riskFactors;
    private final List<String> riskReasons;
    private final Map<PatternType, Double> patternRisks;
    private final Instant assessmentTime;
    private final long assessmentDurationMs;
    private final boolean requiresAction;
    private final String recommendation;
    private final Map<String, Object> metadata;
    
    private RiskAssessment(Builder builder) {
        this.userId = Objects.requireNonNull(builder.userId, "User ID cannot be null");
        this.guildId = Objects.requireNonNull(builder.guildId, "Guild ID cannot be null");
        this.riskLevel = Objects.requireNonNull(builder.riskLevel, "Risk level cannot be null");
        this.riskScore = Math.max(0.0, Math.min(100.0, builder.riskScore));
        this.riskFactors = Collections.unmodifiableMap(new HashMap<>(builder.riskFactors));
        this.riskReasons = Collections.unmodifiableList(new ArrayList<>(builder.riskReasons));
        this.patternRisks = Collections.unmodifiableMap(new HashMap<>(builder.patternRisks));
        this.assessmentTime = builder.assessmentTime != null ? builder.assessmentTime : Instant.now();
        this.assessmentDurationMs = builder.assessmentDurationMs;
        this.requiresAction = builder.requiresAction;
        this.recommendation = builder.recommendation != null ? builder.recommendation : "";
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }
    
    /**
     * Create a new risk assessment builder
     */
    public static Builder builder(String userId, String guildId) {
        return new Builder(userId, guildId);
    }
    
    /**
     * Create a low-risk assessment
     */
    public static RiskAssessment lowRisk(String userId, String guildId, double score) {
        return builder(userId, guildId)
            .withRiskLevel(RiskLevel.LOW)
            .withRiskScore(score)
            .withRecommendation("No action required")
            .build();
    }
    
    /**
     * Create a medium-risk assessment
     */
    public static RiskAssessment mediumRisk(String userId, String guildId, double score, String reason) {
        return builder(userId, guildId)
            .withRiskLevel(RiskLevel.MEDIUM)
            .withRiskScore(score)
            .addRiskReason(reason)
            .withRecommendation("Monitor user activity")
            .withRequiresAction(true)
            .build();
    }
    
    /**
     * Create a high-risk assessment
     */
    public static RiskAssessment highRisk(String userId, String guildId, double score, String reason) {
        return builder(userId, guildId)
            .withRiskLevel(RiskLevel.HIGH)
            .withRiskScore(score)
            .addRiskReason(reason)
            .withRecommendation("Immediate moderation action required")
            .withRequiresAction(true)
            .build();
    }
    
    /**
     * Create a critical-risk assessment
     */
    public static RiskAssessment criticalRisk(String userId, String guildId, double score, String reason) {
        return builder(userId, guildId)
            .withRiskLevel(RiskLevel.CRITICAL)
            .withRiskScore(score)
            .addRiskReason(reason)
            .withRecommendation("Immediate ban or severe action required")
            .withRequiresAction(true)
            .build();
    }
    
    /**
     * Get the primary risk factor
     */
    public String getPrimaryRiskFactor() {
        return riskFactors.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    /**
     * Get the highest pattern risk
     */
    public PatternType getHighestRiskPattern() {
        return patternRisks.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Check if risk score exceeds threshold
     */
    public boolean exceedsThreshold(double threshold) {
        return riskScore >= threshold;
    }
    
    /**
     * Get risk category based on score
     */
    public String getRiskCategory() {
        if (riskScore >= 80) return "Critical";
        if (riskScore >= 60) return "High";
        if (riskScore >= 40) return "Medium";
        if (riskScore >= 20) return "Low";
        return "Minimal";
    }
    
    /**
     * Get confidence level of the assessment
     */
    public double getConfidenceLevel() {
        // Calculate confidence based on number of risk factors and patterns
        double factorConfidence = Math.min(1.0, riskFactors.size() / 5.0);
        double patternConfidence = Math.min(1.0, patternRisks.size() / 3.0);
        return (factorConfidence + patternConfidence) / 2.0 * 100.0;
    }
    
    /**
     * Get assessment summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Risk Assessment for %s:\n", userId));
        summary.append(String.format("- Level: %s (Score: %.1f/100)\n", riskLevel, riskScore));
        summary.append(String.format("- Category: %s\n", getRiskCategory()));
        summary.append(String.format("- Confidence: %.1f%%\n", getConfidenceLevel()));
        
        if (!riskReasons.isEmpty()) {
            summary.append("- Reasons:\n");
            for (String reason : riskReasons) {
                summary.append(String.format("  * %s\n", reason));
            }
        }
        
        if (!recommendation.isEmpty()) {
            summary.append(String.format("- Recommendation: %s\n", recommendation));
        }
        
        return summary.toString();
    }
    
    /**
     * Get detailed analysis
     */
    public Map<String, Object> getDetailedAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("userId", userId);
        analysis.put("guildId", guildId);
        analysis.put("riskLevel", riskLevel.name());
        analysis.put("riskScore", riskScore);
        analysis.put("riskCategory", getRiskCategory());
        analysis.put("confidenceLevel", getConfidenceLevel());
        analysis.put("primaryRiskFactor", getPrimaryRiskFactor());
        analysis.put("highestRiskPattern", getHighestRiskPattern());
        analysis.put("requiresAction", requiresAction);
        analysis.put("recommendation", recommendation);
        analysis.put("assessmentTime", assessmentTime);
        analysis.put("assessmentDurationMs", assessmentDurationMs);
        analysis.put("riskFactors", riskFactors);
        analysis.put("riskReasons", riskReasons);
        analysis.put("patternRisks", patternRisks);
        analysis.put("metadata", metadata);
        return analysis;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public double getRiskScore() { return riskScore; }
    public Map<String, Double> getRiskFactors() { return riskFactors; }
    public List<String> getRiskReasons() { return riskReasons; }
    public Map<PatternType, Double> getPatternRisks() { return patternRisks; }
    public Instant getAssessmentTime() { return assessmentTime; }
    public long getAssessmentDurationMs() { return assessmentDurationMs; }
    public boolean requiresAction() { return requiresAction; }
    public String getRecommendation() { return recommendation; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RiskAssessment that = (RiskAssessment) obj;
        return Double.compare(that.riskScore, riskScore) == 0 &&
               assessmentDurationMs == that.assessmentDurationMs &&
               requiresAction == that.requiresAction &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(guildId, that.guildId) &&
               riskLevel == that.riskLevel &&
               Objects.equals(riskFactors, that.riskFactors) &&
               Objects.equals(riskReasons, that.riskReasons) &&
               Objects.equals(patternRisks, that.patternRisks) &&
               Objects.equals(assessmentTime, that.assessmentTime) &&
               Objects.equals(recommendation, that.recommendation) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, guildId, riskLevel, riskScore, riskFactors, riskReasons,
                          patternRisks, assessmentTime, assessmentDurationMs, requiresAction,
                          recommendation, metadata);
    }
    
    @Override
    public String toString() {
        return String.format("RiskAssessment{userId='%s', guildId='%s', level=%s, score=%.1f, requiresAction=%s}",
            userId, guildId, riskLevel, riskScore, requiresAction);
    }
    
    /**
     * Builder for RiskAssessment
     */
    public static class Builder {
        private final String userId;
        private final String guildId;
        private RiskLevel riskLevel = RiskLevel.LOW;
        private double riskScore = 0.0;
        private final Map<String, Double> riskFactors = new HashMap<>();
        private final List<String> riskReasons = new ArrayList<>();
        private final Map<PatternType, Double> patternRisks = new HashMap<>();
        private Instant assessmentTime;
        private long assessmentDurationMs = 0;
        private boolean requiresAction = false;
        private String recommendation;
        private final Map<String, Object> metadata = new HashMap<>();
        
        private Builder(String userId, String guildId) {
            this.userId = userId;
            this.guildId = guildId;
        }
        
        public Builder withRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }
        
        public Builder withRiskScore(double riskScore) {
            this.riskScore = riskScore;
            return this;
        }
        
        public Builder addRiskFactor(String factor, double weight) {
            this.riskFactors.put(factor, weight);
            return this;
        }
        
        public Builder addRiskReason(String reason) {
            this.riskReasons.add(reason);
            return this;
        }
        
        public Builder addPatternRisk(PatternType pattern, double risk) {
            this.patternRisks.put(pattern, risk);
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
        
        public Builder withRequiresAction(boolean requiresAction) {
            this.requiresAction = requiresAction;
            return this;
        }
        
        public Builder withRecommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public RiskAssessment build() {
            return new RiskAssessment(this);
        }
    }
}