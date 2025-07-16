package com.axion.bot.moderation;

import java.time.Duration;
import java.util.*;
import com.axion.bot.moderation.ConditionType;
import com.axion.bot.moderation.ActionType;

/**
 * Rule condition definition
 */
public class RuleCondition {
    private final ConditionType type;
    private final double threshold;
    private final Duration timeWindow;
    private final String pattern;
    private final List<String> values;
    private final Map<String, Object> customParameters;
    
    private RuleCondition(Builder builder) {
        this.type = builder.type;
        this.threshold = builder.threshold;
        this.timeWindow = builder.timeWindow;
        this.pattern = builder.pattern;
        this.values = new ArrayList<>(builder.values);
        this.customParameters = new HashMap<>(builder.customParameters);
    }
    
    // Getters
    public ConditionType getType() { return type; }
    public double getThreshold() { return threshold; }
    public Duration getTimeWindow() { return timeWindow; }
    public String getPattern() { return pattern; }
    public List<String> getValues() { return new ArrayList<>(values); }
    public Map<String, Object> getCustomParameters() { return new HashMap<>(customParameters); }
    
    public static class Builder {
        private ConditionType type;
        private double threshold = 0.0;
        private Duration timeWindow;
        private String pattern;
        private List<String> values = new ArrayList<>();
        private Map<String, Object> customParameters = new HashMap<>();
        
        public Builder withType(ConditionType type) {
            this.type = type;
            return this;
        }
        
        public Builder withThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }
        
        public Builder withTimeWindow(Duration timeWindow) {
            this.timeWindow = timeWindow;
            return this;
        }
        
        public Builder withPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder withValues(List<String> values) {
            this.values = new ArrayList<>(values);
            return this;
        }
        
        public Builder withCustomParameters(Map<String, Object> customParameters) {
            this.customParameters = new HashMap<>(customParameters);
            return this;
        }
        
        public RuleCondition build() {
            return new RuleCondition(this);
        }
    }
}