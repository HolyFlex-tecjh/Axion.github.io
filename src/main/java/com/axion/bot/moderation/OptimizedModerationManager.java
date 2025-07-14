package com.axion.bot.moderation;

import com.axion.bot.utils.OptimizedAsyncProcessor;
import com.axion.bot.utils.OptimizedTranslationManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Optimized Moderation Manager with enhanced performance and AI-powered features
 * Integrates with the optimization framework for superior performance
 */
public class OptimizedModerationManager {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedModerationManager.class);
    
    // Core dependencies
    private final OptimizedAsyncProcessor asyncProcessor;
    // private final OptimizedTranslationManager translationManager; // Removed unused field
    
    // Enhanced caching system
    private final Cache<String, UserModerationProfile> userProfileCache;
    private final Cache<String, ModerationConfig> guildConfigCache;
    private final Cache<String, List<ModerationResult>> recentModerationCache;
    private final Cache<String, ThreatIntelligenceData> threatIntelCache;
    
    // Advanced detection systems
    private final AIContentAnalyzer aiContentAnalyzer;
    private final BehavioralAnalytics behavioralAnalytics;
    private final SmartRulesEngine smartRulesEngine;
    private final ThreatIntelligenceSystem threatIntelSystem;
    
    // Performance tracking
    private final Map<String, Long> processingTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> detectionCounts = new ConcurrentHashMap<>();
    
    // Enhanced pattern matching
    private final List<EnhancedPattern> toxicityPatterns = new ArrayList<>();
    private final List<EnhancedPattern> spamPatterns = new ArrayList<>();
    private final List<EnhancedPattern> threatPatterns = new ArrayList<>();
    
    public OptimizedModerationManager(OptimizedAsyncProcessor asyncProcessor,
                                    OptimizedTranslationManager translationManager) {
        this.asyncProcessor = asyncProcessor;
        // this.translationManager = translationManager; // Removed unused assignment
        // Initialize caching system
        this.userProfileCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build();
        this.guildConfigCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofHours(1))
            .recordStats()
            .build();
            
        this.recentModerationCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats()
            .build();
            
        this.threatIntelCache = Caffeine.newBuilder()
            .maximumSize(50000)
            .expireAfterWrite(Duration.ofHours(6))
            .recordStats()
            .build();
        
        // Initialize AI systems
        this.aiContentAnalyzer = new AIContentAnalyzer();
        this.behavioralAnalytics = new BehavioralAnalytics();
        
        // Initialize rule engines with proper dependencies
        ContentRuleConfig contentConfig = new ContentRuleConfig();
        BehaviorRuleConfig behaviorConfig = new BehaviorRuleConfig();
        ContextRuleConfig contextConfig = new ContextRuleConfig();
        ContentRuleEngine contentRuleEngine = new ContentRuleEngine(contentConfig);
        BehaviorRuleEngine behaviorRuleEngine = new BehaviorRuleEngine(behaviorConfig);
        ContextRuleEngine contextRuleEngine = new ContextRuleEngine(contextConfig);
        this.smartRulesEngine = new SmartRulesEngine(contentRuleEngine, behaviorRuleEngine, contextRuleEngine);
        
        // Initialize threat intelligence with config
        ThreatIntelligenceSystem.ThreatIntelligenceConfig threatConfig = new ThreatIntelligenceSystem.ThreatIntelligenceConfig();
        this.threatIntelSystem = new ThreatIntelligenceSystem(threatConfig);
        
        // Initialize raid protection with config
        // Initialize raid protection with config
        // AdvancedRaidProtection.RaidProtectionConfig raidConfig = new AdvancedRaidProtection.RaidProtectionConfig();
        // this.raidProtection = new AdvancedRaidProtection(raidConfig);
        
        initializeEnhancedPatterns();
    }
    
    /**
     * Main asynchronous moderation processing method
     */
    public CompletableFuture<ModerationResult> moderateMessageAsync(MessageReceivedEvent event) {
        long startTime = System.currentTimeMillis();
        
        return asyncProcessor.supplyAsync(() -> {
            try {
                User author = event.getAuthor();
                String content = event.getMessage().getContentRaw();
                Member member = event.getMember();
                
                // Skip bots and privileged users
                if (author.isBot() || hasModeratorBypass(member)) {
                    return ModerationResult.allowed();
                }
                
                String userId = author.getId();
                String guildId = event.getGuild().getId();
                
                // Get cached user profile and config
                UserModerationProfile profile = getUserProfileCached(userId, guildId);
                ModerationConfig config = getGuildConfigCached(guildId);
                
                // Update user activity asynchronously
                asyncProcessor.executeAsync(() -> {
                    profile.recordActivity(content, event.getChannel().getId());
                });
                
                // Multi-layer analysis pipeline
                return processContentThroughPipeline(content, profile, config, event);
                
            } finally {
                long processingTime = System.currentTimeMillis() - startTime;
                processingTimes.put("moderation_total", processingTime);
                
                if (processingTime > 100) {
                    logger.warn("Slow moderation processing: {}ms", processingTime);
                }
            }
        });
    }
    
    /**
     * Enhanced content processing pipeline with AI integration
     */
    private ModerationResult processContentThroughPipeline(String content, 
                                                          UserModerationProfile profile,
                                                          ModerationConfig config,
                                                          MessageReceivedEvent event) {
        
        // Extract needed variables from event
        User author = event.getAuthor();
        String guildId = event.getGuild().getId();
        
        StringBuilder analysisLog = new StringBuilder(); // Use regular StringBuilder instead of pooled one
        try {
            List<ModerationResult> detectionResults = new ArrayList<>();
            
            // 1. AI-Enhanced Content Analysis
            if (config.isAiDetectionEnabled()) {
                long aiStart = System.currentTimeMillis();
                // Create conversation context with proper parameters
                List<String> recentMessages = new ArrayList<>(); // Could be populated from message history
                UserBehaviorProfile behaviorProfile = convertToUserBehaviorProfile(profile);
                boolean hasRecentViolations = profile.getTotalViolations() > 0;
                boolean isInSensitiveChannel = false; // Could be determined from channel settings
                
                ConversationContext conversationContext = new ConversationContext(
                    author.getId(),
                    guildId,
                    event.getChannel().getId(),
                    recentMessages,
                    behaviorProfile,
                    hasRecentViolations,
                    isInSensitiveChannel
                );
                
                ContentAnalysisResult aiResult = aiContentAnalyzer.analyzeContent(content, conversationContext);
                
                if (!aiResult.isClean()) {
                    ModerationResult aiModerationResult = convertAIResultToModerationResult(aiResult);
                    detectionResults.add(aiModerationResult);
                    analysisLog.append("AI Detection: ").append(aiResult.getConfidence()).append("; ");
                }
                
                processingTimes.put("ai_analysis", System.currentTimeMillis() - aiStart);
            }
            
            // 2. Behavioral Analytics
            if (config.isBehavioralAnalysisEnabled()) {
                long behaviorStart = System.currentTimeMillis();
                BehaviorAnalysisResult behaviorResult = behavioralAnalytics.analyzeBehavior(profile, 
                    getRecentUserActivity(profile.getUserId()));
                
                if (behaviorResult.isAnomalous()) {
                    ModerationResult behaviorModerationResult = convertBehaviorResultToModerationResult(behaviorResult);
                    detectionResults.add(behaviorModerationResult);
                    analysisLog.append("Behavior Anomaly: ").append(behaviorResult.getAnomalyScore()).append("; ");
                }
                
                processingTimes.put("behavior_analysis", System.currentTimeMillis() - behaviorStart);
            }
            
            // 3. Smart Rules Engine
            if (config.isSmartRulesEnabled()) {
                long rulesStart = System.currentTimeMillis();
                RuleEvaluationResult rulesResult = smartRulesEngine.evaluateRules(content, 
                    new UserContext(author, profile), new GuildContext(event.getGuild()));
                
                if (rulesResult.isMatch()) {
                    ModerationResult rulesModerationResult = convertRulesResultToModerationResult(rulesResult);
                    detectionResults.add(rulesModerationResult);
                    analysisLog.append("Rules Violation: ").append(rulesResult.getViolatedRules().size()).append("; ");
                }
                
                processingTimes.put("rules_evaluation", System.currentTimeMillis() - rulesStart);
            }
            
            // 4. Threat Intelligence
            if (config.isThreatIntelEnabled()) {
                long threatStart = System.currentTimeMillis();
                ThreatAssessment threatResult = threatIntelSystem.analyzeContent(content, 
                    new UserContext(event.getAuthor(), profile), new GuildContext(event.getGuild()));
                
                if (threatResult.getThreatLevel() != ThreatLevel.NONE) {
                    ModerationResult threatModerationResult = convertThreatResultToModerationResult(threatResult);
                    detectionResults.add(threatModerationResult);
                    analysisLog.append("Threat Detected: ").append(threatResult.getThreatLevel()).append("; ");
                }
                
                processingTimes.put("threat_analysis", System.currentTimeMillis() - threatStart);
            }
            
            // 5. Enhanced Spam Detection
            if (config.isAdvancedSpamDetectionEnabled()) {
                long spamStart = System.currentTimeMillis();
                ModerationResult spamResult = checkEnhancedSpam(content, profile, event);
                
                if (!spamResult.isAllowed()) {
                    detectionResults.add(spamResult);
                    analysisLog.append("Spam Detected; ");
                }
                
                processingTimes.put("spam_detection", System.currentTimeMillis() - spamStart);
            }
            
            // Process and consolidate results
            if (!detectionResults.isEmpty()) {
                ModerationResult finalResult = consolidateDetectionResults(detectionResults, profile, config);
                
                // Log the moderation action asynchronously
                asyncProcessor.supplyDatabaseAsync(() -> {
                    logModerationAction(profile.getUserId(), event, finalResult, analysisLog.toString());
                    return null;
                });
                
                return finalResult;
            }
            
            // Update positive behavior score
            profile.incrementGoodBehavior();
            return ModerationResult.allowed();
            
        } finally {
            // No need to return StringBuilder since we're using regular StringBuilder
        }
    }
    
    /**
     * Enhanced spam detection with machine learning
     */
    private ModerationResult checkEnhancedSpam(String content, UserModerationProfile profile, MessageReceivedEvent event) {
        // Check message frequency with sliding window
        if (isMessageFrequencySpam(profile)) {
            return escalateBasedOnProfile(profile, "High message frequency detected", ModerationSeverity.MEDIUM);
        }
        
        // Check for duplicate content with fuzzy matching
        if (isDuplicateContentSpam(content, profile)) {
            return escalateBasedOnProfile(profile, "Duplicate content spam detected", ModerationSeverity.MEDIUM);
        }
        
        // Check enhanced spam patterns
        for (EnhancedPattern pattern : spamPatterns) {
            if (pattern.matches(content, new HashMap<>())) { // Use empty context for now
                return escalateBasedOnProfile(profile, "Spam pattern detected: " + pattern.getName(), 
                    pattern.getSeverity());
            }
        }
        
        // Check for coordinated spam
        if (isCoordinatedSpam(content, event)) {
            return escalateBasedOnProfile(profile, "Coordinated spam detected", ModerationSeverity.HIGH);
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Intelligent result consolidation
     */
    private ModerationResult consolidateDetectionResults(List<ModerationResult> results, 
                                                        UserModerationProfile profile,
                                                        ModerationConfig config) {
        if (results.isEmpty()) {
            return ModerationResult.allowed();
        }
        
        // Calculate weighted severity score
        double totalSeverityScore = 0.0;
        StringBuilder consolidatedReason = new StringBuilder();
        
        try {
            for (ModerationResult result : results) {
                double weight = getDetectionWeight(result.getDetectionType());
                totalSeverityScore += result.getSeverity() * weight;
                
                if (consolidatedReason.length() > 0) {
                    consolidatedReason.append("; ");
                }
                consolidatedReason.append(result.getReason());
            }
            
            // Adjust based on user profile
            totalSeverityScore = adjustSeverityForProfile(totalSeverityScore, profile);
            
            // Determine final action
            ModerationAction finalAction = determineFinalAction(totalSeverityScore, profile, config);
            ModerationSeverity finalSeverity = ModerationSeverity.fromNumericValue(totalSeverityScore);
            
            return ModerationResult.custom(false, consolidatedReason.toString(), finalAction, finalSeverity.getLevel());
            
        } finally {
            // No need to return StringBuilder since we're using regular StringBuilder
        }
    }
    
    /**
     * Profile-based escalation with machine learning insights
     */
    private ModerationResult escalateBasedOnProfile(UserModerationProfile profile, String reason, ModerationSeverity baseSeverity) {
        // Get user's risk level and history
        double riskMultiplier = profile.getRiskLevel();
        int recentViolations = (int) profile.getRecentViolationCount();
        
        // Calculate escalated severity
        double escalatedSeverity = baseSeverity.getLevel() * (1.0 + riskMultiplier);
        
        // Determine action based on escalated severity and history
        ModerationAction action;
        if (escalatedSeverity >= 8.0 || recentViolations >= 5) {
            action = ModerationAction.BAN;
        } else if (escalatedSeverity >= 6.0 || recentViolations >= 3) {
            action = ModerationAction.DELETE_AND_TIMEOUT;
        } else if (escalatedSeverity >= 4.0 || recentViolations >= 1) {
            action = ModerationAction.DELETE_MESSAGE;
        } else {
            action = ModerationAction.DELETE_AND_WARN;
        }
        
        // Update profile
        profile.addViolation(reason);
        
        return ModerationResult.custom(false, reason, action, (int) Math.round(escalatedSeverity));
    }
    
    /**
     * Cached user profile retrieval
     */
    private UserModerationProfile getUserProfileCached(String userId, String guildId) {
        String cacheKey = userId + ":" + guildId;
        return userProfileCache.get(cacheKey, key -> {
            return asyncProcessor.supplyDatabaseAsync(() -> {
                // Create a basic UserModerationProfile since we don't have a database method yet
                return new UserModerationProfile(userId, guildId);
            }).join();
        });
    }
    
    /**
     * Cached guild configuration retrieval
     */
    private ModerationConfig getGuildConfigCached(String guildId) {
        return guildConfigCache.get(guildId, key -> {
            return asyncProcessor.supplyDatabaseAsync(() -> {
                // Create a default ModerationConfig since we don't have a database method yet
                return new ModerationConfig();
            }).join();
        });
    }
    
    /**
     * Initialize enhanced pattern matching
     */
    private void initializeEnhancedPatterns() {
        // Enhanced toxicity patterns with context awareness
        toxicityPatterns.add(new EnhancedPattern("hate_speech", 
            Pattern.compile("\\b(hate|kill|die)\\s+(you|them|all)\\b", Pattern.CASE_INSENSITIVE),
            ModerationSeverity.HIGH, true));
            
        // Enhanced spam patterns with behavioral context
        spamPatterns.add(new EnhancedPattern("repetitive_content",
            Pattern.compile("(.)\\1{5,}", Pattern.CASE_INSENSITIVE),
            ModerationSeverity.MEDIUM, false));
            
        // Enhanced threat patterns with intelligence integration
        threatPatterns.add(new EnhancedPattern("phishing_attempt",
            Pattern.compile("\\b(free\\s+nitro|discord\\s+gift|click\\s+here)\\b", Pattern.CASE_INSENSITIVE),
            ModerationSeverity.HIGH, true));
    }
    
    // Helper methods for detection logic
    private boolean hasModeratorBypass(Member member) {
        return member != null && (member.hasPermission(Permission.ADMINISTRATOR) || 
                                member.hasPermission(Permission.MANAGE_SERVER));
    }
    
    private boolean isMessageFrequencySpam(UserModerationProfile profile) {
        return profile.getRecentMessageRate() > 10.0; // 10 messages per hour threshold
    }
    
    private boolean isDuplicateContentSpam(String content, UserModerationProfile profile) {
        // Get recent messages from user activity instead
        List<String> recentMessages = new ArrayList<>(); // TODO: Implement getRecentMessages
        return recentMessages.stream()
            .filter(msg -> calculateSimilarity(content, msg) > 0.8)
            .count() >= 3;
    }
    
    private boolean isCoordinatedSpam(String content, MessageReceivedEvent event) {
        // Check for coordinated spam patterns across multiple users
        String channelId = event.getChannel().getId();
        List<ModerationResult> recentResults = recentModerationCache.getIfPresent(channelId);
        
        if (recentResults != null) {
            return recentResults.stream()
                .anyMatch(result -> result.getReason().contains("spam"));
        }
        
        return false;
    }
    
    private double calculateSimilarity(String text1, String text2) {
        // Simple Levenshtein distance-based similarity
        int maxLength = Math.max(text1.length(), text2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = levenshteinDistance(text1, text2);
        return 1.0 - (double) distance / maxLength;
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private List<UserActivity> getRecentUserActivity(String userId) {
        // For now, return an empty list since we don't have the database method implemented
        // TODO: Implement getRecentUserActivity in OptimizedDatabaseManager
        return new ArrayList<>();
    }
    
    
    private UserBehaviorProfile convertToUserBehaviorProfile(UserModerationProfile moderationProfile) {
        // Create a UserBehaviorProfile from UserModerationProfile
        // This is a simplified conversion - in a real implementation, you'd map more fields
        UserBehaviorProfile behaviorProfile = new UserBehaviorProfile(
            moderationProfile.getUserId(),
            moderationProfile.getGuildId()
        );
        
        // You could add more mapping here based on available data
        return behaviorProfile;
    }
    
    private void logModerationAction(String userId, MessageReceivedEvent event, ModerationResult result, String analysisLog) {
        // For now, just log to console since we don't have the database method implemented
        // TODO: Implement logModerationAction in OptimizedDatabaseManager
        logger.info("Moderation action: User={}, Action={}, Reason={}, Guild={}", 
                   userId, result.getAction().name(), result.getReason(), event.getGuild().getId());
    }
    
    // Conversion methods for different detection results
    private ModerationResult convertAIResultToModerationResult(ContentAnalysisResult aiResult) {
        ModerationSeverity severity = ModerationSeverity.fromConfidence(aiResult.getConfidence());
        ModerationAction action = determineActionFromSeverity(severity);
        return ModerationResult.custom(false, "AI detected: " + aiResult.getDetectionType(), action, severity.getLevel(), "ai");
    }
    
    private ModerationResult convertBehaviorResultToModerationResult(BehaviorAnalysisResult behaviorResult) {
        ModerationSeverity severity = ModerationSeverity.fromAnomalyScore(behaviorResult.getAnomalyScore());
        ModerationAction action = determineActionFromSeverity(severity);
        return ModerationResult.custom(false, "Behavioral anomaly: " + behaviorResult.getAnomalyType(), action, severity.getLevel(), "behavior");
    }
    
    private ModerationResult convertRulesResultToModerationResult(RuleEvaluationResult rulesResult) {
        ModerationSeverity severity = rulesResult.getHighestSeverity();
        ModerationAction action = determineActionFromSeverity(severity);
        return ModerationResult.custom(false, "Rule violation: " + rulesResult.getViolatedRules(), action, severity.getLevel(), "rules");
    }
    
    private ModerationResult convertThreatResultToModerationResult(ThreatAssessment threatResult) {
        ModerationSeverity severity = ModerationSeverity.fromThreatLevel(threatResult.getThreatLevel());
        ModerationAction action = determineActionFromSeverity(severity);
        return ModerationResult.custom(false, "Threat detected: " + threatResult.getThreatType(), action, severity.getLevel(), "threat");
    }
    
    private ModerationAction determineActionFromSeverity(ModerationSeverity severity) {
        switch (severity) {
            case LOW:
                return ModerationAction.WARN_USER;
            case MEDIUM:
                return ModerationAction.DELETE_MESSAGE;
            case HIGH:
                return ModerationAction.DELETE_AND_TIMEOUT;
            case VERY_HIGH:
                return ModerationAction.BAN;
            default:
                return ModerationAction.WARN_USER;
        }
    }
    
    private double getDetectionWeight(String detectionType) {
        switch (detectionType.toLowerCase()) {
            case "ai":
                return 1.2;
            case "threat":
                return 1.5;
            case "behavior":
                return 1.0;
            case "rules":
                return 1.1;
            default:
                return 1.0;
        }
    }
    
    private double adjustSeverityForProfile(double baseSeverity, UserModerationProfile profile) {
        double adjustment = 1.0;
        
        // Adjust based on trust level
        if (profile.getTrustScore() < 30) {
            adjustment += 0.3;
        } else if (profile.getTrustScore() > 80) {
            adjustment -= 0.2;
        }
        
        // Adjust based on recent violations
        long recentViolations = profile.getRecentViolationCount();
        adjustment += recentViolations * 0.1;
        
        return baseSeverity * Math.max(0.5, Math.min(2.0, adjustment));
    }
    
    private ModerationAction determineFinalAction(double severityScore, UserModerationProfile profile, ModerationConfig config) {
        // Consider user history and configuration
        if (severityScore >= 8.0 || profile.getRecentViolationCount() >= config.getMaxWarningsBeforeBan()) {
            return ModerationAction.BAN;
        } else if (severityScore >= 6.0) {
            return ModerationAction.DELETE_AND_TIMEOUT;
        } else if (severityScore >= 4.0) {
            return ModerationAction.DELETE_MESSAGE;
        } else {
            return ModerationAction.WARN_USER;
        }
    }
    
    /**
     * Get performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("processing_times", new HashMap<>(processingTimes));
        metrics.put("detection_counts", new HashMap<>(detectionCounts));
        metrics.put("cache_stats", Map.of(
            "user_profiles", userProfileCache.stats(),
            "guild_configs", guildConfigCache.stats(),
            "recent_moderation", recentModerationCache.stats(),
            "threat_intel", threatIntelCache.stats()
        ));
        return metrics;
    }
    
    /**
     * Cleanup resources
     */
    public void shutdown() {
        userProfileCache.invalidateAll();
        guildConfigCache.invalidateAll();
        recentModerationCache.invalidateAll();
        threatIntelCache.invalidateAll();
        
        logger.info("OptimizedModerationManager shutdown completed");
    }
}