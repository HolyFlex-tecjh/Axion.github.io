package com.axion.bot.moderation;

/**
 * Advanced configuration for the moderation system
 * Extends the basic ModerationConfig with additional features
 */
public class AdvancedModerationConfig extends ModerationConfig {
    
    // AI and Machine Learning
    private boolean aiToxicityDetectionEnabled = true;
    private double toxicityThreshold = 0.7;
    private boolean contextAwareModeration = true;
    private boolean learningModeEnabled = false;
    
    // Advanced Spam Detection
    private boolean advancedSpamDetectionEnabled = true;
    private boolean crossChannelSpamDetection = true;
    private boolean imageSpamDetectionEnabled = true;
    private boolean linkSpamDetectionEnabled = true;
    private int rapidMessageThreshold = 5;
    private long rapidMessageTimeframe = 10000; // 10 seconds
    
    // Raid Protection
    private boolean advancedRaidProtectionEnabled = true;
    private int raidJoinThreshold = 5;
    private long raidJoinTimeframe = 30000; // 30 seconds
    private boolean autoLockdownOnRaid = true;
    private int lockdownDurationMinutes = 10;
    private boolean suspiciousAccountDetection = true;
    private int minAccountAgeHours = 24;
    
    // Threat Intelligence
    private boolean threatIntelligenceEnabled = true;
    private boolean phishingDetectionEnabled = true;
    private boolean malwareDetectionEnabled = true;
    private boolean scamDetectionEnabled = true;
    private boolean ipReputationCheckEnabled = false;
    
    // Advanced Timeout System
    private boolean smartTimeoutEnabled = true;
    private int baseTimeoutMinutes = 5;
    private double timeoutEscalationMultiplier = 2.0;
    private int maxTimeoutHours = 24;
    private boolean timeoutAppealSystemEnabled = true;
    
    // User Behavior Analysis
    private boolean behaviorAnalysisEnabled = true;
    private boolean trustLevelSystemEnabled = true;
    private boolean suspicionTrackingEnabled = true;
    private int trustLevelDecayDays = 30;
    
    // Custom Rules Engine
    private boolean customRulesEnabled = true;
    private int maxCustomRulesPerGuild = 50;
    private boolean regexRulesEnabled = true;
    private boolean wordFilterRulesEnabled = true;
    
    // Audit and Compliance
    private boolean detailedAuditLoggingEnabled = true;
    private boolean gdprComplianceMode = false;
    private int auditLogRetentionDays = 90;
    private boolean anonymizeLogsAfterRetention = true;
    
    // Performance and Scaling
    private boolean asyncProcessingEnabled = true;
    private int maxConcurrentAnalyses = 10;
    private boolean cacheUserProfilesEnabled = true;
    private int userProfileCacheSize = 10000;
    
    // User Notifications
    private boolean sendUserNotifications = true;
    
    public AdvancedModerationConfig() {
        super();
        // Enable advanced features by default
        setAdvancedFeaturesEnabled(true);
        setTempBanEnabled(true);
        setEscalationEnabled(true);
        setAttachmentScanningEnabled(true);
        setAdvancedSpamDetectionEnabled(true);
        setThreatIntelligenceEnabled(true);
        setAntiRaidEnabled(true);
        setSmartTimeoutEnabled(true);
    }
    
    // AI and Machine Learning getters/setters
    public boolean isAiToxicityDetectionEnabled() { return aiToxicityDetectionEnabled; }
    public void setAiToxicityDetectionEnabled(boolean enabled) { this.aiToxicityDetectionEnabled = enabled; }
    
    public double getToxicityThreshold() { return toxicityThreshold; }
    public void setToxicityThreshold(double threshold) { 
        this.toxicityThreshold = Math.max(0.0, Math.min(1.0, threshold)); 
    }
    
    public boolean isContextAwareModeration() { return contextAwareModeration; }
    public void setContextAwareModeration(boolean enabled) { this.contextAwareModeration = enabled; }
    
    public boolean isLearningModeEnabled() { return learningModeEnabled; }
    public void setLearningModeEnabled(boolean enabled) { this.learningModeEnabled = enabled; }
    
    // Advanced Spam Detection getters/setters
    public boolean isCrossChannelSpamDetection() { return crossChannelSpamDetection; }
    public void setCrossChannelSpamDetection(boolean enabled) { this.crossChannelSpamDetection = enabled; }
    
    public boolean isImageSpamDetectionEnabled() { return imageSpamDetectionEnabled; }
    public void setImageSpamDetectionEnabled(boolean enabled) { this.imageSpamDetectionEnabled = enabled; }
    
    public boolean isLinkSpamDetectionEnabled() { return linkSpamDetectionEnabled; }
    public void setLinkSpamDetectionEnabled(boolean enabled) { this.linkSpamDetectionEnabled = enabled; }
    
    public int getRapidMessageThreshold() { return rapidMessageThreshold; }
    public void setRapidMessageThreshold(int threshold) { 
        this.rapidMessageThreshold = Math.max(1, threshold); 
    }
    
    public long getRapidMessageTimeframe() { return rapidMessageTimeframe; }
    public void setRapidMessageTimeframe(long timeframe) { 
        this.rapidMessageTimeframe = Math.max(1000, timeframe); 
    }
    
    // Raid Protection getters/setters
    public boolean isAdvancedRaidProtectionEnabled() { return advancedRaidProtectionEnabled; }
    public void setAdvancedRaidProtectionEnabled(boolean enabled) { this.advancedRaidProtectionEnabled = enabled; }
    
    public int getRaidJoinThreshold() { return raidJoinThreshold; }
    public void setRaidJoinThreshold(int threshold) { this.raidJoinThreshold = Math.max(1, threshold); }
    
    public long getRaidJoinTimeframe() { return raidJoinTimeframe; }
    public void setRaidJoinTimeframe(long timeframe) { this.raidJoinTimeframe = Math.max(1000, timeframe); }
    
    public boolean isAutoLockdownOnRaid() { return autoLockdownOnRaid; }
    public void setAutoLockdownOnRaid(boolean enabled) { this.autoLockdownOnRaid = enabled; }
    
    public int getLockdownDurationMinutes() { return lockdownDurationMinutes; }
    public void setLockdownDurationMinutes(int minutes) { 
        this.lockdownDurationMinutes = Math.max(1, minutes); 
    }
    
    public boolean isSuspiciousAccountDetection() { return suspiciousAccountDetection; }
    public void setSuspiciousAccountDetection(boolean enabled) { this.suspiciousAccountDetection = enabled; }
    
    public int getMinAccountAgeHours() { return minAccountAgeHours; }
    public void setMinAccountAgeHours(int hours) { this.minAccountAgeHours = Math.max(0, hours); }
    
    // Threat Intelligence getters/setters
    public boolean isIpReputationCheckEnabled() { return ipReputationCheckEnabled; }
    public void setIpReputationCheckEnabled(boolean enabled) { this.ipReputationCheckEnabled = enabled; }
    
    // Advanced Timeout System getters/setters
    public double getTimeoutEscalationMultiplier() { return timeoutEscalationMultiplier; }
    public void setTimeoutEscalationMultiplier(double multiplier) { 
        this.timeoutEscalationMultiplier = Math.max(1.0, multiplier); 
    }
    
    public boolean isTimeoutAppealSystemEnabled() { return timeoutAppealSystemEnabled; }
    public void setTimeoutAppealSystemEnabled(boolean enabled) { this.timeoutAppealSystemEnabled = enabled; }
    
    // User Behavior Analysis getters/setters
    public boolean isBehaviorAnalysisEnabled() { return behaviorAnalysisEnabled; }
    public void setBehaviorAnalysisEnabled(boolean enabled) { this.behaviorAnalysisEnabled = enabled; }
    
    public boolean isTrustLevelSystemEnabled() { return trustLevelSystemEnabled; }
    public void setTrustLevelSystemEnabled(boolean enabled) { this.trustLevelSystemEnabled = enabled; }
    
    public boolean isSuspicionTrackingEnabled() { return suspicionTrackingEnabled; }
    public void setSuspicionTrackingEnabled(boolean enabled) { this.suspicionTrackingEnabled = enabled; }
    
    public int getTrustLevelDecayDays() { return trustLevelDecayDays; }
    public void setTrustLevelDecayDays(int days) { this.trustLevelDecayDays = Math.max(1, days); }
    
    // Custom Rules Engine getters/setters
    public boolean isCustomRulesEnabled() { return customRulesEnabled; }
    public void setCustomRulesEnabled(boolean enabled) { this.customRulesEnabled = enabled; }
    
    public int getMaxCustomRulesPerGuild() { return maxCustomRulesPerGuild; }
    public void setMaxCustomRulesPerGuild(int max) { this.maxCustomRulesPerGuild = Math.max(1, max); }
    
    public boolean isRegexRulesEnabled() { return regexRulesEnabled; }
    public void setRegexRulesEnabled(boolean enabled) { this.regexRulesEnabled = enabled; }
    
    public boolean isWordFilterRulesEnabled() { return wordFilterRulesEnabled; }
    public void setWordFilterRulesEnabled(boolean enabled) { this.wordFilterRulesEnabled = enabled; }
    
    // Audit and Compliance getters/setters
    public boolean isDetailedAuditLoggingEnabled() { return detailedAuditLoggingEnabled; }
    public void setDetailedAuditLoggingEnabled(boolean enabled) { this.detailedAuditLoggingEnabled = enabled; }
    
    public boolean isGdprComplianceMode() { return gdprComplianceMode; }
    public void setGdprComplianceMode(boolean enabled) { this.gdprComplianceMode = enabled; }
    
    public int getAuditLogRetentionDays() { return auditLogRetentionDays; }
    public void setAuditLogRetentionDays(int days) { this.auditLogRetentionDays = Math.max(1, days); }
    
    public boolean isAnonymizeLogsAfterRetention() { return anonymizeLogsAfterRetention; }
    public void setAnonymizeLogsAfterRetention(boolean enabled) { this.anonymizeLogsAfterRetention = enabled; }
    
    // Performance and Scaling getters/setters
    public boolean isAsyncProcessingEnabled() { return asyncProcessingEnabled; }
    public void setAsyncProcessingEnabled(boolean enabled) { this.asyncProcessingEnabled = enabled; }
    
    public int getMaxConcurrentAnalyses() { return maxConcurrentAnalyses; }
    public void setMaxConcurrentAnalyses(int max) { this.maxConcurrentAnalyses = Math.max(1, max); }
    
    public boolean isCacheUserProfilesEnabled() { return cacheUserProfilesEnabled; }
    public void setCacheUserProfilesEnabled(boolean enabled) { this.cacheUserProfilesEnabled = enabled; }
    
    public int getUserProfileCacheSize() { return userProfileCacheSize; }
    public void setUserProfileCacheSize(int size) { this.userProfileCacheSize = Math.max(100, size); }
    
    // User Notifications getters/setters
    public boolean isSendUserNotifications() { return sendUserNotifications; }
    public void setSendUserNotifications(boolean enabled) { this.sendUserNotifications = enabled; }
    
    /**
     * Create a default advanced configuration
     */
    public static AdvancedModerationConfig createAdvancedDefault() {
        return new AdvancedModerationConfig();
    }
    
    /**
     * Create a strict advanced configuration
     */
    public static AdvancedModerationConfig createAdvancedStrict() {
        AdvancedModerationConfig config = new AdvancedModerationConfig();
        config.setToxicityThreshold(0.5);
        config.setRapidMessageThreshold(3);
        config.setRaidJoinThreshold(3);
        config.setMinAccountAgeHours(48);
        config.setTimeoutEscalationMultiplier(2.5);
        return config;
    }
    
    /**
     * Create a lenient advanced configuration
     */
    public static AdvancedModerationConfig createAdvancedLenient() {
        AdvancedModerationConfig config = new AdvancedModerationConfig();
        config.setAiToxicityDetectionEnabled(false);
        config.setAdvancedRaidProtectionEnabled(false);
        config.setToxicityThreshold(0.9);
        config.setRapidMessageThreshold(10);
        config.setRaidJoinThreshold(10);
        config.setMinAccountAgeHours(0);
        return config;
    }
}