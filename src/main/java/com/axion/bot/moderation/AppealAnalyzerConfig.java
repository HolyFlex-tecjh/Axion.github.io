package com.axion.bot.moderation;

/**
 * Configuration class for appeal analyzer settings
 */
public class AppealAnalyzerConfig {
    private boolean sentimentAnalysisEnabled = true;
    private boolean contextAnalysisEnabled = true;
    private double sincerityThreshold = 0.6;
    private double complexityThreshold = 0.5;
    private boolean enablePatternDetection = true;
    
    public AppealAnalyzerConfig() {}
    
    public boolean isSentimentAnalysisEnabled() { return sentimentAnalysisEnabled; }
    public void setSentimentAnalysisEnabled(boolean sentimentAnalysisEnabled) { this.sentimentAnalysisEnabled = sentimentAnalysisEnabled; }
    
    public boolean isContextAnalysisEnabled() { return contextAnalysisEnabled; }
    public void setContextAnalysisEnabled(boolean contextAnalysisEnabled) { this.contextAnalysisEnabled = contextAnalysisEnabled; }
    
    public double getSincerityThreshold() { return sincerityThreshold; }
    public void setSincerityThreshold(double sincerityThreshold) { this.sincerityThreshold = sincerityThreshold; }
    
    public double getComplexityThreshold() { return complexityThreshold; }
    public void setComplexityThreshold(double complexityThreshold) { this.complexityThreshold = complexityThreshold; }
    
    public boolean isEnablePatternDetection() { return enablePatternDetection; }
    public void setEnablePatternDetection(boolean enablePatternDetection) { this.enablePatternDetection = enablePatternDetection; }
}
