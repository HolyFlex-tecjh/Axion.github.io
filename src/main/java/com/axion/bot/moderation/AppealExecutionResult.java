package com.axion.bot.moderation;

import java.util.*;

/**
 * Model class representing the result of appeal execution
 */
public class AppealExecutionResult {
    private final boolean success;
    private final List<String> actionsPerformed;
    private final String errorMessage;
    
    private AppealExecutionResult(boolean success, List<String> actionsPerformed, String errorMessage) {
        this.success = success;
        this.actionsPerformed = actionsPerformed != null ? new ArrayList<>(actionsPerformed) : new ArrayList<>();
        this.errorMessage = errorMessage;
    }
    
    public static AppealExecutionResult success(List<String> actions) {
        return new AppealExecutionResult(true, actions, null);
    }
    
    public static AppealExecutionResult noAction(String reason) {
        return new AppealExecutionResult(true, Arrays.asList("No action required: " + reason), null);
    }
    
    public static AppealExecutionResult error(String errorMessage) {
        return new AppealExecutionResult(false, null, errorMessage);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public List<String> getActionsPerformed() { return new ArrayList<>(actionsPerformed); }
    public String getErrorMessage() { return errorMessage; }
}