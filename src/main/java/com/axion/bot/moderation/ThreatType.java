package com.axion.bot.moderation;

/**
 * Enum representing different types of threats
 */
public enum ThreatType {
    PHISHING("Phishing", "🎣"),
    MALWARE("Malware", "🦠"),
    SCAM("Scam", "💰"),
    DOXXING("Doxxing", "📋"),
    MALICIOUS_LINK("Malicious Link", "🔗"),
    SUSPICIOUS_ACTIVITY("Suspicious Activity", "⚠️"),
    HARASSMENT("Harassment", "😡"),
    SPAM("Spam", "📧"),
    INAPPROPRIATE_CONTENT("Inappropriate Content", "🚫");
    
    private final String description;
    private final String emoji;
    
    ThreatType(String description, String emoji) {
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