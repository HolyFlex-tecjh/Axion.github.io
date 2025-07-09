package com.axion.bot.moderation;

/**
 * Klasse der indeholder moderation statistikker
 */
public class ModerationStats {
    private final int totalTrackedUsers;
    private final int activeViolations;
    private final int activeTempBans;
    private final int totalModerationActions;
    
    public ModerationStats(int totalTrackedUsers, int activeViolations, int activeTempBans, int totalModerationActions) {
        this.totalTrackedUsers = totalTrackedUsers;
        this.activeViolations = activeViolations;
        this.activeTempBans = activeTempBans;
        this.totalModerationActions = totalModerationActions;
    }
    
    public int getTotalTrackedUsers() {
        return totalTrackedUsers;
    }
    
    public int getActiveViolations() {
        return activeViolations;
    }
    
    public int getActiveTempBans() {
        return activeTempBans;
    }
    
    public int getTotalModerationActions() {
        return totalModerationActions;
    }
    
    /**
     * Beregner violation rate som procent
     */
    public double getViolationRate() {
        if (totalTrackedUsers == 0) return 0.0;
        return (double) activeViolations / totalTrackedUsers * 100;
    }
    
    /**
     * Beregner temp ban rate som procent
     */
    public double getTempBanRate() {
        if (totalTrackedUsers == 0) return 0.0;
        return (double) activeTempBans / totalTrackedUsers * 100;
    }
    
    @Override
    public String toString() {
        return String.format("ModerationStats{users=%d, violations=%d, tempBans=%d, actions=%d}",
                totalTrackedUsers, activeViolations, activeTempBans, totalModerationActions);
    }
}