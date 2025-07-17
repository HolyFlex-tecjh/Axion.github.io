package com.axion.bot.moderation;

import java.time.Duration;

/**
 * Configuration class for escalation-based moderation rules
 */
public class EscalationConfig {
    private boolean enableEscalation = true;
    private int escalationThreshold = 3;
    private Duration escalationWindow = Duration.ofDays(7);
    private double escalationMultiplier = 1.5;
    
    public EscalationConfig() {
        // Default constructor with standard settings
    }
    
    public EscalationConfig(boolean enableEscalation, int escalationThreshold, 
                           Duration escalationWindow, double escalationMultiplier) {
        this.enableEscalation = enableEscalation;
        this.escalationThreshold = escalationThreshold;
        this.escalationWindow = escalationWindow;
        this.escalationMultiplier = escalationMultiplier;
    }
    
    // Getters
    public boolean isEnableEscalation() { 
        return enableEscalation; 
    }
    
    public int getEscalationThreshold() { 
        return escalationThreshold; 
    }
    
    public Duration getEscalationWindow() { 
        return escalationWindow; 
    }
    
    public double getEscalationMultiplier() { 
        return escalationMultiplier; 
    }
    
    // Setters
    public void setEnableEscalation(boolean enableEscalation) {
        this.enableEscalation = enableEscalation;
    }
    
    public void setEscalationThreshold(int escalationThreshold) {
        if (escalationThreshold < 1) {
            throw new IllegalArgumentException("Escalation threshold must be at least 1");
        }
        this.escalationThreshold = escalationThreshold;
    }
    
    public void setEscalationWindow(Duration escalationWindow) {
        if (escalationWindow == null || escalationWindow.isNegative()) {
            throw new IllegalArgumentException("Escalation window must be positive");
        }
        this.escalationWindow = escalationWindow;
    }
    
    public void setEscalationMultiplier(double escalationMultiplier) {
        if (escalationMultiplier <= 0) {
            throw new IllegalArgumentException("Escalation multiplier must be positive");
        }
        this.escalationMultiplier = escalationMultiplier;
    }
    
    @Override
    public String toString() {
        return "EscalationConfig{" +
                "enableEscalation=" + enableEscalation +
                ", escalationThreshold=" + escalationThreshold +
                ", escalationWindow=" + escalationWindow +
                ", escalationMultiplier=" + escalationMultiplier +
                '}';
    }
}