package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Advanced threat intelligence system that provides real-time threat detection,
 * analysis, and mitigation for the moderation system.
 */
public class ThreatIntelligenceSystem {
    private static final Logger logger = LoggerFactory.getLogger(ThreatIntelligenceSystem.class);
    
    // Threat databases
    private final Map<String, ThreatIntelligenceData> threatDatabase = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> threatCategories = new ConcurrentHashMap<>();
    private final Map<String, Pattern> threatPatterns = new ConcurrentHashMap<>();
    
    // Real-time tracking
    private final Map<String, List<ThreatEvent>> recentThreats = new ConcurrentHashMap<>();
    private final Map<String, Double> userThreatScores = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastThreatUpdate = new ConcurrentHashMap<>();
    
    // Background processing
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Configuration
    private final ThreatIntelligenceConfig config;
    
    public ThreatIntelligenceSystem(ThreatIntelligenceConfig config) {
        this.config = config;
        initializeThreatDatabase();
        startBackgroundTasks();
    }
    
    /**
     * Analyzes content for potential threats
     */
    public ThreatAssessment analyzeContent(String content, UserContext userContext, GuildContext guildContext) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<String> detectedThreats = new ArrayList<>();
            List<String> indicators = new ArrayList<>();
            Map<String, Object> evidence = new HashMap<>();
            double maxConfidence = 0.0;
            com.axion.bot.moderation.ThreatLevel maxThreatLevel = com.axion.bot.moderation.ThreatLevel.NONE;
            
            // Pattern-based threat detection
            for (Map.Entry<String, Pattern> entry : threatPatterns.entrySet()) {
                if (entry.getValue().matcher(content.toLowerCase()).find()) {
                    String threatType = entry.getKey();
                    detectedThreats.add(threatType);
                    indicators.add("Pattern match: " + threatType);
                    
                    ThreatIntelligenceData threatData = threatDatabase.get(threatType);
                    if (threatData != null && threatData.isActive()) {
                        double confidence = threatData.getConfidence();
                        if (confidence > maxConfidence) {
                            maxConfidence = confidence;
                            maxThreatLevel = threatData.getThreatLevel();
                        }
                        evidence.put(threatType, threatData.getMetadata());
                    }
                }
            }
            
            // User-based threat analysis
            String userId = userContext.getUserId();
            double userThreatScore = getUserThreatScore(userId);
            if (userThreatScore > config.getUserThreatThreshold()) {
                detectedThreats.add("HIGH_RISK_USER");
                indicators.add("User threat score: " + userThreatScore);
                maxConfidence = Math.max(maxConfidence, userThreatScore);
                evidence.put("userThreatScore", userThreatScore);
            }
            
            // Context-based analysis
            if (hasRecentThreatActivity(guildContext.getGuildId())) {
                detectedThreats.add("RECENT_THREAT_ACTIVITY");
                indicators.add("Recent threat activity in guild");
                maxConfidence = Math.max(maxConfidence, 0.6);
            }
            
            // Content analysis
            ThreatAnalysisResult contentAnalysis = analyzeContentCharacteristics(content);
            if (contentAnalysis.isSuspicious()) {
                detectedThreats.addAll(contentAnalysis.getThreats());
                indicators.addAll(contentAnalysis.getIndicators());
                maxConfidence = Math.max(maxConfidence, contentAnalysis.getConfidence());
                evidence.putAll(contentAnalysis.getEvidence());
            }
            
            long analysisTime = System.currentTimeMillis() - startTime;
            
            if (detectedThreats.isEmpty()) {
                return ThreatAssessment.noThreat(userId, guildContext.getGuildId());
            } else {
                // Record threat event
                recordThreatEvent(userId, guildContext.getGuildId(), detectedThreats, maxConfidence);
                
                return ThreatAssessment.detectedThreat(
                    userId, guildContext.getGuildId(), maxThreatLevel, detectedThreats, maxConfidence, indicators,
                    new ArrayList<>(evidence.keySet()), generateThreatDescription(detectedThreats),
                    generateRecommendations(detectedThreats, maxThreatLevel), Instant.now()
                );
            }
            
        } catch (Exception e) {
            logger.error("Error during threat analysis", e);
            String userId = userContext.getUserId();
            return ThreatAssessment.detectedThreat(
                userId, guildContext.getGuildId(), com.axion.bot.moderation.ThreatLevel.HIGH, Collections.singletonList("ANALYSIS_ERROR"),
                1.0, Collections.singletonList("System error during analysis"),
                Collections.singletonList("Analysis system error"), "Analysis system error",
                Collections.singletonList("Review manually"), Instant.now()
            );
        }
    }
    
    /**
     * Updates threat intelligence data
     */
    public void updateThreatData(ThreatIntelligenceData threatData) {
        threatDatabase.put(threatData.getThreatId(), threatData);
        
        // Update patterns if needed
        if (threatData.getIndicators() != null) {
            for (String indicator : threatData.getIndicators()) {
                if (indicator.startsWith("regex:")) {
                    try {
                        String pattern = indicator.substring(6);
                        threatPatterns.put(threatData.getThreatId(), Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
                    } catch (Exception e) {
                        logger.warn("Invalid regex pattern for threat {}: {}", threatData.getThreatId(), indicator);
                    }
                }
            }
        }
        
        // Update categories
        if (threatData.getThreatTypes() != null) {
            for (ThreatType type : threatData.getThreatTypes()) {
                threatCategories.computeIfAbsent(type.name(), k -> ConcurrentHashMap.newKeySet())
                    .add(threatData.getThreatId());
            }
        }
        
        lastThreatUpdate.put(threatData.getThreatId(), Instant.now());
        logger.debug("Updated threat data: {}", threatData.getThreatId());
    }
    
    /**
     * Gets user threat score
     */
    public double getUserThreatScore(String userId) {
        return userThreatScores.getOrDefault(userId, 0.0);
    }
    
    /**
     * Updates user threat score
     */
    public void updateUserThreatScore(String userId, double score) {
        userThreatScores.put(userId, Math.max(0.0, Math.min(1.0, score)));
    }
    
    /**
     * Gets threats by category
     */
    public List<ThreatIntelligenceData> getThreatsByCategory(String category) {
        Set<String> threatIds = threatCategories.getOrDefault(category, Collections.emptySet());
        return threatIds.stream()
            .map(threatDatabase::get)
            .filter(Objects::nonNull)
            .filter(ThreatIntelligenceData::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets system statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalThreats", threatDatabase.size());
        stats.put("activeThreats", threatDatabase.values().stream()
            .mapToLong(t -> t.isActive() ? 1 : 0).sum());
        stats.put("threatCategories", threatCategories.size());
        stats.put("trackedUsers", userThreatScores.size());
        stats.put("recentThreatEvents", recentThreats.values().stream()
            .mapToInt(List::size).sum());
        return stats;
    }
    
    /**
     * Records a threat event
     */
    private void recordThreatEvent(String userId, String guildId, List<String> threats, double confidence) {
        ThreatEvent event = new ThreatEvent(userId, guildId, threats, confidence, Instant.now());
        
        recentThreats.computeIfAbsent(guildId, k -> new ArrayList<>()).add(event);
        
        // Update user threat score
        double currentScore = getUserThreatScore(userId);
        double newScore = Math.min(1.0, currentScore + (confidence * 0.1));
        updateUserThreatScore(userId, newScore);
    }
    
    /**
     * Checks for recent threat activity
     */
    private boolean hasRecentThreatActivity(String guildId) {
        List<ThreatEvent> events = recentThreats.get(guildId);
        if (events == null || events.isEmpty()) {
            return false;
        }
        
        Instant cutoff = Instant.now().minusSeconds(config.getRecentActivityWindow());
        return events.stream().anyMatch(event -> event.getTimestamp().isAfter(cutoff));
    }
    
    /**
     * Analyzes content characteristics for threats
     */
    private ThreatAnalysisResult analyzeContentCharacteristics(String content) {
        List<String> threats = new ArrayList<>();
        List<String> indicators = new ArrayList<>();
        Map<String, Object> evidence = new HashMap<>();
        double confidence = 0.0;
        
        // Check for suspicious patterns
        if (content.length() > config.getMaxContentLength()) {
            threats.add("EXCESSIVE_LENGTH");
            indicators.add("Content length exceeds threshold");
            confidence = Math.max(confidence, 0.3);
        }
        
        // Check for encoded content
        if (containsEncodedContent(content)) {
            threats.add("ENCODED_CONTENT");
            indicators.add("Contains encoded or obfuscated content");
            confidence = Math.max(confidence, 0.7);
        }
        
        // Check for suspicious URLs
        if (containsSuspiciousUrls(content)) {
            threats.add("SUSPICIOUS_URLS");
            indicators.add("Contains suspicious URLs");
            confidence = Math.max(confidence, 0.8);
        }
        
        return new ThreatAnalysisResult(!threats.isEmpty(), threats, indicators, evidence, confidence);
    }
    
    private boolean containsEncodedContent(String content) {
        // Simple heuristics for encoded content
        return content.matches(".*[A-Za-z0-9+/]{20,}={0,2}.*") || // Base64
               content.matches(".*%[0-9A-Fa-f]{2}.*"); // URL encoded
    }
    
    private boolean containsSuspiciousUrls(String content) {
        // Check for URL shorteners and suspicious domains
        String[] suspiciousDomains = {"bit.ly", "tinyurl.com", "t.co", "goo.gl"};
        String lowerContent = content.toLowerCase();
        return Arrays.stream(suspiciousDomains).anyMatch(lowerContent::contains);
    }
    
    private String generateThreatDescription(List<String> threats) {
        if (threats.isEmpty()) {
            return "No threats detected";
        }
        return "Detected threats: " + String.join(", ", threats);
    }
    
    private List<String> generateRecommendations(List<String> threats, com.axion.bot.moderation.ThreatLevel level) {
        List<String> recommendations = new ArrayList<>();
        
        switch (level) {
            case CRITICAL:
                recommendations.add("Immediate action required");
                recommendations.add("Consider temporary user suspension");
                break;
            case HIGH:
                recommendations.add("Review content manually");
                recommendations.add("Monitor user activity closely");
                break;
            case MEDIUM:
                recommendations.add("Flag for review");
                recommendations.add("Increase monitoring");
                break;
            case LOW:
                recommendations.add("Log for analysis");
                break;
        }
        
        return recommendations;
    }
    
    /**
     * Initializes the threat database with default threats
     */
    private void initializeThreatDatabase() {
        // Add default threat patterns
        addDefaultThreat("MALWARE_LINK", com.axion.bot.moderation.ThreatLevel.CRITICAL, 
            Arrays.asList("malware", "virus", "trojan"),
            Arrays.asList("regex:.*\\.(exe|scr|bat|cmd|com|pif)$"));
            
        addDefaultThreat("PHISHING", com.axion.bot.moderation.ThreatLevel.HIGH,
            Arrays.asList("phishing", "social_engineering"),
            Arrays.asList("regex:.*login.*verify.*account.*", "regex:.*urgent.*action.*required.*"));
            
        addDefaultThreat("SPAM_CONTENT", com.axion.bot.moderation.ThreatLevel.MEDIUM,
            Arrays.asList("spam", "advertisement"),
            Arrays.asList("regex:.*free.*money.*", "regex:.*click.*here.*now.*"));
    }
    
    private void addDefaultThreat(String id, com.axion.bot.moderation.ThreatLevel level, List<String> types, List<String> indicators) {
        Set<ThreatType> threatTypeSet = types.stream()
            .map(type -> {
                try {
                    return ThreatType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ThreatType.SUSPICIOUS_ACTIVITY;
                }
            })
            .collect(Collectors.toSet());
        
        ThreatIntelligenceData threat = new ThreatIntelligenceData(
            id, level, threatTypeSet, indicators, new HashMap<>(),
            0.8, "system", Instant.now(), Instant.now(),
            Instant.now().plus(Duration.ofDays(365)), true
        );
        updateThreatData(threat);
    }
    
    /**
     * Starts background maintenance tasks
     */
    private void startBackgroundTasks() {
        // Clean up old threat events
        scheduler.scheduleAtFixedRate(this::cleanupOldEvents, 1, 1, TimeUnit.HOURS);
        
        // Update threat scores
        scheduler.scheduleAtFixedRate(this::decayThreatScores, 6, 6, TimeUnit.HOURS);
    }
    
    private void cleanupOldEvents() {
        Instant cutoff = Instant.now().minusSeconds(config.getEventRetentionPeriod());
        recentThreats.values().forEach(events -> 
            events.removeIf(event -> event.getTimestamp().isBefore(cutoff)));
    }
    
    private void decayThreatScores() {
        userThreatScores.replaceAll((userId, score) -> Math.max(0.0, score * 0.95));
    }
    
    /**
     * Shuts down the system
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // Inner classes
    public static class ThreatEvent {
        private final String userId;
        private final String guildId;
        private final List<String> threats;
        private final double confidence;
        private final Instant timestamp;
        
        public ThreatEvent(String userId, String guildId, List<String> threats, double confidence, Instant timestamp) {
            this.userId = userId;
            this.guildId = guildId;
            this.threats = new ArrayList<>(threats);
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
        
        public String getUserId() { return userId; }
        public String getGuildId() { return guildId; }
        public List<String> getThreats() { return new ArrayList<>(threats); }
        public double getConfidence() { return confidence; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    public static class ThreatAnalysisResult {
        private final boolean suspicious;
        private final List<String> threats;
        private final List<String> indicators;
        private final Map<String, Object> evidence;
        private final double confidence;
        
        public ThreatAnalysisResult(boolean suspicious, List<String> threats, List<String> indicators,
                                   Map<String, Object> evidence, double confidence) {
            this.suspicious = suspicious;
            this.threats = new ArrayList<>(threats);
            this.indicators = new ArrayList<>(indicators);
            this.evidence = new HashMap<>(evidence);
            this.confidence = confidence;
        }
        
        public boolean isSuspicious() { return suspicious; }
        public List<String> getThreats() { return new ArrayList<>(threats); }
        public List<String> getIndicators() { return new ArrayList<>(indicators); }
        public Map<String, Object> getEvidence() { return new HashMap<>(evidence); }
        public double getConfidence() { return confidence; }
        
        public List<ThreatType> getThreatTypes() {
            List<ThreatType> threatTypes = new ArrayList<>();
            for (String threat : threats) {
                try {
                    // Try to map string threats to ThreatType enum
                    switch (threat.toUpperCase()) {
                        case "MALWARE_LINK":
                        case "MALWARE":
                            threatTypes.add(ThreatType.MALWARE);
                            break;
                        case "PHISHING":
                            threatTypes.add(ThreatType.PHISHING);
                            break;
                        case "SPAM_CONTENT":
                        case "SPAM":
                            threatTypes.add(ThreatType.SPAM);
                            break;
                        case "SCAM":
                            threatTypes.add(ThreatType.SCAM);
                            break;
                        case "DOXXING":
                            threatTypes.add(ThreatType.DOXXING);
                            break;
                        case "HARASSMENT":
                            threatTypes.add(ThreatType.HARASSMENT);
                            break;
                        case "TOXICITY":
                            threatTypes.add(ThreatType.TOXICITY);
                            break;
                        case "SUSPICIOUS_URLS":
                        case "MALICIOUS_LINK":
                            threatTypes.add(ThreatType.MALICIOUS_LINK);
                            break;
                        case "EXCESSIVE_LENGTH":
                        case "ENCODED_CONTENT":
                            threatTypes.add(ThreatType.SUSPICIOUS_ACTIVITY);
                            break;
                        default:
                            threatTypes.add(ThreatType.SUSPICIOUS_ACTIVITY);
                            break;
                    }
                } catch (Exception e) {
                    threatTypes.add(ThreatType.SUSPICIOUS_ACTIVITY);
                }
            }
            return threatTypes;
        }
    }
    
    public static class ThreatIntelligenceConfig {
        private double userThreatThreshold = 0.7;
        private long recentActivityWindow = 3600; // 1 hour
        private long eventRetentionPeriod = 86400; // 24 hours
        private int maxContentLength = 10000;
        
        public double getUserThreatThreshold() { return userThreatThreshold; }
        public void setUserThreatThreshold(double threshold) { this.userThreatThreshold = threshold; }
        
        public long getRecentActivityWindow() { return recentActivityWindow; }
        public void setRecentActivityWindow(long window) { this.recentActivityWindow = window; }
        
        public long getEventRetentionPeriod() { return eventRetentionPeriod; }
        public void setEventRetentionPeriod(long period) { this.eventRetentionPeriod = period; }
        
        public int getMaxContentLength() { return maxContentLength; }
        public void setMaxContentLength(int length) { this.maxContentLength = length; }
    }
    

}