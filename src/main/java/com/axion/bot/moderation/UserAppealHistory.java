package com.axion.bot.moderation;

import java.util.*;

/**
 * Model class representing a user's appeal history
 */
public class UserAppealHistory {
    private final String userId;
    private final String guildId;
    private final List<Appeal> appeals;
    private final UserAppealStats stats;
    
    public UserAppealHistory(String userId, String guildId, List<Appeal> appeals, UserAppealStats stats) {
        this.userId = userId;
        this.guildId = guildId;
        this.appeals = new ArrayList<>(appeals);
        this.stats = stats;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public List<Appeal> getAppeals() { return new ArrayList<>(appeals); }
    public UserAppealStats getStats() { return stats; }
}