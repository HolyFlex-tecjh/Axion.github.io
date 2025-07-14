package com.axion.bot.moderation;

/**
 * Represents a single change in configuration
 */
public class ConfigurationChange {
    private final String path;
    private final ChangeType type;
    private final Object oldValue;
    private final Object newValue;
    
    public ConfigurationChange(String path, ChangeType type, Object oldValue, Object newValue) {
        this.path = path;
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    // Getters
    public String getPath() { return path; }
    public ChangeType getType() { return type; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
}