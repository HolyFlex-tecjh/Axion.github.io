package com.axion.bot.moderation;

import java.time.Instant;

/**
 * Repræsenterer en appel anmodning fra en bruger
 */
public class AppealRequest {
    private final String userId;
    private final String userName;
    private final String guildId;
    private final String reason;
    private final Instant timestamp;
    private final String originalAction;
    private final String originalReason;
    private final String violationId;
    private final java.util.List<String> evidence;
    private AppealStatus status;
    private String reviewerId;
    private String reviewerName;
    private String reviewNotes;
    private Instant reviewTimestamp;
    
    public enum AppealStatus {
        PENDING("⏳", "Afventer behandling"),
        APPROVED("✅", "Godkendt"),
        DENIED("❌", "Afvist"),
        UNDER_REVIEW("🔍", "Under behandling");
        
        private final String emoji;
        private final String description;
        
        AppealStatus(String emoji, String description) {
            this.emoji = emoji;
            this.description = description;
        }
        
        public String getEmoji() { return emoji; }
        public String getDescription() { return description; }
    }
    
    public AppealRequest(String userId, String userName, String guildId, String reason, 
                        String originalAction, String originalReason) {
        this.userId = userId;
        this.userName = userName;
        this.guildId = guildId;
        this.reason = reason;
        this.originalAction = originalAction;
        this.originalReason = originalReason;
        this.violationId = null; // Default for backward compatibility
        this.evidence = new java.util.ArrayList<>(); // Default empty list
        this.timestamp = Instant.now();
        this.status = AppealStatus.PENDING;
    }
    
    public AppealRequest(String userId, String guildId, String violationId, 
                        String reason, java.util.List<String> evidence) {
        this.userId = userId;
        this.userName = null; // Will need to be looked up
        this.guildId = guildId;
        this.violationId = violationId;
        this.reason = reason;
        this.evidence = evidence != null ? new java.util.ArrayList<>(evidence) : new java.util.ArrayList<>();
        this.originalAction = null; // Will need to be looked up from violation
        this.originalReason = null; // Will need to be looked up from violation
        this.timestamp = Instant.now();
        this.status = AppealStatus.PENDING;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getGuildId() { return guildId; }
    public String getReason() { return reason; }
    public Instant getTimestamp() { return timestamp; }
    public String getOriginalAction() { return originalAction; }
    public String getOriginalReason() { return originalReason; }
    public AppealStatus getStatus() { return status; }
    public String getReviewerId() { return reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public String getReviewNotes() { return reviewNotes; }
    public Instant getReviewTimestamp() { return reviewTimestamp; }
    public String getViolationId() { return violationId; }
    public java.util.List<String> getEvidence() { return new java.util.ArrayList<>(evidence); }
    
    // Setters for review
    public void setStatus(AppealStatus status) { this.status = status; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public void setReviewTimestamp(Instant reviewTimestamp) { this.reviewTimestamp = reviewTimestamp; }
    
    /**
     * Markerer appellen som behandlet
     */
    public void markAsReviewed(String reviewerId, String reviewerName, AppealStatus newStatus, String notes) {
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.status = newStatus;
        this.reviewNotes = notes;
        this.reviewTimestamp = Instant.now();
    }
    
    /**
     * Returnerer en kort beskrivelse af appellen
     */
    public String getShortDescription() {
        return String.format("%s %s appel for %s", 
            status.getEmoji(), 
            status.getDescription().toLowerCase(), 
            originalAction.toLowerCase());
    }
    
    /**
     * Returnerer en detaljeret beskrivelse af appellen
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Appel Detaljer:**\n");
        sb.append("👤 **Bruger:** ").append(userName).append(" (").append(userId).append(")\n");
        sb.append("⚖️ **Original Handling:** ").append(originalAction).append("\n");
        sb.append("📝 **Original Årsag:** ").append(originalReason).append("\n");
        sb.append("💬 **Appel Årsag:** ").append(reason).append("\n");
        sb.append("📅 **Indsendt:** <t:").append(timestamp.getEpochSecond()).append(":F>\n");
        sb.append("📊 **Status:** ").append(status.getEmoji()).append(" ").append(status.getDescription());
        
        if (reviewTimestamp != null) {
            sb.append("\n\n**Behandling:**\n");
            sb.append("👨‍⚖️ **Behandlet af:** ").append(reviewerName).append("\n");
            sb.append("📅 **Behandlet:** <t:").append(reviewTimestamp.getEpochSecond()).append(":F>\n");
            if (reviewNotes != null && !reviewNotes.isEmpty()) {
                sb.append("📋 **Noter:** ").append(reviewNotes);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Tjekker om appellen er afventende
     */
    public boolean isPending() {
        return status == AppealStatus.PENDING || status == AppealStatus.UNDER_REVIEW;
    }
    
    /**
     * Tjekker om appellen er færdigbehandlet
     */
    public boolean isResolved() {
        return status == AppealStatus.APPROVED || status == AppealStatus.DENIED;
    }
}