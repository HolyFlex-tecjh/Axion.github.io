package com.axion.bot.moderation;

/**
 * Enum der definerer forskellige niveauer af moderation alvorlighed
 */
public enum ModerationSeverity {
    /**
     * Meget lav alvorlighed - kun logging
     */
    VERY_LOW(1, "Meget Lav", "🟢"),
    
    /**
     * Lav alvorlighed - advarsel
     */
    LOW(2, "Lav", "🟡"),
    
    /**
     * Mellem alvorlighed - timeout
     */
    MEDIUM(3, "Mellem", "🟠"),
    
    /**
     * Høj alvorlighed - kick
     */
    HIGH(4, "Høj", "🔴"),
    
    /**
     * Meget høj alvorlighed - ban
     */
    VERY_HIGH(5, "Meget Høj", "⚫");
    
    private final int level;
    private final String description;
    private final String emoji;
    
    ModerationSeverity(int level, String description, String emoji) {
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
    
    /**
     * Konverterer fra integer til ModerationSeverity
     */
    public static ModerationSeverity fromLevel(int level) {
        switch (level) {
            case 1:
                return VERY_LOW;
            case 2:
                return LOW;
            case 3:
                return MEDIUM;
            case 4:
                return HIGH;
            case 5:
                return VERY_HIGH;
            default:
                return LOW;
        }
    }
    
    /**
     * Eskalerer til næste niveau
     */
    public ModerationSeverity escalate() {
        switch (this) {
            case VERY_LOW:
                return LOW;
            case LOW:
                return MEDIUM;
            case MEDIUM:
                return HIGH;
            case HIGH:
            case VERY_HIGH:
                return VERY_HIGH;
            default:
                return this;
        }
    }
    
    @Override
    public String toString() {
        return emoji + " " + description + " (" + level + "/5)";
    }
}