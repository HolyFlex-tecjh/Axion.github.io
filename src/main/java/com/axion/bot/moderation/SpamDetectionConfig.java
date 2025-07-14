package com.axion.bot.moderation;

import java.time.Duration;

/**
 * Configuration for spam detection
 */
public class SpamDetectionConfig {
    private int messageFrequencyThreshold = 5;
    private Duration timeWindow = Duration.ofMinutes(1);
    private double similarityThreshold = 0.8;
    private int consecutiveSpamThreshold = 3;
    private int duplicateMessageThreshold = 3;
    private double confidenceThreshold = 0.5;
    private boolean contentAnalysis = true;
    private boolean frequencyAnalysis = true;
    
    public int getMessageFrequencyThreshold() { return messageFrequencyThreshold; }
    public Duration getTimeWindow() { return timeWindow; }
    public double getSimilarityThreshold() { return similarityThreshold; }
    public int getConsecutiveSpamThreshold() { return consecutiveSpamThreshold; }
    public int getDuplicateMessageThreshold() { return duplicateMessageThreshold; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public boolean isContentAnalysis() { return contentAnalysis; }
    public boolean isFrequencyAnalysis() { return frequencyAnalysis; }
    
    // Setters for configuration
    public void setMessageFrequencyThreshold(int messageFrequencyThreshold) {
        this.messageFrequencyThreshold = messageFrequencyThreshold;
    }
    
    public void setTimeWindow(Duration timeWindow) {
        this.timeWindow = timeWindow;
    }
    
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
    public void setConsecutiveSpamThreshold(int consecutiveSpamThreshold) {
        this.consecutiveSpamThreshold = consecutiveSpamThreshold;
    }
    
    public void setDuplicateMessageThreshold(int duplicateMessageThreshold) {
        this.duplicateMessageThreshold = duplicateMessageThreshold;
    }
    
    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }
    
    public void setContentAnalysis(boolean contentAnalysis) {
        this.contentAnalysis = contentAnalysis;
    }
    
    public void setFrequencyAnalysis(boolean frequencyAnalysis) {
        this.frequencyAnalysis = frequencyAnalysis;
    }
}