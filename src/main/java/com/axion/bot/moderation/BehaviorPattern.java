package com.axion.bot.moderation;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a detected behavior pattern
 */
public class BehaviorPattern {
    private final PatternType patternType;
    private final double riskWeight;
    private final String description;
    private final Instant detectedAt;
    private final double confidence;
    
    public BehaviorPattern(PatternType patternType, double riskWeight, String description) {
        this(patternType, riskWeight, description, Instant.now(), 1.0);
    }
    
    public BehaviorPattern(PatternType patternType, double riskWeight, String description, 
                          Instant detectedAt, double confidence) {
        this.patternType = Objects.requireNonNull(patternType, "Pattern type cannot be null");
        this.riskWeight = Math.max(0.0, Math.min(1.0, riskWeight)); // Clamp between 0 and 1
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.detectedAt = Objects.requireNonNull(detectedAt, "Detection time cannot be null");
        this.confidence = Math.max(0.0, Math.min(1.0, confidence)); // Clamp between 0 and 1
    }
    
    /**
     * Get the type of pattern detected
     */
    public PatternType getPatternType() {
        return patternType;
    }
    
    /**
     * Get the risk weight (0.0 to 1.0)
     */
    public double getRiskWeight() {
        return riskWeight;
    }
    
    /**
     * Get the human-readable description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get when this pattern was detected
     */
    public Instant getDetectedAt() {
        return detectedAt;
    }
    
    /**
     * Get the confidence level of this detection (0.0 to 1.0)
     */
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * Calculate the severity level based on risk weight
     */
    public SeverityLevel getSeverityLevel() {
        if (riskWeight >= 0.8) {
            return SeverityLevel.CRITICAL;
        } else if (riskWeight >= 0.6) {
            return SeverityLevel.HIGH;
        } else if (riskWeight >= 0.4) {
            return SeverityLevel.MEDIUM;
        } else if (riskWeight >= 0.2) {
            return SeverityLevel.LOW;
        } else {
            return SeverityLevel.MINIMAL;
        }
    }
    
    /**
     * Check if this pattern indicates high risk behavior
     */
    public boolean isHighRisk() {
        return riskWeight >= 0.6 && confidence >= 0.7;
    }
    
    /**
     * Check if this pattern requires immediate action
     */
    public boolean requiresImmediateAction() {
        return riskWeight >= 0.8 && confidence >= 0.8;
    }
    
    /**
     * Get a formatted summary of this pattern
     */
    public String getSummary() {
        return String.format("%s (Risk: %.2f, Confidence: %.2f): %s",
            patternType.getDisplayName(),
            riskWeight,
            confidence,
            description
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BehaviorPattern that = (BehaviorPattern) obj;
        return Double.compare(that.riskWeight, riskWeight) == 0 &&
               Double.compare(that.confidence, confidence) == 0 &&
               patternType == that.patternType &&
               Objects.equals(description, that.description) &&
               Objects.equals(detectedAt, that.detectedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternType, riskWeight, description, detectedAt, confidence);
    }
    
    @Override
    public String toString() {
        return "BehaviorPattern{" +
               "type=" + patternType +
               ", risk=" + riskWeight +
               ", confidence=" + confidence +
               ", description='" + description + '\'' +
               ", detectedAt=" + detectedAt +
               '}';
    }
    
    /**
     * Severity levels for behavior patterns
     */
    public enum SeverityLevel {
        MINIMAL("Minimal", 0),
        LOW("Low", 1),
        MEDIUM("Medium", 2),
        HIGH("High", 3),
        CRITICAL("Critical", 4);
        
        private final String displayName;
        private final int numericValue;
        
        SeverityLevel(String displayName, int numericValue) {
            this.displayName = displayName;
            this.numericValue = numericValue;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getNumericValue() {
            return numericValue;
        }
        
        public boolean isHigherThan(SeverityLevel other) {
            return this.numericValue > other.numericValue;
        }
    }
}