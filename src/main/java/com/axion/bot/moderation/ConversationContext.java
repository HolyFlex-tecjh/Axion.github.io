package com.axion.bot.moderation;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents the context of a conversation for AI content analysis
 */
public class ConversationContext {
    private final String userId;
    private final String guildId;
    private final String channelId;
    private final List<String> recentMessages;
    private final UserBehaviorProfile userProfile;
    private final boolean hasRecentViolations;
    private final boolean isInSensitiveChannel;
    private final Instant conversationStart;
    
    public ConversationContext(String userId, String guildId, String channelId, 
                              List<String> recentMessages, UserBehaviorProfile userProfile,
                              boolean hasRecentViolations, boolean isInSensitiveChannel) {
        this.userId = userId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.recentMessages = new ArrayList<>(recentMessages);
        this.userProfile = userProfile;
        this.hasRecentViolations = hasRecentViolations;
        this.isInSensitiveChannel = isInSensitiveChannel;
        this.conversationStart = Instant.now();
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public String getChannelId() { return channelId; }
    public List<String> getRecentMessages() { return new ArrayList<>(recentMessages); }
    public UserBehaviorProfile getUserProfile() { return userProfile; }
    public boolean hasRecentViolations() { return hasRecentViolations; }
    public boolean isInSensitiveChannel() { return isInSensitiveChannel; }
    public Instant getConversationStart() { return conversationStart; }
    
    /**
     * Builder pattern for ConversationContext
     */
    public static class Builder {
        private String userId;
        private String guildId;
        private String channelId;
        private List<String> recentMessages = new ArrayList<>();
        private UserBehaviorProfile userProfile;
        private boolean hasRecentViolations = false;
        private boolean isInSensitiveChannel = false;
        
        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder setGuildId(String guildId) {
            this.guildId = guildId;
            return this;
        }
        
        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }
        
        public Builder setRecentMessages(List<String> recentMessages) {
            this.recentMessages = new ArrayList<>(recentMessages);
            return this;
        }
        
        public Builder setUserProfile(UserBehaviorProfile userProfile) {
            this.userProfile = userProfile;
            return this;
        }
        
        public Builder setHasRecentViolations(boolean hasRecentViolations) {
            this.hasRecentViolations = hasRecentViolations;
            return this;
        }
        
        public Builder setIsInSensitiveChannel(boolean isInSensitiveChannel) {
            this.isInSensitiveChannel = isInSensitiveChannel;
            return this;
        }
        
        public ConversationContext build() {
            return new ConversationContext(userId, guildId, channelId, recentMessages, 
                                         userProfile, hasRecentViolations, isInSensitiveChannel);
        }
    }
}