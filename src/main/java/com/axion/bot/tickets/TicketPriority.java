package com.axion.bot.tickets;

/**
 * Prioritetsniveauer for tickets
 */
public enum TicketPriority {
    LOW("Lav", "\uD83D\uDFE2", 1),
    MEDIUM("Medium", "\uD83D\uDFE1", 2),
    HIGH("Høj", "\uD83D\uDFE0", 3),
    URGENT("Akut", "\uD83D\uDD34", 4);

    private final String displayName;
    private final String emoji;
    private final int level;

    TicketPriority(String displayName, String emoji, int level) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getLevel() {
        return level;
    }

    public static TicketPriority fromString(String priority) {
        if (priority == null) return MEDIUM;
        
        return switch (priority.toLowerCase()) {
            case "low", "lav" -> LOW;
            case "medium" -> MEDIUM;
            case "high", "høj" -> HIGH;
            case "urgent", "akut" -> URGENT;
            default -> MEDIUM;
        };
    }

    @Override
    public String toString() {
        return emoji + " " + displayName;
    }
}