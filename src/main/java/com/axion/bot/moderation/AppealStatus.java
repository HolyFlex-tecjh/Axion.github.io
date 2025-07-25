package com.axion.bot.moderation;

/**
 * Enum representing the status of an appeal
 */
public enum AppealStatus {
    PENDING_ANALYSIS,
    PENDING_REVIEW,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    EXPIRED,
    CANCELLED
}
