package com.axion.bot.moderation;

/**
 * Class representing user appeal statistics
 */
public class UserAppealStats {
    private final int total;
    private final int approved;
    private final int rejected;
    private final int pending;
    
    public UserAppealStats(int total, int approved, int rejected, int pending) {
        this.total = total;
        this.approved = approved;
        this.rejected = rejected;
        this.pending = pending;
    }
    
    public int getTotal() { return total; }
    public int getApproved() { return approved; }
    public int getRejected() { return rejected; }
    public int getPending() { return pending; }
    
    public double getApprovalRate() {
        return total > 0 ? (double) approved / total : 0.0;
    }
}
