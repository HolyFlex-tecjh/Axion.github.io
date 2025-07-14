package com.axion.bot.moderation;

import net.dv8tion.jda.api.entities.User;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Context information about a user for moderation decisions
 */
public class UserContext {
    private final User user;
    private final UserModerationProfile profile;
    private final Set<String> roles;
    private final Map<String, Object> additionalData;
    private final Instant contextCreated;
    
    public UserContext(User user, UserModerationProfile profile) {
        this(user, profile, Set.of(), Map.of());
    }
    
    public UserContext(User user, UserModerationProfile profile, 
                      Set<String> roles, Map<String, Object> additionalData) {
        this.user = user;
        this.profile = profile;
        this.roles = roles;
        this.additionalData = additionalData;
        this.contextCreated = Instant.now();
    }
    
    // Getters
    public User getUser() { return user; }
    public UserModerationProfile getProfile() { return profile; }
    public Set<String> getRoles() { return roles; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    public Instant getContextCreated() { return contextCreated; }
    
    /**
     * Get user ID
     */
    public String getUserId() {
        return user.getId();
    }
    
    /**
     * Get user's trust score
     */
    public int getTrustScore() {
        return profile != null ? profile.getTrustScore() : 100;
    }
    
    /**
     * Get user's risk level
     */
    public double getRiskLevel() {
        return profile != null ? profile.getRiskLevel() : 0.0;
    }
    
    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String roleId) {
        return roles.contains(roleId);
    }
    
    /**
     * Get additional context data
     */
    public Object getContextData(String key) {
        return additionalData.get(key);
    }
    
    /**
     * Check if user is new (account age)
     */
    public boolean isNewUser() {
        if (profile == null) return true;
        return profile.getAccountAge().toDays() < 7;
    }
    
    /**
     * Get recent violation count
     */
    public long getRecentViolationCount() {
        return profile != null ? profile.getRecentViolationCount() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("UserContext{userId='%s', trustScore=%d, riskLevel=%.2f}",
            getUserId(), getTrustScore(), getRiskLevel());
    }
}