package com.axion.bot.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Smart rules engine that provides intelligent rule evaluation and management
 * for the moderation system with machine learning capabilities.
 */
public class SmartRulesEngine {
    private static final Logger logger = LoggerFactory.getLogger(SmartRulesEngine.class);
    
    private final Map<String, ModerationRule> rules = new ConcurrentHashMap<>();
    private final Map<String, Double> ruleEffectiveness = new ConcurrentHashMap<>();
    private final AtomicLong totalEvaluations = new AtomicLong(0);
    private final AtomicLong successfulDetections = new AtomicLong(0);
    
    // Rule evaluation engines
    private final ContentRuleEngine contentEngine;
    private final BehaviorRuleEngine behaviorEngine;
    private final ContextRuleEngine contextEngine;
    
    public SmartRulesEngine(ContentRuleEngine contentEngine, 
                           BehaviorRuleEngine behaviorEngine,
                           ContextRuleEngine contextEngine) {
        this.contentEngine = contentEngine;
        this.behaviorEngine = behaviorEngine;
        this.contextEngine = contextEngine;
        initializeDefaultRules();
    }
    
    /**
     * Evaluates content against all active rules
     */
    public RuleEvaluationResult evaluateRules(String content, UserContext userContext, GuildContext guildContext) {
        totalEvaluations.incrementAndGet();
        
        List<String> violatedRules = new ArrayList<>();
        Map<String, Double> ruleScores = new HashMap<>();
        double overallScore = 0.0;
        ModerationSeverity maxSeverity = ModerationSeverity.LOW;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create moderation context
            ModerationContext context = ModerationContext.builder()
                 .content(content)
                 .userId(userContext.getUser().getId())
                 .guildId(guildContext.getGuild().getId())
                 .channelId("default_channel")
                 .build();
            
            // Evaluate each rule
            for (ModerationRule rule : getActiveRules()) {
                try {
                    RuleEvaluationResult ruleResult = evaluateRule(rule, context);
                    
                    if (ruleResult.isMatch()) {
                        violatedRules.add(rule.getName());
                        double score = ruleResult.getConfidence() * rule.getWeight();
                        ruleScores.put(rule.getName(), score);
                        overallScore += score;
                        
                        if (rule.getSeverity().ordinal() > maxSeverity.ordinal()) {
                            maxSeverity = rule.getSeverity();
                        }
                        
                        // Add rule-specific recommendations if needed
                        updateRuleEffectiveness(rule.getId(), true);
                        updateRuleEffectiveness(rule.getId(), false);
                    }
                } catch (Exception e) {
                    logger.warn("Error evaluating rule {}: {}", rule.getName(), e.getMessage());
                }
            }
            
            // Normalize overall score
            if (!violatedRules.isEmpty()) {
                overallScore = Math.min(overallScore / violatedRules.size(), 1.0);
                successfulDetections.incrementAndGet();
            }
            
            long evaluationTime = System.currentTimeMillis() - startTime;
            
            if (violatedRules.isEmpty()) {
                return RuleEvaluationResult.noMatch();
            } else {
                // Convert violated rules to RuleMatch objects
                List<RuleMatch> ruleMatches = new ArrayList<>();
                for (String ruleName : violatedRules) {
                    // Find the rule by name and create a RuleMatch
                    ModerationRule rule = rules.values().stream()
                        .filter(r -> r.getName().equals(ruleName))
                        .findFirst()
                        .orElse(null);
                    if (rule != null) {
                        List<String> reasons = List.of("Rule violated: " + ruleName);
                        double confidence = ruleScores.getOrDefault(ruleName, 0.0);
                        ruleMatches.add(new RuleMatch(rule, reasons, confidence));
                    }
                }
                return new RuleEvaluationResult(true, ruleMatches, overallScore, evaluationTime);
            }
            
        } catch (Exception e) {
            logger.error("Error during rule evaluation", e);
            return RuleEvaluationResult.error("System error during evaluation");
        }
    }
    
    /**
     * Evaluates a single rule against the context
     */
    private RuleEvaluationResult evaluateRule(ModerationRule rule, ModerationContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Determine rule type from conditions
            String ruleType = determineRuleType(rule);
            ConditionResult result;
            
            switch (ruleType) {
                case "content":
                    result = contentEngine.evaluateContent(context.getContent(), context);
                    break;
                case "behavior":
                    result = behaviorEngine.evaluateBehavior(context);
                    break;
                case "context":
                    result = contextEngine.evaluateContext(context);
                    break;
                default:
                    return RuleEvaluationResult.error("Unknown rule type: " + ruleType);
            }
            
            if (result.isMatch()) {
                return RuleEvaluationResult.match(result.getConfidence(), System.currentTimeMillis() - startTime);
            } else {
                return RuleEvaluationResult.noMatch();
            }
            
        } catch (Exception e) {
            return RuleEvaluationResult.error("Error evaluating rule: " + e.getMessage());
        }
    }
    private String determineRuleType(ModerationRule rule) {
        // Determine rule type based on the first condition's type
        if (!rule.getConditions().isEmpty()) {
            return rule.getConditions().get(0).getType();
        }
        return "content"; // default fallback
    }
    
    /**
     * Adds a new rule to the engine
     */
    public void addRule(ModerationRule rule) {
        rules.put(rule.getId(), rule);
        ruleEffectiveness.put(rule.getId(), 0.5); // Start with neutral effectiveness
        logger.info("Added rule: {} ({})", rule.getName(), rule.getId());
    }
    
    /**
     * Removes a rule from the engine
     */
    public void removeRule(String ruleId) {
        ModerationRule removed = rules.remove(ruleId);
        ruleEffectiveness.remove(ruleId);
        if (removed != null) {
            logger.info("Removed rule: {} ({})", removed.getName(), ruleId);
        }
    }
    
    /**
     * Gets all active rules sorted by priority
     */
    public List<ModerationRule> getActiveRules() {
        return rules.values().stream()
            .filter(ModerationRule::isEnabled)
            .sorted((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()))
            .collect(Collectors.toList());
    }
    
    /**
     * Updates rule effectiveness based on feedback
     */
    private void updateRuleEffectiveness(String ruleId, boolean wasEffective) {
        ruleEffectiveness.compute(ruleId, (id, current) -> {
            if (current == null) current = 0.5;
            // Simple learning algorithm - adjust effectiveness based on feedback
            return wasEffective ? 
                Math.min(current + 0.01, 1.0) : 
                Math.max(current - 0.01, 0.0);
        });
    }
    
    /**
     * Gets rule effectiveness score
     */
    public double getRuleEffectiveness(String ruleId) {
        return ruleEffectiveness.getOrDefault(ruleId, 0.5);
    }
    
    /**
     * Gets engine statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvaluations", totalEvaluations.get());
        stats.put("successfulDetections", successfulDetections.get());
        stats.put("activeRules", rules.size());
        stats.put("detectionRate", 
            totalEvaluations.get() > 0 ? 
                (double) successfulDetections.get() / totalEvaluations.get() : 0.0);
        return stats;
    }
    
    /**
     * Initializes default rules
     */
    private void initializeDefaultRules() {
        // Add basic content rules
        addRule(createSpamDetectionRule());
        addRule(createToxicityRule());
        addRule(createLinkSpamRule());
    }
    
    private ModerationRule createSpamDetectionRule() {
        return ModerationRule.builder("spam_detection")
            .name("Spam Detection")
            .description("Detects spam messages")
            .severity(ModerationSeverity.MEDIUM)
            .priority(100)
            .weight(0.8)
            .enabled(true)
            .addCondition(new RuleCondition("behavior", ComparisonOperator.GREATER_THAN, 0.5, 1.0))
            .build();
    }
    
    private ModerationRule createToxicityRule() {
        return ModerationRule.builder("toxicity_detection")
            .name("Toxicity Detection")
            .description("Detects toxic content")
            .severity(ModerationSeverity.HIGH)
            .priority(200)
            .weight(1.0)
            .enabled(true)
            .addCondition(new RuleCondition("content", ComparisonOperator.GREATER_THAN, 0.7, 1.0))
            .build();
    }
    
    private ModerationRule createLinkSpamRule() {
        return ModerationRule.builder("link_spam")
            .name("Link Spam Detection")
            .description("Detects excessive links")
            .severity(ModerationSeverity.MEDIUM)
            .priority(150)
            .weight(0.7)
            .enabled(true)
            .addCondition(new RuleCondition("content", ComparisonOperator.GREATER_THAN, 0.6, 1.0))
            .build();
    }
}