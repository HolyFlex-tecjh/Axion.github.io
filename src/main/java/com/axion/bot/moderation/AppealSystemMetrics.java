package com.axion.bot.moderation;

/**
 * Model class representing system-wide metrics for the appeal system
 */
public class AppealSystemMetrics {
    private final long totalAppealsProcessed;
    private final long approvedAppeals;
    private final long rejectedAppeals;
    private final long autoProcessedAppeals;
    private final long activeAppeals;
    private final long cachedAnalyses;
    
    public AppealSystemMetrics(long totalAppealsProcessed, long approvedAppeals, long rejectedAppeals,
                              long autoProcessedAppeals, long activeAppeals, long cachedAnalyses) {
        this.totalAppealsProcessed = totalAppealsProcessed;
        this.approvedAppeals = approvedAppeals;
        this.rejectedAppeals = rejectedAppeals;
        this.autoProcessedAppeals = autoProcessedAppeals;
        this.activeAppeals = activeAppeals;
        this.cachedAnalyses = cachedAnalyses;
    }
    
    // Getters
    public long getTotalAppealsProcessed() { return totalAppealsProcessed; }
    public long getApprovedAppeals() { return approvedAppeals; }
    public long getRejectedAppeals() { return rejectedAppeals; }
    public long getAutoProcessedAppeals() { return autoProcessedAppeals; }
    public long getActiveAppeals() { return activeAppeals; }
    public long getCachedAnalyses() { return cachedAnalyses; }
    
    public double getApprovalRate() {
        long totalResolved = approvedAppeals + rejectedAppeals;
        return totalResolved > 0 ? (double) approvedAppeals / totalResolved : 0.0;
    }
    
    public double getAutoProcessingRate() {
        return totalAppealsProcessed > 0 ? (double) autoProcessedAppeals / totalAppealsProcessed : 0.0;
    }
}