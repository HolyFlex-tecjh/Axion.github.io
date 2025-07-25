package com.axion.bot.moderation;

/**
 * Class representing appeal execution result
 */
public class AppealExecutionResult {
    private final boolean success;
    private final String message;
    private final java.util.List<String> actionsPerformed;
    
    private AppealExecutionResult(boolean success, String message, java.util.List<String> actionsPerformed) {
        this.success = success;
        this.message = message;
        this.actionsPerformed = actionsPerformed != null ? actionsPerformed : new java.util.ArrayList<>();
    }
    
    public static AppealExecutionResult success(java.util.List<String> actionsPerformed) {
        return new AppealExecutionResult(true, "Success", actionsPerformed);
    }
    
    public static AppealExecutionResult error(String message) {
        return new AppealExecutionResult(false, message, null);
    }
    
    public static AppealExecutionResult noAction(String message) {
        return new AppealExecutionResult(true, message, new java.util.ArrayList<>());
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public java.util.List<String> getActionsPerformed() { return actionsPerformed; }
}
