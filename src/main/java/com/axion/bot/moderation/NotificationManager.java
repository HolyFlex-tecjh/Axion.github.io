package com.axion.bot.moderation;

/**
 * Manager class for handling appeal-related notifications
 */
public class NotificationManager {
    private final NotificationConfig config;
    
    public NotificationManager(NotificationConfig config) {
        this.config = config;
    }
    
    public void notifyAppealSubmitted(Appeal appeal, AppealSubmissionResult result) {
        if (config.isNotifySubmission()) {
            // Implementation would send actual notifications
            System.out.println("Appeal submitted notification sent for: " + appeal.getId());
        }
    }
    
    public void notifyAppealDecision(Appeal appeal, AppealReview review, AppealExecutionResult executionResult) {
        if (config.isNotifyDecision()) {
            // Implementation would send actual notifications
            System.out.println("Appeal decision notification sent for: " + appeal.getId() + 
                             " - Decision: " + review.getDecision());
        }
    }
}