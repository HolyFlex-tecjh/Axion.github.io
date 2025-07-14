package com.axion.bot.moderation;

import java.util.HashSet;
import java.util.Set;

/**
 * Konfigurationsklasse for moderation systemet
 * Indeholder alle indstillinger for auto-moderation funktioner
 */
public class ModerationConfig {
    
    // Spam protection settings
    private boolean spamProtectionEnabled = true;
    private int maxMessagesPerMinute = 5;
    private int maxWarningsBeforeBan = 3;
    
    // Toxic content detection
    private boolean toxicDetectionEnabled = true;
    private boolean autoDeleteToxicContent = true;
    
    // Link protection
    private boolean linkProtectionEnabled = true;
    private int maxLinksPerMessage = 2;
    private boolean allowDiscordInvites = false;
    
    // Custom filters
    private boolean customFiltersEnabled = true;
    
    // Auto-moderation actions
    private boolean autoTimeoutEnabled = true;
    private boolean autoKickEnabled = false;
    private boolean autoBanEnabled = false;
    
    // Advanced features
    private boolean advancedFeaturesEnabled = true;
    private boolean tempBanEnabled = true;
    private int maxTempBanHours = 168; // 1 uge
    private boolean escalationEnabled = true;
    private boolean attachmentScanningEnabled = true;
    private boolean advancedSpamDetectionEnabled = true;
    
    // Violation tracking
    private int violationDecayHours = 24; // Violations forsvinder efter 24 timer
    private boolean autoViolationReset = true;
    
    // Logging
    private boolean logModerationActions = true;
    private String logChannelId = null;
    private String auditChannelId = null;
    private boolean detailedLogging = false;
    private int logRetentionDays = 30;
    
    // Anti-raid protection
    private boolean antiRaidEnabled = true;
    private int raidDetectionThreshold = 5; // users joining within timeframe
    private int raidDetectionTimeframe = 60; // seconds
    private boolean autoLockdownEnabled = true;
    private int lockdownDuration = 300; // seconds
    
    // Threat intelligence
    private boolean threatIntelligenceEnabled = true;
    private boolean phishingDetectionEnabled = true;
    private boolean malwareDetectionEnabled = true;
    private boolean scamDetectionEnabled = true;
    
    // Advanced timeout system
    private boolean smartTimeoutEnabled = true;
    private int baseTimeoutMinutes = 5;
    private double timeoutMultiplier = 2.0;
    private int maxTimeoutHours = 24;
    
    // Behavioral analysis
    private boolean behavioralAnalysisEnabled = true;
    
    // AI detection
    private boolean aiDetectionEnabled = true;
    
    // Smart rules
    private boolean smartRulesEnabled = true;
    
    // Threat intelligence
    private boolean threatIntelEnabled = true;
    
    // Whitelist
    private boolean whitelistEnabled = false;
    private Set<String> whitelistedUsers = new HashSet<>();
    private Set<String> whitelistedRoles = new HashSet<>();
    private Set<String> trustedDomains = new HashSet<>();
    
    // Default constructor
    public ModerationConfig() {
        // Standard indstillinger er allerede sat
    }
    
    // Spam Protection
    public boolean isSpamProtectionEnabled() {
        return spamProtectionEnabled;
    }
    
    public void setSpamProtectionEnabled(boolean spamProtectionEnabled) {
        this.spamProtectionEnabled = spamProtectionEnabled;
    }
    
    public int getMaxMessagesPerMinute() {
        return maxMessagesPerMinute;
    }
    
    public void setMaxMessagesPerMinute(int maxMessagesPerMinute) {
        this.maxMessagesPerMinute = maxMessagesPerMinute;
    }
    
    public int getMaxWarningsBeforeBan() {
        return maxWarningsBeforeBan;
    }
    
    public void setMaxWarningsBeforeBan(int maxWarningsBeforeBan) {
        this.maxWarningsBeforeBan = maxWarningsBeforeBan;
    }
    
    // Toxic Detection
    public boolean isToxicDetectionEnabled() {
        return toxicDetectionEnabled;
    }
    
    public void setToxicDetectionEnabled(boolean toxicDetectionEnabled) {
        this.toxicDetectionEnabled = toxicDetectionEnabled;
    }
    
    public boolean isAutoDeleteToxicContent() {
        return autoDeleteToxicContent;
    }
    
    public void setAutoDeleteToxicContent(boolean autoDeleteToxicContent) {
        this.autoDeleteToxicContent = autoDeleteToxicContent;
    }
    
    // Link Protection
    public boolean isLinkProtectionEnabled() {
        return linkProtectionEnabled;
    }
    
    public void setLinkProtectionEnabled(boolean linkProtectionEnabled) {
        this.linkProtectionEnabled = linkProtectionEnabled;
    }
    
    public int getMaxLinksPerMessage() {
        return maxLinksPerMessage;
    }
    
    public void setMaxLinksPerMessage(int maxLinksPerMessage) {
        this.maxLinksPerMessage = maxLinksPerMessage;
    }
    
    public boolean isAllowDiscordInvites() {
        return allowDiscordInvites;
    }
    
    public void setAllowDiscordInvites(boolean allowDiscordInvites) {
        this.allowDiscordInvites = allowDiscordInvites;
    }
    
    // Custom Filters
    public boolean isCustomFiltersEnabled() {
        return customFiltersEnabled;
    }
    
    public void setCustomFiltersEnabled(boolean customFiltersEnabled) {
        this.customFiltersEnabled = customFiltersEnabled;
    }
    
    // Auto Actions
    public boolean isAutoTimeoutEnabled() {
        return autoTimeoutEnabled;
    }
    
    public void setAutoTimeoutEnabled(boolean autoTimeoutEnabled) {
        this.autoTimeoutEnabled = autoTimeoutEnabled;
    }
    
    public boolean isAutoKickEnabled() {
        return autoKickEnabled;
    }
    
    public void setAutoKickEnabled(boolean autoKickEnabled) {
        this.autoKickEnabled = autoKickEnabled;
    }
    
    public boolean isAutoBanEnabled() {
        return autoBanEnabled;
    }
    
    public void setAutoBanEnabled(boolean autoBanEnabled) {
        this.autoBanEnabled = autoBanEnabled;
    }
    
    // Logging
    public boolean isLogModerationActions() {
        return logModerationActions;
    }
    
    public void setLogModerationActions(boolean logModerationActions) {
        this.logModerationActions = logModerationActions;
    }
    
    public String getLogChannelId() {
        return logChannelId;
    }
    
    public void setLogChannelId(String logChannelId) {
        this.logChannelId = logChannelId;
    }
    
    public String getAuditChannelId() {
        return auditChannelId;
    }
    
    public void setAuditChannelId(String auditChannelId) {
        this.auditChannelId = auditChannelId;
    }
    
    public int getLogRetentionDays() {
        return logRetentionDays;
    }
    
    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }
    
    // Whitelist
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }
    
    public void setWhitelistEnabled(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }
    
    // Advanced features getters and setters
    public boolean isAdvancedFeaturesEnabled() { return advancedFeaturesEnabled; }
    public void setAdvancedFeaturesEnabled(boolean advancedFeaturesEnabled) { this.advancedFeaturesEnabled = advancedFeaturesEnabled; }
    
    public boolean isTempBanEnabled() { return tempBanEnabled; }
    public void setTempBanEnabled(boolean tempBanEnabled) { this.tempBanEnabled = tempBanEnabled; }
    
    public int getMaxTempBanHours() { return maxTempBanHours; }
    public void setMaxTempBanHours(int maxTempBanHours) { this.maxTempBanHours = maxTempBanHours; }
    
    public boolean isEscalationEnabled() { return escalationEnabled; }
    public void setEscalationEnabled(boolean escalationEnabled) { this.escalationEnabled = escalationEnabled; }
    
    public boolean isAttachmentScanningEnabled() { return attachmentScanningEnabled; }
    public void setAttachmentScanningEnabled(boolean attachmentScanningEnabled) { this.attachmentScanningEnabled = attachmentScanningEnabled; }
    
    public boolean isAdvancedSpamDetectionEnabled() { return advancedSpamDetectionEnabled; }
    public void setAdvancedSpamDetectionEnabled(boolean advancedSpamDetectionEnabled) { this.advancedSpamDetectionEnabled = advancedSpamDetectionEnabled; }
    
    // Violation tracking getters and setters
    public int getViolationDecayHours() { return violationDecayHours; }
    public void setViolationDecayHours(int violationDecayHours) { this.violationDecayHours = violationDecayHours; }
    
    public boolean isAutoViolationReset() { return autoViolationReset; }
    public void setAutoViolationReset(boolean autoViolationReset) { this.autoViolationReset = autoViolationReset; }
    
    // Detailed logging
    public boolean isDetailedLogging() { return detailedLogging; }
    public void setDetailedLogging(boolean detailedLogging) { this.detailedLogging = detailedLogging; }
    
    // Trusted domains
    public Set<String> getTrustedDomains() { return trustedDomains; }
    public void setTrustedDomains(Set<String> trustedDomains) { this.trustedDomains = trustedDomains; }
    public void addTrustedDomain(String domain) { this.trustedDomains.add(domain); }
    public void removeTrustedDomain(String domain) { this.trustedDomains.remove(domain); }
    
    // Whitelisted users and roles
    public Set<String> getWhitelistedUsers() { return whitelistedUsers; }
    public void setWhitelistedUsers(Set<String> whitelistedUsers) { this.whitelistedUsers = whitelistedUsers; }
    public void addWhitelistedUser(String userId) { this.whitelistedUsers.add(userId); }
    public void removeWhitelistedUser(String userId) { this.whitelistedUsers.remove(userId); }
    
    public Set<String> getWhitelistedRoles() { return whitelistedRoles; }
    public void setWhitelistedRoles(Set<String> whitelistedRoles) { this.whitelistedRoles = whitelistedRoles; }
    public void addWhitelistedRole(String roleId) { this.whitelistedRoles.add(roleId); }
    public void removeWhitelistedRole(String roleId) { this.whitelistedRoles.remove(roleId); }
    
    // Anti-raid protection getters and setters
    public boolean isAntiRaidEnabled() { return antiRaidEnabled; }
    public void setAntiRaidEnabled(boolean antiRaidEnabled) { this.antiRaidEnabled = antiRaidEnabled; }
    
    public int getRaidDetectionThreshold() { return raidDetectionThreshold; }
    public void setRaidDetectionThreshold(int raidDetectionThreshold) { this.raidDetectionThreshold = raidDetectionThreshold; }
    
    public int getRaidDetectionTimeframe() { return raidDetectionTimeframe; }
    public void setRaidDetectionTimeframe(int raidDetectionTimeframe) { this.raidDetectionTimeframe = raidDetectionTimeframe; }
    
    public boolean isAutoLockdownEnabled() { return autoLockdownEnabled; }
    public void setAutoLockdownEnabled(boolean autoLockdownEnabled) { this.autoLockdownEnabled = autoLockdownEnabled; }
    
    public int getLockdownDuration() { return lockdownDuration; }
    public void setLockdownDuration(int lockdownDuration) { this.lockdownDuration = lockdownDuration; }
    
    // Threat intelligence getters and setters
    public boolean isThreatIntelligenceEnabled() { return threatIntelligenceEnabled; }
    public void setThreatIntelligenceEnabled(boolean threatIntelligenceEnabled) { this.threatIntelligenceEnabled = threatIntelligenceEnabled; }
    
    public boolean isPhishingDetectionEnabled() { return phishingDetectionEnabled; }
    public void setPhishingDetectionEnabled(boolean phishingDetectionEnabled) { this.phishingDetectionEnabled = phishingDetectionEnabled; }
    
    public boolean isMalwareDetectionEnabled() { return malwareDetectionEnabled; }
    public void setMalwareDetectionEnabled(boolean malwareDetectionEnabled) { this.malwareDetectionEnabled = malwareDetectionEnabled; }
    
    public boolean isScamDetectionEnabled() { return scamDetectionEnabled; }
    public void setScamDetectionEnabled(boolean scamDetectionEnabled) { this.scamDetectionEnabled = scamDetectionEnabled; }
    
    // Advanced timeout system getters and setters
    public boolean isSmartTimeoutEnabled() { return smartTimeoutEnabled; }
    public void setSmartTimeoutEnabled(boolean smartTimeoutEnabled) { this.smartTimeoutEnabled = smartTimeoutEnabled; }
    
    public int getBaseTimeoutMinutes() { return baseTimeoutMinutes; }
    public void setBaseTimeoutMinutes(int baseTimeoutMinutes) { this.baseTimeoutMinutes = baseTimeoutMinutes; }
    
    public double getTimeoutMultiplier() { return timeoutMultiplier; }
    public void setTimeoutMultiplier(double timeoutMultiplier) { this.timeoutMultiplier = timeoutMultiplier; }
    
    public int getMaxTimeoutHours() { return maxTimeoutHours; }
    public void setMaxTimeoutHours(int maxTimeoutHours) { this.maxTimeoutHours = maxTimeoutHours; }
    
    // Behavioral analysis getters and setters
    public boolean isBehavioralAnalysisEnabled() { return behavioralAnalysisEnabled; }
    public void setBehavioralAnalysisEnabled(boolean behavioralAnalysisEnabled) { this.behavioralAnalysisEnabled = behavioralAnalysisEnabled; }
    
    // AI detection getters and setters
    public boolean isAiDetectionEnabled() { return aiDetectionEnabled; }
    public void setAiDetectionEnabled(boolean aiDetectionEnabled) { this.aiDetectionEnabled = aiDetectionEnabled; }
    
    // Smart rules getters and setters
    public boolean isSmartRulesEnabled() { return smartRulesEnabled; }
    public void setSmartRulesEnabled(boolean smartRulesEnabled) { this.smartRulesEnabled = smartRulesEnabled; }
    
    // Threat intelligence getters and setters
    public boolean isThreatIntelEnabled() { return threatIntelEnabled; }
    public void setThreatIntelEnabled(boolean threatIntelEnabled) { this.threatIntelEnabled = threatIntelEnabled; }
    
    /**
     * Opretter en standard konfiguration med moderate indstillinger
     */
    public static ModerationConfig createDefault() {
        ModerationConfig config = new ModerationConfig();
        config.setSpamProtectionEnabled(true);
        config.setToxicDetectionEnabled(true);
        config.setLinkProtectionEnabled(true);
        config.setCustomFiltersEnabled(true);
        config.setAutoTimeoutEnabled(true);
        config.setAutoKickEnabled(false);
        config.setAutoBanEnabled(false);
        config.setAntiRaidEnabled(true);
        config.setThreatIntelligenceEnabled(true);
        config.setSmartTimeoutEnabled(true);
        config.addTrustedDomain("youtube.com");
        config.addTrustedDomain("github.com");
        config.addTrustedDomain("stackoverflow.com");
        return config;
    }
    
    /**
     * Opretter en streng konfiguration med alle funktioner aktiveret
     */
    public static ModerationConfig createStrict() {
        ModerationConfig config = new ModerationConfig();
        config.setSpamProtectionEnabled(true);
        config.setMaxMessagesPerMinute(3);
        config.setMaxWarningsBeforeBan(2);
        config.setToxicDetectionEnabled(true);
        config.setAutoDeleteToxicContent(true);
        config.setLinkProtectionEnabled(true);
        config.setMaxLinksPerMessage(1);
        config.setAllowDiscordInvites(false);
        config.setCustomFiltersEnabled(true);
        config.setAutoTimeoutEnabled(true);
        config.setAutoKickEnabled(true);
        config.setAutoBanEnabled(true);
        config.setEscalationEnabled(true);
        config.setAdvancedSpamDetectionEnabled(true);
        config.setAttachmentScanningEnabled(true);
        config.setDetailedLogging(true);
        config.setAntiRaidEnabled(true);
        config.setRaidDetectionThreshold(3);
        config.setRaidDetectionTimeframe(30);
        config.setAutoLockdownEnabled(true);
        config.setThreatIntelligenceEnabled(true);
        config.setPhishingDetectionEnabled(true);
        config.setMalwareDetectionEnabled(true);
        config.setScamDetectionEnabled(true);
        config.setSmartTimeoutEnabled(true);
        config.setBaseTimeoutMinutes(10);
        config.setTimeoutMultiplier(2.5);
        return config;
    }
    
    /**
     * Opretter en mild konfiguration med færre restriktioner
     */
    public static ModerationConfig createLenient() {
        ModerationConfig config = new ModerationConfig();
        config.setSpamProtectionEnabled(true);
        config.setMaxMessagesPerMinute(8);
        config.setMaxWarningsBeforeBan(5);
        config.setToxicDetectionEnabled(false);
        config.setLinkProtectionEnabled(false);
        config.setCustomFiltersEnabled(false);
        config.setAutoTimeoutEnabled(false);
        config.setAutoKickEnabled(false);
        config.setAutoBanEnabled(false);
        config.setAntiRaidEnabled(false);
        config.setThreatIntelligenceEnabled(false);
        config.setSmartTimeoutEnabled(false);
        config.setDetailedLogging(false);
        return config;
    }
    
    @Override
    public String toString() {
        return "ModerationConfig{" +
                "spamProtection=" + spamProtectionEnabled +
                ", toxicDetection=" + toxicDetectionEnabled +
                ", linkProtection=" + linkProtectionEnabled +
                ", customFilters=" + customFiltersEnabled +
                ", autoTimeout=" + autoTimeoutEnabled +
                ", autoKick=" + autoKickEnabled +
                ", autoBan=" + autoBanEnabled +
                '}';
    }
}