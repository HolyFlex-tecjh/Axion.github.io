package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a final moderation decision
 */
public class ModerationDecision {
    private final ModerationActionType action;
    private final String reason;
    private final double confidence;
    private final ModerationSeverity severity;
    private final List<String> triggeredRules;
    private final Map<String, Object> actionParameters;
    private final Instant timestamp;
    private final long processingTimeMs;
    private final String error;
    private final Duration duration;
    
    private ModerationDecision(Builder builder) {
        this.action = builder.action;
        this.reason = builder.reason;
        this.confidence = builder.confidence;
        this.severity = builder.severity;
        this.triggeredRules = new ArrayList<>(builder.triggeredRules);
        this.actionParameters = new HashMap<>(builder.actionParameters);
        this.timestamp = Instant.now();
        this.processingTimeMs = builder.processingTimeMs;
        this.error = builder.error;
        this.duration = builder.duration;
    }
    
    // Static factory methods
    public static ModerationDecision noAction(String reason) {
        return new Builder(ModerationActionType.NONE)
            .reason(reason)
            .confidence(0.0)
            .severity(ModerationSeverity.NONE)
            .build();
    }
    
    public static ModerationDecision warn(String reason, double confidence) {
        return new Builder(ModerationActionType.WARN)
            .reason(reason)
            .confidence(confidence)
            .severity(ModerationSeverity.LOW)
            .build();
    }
    
    public static ModerationDecision delete(String reason, double confidence) {
        return new Builder(ModerationActionType.DELETE_MESSAGE)
            .reason(reason)
            .confidence(confidence)
            .severity(ModerationSeverity.MEDIUM)
            .build();
    }
    
    public static ModerationDecision timeout(String reason, double confidence, long durationMinutes) {
        return new Builder(ModerationActionType.TIMEOUT)
            .reason(reason)
            .confidence(confidence)
            .severity(ModerationSeverity.HIGH)
            .addParameter("duration_minutes", durationMinutes)
            .build();
    }
    
    public static ModerationDecision kick(String reason, double confidence) {
        return new Builder(ModerationActionType.KICK)
            .reason(reason)
            .confidence(confidence)
            .severity(ModerationSeverity.HIGH)
            .build();
    }
    
    public static ModerationDecision ban(String reason, double confidence, long durationDays) {
        return new Builder(ModerationActionType.BAN)
            .reason(reason)
            .confidence(confidence)
            .severity(ModerationSeverity.VERY_HIGH)
            .addParameter("duration_days", durationDays)
            .build();
    }
    
    public static ModerationDecision error(String errorMessage) {
        return new Builder(ModerationActionType.NONE)
            .reason("Processing error")
            .confidence(0.0)
            .severity(ModerationSeverity.NONE)
            .error(errorMessage)
            .build();
    }
    
    // Getters
    public ModerationActionType getAction() { return action; }
    public String getReason() { return reason; }
    public double getConfidence() { return confidence; }
    public ModerationSeverity getSeverity() { return severity; }
    public List<String> getTriggeredRules() { return triggeredRules; }
    public Map<String, Object> getActionParameters() { return actionParameters; }
    public Instant getTimestamp() { return timestamp; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public String getError() { return error; }
    public boolean hasError() { return error != null; }
    public Duration getDuration() { return duration; }
    
    public Object getParameter(String key) {
        return actionParameters.get(key);
    }
    
    public <T> T getParameter(String key, Class<T> type) {
        Object value = actionParameters.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    /**
     * Check if this decision requires immediate action
     */
    public boolean requiresImmediateAction() {
        return action != ModerationActionType.NONE && 
               action != ModerationActionType.LOG_ONLY &&
               severity.ordinal() >= ModerationSeverity.MEDIUM.ordinal();
    }
    
    /**
     * Check if this decision should be logged
     */
    public boolean shouldLog() {
        return action != ModerationActionType.NONE || hasError();
    }
    
    /**
     * Get a human-readable description of the decision
     */
    public String getDescription() {
        if (hasError()) {
            return "Error: " + error;
        }
        
        StringBuilder desc = new StringBuilder();
        desc.append("Action: ").append(action.getDisplayName());
        
        if (reason != null && !reason.isEmpty()) {
            desc.append(", Reason: ").append(reason);
        }
        
        desc.append(", Confidence: ").append(String.format("%.2f", confidence));
        desc.append(", Severity: ").append(severity);
        
        if (!triggeredRules.isEmpty()) {
            desc.append(", Rules: ").append(String.join(", ", triggeredRules));
        }
        
        return desc.toString();
    }
    
    public static Builder builder(ModerationActionType action) {
        return new Builder(action);
    }
    
    public static class Builder {
        private final ModerationActionType action;
        private String reason;
        private double confidence = 0.0;
        private ModerationSeverity severity = ModerationSeverity.NONE;
        private List<String> triggeredRules = new ArrayList<>();
        private Map<String, Object> actionParameters = new HashMap<>();
        private long processingTimeMs = 0;
        private String error;
        private Duration duration;
        
        public Builder(ModerationActionType action) {
            this.action = action;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder confidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
            return this;
        }
        
        public Builder severity(ModerationSeverity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder addTriggeredRule(String ruleName) {
            this.triggeredRules.add(ruleName);
            return this;
        }
        
        public Builder triggeredRules(List<String> rules) {
            this.triggeredRules = new ArrayList<>(rules);
            return this;
        }
        
        public Builder addParameter(String key, Object value) {
            this.actionParameters.put(key, value);
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.actionParameters = new HashMap<>(parameters);
            return this;
        }
        
        public Builder processingTime(long timeMs) {
            this.processingTimeMs = timeMs;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }
        
        public ModerationDecision build() {
            return new ModerationDecision(this);
        }
    }
    
    @Override
    public String toString() {
        return "ModerationDecision{" +
                "action=" + action +
                ", reason='" + reason + '\'' +
                ", confidence=" + confidence +
                ", severity=" + severity +
                ", triggeredRules=" + triggeredRules.size() +
                ", hasError=" + hasError() +
                '}';
    }
}