package com.axion.bot.moderation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents differences between two moderation configurations
 */
public class ConfigurationDiff {
    private final List<ConfigurationChange> changes;
    
    public ConfigurationDiff(List<ConfigurationChange> changes) {
        this.changes = new ArrayList<>(changes);
    }
    
    public List<ConfigurationChange> getChanges() {
        return new ArrayList<>(changes);
    }
}