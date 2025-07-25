package com.axion.bot.moderation;

/**
 * Configuration class for workflow settings
 */
public class WorkflowConfig {
    private int maxConcurrentReviews = 5;
    private int reviewTimeoutHours = 48;
    private boolean enablePriorityReview = true;
    private boolean autoAssignReviewers = true;
    
    public WorkflowConfig() {}
    
    public int getMaxConcurrentReviews() { return maxConcurrentReviews; }
    public void setMaxConcurrentReviews(int maxConcurrentReviews) { this.maxConcurrentReviews = maxConcurrentReviews; }
    
    public int getReviewTimeoutHours() { return reviewTimeoutHours; }
    public void setReviewTimeoutHours(int reviewTimeoutHours) { this.reviewTimeoutHours = reviewTimeoutHours; }
    
    public boolean isEnablePriorityReview() { return enablePriorityReview; }
    public void setEnablePriorityReview(boolean enablePriorityReview) { this.enablePriorityReview = enablePriorityReview; }
    
    public boolean isAutoAssignReviewers() { return autoAssignReviewers; }
    public void setAutoAssignReviewers(boolean autoAssignReviewers) { this.autoAssignReviewers = autoAssignReviewers; }
    
    public boolean isPriorityQueueEnabled() { return enablePriorityReview; }
}
