package com.axion.bot.moderation;

/**
 * Enum representing different types of spam
 */
public enum SpamType {
    HIGH_FREQUENCY("High Frequency", "⚡"),
    IDENTICAL_MESSAGES("Identical Messages", "📋"),
    SIMILAR_MESSAGES("Similar Messages", "📄"),
    SPAM_PATTERNS("Spam Patterns", "🔍"),
    EXCESSIVE_MENTIONS("Excessive Mentions", "@"),
    EXCESSIVE_LINKS("Excessive Links", "🔗"),
    FAST_TYPING("Fast Typing", "⌨️"),
    SUSPICIOUS_LINKS("Suspicious Links", "⚠️"),
    BOT_BEHAVIOR("Bot Behavior", "🤖"),
    REPETITIVE_CONTENT("Repetitive Content", "🔄"),
    CAPS_SPAM("Caps Spam", "🔠"),
    EMOJI_SPAM("Emoji Spam", "😀");
    
    private final String description;
    private final String emoji;
    
    SpamType(String description, String emoji) {
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