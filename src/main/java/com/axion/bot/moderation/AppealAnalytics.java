package com.axion.bot.moderation;

import java.time.Duration;
import java.util.*;

/**
 * Model class representing analytics data for appeals
 */
public class AppealAnalytics {
    private final int totalAppeals;
    private final int approvedAppeals;
    private final int rejectedAppeals;
    private final int pendingAppeals;
    private final double approvalRate;
    private final double averageProcessingTimeMinutes;
    private final double autoProcessingRate;
    private final Map<String, Long> violationTypeBreakdown;
    private final Duration period;
    
    public AppealAnalytics(int totalAppeals, int approvedAppeals, int rejectedAppeals, int pendingAppeals,
                          double approvalRate, double averageProcessingTimeMinutes, double autoProcessingRate,
                          Map<String, Long> violationTypeBreakdown, Duration period) {
        this.totalAppeals = totalAppeals;
        this.approvedAppeals = approvedAppeals;
        this.rejectedAppeals = rejectedAppeals;
        this.pendingAppeals = pendingAppeals;
        this.approvalRate = approvalRate;
        this.averageProcessingTimeMinutes = averageProcessingTimeMinutes;
        this.autoProcessingRate = autoProcessingRate;
        this.violationTypeBreakdown = new HashMap<>(violationTypeBreakdown);
        this.period = period;
    }
    
    // Getters
    public int getTotalAppeals() { return totalAppeals; }
    public int getApprovedAppeals() { return approvedAppeals; }
    public int getRejectedAppeals() { return rejectedAppeals; }
    public int getPendingAppeals() { return pendingAppeals; }
    public double getApprovalRate() { return approvalRate; }
    public double getAverageProcessingTimeMinutes() { return averageProcessingTimeMinutes; }
    public double getAutoProcessingRate() { return autoProcessingRate; }
    public Map<String, Long> getViolationTypeBreakdown() { return new HashMap<>(violationTypeBreakdown); }
    public Duration getPeriod() { return period; }
}