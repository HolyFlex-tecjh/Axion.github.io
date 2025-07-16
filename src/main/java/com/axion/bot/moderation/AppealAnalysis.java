package com.axion.bot.moderation;

import java.util.*;

/**
 * Model class representing the analysis of an appeal
 */
public class AppealAnalysis {
    private final double sincerityScore;
    private final double complexityScore;
    private final double autoApprovalConfidence;
    private final double autoRejectionConfidence;
    private final boolean priorityCase;
    private final Map<String, Double> sentimentScores;
    private final List<String> detectedPatterns;
    private final List<String> riskFactors;
    private final String analysisNotes;
    
    public AppealAnalysis(double sincerityScore, double complexityScore, 
                         double autoApprovalConfidence, double autoRejectionConfidence,
                         boolean priorityCase, Map<String, Double> sentimentScores,
                         List<String> detectedPatterns, List<String> riskFactors,
                         String analysisNotes) {
        this.sincerityScore = sincerityScore;
        this.complexityScore = complexityScore;
        this.autoApprovalConfidence = autoApprovalConfidence;
        this.autoRejectionConfidence = autoRejectionConfidence;
        this.priorityCase = priorityCase;
        this.sentimentScores = new HashMap<>(sentimentScores);
        this.detectedPatterns = new ArrayList<>(detectedPatterns);
        this.riskFactors = new ArrayList<>(riskFactors);
        this.analysisNotes = analysisNotes;
    }
    
    // Getters
    public double getSincerityScore() { return sincerityScore; }
    public double getComplexityScore() { return complexityScore; }
    public double getAutoApprovalConfidence() { return autoApprovalConfidence; }
    public double getAutoRejectionConfidence() { return autoRejectionConfidence; }
    public boolean isPriorityCase() { return priorityCase; }
    public Map<String, Double> getSentimentScores() { return new HashMap<>(sentimentScores); }
    public List<String> getDetectedPatterns() { return new ArrayList<>(detectedPatterns); }
    public List<String> getRiskFactors() { return new ArrayList<>(riskFactors); }
    public String getAnalysisNotes() { return analysisNotes; }
}