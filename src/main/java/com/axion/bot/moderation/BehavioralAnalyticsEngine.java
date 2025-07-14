package com.axion.bot.moderation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Advanced Behavioral Analytics Engine for user behavior pattern analysis
 * Tracks user interactions, identifies suspicious patterns, and calculates risk scores
 */
public class BehavioralAnalyticsEngine {
    private static final Logger logger = LoggerFactory.getLogger(BehavioralAnalyticsEngine.class);
    
    // Caching for performance
    private final Cache<String, UserBehaviorProfile> userProfiles;
    private final Cache<String, List<UserActivity>> recentActivities;
    private final Cache<String, RiskAssessment> riskAssessments;
    
    // Pattern detection engines
    private final SpamPatternDetector spamDetector;
    private final ToxicityPatternDetector toxicityDetector;
    private final SuspiciousActivityDetector suspiciousDetector;
    private final RaidPatternDetector raidDetector;
    
    // Analytics storage
    private final Map<String, UserBehaviorMetrics> behaviorMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<BehaviorPattern>> detectedPatterns = new ConcurrentHashMap<>();
    
    // Configuration
    private final BehavioralAnalyticsConfig config;
    private final SpamDetectionConfig spamConfig = new SpamDetectionConfig();
    
    // Performance metrics
    private final AtomicLong totalAnalyses = new AtomicLong(0);
    private final AtomicLong suspiciousActivities = new AtomicLong(0);
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    
    public BehavioralAnalyticsEngine(BehavioralAnalyticsConfig config) {
        this.config = config;
        
        // Initialize caches
        this.userProfiles = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedProfiles())
            .expireAfterWrite(Duration.ofHours(config.getProfileCacheHours()))
            .build();
            
        this.recentActivities = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedActivities())
            .expireAfterWrite(Duration.ofMinutes(config.getActivityCacheMinutes()))
            .build();
            
        this.riskAssessments = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedAssessments())
            .expireAfterWrite(Duration.ofMinutes(config.getAssessmentCacheMinutes()))
            .build();
        
        // Initialize pattern detectors
        this.spamDetector = new SpamPatternDetector(spamConfig);
        this.toxicityDetector = new ToxicityPatternDetector(config.getToxicityConfig());
        this.suspiciousDetector = new SuspiciousActivityDetector(config.getSuspiciousConfig());
        this.raidDetector = new RaidPatternDetector(config.getRaidConfig());
        
        logger.info("BehavioralAnalyticsEngine initialized with {} pattern detectors", 4);
    }
    
    /**
     * Main method to analyze user behavior and update profiles
     */
    public BehaviorAnalysisResult analyzeUserBehavior(String userId, String guildId, 
                                                     UserActivity activity) {
        long startTime = System.currentTimeMillis();
        totalAnalyses.incrementAndGet();
        
        try {
            // Get or create user behavior profile
            UserBehaviorProfile profile = getUserProfile(userId, guildId);
            
            // Update profile with new activity
            updateUserProfile(profile, activity);
            
            // Analyze patterns
            List<BehaviorPattern> patterns = analyzePatterns(profile, activity);
            
            // Calculate risk assessment
            RiskAssessment riskAssessment = calculateRiskAssessment(profile, patterns);
            
            // Update caches
            userProfiles.put(userId + ":" + guildId, profile);
            riskAssessments.put(userId + ":" + guildId, riskAssessment);
            
            // Store detected patterns
            if (!patterns.isEmpty()) {
                detectedPatterns.put(userId + ":" + guildId, patterns);
                suspiciousActivities.incrementAndGet();
            }
            
            // Create analysis result
            BehaviorAnalysisResult result = new BehaviorAnalysisResult(
                userId, guildId, profile, patterns, riskAssessment,
                System.currentTimeMillis() - startTime
            );
            
            // Log significant findings
            if (riskAssessment.getRiskLevel().ordinal() >= RiskLevel.HIGH.ordinal()) {
                logger.warn("High-risk behavior detected for user {} in guild {}: {}", 
                           userId, guildId, riskAssessment.getRiskFactors());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error analyzing user behavior for user {} in guild {}", userId, guildId, e);
            return BehaviorAnalysisResult.error(userId, guildId, "Analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze multiple users for coordinated behavior (raid detection)
     */
    public CoordinatedBehaviorResult analyzeCoordinatedBehavior(String guildId, 
                                                              List<UserActivity> activities) {
        try {
            // Group activities by time windows
            Map<Instant, List<UserActivity>> timeWindows = groupActivitiesByTimeWindow(activities);
            
            List<CoordinatedPattern> coordinatedPatterns = new ArrayList<>();
            
            for (Map.Entry<Instant, List<UserActivity>> window : timeWindows.entrySet()) {
                List<UserActivity> windowActivities = window.getValue();
                
                // Check for raid patterns
                Optional<CoordinatedPattern> raidPattern = raidDetector.detectRaidPattern(windowActivities);
                raidPattern.ifPresent(coordinatedPatterns::add);
                
                // Check for coordinated spam
                Optional<CoordinatedPattern> spamPattern = detectCoordinatedSpam(windowActivities);
                spamPattern.ifPresent(coordinatedPatterns::add);
                
                // Check for coordinated toxicity
                Optional<CoordinatedPattern> toxicityPattern = detectCoordinatedToxicity(windowActivities);
                toxicityPattern.ifPresent(coordinatedPatterns::add);
            }
            
            // Calculate overall threat level
            ThreatLevel threatLevel = calculateThreatLevel(coordinatedPatterns);
            
            CoordinatedBehaviorResult.Builder builder = CoordinatedBehaviorResult.builder(guildId)
                .withThreatLevel(threatLevel)
                .withAnalysisTime(activities.size());
            
            // Add all detected patterns
            for (CoordinatedPattern pattern : coordinatedPatterns) {
                builder.addDetectedPattern(pattern);
            }
            
            return builder.build();
            
        } catch (Exception e) {
            logger.error("Error analyzing coordinated behavior in guild {}", guildId, e);
            return CoordinatedBehaviorResult.error(guildId, "Coordinated analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Get comprehensive user behavior insights
     */
    public UserBehaviorInsights getUserBehaviorInsights(String userId, String guildId) {
        UserBehaviorProfile profile = userProfiles.getIfPresent(userId + ":" + guildId);
        if (profile == null) {
            return UserBehaviorInsights.notFound(userId, guildId);
        }
        
        RiskAssessment riskAssessment = riskAssessments.getIfPresent(userId + ":" + guildId);
        List<BehaviorPattern> patterns = detectedPatterns.getOrDefault(userId + ":" + guildId, 
                                                                     new ArrayList<>());
        
        return UserBehaviorInsights.fromProfile(profile);
    }
    
    /**
     * Get or create user behavior profile
     */
    private UserBehaviorProfile getUserProfile(String userId, String guildId) {
        String key = userId + ":" + guildId;
        UserBehaviorProfile profile = userProfiles.getIfPresent(key);
        
        if (profile == null) {
            profile = new UserBehaviorProfile(userId, guildId);
            activeUsers.incrementAndGet();
        }
        
        return profile;
    }
    
    /**
     * Update user profile with new activity
     */
    private void updateUserProfile(UserBehaviorProfile profile, UserActivity activity) {
        profile.addActivity(activity.getType());
        
        // Update metrics
        UserBehaviorMetrics metrics = behaviorMetrics.computeIfAbsent(
            profile.getUserId() + ":" + profile.getGuildId(), 
            k -> new UserBehaviorMetrics()
        );
        
        metrics.updateWithActivity(activity);
    }
    
    /**
     * Analyze behavior patterns for a user
     */
    private List<BehaviorPattern> analyzePatterns(UserBehaviorProfile profile, UserActivity activity) {
        List<BehaviorPattern> patterns = new ArrayList<>();
        
        // Spam pattern detection
        Optional<BehaviorPattern> spamPattern = spamDetector.detectPattern(profile, activity);
        spamPattern.ifPresent(patterns::add);
        
        // Toxicity pattern detection
        Optional<BehaviorPattern> toxicityPattern = toxicityDetector.detectPattern(profile, activity);
        toxicityPattern.ifPresent(patterns::add);
        
        // Suspicious activity detection
        Optional<BehaviorPattern> suspiciousPattern = suspiciousDetector.detectPattern(profile, activity);
        suspiciousPattern.ifPresent(patterns::add);
        
        // Additional pattern checks
        patterns.addAll(detectAdvancedPatterns(profile, activity));
        
        return patterns;
    }
    
    /**
     * Calculate comprehensive risk assessment
     */
    private RiskAssessment calculateRiskAssessment(UserBehaviorProfile profile, 
                                                 List<BehaviorPattern> patterns) {
        double baseRiskScore = 0.0;
        List<String> riskFactors = new ArrayList<>();
        
        // Account age factor
        long accountAgeHours = ChronoUnit.HOURS.between(profile.getAccountCreated(), Instant.now());
        if (accountAgeHours < config.getNewAccountThresholdHours()) {
            baseRiskScore += 0.3;
            riskFactors.add("new_account");
        }
        
        // Activity frequency factor
        double activityRate = profile.getActivityRate();
        if (activityRate > config.getHighActivityThreshold()) {
            baseRiskScore += 0.2;
            riskFactors.add("high_activity_rate");
        }
        
        // Pattern-based risk
        for (BehaviorPattern pattern : patterns) {
            baseRiskScore += pattern.getRiskWeight();
            riskFactors.add(pattern.getPatternType().toString().toLowerCase());
        }
        
        // Historical violations
        int violationCount = profile.getViolationHistory().size();
        if (violationCount > 0) {
            baseRiskScore += Math.min(0.4, violationCount * 0.1);
            riskFactors.add("violation_history");
        }
        
        // Trust score factor (inverse relationship)
        double trustScore = profile.getTrustScore();
        baseRiskScore += (1.0 - trustScore) * 0.3;
        
        // Normalize risk score
        double finalRiskScore = Math.min(1.0, baseRiskScore);
        
        // Determine risk level
        RiskLevel riskLevel = determineRiskLevel(finalRiskScore);
        
        return RiskAssessment.builder(profile.getUserId(), profile.getGuildId())
                .withRiskScore(finalRiskScore)
                .withRiskLevel(riskLevel)
                .addRiskReason(String.join(", ", riskFactors))
                .build();
    }
    
    /**
     * Detect advanced behavior patterns
     */
    private List<BehaviorPattern> detectAdvancedPatterns(UserBehaviorProfile profile, 
                                                       UserActivity activity) {
        List<BehaviorPattern> patterns = new ArrayList<>();
        
        // Escalation pattern (increasing severity over time)
        if (detectEscalationPattern(profile)) {
            patterns.add(new BehaviorPattern(PatternType.ESCALATION, 0.6, 
                        "User behavior shows escalating severity"));
        }
        
        // Time-based suspicious activity
        if (detectSuspiciousTimingPattern(profile)) {
            patterns.add(new BehaviorPattern(PatternType.SUSPICIOUS_TIMING, 0.4, 
                        "Unusual activity timing detected"));
        }
        
        // Evasion attempts
        if (detectEvasionPattern(profile, activity)) {
            patterns.add(new BehaviorPattern(PatternType.EVASION, 0.7, 
                        "Potential filter evasion detected"));
        }
        
        // Social engineering indicators
        if (detectSocialEngineeringPattern(profile, activity)) {
            patterns.add(new BehaviorPattern(PatternType.SOCIAL_ENGINEERING, 0.8, 
                        "Social engineering behavior detected"));
        }
        
        return patterns;
    }
    
    /**
     * Group activities by time windows for coordinated analysis
     */
    private Map<Instant, List<UserActivity>> groupActivitiesByTimeWindow(List<UserActivity> activities) {
        Map<Instant, List<UserActivity>> windows = new HashMap<>();
        Duration windowSize = Duration.ofMinutes(config.getCoordinatedAnalysisWindowMinutes());
        
        for (UserActivity activity : activities) {
            Instant windowStart = activity.getTimestamp().truncatedTo(ChronoUnit.MINUTES)
                .minus(activity.getTimestamp().getEpochSecond() % windowSize.getSeconds(), 
                       ChronoUnit.SECONDS);
            
            windows.computeIfAbsent(windowStart, k -> new ArrayList<>()).add(activity);
        }
        
        return windows;
    }
    
    /**
     * Detect coordinated spam patterns
     */
    private Optional<CoordinatedPattern> detectCoordinatedSpam(List<UserActivity> activities) {
        if (activities.size() < config.getMinCoordinatedUsers()) {
            return Optional.empty();
        }
        
        // Check for similar content
        Map<String, List<UserActivity>> contentGroups = activities.stream()
            .filter(a -> a.getType() == ActivityType.MESSAGE)
            .collect(Collectors.groupingBy(a -> normalizeContent(a.getContent())));
        
        for (Map.Entry<String, List<UserActivity>> group : contentGroups.entrySet()) {
            if (group.getValue().size() >= config.getMinCoordinatedUsers()) {
                Set<String> userIds = group.getValue().stream()
                    .map(UserActivity::getUserId)
                    .collect(Collectors.toSet());
                
                if (userIds.size() >= config.getMinCoordinatedUsers()) {
                    return Optional.of(CoordinatedPattern.builder(CoordinatedPatternType.SPAM)
                         .addInvolvedUsers(userIds)
                         .withStrength(80.0)
                        .withDescription("Coordinated spam detected: " + group.getKey())
                        .build());
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Detect coordinated toxicity patterns
     */
    private Optional<CoordinatedPattern> detectCoordinatedToxicity(List<UserActivity> activities) {
        List<UserActivity> toxicActivities = activities.stream()
            .filter(a -> a.getType() == ActivityType.MESSAGE && a.isToxic())
            .collect(Collectors.toList());
        
        if (toxicActivities.size() >= config.getMinCoordinatedUsers()) {
            Set<String> userIds = toxicActivities.stream()
                .map(UserActivity::getUserId)
                .collect(Collectors.toSet());
            
            if (userIds.size() >= config.getMinCoordinatedUsers()) {
                return Optional.of(CoordinatedPattern.builder(CoordinatedPatternType.TOXICITY)
                     .addInvolvedUsers(userIds)
                     .withStrength(90.0)
                    .withDescription("Coordinated toxicity attack detected")
                    .build());
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Calculate threat level for coordinated behavior
     */
    private ThreatLevel calculateThreatLevel(List<CoordinatedPattern> patterns) {
        if (patterns.isEmpty()) {
            return ThreatLevel.NONE;
        }
        
        double maxSeverity = patterns.stream()
            .mapToDouble(pattern -> pattern.getStrength() / 100.0)
            .max()
            .orElse(0.0);
        
        if (maxSeverity >= 0.8) return ThreatLevel.CRITICAL;
        if (maxSeverity >= 0.6) return ThreatLevel.HIGH;
        if (maxSeverity >= 0.4) return ThreatLevel.MEDIUM;
        return ThreatLevel.LOW;
    }
    
    // Pattern detection helper methods
    private boolean detectEscalationPattern(UserBehaviorProfile profile) {
        List<UserActivity> recentActivities = profile.getRecentActivities(Duration.ofHours(24));
        if (recentActivities.size() < 3) return false;
        
        // Check if severity is increasing over time
        for (int i = 1; i < recentActivities.size(); i++) {
            if (recentActivities.get(i).getSeverity() <= recentActivities.get(i-1).getSeverity()) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean detectSuspiciousTimingPattern(UserBehaviorProfile profile) {
        List<UserActivity> activities = profile.getRecentActivities(Duration.ofHours(1));
        if (activities.size() < 5) return false;
        
        // Check for activities at unusual hours (e.g., 2-6 AM)
        long suspiciousHourActivities = activities.stream()
            .filter(a -> {
                int hour = LocalDateTime.ofInstant(a.getTimestamp(), 
                                                 java.time.ZoneOffset.UTC).getHour();
                return hour >= 2 && hour <= 6;
            })
            .count();
        
        return suspiciousHourActivities > activities.size() * 0.8;
    }
    
    private boolean detectEvasionPattern(UserBehaviorProfile profile, UserActivity activity) {
        if (activity.getType() != ActivityType.MESSAGE) return false;
        
        String content = activity.getContent().toLowerCase();
        
        // Check for character substitution patterns
        boolean hasSubstitutions = content.matches(".*[4@][3€][1!|][0°][5$].*");
        
        // Check for excessive spacing or special characters
        boolean hasSpacing = content.matches(".*\\w\\s+\\w\\s+\\w.*");
        
        // Check for zalgo text or unicode abuse
        boolean hasUnicodeAbuse = content.matches(".*[\u0300-\u036F]{3,}.*");
        
        return hasSubstitutions || hasSpacing || hasUnicodeAbuse;
    }
    
    private boolean detectSocialEngineeringPattern(UserBehaviorProfile profile, UserActivity activity) {
        if (activity.getType() != ActivityType.MESSAGE) return false;
        
        String content = activity.getContent().toLowerCase();
        
        // Check for urgency indicators
        boolean hasUrgency = content.matches(".*(urgent|immediately|now|quick|fast|limited time).*");
        
        // Check for authority claims
        boolean hasAuthority = content.matches(".*(admin|moderator|staff|official|discord team).*");
        
        // Check for reward promises
        boolean hasRewards = content.matches(".*(free|gift|prize|win|reward|nitro).*");
        
        return (hasUrgency && hasAuthority) || (hasUrgency && hasRewards);
    }
    
    private RiskLevel determineRiskLevel(double riskScore) {
        if (riskScore >= 0.8) return RiskLevel.CRITICAL;
        if (riskScore >= 0.6) return RiskLevel.HIGH;
        if (riskScore >= 0.4) return RiskLevel.MEDIUM;
        if (riskScore >= 0.2) return RiskLevel.LOW;
        return RiskLevel.MINIMAL;
    }
    
    private String normalizeContent(String content) {
        return content.toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim();
    }
    
    /**
     * Get analytics metrics and statistics
     */
    public BehavioralAnalyticsMetrics getMetrics() {
        return new BehavioralAnalyticsMetrics();
    }
    
    /**
     * Get behavior metrics for a specific user
     */
    public UserBehaviorMetrics getBehaviorMetrics(String userId) {
        return behaviorMetrics.get(userId);
    }
    
    /**
     * Update behavior profile for a user with new activity
     */
    public void updateBehaviorProfile(String userId, String guildId, UserActivity activity) {
        UserBehaviorProfile profile = getUserProfile(userId, guildId);
        updateUserProfile(profile, activity);
    }
    
    private double calculateAverageRiskScore() {
        return riskAssessments.asMap().values().stream()
            .mapToDouble(RiskAssessment::getRiskScore)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Clean up old data and optimize performance
     */
    public void performMaintenance() {
        logger.info("Performing behavioral analytics maintenance");
        
        // Clean up old patterns
        Instant cutoff = Instant.now().minus(Duration.ofDays(config.getDataRetentionDays()));
        detectedPatterns.entrySet().removeIf(entry -> 
            entry.getValue().stream().allMatch(pattern -> 
                pattern.getDetectedAt().isBefore(cutoff)));
        
        // Clean up old metrics
        behaviorMetrics.entrySet().removeIf(entry -> 
            entry.getValue().getLastActivity().isBefore(cutoff));
        
        // Force cache cleanup
        userProfiles.cleanUp();
        recentActivities.cleanUp();
        riskAssessments.cleanUp();
        
        logger.info("Behavioral analytics maintenance completed");
    }
}

// Supporting classes and enums

// Configuration class
class BehavioralAnalyticsConfig {
    private int maxCachedProfiles = 10000;
    private int maxCachedActivities = 50000;
    private int maxCachedAssessments = 10000;
    private int profileCacheHours = 24;
    private int activityCacheMinutes = 60;
    private int assessmentCacheMinutes = 30;
    private int newAccountThresholdHours = 168; // 1 week
    private double highActivityThreshold = 10.0; // messages per minute
    private int minCoordinatedUsers = 3;
    private int coordinatedAnalysisWindowMinutes = 5;
    private int dataRetentionDays = 30;
    
    // Pattern detector configurations
    private SpamDetectionConfig spamConfig = new SpamDetectionConfig();
    private ToxicityDetectionConfig toxicityConfig = new ToxicityDetectionConfig();
    private SuspiciousActivityConfig suspiciousConfig = new SuspiciousActivityConfig();
    private RaidDetectionConfig raidConfig = new RaidDetectionConfig();

// Placeholder configuration classes
    
    // Getters and setters
    public int getMaxCachedProfiles() { return maxCachedProfiles; }
    public int getMaxCachedActivities() { return maxCachedActivities; }
    public int getMaxCachedAssessments() { return maxCachedAssessments; }
    public int getProfileCacheHours() { return profileCacheHours; }
    public int getActivityCacheMinutes() { return activityCacheMinutes; }
    public int getAssessmentCacheMinutes() { return assessmentCacheMinutes; }
    public int getNewAccountThresholdHours() { return newAccountThresholdHours; }
    public double getHighActivityThreshold() { return highActivityThreshold; }
    public int getMinCoordinatedUsers() { return minCoordinatedUsers; }
    public int getCoordinatedAnalysisWindowMinutes() { return coordinatedAnalysisWindowMinutes; }
    public int getDataRetentionDays() { return dataRetentionDays; }
    public SpamDetectionConfig getSpamConfig() { return spamConfig; }
    public ToxicityDetectionConfig getToxicityConfig() { return toxicityConfig; }
    public SuspiciousActivityConfig getSuspiciousConfig() { return suspiciousConfig; }
    public RaidDetectionConfig getRaidConfig() { return raidConfig; }
}

// Placeholder configuration classes

class ToxicityDetectionConfig {
    private double toxicityThreshold = 0.7;
    private int consecutiveToxicThreshold = 2;
    private double escalationThreshold = 0.3;
    
    public double getToxicityThreshold() { return toxicityThreshold; }
    public int getConsecutiveToxicThreshold() { return consecutiveToxicThreshold; }
    public double getEscalationThreshold() { return escalationThreshold; }
}

class SuspiciousActivityConfig {
    private int rapidActionThreshold = 10;
    private Duration rapidActionWindow = Duration.ofMinutes(1);
    private double suspiciousPatternThreshold = 0.6;
    
    public int getRapidActionThreshold() { return rapidActionThreshold; }
    public Duration getRapidActionWindow() { return rapidActionWindow; }
    public double getSuspiciousPatternThreshold() { return suspiciousPatternThreshold; }
}

class RaidDetectionConfig {
    private int minRaidUsers = 5;
    private Duration raidTimeWindow = Duration.ofMinutes(2);
    private double raidSimilarityThreshold = 0.7;
    
    public int getMinRaidUsers() { return minRaidUsers; }
    public Duration getRaidTimeWindow() { return raidTimeWindow; }
    public double getRaidSimilarityThreshold() { return raidSimilarityThreshold; }
}