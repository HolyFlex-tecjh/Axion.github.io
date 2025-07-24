package com.axion.bot.gdpr;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the current GDPR compliance status of the system.
 * Contains metrics and information about compliance state.
 */
public class GDPRComplianceStatus {
    private final boolean isCompliant;
    private final Map<String, Object> metrics;
    private final Instant timestamp;
    
    /**
     * Creates a new GDPR compliance status.
     * 
     * @param isCompliant whether the system is currently compliant
     * @param metrics compliance metrics and statistics
     * @param timestamp when this status was generated
     */
    public GDPRComplianceStatus(boolean isCompliant, Map<String, Object> metrics, Instant timestamp) {
        this.isCompliant = isCompliant;
        this.metrics = Map.copyOf(metrics); // Create immutable copy
        this.timestamp = timestamp;
    }
    
    /**
     * @return true if the system is GDPR compliant
     */
    public boolean isCompliant() {
        return isCompliant;
    }
    
    /**
     * @return compliance metrics and statistics
     */
    public Map<String, Object> getMetrics() {
        return metrics;
    }
    
    /**
     * @return timestamp when this status was generated
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets a specific metric value.
     * 
     * @param key the metric key
     * @return the metric value, or null if not found
     */
    public Object getMetric(String key) {
        return metrics.get(key);
    }
    
    /**
     * Gets a specific metric value as an integer.
     * 
     * @param key the metric key
     * @return the metric value as integer, or 0 if not found or not a number
     */
    public int getMetricAsInt(String key) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    
    /**
     * Gets a specific metric value as a double.
     * 
     * @param key the metric key
     * @return the metric value as double, or 0.0 if not found or not a number
     */
    public double getMetricAsDouble(String key) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GDPRComplianceStatus that = (GDPRComplianceStatus) o;
        return isCompliant == that.isCompliant &&
               Objects.equals(metrics, that.metrics) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(isCompliant, metrics, timestamp);
    }
    
    @Override
    public String toString() {
        return "GDPRComplianceStatus{" +
               "isCompliant=" + isCompliant +
               ", metrics=" + metrics +
               ", timestamp=" + timestamp +
               '}';
    }
}