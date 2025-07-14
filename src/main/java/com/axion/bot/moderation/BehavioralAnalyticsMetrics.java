package com.axion.bot.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Metrics and statistics for behavioral analytics engine
 */
public class BehavioralAnalyticsMetrics {
    // Analysis metrics
    private final AtomicLong totalAnalysesPerformed = new AtomicLong(0);
    private final AtomicLong totalUsersAnalyzed = new AtomicLong(0);
    private final AtomicLong totalPatternsDetected = new AtomicLong(0);
    private final AtomicLong totalHighRiskDetections = new AtomicLong(0);
    private final AtomicLong totalCoordinatedBehaviorDetected = new AtomicLong(0);
    
    // Performance metrics
    private final DoubleAdder totalAnalysisTimeMs = new DoubleAdder();
    private final AtomicLong maxAnalysisTimeMs = new AtomicLong(0);
    private final AtomicLong minAnalysisTimeMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicInteger concurrentAnalyses = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentAnalyses = new AtomicInteger(0);
    
    // Pattern detection metrics
    private final Map<String, AtomicLong> patternTypeCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> riskLevelCounters = new ConcurrentHashMap<>();
    private final Map<String, DoubleAdder> confidenceScores = new ConcurrentHashMap<>();
    
    // Error and reliability metrics
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalTimeouts = new AtomicLong(0);
    private final AtomicLong totalFalsePositives = new AtomicLong(0);
    private final AtomicLong totalFalseNegatives = new AtomicLong(0);
    private final Map<String, AtomicLong> errorTypeCounters = new ConcurrentHashMap<>();
    
    // Cache and efficiency metrics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);
    private final AtomicLong memoryUsageBytes = new AtomicLong(0);
    
    // Time-based metrics
    private final Instant startTime = Instant.now();
    private volatile Instant lastAnalysisTime = Instant.now();
    private volatile Instant lastResetTime = Instant.now();
    
    // Rate limiting and throttling
    private final AtomicLong throttledRequests = new AtomicLong(0);
    private final AtomicLong rateLimitedRequests = new AtomicLong(0);
    
    // Guild-specific metrics
    private final Map<String, GuildMetrics> guildMetrics = new ConcurrentHashMap<>();
    
    /**
     * Record a completed analysis
     */
    public void recordAnalysis(String guildId, long analysisTimeMs, int patternsDetected, 
                              String riskLevel, boolean isHighRisk) {
        totalAnalysesPerformed.incrementAndGet();
        totalUsersAnalyzed.incrementAndGet();
        totalPatternsDetected.addAndGet(patternsDetected);
        
        if (isHighRisk) {
            totalHighRiskDetections.incrementAndGet();
        }
        
        // Update timing metrics
        totalAnalysisTimeMs.add(analysisTimeMs);
        updateMinMax(analysisTimeMs);
        lastAnalysisTime = Instant.now();
        
        // Update risk level counters
        riskLevelCounters.computeIfAbsent(riskLevel, k -> new AtomicLong(0)).incrementAndGet();
        
        // Update guild-specific metrics
        getGuildMetrics(guildId).recordAnalysis(analysisTimeMs, patternsDetected, isHighRisk);
    }
    
    /**
     * Record pattern detection
     */
    public void recordPatternDetection(String guildId, String patternType, double confidence) {
        patternTypeCounters.computeIfAbsent(patternType, k -> new AtomicLong(0)).incrementAndGet();
        confidenceScores.computeIfAbsent(patternType, k -> new DoubleAdder()).add(confidence);
        
        getGuildMetrics(guildId).recordPattern(patternType, confidence);
    }
    
    /**
     * Record coordinated behavior detection
     */
    public void recordCoordinatedBehavior(String guildId, int usersInvolved, double coordinationScore) {
        totalCoordinatedBehaviorDetected.incrementAndGet();
        getGuildMetrics(guildId).recordCoordinatedBehavior(usersInvolved, coordinationScore);
    }
    
    /**
     * Record analysis start
     */
    public void recordAnalysisStart() {
        int current = concurrentAnalyses.incrementAndGet();
        maxConcurrentAnalyses.updateAndGet(max -> Math.max(max, current));
    }
    
    /**
     * Record analysis completion
     */
    public void recordAnalysisComplete() {
        concurrentAnalyses.decrementAndGet();
    }
    
    /**
     * Record an error
     */
    public void recordError(String guildId, String errorType, String errorMessage) {
        totalErrors.incrementAndGet();
        errorTypeCounters.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
        
        getGuildMetrics(guildId).recordError(errorType);
    }
    
    /**
     * Record a timeout
     */
    public void recordTimeout(String guildId) {
        totalTimeouts.incrementAndGet();
        getGuildMetrics(guildId).recordTimeout();
    }
    
    /**
     * Record false positive
     */
    public void recordFalsePositive(String guildId, String patternType) {
        totalFalsePositives.incrementAndGet();
        getGuildMetrics(guildId).recordFalsePositive(patternType);
    }
    
    /**
     * Record false negative
     */
    public void recordFalseNegative(String guildId, String patternType) {
        totalFalseNegatives.incrementAndGet();
        getGuildMetrics(guildId).recordFalseNegative(patternType);
    }
    
    /**
     * Record cache hit
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }
    
    /**
     * Record cache miss
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }
    
    /**
     * Record cache eviction
     */
    public void recordCacheEviction() {
        cacheEvictions.incrementAndGet();
    }
    
    /**
     * Record throttled request
     */
    public void recordThrottledRequest(String guildId) {
        throttledRequests.incrementAndGet();
        getGuildMetrics(guildId).recordThrottled();
    }
    
    /**
     * Record rate limited request
     */
    public void recordRateLimitedRequest(String guildId) {
        rateLimitedRequests.incrementAndGet();
        getGuildMetrics(guildId).recordRateLimited();
    }
    
    /**
     * Update memory usage
     */
    public void updateMemoryUsage(long bytes) {
        memoryUsageBytes.set(bytes);
    }
    
    /**
     * Get average analysis time
     */
    public double getAverageAnalysisTimeMs() {
        long total = totalAnalysesPerformed.get();
        return total > 0 ? totalAnalysisTimeMs.sum() / total : 0.0;
    }
    
    /**
     * Get analyses per second
     */
    public double getAnalysesPerSecond() {
        Duration uptime = Duration.between(startTime, Instant.now());
        long totalSeconds = uptime.getSeconds();
        return totalSeconds > 0 ? (double) totalAnalysesPerformed.get() / totalSeconds : 0.0;
    }
    
    /**
     * Get cache hit rate
     */
    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * Get error rate
     */
    public double getErrorRate() {
        long errors = totalErrors.get();
        long total = totalAnalysesPerformed.get();
        return total > 0 ? (double) errors / total : 0.0;
    }
    
    /**
     * Get high risk detection rate
     */
    public double getHighRiskDetectionRate() {
        long highRisk = totalHighRiskDetections.get();
        long total = totalAnalysesPerformed.get();
        return total > 0 ? (double) highRisk / total : 0.0;
    }
    
    /**
     * Get pattern detection accuracy
     */
    public double getPatternDetectionAccuracy() {
        long truePositives = totalPatternsDetected.get() - totalFalsePositives.get();
        long total = totalPatternsDetected.get() + totalFalseNegatives.get();
        return total > 0 ? (double) truePositives / total : 0.0;
    }
    
    /**
     * Get average confidence for pattern type
     */
    public double getAverageConfidence(String patternType) {
        DoubleAdder totalConfidence = confidenceScores.get(patternType);
        AtomicLong count = patternTypeCounters.get(patternType);
        
        if (totalConfidence == null || count == null || count.get() == 0) {
            return 0.0;
        }
        
        return totalConfidence.sum() / count.get();
    }
    
    /**
     * Get most detected pattern type
     */
    public String getMostDetectedPatternType() {
        return patternTypeCounters.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
            .map(Map.Entry::getKey)
            .orElse("None");
    }
    
    /**
     * Get most common risk level
     */
    public String getMostCommonRiskLevel() {
        return riskLevelCounters.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
            .map(Map.Entry::getKey)
            .orElse("Unknown");
    }
    
    /**
     * Get uptime
     */
    public Duration getUptime() {
        return Duration.between(startTime, Instant.now());
    }
    
    /**
     * Get time since last analysis
     */
    public Duration getTimeSinceLastAnalysis() {
        return Duration.between(lastAnalysisTime, Instant.now());
    }
    
    /**
     * Get guild metrics
     */
    public GuildMetrics getGuildMetrics(String guildId) {
        return guildMetrics.computeIfAbsent(guildId, k -> new GuildMetrics());
    }
    
    /**
     * Get all guild metrics
     */
    public Map<String, GuildMetrics> getAllGuildMetrics() {
        return new HashMap<>(guildMetrics);
    }
    
    /**
     * Reset all metrics
     */
    public void reset() {
        totalAnalysesPerformed.set(0);
        totalUsersAnalyzed.set(0);
        totalPatternsDetected.set(0);
        totalHighRiskDetections.set(0);
        totalCoordinatedBehaviorDetected.set(0);
        
        totalAnalysisTimeMs.reset();
        maxAnalysisTimeMs.set(0);
        minAnalysisTimeMs.set(Long.MAX_VALUE);
        concurrentAnalyses.set(0);
        maxConcurrentAnalyses.set(0);
        
        patternTypeCounters.clear();
        riskLevelCounters.clear();
        confidenceScores.clear();
        
        totalErrors.set(0);
        totalTimeouts.set(0);
        totalFalsePositives.set(0);
        totalFalseNegatives.set(0);
        errorTypeCounters.clear();
        
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
        
        throttledRequests.set(0);
        rateLimitedRequests.set(0);
        
        guildMetrics.clear();
        lastResetTime = Instant.now();
    }
    
    /**
     * Get comprehensive metrics summary
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Basic metrics
        summary.put("totalAnalyses", totalAnalysesPerformed.get());
        summary.put("totalUsersAnalyzed", totalUsersAnalyzed.get());
        summary.put("totalPatternsDetected", totalPatternsDetected.get());
        summary.put("totalHighRiskDetections", totalHighRiskDetections.get());
        summary.put("totalCoordinatedBehaviorDetected", totalCoordinatedBehaviorDetected.get());
        
        // Performance metrics
        summary.put("averageAnalysisTimeMs", getAverageAnalysisTimeMs());
        summary.put("maxAnalysisTimeMs", maxAnalysisTimeMs.get());
        summary.put("minAnalysisTimeMs", minAnalysisTimeMs.get() == Long.MAX_VALUE ? 0 : minAnalysisTimeMs.get());
        summary.put("currentConcurrentAnalyses", concurrentAnalyses.get());
        summary.put("maxConcurrentAnalyses", maxConcurrentAnalyses.get());
        summary.put("analysesPerSecond", getAnalysesPerSecond());
        
        // Quality metrics
        summary.put("errorRate", getErrorRate());
        summary.put("highRiskDetectionRate", getHighRiskDetectionRate());
        summary.put("patternDetectionAccuracy", getPatternDetectionAccuracy());
        summary.put("cacheHitRate", getCacheHitRate());
        
        // Pattern metrics
        summary.put("mostDetectedPatternType", getMostDetectedPatternType());
        summary.put("mostCommonRiskLevel", getMostCommonRiskLevel());
        summary.put("patternTypeCounts", getPatternTypeCounts());
        summary.put("riskLevelCounts", getRiskLevelCounts());
        
        // System metrics
        summary.put("uptime", getUptime().toString());
        summary.put("timeSinceLastAnalysis", getTimeSinceLastAnalysis().toString());
        summary.put("memoryUsageBytes", memoryUsageBytes.get());
        summary.put("totalErrors", totalErrors.get());
        summary.put("totalTimeouts", totalTimeouts.get());
        summary.put("throttledRequests", throttledRequests.get());
        summary.put("rateLimitedRequests", rateLimitedRequests.get());
        
        // Guild metrics
        summary.put("totalGuilds", guildMetrics.size());
        summary.put("activeGuilds", getActiveGuildCount());
        
        return summary;
    }
    
    private void updateMinMax(long analysisTimeMs) {
        maxAnalysisTimeMs.updateAndGet(max -> Math.max(max, analysisTimeMs));
        minAnalysisTimeMs.updateAndGet(min -> Math.min(min, analysisTimeMs));
    }
    
    private Map<String, Long> getPatternTypeCounts() {
        Map<String, Long> counts = new HashMap<>();
        patternTypeCounters.forEach((type, counter) -> counts.put(type, counter.get()));
        return counts;
    }
    
    private Map<String, Long> getRiskLevelCounts() {
        Map<String, Long> counts = new HashMap<>();
        riskLevelCounters.forEach((level, counter) -> counts.put(level, counter.get()));
        return counts;
    }
    
    private long getActiveGuildCount() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(1));
        return guildMetrics.values().stream()
            .mapToLong(metrics -> metrics.getLastActivity().isAfter(cutoff) ? 1 : 0)
            .sum();
    }
    
    // Getters for individual metrics
    public long getTotalAnalysesPerformed() { return totalAnalysesPerformed.get(); }
    public long getTotalUsersAnalyzed() { return totalUsersAnalyzed.get(); }
    public long getTotalPatternsDetected() { return totalPatternsDetected.get(); }
    public long getTotalHighRiskDetections() { return totalHighRiskDetections.get(); }
    public long getTotalCoordinatedBehaviorDetected() { return totalCoordinatedBehaviorDetected.get(); }
    public long getMaxAnalysisTimeMs() { return maxAnalysisTimeMs.get(); }
    public long getMinAnalysisTimeMs() { return minAnalysisTimeMs.get() == Long.MAX_VALUE ? 0 : minAnalysisTimeMs.get(); }
    public int getCurrentConcurrentAnalyses() { return concurrentAnalyses.get(); }
    public int getMaxConcurrentAnalyses() { return maxConcurrentAnalyses.get(); }
    public long getTotalErrors() { return totalErrors.get(); }
    public long getTotalTimeouts() { return totalTimeouts.get(); }
    public long getTotalFalsePositives() { return totalFalsePositives.get(); }
    public long getTotalFalseNegatives() { return totalFalseNegatives.get(); }
    public long getCacheHits() { return cacheHits.get(); }
    public long getCacheMisses() { return cacheMisses.get(); }
    public long getCacheEvictions() { return cacheEvictions.get(); }
    public long getMemoryUsageBytes() { return memoryUsageBytes.get(); }
    public long getThrottledRequests() { return throttledRequests.get(); }
    public long getRateLimitedRequests() { return rateLimitedRequests.get(); }
    public Instant getStartTime() { return startTime; }
    public Instant getLastAnalysisTime() { return lastAnalysisTime; }
    public Instant getLastResetTime() { return lastResetTime; }
    
    /**
     * Guild-specific metrics
     */
    public static class GuildMetrics {
        private final AtomicLong analysisCount = new AtomicLong(0);
        private final AtomicLong patternCount = new AtomicLong(0);
        private final AtomicLong highRiskCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong timeoutCount = new AtomicLong(0);
        private final AtomicLong throttledCount = new AtomicLong(0);
        private final AtomicLong rateLimitedCount = new AtomicLong(0);
        private final DoubleAdder totalAnalysisTime = new DoubleAdder();
        private final DoubleAdder totalConfidence = new DoubleAdder();
        private final Map<String, AtomicLong> patternTypeCounts = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> falsePositiveCounts = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> falseNegativeCounts = new ConcurrentHashMap<>();
        private volatile Instant lastActivity = Instant.now();
        
        public void recordAnalysis(long analysisTimeMs, int patterns, boolean isHighRisk) {
            analysisCount.incrementAndGet();
            patternCount.addAndGet(patterns);
            totalAnalysisTime.add(analysisTimeMs);
            
            if (isHighRisk) {
                highRiskCount.incrementAndGet();
            }
            
            lastActivity = Instant.now();
        }
        
        public void recordPattern(String patternType, double confidence) {
            patternTypeCounts.computeIfAbsent(patternType, k -> new AtomicLong(0)).incrementAndGet();
            totalConfidence.add(confidence);
            lastActivity = Instant.now();
        }
        
        public void recordCoordinatedBehavior(int usersInvolved, double coordinationScore) {
            // Could add specific coordinated behavior metrics here
            lastActivity = Instant.now();
        }
        
        public void recordError(String errorType) {
            errorCount.incrementAndGet();
            lastActivity = Instant.now();
        }
        
        public void recordTimeout() {
            timeoutCount.incrementAndGet();
            lastActivity = Instant.now();
        }
        
        public void recordFalsePositive(String patternType) {
            falsePositiveCounts.computeIfAbsent(patternType, k -> new AtomicLong(0)).incrementAndGet();
            lastActivity = Instant.now();
        }
        
        public void recordFalseNegative(String patternType) {
            falseNegativeCounts.computeIfAbsent(patternType, k -> new AtomicLong(0)).incrementAndGet();
            lastActivity = Instant.now();
        }
        
        public void recordThrottled() {
            throttledCount.incrementAndGet();
            lastActivity = Instant.now();
        }
        
        public void recordRateLimited() {
            rateLimitedCount.incrementAndGet();
            lastActivity = Instant.now();
        }
        
        public double getAverageAnalysisTime() {
            long count = analysisCount.get();
            return count > 0 ? totalAnalysisTime.sum() / count : 0.0;
        }
        
        public double getAverageConfidence() {
            long count = patternCount.get();
            return count > 0 ? totalConfidence.sum() / count : 0.0;
        }
        
        public double getHighRiskRate() {
            long total = analysisCount.get();
            return total > 0 ? (double) highRiskCount.get() / total : 0.0;
        }
        
        public double getErrorRate() {
            long total = analysisCount.get();
            return total > 0 ? (double) errorCount.get() / total : 0.0;
        }
        
        // Getters
        public long getAnalysisCount() { return analysisCount.get(); }
        public long getPatternCount() { return patternCount.get(); }
        public long getHighRiskCount() { return highRiskCount.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public long getTimeoutCount() { return timeoutCount.get(); }
        public long getThrottledCount() { return throttledCount.get(); }
        public long getRateLimitedCount() { return rateLimitedCount.get(); }
        public Instant getLastActivity() { return lastActivity; }
        public Map<String, Long> getPatternTypeCounts() {
            Map<String, Long> counts = new HashMap<>();
            patternTypeCounts.forEach((type, counter) -> counts.put(type, counter.get()));
            return counts;
        }
    }
}