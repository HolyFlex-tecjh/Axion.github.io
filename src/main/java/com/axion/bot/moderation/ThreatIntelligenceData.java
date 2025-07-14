package com.axion.bot.moderation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data structure for threat intelligence information
 * Used for caching and storing threat-related data
 */
public class ThreatIntelligenceData {
    private final String threatId;
    private final ThreatLevel threatLevel;
    private final Set<ThreatType> threatTypes;
    private final List<String> indicators;
    private final Map<String, Object> metadata;
    private final double confidence;
    private final String source;
    private final Instant firstSeen;
    private final Instant lastSeen;
    private final Instant expiresAt;
    private final boolean isActive;
    
    public ThreatIntelligenceData(String threatId, ThreatLevel threatLevel, 
                                 Set<ThreatType> threatTypes, List<String> indicators,
                                 Map<String, Object> metadata, double confidence,
                                 String source, Instant firstSeen, Instant lastSeen,
                                 Instant expiresAt, boolean isActive) {
        this.threatId = threatId;
        this.threatLevel = threatLevel;
        this.threatTypes = threatTypes;
        this.indicators = indicators;
        this.metadata = metadata;
        this.confidence = confidence;
        this.source = source;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
    }
    
    // Getters
    public String getThreatId() { return threatId; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public Set<ThreatType> getThreatTypes() { return threatTypes; }
    public List<String> getIndicators() { return indicators; }
    public Map<String, Object> getMetadata() { return metadata; }
    public double getConfidence() { return confidence; }
    public String getSource() { return source; }
    public Instant getFirstSeen() { return firstSeen; }
    public Instant getLastSeen() { return lastSeen; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isActive() { return isActive; }
    
    /**
     * Check if this threat intelligence data is still valid
     */
    public boolean isValid() {
        return isActive && (expiresAt == null || Instant.now().isBefore(expiresAt));
    }
    
    /**
     * Check if this threat matches the given content
     */
    public boolean matches(String content) {
        if (!isValid()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        return indicators.stream()
            .anyMatch(indicator -> lowerContent.contains(indicator.toLowerCase()));
    }
    
    /**
     * Get the severity score based on threat level and confidence
     */
    public double getSeverityScore() {
        double levelMultiplier = switch (threatLevel) {
            case CRITICAL -> 1.0;
            case VERY_HIGH -> 0.9;
            case HIGH -> 0.8;
            case MEDIUM -> 0.6;
            case LOW -> 0.4;
            case NONE -> 0.0;
        };
        return confidence * levelMultiplier;
    }
    
    @Override
    public String toString() {
        return String.format("ThreatIntelligenceData{id='%s', level=%s, confidence=%.2f, types=%s}",
            threatId, threatLevel, confidence, threatTypes);
    }
}