package com.axion.bot.moderation;

/**
 * Model class representing the result of a review
 */
public class ReviewResult {
    private final boolean success;
    private final Appeal appeal;
    private final AppealReview review;
    private final AppealExecutionResult executionResult;
    private final String errorMessage;
    
    private ReviewResult(boolean success, Appeal appeal, AppealReview review, 
                        AppealExecutionResult executionResult, String errorMessage) {
        this.success = success;
        this.appeal = appeal;
        this.review = review;
        this.executionResult = executionResult;
        this.errorMessage = errorMessage;
    }
    
    public static ReviewResult success(Appeal appeal, AppealReview review, AppealExecutionResult executionResult) {
        return new ReviewResult(true, appeal, review, executionResult, null);
    }
    
    public static ReviewResult error(String errorMessage) {
        return new ReviewResult(false, null, null, null, errorMessage);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public Appeal getAppeal() { return appeal; }
    public AppealReview getReview() { return review; }
    public AppealExecutionResult getExecutionResult() { return executionResult; }
    public String getErrorMessage() { return errorMessage; }
}