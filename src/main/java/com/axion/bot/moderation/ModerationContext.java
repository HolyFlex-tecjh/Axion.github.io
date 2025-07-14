package com.axion.bot.moderation;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Context information for moderation evaluation
 */
public class ModerationContext {
    private final String userId;
    private final String guildId;
    private final String channelId;
    private final String content;
    private final String channelType;
    private final String channelName;
    private final Set<String> userRoles;
    private final String timezone;
    private final Map<String, Object> additionalContext;
    
    private ModerationContext(Builder builder) {
        this.userId = builder.userId;
        this.guildId = builder.guildId;
        this.channelId = builder.channelId;
        this.content = builder.content;
        this.channelType = builder.channelType;
        this.channelName = builder.channelName;
        this.userRoles = new HashSet<>(builder.userRoles);
        this.timezone = builder.timezone;
        this.additionalContext = new HashMap<>(builder.additionalContext);
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public String getChannelId() { return channelId; }
    public String getContent() { return content; }
    public String getChannelType() { return channelType; }
    public String getChannelName() { return channelName; }
    public Set<String> getUserRoles() { return userRoles; }
    public String getTimezone() { return timezone; }
    public Map<String, Object> getAdditionalContext() { return additionalContext; }
    
    public Object getContextValue(String key) {
        return additionalContext.get(key);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private String guildId;
        private String channelId;
        private String content;
        private String channelType = "TEXT";
        private String channelName;
        private Set<String> userRoles = new HashSet<>();
        private String timezone = "UTC";
        private Map<String, Object> additionalContext = new HashMap<>();
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder guildId(String guildId) {
            this.guildId = guildId;
            return this;
        }
        
        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder channelType(String channelType) {
            this.channelType = channelType;
            return this;
        }
        
        public Builder channelName(String channelName) {
            this.channelName = channelName;
            return this;
        }
        
        public Builder userRoles(Set<String> userRoles) {
            this.userRoles = new HashSet<>(userRoles);
            return this;
        }
        
        public Builder addUserRole(String role) {
            this.userRoles.add(role);
            return this;
        }
        
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }
        
        public Builder additionalContext(Map<String, Object> context) {
            this.additionalContext = new HashMap<>(context);
            return this;
        }
        
        public Builder addContext(String key, Object value) {
            this.additionalContext.put(key, value);
            return this;
        }
        
        public ModerationContext build() {
            if (userId == null || guildId == null || channelId == null) {
                throw new IllegalArgumentException("userId, guildId, and channelId are required");
            }
            return new ModerationContext(this);
        }
    }
}