package com.axion.bot.moderation;

/**
 * Enum for different types of moderation rule conditions
 */
public enum ConditionType {
    MESSAGE_FREQUENCY,
    TOXICITY_SCORE,
    SPAM_SCORE,
    CAPS_PERCENTAGE,
    MENTION_COUNT,
    LINK_COUNT,
    WORD_MATCH,
    REGEX_MATCH,
    USER_REPUTATION,
    RAPID_JOINS,
    DUPLICATE_CONTENT,
    CUSTOM_CONDITION,
    // Additional values from SmartAutoModerationEngine
    CONTENT,
    BEHAVIOR,
    CONTEXT,
    USER_HISTORY,
    FREQUENCY,
    CUSTOM
}