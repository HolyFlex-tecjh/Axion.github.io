package com.axion.bot.moderation;

/**
 * Configuration class for notification settings
 */
public class NotificationConfig {
    private boolean notifySubmission = true;
    private boolean notifyDecision = true;
    private boolean notifyModerators = true;
    
    public boolean isNotifySubmission() { return notifySubmission; }
    public boolean isNotifyDecision() { return notifyDecision; }
    public boolean isNotifyModerators() { return notifyModerators; }
    
    public void setNotifySubmission(boolean notifySubmission) {
        this.notifySubmission = notifySubmission;
    }
    
    public void setNotifyDecision(boolean notifyDecision) {
        this.notifyDecision = notifyDecision;
    }
    
    public void setNotifyModerators(boolean notifyModerators) {
        this.notifyModerators = notifyModerators;
    }
}