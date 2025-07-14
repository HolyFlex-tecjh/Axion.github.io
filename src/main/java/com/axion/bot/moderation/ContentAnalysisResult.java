package com.axion.bot.moderation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of AI content analysis
 */
public class ContentAnalysisResult {
    private final boolean isClean;
    private final double confidence;
    private final List<String> detectedIssues;
    private final Map<String, Double> categoryScores;
    private final String primaryCategory;
    private final String explanation;
    private final Instant analysisTimestamp;
    private final long analysisTimeMs;
    private final String detectedLanguage;
    private final int contentLength;
    
    public ContentAnalysisResult(boolean isClean, double confidence, List<String> detectedIssues,
                                Map<String, Double> categoryScores, String primaryCategory,
                                String explanation, long analysisTimeMs) {
        this.isClean = isClean;
        this.confidence = confidence;
        this.detectedIssues = detectedIssues;
        this.categoryScores = categoryScores;
        this.primaryCategory = primaryCategory;
        this.explanation = explanation;
        this.analysisTimestamp = Instant.now();
        this.analysisTimeMs = analysisTimeMs;
        this.detectedLanguage = "unknown";
        this.contentLength = 0;
    }
    
    // Constructor for AIContentAnalyzer
    public ContentAnalysisResult(boolean isClean, double confidence, String detectionType,
                                List<String> detectionReasons, String detectedLanguage,
                                Map<String, Double> layerScores, int contentLength) {
        this.isClean = isClean;
        this.confidence = confidence;
        this.detectedIssues = detectionReasons;
        this.categoryScores = layerScores;
        this.primaryCategory = detectionType;
        this.explanation = detectionReasons.isEmpty() ? "No issues detected" : String.join(", ", detectionReasons);
        this.analysisTimestamp = Instant.now();
        this.analysisTimeMs = 0;
        this.detectedLanguage = detectedLanguage;
        this.contentLength = contentLength;
    }
    
    // Static factory methods
    public static ContentAnalysisResult clean(double confidence) {
        return new ContentAnalysisResult(true, confidence, List.of(), Map.of(), 
            "clean", "Content appears to be clean", 0);
    }
    
    public static ContentAnalysisResult violation(double confidence, List<String> issues, 
                                                 String primaryCategory, String explanation) {
        return new ContentAnalysisResult(false, confidence, issues, Map.of(), 
            primaryCategory, explanation, 0);
    }
    
    public static ContentAnalysisResult error(String errorMessage) {
        return new ContentAnalysisResult(false, 0.0, List.of("Analysis Error"), Map.of(), 
            "error", errorMessage, 0);
    }
    
    // Getters
    public boolean isClean() { return isClean; }
    public double getConfidence() { return confidence; }
    public List<String> getDetectedIssues() { return detectedIssues; }
    public Map<String, Double> getCategoryScores() { return categoryScores; }
    public String getPrimaryCategory() { return primaryCategory; }
    public String getExplanation() { return explanation; }
    public Instant getAnalysisTimestamp() { return analysisTimestamp; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getDetectedLanguage() { return detectedLanguage; }
    public int getContentLength() { return contentLength; }
    public String getDetectionType() { return primaryCategory; }
    
    /**
     * Get the severity level based on confidence and category
     */
    public ModerationSeverity getSeverity() {
        if (isClean) {
            return ModerationSeverity.NONE;
        }
        
        if (confidence >= 0.9) {
            return ModerationSeverity.VERY_HIGH;
        } else if (confidence >= 0.7) {
            return ModerationSeverity.HIGH;
        } else if (confidence >= 0.5) {
            return ModerationSeverity.MEDIUM;
        } else {
            return ModerationSeverity.LOW;
        }
    }
    
    @Override
    public String toString() {
        return String.format("ContentAnalysisResult{clean=%s, confidence=%.2f, category='%s'}",
            isClean, confidence, primaryCategory);
    }
}