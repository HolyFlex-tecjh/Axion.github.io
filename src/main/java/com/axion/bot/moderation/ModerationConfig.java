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
    private boolean detailedLogging = false;
    
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
    
    // Whitelist
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }
    
    public void setWhitelistEnabled(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }
    
    // Advanced features getters and setters
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