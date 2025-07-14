package com.axion.bot.moderation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Result of behavioral analysis for a user
 */
public class BehaviorAnalysisResult {
    private final String userId;
    private final String guildId;
    private final UserBehaviorProfile profile;
    private final List<BehaviorPattern> detectedPatterns;
    private final RiskAssessment riskAssessment;
    private final long analysisTimeMs;
    private final Instant analysisTimestamp;
    private final boolean isError;
    private final String errorMessage;
    
    // Constructor for successful analysis
    public BehaviorAnalysisResult(String userId, String guildId, UserBehaviorProfile profile,
                                 List<BehaviorPattern> detectedPatterns, RiskAssessment riskAssessment,
                                 long analysisTimeMs) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.guildId = Objects.requireNonNull(guildId, "Guild ID cannot be null");
        this.profile = Objects.requireNonNull(profile, "Profile cannot be null");
        this.detectedPatterns = detectedPatterns != null ? List.copyOf(detectedPatterns) : Collections.emptyList();
        this.riskAssessment = Objects.requireNonNull(riskAssessment, "Risk assessment cannot be null");
        this.analysisTimeMs = analysisTimeMs;
        this.analysisTimestamp = Instant.now();
        this.isError = false;
        this.errorMessage = null;
    }
    
    // Constructor for error cases
    private BehaviorAnalysisResult(String userId, String guildId, String errorMessage) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.guildId = Objects.requireNonNull(guildId, "Guild ID cannot be null");
        this.profile = null;
        this.detectedPatterns = Collections.emptyList();
        this.riskAssessment = null;
        this.analysisTimeMs = 0;
        this.analysisTimestamp = Instant.now();
        this.isError = true;
        this.errorMessage = Objects.requireNonNull(errorMessage, "Error message cannot be null");
    }
    
    /**
     * Create an error result
     */
    public static BehaviorAnalysisResult error(String userId, String guildId, String errorMessage) {
        return new BehaviorAnalysisResult(userId, guildId, errorMessage);
    }
    
    /**
     * Get the user ID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Get the guild ID
     */
    public String getGuildId() {
        return guildId;
    }
    
    /**
     * Get the user behavior profile
     */
    public UserBehaviorProfile getProfile() {
        return profile;
    }
    
    /**
     * Get the detected behavior patterns
     */
    public List<BehaviorPattern> getDetectedPatterns() {
        return detectedPatterns;
    }
    
    /**
     * Get the risk assessment
     */
    public RiskAssessment getRiskAssessment() {
        return riskAssessment;
    }
    
    /**
     * Get the analysis time in milliseconds
     */
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    /**
     * Get when the analysis was performed
     */
    public Instant getAnalysisTimestamp() {
        return analysisTimestamp;
    }
    
    /**
     * Check if this result represents an error
     */
    public boolean isError() {
        return isError;
    }
    
    /**
     * Get the error message (if any)
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Check if any high-risk patterns were detected
     */
    public boolean hasHighRiskPatterns() {
        return !isError && detectedPatterns.stream().anyMatch(BehaviorPattern::isHighRisk);
    }
    
    /**
     * Check if immediate action is required
     */
    public boolean requiresImmediateAction() {
        return !isError && (detectedPatterns.stream().anyMatch(BehaviorPattern::requiresImmediateAction) ||
                           (riskAssessment != null && riskAssessment.getRiskLevel().ordinal() >= RiskLevel.HIGH.ordinal()));
    }
    
    /**
     * Get patterns of a specific type
     */
    public List<BehaviorPattern> getPatternsByType(PatternType type) {
        return detectedPatterns.stream()
            .filter(pattern -> pattern.getPatternType() == type)
            .collect(Collectors.toList());
    }
    
    /**
     * Get the highest risk pattern
     */
    public BehaviorPattern getHighestRiskPattern() {
        return detectedPatterns.stream()
            .max((p1, p2) -> Double.compare(p1.getRiskWeight(), p2.getRiskWeight()))
            .orElse(null);
    }
    
    /**
     * Get a summary of the analysis
     */
    public String getSummary() {
        if (isError) {
            return String.format("Analysis failed for user %s in guild %s: %s", userId, guildId, errorMessage);
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Analysis for user %s in guild %s:\n", userId, guildId));
        summary.append(String.format("- Risk Level: %s (Score: %.2f)\n", 
            riskAssessment.getRiskLevel(), riskAssessment.getRiskScore()));
        summary.append(String.format("- Patterns Detected: %d\n", detectedPatterns.size()));
        
        if (!detectedPatterns.isEmpty()) {
            summary.append("- Pattern Details:\n");
            for (BehaviorPattern pattern : detectedPatterns) {
                summary.append(String.format("  * %s\n", pattern.getSummary()));
            }
        }
        
        summary.append(String.format("- Analysis Time: %d ms", analysisTimeMs));
        
        return summary.toString();
    }
    
    /**
     * Check if the behavior is anomalous
     */
    public boolean isAnomalous() {
        return !isError && (hasHighRiskPatterns() || requiresImmediateAction());
    }
    
    /**
     * Get the anomaly score (0.0 to 1.0)
     */
    public double getAnomalyScore() {
        if (isError || riskAssessment == null) {
            return 0.0;
        }
        return riskAssessment.getRiskScore();
    }
    
    /**
     * Get the type of anomaly detected
     */
    public String getAnomalyType() {
        if (isError || detectedPatterns.isEmpty()) {
            return "None";
        }
        
        BehaviorPattern highestRisk = getHighestRiskPattern();
        return highestRisk != null ? highestRisk.getPatternType().toString() : "Unknown";
    }
    
    /**
     * Get analysis performance metrics
     */
    public AnalysisMetrics getMetrics() {
        return new AnalysisMetrics(
            analysisTimeMs,
            detectedPatterns.size(),
            isError,
            hasHighRiskPatterns(),
            requiresImmediateAction()
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BehaviorAnalysisResult that = (BehaviorAnalysisResult) obj;
        return analysisTimeMs == that.analysisTimeMs &&
               isError == that.isError &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(guildId, that.guildId) &&
               Objects.equals(profile, that.profile) &&
               Objects.equals(detectedPatterns, that.detectedPatterns) &&
               Objects.equals(riskAssessment, that.riskAssessment) &&
               Objects.equals(analysisTimestamp, that.analysisTimestamp) &&
               Objects.equals(errorMessage, that.errorMessage);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, guildId, profile, detectedPatterns, riskAssessment, 
                          analysisTimeMs, analysisTimestamp, isError, errorMessage);
    }
    
    @Override
    public String toString() {
        if (isError) {
            return String.format("BehaviorAnalysisResult{userId='%s', guildId='%s', error='%s'}",
                userId, guildId, errorMessage);
        }
        
        return String.format("BehaviorAnalysisResult{userId='%s', guildId='%s', riskLevel=%s, patterns=%d, timeMs=%d}",
            userId, guildId, riskAssessment.getRiskLevel(), detectedPatterns.size(), analysisTimeMs);
    }
    
    /**
     * Analysis performance metrics
     */
    public static class AnalysisMetrics {
        private final long analysisTimeMs;
        private final int patternsDetected;
        private final boolean hasError;
        private final boolean hasHighRiskPatterns;
        private final boolean requiresImmediateAction;
        
        public AnalysisMetrics(long analysisTimeMs, int patternsDetected, boolean hasError,
                              boolean hasHighRiskPatterns, boolean requiresImmediateAction) {
            this.analysisTimeMs = analysisTimeMs;
            this.patternsDetected = patternsDetected;
            this.hasError = hasError;
            this.hasHighRiskPatterns = hasHighRiskPatterns;
            this.requiresImmediateAction = requiresImmediateAction;
        }
        
        public long getAnalysisTimeMs() { return analysisTimeMs; }
        public int getPatternsDetected() { return patternsDetected; }
        public boolean hasError() { return hasError; }
        public boolean hasHighRiskPatterns() { return hasHighRiskPatterns; }
        public boolean requiresImmediateAction() { return requiresImmediateAction; }
    }
}