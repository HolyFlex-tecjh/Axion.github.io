package com.axion.bot.moderation;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Guild-specific moderation settings and configuration
 */
public class GuildModerationSettings {
    private final String guildId;
    private boolean spamProtectionEnabled = true;
    private boolean toxicityDetectionEnabled = true;
    private boolean threatIntelEnabled = true;
    private boolean antiRaidEnabled = true;
    private boolean accountAgeCheckEnabled = false;
    private boolean suspiciousUsernameDetectionEnabled = true;
    private int minAccountAgeDays = 7;
    private int raidDetectionThreshold = 5;
    private long raidDetectionTimeframe = 60000; // 1 minute
    private boolean autoLockdownEnabled = true;
    private int lockdownDuration = 300; // 5 minutes
    private Set<String> whitelistedUsers = new HashSet<>();
    private Set<String> whitelistedRoles = new HashSet<>();
    private Instant lastUpdated;
    
    public GuildModerationSettings(String guildId) {
        this.guildId = guildId;
        this.lastUpdated = Instant.now();
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public boolean isSpamProtectionEnabled() { return spamProtectionEnabled; }
    public boolean isToxicityDetectionEnabled() { return toxicityDetectionEnabled; }
    public boolean isThreatIntelEnabled() { return threatIntelEnabled; }
    public boolean isAntiRaidEnabled() { return antiRaidEnabled; }
    public boolean isAccountAgeCheckEnabled() { return accountAgeCheckEnabled; }
    public boolean isSuspiciousUsernameDetectionEnabled() { return suspiciousUsernameDetectionEnabled; }
    public int getMinAccountAgeDays() { return minAccountAgeDays; }
    public int getRaidDetectionThreshold() { return raidDetectionThreshold; }
    public long getRaidDetectionTimeframe() { return raidDetectionTimeframe; }
    public boolean isAutoLockdownEnabled() { return autoLockdownEnabled; }
    public int getLockdownDuration() { return lockdownDuration; }
    public Set<String> getWhitelistedUsers() { return new HashSet<>(whitelistedUsers); }
    public Set<String> getWhitelistedRoles() { return new HashSet<>(whitelistedRoles); }
    public Instant getLastUpdated() { return lastUpdated; }
    
    // Setters
    public void setSpamProtectionEnabled(boolean enabled) {
        this.spamProtectionEnabled = enabled;
        updateTimestamp();
    }
    
    public void setToxicityDetectionEnabled(boolean enabled) {
        this.toxicityDetectionEnabled = enabled;
        updateTimestamp();
    }
    
    public void setThreatIntelEnabled(boolean enabled) {
        this.threatIntelEnabled = enabled;
        updateTimestamp();
    }
    
    public void setAntiRaidEnabled(boolean enabled) {
        this.antiRaidEnabled = enabled;
        updateTimestamp();
    }
    
    public void setAccountAgeCheckEnabled(boolean enabled) {
        this.accountAgeCheckEnabled = enabled;
        updateTimestamp();
    }
    
    public void setSuspiciousUsernameDetectionEnabled(boolean enabled) {
        this.suspiciousUsernameDetectionEnabled = enabled;
        updateTimestamp();
    }
    
    public void setMinAccountAgeDays(int days) {
        this.minAccountAgeDays = Math.max(0, days);
        updateTimestamp();
    }
    
    public void setRaidDetectionThreshold(int threshold) {
        this.raidDetectionThreshold = Math.max(1, threshold);
        updateTimestamp();
    }
    
    public void setRaidDetectionTimeframe(long timeframe) {
        this.raidDetectionTimeframe = Math.max(1000, timeframe);
        updateTimestamp();
    }
    
    public void setAutoLockdownEnabled(boolean enabled) {
        this.autoLockdownEnabled = enabled;
        updateTimestamp();
    }
    
    public void setLockdownDuration(int duration) {
        this.lockdownDuration = Math.max(60, duration);
        updateTimestamp();
    }
    
    public void addWhitelistedUser(String userId) {
        this.whitelistedUsers.add(userId);
        updateTimestamp();
    }
    
    public void removeWhitelistedUser(String userId) {
        this.whitelistedUsers.remove(userId);
        updateTimestamp();
    }
    
    public void addWhitelistedRole(String roleId) {
        this.whitelistedRoles.add(roleId);
        updateTimestamp();
    }
    
    public void removeWhitelistedRole(String roleId) {
        this.whitelistedRoles.remove(roleId);
        updateTimestamp();
    }
    
    public boolean isUserWhitelisted(String userId) {
        return whitelistedUsers.contains(userId);
    }
    
    public boolean isRoleWhitelisted(String roleId) {
        return whitelistedRoles.contains(roleId);
    }
    
    private void updateTimestamp() {
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Create default settings for a guild
     */
    public static GuildModerationSettings createDefault(String guildId) {
        return new GuildModerationSettings(guildId);
    }
    
    /**
     * Create strict settings for a guild
     */
    public static GuildModerationSettings createStrict(String guildId) {
        GuildModerationSettings settings = new GuildModerationSettings(guildId);
        settings.setAccountAgeCheckEnabled(true);
        settings.setMinAccountAgeDays(14);
        settings.setRaidDetectionThreshold(3);
        settings.setRaidDetectionTimeframe(30000); // 30 seconds
        return settings;
    }
    
    /**
     * Create lenient settings for a guild
     */
    public static GuildModerationSettings createLenient(String guildId) {
        GuildModerationSettings settings = new GuildModerationSettings(guildId);
        settings.setToxicityDetectionEnabled(false);
        settings.setAccountAgeCheckEnabled(false);
        settings.setSuspiciousUsernameDetectionEnabled(false);
        settings.setRaidDetectionThreshold(10);
        return settings;
    }
}