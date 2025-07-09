package com.axion.bot.moderation;

/**
 * Enum representing different types of raids
 */
public enum RaidType {
    JOIN_SPAM("Join Spam", "👥"),
    MESSAGE_SPAM("Message Spam", "💬"),
    COORDINATED_ATTACK("Coordinated Attack", "⚔️"),
    BOT_RAID("Bot Raid", "🤖"),
    MASS_MENTION("Mass Mention", "@"),
    CHANNEL_SPAM("Channel Spam", "📢"),
    ROLE_SPAM("Role Spam", "🏷️"),
    UNKNOWN("Unknown", "❓");
    
    private final String description;
    private final String emoji;
    
    RaidType(String description, String emoji) {
        this.description = description;
        this.emoji = emoji;
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