package com.axion.bot.moderation;

/**
 * Main configuration class for the Appeal System
 */
public class AppealSystemConfig {
    private boolean autoReviewEnabled = true;
    private int minAppealLength = 50;
    private int maxAppealLength = 2000;
    private int appealCooldownHours = 24;
    private int maxAppealsPerUser = 3;
    private double autoApprovalThreshold = 0.85;
    private double autoRejectionThreshold = 0.85;
    private double complexityThreshold = 0.7;
    private int baseProcessingHours = 24;
    private int processingTimePerAppeal = 30; // minutes
    private int appealRetentionDays = 90;
    
    // Sub-configurations
    private WorkflowConfig workflowConfig = new WorkflowConfig();
    private AppealAnalyzerConfig analyzerConfig = new AppealAnalyzerConfig();
    private AutoReviewConfig autoReviewConfig = new AutoReviewConfig();
    private NotificationConfig notificationConfig = new NotificationConfig();
    
    // Getters
    public boolean isAutoReviewEnabled() { return autoReviewEnabled; }
    public int getMinAppealLength() { return minAppealLength; }
    public int getMaxAppealLength() { return maxAppealLength; }
    public int getAppealCooldownHours() { return appealCooldownHours; }
    public int getMaxAppealsPerUser() { return maxAppealsPerUser; }
    public double getAutoApprovalThreshold() { return autoApprovalThreshold; }
    public double getAutoRejectionThreshold() { return autoRejectionThreshold; }
    public double getComplexityThreshold() { return complexityThreshold; }
    public int getBaseProcessingHours() { return baseProcessingHours; }
    public int getProcessingTimePerAppeal() { return processingTimePerAppeal; }
    public int getAppealRetentionDays() { return appealRetentionDays; }
    
    public WorkflowConfig getWorkflowConfig() { return workflowConfig; }
    public AppealAnalyzerConfig getAnalyzerConfig() { return analyzerConfig; }
    public AutoReviewConfig getAutoReviewConfig() { return autoReviewConfig; }
    public NotificationConfig getNotificationConfig() { return notificationConfig; }
    
    // Setters
    public void setAutoReviewEnabled(boolean autoReviewEnabled) { this.autoReviewEnabled = autoReviewEnabled; }
    public void setMinAppealLength(int minAppealLength) { this.minAppealLength = minAppealLength; }
    public void setMaxAppealLength(int maxAppealLength) { this.maxAppealLength = maxAppealLength; }
    public void setAppealCooldownHours(int appealCooldownHours) { this.appealCooldownHours = appealCooldownHours; }
    public void setMaxAppealsPerUser(int maxAppealsPerUser) { this.maxAppealsPerUser = maxAppealsPerUser; }
    public void setAutoApprovalThreshold(double autoApprovalThreshold) { this.autoApprovalThreshold = autoApprovalThreshold; }
    public void setAutoRejectionThreshold(double autoRejectionThreshold) { this.autoRejectionThreshold = autoRejectionThreshold; }
    public void setComplexityThreshold(double complexityThreshold) { this.complexityThreshold = complexityThreshold; }
    public void setBaseProcessingHours(int baseProcessingHours) { this.baseProcessingHours = baseProcessingHours; }
    public void setProcessingTimePerAppeal(int processingTimePerAppeal) { this.processingTimePerAppeal = processingTimePerAppeal; }
    public void setAppealRetentionDays(int appealRetentionDays) { this.appealRetentionDays = appealRetentionDays; }
}