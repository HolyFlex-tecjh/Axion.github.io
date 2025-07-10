package com.axion.bot.tickets;

/**
 * Status for tickets
 */
public enum TicketStatus {
    OPEN("Åben", "\uD83D\uDFE2"),
    IN_PROGRESS("I gang", "\uD83D\uDFE1"),
    WAITING_FOR_USER("Venter på bruger", "\u23F3"),
    CLOSED("Lukket", "\uD83D\uDD34");

    private final String displayName;
    private final String emoji;

    TicketStatus(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public static TicketStatus fromString(String status) {
        if (status == null) return OPEN;
        
        return switch (status.toLowerCase()) {
            case "open", "åben" -> OPEN;
            case "in_progress", "i_gang" -> IN_PROGRESS;
            case "waiting_for_user", "venter_på_bruger" -> WAITING_FOR_USER;
            case "closed", "lukket" -> CLOSED;
            default -> OPEN;
        };
    }

    @Override
    public String toString() {
        return emoji + " " + displayName;
    }
}