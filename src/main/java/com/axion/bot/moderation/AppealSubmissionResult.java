package com.axion.bot.moderation;

import java.time.Duration;
import java.util.*;

/**
 * Model class representing the result of an appeal submission
 */
public class AppealSubmissionResult {
    private final boolean success;
    private final Appeal appeal;
    private final List<String> errors;
    private final Duration estimatedProcessingTime;
    private final AutoReviewResult autoReviewResult;
    private final AppealExecutionResult executionResult;
    private final String message;
    
    private AppealSubmissionResult(boolean success, Appeal appeal, List<String> errors,
                                  Duration estimatedProcessingTime, AutoReviewResult autoReviewResult,
                                  AppealExecutionResult executionResult, String message) {
        this.success = success;
        this.appeal = appeal;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.estimatedProcessingTime = estimatedProcessingTime;
        this.autoReviewResult = autoReviewResult;
        this.executionResult = executionResult;
        this.message = message;
    }
    
    public static AppealSubmissionResult rejected(List<String> errors) {
        return new AppealSubmissionResult(false, null, errors, null, null, null, "Appeal rejected");
    }
    
    public static AppealSubmissionResult queued(Appeal appeal, Duration estimatedTime) {
        return new AppealSubmissionResult(true, appeal, null, estimatedTime, null, null, "Appeal queued for review");
    }
    
    public static AppealSubmissionResult autoProcessed(Appeal appeal, AutoReviewResult autoResult, 
                                                      AppealExecutionResult executionResult) {
        return new AppealSubmissionResult(true, appeal, null, null, autoResult, executionResult, "Appeal auto-processed");
    }
    
    public static AppealSubmissionResult error(String message) {
        return new AppealSubmissionResult(false, null, Arrays.asList(message), null, null, null, message);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public Appeal getAppeal() { return appeal; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public Duration getEstimatedProcessingTime() { return estimatedProcessingTime; }
    public AutoReviewResult getAutoReviewResult() { return autoReviewResult; }
    public AppealExecutionResult getExecutionResult() { return executionResult; }
    public String getMessage() { return message; }
}