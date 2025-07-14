package com.axion.bot.moderation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Import for EscalationResult and ConditionType
import com.axion.bot.moderation.EscalationEngine.EscalationResult;
import static com.axion.bot.moderation.ConditionType.*;

/**
 * Smart Auto-Moderation Rules Engine with dynamic rule evaluation and adaptive enforcement
 * Supports complex rule conditions, escalation policies, and context-aware moderation
 */
public class SmartAutoModerationEngine {
    private static final Logger logger = LoggerFactory.getLogger(SmartAutoModerationEngine.class);
    
    // Rule storage and caching
    private final Map<String, ModerationRule> activeRules = new ConcurrentHashMap<>();
    private final Cache<String, RuleEvaluationResult> evaluationCache;
    private final Cache<String, List<ActionRecord>> actionHistory;
    
    // Rule evaluation engines
    private final ContentRuleEngine contentEngine;
    private final BehaviorRuleEngine behaviorEngine;
    private final ContextRuleEngine contextEngine;
    private final EscalationEngine escalationEngine;
    
    // Performance tracking
    private final AtomicLong totalEvaluations = new AtomicLong(0);
    private final AtomicLong rulesTriggered = new AtomicLong(0);
    private final AtomicLong actionsExecuted = new AtomicLong(0);
    
    // Configuration
    private final AutoModerationConfig config;
    
    public SmartAutoModerationEngine(AutoModerationConfig config) {
        this.config = config;
        
        // Initialize caches
        this.evaluationCache = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedEvaluations())
            .expireAfterWrite(Duration.ofMinutes(config.getEvaluationCacheMinutes()))
            .build();
            
        this.actionHistory = Caffeine.newBuilder()
            .maximumSize(config.getMaxCachedActions())
            .expireAfterWrite(Duration.ofHours(config.getActionHistoryHours()))
            .build();
        
        // Initialize rule engines
        this.contentEngine = new ContentRuleEngine(config.getContentConfig());
        this.behaviorEngine = new BehaviorRuleEngine(config.getBehaviorConfig());
        this.contextEngine = new ContextRuleEngine(config.getContextConfig());
        this.escalationEngine = new EscalationEngine(config.getEscalationConfig());
        
        // Load default rules
        loadDefaultRules();
        
        logger.info("SmartAutoModerationEngine initialized with {} active rules", activeRules.size());
    }
    
    /**
     * Main method to evaluate content against all active rules
     */
    public ModerationDecision evaluateContent(ModerationContext context) {
        long startTime = System.currentTimeMillis();
        totalEvaluations.incrementAndGet();
        
        try {
            // Check cache first
            String cacheKey = generateCacheKey(context);
            RuleEvaluationResult cachedResult = evaluationCache.getIfPresent(cacheKey);
            if (cachedResult != null && !cachedResult.isExpired()) {
                return createDecisionFromCachedResult(cachedResult, context);
            }
            
            // Evaluate all applicable rules
            List<RuleMatch> ruleMatches = new ArrayList<>();
            
            for (ModerationRule rule : getApplicableRules(context)) {
                RuleEvaluationResult result = evaluateRule(rule, context);
                if (result.isMatch()) {
                    ruleMatches.add(new RuleMatch(rule, result));
                    rulesTriggered.incrementAndGet();
                }
            }
            
            // Determine final moderation decision
            ModerationDecision decision = determineDecision(ruleMatches, context);
            
            // Cache the evaluation result
            RuleEvaluationResult evaluationResult = new RuleEvaluationResult(
                !ruleMatches.isEmpty(), ruleMatches, decision.getConfidence(), 
                System.currentTimeMillis() - startTime
            );
            evaluationCache.put(cacheKey, evaluationResult);
            
            // Log significant decisions
            if (decision.getAction() != ModerationActionType.NONE) {
                logger.info("Moderation action triggered: {} for user {} in guild {} - Rules: {}", 
                           decision.getAction(), context.getUserId(), context.getGuildId(),
                           ruleMatches.stream().map(m -> m.getRule().getName())
                                     .collect(Collectors.joining(", ")));
                actionsExecuted.incrementAndGet();
            }
            
            return decision;
            
        } catch (Exception e) {
            logger.error("Error evaluating moderation rules for user {} in guild {}", 
                        context.getUserId(), context.getGuildId(), e);
            return ModerationDecision.error("Rule evaluation failed: " + e.getMessage());
        }
    }
    
    /**
     * Add or update a moderation rule
     */
    public void addRule(ModerationRule rule) {
        validateRule(rule);
        activeRules.put(rule.getId(), rule);
        
        // Clear relevant caches
        evaluationCache.invalidateAll();
        
        logger.info("Added/updated moderation rule: {} ({})", rule.getName(), rule.getId());
    }
    
    /**
     * Remove a moderation rule
     */
    public void removeRule(String ruleId) {
        ModerationRule removed = activeRules.remove(ruleId);
        if (removed != null) {
            evaluationCache.invalidateAll();
            logger.info("Removed moderation rule: {} ({})", removed.getName(), ruleId);
        }
    }
    
    /**
     * Get all active rules
     */
    public List<ModerationRule> getActiveRules() {
        return new ArrayList<>(activeRules.values());
    }
    
    /**
     * Get rules applicable to the current context
     */
    private List<ModerationRule> getApplicableRules(ModerationContext context) {
        return activeRules.values().stream()
            .filter(rule -> rule.isEnabled())
            .filter(rule -> isRuleApplicable(rule, context))
            .sorted(Comparator.comparingInt(ModerationRule::getPriority).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a rule is applicable to the current context
     */
    private boolean isRuleApplicable(ModerationRule rule, ModerationContext context) {
        // Check guild restrictions
        if (!rule.getGuildIds().isEmpty() && !rule.getGuildIds().contains(context.getGuildId())) {
            return false;
        }
        
        // Check channel restrictions
        if (!rule.getChannelIds().isEmpty() && !rule.getChannelIds().contains(context.getChannelId())) {
            return false;
        }
        
        // Check role restrictions
        if (!rule.getExcludedRoles().isEmpty()) {
            for (String excludedRole : rule.getExcludedRoles()) {
                if (context.getUserRoles().contains(excludedRole)) {
                    return false;
                }
            }
        }
        
        // Check time restrictions
        if (rule.getTimeRestrictions() != null) {
            LocalTime now = LocalTime.now(ZoneId.of(context.getTimezone()));
            TimeRestriction timeRestriction = rule.getTimeRestrictions();
            if (!timeRestriction.isTimeAllowed(now)) {
                return false;
            }
        }
        
        // Check user exemptions
        if (rule.getExemptUsers().contains(context.getUserId())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Evaluate a single rule against the context
     */
    private RuleEvaluationResult evaluateRule(ModerationRule rule, ModerationContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            boolean matches = false;
            double confidence = 0.0;
            List<String> matchReasons = new ArrayList<>();
            
            // Evaluate rule conditions
            for (RuleCondition condition : rule.getConditions()) {
                ConditionResult result = evaluateCondition(condition, context);
                
                if (rule.getLogicOperator() == LogicOperator.AND) {
                    if (!result.isMatch()) {
                        matches = false;
                        break;
                    }
                    matches = true;
                    confidence = Math.max(confidence, result.getConfidence());
                    matchReasons.addAll(result.getReasons());
                } else { // OR
                    if (result.isMatch()) {
                        matches = true;
                        confidence = Math.max(confidence, result.getConfidence());
                        matchReasons.addAll(result.getReasons());
                    }
                }
            }
            
            // Apply rule weight to confidence
            if (matches) {
                confidence *= rule.getWeight();
            }
            
            // Convert matchReasons to RuleMatch objects
            List<RuleMatch> ruleMatches = new ArrayList<>();
            if (matches) {
                ruleMatches.add(new RuleMatch(rule, matchReasons, confidence));
            }
            
            return new RuleEvaluationResult(matches, ruleMatches, confidence, 
                                          System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            logger.error("Error evaluating rule {}", rule.getName(), e);
            return RuleEvaluationResult.error("Rule evaluation failed: " + e.getMessage());
        }
    }
    
    /**
     * Evaluate a single condition
     */
    private ConditionResult evaluateCondition(RuleCondition condition, ModerationContext context) {
        String conditionType = condition.getType();
        switch (conditionType) {
            case "content":
                return contentEngine.evaluateContent(context.getContent(), context);
            case "behavior":
                return behaviorEngine.evaluateBehavior(context);
            case "context":
                return contextEngine.evaluateContext(context);
            case "user_history":
                return evaluateUserHistoryCondition(condition, context);
            case "frequency":
                return evaluateFrequencyCondition(condition, context);
            case "custom":
                return evaluateCustomCondition(condition, context);
            default:
                return ConditionResult.noMatch("Unknown condition type: " + condition.getType());
        }
    }
    
    /**
     * Determine final moderation decision from rule matches
     */
    private ModerationDecision determineDecision(List<RuleMatch> ruleMatches, ModerationContext context) {
        if (ruleMatches.isEmpty()) {
            return ModerationDecision.noAction("No rules triggered");
        }
        
        // Sort by severity and confidence
        ruleMatches.sort((a, b) -> {
            int severityCompare = Integer.compare(
                b.getRule().getSeverity().ordinal(), 
                a.getRule().getSeverity().ordinal()
            );
            if (severityCompare != 0) return severityCompare;
            
            return Double.compare(b.getResult().getConfidence(), a.getResult().getConfidence());
        });
        
        RuleMatch primaryMatch = ruleMatches.get(0);
        ModerationRule primaryRule = primaryMatch.getRule();
        
        // Check for escalation based on user history
        List<ActionRecord> userHistory = getUserActionHistory(context.getUserId(), context.getGuildId());
        EscalationResult escalation = escalationEngine.evaluateEscalation(context, primaryMatch.getResult().getConfidence());
        
        // Determine action type based on severity
        ModerationActionType actionType = escalation.shouldEscalate() ? 
            getEscalatedAction(primaryRule.getSeverity()) : getActionFromSeverity(primaryRule.getSeverity());
        
        // Calculate final confidence
        double finalConfidence = escalation.getEscalatedConfidence();
        
        // Collect all triggered rule names
        List<String> triggeredRules = ruleMatches.stream()
            .map(match -> match.getRule().getName())
            .collect(Collectors.toList());
        
        // Create moderation decision
        ModerationDecision decision = ModerationDecision.builder(actionType)
            .confidence(finalConfidence)
            .reason(primaryRule.getDescription())
            .triggeredRules(triggeredRules)
            .severity(primaryRule.getSeverity())
            .build();
        
        // Record action in history
        if (actionType != ModerationActionType.NONE) {
            recordModerationAction(context, decision, primaryRule);
        }
        
        return decision;
    }
    
    /**
     * Load default moderation rules
     */
    private void loadDefaultRules() {
        // Spam detection rule
        addRule(createSpamDetectionRule());
        
        // Toxicity detection rule
        addRule(createToxicityDetectionRule());
        
        // Link spam rule
        addRule(createLinkSpamRule());
        
        // Caps spam rule
        addRule(createCapsSpamRule());
        
        // Mention spam rule
        addRule(createMentionSpamRule());
        
        // Raid protection rule
        addRule(createRaidProtectionRule());
        
        // NSFW content rule
        addRule(createNSFWContentRule());
        
        // Phishing protection rule
        addRule(createPhishingProtectionRule());
    }
    
    /**
     * Create default spam detection rule
     */
    private ModerationRule createSpamDetectionRule() {
        return ModerationRule.builder("spam_detection")
            .name("Spam Detection")
            .description("Detects and prevents spam messages")
            .severity(ModerationSeverity.MEDIUM)
            .priority(100)
            .weight(0.8)
            .addCondition(new RuleCondition(
                "frequency",
                ComparisonOperator.GREATER_THAN,
                "5",
                1.0
            ))
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "0.7",
                0.8
            ))
            .build();
    }
    
    /**
     * Create default toxicity detection rule
     */
    private ModerationRule createToxicityDetectionRule() {
        return ModerationRule.builder("toxicity_detection")
            .name("Toxicity Detection")
            .description("Detects toxic and harmful content")
            .severity(ModerationSeverity.HIGH)
            .priority(200)
            .weight(0.9)
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "0.7",
                0.9
            ))
            .build();
    }
    
    /**
     * Create link spam detection rule
     */
    private ModerationRule createLinkSpamRule() {
        return ModerationRule.builder("link_spam")
            .name("Link Spam Detection")
            .description("Prevents excessive link posting")
            .severity(ModerationSeverity.MEDIUM)
            .priority(80)
            .weight(0.7)
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "3",
                1.0
            ))
            .addCondition(new RuleCondition(
                "frequency",
                ComparisonOperator.GREATER_THAN,
                "2.0",
                1.0
            ))
            .logicOperator(LogicOperator.OR)
            .build();
    }
    
    /**
     * Create caps spam detection rule
     */
    private ModerationRule createCapsSpamRule() {
        return ModerationRule.builder("caps_spam")
            .name("Caps Spam Detection")
            .description("Prevents excessive use of capital letters")
            .severity(ModerationSeverity.LOW)
            .priority(50)
            .weight(0.5)
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "70",
                1.0
            ))
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "20",
                1.0
            ))
            .logicOperator(LogicOperator.AND)
            .build();
    }
    
    /**
     * Create mention spam detection rule
     */
    private ModerationRule createMentionSpamRule() {
        return ModerationRule.builder("mention_spam")
            .name("Mention Spam Detection")
            .description("Prevents excessive user mentions")
            .severity(ModerationSeverity.MEDIUM)
            .priority(90)
            .weight(0.8)
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "5",
                1.0
            ))
            .build();
    }
    
    /**
     * Create raid protection rule
     */
    private ModerationRule createRaidProtectionRule() {
        return ModerationRule.builder("raid_protection")
            .name("Raid Protection")
            .description("Detects and prevents raid attacks")
            .severity(ModerationSeverity.VERY_HIGH)
            .priority(300)
            .weight(1.0)
            .addCondition(new RuleCondition(
                "behavior",
                ComparisonOperator.EQUALS,
                "true",
                1.0
            ))
            .build();
    }
    
    /**
     * Create NSFW content detection rule
     */
    private ModerationRule createNSFWContentRule() {
        return ModerationRule.builder("nsfw_content")
            .name("NSFW Content Detection")
            .description("Detects NSFW content in non-NSFW channels")
            .severity(ModerationSeverity.HIGH)
            .priority(150)
            .weight(0.9)
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "0.8",
                1.0
            ))
            .addCondition(new RuleCondition(
                "context",
                ComparisonOperator.EQUALS,
                "false",
                1.0
            ))
            .logicOperator(LogicOperator.AND)
            .build();
    }
    
    /**
     * Create phishing protection rule
     */
    private ModerationRule createPhishingProtectionRule() {
        return ModerationRule.builder("phishing_protection")
            .name("Phishing Protection")
            .description("Detects and blocks phishing attempts")
            .severity(ModerationSeverity.VERY_HIGH)
            .priority(250)
            .weight(1.0)
            .addCondition(new RuleCondition(
                "content",
                ComparisonOperator.GREATER_THAN,
                "0.8",
                1.0
            ))
            .build();
    }
    
    // Helper methods for condition evaluation
    private ConditionResult evaluateUserHistoryCondition(RuleCondition condition, ModerationContext context) {
        List<ActionRecord> history = getUserActionHistory(context.getUserId(), context.getGuildId());
        
        switch (condition.getType()) {
            case "violation_count":
                int violationCount = history.size();
                return compareValue(condition, violationCount);
            case "recent_violations":
                long recentViolations = history.stream()
                    .filter(action -> action.getTimestamp().isAfter(
                        Instant.now().minus(Duration.ofDays(7))))
                    .count();
                return compareValue(condition, recentViolations);
            case "severity_escalation":
                boolean hasEscalation = hasRecentSeverityEscalation(history);
                return compareValue(condition, hasEscalation ? 1 : 0);
            default:
                return ConditionResult.noMatch("Unknown user history field: " + condition.getType());
        }
    }
    
    private ConditionResult evaluateFrequencyCondition(RuleCondition condition, ModerationContext context) {
        // Implementation would depend on frequency tracking system
        // This is a simplified version
        switch (condition.getType()) {
            case "messages_per_minute":
                Object messageRateObj = context.getContextValue("messageFrequency");
                double messageRate = messageRateObj instanceof Number ? ((Number) messageRateObj).doubleValue() : 0.0;
                return compareValue(condition, messageRate);
            case "actions_per_hour":
                Object actionRateObj = context.getContextValue("actionFrequency");
                double actionRate = actionRateObj instanceof Number ? ((Number) actionRateObj).doubleValue() : 0.0;
                return compareValue(condition, actionRate);
            default:
                return ConditionResult.noMatch("Unknown frequency field: " + condition.getType());
        }
    }
    
    private ConditionResult evaluateCustomCondition(RuleCondition condition, ModerationContext context) {
        // Allow for custom condition evaluation
        // This could be extended with a scripting engine or plugin system
        return ConditionResult.noMatch("Custom conditions not implemented");
    }
    
    private ConditionResult compareValue(RuleCondition condition, Object actualValue) {
        try {
            String expectedStr = condition.getValue().toString();
            ComparisonOperator operator = condition.getOperator();
            
            if (actualValue instanceof Number) {
                double actual = ((Number) actualValue).doubleValue();
                double expected = Double.parseDouble(expectedStr);
                
                boolean matches = false;
                switch (operator) {
                    case EQUALS:
                        matches = Math.abs(actual - expected) < 0.001;
                        break;
                    case NOT_EQUALS:
                        matches = Math.abs(actual - expected) >= 0.001;
                        break;
                    case GREATER_THAN:
                        matches = actual > expected;
                        break;
                    case GREATER_THAN_OR_EQUAL:
                        matches = actual >= expected;
                        break;
                    case LESS_THAN:
                        matches = actual < expected;
                        break;
                    case LESS_THAN_OR_EQUAL:
                        matches = actual <= expected;
                        break;
                }
                
                if (matches) {
                    return ConditionResult.match(
                        String.format("%s %s %s", actual, operator, expected),
                        0.8
                    );
                }
            } else if (actualValue instanceof String) {
                String actual = (String) actualValue;
                
                boolean matches = false;
                switch (operator) {
                    case EQUALS:
                        matches = actual.equals(expectedStr);
                        break;
                    case NOT_EQUALS:
                        matches = !actual.equals(expectedStr);
                        break;
                    case CONTAINS:
                        matches = actual.contains(expectedStr);
                        break;
                    case NOT_CONTAINS:
                        matches = !actual.contains(expectedStr);
                        break;
                    case REGEX_MATCH:
                        matches = Pattern.matches(expectedStr, actual);
                        break;
                }
                
                if (matches) {
                    return ConditionResult.match(
                        String.format("String condition matched: %s %s %s", actual, operator, expectedStr),
                        0.8
                    );
                }
            }
            
            return ConditionResult.noMatch("Condition not met");
            
        } catch (Exception e) {
            return ConditionResult.error("Error comparing values: " + e.getMessage());
        }
    }
    
    private List<ActionRecord> getUserActionHistory(String userId, String guildId) {
        String key = userId + ":" + guildId;
        return actionHistory.getIfPresent(key);
    }
    
    private void recordModerationAction(ModerationContext context, ModerationDecision decision, 
                                      ModerationRule rule) {
        String key = context.getUserId() + ":" + context.getGuildId();
        List<ActionRecord> history = actionHistory.getIfPresent(key);
        if (history == null) {
            history = new ArrayList<>();
        }
        
        ActionRecord action = new ActionRecord(
            context.getUserId(),
            context.getGuildId(),
            decision.getAction(),
            rule.getName(),
            decision.getReason(),
            Instant.now()
        );
        
        history.add(action);
        actionHistory.put(key, history);
    }
    
    private boolean hasRecentSeverityEscalation(List<ActionRecord> history) {
        if (history.size() < 2) return false;
        
        // Sort by timestamp
        history.sort(Comparator.comparing(ActionRecord::getTimestamp));
        
        // Check if recent actions show escalating severity
        for (int i = 1; i < history.size(); i++) {
            ActionRecord current = history.get(i);
            ActionRecord previous = history.get(i - 1);
            
            if (current.getAction().ordinal() > previous.getAction().ordinal()) {
                return true;
            }
        }
        
        return false;
    }
    
    private void validateRule(ModerationRule rule) {
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be empty");
        }
        
        if (rule.getConditions().isEmpty()) {
            throw new IllegalArgumentException("Rule must have at least one condition");
        }
    }
    
    /**
     * Map severity to appropriate moderation action
     */
    private ModerationActionType getActionFromSeverity(ModerationSeverity severity) {
        switch (severity) {
            case LOW:
                return ModerationActionType.WARN;
            case MEDIUM:
                return ModerationActionType.DELETE_MESSAGE;
            case HIGH:
                return ModerationActionType.TIMEOUT;
            case VERY_HIGH:
                return ModerationActionType.BAN;
            default:
                return ModerationActionType.NONE;
        }
    }
    
    /**
     * Map severity to escalated moderation action
     */
    private ModerationActionType getEscalatedAction(ModerationSeverity severity) {
        switch (severity) {
            case LOW:
                return ModerationActionType.DELETE_MESSAGE;
            case MEDIUM:
                return ModerationActionType.TIMEOUT;
            case HIGH:
                return ModerationActionType.BAN;
            case VERY_HIGH:
                return ModerationActionType.BAN;
            default:
                return ModerationActionType.WARN;
        }
    }
    
    /**
     * Get appropriate duration for moderation action
     */
    private long getDurationFromAction(ModerationActionType actionType) {
        switch (actionType) {
            case TIMEOUT:
                return 300000; // 5 minutes in milliseconds
            case BAN:
                return 86400000; // 24 hours in milliseconds
            default:
                return 0; // No duration for other actions
        }
    }
    
    private String generateCacheKey(ModerationContext context) {
        return String.format("%s:%s:%s:%d", 
            context.getUserId(), 
            context.getGuildId(), 
            context.getChannelId(),
            context.getContent().hashCode()
        );
    }
    
    private ModerationDecision createDecisionFromCachedResult(RuleEvaluationResult cachedResult, 
                                                            ModerationContext context) {
        if (!cachedResult.isMatch()) {
            return ModerationDecision.noAction("No rules triggered (cached)");
        }
        
        // For cached results, we need to reconstruct the decision
        // This is a simplified version - in practice, you might cache the full decision
        return ModerationDecision.noAction("Cached result processing not fully implemented");
    }
    
    /**
     * Get engine performance metrics
     */
    public AutoModerationMetrics getMetrics() {
        return new AutoModerationMetrics(
            totalEvaluations.get(),
            rulesTriggered.get(),
            actionsExecuted.get(),
            activeRules.size(),
            evaluationCache.estimatedSize(),
            actionHistory.estimatedSize()
        );
    }
    
    /**
     * Perform maintenance and cleanup
     */
    public void performMaintenance() {
        logger.info("Performing auto-moderation engine maintenance");
        
        // Clean up expired cache entries
        evaluationCache.cleanUp();
        actionHistory.cleanUp();
        
        // Clean up old action history
        Instant cutoff = Instant.now().minus(Duration.ofDays(config.getActionHistoryRetentionDays()));
        actionHistory.asMap().values().forEach(history -> 
            history.removeIf(action -> action.getTimestamp().isBefore(cutoff)));
        
        logger.info("Auto-moderation engine maintenance completed");
    }
}

// ActionRecord class for storing moderation action history
 class ActionRecord {
     private final String userId;
     private final String guildId;
     private final ModerationActionType action;
     private final String ruleName;
     private final String reason;
     private final Instant timestamp;
     
     public ActionRecord(String userId, String guildId, ModerationActionType action, 
                        String ruleName, String reason, Instant timestamp) {
         this.userId = userId;
         this.guildId = guildId;
         this.action = action;
         this.ruleName = ruleName;
         this.reason = reason;
         this.timestamp = timestamp;
     }
     
     public String getUserId() { return userId; }
     public String getGuildId() { return guildId; }
     public ModerationActionType getAction() { return action; }
     public String getRuleName() { return ruleName; }
     public String getReason() { return reason; }
     public Instant getTimestamp() { return timestamp; }
 }

// Supporting enums and classes are defined in separate files

// Configuration class
class AutoModerationConfig {
    private int maxCachedEvaluations = 10000;
    private int maxCachedActions = 50000;
    private int evaluationCacheMinutes = 5;
    private int actionHistoryHours = 24;
    private int actionHistoryRetentionDays = 30;
    
    private ContentRuleConfig contentConfig = new ContentRuleConfig();
    private BehaviorRuleConfig behaviorConfig = new BehaviorRuleConfig();
    private ContextRuleConfig contextConfig = new ContextRuleConfig();
    private EscalationConfig escalationConfig = new EscalationConfig();
    
    // Getters
    public int getMaxCachedEvaluations() { return maxCachedEvaluations; }
    public int getMaxCachedActions() { return maxCachedActions; }
    public int getEvaluationCacheMinutes() { return evaluationCacheMinutes; }
    public int getActionHistoryHours() { return actionHistoryHours; }
    public int getActionHistoryRetentionDays() { return actionHistoryRetentionDays; }
    public ContentRuleConfig getContentConfig() { return contentConfig; }
    public BehaviorRuleConfig getBehaviorConfig() { return behaviorConfig; }
    public ContextRuleConfig getContextConfig() { return contextConfig; }
    public EscalationConfig getEscalationConfig() { return escalationConfig; }
}

// Placeholder configuration classes
class ContentRuleConfig {
    private double defaultToxicityThreshold = 0.7;
    private int maxLinksPerMessage = 3;
    private double capsPercentageThreshold = 70.0;
    
    public double getDefaultToxicityThreshold() { return defaultToxicityThreshold; }
    public int getMaxLinksPerMessage() { return maxLinksPerMessage; }
    public double getCapsPercentageThreshold() { return capsPercentageThreshold; }
}

class BehaviorRuleConfig {
    private double spamFrequencyThreshold = 5.0;
    private int duplicateMessageThreshold = 3;
    private Duration behaviorAnalysisWindow = Duration.ofMinutes(5);
    
    public double getSpamFrequencyThreshold() { return spamFrequencyThreshold; }
    public int getDuplicateMessageThreshold() { return duplicateMessageThreshold; }
    public Duration getBehaviorAnalysisWindow() { return behaviorAnalysisWindow; }
}

class ContextRuleConfig {
    private boolean considerChannelType = true;
    private boolean considerUserRoles = true;
    private boolean considerTimeOfDay = false;
    
    public boolean isConsiderChannelType() { return considerChannelType; }
    public boolean isConsiderUserRoles() { return considerUserRoles; }
    public boolean isConsiderTimeOfDay() { return considerTimeOfDay; }
}

class EscalationConfig {
    private boolean enableEscalation = true;
    private int escalationThreshold = 3;
    private Duration escalationWindow = Duration.ofDays(7);
    private double escalationMultiplier = 1.5;
    
    public boolean isEnableEscalation() { return enableEscalation; }
    public int getEscalationThreshold() { return escalationThreshold; }
    public Duration getEscalationWindow() { return escalationWindow; }
    public double getEscalationMultiplier() { return escalationMultiplier; }
}