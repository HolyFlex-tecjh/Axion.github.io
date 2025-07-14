package com.axion.bot.moderation;

import java.time.Instant;
import java.util.*;

/**
 * Comprehensive insights about user behavior patterns and trends
 */
public class UserBehaviorInsights {
    private final String userId;
    private final String guildId;
    private final UserBehaviorProfile profile;
    private final Map<String, Double> behaviorScores;
    private final Map<String, String> insights;
    private final List<String> trends;
    private final List<String> recommendations;
    private final Map<String, Object> statistics;
    private final Instant generatedAt;
    private final long analysisTimeMs;
    private final double overallRiskScore;
    private final String riskCategory;
    private final Map<PatternType, Integer> patternFrequency;
    private final Map<ActivityType, Double> activityMetrics;
    
    private UserBehaviorInsights(Builder builder) {
        this.userId = Objects.requireNonNull(builder.userId, "User ID cannot be null");
        this.guildId = Objects.requireNonNull(builder.guildId, "Guild ID cannot be null");
        this.profile = Objects.requireNonNull(builder.profile, "Profile cannot be null");
        this.behaviorScores = Collections.unmodifiableMap(new HashMap<>(builder.behaviorScores));
        this.insights = Collections.unmodifiableMap(new HashMap<>(builder.insights));
        this.trends = Collections.unmodifiableList(new ArrayList<>(builder.trends));
        this.recommendations = Collections.unmodifiableList(new ArrayList<>(builder.recommendations));
        this.statistics = Collections.unmodifiableMap(new HashMap<>(builder.statistics));
        this.generatedAt = builder.generatedAt != null ? builder.generatedAt : Instant.now();
        this.analysisTimeMs = builder.analysisTimeMs;
        this.overallRiskScore = Math.max(0.0, Math.min(100.0, builder.overallRiskScore));
        this.riskCategory = builder.riskCategory != null ? builder.riskCategory : "Unknown";
        this.patternFrequency = Collections.unmodifiableMap(new HashMap<>(builder.patternFrequency));
        this.activityMetrics = Collections.unmodifiableMap(new HashMap<>(builder.activityMetrics));
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder(String userId, String guildId, UserBehaviorProfile profile) {
        return new Builder(userId, guildId, profile);
    }
    
    /**
     * Create a not found result
     */
    public static UserBehaviorInsights notFound(String userId, String guildId) {
        // Create a minimal profile for the not found case
        UserBehaviorProfile emptyProfile = new UserBehaviorProfile(userId, guildId);
        return builder(userId, guildId, emptyProfile)
            .withOverallRiskScore(0.0)
            .withRiskCategory("Unknown")
            .addInsight("status", "User profile not found")
            .addRecommendation("User data needs to be initialized")
            .build();
    }
    
    /**
     * Create an error result
     */
    public static UserBehaviorInsights error(String userId, String errorMessage) {
        // Create a minimal profile for the error case
        UserBehaviorProfile emptyProfile = new UserBehaviorProfile(userId, "unknown");
        return builder(userId, "unknown", emptyProfile)
            .withOverallRiskScore(0.0)
            .withRiskCategory("Error")
            .addInsight("error", errorMessage)
            .addRecommendation("Review system logs for details")
            .build();
    }
    
    /**
     * Generate insights from user profile
     */
    public static UserBehaviorInsights fromProfile(UserBehaviorProfile profile) {
        Builder builder = builder(profile.getUserId(), profile.getGuildId(), profile);
        
        // Calculate behavior scores
        UserBehaviorMetrics metrics = profile.getMetrics();
        builder.addBehaviorScore("toxicity", metrics.getToxicityScore())
               .addBehaviorScore("spam", metrics.getSpamScore())
               .addBehaviorScore("trust", profile.getTrustScore())
               .addBehaviorScore("reputation", profile.getReputationScore());
        
        // Calculate overall risk score
        double riskScore = calculateOverallRisk(profile);
        builder.withOverallRiskScore(riskScore)
               .withRiskCategory(categorizeRisk(riskScore));
        
        // Add pattern frequency
        for (Map.Entry<PatternType, Integer> entry : profile.getPatternCounts().entrySet()) {
            builder.addPatternFrequency(entry.getKey(), entry.getValue());
        }
        
        // Generate insights
        generateInsights(builder, profile);
        
        // Generate trends
        generateTrends(builder, profile);
        
        // Generate recommendations
        generateRecommendations(builder, profile);
        
        return builder.build();
    }
    
    /**
     * Calculate overall risk score from profile
     */
    private static double calculateOverallRisk(UserBehaviorProfile profile) {
        UserBehaviorMetrics metrics = profile.getMetrics();
        
        double toxicityWeight = 0.3;
        double spamWeight = 0.25;
        double trustWeight = 0.2;
        double reputationWeight = 0.15;
        double patternWeight = 0.1;
        
        double toxicityRisk = metrics.getToxicityScore();
        double spamRisk = metrics.getSpamScore();
        double trustRisk = 100.0 - profile.getTrustScore(); // Invert trust score
        double reputationRisk = 100.0 - profile.getReputationScore(); // Invert reputation score
        double patternRisk = Math.min(100.0, profile.getTotalPatternCount() * 5.0);
        
        return (toxicityRisk * toxicityWeight) +
               (spamRisk * spamWeight) +
               (trustRisk * trustWeight) +
               (reputationRisk * reputationWeight) +
               (patternRisk * patternWeight);
    }
    
    /**
     * Categorize risk level
     */
    private static String categorizeRisk(double riskScore) {
        if (riskScore >= 80) return "Critical";
        if (riskScore >= 60) return "High";
        if (riskScore >= 40) return "Medium";
        if (riskScore >= 20) return "Low";
        return "Minimal";
    }
    
    /**
     * Generate behavioral insights
     */
    private static void generateInsights(Builder builder, UserBehaviorProfile profile) {
        UserBehaviorMetrics metrics = profile.getMetrics();
        
        // Toxicity insights
        if (metrics.getToxicityScore() > 50) {
            builder.addInsight("toxicity", "User shows elevated toxicity patterns in messages");
        } else if (metrics.getToxicityScore() < 10) {
            builder.addInsight("toxicity", "User maintains very low toxicity levels");
        }
        
        // Spam insights
        if (metrics.getSpamScore() > 60) {
            builder.addInsight("spam", "User exhibits significant spam-like behavior");
        } else if (metrics.getSpamScore() < 15) {
            builder.addInsight("spam", "User rarely engages in spam behavior");
        }
        
        // Trust insights
        if (profile.getTrustScore() > 80) {
            builder.addInsight("trust", "User has established high trust within the community");
        } else if (profile.getTrustScore() < 30) {
            builder.addInsight("trust", "User has low trust score, requires monitoring");
        }
        
        // Activity insights
        if (metrics.getTotalMessages() > 1000) {
            builder.addInsight("activity", "User is highly active in the server");
        } else if (metrics.getTotalMessages() < 10) {
            builder.addInsight("activity", "User has minimal activity in the server");
        }
        
        // Pattern insights
        int totalPatterns = profile.getTotalPatternCount();
        if (totalPatterns > 20) {
            builder.addInsight("patterns", "User shows concerning pattern of violations");
        } else if (totalPatterns == 0) {
            builder.addInsight("patterns", "User has clean behavioral record");
        }
    }
    
    /**
     * Generate behavioral trends
     */
    private static void generateTrends(Builder builder, UserBehaviorProfile profile) {
        UserBehaviorMetrics metrics = profile.getMetrics();
        
        // Trust trend
        if (profile.getTrustScore() > 70) {
            builder.addTrend("Trust score trending upward - positive community member");
        } else if (profile.getTrustScore() < 30) {
            builder.addTrend("Trust score concerning - increased monitoring recommended");
        }
        
        // Activity trend
        if (metrics.getTotalMessages() > 500) {
            builder.addTrend("High activity levels - engaged community member");
        }
        
        // Risk trend
        if (profile.getCurrentRiskLevel().ordinal() >= RiskLevel.HIGH.ordinal()) {
            builder.addTrend("Risk level elevated - immediate attention required");
        }
        
        // Pattern trend
        if (profile.getTotalPatternCount() > 10) {
            builder.addTrend("Increasing pattern violations - behavioral intervention needed");
        }
    }
    
    /**
     * Generate recommendations
     */
    private static void generateRecommendations(Builder builder, UserBehaviorProfile profile) {
        UserBehaviorMetrics metrics = profile.getMetrics();
        
        // High risk recommendations
        if (profile.getCurrentRiskLevel().ordinal() >= RiskLevel.HIGH.ordinal()) {
            builder.addRecommendation("Implement immediate monitoring and intervention measures");
            builder.addRecommendation("Consider temporary restrictions or warnings");
        }
        
        // Toxicity recommendations
        if (metrics.getToxicityScore() > 60) {
            builder.addRecommendation("Address toxic behavior through warnings or education");
        }
        
        // Spam recommendations
        if (metrics.getSpamScore() > 50) {
            builder.addRecommendation("Implement spam detection and rate limiting");
        }
        
        // Trust building recommendations
        if (profile.getTrustScore() < 40) {
            builder.addRecommendation("Encourage positive community participation");
            builder.addRecommendation("Provide clear guidelines and expectations");
        }
        
        // Monitoring recommendations
        if (profile.requiresMonitoring()) {
            builder.addRecommendation("Enable enhanced monitoring for this user");
        }
        
        // Positive reinforcement
        if (profile.getTrustScore() > 80 && metrics.getToxicityScore() < 10) {
            builder.addRecommendation("Consider user for trusted member or moderator roles");
        }
    }
    
    /**
     * Get insight for specific category
     */
    public String getInsight(String category) {
        return insights.get(category);
    }
    
    /**
     * Get behavior score for specific category
     */
    public Double getBehaviorScore(String category) {
        return behaviorScores.get(category);
    }
    
    /**
     * Check if user is high risk
     */
    public boolean isHighRisk() {
        return overallRiskScore >= 60.0;
    }
    
    /**
     * Check if user requires attention
     */
    public boolean requiresAttention() {
        return overallRiskScore >= 40.0 || profile.requiresMonitoring();
    }
    
    /**
     * Get summary of insights
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Behavior Insights for User %s:\n", userId));
        summary.append(String.format("- Overall Risk: %s (%.1f/100)\n", riskCategory, overallRiskScore));
        summary.append(String.format("- Trust Score: %.1f\n", profile.getTrustScore()));
        summary.append(String.format("- Reputation Score: %.1f\n", profile.getReputationScore()));
        summary.append(String.format("- Total Patterns: %d\n", profile.getTotalPatternCount()));
        
        if (!trends.isEmpty()) {
            summary.append("\nKey Trends:\n");
            for (String trend : trends) {
                summary.append(String.format("- %s\n", trend));
            }
        }
        
        if (!recommendations.isEmpty()) {
            summary.append("\nRecommendations:\n");
            for (String recommendation : recommendations) {
                summary.append(String.format("- %s\n", recommendation));
            }
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
        analysis.put("overallRiskScore", overallRiskScore);
        analysis.put("riskCategory", riskCategory);
        analysis.put("isHighRisk", isHighRisk());
        analysis.put("requiresAttention", requiresAttention());
        analysis.put("behaviorScores", behaviorScores);
        analysis.put("insights", insights);
        analysis.put("trends", trends);
        analysis.put("recommendations", recommendations);
        analysis.put("patternFrequency", patternFrequency);
        analysis.put("activityMetrics", activityMetrics);
        analysis.put("statistics", statistics);
        analysis.put("generatedAt", generatedAt);
        analysis.put("analysisTimeMs", analysisTimeMs);
        return analysis;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public UserBehaviorProfile getProfile() { return profile; }
    public Map<String, Double> getBehaviorScores() { return behaviorScores; }
    public Map<String, String> getInsights() { return insights; }
    public List<String> getTrends() { return trends; }
    public List<String> getRecommendations() { return recommendations; }
    public Map<String, Object> getStatistics() { return statistics; }
    public Instant getGeneratedAt() { return generatedAt; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public double getOverallRiskScore() { return overallRiskScore; }
    public String getRiskCategory() { return riskCategory; }
    public Map<PatternType, Integer> getPatternFrequency() { return patternFrequency; }
    public Map<ActivityType, Double> getActivityMetrics() { return activityMetrics; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserBehaviorInsights that = (UserBehaviorInsights) obj;
        return Objects.equals(userId, that.userId) && Objects.equals(guildId, that.guildId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, guildId);
    }
    
    @Override
    public String toString() {
        return String.format("UserBehaviorInsights{userId='%s', guildId='%s', risk=%s, score=%.1f}",
            userId, guildId, riskCategory, overallRiskScore);
    }
    
    /**
     * Builder for UserBehaviorInsights
     */
    public static class Builder {
        private final String userId;
        private final String guildId;
        private final UserBehaviorProfile profile;
        private final Map<String, Double> behaviorScores = new HashMap<>();
        private final Map<String, String> insights = new HashMap<>();
        private final List<String> trends = new ArrayList<>();
        private final List<String> recommendations = new ArrayList<>();
        private final Map<String, Object> statistics = new HashMap<>();
        private Instant generatedAt;
        private long analysisTimeMs = 0;
        private double overallRiskScore = 0.0;
        private String riskCategory;
        private final Map<PatternType, Integer> patternFrequency = new HashMap<>();
        private final Map<ActivityType, Double> activityMetrics = new HashMap<>();
        
        private Builder(String userId, String guildId, UserBehaviorProfile profile) {
            this.userId = userId;
            this.guildId = guildId;
            this.profile = profile;
        }
        
        public Builder addBehaviorScore(String category, double score) {
            this.behaviorScores.put(category, score);
            return this;
        }
        
        public Builder addInsight(String category, String insight) {
            this.insights.put(category, insight);
            return this;
        }
        
        public Builder addTrend(String trend) {
            this.trends.add(trend);
            return this;
        }
        
        public Builder addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
            return this;
        }
        
        public Builder addStatistic(String key, Object value) {
            this.statistics.put(key, value);
            return this;
        }
        
        public Builder withGeneratedAt(Instant generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }
        
        public Builder withAnalysisTime(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }
        
        public Builder withOverallRiskScore(double overallRiskScore) {
            this.overallRiskScore = overallRiskScore;
            return this;
        }
        
        public Builder withRiskCategory(String riskCategory) {
            this.riskCategory = riskCategory;
            return this;
        }
        
        public Builder addPatternFrequency(PatternType pattern, int frequency) {
            this.patternFrequency.put(pattern, frequency);
            return this;
        }
        
        public Builder addActivityMetric(ActivityType activity, double metric) {
            this.activityMetrics.put(activity, metric);
            return this;
        }
        
        public UserBehaviorInsights build() {
            return new UserBehaviorInsights(this);
        }
    }
}