package com.axion.bot.moderation;

/**
 * Enum representing different levels of spam likelihood
 */
public enum SpamLikelihood {
    VERY_LOW(0, "Very Low", "\uD83D\uDFE2"),
    LOW(1, "Low", "\uD83D\uDFE1"),
    MEDIUM(2, "Medium", "\uD83D\uDFE0"),
    HIGH(3, "High", "\uD83D\uDD34"),
    VERY_HIGH(4, "Very High", "\u26AB");
    
    private final int level;
    private final String description;
    private final String emoji;
    
    SpamLikelihood(int level, String description, String emoji) {
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