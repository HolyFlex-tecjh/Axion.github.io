package com.axion.bot.moderation;

/**
 * Enum for different types of behavior patterns
 */
public enum PatternType {
    SPAM("Spam"), 
    TOXICITY("Toxicity"), 
    SUSPICIOUS_ACTIVITY("Suspicious Activity"), 
    ESCALATION("Escalation"), 
    SUSPICIOUS_TIMING("Suspicious Timing"), 
    EVASION("Evasion"), 
    SOCIAL_ENGINEERING("Social Engineering"), 
    RAID_PARTICIPATION("Raid Participation");
    
    private final String displayName;
    
    PatternType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}