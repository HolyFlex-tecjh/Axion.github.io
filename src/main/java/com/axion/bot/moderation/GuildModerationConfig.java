package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for guild-specific moderation settings
 */
public class GuildModerationConfig {
    private final String guildId;
    private final String guildName;
    private boolean moderationEnabled;
    private boolean autoModerationEnabled;
    private boolean spamDetectionEnabled;
    private boolean toxicityDetectionEnabled;
    private boolean raidProtectionEnabled;
    private boolean behavioralAnalysisEnabled;
    
    // Spam detection settings
    private int maxMessagesPerMinute;
    private int maxDuplicateMessages;
    private int maxMentionsPerMessage;
    private boolean linkDetectionEnabled;
    private Set<String> allowedDomains;
    private Set<String> blockedDomains;
    
    // Toxicity detection settings
    private double toxicityThreshold;
    private boolean profanityFilterEnabled;
    private boolean hateSpeechDetectionEnabled;
    private boolean threatDetectionEnabled;
    private Set<String> customBannedWords;
    
    // Raid protection settings
    private int raidDetectionThreshold;
    private Duration raidDetectionWindow;
    private boolean autoKickRaiders;
    private boolean lockdownOnRaid;
    
    // Behavioral analysis settings
    private boolean suspiciousActivityDetection;
    private boolean coordinatedBehaviorDetection;
    private double riskThreshold;
    private Duration behaviorAnalysisWindow;
    
    // Punishment settings
    private boolean autoWarnEnabled;
    private boolean autoMuteEnabled;
    private boolean autoKickEnabled;
    private boolean autoBanEnabled;
    private Duration defaultMuteDuration;
    private int maxWarningsBeforeMute;
    private int maxMutesBeforeKick;
    private int maxKicksBeforeBan;
    
    // Channel settings
    private Set<String> exemptChannels;
    private Set<String> exemptRoles;
    private Set<String> moderatorRoles;
    private String logChannelId;
    private String appealChannelId;
    
    // Advanced settings
    private boolean aiAnalysisEnabled;
    private boolean contextAwareModeration;
    private boolean crossGuildDataSharing;
    private Map<String, Object> customSettings;
    
    // Metadata
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private int configVersion;
    
    private GuildModerationConfig(Builder builder) {
        this.guildId = Objects.requireNonNull(builder.guildId, "Guild ID cannot be null");
        this.guildName = builder.guildName;
        this.moderationEnabled = builder.moderationEnabled;
        this.autoModerationEnabled = builder.autoModerationEnabled;
        this.spamDetectionEnabled = builder.spamDetectionEnabled;
        this.toxicityDetectionEnabled = builder.toxicityDetectionEnabled;
        this.raidProtectionEnabled = builder.raidProtectionEnabled;
        this.behavioralAnalysisEnabled = builder.behavioralAnalysisEnabled;
        
        this.maxMessagesPerMinute = builder.maxMessagesPerMinute;
        this.maxDuplicateMessages = builder.maxDuplicateMessages;
        this.maxMentionsPerMessage = builder.maxMentionsPerMessage;
        this.linkDetectionEnabled = builder.linkDetectionEnabled;
        this.allowedDomains = Collections.unmodifiableSet(new HashSet<>(builder.allowedDomains));
        this.blockedDomains = Collections.unmodifiableSet(new HashSet<>(builder.blockedDomains));
        
        this.toxicityThreshold = builder.toxicityThreshold;
        this.profanityFilterEnabled = builder.profanityFilterEnabled;
        this.hateSpeechDetectionEnabled = builder.hateSpeechDetectionEnabled;
        this.threatDetectionEnabled = builder.threatDetectionEnabled;
        this.customBannedWords = Collections.unmodifiableSet(new HashSet<>(builder.customBannedWords));
        
        this.raidDetectionThreshold = builder.raidDetectionThreshold;
        this.raidDetectionWindow = builder.raidDetectionWindow;
        this.autoKickRaiders = builder.autoKickRaiders;
        this.lockdownOnRaid = builder.lockdownOnRaid;
        
        this.suspiciousActivityDetection = builder.suspiciousActivityDetection;
        this.coordinatedBehaviorDetection = builder.coordinatedBehaviorDetection;
        this.riskThreshold = builder.riskThreshold;
        this.behaviorAnalysisWindow = builder.behaviorAnalysisWindow;
        
        this.autoWarnEnabled = builder.autoWarnEnabled;
        this.autoMuteEnabled = builder.autoMuteEnabled;
        this.autoKickEnabled = builder.autoKickEnabled;
        this.autoBanEnabled = builder.autoBanEnabled;
        this.defaultMuteDuration = builder.defaultMuteDuration;
        this.maxWarningsBeforeMute = builder.maxWarningsBeforeMute;
        this.maxMutesBeforeKick = builder.maxMutesBeforeKick;
        this.maxKicksBeforeBan = builder.maxKicksBeforeBan;
        
        this.exemptChannels = Collections.unmodifiableSet(new HashSet<>(builder.exemptChannels));
        this.exemptRoles = Collections.unmodifiableSet(new HashSet<>(builder.exemptRoles));
        this.moderatorRoles = Collections.unmodifiableSet(new HashSet<>(builder.moderatorRoles));
        this.logChannelId = builder.logChannelId;
        this.appealChannelId = builder.appealChannelId;
        
        this.aiAnalysisEnabled = builder.aiAnalysisEnabled;
        this.contextAwareModeration = builder.contextAwareModeration;
        this.crossGuildDataSharing = builder.crossGuildDataSharing;
        this.customSettings = Collections.unmodifiableMap(new HashMap<>(builder.customSettings));
        
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
        this.configVersion = builder.configVersion;
    }
    
    /**
     * Create a new builder
     */
    public static Builder builder(String guildId) {
        return new Builder(guildId);
    }
    
    /**
     * Create default configuration for a guild
     */
    public static GuildModerationConfig createDefault(String guildId, String guildName) {
        return builder(guildId)
            .withGuildName(guildName)
            .withModerationEnabled(true)
            .withAutoModerationEnabled(true)
            .withSpamDetectionEnabled(true)
            .withToxicityDetectionEnabled(true)
            .withRaidProtectionEnabled(true)
            .withBehavioralAnalysisEnabled(true)
            .withMaxMessagesPerMinute(10)
            .withMaxDuplicateMessages(3)
            .withMaxMentionsPerMessage(5)
            .withLinkDetectionEnabled(true)
            .withToxicityThreshold(0.7)
            .withProfanityFilterEnabled(true)
            .withHateSpeechDetectionEnabled(true)
            .withThreatDetectionEnabled(true)
            .withRaidDetectionThreshold(5)
            .withRaidDetectionWindow(Duration.ofMinutes(2))
            .withAutoKickRaiders(true)
            .withLockdownOnRaid(false)
            .withSuspiciousActivityDetection(true)
            .withCoordinatedBehaviorDetection(true)
            .withRiskThreshold(0.8)
            .withBehaviorAnalysisWindow(Duration.ofHours(24))
            .withAutoWarnEnabled(true)
            .withAutoMuteEnabled(true)
            .withAutoKickEnabled(false)
            .withAutoBanEnabled(false)
            .withDefaultMuteDuration(Duration.ofMinutes(10))
            .withMaxWarningsBeforeMute(3)
            .withMaxMutesBeforeKick(3)
            .withMaxKicksBeforeBan(2)
            .withAiAnalysisEnabled(true)
            .withContextAwareModeration(true)
            .withCrossGuildDataSharing(false)
            .build();
    }
    
    /**
     * Create a copy of this configuration with modifications
     */
    public GuildModerationConfig withUpdates(String updatedBy) {
        return builder(this.guildId)
            .copyFrom(this)
            .withUpdatedBy(updatedBy)
            .withUpdatedAt(Instant.now())
            .withConfigVersion(this.configVersion + 1)
            .build();
    }
    
    /**
     * Check if a channel is exempt from moderation
     */
    public boolean isChannelExempt(String channelId) {
        return exemptChannels.contains(channelId);
    }
    
    /**
     * Check if a role is exempt from moderation
     */
    public boolean isRoleExempt(String roleId) {
        return exemptRoles.contains(roleId);
    }
    
    /**
     * Check if a role is a moderator role
     */
    public boolean isModeratorRole(String roleId) {
        return moderatorRoles.contains(roleId);
    }
    
    /**
     * Check if a domain is allowed
     */
    public boolean isDomainAllowed(String domain) {
        return allowedDomains.isEmpty() || allowedDomains.contains(domain.toLowerCase());
    }
    
    /**
     * Check if a domain is blocked
     */
    public boolean isDomainBlocked(String domain) {
        return blockedDomains.contains(domain.toLowerCase());
    }
    
    /**
     * Check if a word is banned
     */
    public boolean isWordBanned(String word) {
        return customBannedWords.contains(word.toLowerCase());
    }
    
    /**
     * Get effective moderation level
     */
    public String getModerationLevel() {
        if (!moderationEnabled) return "Disabled";
        if (!autoModerationEnabled) return "Manual";
        
        int enabledFeatures = 0;
        if (spamDetectionEnabled) enabledFeatures++;
        if (toxicityDetectionEnabled) enabledFeatures++;
        if (raidProtectionEnabled) enabledFeatures++;
        if (behavioralAnalysisEnabled) enabledFeatures++;
        
        if (enabledFeatures >= 4) return "Maximum";
        if (enabledFeatures >= 3) return "High";
        if (enabledFeatures >= 2) return "Medium";
        return "Low";
    }
    
    /**
     * Get configuration summary
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("guildId", guildId);
        summary.put("guildName", guildName);
        summary.put("moderationLevel", getModerationLevel());
        summary.put("moderationEnabled", moderationEnabled);
        summary.put("autoModerationEnabled", autoModerationEnabled);
        summary.put("enabledFeatures", getEnabledFeatures());
        summary.put("configVersion", configVersion);
        summary.put("lastUpdated", updatedAt);
        return summary;
    }
    
    /**
     * Get list of enabled features
     */
    public List<String> getEnabledFeatures() {
        List<String> features = new ArrayList<>();
        if (spamDetectionEnabled) features.add("Spam Detection");
        if (toxicityDetectionEnabled) features.add("Toxicity Detection");
        if (raidProtectionEnabled) features.add("Raid Protection");
        if (behavioralAnalysisEnabled) features.add("Behavioral Analysis");
        if (aiAnalysisEnabled) features.add("AI Analysis");
        if (contextAwareModeration) features.add("Context Aware Moderation");
        return features;
    }
    
    /**
     * Validate configuration
     */
    public List<String> validate() {
        List<String> issues = new ArrayList<>();
        
        if (guildId == null || guildId.trim().isEmpty()) {
            issues.add("Guild ID is required");
        }
        
        if (maxMessagesPerMinute <= 0) {
            issues.add("Max messages per minute must be positive");
        }
        
        if (maxDuplicateMessages <= 0) {
            issues.add("Max duplicate messages must be positive");
        }
        
        if (maxMentionsPerMessage <= 0) {
            issues.add("Max mentions per message must be positive");
        }
        
        if (toxicityThreshold < 0.0 || toxicityThreshold > 1.0) {
            issues.add("Toxicity threshold must be between 0.0 and 1.0");
        }
        
        if (riskThreshold < 0.0 || riskThreshold > 1.0) {
            issues.add("Risk threshold must be between 0.0 and 1.0");
        }
        
        if (raidDetectionThreshold <= 0) {
            issues.add("Raid detection threshold must be positive");
        }
        
        if (raidDetectionWindow == null || raidDetectionWindow.isNegative()) {
            issues.add("Raid detection window must be positive");
        }
        
        if (behaviorAnalysisWindow == null || behaviorAnalysisWindow.isNegative()) {
            issues.add("Behavior analysis window must be positive");
        }
        
        if (defaultMuteDuration == null || defaultMuteDuration.isNegative()) {
            issues.add("Default mute duration must be positive");
        }
        
        return issues;
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public String getGuildName() { return guildName; }
    public boolean isModerationEnabled() { return moderationEnabled; }
    public boolean isAutoModerationEnabled() { return autoModerationEnabled; }
    public boolean isSpamDetectionEnabled() { return spamDetectionEnabled; }
    public boolean isToxicityDetectionEnabled() { return toxicityDetectionEnabled; }
    public boolean isRaidProtectionEnabled() { return raidProtectionEnabled; }
    public boolean isBehavioralAnalysisEnabled() { return behavioralAnalysisEnabled; }
    public int getMaxMessagesPerMinute() { return maxMessagesPerMinute; }
    public int getMaxDuplicateMessages() { return maxDuplicateMessages; }
    public int getMaxMentionsPerMessage() { return maxMentionsPerMessage; }
    public boolean isLinkDetectionEnabled() { return linkDetectionEnabled; }
    public Set<String> getAllowedDomains() { return allowedDomains; }
    public Set<String> getBlockedDomains() { return blockedDomains; }
    public double getToxicityThreshold() { return toxicityThreshold; }
    public boolean isProfanityFilterEnabled() { return profanityFilterEnabled; }
    public boolean isHateSpeechDetectionEnabled() { return hateSpeechDetectionEnabled; }
    public boolean isThreatDetectionEnabled() { return threatDetectionEnabled; }
    public Set<String> getCustomBannedWords() { return customBannedWords; }
    public int getRaidDetectionThreshold() { return raidDetectionThreshold; }
    public Duration getRaidDetectionWindow() { return raidDetectionWindow; }
    public boolean isAutoKickRaiders() { return autoKickRaiders; }
    public boolean isLockdownOnRaid() { return lockdownOnRaid; }
    public boolean isSuspiciousActivityDetection() { return suspiciousActivityDetection; }
    public boolean isCoordinatedBehaviorDetection() { return coordinatedBehaviorDetection; }
    public double getRiskThreshold() { return riskThreshold; }
    public Duration getBehaviorAnalysisWindow() { return behaviorAnalysisWindow; }
    public boolean isAutoWarnEnabled() { return autoWarnEnabled; }
    public boolean isAutoMuteEnabled() { return autoMuteEnabled; }
    public boolean isAutoKickEnabled() { return autoKickEnabled; }
    public boolean isAutoBanEnabled() { return autoBanEnabled; }
    public Duration getDefaultMuteDuration() { return defaultMuteDuration; }
    public int getMaxWarningsBeforeMute() { return maxWarningsBeforeMute; }
    public int getMaxMutesBeforeKick() { return maxMutesBeforeKick; }
    public int getMaxKicksBeforeBan() { return maxKicksBeforeBan; }
    public Set<String> getExemptChannels() { return exemptChannels; }
    public Set<String> getExemptRoles() { return exemptRoles; }
    public Set<String> getModeratorRoles() { return moderatorRoles; }
    public String getLogChannelId() { return logChannelId; }
    public String getAppealChannelId() { return appealChannelId; }
    public boolean isAiAnalysisEnabled() { return aiAnalysisEnabled; }
    public boolean isContextAwareModeration() { return contextAwareModeration; }
    public boolean isCrossGuildDataSharing() { return crossGuildDataSharing; }
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public int getConfigVersion() { return configVersion; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GuildModerationConfig that = (GuildModerationConfig) obj;
        return Objects.equals(guildId, that.guildId) &&
               configVersion == that.configVersion;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(guildId, configVersion);
    }
    
    @Override
    public String toString() {
        return String.format("GuildModerationConfig{guildId='%s', guildName='%s', level='%s', version=%d}",
            guildId, guildName, getModerationLevel(), configVersion);
    }
    
    /**
     * Builder for GuildModerationConfig
     */
    public static class Builder {
        private final String guildId;
        private String guildName;
        private boolean moderationEnabled = true;
        private boolean autoModerationEnabled = true;
        private boolean spamDetectionEnabled = true;
        private boolean toxicityDetectionEnabled = true;
        private boolean raidProtectionEnabled = true;
        private boolean behavioralAnalysisEnabled = true;
        
        private int maxMessagesPerMinute = 10;
        private int maxDuplicateMessages = 3;
        private int maxMentionsPerMessage = 5;
        private boolean linkDetectionEnabled = true;
        private final Set<String> allowedDomains = new HashSet<>();
        private final Set<String> blockedDomains = new HashSet<>();
        
        private double toxicityThreshold = 0.7;
        private boolean profanityFilterEnabled = true;
        private boolean hateSpeechDetectionEnabled = true;
        private boolean threatDetectionEnabled = true;
        private final Set<String> customBannedWords = new HashSet<>();
        
        private int raidDetectionThreshold = 5;
        private Duration raidDetectionWindow = Duration.ofMinutes(2);
        private boolean autoKickRaiders = true;
        private boolean lockdownOnRaid = false;
        
        private boolean suspiciousActivityDetection = true;
        private boolean coordinatedBehaviorDetection = true;
        private double riskThreshold = 0.8;
        private Duration behaviorAnalysisWindow = Duration.ofHours(24);
        
        private boolean autoWarnEnabled = true;
        private boolean autoMuteEnabled = true;
        private boolean autoKickEnabled = false;
        private boolean autoBanEnabled = false;
        private Duration defaultMuteDuration = Duration.ofMinutes(10);
        private int maxWarningsBeforeMute = 3;
        private int maxMutesBeforeKick = 3;
        private int maxKicksBeforeBan = 2;
        
        private final Set<String> exemptChannels = new HashSet<>();
        private final Set<String> exemptRoles = new HashSet<>();
        private final Set<String> moderatorRoles = new HashSet<>();
        private String logChannelId;
        private String appealChannelId;
        
        private boolean aiAnalysisEnabled = true;
        private boolean contextAwareModeration = true;
        private boolean crossGuildDataSharing = false;
        private final Map<String, Object> customSettings = new ConcurrentHashMap<>();
        
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        private int configVersion = 1;
        
        private Builder(String guildId) {
            this.guildId = guildId;
        }
        
        public Builder copyFrom(GuildModerationConfig config) {
            this.guildName = config.guildName;
            this.moderationEnabled = config.moderationEnabled;
            this.autoModerationEnabled = config.autoModerationEnabled;
            this.spamDetectionEnabled = config.spamDetectionEnabled;
            this.toxicityDetectionEnabled = config.toxicityDetectionEnabled;
            this.raidProtectionEnabled = config.raidProtectionEnabled;
            this.behavioralAnalysisEnabled = config.behavioralAnalysisEnabled;
            
            this.maxMessagesPerMinute = config.maxMessagesPerMinute;
            this.maxDuplicateMessages = config.maxDuplicateMessages;
            this.maxMentionsPerMessage = config.maxMentionsPerMessage;
            this.linkDetectionEnabled = config.linkDetectionEnabled;
            this.allowedDomains.addAll(config.allowedDomains);
            this.blockedDomains.addAll(config.blockedDomains);
            
            this.toxicityThreshold = config.toxicityThreshold;
            this.profanityFilterEnabled = config.profanityFilterEnabled;
            this.hateSpeechDetectionEnabled = config.hateSpeechDetectionEnabled;
            this.threatDetectionEnabled = config.threatDetectionEnabled;
            this.customBannedWords.addAll(config.customBannedWords);
            
            this.raidDetectionThreshold = config.raidDetectionThreshold;
            this.raidDetectionWindow = config.raidDetectionWindow;
            this.autoKickRaiders = config.autoKickRaiders;
            this.lockdownOnRaid = config.lockdownOnRaid;
            
            this.suspiciousActivityDetection = config.suspiciousActivityDetection;
            this.coordinatedBehaviorDetection = config.coordinatedBehaviorDetection;
            this.riskThreshold = config.riskThreshold;
            this.behaviorAnalysisWindow = config.behaviorAnalysisWindow;
            
            this.autoWarnEnabled = config.autoWarnEnabled;
            this.autoMuteEnabled = config.autoMuteEnabled;
            this.autoKickEnabled = config.autoKickEnabled;
            this.autoBanEnabled = config.autoBanEnabled;
            this.defaultMuteDuration = config.defaultMuteDuration;
            this.maxWarningsBeforeMute = config.maxWarningsBeforeMute;
            this.maxMutesBeforeKick = config.maxMutesBeforeKick;
            this.maxKicksBeforeBan = config.maxKicksBeforeBan;
            
            this.exemptChannels.addAll(config.exemptChannels);
            this.exemptRoles.addAll(config.exemptRoles);
            this.moderatorRoles.addAll(config.moderatorRoles);
            this.logChannelId = config.logChannelId;
            this.appealChannelId = config.appealChannelId;
            
            this.aiAnalysisEnabled = config.aiAnalysisEnabled;
            this.contextAwareModeration = config.contextAwareModeration;
            this.crossGuildDataSharing = config.crossGuildDataSharing;
            this.customSettings.putAll(config.customSettings);
            
            this.createdAt = config.createdAt;
            this.createdBy = config.createdBy;
            this.configVersion = config.configVersion;
            
            return this;
        }
        
        public Builder withGuildName(String guildName) {
            this.guildName = guildName;
            return this;
        }
        
        public Builder withModerationEnabled(boolean enabled) {
            this.moderationEnabled = enabled;
            return this;
        }
        
        public Builder withAutoModerationEnabled(boolean enabled) {
            this.autoModerationEnabled = enabled;
            return this;
        }
        
        public Builder withSpamDetectionEnabled(boolean enabled) {
            this.spamDetectionEnabled = enabled;
            return this;
        }
        
        public Builder withToxicityDetectionEnabled(boolean enabled) {
            this.toxicityDetectionEnabled = enabled;
            return this;
        }
        
        public Builder withRaidProtectionEnabled(boolean enabled) {
            this.raidProtectionEnabled = enabled;
            return this;
        }
        
        public Builder withBehavioralAnalysisEnabled(boolean enabled) {
            this.behavioralAnalysisEnabled = enabled;
            return this;
        }
        
        public Builder withMaxMessagesPerMinute(int max) {
            this.maxMessagesPerMinute = max;
            return this;
        }
        
        public Builder withMaxDuplicateMessages(int max) {
            this.maxDuplicateMessages = max;
            return this;
        }
        
        public Builder withMaxMentionsPerMessage(int max) {
            this.maxMentionsPerMessage = max;
            return this;
        }
        
        public Builder withLinkDetectionEnabled(boolean enabled) {
            this.linkDetectionEnabled = enabled;
            return this;
        }
        
        public Builder addAllowedDomain(String domain) {
            this.allowedDomains.add(domain.toLowerCase());
            return this;
        }
        
        public Builder addBlockedDomain(String domain) {
            this.blockedDomains.add(domain.toLowerCase());
            return this;
        }
        
        public Builder withToxicityThreshold(double threshold) {
            this.toxicityThreshold = threshold;
            return this;
        }
        
        public Builder withProfanityFilterEnabled(boolean enabled) {
            this.profanityFilterEnabled = enabled;
            return this;
        }
        
        public Builder withHateSpeechDetectionEnabled(boolean enabled) {
            this.hateSpeechDetectionEnabled = enabled;
            return this;
        }
        
        public Builder withThreatDetectionEnabled(boolean enabled) {
            this.threatDetectionEnabled = enabled;
            return this;
        }
        
        public Builder addCustomBannedWord(String word) {
            this.customBannedWords.add(word.toLowerCase());
            return this;
        }
        
        public Builder withRaidDetectionThreshold(int threshold) {
            this.raidDetectionThreshold = threshold;
            return this;
        }
        
        public Builder withRaidDetectionWindow(Duration window) {
            this.raidDetectionWindow = window;
            return this;
        }
        
        public Builder withAutoKickRaiders(boolean enabled) {
            this.autoKickRaiders = enabled;
            return this;
        }
        
        public Builder withLockdownOnRaid(boolean enabled) {
            this.lockdownOnRaid = enabled;
            return this;
        }
        
        public Builder withSuspiciousActivityDetection(boolean enabled) {
            this.suspiciousActivityDetection = enabled;
            return this;
        }
        
        public Builder withCoordinatedBehaviorDetection(boolean enabled) {
            this.coordinatedBehaviorDetection = enabled;
            return this;
        }
        
        public Builder withRiskThreshold(double threshold) {
            this.riskThreshold = threshold;
            return this;
        }
        
        public Builder withBehaviorAnalysisWindow(Duration window) {
            this.behaviorAnalysisWindow = window;
            return this;
        }
        
        public Builder withAutoWarnEnabled(boolean enabled) {
            this.autoWarnEnabled = enabled;
            return this;
        }
        
        public Builder withAutoMuteEnabled(boolean enabled) {
            this.autoMuteEnabled = enabled;
            return this;
        }
        
        public Builder withAutoKickEnabled(boolean enabled) {
            this.autoKickEnabled = enabled;
            return this;
        }
        
        public Builder withAutoBanEnabled(boolean enabled) {
            this.autoBanEnabled = enabled;
            return this;
        }
        
        public Builder withDefaultMuteDuration(Duration duration) {
            this.defaultMuteDuration = duration;
            return this;
        }
        
        public Builder withMaxWarningsBeforeMute(int max) {
            this.maxWarningsBeforeMute = max;
            return this;
        }
        
        public Builder withMaxMutesBeforeKick(int max) {
            this.maxMutesBeforeKick = max;
            return this;
        }
        
        public Builder withMaxKicksBeforeBan(int max) {
            this.maxKicksBeforeBan = max;
            return this;
        }
        
        public Builder addExemptChannel(String channelId) {
            this.exemptChannels.add(channelId);
            return this;
        }
        
        public Builder addExemptRole(String roleId) {
            this.exemptRoles.add(roleId);
            return this;
        }
        
        public Builder addModeratorRole(String roleId) {
            this.moderatorRoles.add(roleId);
            return this;
        }
        
        public Builder withLogChannelId(String channelId) {
            this.logChannelId = channelId;
            return this;
        }
        
        public Builder withAppealChannelId(String channelId) {
            this.appealChannelId = channelId;
            return this;
        }
        
        public Builder withAiAnalysisEnabled(boolean enabled) {
            this.aiAnalysisEnabled = enabled;
            return this;
        }
        
        public Builder withContextAwareModeration(boolean enabled) {
            this.contextAwareModeration = enabled;
            return this;
        }
        
        public Builder withCrossGuildDataSharing(boolean enabled) {
            this.crossGuildDataSharing = enabled;
            return this;
        }
        
        public Builder addCustomSetting(String key, Object value) {
            this.customSettings.put(key, value);
            return this;
        }
        
        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder withUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder withCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder withUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }
        
        public Builder withConfigVersion(int version) {
            this.configVersion = version;
            return this;
        }
        
        public GuildModerationConfig build() {
            return new GuildModerationConfig(this);
        }
    }
}