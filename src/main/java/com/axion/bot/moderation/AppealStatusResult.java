package com.axion.bot.moderation;

import java.time.Duration;

/**
 * Model class representing the result of an appeal status query
 */
public class AppealStatusResult {
    private final boolean found;
    private final Appeal appeal;
    private final Duration estimatedProcessingTime;
    private final String message;
    
    private AppealStatusResult(boolean found, Appeal appeal, Duration estimatedProcessingTime, String message) {
        this.found = found;
        this.appeal = appeal;
        this.estimatedProcessingTime = estimatedProcessingTime;
        this.message = message;
    }
    
    public static AppealStatusResult notFound(String appealId) {
        return new AppealStatusResult(false, null, null, "Appeal not found: " + appealId);
    }
    
    public AppealStatusResult(Appeal appeal, Duration estimatedProcessingTime) {
        this(true, appeal, estimatedProcessingTime, "Appeal found");
    }
    
    // Getters
    public boolean isFound() { return found; }
    public Appeal getAppeal() { return appeal; }
    public Duration getEstimatedProcessingTime() { return estimatedProcessingTime; }
    public String getMessage() { return message; }
}