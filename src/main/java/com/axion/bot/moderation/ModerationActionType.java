package com.axion.bot.moderation;

/**
 * Enumeration of moderation action types with severity levels
 */
public enum ModerationActionType {
    NONE(0, "No Action"),
    LOG_ONLY(0, "Log Only"),
    WARN(2, "Warning"),
    DELETE_MESSAGE(1, "Delete Message"),
    TIMEOUT(3, "Timeout"),
    KICK(4, "Kick"),
    BAN(5, "Ban"),
    MUTE(3, "Mute"),
    QUARANTINE(4, "Quarantine");
    
    private final int severity;
    private final String displayName;
    
    ModerationActionType(int severity, String displayName) {
        this.severity = severity;
        this.displayName = displayName;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}