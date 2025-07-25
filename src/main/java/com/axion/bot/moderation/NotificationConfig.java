package com.axion.bot.moderation;

/**
 * Configuration class for notifications
 */
public class NotificationConfig {
    private boolean notifySubmission = true;
    private boolean notifyDecision = true;
    private boolean notifyStatusUpdates = true;
    private int notificationDelaySeconds = 0;
    
    public NotificationConfig() {}
    
    public boolean isNotifySubmission() { return notifySubmission; }
    public void setNotifySubmission(boolean notifySubmission) { this.notifySubmission = notifySubmission; }
    
    public boolean isNotifyDecision() { return notifyDecision; }
    public void setNotifyDecision(boolean notifyDecision) { this.notifyDecision = notifyDecision; }
    
    public boolean isNotifyStatusUpdates() { return notifyStatusUpdates; }
    public void setNotifyStatusUpdates(boolean notifyStatusUpdates) { this.notifyStatusUpdates = notifyStatusUpdates; }
    
    public int getNotificationDelaySeconds() { return notificationDelaySeconds; }
    public void setNotificationDelaySeconds(int notificationDelaySeconds) { this.notificationDelaySeconds = notificationDelaySeconds; }
}
