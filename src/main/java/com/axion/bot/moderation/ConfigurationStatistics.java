package com.axion.bot.moderation;

import java.time.Instant;

/**
 * Statistics about moderation configuration usage
 */
public class ConfigurationStatistics {
    private final String guildId;
    private final int totalConfigurations;
    private final Instant firstConfigured;
    private final Instant lastModified;
    private final int totalRules;
    private final int activeFilters;
    
    public ConfigurationStatistics(String guildId, int totalConfigurations, Instant firstConfigured,
                                  Instant lastModified, int totalRules, int activeFilters) {
        this.guildId = guildId;
        this.totalConfigurations = totalConfigurations;
        this.firstConfigured = firstConfigured;
        this.lastModified = lastModified;
        this.totalRules = totalRules;
        this.activeFilters = activeFilters;
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public int getTotalConfigurations() { return totalConfigurations; }
    public Instant getFirstConfigured() { return firstConfigured; }
    public Instant getLastModified() { return lastModified; }
    public int getTotalRules() { return totalRules; }
    public int getActiveFilters() { return activeFilters; }
}