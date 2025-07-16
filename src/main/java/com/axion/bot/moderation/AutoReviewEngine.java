package com.axion.bot.moderation;

import java.util.*;

/**
 * Engine class for automatically reviewing appeals
 */
public class AutoReviewEngine {
    private final AutoReviewConfig config;
    
    public AutoReviewEngine(AutoReviewConfig config) {
        this.config = config;
    }
    
    public AutoReviewResult processAppeal(Appeal appeal) {
        AppealAnalysis analysis = appeal.getAnalysis();
        
        // Determine decision based on analysis
        ReviewDecision decision;
        double confidence;
        List<String> factors = new ArrayList<>();
        
        if (analysis.getAutoApprovalConfidence() > config.getConfidenceThreshold()) {
            decision = ReviewDecision.APPROVED;
            confidence = analysis.getAutoApprovalConfidence();
            factors.add("High sincerity score: " + analysis.getSincerityScore());
            factors.add("Supporting evidence provided");
        } else if (analysis.getAutoRejectionConfidence() > config.getConfidenceThreshold()) {
            decision = ReviewDecision.REJECTED;
            confidence = analysis.getAutoRejectionConfidence();
            factors.add("Low sincerity score: " + analysis.getSincerityScore());
            factors.add("Insufficient evidence");
        } else {
            // Default to rejection for auto-review if not confident
            decision = ReviewDecision.REJECTED;
            confidence = 0.6;
            factors.add("Insufficient confidence for auto-approval");
        }
        
        // Override for bans if configured
        if (config.isRequireHumanReviewForBans() && appeal.getViolationId().contains("BAN")) {
            decision = ReviewDecision.REJECTED;
            confidence = 0.5;
            factors.clear();
            factors.add("Ban appeals require human review");
        }
        
        String reason = generateAutoReviewReason(decision, factors);
        
        return new AutoReviewResult(decision, confidence, reason, factors);
    }
    
    private String generateAutoReviewReason(ReviewDecision decision, List<String> factors) {
        StringBuilder reason = new StringBuilder();
        reason.append("Auto-review decision: ").append(decision.name().toLowerCase()).append(". ");
        reason.append("Factors: ").append(String.join(", ", factors));
        return reason.toString();
    }
}