package com.axion.bot.moderation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Behavioral Analytics system for analyzing user behavior patterns
 * Acts as a facade for the BehavioralAnalyticsEngine
 */
public class BehavioralAnalytics {
    private final BehavioralAnalyticsEngine engine;
    
    public BehavioralAnalytics() {
        this.engine = new BehavioralAnalyticsEngine(new BehavioralAnalyticsConfig());
    }
    
    public BehavioralAnalytics(BehavioralAnalyticsEngine engine) {
        this.engine = engine;
    }
    
    /**
     * Analyze user behavior based on their profile and recent activities
     */
    public BehaviorAnalysisResult analyzeBehavior(UserModerationProfile profile, List<UserActivity> recentActivities) {
        try {
            // Analyze each activity individually and return the last result
            BehaviorAnalysisResult lastResult = null;
            for (UserActivity activity : recentActivities) {
                lastResult = engine.analyzeUserBehavior(profile.getUserId(), profile.getGuildId(), activity);
            }
            return lastResult != null ? lastResult : BehaviorAnalysisResult.error(profile.getUserId(), profile.getGuildId(), "No activities to analyze");
        } catch (Exception e) {
            // Return error result if analysis fails
            return BehaviorAnalysisResult.error(profile.getUserId(), profile.getGuildId(), 
                "Behavioral analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze user behavior asynchronously
     */
    public CompletableFuture<BehaviorAnalysisResult> analyzeBehaviorAsync(UserModerationProfile profile, 
                                                                          List<UserActivity> recentActivities) {
        return CompletableFuture.supplyAsync(() -> analyzeBehavior(profile, recentActivities));
    }
    
    /**
     * Check if a user's behavior is anomalous based on their recent activity
     */
    public boolean isAnomalousBehavior(UserModerationProfile profile, List<UserActivity> recentActivities) {
        BehaviorAnalysisResult result = analyzeBehavior(profile, recentActivities);
        return result.isAnomalous();
    }
    
    /**
     * Get behavior metrics for a user
     */
    public UserBehaviorMetrics getBehaviorMetrics(String userId) {
        return engine.getBehaviorMetrics(userId);
    }
    
    /**
     * Update user behavior profile with new activity
     */
    public void updateBehaviorProfile(String userId, String guildId, UserActivity activity) {
        engine.updateBehaviorProfile(userId, guildId, activity);
    }
    
    /**
     * Get the underlying analytics engine
     */
    public BehavioralAnalyticsEngine getEngine() {
        return engine;
    }
    
    /**
     * Get analytics metrics
     */
    public BehavioralAnalyticsMetrics getMetrics() {
        return engine.getMetrics();
    }
}