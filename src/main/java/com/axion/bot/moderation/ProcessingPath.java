package com.axion.bot.moderation;

/**
 * Enum representing the processing path for an appeal
 */
public enum ProcessingPath {
    AUTO_REVIEW,
    MANUAL_REVIEW,
    PRIORITY_REVIEW,
    ESCALATED_REVIEW,
    FAST_TRACK
}
