package com.axion.bot.moderation;

/**
 * Metrics class for auto moderation system performance tracking
 */
public class AutoModerationMetrics {
    private final long totalEvaluations;
    private final long rulesTriggered;
    private final long actionsExecuted;
    private final int activeRulesCount;
    private final long evaluationCacheSize;
    private final long actionHistorySize;
    
    public AutoModerationMetrics(long totalEvaluations, long rulesTriggered, long actionsExecuted,
                                int activeRulesCount, long evaluationCacheSize, long actionHistorySize) {
        this.totalEvaluations = totalEvaluations;
        this.rulesTriggered = rulesTriggered;
        this.actionsExecuted = actionsExecuted;
        this.activeRulesCount = activeRulesCount;
        this.evaluationCacheSize = evaluationCacheSize;
        this.actionHistorySize = actionHistorySize;
    }
    
    // Getters
    public long getTotalEvaluations() { return totalEvaluations; }
    public long getRulesTriggered() { return rulesTriggered; }
    public long getActionsExecuted() { return actionsExecuted; }
    public int getActiveRulesCount() { return activeRulesCount; }
    public long getEvaluationCacheSize() { return evaluationCacheSize; }
    public long getActionHistorySize() { return actionHistorySize; }
    
    /**
     * Calculate the rule trigger rate
     */
    public double getRuleTriggerRate() {
        return totalEvaluations > 0 ? (double) rulesTriggered / totalEvaluations : 0.0;
    }
    
    /**
     * Calculate the action execution rate
     */
    public double getActionExecutionRate() {
        return rulesTriggered > 0 ? (double) actionsExecuted / rulesTriggered : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("AutoModerationMetrics{totalEvaluations=%d, rulesTriggered=%d, " +
                           "actionsExecuted=%d, activeRulesCount=%d, evaluationCacheSize=%d, " +
                           "actionHistorySize=%d, triggerRate=%.2f%%, executionRate=%.2f%%}",
                           totalEvaluations, rulesTriggered, actionsExecuted, activeRulesCount,
                           evaluationCacheSize, actionHistorySize, 
                           getRuleTriggerRate() * 100, getActionExecutionRate() * 100);
    }
}