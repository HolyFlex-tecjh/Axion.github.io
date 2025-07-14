package com.axion.bot.moderation;

/**
 * Enum for different types of moderation actions
 */
public enum ActionType {
    DELETE_MESSAGE,
    WARN_USER,
    TIMEOUT_USER,
    KICK_USER,
    BAN_USER,
    LOCKDOWN_CHANNEL,
    SLOW_MODE,
    NOTIFY_MODERATORS,
    LOG_VIOLATION,
    CUSTOM_ACTION
}