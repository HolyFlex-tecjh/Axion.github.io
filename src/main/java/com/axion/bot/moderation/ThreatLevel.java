package com.axion.bot.moderation;

/**
 * Enum representing different threat levels
 */
public enum ThreatLevel {
    NONE(0, "No Threat", "\uD83D\uDFE2"),
    LOW(1, "Low Threat", "\uD83D\uDFE1"),
    MEDIUM(2, "Medium Threat", "\uD83D\uDFE0"),
    HIGH(3, "High Threat", "\uD83D\uDD34"),
    VERY_HIGH(4, "Very High Threat", "\u26AB");
    
    private final int level;
    private final String description;
    private final String emoji;
    
    ThreatLevel(int level, String description, String emoji) {
        this.level = level;
        this.description = description;
        this.emoji = emoji;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    @Override
    public String toString() {
        return emoji + " " + description;
    }
}