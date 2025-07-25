package com.axion.bot.moderation;

/**
 * Model class representing statistics for a user's appeals
 */
public class UserAppealStats {
    private final int totalAppeals;
    private final int approvedAppeals;
    private final int rejectedAppeals;
    private final int pendingAppeals;
    
    public UserAppealStats(int totalAppeals, int approvedAppeals, int rejectedAppeals, int pendingAppeals) {
        this.totalAppeals = totalAppeals;
        this.approvedAppeals = approvedAppeals;
        this.rejectedAppeals = rejectedAppeals;
        this.pendingAppeals = pendingAppeals;
    }
    
    // Getters
    public int getTotalAppeals() { return totalAppeals; }
    public int getApprovedAppeals() { return approvedAppeals; }
    public int getRejectedAppeals() { return rejectedAppeals; }
    public int getPendingAppeals() { return pendingAppeals; }
    
    public double getApprovalRate() {
        return totalAppeals > 0 ? (double) approvedAppeals / totalAppeals : 0.0;
    }
}