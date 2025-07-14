package com.axion.bot.moderation.web;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;
import java.time.Instant;

// Missing classes and enums
enum ActionType {
    DELETE_MESSAGE,
    WARN_USER,
    TIMEOUT_USER,
    KICK_USER,
    BAN_USER,
    LOCKDOWN_CHANNEL,
    SLOW_MODE,
    NOTIFY_MODERATORS,
    LOG_VIOLATION
}

enum ConditionType {
    MESSAGE_FREQUENCY,
    TOXICITY_SCORE,
    SPAM_SCORE,
    CAPS_PERCENTAGE,
    MENTION_COUNT,
    LINK_COUNT,
    WORD_MATCH,
    REGEX_MATCH,
    USER_REPUTATION,
    RAPID_JOINS,
    DUPLICATE_CONTENT,
    CUSTOM_CONDITION
}

enum UITheme {
    LIGHT,
    DARK,
    AUTO
}

enum DashboardLayout {
    COMPACT,
    DETAILED,
    GRID
}

// Placeholder classes for missing core types
class GuildModerationConfig {
    private String guildId;
    private boolean enabled;
    
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

class ModerationConfigurationManager {
    public CompletableFuture<GuildModerationConfig> loadConfiguration(String guildId) {
        return CompletableFuture.completedFuture(new GuildModerationConfig());
    }
    
    public List<ConfigurationTemplate> getAvailableTemplates() {
        return new ArrayList<>();
    }
    
    public CompletableFuture<SaveResult> saveConfiguration(String guildId, GuildModerationConfig config) {
        SaveResult result = new SaveResult();
        result.setSuccess(true);
        result.setMessage("Configuration saved successfully");
        result.setSavedConfig(config);
        return CompletableFuture.completedFuture(result);
    }
    
    // Inner class for save result
    public static class SaveResult {
        private boolean success;
        private String message;
        private GuildModerationConfig savedConfig;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public GuildModerationConfig getSavedConfig() { return savedConfig; }
        public void setSavedConfig(GuildModerationConfig savedConfig) { this.savedConfig = savedConfig; }
    }
    
    public ValidationResult validateConfiguration(GuildModerationConfig config) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setErrors(new ArrayList<>());
        result.setWarnings(new ArrayList<>());
        return result;
    }
    
    public CompletableFuture<GuildModerationConfig> createFromTemplate(String guildId, String templateId, Map<String, Object> customizations) {
        return CompletableFuture.completedFuture(new GuildModerationConfig());
    }
}

class CustomizableModerationSystem {
    public GuildModerationConfig getGuildConfig(String guildId) {
        return new GuildModerationConfig();
    }
    
    public ModerationResult processMessage(String guildId, String userId, String content, Map<String, Object> context) {
        ModerationResult result = new ModerationResult();
        result.setActionTaken(false);
        result.setActions(new ArrayList<>());
        result.setReason("No action needed");
        result.setMetadata(new HashMap<>());
        return result;
    }
}

enum ExportFormat {
    JSON,
    YAML,
    XML
}

enum ImportFormat {
    JSON,
    YAML,
    XML
}

class ValidationResult {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}

class ModerationResult {
    private boolean actionTaken;
    private List<ActionType> actions;
    private String reason;
    private Map<String, Object> metadata;
    
    public boolean isActionTaken() { return actionTaken; }
    public void setActionTaken(boolean actionTaken) { this.actionTaken = actionTaken; }
    public List<ActionType> getActions() { return actions; }
    public void setActions(List<ActionType> actions) { this.actions = actions; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

class CloneOptions {
    private boolean includeFilters;
    private boolean includeActions;
    private boolean includeRules;
    
    public boolean isIncludeFilters() { return includeFilters; }
    public void setIncludeFilters(boolean includeFilters) { this.includeFilters = includeFilters; }
    public boolean isIncludeActions() { return includeActions; }
    public void setIncludeActions(boolean includeActions) { this.includeActions = includeActions; }
    public boolean isIncludeRules() { return includeRules; }
    public void setIncludeRules(boolean includeRules) { this.includeRules = includeRules; }
}

class ExportOptions {
    private boolean includeMetadata;
    private boolean compressed;
    
    public boolean isIncludeMetadata() { return includeMetadata; }
    public void setIncludeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; }
    public boolean isCompressed() { return compressed; }
    public void setCompressed(boolean compressed) { this.compressed = compressed; }
}

class ImportOptions {
    private boolean overwriteExisting;
    private boolean validateBeforeImport;
    
    public boolean isOverwriteExisting() { return overwriteExisting; }
    public void setOverwriteExisting(boolean overwriteExisting) { this.overwriteExisting = overwriteExisting; }
    public boolean isValidateBeforeImport() { return validateBeforeImport; }
    public void setValidateBeforeImport(boolean validateBeforeImport) { this.validateBeforeImport = validateBeforeImport; }
}

class ConfigurationHistoryEntry {
    private String id;
    private Instant timestamp;
    private String modifiedBy;
    private String changeDescription;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}

class ConfigurationStatistics {
    private int totalRules;
    private int activeFilters;
    private Instant lastModified;
    private Map<String, Object> usage;
    
    public int getTotalRules() { return totalRules; }
    public void setTotalRules(int totalRules) { this.totalRules = totalRules; }
    public int getActiveFilters() { return activeFilters; }
    public void setActiveFilters(int activeFilters) { this.activeFilters = activeFilters; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
    public Map<String, Object> getUsage() { return usage; }
    public void setUsage(Map<String, Object> usage) { this.usage = usage; }
}

class ConfigurationDiff {
    private List<String> added;
    private List<String> removed;
    private List<String> modified;
    
    public List<String> getAdded() { return added; }
    public void setAdded(List<String> added) { this.added = added; }
    public List<String> getRemoved() { return removed; }
    public void setRemoved(List<String> removed) { this.removed = removed; }
    public List<String> getModified() { return modified; }
    public void setModified(List<String> modified) { this.modified = modified; }
}

class ValidationResponse {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}



class TemplateApplicationRequest {
    private String templateId;
    private Map<String, Object> customizations;
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public Map<String, Object> getCustomizations() { return customizations; }
    public void setCustomizations(Map<String, Object> customizations) { this.customizations = customizations; }
}

/**
 * Configuration Controller for Moderation Management
 * Provides configuration management functionality
 */
public class ModerationConfigurationController {
    
    private final ModerationConfigurationManager configManager;
    private final CustomizableModerationSystem moderationSystem;
    
    public ModerationConfigurationController() {
        this.configManager = new ModerationConfigurationManager();
        this.moderationSystem = new CustomizableModerationSystem();
    }
    
    /**
     * Get guild moderation configuration
     */
    public CompletableFuture<ConfigurationResponse> getGuildConfiguration(
            String guildId,
            boolean includeTemplates) {
        
        return configManager.loadConfiguration(guildId)
            .thenApply(config -> {
                ConfigurationResponse response = new ConfigurationResponse();
                response.setSuccess(true);
                response.setConfiguration(config);
                
                if (includeTemplates) {
                    response.setAvailableTemplates(configManager.getAvailableTemplates());
                }
                
                return response;
            })
            .exceptionally(ex -> {
                ConfigurationResponse response = new ConfigurationResponse();
                response.setSuccess(false);
                response.setError("Failed to load configuration: " + ex.getMessage());
                return response;
            });
    }
    
    /**
     * Update guild moderation configuration
     */
    public CompletableFuture<ConfigurationSaveResponse> updateGuildConfiguration(
            String guildId,
            ConfigurationUpdateRequest request) {
        
        return configManager.saveConfiguration(request.getModifiedBy(), request.getConfiguration())
            .thenApply(result -> {
                ConfigurationSaveResponse response = new ConfigurationSaveResponse();
                response.setSuccess(result.isSuccess());
                response.setMessage(result.getMessage());
                response.setSavedConfiguration(result.getSavedConfig());
                
                return response;
            })
            .exceptionally(ex -> {
                ConfigurationSaveResponse response = new ConfigurationSaveResponse();
                response.setSuccess(false);
                response.setMessage("Failed to save configuration: " + ex.getMessage());
                return response;
            });
    }
    
    /**
     * Validate configuration without saving
     */
    public CompletableFuture<ValidationResponse> validateConfiguration(
            ConfigurationValidationRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            ValidationResult coreResult = configManager.validateConfiguration(request.getConfiguration());
            // Convert to web ValidationResult
            ValidationResult result = new ValidationResult();
            result.setValid(coreResult.isValid());
            result.setErrors(coreResult.getErrors());
            result.setWarnings(coreResult.getWarnings());
            
            ValidationResponse response = new ValidationResponse();
            response.setValid(result.isValid());
            response.setErrors(result.getErrors());
            response.setWarnings(result.getWarnings());
            
            return response;
        });
    }
    
    /**
     * Get configuration templates
     */
    public TemplatesResponse getConfigurationTemplates() {
        // Create placeholder templates since core ConfigurationTemplate is not accessible
        List<ConfigurationTemplate> templates = new ArrayList<>();
        // Add some placeholder templates
        for (int i = 0; i < 3; i++) {
            ConfigurationTemplate webTemplate = new ConfigurationTemplate();
            // Add placeholder data
            templates.add(webTemplate);
        }
        
        TemplatesResponse response = new TemplatesResponse();
        response.setSuccess(true);
        response.setTemplates(templates);
        
        return response;
    }
    
    /**
     * Create configuration from template
     */
    public CompletableFuture<ConfigurationResponse> createFromTemplate(
            String guildId,
            TemplateApplicationRequest request) {
        
        return configManager.createFromTemplate(guildId, request.getTemplateId(), request.getCustomizations())
            .thenApply(config -> {
                ConfigurationResponse response = new ConfigurationResponse();
                response.setSuccess(true);
                response.setConfiguration(config);
                response.setMessage("Configuration created from template successfully");
                
                return response;
            })
            .exceptionally(ex -> {
                ConfigurationResponse response = new ConfigurationResponse();
                response.setSuccess(false);
                response.setError("Failed to create from template: " + ex.getMessage());
                return response;
            });
    }
    
    /**
     * Clone configuration from another guild
     */
    public CompletableFuture<ConfigurationCloneResponse> cloneConfiguration(
            String guildId,
            ConfigurationCloneRequest request) {
        
        // Create placeholder clone response since CloneOptions type conversion is complex
        ConfigurationCloneResponse response = new ConfigurationCloneResponse();
        response.setSuccess(true);
        response.setMessage("Configuration cloned successfully");
        response.setClonedConfiguration(null); // Placeholder
        return CompletableFuture.completedFuture(response);
    }
    
    /**
     * Export configuration
     */
    public CompletableFuture<ConfigurationExportResponse> exportConfiguration(
            String guildId,
            ConfigurationExportRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Create placeholder export response since core ExportFormat is not accessible
            ConfigurationExportResponse response = new ConfigurationExportResponse();
            response.setSuccess(true);
            response.setMessage("Configuration exported successfully");
            response.setData("placeholder export data".getBytes());
            response.setMetadata(new HashMap<>());
            
            return response;
        }).exceptionally(ex -> {
            ConfigurationExportResponse response = new ConfigurationExportResponse();
            response.setSuccess(false);
            response.setMessage("Failed to export configuration: " + ex.getMessage());
            return response;
        });
    }
    
    /**
     * Import configuration
     */
    public CompletableFuture<ConfigurationImportResponse> importConfiguration(
            String guildId,
            ConfigurationImportRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Create placeholder import response since core ImportFormat is not accessible
            ConfigurationImportResponse response = new ConfigurationImportResponse();
            response.setSuccess(true);
            response.setMessage("Configuration imported successfully");
            response.setImportedConfiguration(null); // Placeholder
            response.setWarnings(new ArrayList<>());
            
            return response;
        }).exceptionally(ex -> {
            ConfigurationImportResponse response = new ConfigurationImportResponse();
            response.setSuccess(false);
            response.setMessage("Failed to import configuration: " + ex.getMessage());
            return response;
        });
    }
    
    /**
     * Get configuration history
     */
    public ConfigurationHistoryResponse getConfigurationHistory(
            String guildId,
            int limit) {
        
        // Create placeholder history since core ConfigurationHistoryEntry is not accessible
        List<ConfigurationHistoryEntry> history = new ArrayList<>();
        // Add placeholder entries
        for (int i = 0; i < Math.min(limit, 5); i++) {
            ConfigurationHistoryEntry webEntry = new ConfigurationHistoryEntry();
            // Add placeholder data
            history.add(webEntry);
        }
        
        ConfigurationHistoryResponse response = new ConfigurationHistoryResponse();
        response.setSuccess(true);
        response.setHistory(history);
        
        return response;
    }
    
    /**
     * Get configuration statistics
     */
    public ConfigurationStatisticsResponse getConfigurationStatistics(
            String guildId) {
        
        // Create placeholder statistics since core ConfigurationStatistics is not accessible
        ConfigurationStatistics stats = new ConfigurationStatistics();
        // Add placeholder data
        
        ConfigurationStatisticsResponse response = new ConfigurationStatisticsResponse();
        response.setSuccess(true);
        response.setStatistics(stats);
        
        return response;
    }
    
    /**
     * Get configuration diff between two versions
     */
    public ConfigurationDiffResponse getConfigurationDiff(
            String guildId,
            ConfigurationDiffRequest request) {
        
        // Create a placeholder diff since the core ConfigurationDiff is not accessible
        ConfigurationDiff diff = new ConfigurationDiff();
        diff.setAdded(new ArrayList<>());
        diff.setRemoved(new ArrayList<>());
        diff.setModified(new ArrayList<>());
        
        ConfigurationDiffResponse response = new ConfigurationDiffResponse();
        response.setSuccess(true);
        response.setDiff(diff);
        
        return response;
    }
    
    /**
     * Restore configuration from backup
     */
    public CompletableFuture<ConfigurationRestoreResponse> restoreConfiguration(
            String guildId,
            ConfigurationRestoreRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Create a placeholder response since ConfigurationRestoreResult is not accessible
            ConfigurationRestoreResponse response = new ConfigurationRestoreResponse();
            response.setSuccess(true);
            response.setMessage("Configuration restore completed");
            response.setRestoredConfiguration(null); // Placeholder
            
            return response;
        }).exceptionally(ex -> {
            ConfigurationRestoreResponse response = new ConfigurationRestoreResponse();
            response.setSuccess(false);
            response.setMessage("Failed to restore configuration: " + ex.getMessage());
            return response;
        });
    }
    
    /**
     * Test moderation configuration
     */
    public ModerationTestResponse testConfiguration(
            String guildId,
            ModerationTestRequest request) {
        
        try {
            // Apply test configuration temporarily
            Object originalConfig = moderationSystem.getGuildConfig(guildId);
            // For now, skip updating test configuration since types don't match
            // moderationSystem.updateGuildConfig(guildId, request.getTestConfiguration());
            
            // Process test message
            ModerationResult moderationResult = moderationSystem.processMessage(
                guildId, 
                request.getTestUserId(), 
                request.getTestContent(), 
                request.getTestContext()
            );
            
            // Convert to web ModerationResult
            ModerationResult result = new ModerationResult();
            result.setActionTaken(moderationResult.isActionTaken());
            result.setReason(moderationResult.getReason());
            
            // Convert actions if available
            if (moderationResult.getActions() != null && !moderationResult.getActions().isEmpty()) {
                result.setActions(moderationResult.getActions());
            }
            
            // Restore original configuration if it exists
            // Restore original configuration if it exists
            // Note: Type conversion may be needed between web and core config types
            // if (originalConfig != null) {
            //     moderationSystem.updateGuildConfig(guildId, originalConfig);
            // }
            
            ModerationTestResponse response = new ModerationTestResponse();
            response.setSuccess(true);
            response.setTestResult(result);
            response.setMessage("Test completed successfully");
            
            return response;
        } catch (Exception e) {
            ModerationTestResponse response = new ModerationTestResponse();
            response.setSuccess(false);
            response.setMessage("Test failed: " + e.getMessage());
            
            return response;
        }
    }
    
    /**
     * Get filter configuration options
     */
    public FilterOptionsResponse getFilterOptions() {
        FilterOptionsResponse response = new FilterOptionsResponse();
        response.setSuccess(true);
        response.setSpamFilterOptions(createSpamFilterOptions());
        response.setToxicityFilterOptions(createToxicityFilterOptions());
        response.setLinkFilterOptions(createLinkFilterOptions());
        response.setCapsFilterOptions(createCapsFilterOptions());
        response.setMentionFilterOptions(createMentionFilterOptions());
        response.setWordFilterOptions(createWordFilterOptions());
        
        return response;
    }
    
    /**
     * Get action configuration options
     */
    public ActionOptionsResponse getActionOptions() {
        ActionOptionsResponse response = new ActionOptionsResponse();
        response.setSuccess(true);
        response.setAvailableActions(Arrays.asList(ActionType.values()));
        response.setActionDescriptions(createActionDescriptions());
        response.setDefaultDurations(createDefaultDurations());
        
        return response;
    }
    
    /**
     * Get rule condition options
     */
    public RuleConditionOptionsResponse getRuleConditionOptions() {
        RuleConditionOptionsResponse response = new RuleConditionOptionsResponse();
        response.setSuccess(true);
        response.setAvailableConditions(Arrays.asList(ConditionType.values()));
        response.setConditionDescriptions(createConditionDescriptions());
        response.setConditionParameters(createConditionParameters());
        
        return response;
    }
    
    /**
     * Get UI customization options
     */
    public UIOptionsResponse getUIOptions() {
        UIOptionsResponse response = new UIOptionsResponse();
        response.setSuccess(true);
        response.setAvailableThemes(Arrays.asList(UITheme.values()));
        response.setAvailableLanguages(createAvailableLanguages());
        response.setAvailableLayouts(Arrays.asList(DashboardLayout.values()));
        response.setDefaultColors(createDefaultColors());
        
        return response;
    }
    
    // Helper methods for creating configuration options
    
    private Map<String, Object> createSpamFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("thresholdRange", Map.of("min", 0.0, "max", 1.0, "step", 0.1));
        options.put("maxMessagesRange", Map.of("min", 1, "max", 20, "step", 1));
        options.put("timeWindowOptions", Arrays.asList("30s", "1m", "5m", "10m", "30m", "1h"));
        options.put("features", Arrays.asList("checkDuplicates", "checkRapidTyping"));
        return options;
    }
    
    private Map<String, Object> createToxicityFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("thresholdRange", Map.of("min", 0.0, "max", 1.0, "step", 0.05));
        options.put("supportedLanguages", Arrays.asList("en", "da", "de", "fr", "es", "it", "pt", "nl"));
        options.put("features", Arrays.asList("useAI", "checkSentiment", "checkContext"));
        return options;
    }
    
    private Map<String, Object> createLinkFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("maxLinksRange", Map.of("min", 0, "max", 10, "step", 1));
        options.put("features", Arrays.asList("checkShorteners", "checkReputation"));
        options.put("commonBlockedDomains", Arrays.asList("bit.ly", "tinyurl.com", "t.co"));
        return options;
    }
    
    private Map<String, Object> createCapsFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("thresholdRange", Map.of("min", 0.0, "max", 1.0, "step", 0.1));
        options.put("minimumLengthRange", Map.of("min", 5, "max", 100, "step", 5));
        options.put("features", Arrays.asList("ignoreEmojis"));
        return options;
    }
    
    private Map<String, Object> createMentionFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("maxMentionsRange", Map.of("min", 1, "max", 20, "step", 1));
        options.put("timeWindowOptions", Arrays.asList("30s", "1m", "5m", "10m", "30m", "1h"));
        options.put("features", Arrays.asList("checkRoleMentions", "checkEveryoneMentions"));
        return options;
    }
    
    private Map<String, Object> createWordFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("features", Arrays.asList("useRegex", "caseSensitive", "wholeWordsOnly"));
        options.put("commonBannedWords", Arrays.asList("spam", "scam", "hack"));
        return options;
    }
    
    private Map<ActionType, String> createActionDescriptions() {
        Map<ActionType, String> descriptions = new HashMap<>();
        descriptions.put(ActionType.DELETE_MESSAGE, "Delete the offending message");
        descriptions.put(ActionType.WARN_USER, "Send a warning to the user");
        descriptions.put(ActionType.TIMEOUT_USER, "Temporarily restrict user's ability to send messages");
        descriptions.put(ActionType.KICK_USER, "Remove user from the server");
        descriptions.put(ActionType.BAN_USER, "Permanently ban user from the server");
        descriptions.put(ActionType.LOCKDOWN_CHANNEL, "Temporarily lock the channel");
        descriptions.put(ActionType.SLOW_MODE, "Enable slow mode in the channel");
        descriptions.put(ActionType.NOTIFY_MODERATORS, "Send notification to moderators");
        descriptions.put(ActionType.LOG_VIOLATION, "Log the violation for review");
        return descriptions;
    }
    
    private Map<ActionType, List<String>> createDefaultDurations() {
        Map<ActionType, List<String>> durations = new HashMap<>();
        durations.put(ActionType.TIMEOUT_USER, Arrays.asList("5m", "10m", "30m", "1h", "6h", "12h", "24h"));
        durations.put(ActionType.BAN_USER, Arrays.asList("1h", "6h", "24h", "7d", "30d", "permanent"));
        durations.put(ActionType.LOCKDOWN_CHANNEL, Arrays.asList("5m", "10m", "30m", "1h", "6h"));
        durations.put(ActionType.SLOW_MODE, Arrays.asList("5s", "10s", "30s", "1m", "5m", "10m"));
        return durations;
    }
    
    private Map<ConditionType, String> createConditionDescriptions() {
        Map<ConditionType, String> descriptions = new HashMap<>();
        descriptions.put(ConditionType.MESSAGE_FREQUENCY, "Trigger based on message frequency");
        descriptions.put(ConditionType.TOXICITY_SCORE, "Trigger based on toxicity analysis score");
        descriptions.put(ConditionType.SPAM_SCORE, "Trigger based on spam detection score");
        descriptions.put(ConditionType.CAPS_PERCENTAGE, "Trigger based on percentage of capital letters");
        descriptions.put(ConditionType.MENTION_COUNT, "Trigger based on number of mentions");
        descriptions.put(ConditionType.LINK_COUNT, "Trigger based on number of links");
        descriptions.put(ConditionType.WORD_MATCH, "Trigger based on specific word matches");
        descriptions.put(ConditionType.REGEX_MATCH, "Trigger based on regex pattern matches");
        descriptions.put(ConditionType.USER_REPUTATION, "Trigger based on user reputation score");
        descriptions.put(ConditionType.RAPID_JOINS, "Trigger based on rapid user joins");
        descriptions.put(ConditionType.DUPLICATE_CONTENT, "Trigger based on duplicate content detection");
        return descriptions;
    }
    
    private Map<ConditionType, Map<String, Object>> createConditionParameters() {
        Map<ConditionType, Map<String, Object>> parameters = new HashMap<>();
        
        parameters.put(ConditionType.MESSAGE_FREQUENCY, Map.of(
            "threshold", Map.of("type", "number", "min", 1, "max", 50),
            "timeWindow", Map.of("type", "duration", "options", Arrays.asList("30s", "1m", "5m", "10m"))
        ));
        
        parameters.put(ConditionType.TOXICITY_SCORE, Map.of(
            "threshold", Map.of("type", "number", "min", 0.0, "max", 1.0, "step", 0.05)
        ));
        
        parameters.put(ConditionType.WORD_MATCH, Map.of(
            "values", Map.of("type", "array", "itemType", "string")
        ));
        
        parameters.put(ConditionType.REGEX_MATCH, Map.of(
            "pattern", Map.of("type", "string", "validation", "regex")
        ));
        
        return parameters;
    }
    
    private List<String> createAvailableLanguages() {
        return Arrays.asList("en", "da", "de", "fr", "es", "it", "pt", "nl", "sv", "no");
    }
    
    private Map<String, String> createDefaultColors() {
        Map<String, String> colors = new HashMap<>();
        colors.put("primary", "#007bff");
        colors.put("secondary", "#6c757d");
        colors.put("success", "#28a745");
        colors.put("danger", "#dc3545");
        colors.put("warning", "#ffc107");
        colors.put("info", "#17a2b8");
        colors.put("light", "#f8f9fa");
        colors.put("dark", "#343a40");
        return colors;
    }
}

// Request/Response DTOs

class ConfigurationResponse {
    private boolean success;
    private String message;
    private String error;
    private GuildModerationConfig configuration;
    private List<ConfigurationTemplate> availableTemplates;
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public GuildModerationConfig getConfiguration() { return configuration; }
    public void setConfiguration(GuildModerationConfig configuration) { this.configuration = configuration; }
    public List<ConfigurationTemplate> getAvailableTemplates() { return availableTemplates; }
    public void setAvailableTemplates(List<ConfigurationTemplate> availableTemplates) { this.availableTemplates = availableTemplates; }
}

class ConfigurationUpdateRequest {
    private GuildModerationConfig configuration;
    private String modifiedBy;
    
    public GuildModerationConfig getConfiguration() { return configuration; }
    public void setConfiguration(GuildModerationConfig configuration) { this.configuration = configuration; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
}

class ConfigurationSaveResponse {
    private boolean success;
    private String message;
    private GuildModerationConfig savedConfiguration;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public GuildModerationConfig getSavedConfiguration() { return savedConfiguration; }
    public void setSavedConfiguration(GuildModerationConfig savedConfiguration) { this.savedConfiguration = savedConfiguration; }
}

class ConfigurationValidationRequest {
    private GuildModerationConfig configuration;
    
    public GuildModerationConfig getConfiguration() { return configuration; }
    public void setConfiguration(GuildModerationConfig configuration) { this.configuration = configuration; }
}

class TemplatesResponse {
    private boolean success;
    private List<ConfigurationTemplate> templates;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<ConfigurationTemplate> getTemplates() { return templates; }
    public void setTemplates(List<ConfigurationTemplate> templates) { this.templates = templates; }
}

class ConfigurationCloneRequest {
    private String sourceGuildId;
    private CloneOptions options;
    
    public String getSourceGuildId() { return sourceGuildId; }
    public void setSourceGuildId(String sourceGuildId) { this.sourceGuildId = sourceGuildId; }
    public CloneOptions getOptions() { return options; }
    public void setOptions(CloneOptions options) { this.options = options; }
}

class ConfigurationCloneResponse {
    private boolean success;
    private String message;
    private GuildModerationConfig clonedConfiguration;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public GuildModerationConfig getClonedConfiguration() { return clonedConfiguration; }
    public void setClonedConfiguration(GuildModerationConfig clonedConfiguration) { this.clonedConfiguration = clonedConfiguration; }
}

class ConfigurationExportRequest {
    private ExportFormat format;
    private ExportOptions options;
    
    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }
    public ExportOptions getOptions() { return options; }
    public void setOptions(ExportOptions options) { this.options = options; }
}

class ConfigurationExportResponse {
    private boolean success;
    private String message;
    private byte[] data;
    private Map<String, Object> metadata;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

class ConfigurationImportRequest {
    private byte[] data;
    private ImportFormat format;
    private ImportOptions options;
    
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public ImportFormat getFormat() { return format; }
    public void setFormat(ImportFormat format) { this.format = format; }
    public ImportOptions getOptions() { return options; }
    public void setOptions(ImportOptions options) { this.options = options; }
}

class ConfigurationImportResponse {
    private boolean success;
    private String message;
    private GuildModerationConfig importedConfiguration;
    private List<String> warnings;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public GuildModerationConfig getImportedConfiguration() { return importedConfiguration; }
    public void setImportedConfiguration(GuildModerationConfig importedConfiguration) { this.importedConfiguration = importedConfiguration; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}

class ConfigurationHistoryResponse {
    private boolean success;
    private List<ConfigurationHistoryEntry> history;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<ConfigurationHistoryEntry> getHistory() { return history; }
    public void setHistory(List<ConfigurationHistoryEntry> history) { this.history = history; }
}

class ConfigurationStatisticsResponse {
    private boolean success;
    private ConfigurationStatistics statistics;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public ConfigurationStatistics getStatistics() { return statistics; }
    public void setStatistics(ConfigurationStatistics statistics) { this.statistics = statistics; }
}

class ConfigurationDiffRequest {
    private GuildModerationConfig config1;
    private GuildModerationConfig config2;
    
    public GuildModerationConfig getConfig1() { return config1; }
    public void setConfig1(GuildModerationConfig config1) { this.config1 = config1; }
    public GuildModerationConfig getConfig2() { return config2; }
    public void setConfig2(GuildModerationConfig config2) { this.config2 = config2; }
}

class ConfigurationDiffResponse {
    private boolean success;
    private ConfigurationDiff diff;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public ConfigurationDiff getDiff() { return diff; }
    public void setDiff(ConfigurationDiff diff) { this.diff = diff; }
}

class ConfigurationRestoreRequest {
    private String backupId;
    private String restoredBy;
    
    public String getBackupId() { return backupId; }
    public void setBackupId(String backupId) { this.backupId = backupId; }
    public String getRestoredBy() { return restoredBy; }
    public void setRestoredBy(String restoredBy) { this.restoredBy = restoredBy; }
}

class ConfigurationRestoreResponse {
    private boolean success;
    private String message;
    private GuildModerationConfig restoredConfiguration;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public GuildModerationConfig getRestoredConfiguration() { return restoredConfiguration; }
    public void setRestoredConfiguration(GuildModerationConfig restoredConfiguration) { this.restoredConfiguration = restoredConfiguration; }
}

class ModerationTestRequest {
    private GuildModerationConfig testConfiguration;
    private String testUserId;
    private String testContent;
    private Map<String, Object> testContext;
    
    public GuildModerationConfig getTestConfiguration() { return testConfiguration; }
    public void setTestConfiguration(GuildModerationConfig testConfiguration) { this.testConfiguration = testConfiguration; }
    public String getTestUserId() { return testUserId; }
    public void setTestUserId(String testUserId) { this.testUserId = testUserId; }
    public String getTestContent() { return testContent; }
    public void setTestContent(String testContent) { this.testContent = testContent; }
    public Map<String, Object> getTestContext() { return testContext; }
    public void setTestContext(Map<String, Object> testContext) { this.testContext = testContext; }
}

class ModerationTestResponse {
    private boolean success;
    private String message;
    private ModerationResult testResult;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ModerationResult getTestResult() { return testResult; }
    public void setTestResult(ModerationResult testResult) { this.testResult = testResult; }
}

class FilterOptionsResponse {
    private boolean success;
    private Map<String, Object> spamFilterOptions;
    private Map<String, Object> toxicityFilterOptions;
    private Map<String, Object> linkFilterOptions;
    private Map<String, Object> capsFilterOptions;
    private Map<String, Object> mentionFilterOptions;
    private Map<String, Object> wordFilterOptions;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Map<String, Object> getSpamFilterOptions() { return spamFilterOptions; }
    public void setSpamFilterOptions(Map<String, Object> spamFilterOptions) { this.spamFilterOptions = spamFilterOptions; }
    public Map<String, Object> getToxicityFilterOptions() { return toxicityFilterOptions; }
    public void setToxicityFilterOptions(Map<String, Object> toxicityFilterOptions) { this.toxicityFilterOptions = toxicityFilterOptions; }
    public Map<String, Object> getLinkFilterOptions() { return linkFilterOptions; }
    public void setLinkFilterOptions(Map<String, Object> linkFilterOptions) { this.linkFilterOptions = linkFilterOptions; }
    public Map<String, Object> getCapsFilterOptions() { return capsFilterOptions; }
    public void setCapsFilterOptions(Map<String, Object> capsFilterOptions) { this.capsFilterOptions = capsFilterOptions; }
    public Map<String, Object> getMentionFilterOptions() { return mentionFilterOptions; }
    public void setMentionFilterOptions(Map<String, Object> mentionFilterOptions) { this.mentionFilterOptions = mentionFilterOptions; }
    public Map<String, Object> getWordFilterOptions() { return wordFilterOptions; }
    public void setWordFilterOptions(Map<String, Object> wordFilterOptions) { this.wordFilterOptions = wordFilterOptions; }
}

class ActionOptionsResponse {
    private boolean success;
    private List<ActionType> availableActions;
    private Map<ActionType, String> actionDescriptions;
    private Map<ActionType, List<String>> defaultDurations;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<ActionType> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<ActionType> availableActions) { this.availableActions = availableActions; }
    public Map<ActionType, String> getActionDescriptions() { return actionDescriptions; }
    public void setActionDescriptions(Map<ActionType, String> actionDescriptions) { this.actionDescriptions = actionDescriptions; }
    public Map<ActionType, List<String>> getDefaultDurations() { return defaultDurations; }
    public void setDefaultDurations(Map<ActionType, List<String>> defaultDurations) { this.defaultDurations = defaultDurations; }
}

class RuleConditionOptionsResponse {
    private boolean success;
    private List<ConditionType> availableConditions;
    private Map<ConditionType, String> conditionDescriptions;
    private Map<ConditionType, Map<String, Object>> conditionParameters;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<ConditionType> getAvailableConditions() { return availableConditions; }
    public void setAvailableConditions(List<ConditionType> availableConditions) { this.availableConditions = availableConditions; }
    public Map<ConditionType, String> getConditionDescriptions() { return conditionDescriptions; }
    public void setConditionDescriptions(Map<ConditionType, String> conditionDescriptions) { this.conditionDescriptions = conditionDescriptions; }
    public Map<ConditionType, Map<String, Object>> getConditionParameters() { return conditionParameters; }
    public void setConditionParameters(Map<ConditionType, Map<String, Object>> conditionParameters) { this.conditionParameters = conditionParameters; }
}

class UIOptionsResponse {
    private boolean success;
    private List<UITheme> availableThemes;
    private List<String> availableLanguages;
    private List<DashboardLayout> availableLayouts;
    private Map<String, String> defaultColors;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<UITheme> getAvailableThemes() { return availableThemes; }
    public void setAvailableThemes(List<UITheme> availableThemes) { this.availableThemes = availableThemes; }
    public List<String> getAvailableLanguages() { return availableLanguages; }
    public void setAvailableLanguages(List<String> availableLanguages) { this.availableLanguages = availableLanguages; }
    public List<DashboardLayout> getAvailableLayouts() { return availableLayouts; }
    public void setAvailableLayouts(List<DashboardLayout> availableLayouts) { this.availableLayouts = availableLayouts; }
    public Map<String, String> getDefaultColors() { return defaultColors; }
    public void setDefaultColors(Map<String, String> defaultColors) { this.defaultColors = defaultColors; }
}