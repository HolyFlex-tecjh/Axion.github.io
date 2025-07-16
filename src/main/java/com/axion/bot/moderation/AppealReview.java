package com.axion.bot.moderation;

import java.time.Instant;

/**
 * Model class representing a review of an appeal
 */
public class AppealReview {
    private final String reviewerId;
    private final ReviewDecision decision;
    private final String notes;
    private final Instant reviewedAt;
    
    public AppealReview(String reviewerId, ReviewDecision decision, String notes, Instant reviewedAt) {
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.notes = notes;
        this.reviewedAt = reviewedAt;
    }
    
    // Getters
    public String getReviewerId() { return reviewerId; }
    public ReviewDecision getDecision() { return decision; }
    public String getNotes() { return notes; }
    public Instant getReviewedAt() { return reviewedAt; }
}