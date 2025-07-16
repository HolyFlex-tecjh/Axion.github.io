package com.axion.bot.moderation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an appeal submitted by a user for a moderation action
 */
public class Appeal {
    private final String id;
    private final String userId;
    private final String guildId;
    private final String violationId;
    private final String reason;
    private final List<String> evidence;
    private AppealStatus status;
    private final Instant submittedAt;
    private Instant resolvedAt;
    private ProcessingPath processingPath;
    private AppealAnalysis analysis;
    private final List<AppealReview> reviews;
    
    public Appeal(String id, String userId, String guildId, String violationId, 
                  String reason, List<String> evidence, AppealStatus status, Instant submittedAt) {
        this.id = id;
        this.userId = userId;
        this.guildId = guildId;
        this.violationId = violationId;
        this.reason = reason;
        this.evidence = evidence != null ? new ArrayList<>(evidence) : new ArrayList<>();
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviews = new ArrayList<>();
    }
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public String getViolationId() { return violationId; }
    public String getReason() { return reason; }
    public List<String> getEvidence() { return new ArrayList<>(evidence); }
    public AppealStatus getStatus() { return status; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public ProcessingPath getProcessingPath() { return processingPath; }
    public AppealAnalysis getAnalysis() { return analysis; }
    public List<AppealReview> getReviews() { return new ArrayList<>(reviews); }
    
    // Setters
    public void setStatus(AppealStatus status) { this.status = status; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setProcessingPath(ProcessingPath processingPath) { this.processingPath = processingPath; }
    public void setAnalysis(AppealAnalysis analysis) { this.analysis = analysis; }
    
    public void addReview(AppealReview review) {
        this.reviews.add(review);
    }
    
    @Override
    public String toString() {
        return "Appeal{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", guildId='" + guildId + '\'' +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                '}';
    }
}