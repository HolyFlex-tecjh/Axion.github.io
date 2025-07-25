package com.axion.bot.moderation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Real-time Threat Intelligence System for enhanced moderation
 * Provides threat detection, IP/domain reputation, and coordinated attack detection
 */
public class RealTimeThreatIntelligence {
    private static final Logger logger = LoggerFactory.getLogger(RealTimeThreatIntelligence.class);
    
    // Threat intelligence caches
    private final Cache<String, ThreatIntelligenceResult> threatCache;
    private final Cache<String, DomainReputation> domainReputationCache;
    private final Cache<String, IPReputation> ipReputationCache;
    private final Cache<String, UserThreatProfile> userThreatCache;
    
    // Threat detection engines
    private final MalwareDetectionEngine malwareEngine;
    private final PhishingDetectionEngine phishingEngine;
    private final SpamDetectionEngine spamEngine;
    private final CoordinatedAttackDetector attackDetector;
    private final ThreatFeedManager feedManager;
    
    // Real-time monitoring
    private final ScheduledExecutorService scheduler;
    private final ExecutorService analysisExecutor;
    
    // Performance tracking
    private final AtomicLong totalThreatsDetected = new AtomicLong(0);
    private final AtomicLong malwareDetections = new AtomicLong(0);
    private final AtomicLong phishingDetections = new AtomicLong(0);
    private final AtomicLong spamDetections = new AtomicLong(0);
    private final AtomicLong coordinatedAttacks = new AtomicLong(0);
    
    // Configuration
    private final ThreatIntelligenceConfig config;
    
    public RealTimeThreatIntelligence(ThreatIntelligenceConfig config) {
        this.config = config;
        
        // Initialize caches
        this.threatCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedThreats())
            .expireAfterWrite(Duration.ofMinutes(config.getThreatCacheMinutes()))
            .build();
        
        this.domainReputationCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedDomains())
            .expireAfterWrite(Duration.ofHours(config.getDomainCacheHours()))
            .build();
        
        this.ipReputationCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedIPs())
            .expireAfterWrite(Duration.ofHours(config.getIpCacheHours()))
            .build();
        
        this.userThreatCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedUsers())
            .expireAfterWrite(Duration.ofHours(config.getUserCacheHours()))
            .build();
        
        // Initialize detection engines
        this.malwareEngine = new MalwareDetectionEngine(config.getMalwareConfig());
        this.phishingEngine = new PhishingDetectionEngine(config.getPhishingConfig());
        this.spamEngine = new SpamDetectionEngine(); // Use no-argument constructor
        this.attackDetector = new CoordinatedAttackDetector(config.getAttackConfig());
        this.feedManager = new ThreatFeedManager(config.getFeedConfig());
        
        // Initialize executors
        this.scheduler = Executors.newScheduledThreadPool(config.getSchedulerThreads());
        this.analysisExecutor = Executors.newFixedThreadPool(config.getAnalysisThreads());
        
        // Start background tasks
        startBackgroundTasks();
        
        logger.info("RealTimeThreatIntelligence initialized with {} threat feeds", 
                   config.getFeedConfig().getEnabledFeeds().size());
    }
    
    /**
     * Analyze content for threats
     */
    public CompletableFuture<ThreatAnalysisResult> analyzeContent(String content, String userId, 
                                                                 String guildId, ContentType contentType) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Check cache first
                String cacheKey = generateContentCacheKey(content, contentType);
                ThreatIntelligenceResult cached = threatCache.getIfPresent(cacheKey);
                if (cached != null) {
                    return new ThreatAnalysisResult(cached, System.currentTimeMillis() - startTime);
                }
                
                // Perform multi-layer threat analysis
                List<ThreatDetection> detections = new ArrayList<>();
                
                // Malware detection
                if (config.isMalwareDetectionEnabled()) {
                    ThreatDetection malwareResult = malwareEngine.analyzeContent(content, contentType);
                    if (malwareResult.isDetected()) {
                        detections.add(malwareResult);
                        malwareDetections.incrementAndGet();
                    }
                }
                
                // Phishing detection
                if (config.isPhishingDetectionEnabled()) {
                    ThreatDetection phishingResult = phishingEngine.analyzeContent(content, contentType);
                    if (phishingResult.isDetected()) {
                        detections.add(phishingResult);
                        phishingDetections.incrementAndGet();
                    }
                }
                
                // Spam detection
                if (config.isSpamDetectionEnabled()) {
                    ThreatDetection spamResult = spamEngine.analyzeContent(content, contentType);
                    if (spamResult.isDetected()) {
                        detections.add(spamResult);
                        spamDetections.incrementAndGet();
                    }
                }
                
                // Check for coordinated attacks
                if (config.isCoordinatedAttackDetectionEnabled()) {
                    ThreatDetection attackResult = attackDetector.analyzeUserActivity(userId, guildId, content);
                    if (attackResult.isDetected()) {
                        detections.add(attackResult);
                        coordinatedAttacks.incrementAndGet();
                    }
                }
                
                // Consolidate results
                ThreatIntelligenceResult result = consolidateDetections(detections, content, userId, guildId);
                
                // Update user threat profile
                updateUserThreatProfile(userId, result);
                
                // Cache result
                threatCache.put(cacheKey, result);
                
                // Update statistics
                if (result.getThreatLevel() != ThreatLevel.NONE) {
                    totalThreatsDetected.incrementAndGet();
                }
                
                return new ThreatAnalysisResult(result, System.currentTimeMillis() - startTime);
                
            } catch (Exception e) {
                logger.error("Error analyzing content for threats", e);
                return new ThreatAnalysisResult(
                    ThreatIntelligenceResult.error("Analysis failed: " + e.getMessage()),
                    System.currentTimeMillis() - startTime
                );
            }
        }, analysisExecutor);
    }
    
    /**
     * Check domain reputation
     */
    public CompletableFuture<DomainReputation> checkDomainReputation(String domain) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache first
            DomainReputation cached = domainReputationCache.getIfPresent(domain);
            if (cached != null) {
                return cached;
            }
            
            // Perform reputation check
            DomainReputation reputation = performDomainReputationCheck(domain);
            
            // Cache result
            domainReputationCache.put(domain, reputation);
            
            return reputation;
        }, analysisExecutor);
    }
    
    /**
     * Check IP reputation
     */
    public CompletableFuture<IPReputation> checkIPReputation(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache first
            IPReputation cached = ipReputationCache.getIfPresent(ipAddress);
            if (cached != null) {
                return cached;
            }
            
            // Perform reputation check
            IPReputation reputation = performIPReputationCheck(ipAddress);
            
            // Cache result
            ipReputationCache.put(ipAddress, reputation);
            
            return reputation;
        }, analysisExecutor);
    }
    
    /**
     * Get user threat profile
     */
    public UserThreatProfile getUserThreatProfile(String userId) {
        return userThreatCache.get(userId, k -> new UserThreatProfile(userId));
    }
    
    /**
     * Report threat intelligence
     */
    public void reportThreat(ThreatReport report) {
        try {
            // Validate report
            if (!validateThreatReport(report)) {
                logger.warn("Invalid threat report received: {}", report);
                return;
            }
            
            // Process threat report
            processThreatReport(report);
            
            // Update threat feeds
            feedManager.updateThreatFeeds(report);
            
            // Notify other systems
            notifyThreatDetected(report);
            
            logger.info("Threat report processed: {} - {}", report.getThreatType(), report.getIndicator());
            
        } catch (Exception e) {
            logger.error("Error processing threat report", e);
        }
    }
    
    /**
     * Get threat intelligence statistics
     */
    public ThreatIntelligenceStats getStats() {
        return new ThreatIntelligenceStats(
            totalThreatsDetected.get(),
            malwareDetections.get(),
            phishingDetections.get(),
            spamDetections.get(),
            coordinatedAttacks.get(),
            threatCache.estimatedSize(),
            domainReputationCache.estimatedSize(),
            ipReputationCache.estimatedSize(),
            userThreatCache.estimatedSize()
        );
    }
    
    /**
     * Get active threat indicators
     */
    public List<ThreatIndicator> getActiveThreatIndicators(Duration period) {
        Instant since = Instant.now().minus(period);
        
        return feedManager.getActiveThreatIndicators(since);
    }
    
    /**
     * Consolidate threat detections
     */
    private ThreatIntelligenceResult consolidateDetections(List<ThreatDetection> detections, 
                                                          String content, String userId, String guildId) {
        if (detections.isEmpty()) {
            return ThreatIntelligenceResult.clean();
        }
        
        // Determine overall threat level
        ThreatLevel maxLevel = detections.stream()
            .map(ThreatDetection::getThreatLevel)
            .max(Comparator.comparing(ThreatLevel::getSeverity))
            .orElse(ThreatLevel.NONE);
        
        // Calculate confidence score
        double avgConfidence = detections.stream()
            .mapToDouble(ThreatDetection::getConfidence)
            .average()
            .orElse(0.0);
        
        // Collect threat types
        Set<ThreatType> threatTypes = detections.stream()
            .map(ThreatDetection::getThreatType)
            .collect(Collectors.toSet());
        
        // Collect indicators
        List<String> indicators = detections.stream()
            .flatMap(d -> d.getIndicators().stream())
            .distinct()
            .collect(Collectors.toList());
        
        // Generate recommendations
        List<String> recommendations = generateRecommendations(detections, maxLevel);
        
        // Create metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("detectionCount", detections.size());
        metadata.put("userId", userId);
        metadata.put("guildId", guildId);
        metadata.put("contentLength", content.length());
        metadata.put("timestamp", Instant.now());
        
        return new ThreatIntelligenceResult(
            maxLevel,
            avgConfidence,
            threatTypes,
            indicators,
            recommendations,
            metadata,
            detections
        );
    }
    
    /**
     * Update user threat profile
     */
    private void updateUserThreatProfile(String userId, ThreatIntelligenceResult result) {
        UserThreatProfile profile = getUserThreatProfile(userId);
        profile.addThreatResult(result);
        userThreatCache.put(userId, profile);
    }
    
    /**
     * Perform domain reputation check
     */
    private DomainReputation performDomainReputationCheck(String domain) {
        // Simplified implementation - would integrate with real threat feeds
        
        // Check against known malicious domains
        if (isKnownMaliciousDomain(domain)) {
            return new DomainReputation(
                domain,
                ReputationLevel.MALICIOUS,
                0.9,
                Arrays.asList("Known malware distribution", "Phishing campaigns"),
                Instant.now()
            );
        }
        
        // Check domain age and characteristics
        if (isSuspiciousDomain(domain)) {
            return new DomainReputation(
                domain,
                ReputationLevel.SUSPICIOUS,
                0.6,
                Arrays.asList("Recently registered", "Suspicious TLD"),
                Instant.now()
            );
        }
        
        // Default to clean
        return new DomainReputation(
            domain,
            ReputationLevel.CLEAN,
            0.1,
            new ArrayList<>(),
            Instant.now()
        );
    }
    
    /**
     * Perform IP reputation check
     */
    private IPReputation performIPReputationCheck(String ipAddress) {
        // Simplified implementation - would integrate with real threat feeds
        
        // Check against known malicious IPs
        if (isKnownMaliciousIP(ipAddress)) {
            return new IPReputation(
                ipAddress,
                ReputationLevel.MALICIOUS,
                0.95,
                Arrays.asList("Botnet C&C", "Spam source"),
                determineIPGeolocation(ipAddress),
                Instant.now()
            );
        }
        
        // Check for suspicious patterns
        if (isSuspiciousIP(ipAddress)) {
            return new IPReputation(
                ipAddress,
                ReputationLevel.SUSPICIOUS,
                0.7,
                Arrays.asList("Tor exit node", "VPN service"),
                determineIPGeolocation(ipAddress),
                Instant.now()
            );
        }
        
        // Default to clean
        return new IPReputation(
            ipAddress,
            ReputationLevel.CLEAN,
            0.1,
            new ArrayList<>(),
            determineIPGeolocation(ipAddress),
            Instant.now()
        );
    }
    
    /**
     * Generate recommendations based on detections
     */
    private List<String> generateRecommendations(List<ThreatDetection> detections, ThreatLevel maxLevel) {
        List<String> recommendations = new ArrayList<>();
        
        switch (maxLevel) {
            case CRITICAL:
                recommendations.add("Immediately ban user and delete content");
                recommendations.add("Report to Discord Trust & Safety");
                recommendations.add("Scan all recent messages from user");
                break;
            case VERY_HIGH:
                recommendations.add("Ban user and escalate to moderators");
                recommendations.add("Delete all related content");
                recommendations.add("Initiate incident response procedures");
                break;
            case HIGH:
                recommendations.add("Timeout user and delete content");
                recommendations.add("Review user's recent activity");
                recommendations.add("Consider temporary ban");
                break;
            case MEDIUM:
                recommendations.add("Delete content and warn user");
                recommendations.add("Monitor user activity closely");
                break;
            case LOW:
                recommendations.add("Flag content for manual review");
                recommendations.add("Add to watch list");
                break;
            case NONE:
                // No action needed for NONE threat level
                break;
        }
        
        // Add specific recommendations based on threat types
        Set<ThreatType> threatTypes = detections.stream()
            .map(ThreatDetection::getThreatType)
            .collect(Collectors.toSet());
        
        if (threatTypes.contains(ThreatType.MALWARE)) {
            recommendations.add("Scan all file attachments from user");
        }
        
        if (threatTypes.contains(ThreatType.PHISHING)) {
            recommendations.add("Check for similar phishing attempts");
            recommendations.add("Warn other users about phishing");
        }
        
        if (threatTypes.contains(ThreatType.COORDINATED_ATTACK)) {
            recommendations.add("Investigate related accounts");
            recommendations.add("Enable enhanced security measures");
        }
        
        return recommendations;
    }
    
    /**
     * Start background tasks
     */
    private void startBackgroundTasks() {
        // Update threat feeds periodically
        scheduler.scheduleAtFixedRate(
            this::updateThreatFeeds,
            0,
            config.getFeedUpdateIntervalMinutes(),
            TimeUnit.MINUTES
        );
        
        // Clean up old cache entries
        scheduler.scheduleAtFixedRate(
            this::performCacheMaintenance,
            config.getCacheMaintenanceIntervalHours(),
            config.getCacheMaintenanceIntervalHours(),
            TimeUnit.HOURS
        );
        
        // Generate threat intelligence reports
        scheduler.scheduleAtFixedRate(
            this::generateThreatReport,
            config.getReportGenerationIntervalHours(),
            config.getReportGenerationIntervalHours(),
            TimeUnit.HOURS
        );
    }
    
    /**
     * Update threat feeds
     */
    private void updateThreatFeeds() {
        try {
            logger.debug("Updating threat intelligence feeds");
            feedManager.updateAllFeeds();
            logger.debug("Threat intelligence feeds updated successfully");
        } catch (Exception e) {
            logger.error("Error updating threat intelligence feeds", e);
        }
    }
    
    /**
     * Perform cache maintenance
     */
    private void performCacheMaintenance() {
        logger.debug("Performing threat intelligence cache maintenance");
        
        threatCache.cleanUp();
        domainReputationCache.cleanUp();
        ipReputationCache.cleanUp();
        userThreatCache.cleanUp();
        
        logger.debug("Cache maintenance completed");
    }
    
    /**
     * Generate threat intelligence report
     */
    private void generateThreatReport() {
        try {
            logger.info("Generating threat intelligence report");
            
            ThreatIntelligenceStats stats = getStats();
            List<ThreatIndicator> recentThreats = getActiveThreatIndicators(Duration.ofHours(24));
            
            // Log summary
            logger.info("Threat Intelligence Summary - Total: {}, Malware: {}, Phishing: {}, Spam: {}, Attacks: {}",
                       stats.getTotalThreatsDetected(),
                       stats.getMalwareDetections(),
                       stats.getPhishingDetections(),
                       stats.getSpamDetections(),
                       stats.getCoordinatedAttacks());
            logger.info("Recent Threat Indicators (last 24h): {}", recentThreats);
            
        } catch (Exception e) {
            logger.error("Error generating threat intelligence report", e);
        }
    }
    
    // Helper methods
    private String generateContentCacheKey(String content, ContentType contentType) {
        return contentType.name() + ":" + Integer.toHexString(content.hashCode());
    }
    
    private boolean validateThreatReport(ThreatReport report) {
        return report != null && 
               report.getThreatType() != null && 
               report.getIndicator() != null && 
               !report.getIndicator().trim().isEmpty();
    }
    
    private void processThreatReport(ThreatReport report) {
        // Process and store threat report
        // This would integrate with the threat database
    }
    
    private void notifyThreatDetected(ThreatReport report) {
        // Notify other systems about the threat
        // This could include webhooks, alerts, etc.
    }
    
    private boolean isKnownMaliciousDomain(String domain) {
        // Check against threat feeds
        return feedManager.isMaliciousDomain(domain);
    }
    
    private boolean isSuspiciousDomain(String domain) {
        // Check for suspicious characteristics
        return domain.length() > 50 || 
               domain.contains("bit.ly") || 
               domain.contains("tinyurl") ||
               domain.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"); // IP-based domains
    }
    
    private boolean isKnownMaliciousIP(String ipAddress) {
        return feedManager.isMaliciousIP(ipAddress);
    }
    
    private boolean isSuspiciousIP(String ipAddress) {
        // Check for suspicious IP patterns
        return feedManager.isSuspiciousIP(ipAddress);
    }
    
    private String determineIPGeolocation(String ipAddress) {
        // Simplified geolocation - would use real GeoIP service
        return "Unknown";
    }
    
    /**
     * Shutdown the threat intelligence system
     */
    public void shutdown() {
        logger.info("Shutting down threat intelligence system");
        
        scheduler.shutdown();
        analysisExecutor.shutdown();
        
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!analysisExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                analysisExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            analysisExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Threat intelligence system shutdown completed");
    }
}

// ThreatLevel enum removed - using separate ThreatLevel.java file

// ThreatType enum removed - using separate ThreatType.java file

enum ContentType {
    TEXT, URL, FILE, IMAGE, VIDEO, AUDIO
}

enum ReputationLevel {
    CLEAN, SUSPICIOUS, MALICIOUS
}

// Configuration class
class ThreatIntelligenceConfig {
    private int maxCachedThreats = 10000;
    private int threatCacheMinutes = 30;
    private int maxCachedDomains = 5000;
    private int domainCacheHours = 24;
    private int maxCachedIPs = 5000;
    private int ipCacheHours = 12;
    private int maxCachedUsers = 10000;
    private int userCacheHours = 6;
    
    private boolean malwareDetectionEnabled = true;
    private boolean phishingDetectionEnabled = true;
    private boolean spamDetectionEnabled = true;
    private boolean coordinatedAttackDetectionEnabled = true;
    
    private int schedulerThreads = 2;
    private int analysisThreads = 4;
    private int feedUpdateIntervalMinutes = 15;
    private int cacheMaintenanceIntervalHours = 6;
    private int reportGenerationIntervalHours = 24;
    
    private MalwareDetectionConfig malwareConfig = new MalwareDetectionConfig();
    private PhishingDetectionConfig phishingConfig = new PhishingDetectionConfig();
    private RealTimeSpamDetectionConfig spamConfig = new RealTimeSpamDetectionConfig();
    private CoordinatedAttackConfig attackConfig = new CoordinatedAttackConfig();
    private ThreatFeedConfig feedConfig = new ThreatFeedConfig();
    
    // Getters
    public int getMaxCachedThreats() { return maxCachedThreats; }
    public int getThreatCacheMinutes() { return threatCacheMinutes; }
    public int getMaxCachedDomains() { return maxCachedDomains; }
    public int getDomainCacheHours() { return domainCacheHours; }
    public int getMaxCachedIPs() { return maxCachedIPs; }
    public int getIpCacheHours() { return ipCacheHours; }
    public int getMaxCachedUsers() { return maxCachedUsers; }
    public int getUserCacheHours() { return userCacheHours; }
    public boolean isMalwareDetectionEnabled() { return malwareDetectionEnabled; }
    public boolean isPhishingDetectionEnabled() { return phishingDetectionEnabled; }
    public boolean isSpamDetectionEnabled() { return spamDetectionEnabled; }
    public boolean isCoordinatedAttackDetectionEnabled() { return coordinatedAttackDetectionEnabled; }
    public int getSchedulerThreads() { return schedulerThreads; }
    public int getAnalysisThreads() { return analysisThreads; }
    public int getFeedUpdateIntervalMinutes() { return feedUpdateIntervalMinutes; }
    public int getCacheMaintenanceIntervalHours() { return cacheMaintenanceIntervalHours; }
    public int getReportGenerationIntervalHours() { return reportGenerationIntervalHours; }
    public MalwareDetectionConfig getMalwareConfig() { return malwareConfig; }
    public PhishingDetectionConfig getPhishingConfig() { return phishingConfig; }
    public RealTimeSpamDetectionConfig getSpamConfig() { return spamConfig; }
    public CoordinatedAttackConfig getAttackConfig() { return attackConfig; }
    public ThreatFeedConfig getFeedConfig() { return feedConfig; }
}

// Placeholder configuration classes
class MalwareDetectionConfig {
    private double confidenceThreshold = 0.7;
    private boolean fileHashChecking = true;
    private boolean urlAnalysis = true;
    
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public boolean isFileHashChecking() { return fileHashChecking; }
    public boolean isUrlAnalysis() { return urlAnalysis; }
}

class PhishingDetectionConfig {
    private double confidenceThreshold = 0.6;
    private boolean domainSimilarityCheck = true;
    private boolean urlStructureAnalysis = true;
    
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public boolean isDomainSimilarityCheck() { return domainSimilarityCheck; }
    public boolean isUrlStructureAnalysis() { return urlStructureAnalysis; }
}

class RealTimeSpamDetectionConfig {
    private double confidenceThreshold = 0.5;
    private boolean contentAnalysis = true;
    private boolean frequencyAnalysis = true;
    
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public boolean isContentAnalysis() { return contentAnalysis; }
    public boolean isFrequencyAnalysis() { return frequencyAnalysis; }
}

class CoordinatedAttackConfig {
    private double confidenceThreshold = 0.8;
    private int timeWindowMinutes = 30;
    private int minimumParticipants = 3;
    
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public int getTimeWindowMinutes() { return timeWindowMinutes; }
    public int getMinimumParticipants() { return minimumParticipants; }
}

class ThreatFeedConfig {
    private List<String> enabledFeeds = Arrays.asList("internal", "community");
    private boolean autoUpdate = true;
    private int maxFeedSize = 100000;
    
    public List<String> getEnabledFeeds() { return new ArrayList<>(enabledFeeds); }
    public boolean isAutoUpdate() { return autoUpdate; }
    public int getMaxFeedSize() { return maxFeedSize; }
}