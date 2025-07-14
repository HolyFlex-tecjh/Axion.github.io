package com.axion.bot.moderation;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a moderation rule with conditions and actions
 */
public class ModerationRule {
    private final String id;
    private final String name;
    private final String description;
    private final boolean enabled;
    private final int priority;
    private final double weight;
    private final ModerationSeverity severity;
    private final List<RuleCondition> conditions;
    private final LogicOperator logicOperator;
    private final Set<String> guildIds;
    private final Set<String> channelIds;
    private final Set<String> excludedRoles;
    private final Set<String> exemptUsers;
    private final TimeRestriction timeRestrictions;
    
    private ModerationRule(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.enabled = builder.enabled;
        this.priority = builder.priority;
        this.weight = builder.weight;
        this.severity = builder.severity;
        this.conditions = new ArrayList<>(builder.conditions);
        this.logicOperator = builder.logicOperator;
        this.guildIds = new HashSet<>(builder.guildIds);
        this.channelIds = new HashSet<>(builder.channelIds);
        this.excludedRoles = new HashSet<>(builder.excludedRoles);
        this.exemptUsers = new HashSet<>(builder.exemptUsers);
        this.timeRestrictions = builder.timeRestrictions;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public int getPriority() { return priority; }
    public double getWeight() { return weight; }
    public ModerationSeverity getSeverity() { return severity; }
    public List<RuleCondition> getConditions() { return conditions; }
    public LogicOperator getLogicOperator() { return logicOperator; }
    public Set<String> getGuildIds() { return guildIds; }
    public Set<String> getChannelIds() { return channelIds; }
    public Set<String> getExcludedRoles() { return excludedRoles; }
    public Set<String> getExemptUsers() { return exemptUsers; }
    public TimeRestriction getTimeRestrictions() { return timeRestrictions; }
    
    public static Builder builder(String id) {
        return new Builder(id);
    }
    
    public static class Builder {
        private final String id;
        private String name;
        private String description;
        private boolean enabled = true;
        private int priority = 0;
        private double weight = 1.0;
        private ModerationSeverity severity = ModerationSeverity.LOW;
        private List<RuleCondition> conditions = new ArrayList<>();
        private LogicOperator logicOperator = LogicOperator.AND;
        private Set<String> guildIds = new HashSet<>();
        private Set<String> channelIds = new HashSet<>();
        private Set<String> excludedRoles = new HashSet<>();
        private Set<String> exemptUsers = new HashSet<>();
        private TimeRestriction timeRestrictions;
        
        public Builder(String id) {
            this.id = id;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }
        
        public Builder severity(ModerationSeverity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder addCondition(RuleCondition condition) {
            this.conditions.add(condition);
            return this;
        }
        
        public Builder conditions(List<RuleCondition> conditions) {
            this.conditions = new ArrayList<>(conditions);
            return this;
        }
        
        public Builder logicOperator(LogicOperator operator) {
            this.logicOperator = operator;
            return this;
        }
        
        public Builder addGuildId(String guildId) {
            this.guildIds.add(guildId);
            return this;
        }
        
        public Builder guildIds(Set<String> guildIds) {
            this.guildIds = new HashSet<>(guildIds);
            return this;
        }
        
        public Builder addChannelId(String channelId) {
            this.channelIds.add(channelId);
            return this;
        }
        
        public Builder channelIds(Set<String> channelIds) {
            this.channelIds = new HashSet<>(channelIds);
            return this;
        }
        
        public Builder addExcludedRole(String role) {
            this.excludedRoles.add(role);
            return this;
        }
        
        public Builder excludedRoles(Set<String> roles) {
            this.excludedRoles = new HashSet<>(roles);
            return this;
        }
        
        public Builder addExemptUser(String userId) {
            this.exemptUsers.add(userId);
            return this;
        }
        
        public Builder exemptUsers(Set<String> users) {
            this.exemptUsers = new HashSet<>(users);
            return this;
        }
        
        public Builder timeRestrictions(TimeRestriction timeRestrictions) {
            this.timeRestrictions = timeRestrictions;
            return this;
        }
        
        public ModerationRule build() {
            if (name == null) {
                throw new IllegalArgumentException("Rule name is required");
            }
            if (conditions.isEmpty()) {
                throw new IllegalArgumentException("At least one condition is required");
            }
            return new ModerationRule(this);
        }
    }
}

/**
 * Represents a condition within a moderation rule
 */
class RuleCondition {
    private final String type;
    private final ComparisonOperator operator;
    private final Object value;
    private final double weight;
    
    public RuleCondition(String type, ComparisonOperator operator, Object value, double weight) {
        this.type = type;
        this.operator = operator;
        this.value = value;
        this.weight = weight;
    }
    
    public String getType() { return type; }
    public ComparisonOperator getOperator() { return operator; }
    public Object getValue() { return value; }
    public double getWeight() { return weight; }
}



/**
 * Time restrictions for rules
 */
class TimeRestriction {
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Set<Integer> allowedDays; // 1-7, Monday to Sunday
    
    public TimeRestriction(LocalTime startTime, LocalTime endTime, Set<Integer> allowedDays) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.allowedDays = new HashSet<>(allowedDays);
    }
    
    public boolean isTimeAllowed(LocalTime currentTime) {
        if (startTime != null && endTime != null) {
            if (startTime.isBefore(endTime)) {
                return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            } else {
                // Overnight period (e.g., 22:00 to 06:00)
                return !currentTime.isAfter(endTime) || !currentTime.isBefore(startTime);
            }
        }
        return true;
    }
    
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Set<Integer> getAllowedDays() { return allowedDays; }
}