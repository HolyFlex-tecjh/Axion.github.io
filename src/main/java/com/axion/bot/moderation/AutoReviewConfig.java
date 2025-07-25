package com.axion.bot.moderation;

/**
 * Configuration class for auto review settings
 */
public class AutoReviewConfig {
    private boolean enabled = true;
    private double autoApprovalThreshold = 0.8;
    private double autoRejectionThreshold = 0.8;
    private boolean requireHumanReviewForBans = true;
    private int maxAutoApprovalsPerHour = 10;
    
    public AutoReviewConfig() {}
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public double getAutoApprovalThreshold() { return autoApprovalThreshold; }
    public void setAutoApprovalThreshold(double autoApprovalThreshold) { this.autoApprovalThreshold = autoApprovalThreshold; }
    
    public double getAutoRejectionThreshold() { return autoRejectionThreshold; }
    public void setAutoRejectionThreshold(double autoRejectionThreshold) { this.autoRejectionThreshold = autoRejectionThreshold; }
    
    public boolean isRequireHumanReviewForBans() { return requireHumanReviewForBans; }
    public void setRequireHumanReviewForBans(boolean requireHumanReviewForBans) { this.requireHumanReviewForBans = requireHumanReviewForBans; }
    
    public int getMaxAutoApprovalsPerHour() { return maxAutoApprovalsPerHour; }
    public void setMaxAutoApprovalsPerHour(int maxAutoApprovalsPerHour) { this.maxAutoApprovalsPerHour = maxAutoApprovalsPerHour; }
    
    public double getConfidenceThreshold() { return autoApprovalThreshold; }
}
