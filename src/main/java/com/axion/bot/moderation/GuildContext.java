package com.axion.bot.moderation;

import net.dv8tion.jda.api.entities.Guild;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Context information about a guild for moderation decisions
 */
public class GuildContext {
    private final Guild guild;
    private final GuildModerationConfig config;
    private final Set<String> activeChannels;
    private final Map<String, Object> guildMetadata;
    private final Instant contextCreated;
    
    public GuildContext(Guild guild) {
        this(guild, null, Set.of(), Map.of());
    }
    
    public GuildContext(Guild guild, GuildModerationConfig config, 
                       Set<String> activeChannels, Map<String, Object> guildMetadata) {
        this.guild = guild;
        this.config = config;
        this.activeChannels = activeChannels;
        this.guildMetadata = guildMetadata;
        this.contextCreated = Instant.now();
    }
    
    // Getters
    public Guild getGuild() { return guild; }
    public GuildModerationConfig getConfig() { return config; }
    public Set<String> getActiveChannels() { return activeChannels; }
    public Map<String, Object> getGuildMetadata() { return guildMetadata; }
    public Instant getContextCreated() { return contextCreated; }
    
    /**
     * Get guild ID
     */
    public String getGuildId() {
        return guild.getId();
    }
    
    /**
     * Get guild name
     */
    public String getGuildName() {
        return guild.getName();
    }
    
    /**
     * Get guild member count
     */
    public int getMemberCount() {
        return guild.getMemberCount();
    }
    
    /**
     * Check if moderation is enabled
     */
    public boolean isModerationEnabled() {
        return config != null && config.isModerationEnabled();
    }
    
    /**
     * Check if auto-moderation is enabled
     */
    public boolean isAutoModerationEnabled() {
        return config != null && config.isAutoModerationEnabled();
    }
    
    /**
     * Check if a channel is active
     */
    public boolean isChannelActive(String channelId) {
        return activeChannels.contains(channelId);
    }
    
    /**
     * Get guild metadata
     */
    public Object getMetadata(String key) {
        return guildMetadata.get(key);
    }
    
    /**
     * Check if guild has specific feature enabled
     */
    public boolean hasFeatureEnabled(String feature) {
        if (config == null) return false;
        
        return switch (feature.toLowerCase()) {
            case "spam_detection" -> config.isSpamDetectionEnabled();
            case "toxicity_detection" -> config.isToxicityDetectionEnabled();
            case "raid_protection" -> config.isRaidProtectionEnabled();
            case "behavioral_analysis" -> config.isBehavioralAnalysisEnabled();
            default -> false;
        };
    }
    
    /**
     * Get guild verification level
     */
    public Guild.VerificationLevel getVerificationLevel() {
        return guild.getVerificationLevel();
    }
    
    /**
     * Check if guild is considered large
     */
    public boolean isLargeGuild() {
        return getMemberCount() > 1000;
    }
    
    /**
     * Get guild boost level
     */
    public Guild.BoostTier getBoostTier() {
        return guild.getBoostTier();
    }
    
    @Override
    public String toString() {
        return String.format("GuildContext{guildId='%s', name='%s', members=%d, moderationEnabled=%s}",
            getGuildId(), getGuildName(), getMemberCount(), isModerationEnabled());
    }
}