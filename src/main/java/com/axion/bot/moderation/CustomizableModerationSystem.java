package com.axion.bot.moderation;

// Removed Caffeine dependency - using simple Map instead
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fully Customizable Moderation System
 * Allows complete customization of all moderation aspects through configuration
 */
public class CustomizableModerationSystem {
    
    // Configuration cache
    private final Map<String, CustomGuildModerationConfig> configCache;
    
    // Rule engines
    private final CustomRuleEngine ruleEngine;
    private final ActionEngine actionEngine;
    private final ThresholdEngine thresholdEngine;
    
    // Customization managers
    private final FilterCustomizationManager filterManager;
    
    public CustomizableModerationSystem() {
        this.configCache = new ConcurrentHashMap<>();
            
        this.ruleEngine = new CustomRuleEngine();
        this.actionEngine = new ActionEngine();
        this.thresholdEngine = new ThresholdEngine();
        
        this.filterManager = new FilterCustomizationManager();
    }
    
    /**
     * Get or create guild moderation configuration
     */
    public CustomGuildModerationConfig getGuildConfig(String guildId) {
        return configCache.computeIfAbsent(guildId, k -> createDefaultConfig(guildId));
    }
    
    /**
     * Update guild moderation configuration
     */
    public void updateGuildConfig(String guildId, CustomGuildModerationConfig config) {
        configCache.put(guildId, config);
        // In a real implementation, this would also save to database
    }
    
    /**
     * Process message with customized rules
     */
    public ModerationResult processMessage(String guildId, String userId, String content, Map<String, Object> context) {
        CustomGuildModerationConfig config = getGuildConfig(guildId);
        
        // Apply custom filters
        filterManager.applyFilters(content, config.getFilterConfig(), context);
        
        // Evaluate custom rules
        List<RuleViolation> violations = ruleEngine.evaluateRules(content, userId, config.getCustomRules(), context);
        
        // Calculate thresholds
        ThresholdResult thresholdResult = thresholdEngine.evaluateThresholds(userId, violations, config.getThresholdConfig());
        
        // Determine actions
        actionEngine.determineActions(violations, thresholdResult, config.getActionConfig());
        
        // Determine overall result based on violations and actions
        boolean allowed = violations.isEmpty();
        String reason = violations.isEmpty() ? "No violations detected" : 
            violations.stream().map(v -> v.getRuleId()).collect(java.util.stream.Collectors.joining(", "));
        ModerationAction primaryAction = ModerationAction.NONE;
        
        // Use static factory methods instead of constructor
        if (allowed) {
            return ModerationResult.allowed();
        } else {
            int severity = violations.isEmpty() ? 0 : 
                violations.stream().mapToInt(v -> v.getSeverity()).max().orElse(1);
            if (severity >= 3) {
                return ModerationResult.moderate(reason, primaryAction);
            } else {
                return ModerationResult.warn(reason, primaryAction);
            }
        }
    }
    
    /**
     * Create default configuration for a guild
     */
    private CustomGuildModerationConfig createDefaultConfig(String guildId) {
        return new CustomGuildModerationConfig.Builder(guildId)
            .withFilterConfig(createDefaultFilterConfig())
            .withCustomRules(createDefaultRules())
            .withThresholdConfig(createDefaultThresholdConfig())
            .withActionConfig(createDefaultActionConfig())
            .withUIConfig(createDefaultUIConfig())
            .build();
    }
    
    private FilterConfig createDefaultFilterConfig() {
        return new FilterConfig.Builder()
            .withSpamDetection(new SpamFilterConfig(true, 0.7, Duration.ofMinutes(1), 5))
            .withToxicityDetection(new ToxicityFilterConfig(true, 0.8, Arrays.asList("en", "da")))
            .withLinkProtection(new LinkFilterConfig(true, 3, Arrays.asList("malware.com", "phishing.net")))
            .withCapsFilter(new CapsFilterConfig(true, 0.7, 20))
            .withMentionSpam(new MentionFilterConfig(true, 5, Duration.ofMinutes(1)))
            .withCustomWordFilter(new WordFilterConfig(true, new ArrayList<>(), new ArrayList<>()))
            .build();
    }
    
    private List<CustomRule> createDefaultRules() {
        List<CustomRule> rules = new ArrayList<>();
        
        // Anti-spam rule
        rules.add(new CustomRule.Builder("anti_spam")
            .withName("Anti-Spam Protection")
            .withDescription("Detects and prevents spam messages")
            .withCondition(new RuleCondition.Builder()
                .withType(ConditionType.MESSAGE_FREQUENCY)
                .withThreshold(5.0)
                .build())
            .withAction(ActionType.DELETE_MESSAGE)
            .withSeverity(2)
            .withEnabled(true)
            .build());
        
        // Toxicity rule
        rules.add(new CustomRule.Builder("toxicity")
            .withName("Toxicity Detection")
            .withDescription("Detects toxic and harmful content")
            .withCondition(new RuleCondition.Builder()
                .withType(ConditionType.TOXICITY_SCORE)
                .withThreshold(0.8)
                .build())
            .withAction(ActionType.WARN_USER)
            .withSeverity(3)
            .withEnabled(true)
            .build());
        
        // Raid protection rule
        rules.add(new CustomRule.Builder("raid_protection")
            .withName("Raid Protection")
            .withDescription("Protects against coordinated attacks")
            .withCondition(new RuleCondition.Builder()
                .withType(ConditionType.RAPID_JOINS)
                .withThreshold(10.0)
                .build())
            .withAction(ActionType.LOCKDOWN_CHANNEL)
            .withSeverity(4)
            .withEnabled(true)
            .build());
        
        return rules;
    }
    
    private ThresholdConfig createDefaultThresholdConfig() {
        Map<String, ViolationThreshold> thresholds = new HashMap<>();
        
        thresholds.put("spam", new ViolationThreshold(3, Duration.ofHours(1), ActionType.TIMEOUT_USER, Duration.ofMinutes(10)));
        thresholds.put("toxicity", new ViolationThreshold(2, Duration.ofHours(6), ActionType.TIMEOUT_USER, Duration.ofHours(1)));
        thresholds.put("severe", new ViolationThreshold(1, Duration.ofDays(1), ActionType.BAN_USER, Duration.ofDays(7)));
        
        return new ThresholdConfig(thresholds, true, true);
    }
    
    private ActionConfig createDefaultActionConfig() {
        Map<ActionType, ActionSettings> actionSettings = new HashMap<>();
        
        actionSettings.put(ActionType.DELETE_MESSAGE, new ActionSettings(true, true, "Message deleted for policy violation"));
        actionSettings.put(ActionType.WARN_USER, new ActionSettings(true, true, "You have received a warning for violating server rules"));
        actionSettings.put(ActionType.TIMEOUT_USER, new ActionSettings(true, true, "You have been timed out for violating server rules"));
        actionSettings.put(ActionType.BAN_USER, new ActionSettings(true, false, "You have been banned for severe rule violations"));
        
        return new ActionConfig(actionSettings, true, true, Arrays.asList("123456789"));
    }
    
    private UIConfig createDefaultUIConfig() {
        return new UIConfig.Builder()
            .withTheme(UITheme.DARK)
            .withLanguage("en")
            .withShowAdvancedOptions(false)
            .withCustomColors(new HashMap<>())
            .withDashboardLayout(DashboardLayout.COMPACT)
            .build();
    }
}

/**
 * Guild-specific moderation configuration
 */
class CustomGuildModerationConfig {
    private final String guildId;
    private final FilterConfig filterConfig;
    private final List<CustomRule> customRules;
    private final ThresholdConfig thresholdConfig;
    private final ActionConfig actionConfig;
    private final UIConfig uiConfig;
    private final Map<String, Object> customSettings;
    private final Instant lastModified;
    private final String modifiedBy;
    
    private CustomGuildModerationConfig(Builder builder) {
        this.guildId = builder.guildId;
        this.filterConfig = builder.filterConfig;
        this.customRules = new ArrayList<>(builder.customRules);
        this.thresholdConfig = builder.thresholdConfig;
        this.actionConfig = builder.actionConfig;
        this.uiConfig = builder.uiConfig;
        this.customSettings = new HashMap<>(builder.customSettings);
        this.lastModified = Instant.now();
        this.modifiedBy = builder.modifiedBy;
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public FilterConfig getFilterConfig() { return filterConfig; }
    public List<CustomRule> getCustomRules() { return new ArrayList<>(customRules); }
    public ThresholdConfig getThresholdConfig() { return thresholdConfig; }
    public ActionConfig getActionConfig() { return actionConfig; }
    public UIConfig getUiConfig() { return uiConfig; }
    public Map<String, Object> getCustomSettings() { return new HashMap<>(customSettings); }
    public Instant getLastModified() { return lastModified; }
    public String getModifiedBy() { return modifiedBy; }
    
    public static class Builder {
        private final String guildId;
        private FilterConfig filterConfig;
        private List<CustomRule> customRules = new ArrayList<>();
        private ThresholdConfig thresholdConfig;
        private ActionConfig actionConfig;
        private UIConfig uiConfig;
        private Map<String, Object> customSettings = new HashMap<>();
        private String modifiedBy;
        
        public Builder(String guildId) {
            this.guildId = guildId;
        }
        
        public Builder withFilterConfig(FilterConfig filterConfig) {
            this.filterConfig = filterConfig;
            return this;
        }
        
        public Builder withCustomRules(List<CustomRule> customRules) {
            this.customRules = new ArrayList<>(customRules);
            return this;
        }
        
        public Builder withThresholdConfig(ThresholdConfig thresholdConfig) {
            this.thresholdConfig = thresholdConfig;
            return this;
        }
        
        public Builder withActionConfig(ActionConfig actionConfig) {
            this.actionConfig = actionConfig;
            return this;
        }
        
        public Builder withUIConfig(UIConfig uiConfig) {
            this.uiConfig = uiConfig;
            return this;
        }
        
        public Builder withCustomSettings(Map<String, Object> customSettings) {
            this.customSettings = new HashMap<>(customSettings);
            return this;
        }
        
        public Builder withModifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }
        
        public CustomGuildModerationConfig build() {
            return new CustomGuildModerationConfig(this);
        }
    }
}

/**
 * Filter configuration for various detection types
 */
class FilterConfig {
    private final SpamFilterConfig spamDetection;
    private final ToxicityFilterConfig toxicityDetection;
    private final LinkFilterConfig linkProtection;
    private final CapsFilterConfig capsFilter;
    private final MentionFilterConfig mentionSpam;
    private final WordFilterConfig customWordFilter;
    private final Map<String, Object> customFilters;
    
    private FilterConfig(Builder builder) {
        this.spamDetection = builder.spamDetection;
        this.toxicityDetection = builder.toxicityDetection;
        this.linkProtection = builder.linkProtection;
        this.capsFilter = builder.capsFilter;
        this.mentionSpam = builder.mentionSpam;
        this.customWordFilter = builder.customWordFilter;
        this.customFilters = new HashMap<>(builder.customFilters);
    }
    
    // Getters
    public SpamFilterConfig getSpamDetection() { return spamDetection; }
    public ToxicityFilterConfig getToxicityDetection() { return toxicityDetection; }
    public LinkFilterConfig getLinkProtection() { return linkProtection; }
    public CapsFilterConfig getCapsFilter() { return capsFilter; }
    public MentionFilterConfig getMentionSpam() { return mentionSpam; }
    public WordFilterConfig getCustomWordFilter() { return customWordFilter; }
    public Map<String, Object> getCustomFilters() { return new HashMap<>(customFilters); }
    
    public static class Builder {
        private SpamFilterConfig spamDetection;
        private ToxicityFilterConfig toxicityDetection;
        private LinkFilterConfig linkProtection;
        private CapsFilterConfig capsFilter;
        private MentionFilterConfig mentionSpam;
        private WordFilterConfig customWordFilter;
        private Map<String, Object> customFilters = new HashMap<>();
        
        public Builder withSpamDetection(SpamFilterConfig spamDetection) {
            this.spamDetection = spamDetection;
            return this;
        }
        
        public Builder withToxicityDetection(ToxicityFilterConfig toxicityDetection) {
            this.toxicityDetection = toxicityDetection;
            return this;
        }
        
        public Builder withLinkProtection(LinkFilterConfig linkProtection) {
            this.linkProtection = linkProtection;
            return this;
        }
        
        public Builder withCapsFilter(CapsFilterConfig capsFilter) {
            this.capsFilter = capsFilter;
            return this;
        }
        
        public Builder withMentionSpam(MentionFilterConfig mentionSpam) {
            this.mentionSpam = mentionSpam;
            return this;
        }
        
        public Builder withCustomWordFilter(WordFilterConfig customWordFilter) {
            this.customWordFilter = customWordFilter;
            return this;
        }
        
        public Builder withCustomFilters(Map<String, Object> customFilters) {
            this.customFilters = new HashMap<>(customFilters);
            return this;
        }
        
        public FilterConfig build() {
            return new FilterConfig(this);
        }
    }
}

// Individual filter configurations
class SpamFilterConfig {
    private final boolean enabled;
    private final double threshold;
    private final Duration timeWindow;
    private final int maxMessages;
    private final boolean checkDuplicates;
    private final boolean checkRapidTyping;
    
    public SpamFilterConfig(boolean enabled, double threshold, Duration timeWindow, int maxMessages) {
        this(enabled, threshold, timeWindow, maxMessages, true, true);
    }
    
    public SpamFilterConfig(boolean enabled, double threshold, Duration timeWindow, int maxMessages,
                           boolean checkDuplicates, boolean checkRapidTyping) {
        this.enabled = enabled;
        this.threshold = threshold;
        this.timeWindow = timeWindow;
        this.maxMessages = maxMessages;
        this.checkDuplicates = checkDuplicates;
        this.checkRapidTyping = checkRapidTyping;
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public double getThreshold() { return threshold; }
    public Duration getTimeWindow() { return timeWindow; }
    public int getMaxMessages() { return maxMessages; }
    public boolean isCheckDuplicates() { return checkDuplicates; }
    public boolean isCheckRapidTyping() { return checkRapidTyping; }
}

class ToxicityFilterConfig {
    private final boolean enabled;
    private final double threshold;
    private final List<String> supportedLanguages;
    private final boolean useAI;
    private final boolean checkSentiment;
    private final boolean checkContext;
    
    public ToxicityFilterConfig(boolean enabled, double threshold, List<String> supportedLanguages) {
        this(enabled, threshold, supportedLanguages, true, true, true);
    }
    
    public ToxicityFilterConfig(boolean enabled, double threshold, List<String> supportedLanguages,
                               boolean useAI, boolean checkSentiment, boolean checkContext) {
        this.enabled = enabled;
        this.threshold = threshold;
        this.supportedLanguages = new ArrayList<>(supportedLanguages);
        this.useAI = useAI;
        this.checkSentiment = checkSentiment;
        this.checkContext = checkContext;
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public double getThreshold() { return threshold; }
    public List<String> getSupportedLanguages() { return new ArrayList<>(supportedLanguages); }
    public boolean isUseAI() { return useAI; }
    public boolean isCheckSentiment() { return checkSentiment; }
    public boolean isCheckContext() { return checkContext; }
}

class LinkFilterConfig {
    private final boolean enabled;
    private final int maxLinks;
    private final List<String> blockedDomains;
    private final List<String> allowedDomains;
    private final boolean checkShorteners;
    private final boolean checkReputation;
    
    public LinkFilterConfig(boolean enabled, int maxLinks, List<String> blockedDomains) {
        this(enabled, maxLinks, blockedDomains, new ArrayList<>(), true, true);
    }
    
    public LinkFilterConfig(boolean enabled, int maxLinks, List<String> blockedDomains,
                           List<String> allowedDomains, boolean checkShorteners, boolean checkReputation) {
        this.enabled = enabled;
        this.maxLinks = maxLinks;
        this.blockedDomains = new ArrayList<>(blockedDomains);
        this.allowedDomains = new ArrayList<>(allowedDomains);
        this.checkShorteners = checkShorteners;
        this.checkReputation = checkReputation;
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public int getMaxLinks() { return maxLinks; }
    public List<String> getBlockedDomains() { return new ArrayList<>(blockedDomains); }
    public List<String> getAllowedDomains() { return new ArrayList<>(allowedDomains); }
    public boolean isCheckShorteners() { return checkShorteners; }
    public boolean isCheckReputation() { return checkReputation; }
}

class CapsFilterConfig {
    private final boolean enabled;
    private final double threshold;
    private final int minimumLength;
    private final boolean ignoreEmojis;
    
    public CapsFilterConfig(boolean enabled, double threshold, int minimumLength) {
        this(enabled, threshold, minimumLength, true);
    }
    
    public CapsFilterConfig(boolean enabled, double threshold, int minimumLength, boolean ignoreEmojis) {
        this.enabled = enabled;
        this.threshold = threshold;
        this.minimumLength = minimumLength;
        this.ignoreEmojis = ignoreEmojis;
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public double getThreshold() { return threshold; }
    public int getMinimumLength() { return minimumLength; }
    public boolean isIgnoreEmojis() { return ignoreEmojis; }
}

class MentionFilterConfig {
    private final boolean enabled;
    private final int maxMentions;
    private final Duration timeWindow;
    private final boolean checkRoleMentions;
    private final boolean checkEveryoneMentions;
    
    public MentionFilterConfig(boolean enabled, int maxMentions, Duration timeWindow) {
        this(enabled, maxMentions, timeWindow, true, true);
    }
    
    public MentionFilterConfig(boolean enabled, int maxMentions, Duration timeWindow,
                              boolean checkRoleMentions, boolean checkEveryoneMentions) {
        this.enabled = enabled;
        this.maxMentions = maxMentions;
        this.timeWindow = timeWindow;
        this.checkRoleMentions = checkRoleMentions;
        this.checkEveryoneMentions = checkEveryoneMentions;
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public int getMaxMentions() { return maxMentions; }
    public Duration getTimeWindow() { return timeWindow; }
    public boolean isCheckRoleMentions() { return checkRoleMentions; }
    public boolean isCheckEveryoneMentions() { return checkEveryoneMentions; }
}

class WordFilterConfig {
    private final boolean enabled;
    private final List<String> bannedWords;
    private final List<String> suspiciousWords;
    private final boolean useRegex;
    private final boolean caseSensitive;
    private final boolean wholeWordsOnly;
    
    public WordFilterConfig(boolean enabled, List<String> bannedWords, List<String> suspiciousWords) {
        this(enabled, bannedWords, suspiciousWords, false, false, true);
    }
    
    public WordFilterConfig(boolean enabled, List<String> bannedWords, List<String> suspiciousWords,
                           boolean useRegex, boolean caseSensitive, boolean wholeWordsOnly) {
        this.enabled = enabled;
        this.bannedWords = new ArrayList<>(bannedWords);
        this.suspiciousWords = new ArrayList<>(suspiciousWords);
        this.useRegex = useRegex;
        this.caseSensitive = caseSensitive;
        this.wholeWordsOnly = wholeWordsOnly;
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public List<String> getBannedWords() { return new ArrayList<>(bannedWords); }
    public List<String> getSuspiciousWords() { return new ArrayList<>(suspiciousWords); }
    public boolean isUseRegex() { return useRegex; }
    public boolean isCaseSensitive() { return caseSensitive; }
    public boolean isWholeWordsOnly() { return wholeWordsOnly; }
}

/**
 * Custom rule definition
 */
class CustomRule {
    private final String id;
    private final String name;
    private final String description;
    private final RuleCondition condition;
    private final ActionType action;
    private final int severity;
    private final boolean enabled;
    private final Map<String, Object> parameters;
    private final List<String> exemptRoles;
    private final List<String> exemptChannels;
    
    private CustomRule(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.condition = builder.condition;
        this.action = builder.action;
        this.severity = builder.severity;
        this.enabled = builder.enabled;
        this.parameters = new HashMap<>(builder.parameters);
        this.exemptRoles = new ArrayList<>(builder.exemptRoles);
        this.exemptChannels = new ArrayList<>(builder.exemptChannels);
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RuleCondition getCondition() { return condition; }
    public ActionType getAction() { return action; }
    public int getSeverity() { return severity; }
    public boolean isEnabled() { return enabled; }
    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
    public List<String> getExemptRoles() { return new ArrayList<>(exemptRoles); }
    public List<String> getExemptChannels() { return new ArrayList<>(exemptChannels); }
    
    public static class Builder {
        private final String id;
        private String name;
        private String description;
        private RuleCondition condition;
        private ActionType action;
        private int severity = 1;
        private boolean enabled = true;
        private Map<String, Object> parameters = new HashMap<>();
        private List<String> exemptRoles = new ArrayList<>();
        private List<String> exemptChannels = new ArrayList<>();
        
        public Builder(String id) {
            this.id = id;
        }
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder withCondition(RuleCondition condition) {
            this.condition = condition;
            return this;
        }
        
        public Builder withAction(ActionType action) {
            this.action = action;
            return this;
        }
        
        public Builder withSeverity(int severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }
        
        public Builder withExemptRoles(List<String> exemptRoles) {
            this.exemptRoles = new ArrayList<>(exemptRoles);
            return this;
        }
        
        public Builder withExemptChannels(List<String> exemptChannels) {
            this.exemptChannels = new ArrayList<>(exemptChannels);
            return this;
        }
        
        public CustomRule build() {
            return new CustomRule(this);
        }
    }
}

// RuleCondition class removed - using separate class file

// ConditionType enum moved to separate file
// ActionType enum moved to separate file

/**
 * Threshold configuration for escalating actions
 */
class ThresholdConfig {
    private final Map<String, ViolationThreshold> thresholds;
    private final boolean enableEscalation;
    private final boolean resetOnGoodBehavior;
    private final Duration resetPeriod;
    
    public ThresholdConfig(Map<String, ViolationThreshold> thresholds, boolean enableEscalation, boolean resetOnGoodBehavior) {
        this(thresholds, enableEscalation, resetOnGoodBehavior, Duration.ofDays(30));
    }
    
    public ThresholdConfig(Map<String, ViolationThreshold> thresholds, boolean enableEscalation,
                          boolean resetOnGoodBehavior, Duration resetPeriod) {
        this.thresholds = new HashMap<>(thresholds);
        this.enableEscalation = enableEscalation;
        this.resetOnGoodBehavior = resetOnGoodBehavior;
        this.resetPeriod = resetPeriod;
    }
    
    // Getters
    public Map<String, ViolationThreshold> getThresholds() { return new HashMap<>(thresholds); }
    public boolean isEnableEscalation() { return enableEscalation; }
    public boolean isResetOnGoodBehavior() { return resetOnGoodBehavior; }
    public Duration getResetPeriod() { return resetPeriod; }
}

class ViolationThreshold {
    private final int violationCount;
    private final Duration timeWindow;
    private final ActionType action;
    private final Duration actionDuration;
    private final String customMessage;
    
    public ViolationThreshold(int violationCount, Duration timeWindow, ActionType action, Duration actionDuration) {
        this(violationCount, timeWindow, action, actionDuration, null);
    }
    
    public ViolationThreshold(int violationCount, Duration timeWindow, ActionType action,
                             Duration actionDuration, String customMessage) {
        this.violationCount = violationCount;
        this.timeWindow = timeWindow;
        this.action = action;
        this.actionDuration = actionDuration;
        this.customMessage = customMessage;
    }
    
    // Getters
    public int getViolationCount() { return violationCount; }
    public Duration getTimeWindow() { return timeWindow; }
    public ActionType getAction() { return action; }
    public Duration getActionDuration() { return actionDuration; }
    public String getCustomMessage() { return customMessage; }
}

/**
 * Action configuration
 */
class ActionConfig {
    private final Map<ActionType, ActionSettings> actionSettings;
    private final boolean enableLogging;
    private final boolean enableNotifications;
    private final List<String> moderatorRoles;
    private final String logChannelId;
    
    public ActionConfig(Map<ActionType, ActionSettings> actionSettings, boolean enableLogging,
                       boolean enableNotifications, List<String> moderatorRoles) {
        this(actionSettings, enableLogging, enableNotifications, moderatorRoles, null);
    }
    
    public ActionConfig(Map<ActionType, ActionSettings> actionSettings, boolean enableLogging,
                       boolean enableNotifications, List<String> moderatorRoles, String logChannelId) {
        this.actionSettings = new HashMap<>(actionSettings);
        this.enableLogging = enableLogging;
        this.enableNotifications = enableNotifications;
        this.moderatorRoles = new ArrayList<>(moderatorRoles);
        this.logChannelId = logChannelId;
    }
    
    // Getters
    public Map<ActionType, ActionSettings> getActionSettings() { return new HashMap<>(actionSettings); }
    public boolean isEnableLogging() { return enableLogging; }
    public boolean isEnableNotifications() { return enableNotifications; }
    public List<String> getModeratorRoles() { return new ArrayList<>(moderatorRoles); }
    public String getLogChannelId() { return logChannelId; }
}

class ActionSettings {
    private final boolean enabled;
    private final boolean requireConfirmation;
    private final String customMessage;
    private final Duration defaultDuration;
    private final Map<String, Object> parameters;
    
    public ActionSettings(boolean enabled, boolean requireConfirmation, String customMessage) {
        this(enabled, requireConfirmation, customMessage, null, new HashMap<>());
    }
    
    public ActionSettings(boolean enabled, boolean requireConfirmation, String customMessage,
                         Duration defaultDuration, Map<String, Object> parameters) {
        this.enabled = enabled;
        this.requireConfirmation = requireConfirmation;
        this.customMessage = customMessage;
        this.defaultDuration = defaultDuration;
        this.parameters = new HashMap<>(parameters);
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public boolean isRequireConfirmation() { return requireConfirmation; }
    public String getCustomMessage() { return customMessage; }
    public Duration getDefaultDuration() { return defaultDuration; }
    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
}

/**
 * UI customization configuration
 */
class UIConfig {
    private final UITheme theme;
    private final String language;
    private final boolean showAdvancedOptions;
    private final Map<String, String> customColors;
    private final DashboardLayout dashboardLayout;
    private final List<String> enabledWidgets;
    private final Map<String, Object> customSettings;
    
    private UIConfig(Builder builder) {
        this.theme = builder.theme;
        this.language = builder.language;
        this.showAdvancedOptions = builder.showAdvancedOptions;
        this.customColors = new HashMap<>(builder.customColors);
        this.dashboardLayout = builder.dashboardLayout;
        this.enabledWidgets = new ArrayList<>(builder.enabledWidgets);
        this.customSettings = new HashMap<>(builder.customSettings);
    }
    
    // Getters
    public UITheme getTheme() { return theme; }
    public String getLanguage() { return language; }
    public boolean isShowAdvancedOptions() { return showAdvancedOptions; }
    public Map<String, String> getCustomColors() { return new HashMap<>(customColors); }
    public DashboardLayout getDashboardLayout() { return dashboardLayout; }
    public List<String> getEnabledWidgets() { return new ArrayList<>(enabledWidgets); }
    public Map<String, Object> getCustomSettings() { return new HashMap<>(customSettings); }
    
    public static class Builder {
        private UITheme theme = UITheme.DARK;
        private String language = "en";
        private boolean showAdvancedOptions = false;
        private Map<String, String> customColors = new HashMap<>();
        private DashboardLayout dashboardLayout = DashboardLayout.COMPACT;
        private List<String> enabledWidgets = new ArrayList<>();
        private Map<String, Object> customSettings = new HashMap<>();
        
        public Builder withTheme(UITheme theme) {
            this.theme = theme;
            return this;
        }
        
        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }
        
        public Builder withShowAdvancedOptions(boolean showAdvancedOptions) {
            this.showAdvancedOptions = showAdvancedOptions;
            return this;
        }
        
        public Builder withCustomColors(Map<String, String> customColors) {
            this.customColors = new HashMap<>(customColors);
            return this;
        }
        
        public Builder withDashboardLayout(DashboardLayout dashboardLayout) {
            this.dashboardLayout = dashboardLayout;
            return this;
        }
        
        public Builder withEnabledWidgets(List<String> enabledWidgets) {
            this.enabledWidgets = new ArrayList<>(enabledWidgets);
            return this;
        }
        
        public Builder withCustomSettings(Map<String, Object> customSettings) {
            this.customSettings = new HashMap<>(customSettings);
            return this;
        }
        
        public UIConfig build() {
            return new UIConfig(this);
        }
    }
}

// UITheme and DashboardLayout enums moved to separate files

// ModerationResult class removed - using separate ModerationResult.java file

// Placeholder ModerationResult class - should match the separate ModerationResult.java file
class ModerationResult {
    private final boolean allowed;
    private final String reason;
    private final ModerationAction action;
    
    private ModerationResult(boolean allowed, String reason, ModerationAction action) {
        this.allowed = allowed;
        this.reason = reason;
        this.action = action;
    }
    
    public static ModerationResult allowed() {
        return new ModerationResult(true, "No violations detected", ModerationAction.NONE);
    }
    
    public static ModerationResult moderate(String reason, ModerationAction action) {
        return new ModerationResult(false, reason, action);
    }
    
    public static ModerationResult warn(String reason, ModerationAction action) {
        return new ModerationResult(false, reason, action);
    }
    
    // Getters
    public boolean isAllowed() { return allowed; }
    public String getReason() { return reason; }
    public ModerationAction getAction() { return action; }
}

// Placeholder ModerationAction enum - should match the separate ModerationAction.java file
enum ModerationAction {
    NONE, DELETE_MESSAGE, WARN_USER, TIMEOUT_USER, BAN_USER, LOCKDOWN_CHANNEL, LOG_VIOLATION
}

class FilterResult {
    private final String filterType;
    private final boolean triggered;
    private final double confidence;
    private final String reason;
    private final Map<String, Object> details;
    
    public FilterResult(String filterType, boolean triggered, double confidence, String reason, Map<String, Object> details) {
        this.filterType = filterType;
        this.triggered = triggered;
        this.confidence = confidence;
        this.reason = reason;
        this.details = new HashMap<>(details);
    }
    
    // Getters
    public String getFilterType() { return filterType; }
    public boolean isTriggered() { return triggered; }
    public double getConfidence() { return confidence; }
    public String getReason() { return reason; }
    public Map<String, Object> getDetails() { return new HashMap<>(details); }
}

class RuleViolation {
    private final String ruleId;
    private final String ruleName;
    private final int severity;
    private final String description;
    private final double confidence;
    private final Map<String, Object> evidence;
    
    public RuleViolation(String ruleId, String ruleName, int severity, String description,
                        double confidence, Map<String, Object> evidence) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.severity = severity;
        this.description = description;
        this.confidence = confidence;
        this.evidence = new HashMap<>(evidence);
    }
    
    // Getters
    public String getRuleId() { return ruleId; }
    public String getRuleName() { return ruleName; }
    public int getSeverity() { return severity; }
    public String getDescription() { return description; }
    public double getConfidence() { return confidence; }
    public Map<String, Object> getEvidence() { return new HashMap<>(evidence); }
}

class ThresholdResult {
    private final boolean thresholdExceeded;
    private final String thresholdType;
    private final int currentViolations;
    private final int thresholdLimit;
    private final ActionType recommendedAction;
    private final Duration actionDuration;
    
    public ThresholdResult(boolean thresholdExceeded, String thresholdType, int currentViolations,
                          int thresholdLimit, ActionType recommendedAction, Duration actionDuration) {
        this.thresholdExceeded = thresholdExceeded;
        this.thresholdType = thresholdType;
        this.currentViolations = currentViolations;
        this.thresholdLimit = thresholdLimit;
        this.recommendedAction = recommendedAction;
        this.actionDuration = actionDuration;
    }
    
    // Getters
    public boolean isThresholdExceeded() { return thresholdExceeded; }
    public String getThresholdType() { return thresholdType; }
    public int getCurrentViolations() { return currentViolations; }
    public int getThresholdLimit() { return thresholdLimit; }
    public ActionType getRecommendedAction() { return recommendedAction; }
    public Duration getActionDuration() { return actionDuration; }
}

// ModerationAction class removed - using separate ModerationAction.java file

// Engine placeholder classes
class CustomRuleEngine {
    public List<RuleViolation> evaluateRules(String content, String userId, List<CustomRule> rules, Map<String, Object> context) {
        // Placeholder implementation
        return new ArrayList<>();
    }
}

class ActionEngine {
    public List<ModerationAction> determineActions(List<RuleViolation> violations, ThresholdResult thresholdResult, ActionConfig actionConfig) {
        // Placeholder implementation
        return new ArrayList<>();
    }
}

class ThresholdEngine {
    public ThresholdResult evaluateThresholds(String userId, List<RuleViolation> violations, ThresholdConfig thresholdConfig) {
        // Placeholder implementation
        return new ThresholdResult(false, "none", 0, 0, ActionType.LOG_VIOLATION, Duration.ZERO);
    }
}

class FilterCustomizationManager {
    public List<FilterResult> applyFilters(String content, FilterConfig filterConfig, Map<String, Object> context) {
        // Placeholder implementation
        return new ArrayList<>();
    }
}

class ActionCustomizationManager {
    // Placeholder implementation
}

class UICustomizationManager {
    // Placeholder implementation
}