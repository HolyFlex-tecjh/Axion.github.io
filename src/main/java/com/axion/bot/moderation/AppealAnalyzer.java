package com.axion.bot.moderation;

import java.util.*;

/**
 * Analyzer class for processing and analyzing appeals
 */
public class AppealAnalyzer {
    private final AppealAnalyzerConfig config;
    
    public AppealAnalyzer(AppealAnalyzerConfig config) {
        this.config = config;
    }
    
    public AppealAnalysis analyze(Appeal appeal) {
        // Simplified analysis implementation
        double sincerityScore = analyzeSincerity(appeal.getReason());
        double complexityScore = analyzeComplexity(appeal);
        
        // Determine auto-processing confidence
        double autoApprovalConfidence = calculateAutoApprovalConfidence(appeal, sincerityScore);
        double autoRejectionConfidence = calculateAutoRejectionConfidence(appeal, sincerityScore);
        
        // Check if priority case
        boolean priorityCase = isPriorityCase(appeal);
        
        // Sentiment analysis
        Map<String, Double> sentimentScores = analyzeSentiment(appeal.getReason());
        
        // Pattern detection
        List<String> detectedPatterns = detectPatterns(appeal.getReason());
        
        // Risk factors
        List<String> riskFactors = identifyRiskFactors(appeal);
        
        String analysisNotes = generateAnalysisNotes(sincerityScore, complexityScore, detectedPatterns, riskFactors);
        
        return new AppealAnalysis(
            sincerityScore,
            complexityScore,
            autoApprovalConfidence,
            autoRejectionConfidence,
            priorityCase,
            sentimentScores,
            detectedPatterns,
            riskFactors,
            analysisNotes
        );
    }
    
    private double analyzeSincerity(String reason) {
        // Simplified sincerity analysis
        double score = 0.5; // Base score
        
        // Check for apologetic language
        if (reason.toLowerCase().contains("sorry") || reason.toLowerCase().contains("apologize")) {
            score += 0.2;
        }
        
        // Check for responsibility acceptance
        if (reason.toLowerCase().contains("my fault") || reason.toLowerCase().contains("i was wrong")) {
            score += 0.2;
        }
        
        // Check for detailed explanation
        if (reason.length() > 200) {
            score += 0.1;
        }
        
        return Math.min(1.0, score);
    }
    
    private double analyzeComplexity(Appeal appeal) {
        double complexity = 0.0;
        
        // Multiple evidence pieces increase complexity
        if (appeal.getEvidence().size() > 2) {
            complexity += 0.3;
        }
        
        // Long appeals are more complex
        if (appeal.getReason().length() > 500) {
            complexity += 0.2;
        }
        
        // Check for multiple violation references
        if (appeal.getReason().toLowerCase().contains("multiple") || 
            appeal.getReason().toLowerCase().contains("several")) {
            complexity += 0.3;
        }
        
        return Math.min(1.0, complexity);
    }
    
    private double calculateAutoApprovalConfidence(Appeal appeal, double sincerityScore) {
        if (sincerityScore > config.getSincerityThreshold() && 
            appeal.getEvidence().size() > 0) {
            return 0.8;
        }
        return 0.3;
    }
    
    private double calculateAutoRejectionConfidence(Appeal appeal, double sincerityScore) {
        if (sincerityScore < 0.3 && appeal.getEvidence().isEmpty()) {
            return 0.7;
        }
        return 0.2;
    }
    
    private boolean isPriorityCase(Appeal appeal) {
        // Ban appeals are priority
        return appeal.getViolationId().contains("BAN");
    }
    
    private Map<String, Double> analyzeSentiment(String text) {
        Map<String, Double> sentiments = new HashMap<>();
        sentiments.put("positive", 0.6);
        sentiments.put("negative", 0.2);
        sentiments.put("neutral", 0.2);
        return sentiments;
    }
    
    private List<String> detectPatterns(String text) {
        List<String> patterns = new ArrayList<>();
        if (text.toLowerCase().contains("won't happen again")) {
            patterns.add("Promise of improvement");
        }
        if (text.toLowerCase().contains("misunderstanding")) {
            patterns.add("Claims misunderstanding");
        }
        return patterns;
    }
    
    private List<String> identifyRiskFactors(Appeal appeal) {
        List<String> risks = new ArrayList<>();
        if (appeal.getEvidence().isEmpty()) {
            risks.add("No supporting evidence provided");
        }
        if (appeal.getReason().length() < 100) {
            risks.add("Very brief explanation");
        }
        return risks;
    }
    
    private String generateAnalysisNotes(double sincerityScore, double complexityScore, 
                                        List<String> patterns, List<String> risks) {
        StringBuilder notes = new StringBuilder();
        notes.append(String.format("Sincerity: %.2f, Complexity: %.2f. ", sincerityScore, complexityScore));
        
        if (!patterns.isEmpty()) {
            notes.append("Patterns: ").append(String.join(", ", patterns)).append(". ");
        }
        
        if (!risks.isEmpty()) {
            notes.append("Risks: ").append(String.join(", ", risks)).append(".");
        }
        
        return notes.toString();
    }
}