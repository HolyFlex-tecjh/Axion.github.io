package com.axion.bot.moderation;

// Removed Caffeine cache dependency
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.*;

/**
 * Configuration Management System for Customizable Moderation
 * Handles saving, loading, importing, exporting, and validating configurations
 */
public class ModerationConfigurationManager {
    
    private final Map<String, GuildModerationConfig> configCache;
    private final ConfigurationValidator validator;
    private final ConfigurationPersistence persistence;
    private final ConfigurationTemplateManager templateManager;
    private final ConfigurationMigrationManager migrationManager;
    
    // Configuration change listeners
    private final Map<String, List<ConfigurationChangeListener>> changeListeners;
    
    // Configuration backup system
    private final ConfigurationBackupManager backupManager;
    
    public ModerationConfigurationManager() {
        this.configCache = new ConcurrentHashMap<>();
            
        this.validator = new ConfigurationValidator();
        this.persistence = new ConfigurationPersistence();
        this.templateManager = new ConfigurationTemplateManager();
        this.migrationManager = new ConfigurationMigrationManager();
        this.changeListeners = new ConcurrentHashMap<>();
        this.backupManager = new ConfigurationBackupManager();
    }
    
    /**
     * Load configuration for a guild
     */
    public CompletableFuture<GuildModerationConfig> loadConfiguration(String guildId) {
        return CompletableFuture.supplyAsync(() -> {
            // Try cache first
            GuildModerationConfig cached = configCache.get(guildId);
            if (cached != null) {
                return cached;
            }
            
            // Load from persistence
            GuildModerationConfig config = persistence.loadConfiguration(guildId);
            if (config == null) {
                config = createDefaultConfiguration(guildId);
                persistence.saveConfiguration(config);
            }
            
            // Validate and migrate if necessary
            config = migrationManager.migrateIfNeeded(config);
            ValidationResult validation = validator.validateConfiguration(config);
            if (!validation.isValid()) {
                throw new ConfigurationException("Invalid configuration: " + validation.getErrors());
            }
            
            configCache.put(guildId, config);
            return config;
        });
    }
    
    /**
     * Save configuration for a guild
     */
    public CompletableFuture<ConfigurationSaveResult> saveConfiguration(GuildModerationConfig config, String modifiedBy) {
        return CompletableFuture.supplyAsync(() -> {
            // Validate configuration
            ValidationResult validation = validator.validateConfiguration(config);
            if (!validation.isValid()) {
                return new ConfigurationSaveResult(false, "Validation failed: " + validation.getErrors(), null);
            }
            
            // Create backup before saving
            GuildModerationConfig oldConfig = configCache.get(config.getGuildId());
            if (oldConfig != null) {
                backupManager.createBackup(oldConfig, "pre_update_" + Instant.now().toEpochMilli());
            }
            
            // Update configuration with metadata
            GuildModerationConfig updatedConfig = GuildModerationConfig.builder(config.getGuildId())
                .copyFrom(config)
                .withUpdatedBy(modifiedBy)
                .build();
            
            // Save to persistence
            boolean saved = persistence.saveConfiguration(updatedConfig);
            if (!saved) {
                return new ConfigurationSaveResult(false, "Failed to save to persistence layer", null);
            }
            
            // Update cache
            configCache.put(config.getGuildId(), updatedConfig);
            
            // Notify listeners
            notifyConfigurationChanged(config.getGuildId(), oldConfig, updatedConfig, modifiedBy);
            
            return new ConfigurationSaveResult(true, "Configuration saved successfully", updatedConfig);
        });
    }
    
    /**
     * Export configuration to various formats
     */
    public CompletableFuture<ConfigurationExportResult> exportConfiguration(String guildId, ExportFormat format, ExportOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            GuildModerationConfig config = configCache.get(guildId);
            if (config == null) {
                config = persistence.loadConfiguration(guildId);
                if (config == null) {
                    return new ConfigurationExportResult(false, "Configuration not found", null, null);
                }
            }
            
            try {
                ConfigurationExporter exporter = getExporter(format);
                ExportedConfiguration exported = exporter.export(config, options);
                
                return new ConfigurationExportResult(true, "Export successful", exported.getData(), exported.getMetadata());
            } catch (Exception e) {
                return new ConfigurationExportResult(false, "Export failed: " + e.getMessage(), null, null);
            }
        });
    }
    
    /**
     * Import configuration from various formats
     */
    public CompletableFuture<ConfigurationImportResult> importConfiguration(String guildId, byte[] data, ImportFormat format, ImportOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ConfigurationImporter importer = getImporter(format);
                GuildModerationConfig importedConfig = importer.importConfiguration(guildId, data, options);
                
                // Validate imported configuration
                ValidationResult validation = validator.validateConfiguration(importedConfig);
                if (!validation.isValid()) {
                    return new ConfigurationImportResult(false, "Imported configuration is invalid: " + validation.getErrors(), null, validation.getWarnings());
                }
                
                // Apply import options
                GuildModerationConfig finalConfig = applyImportOptions(importedConfig, options);
                
                return new ConfigurationImportResult(true, "Import successful", finalConfig, validation.getWarnings());
            } catch (Exception e) {
                return new ConfigurationImportResult(false, "Import failed: " + e.getMessage(), null, Collections.emptyList());
            }
        });
    }
    
    /**
     * Create configuration from template
     */
    public CompletableFuture<GuildModerationConfig> createFromTemplate(String guildId, String templateId, Map<String, Object> customizations) {
        return CompletableFuture.supplyAsync(() -> {
            ConfigurationTemplate template = templateManager.getTemplate(templateId);
            if (template == null) {
                throw new ConfigurationException("Template not found: " + templateId);
            }
            
            GuildModerationConfig config = templateManager.applyTemplate(guildId, template, customizations);
            
            // Validate generated configuration
            ValidationResult validation = validator.validateConfiguration(config);
            if (!validation.isValid()) {
                throw new ConfigurationException("Generated configuration is invalid: " + validation.getErrors());
            }
            
            return config;
        });
    }
    
    /**
     * Clone configuration from another guild
     */
    public CompletableFuture<ConfigurationCloneResult> cloneConfiguration(String sourceGuildId, String targetGuildId, CloneOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            GuildModerationConfig sourceConfig = persistence.loadConfiguration(sourceGuildId);
            if (sourceConfig == null) {
                return new ConfigurationCloneResult(false, "Source configuration not found", null);
            }
            
            try {
                GuildModerationConfig clonedConfig = cloneConfigurationInternal(sourceConfig, targetGuildId, options);
                
                // Validate cloned configuration
                ValidationResult validation = validator.validateConfiguration(clonedConfig);
                if (!validation.isValid()) {
                    return new ConfigurationCloneResult(false, "Cloned configuration is invalid: " + validation.getErrors(), null);
                }
                
                return new ConfigurationCloneResult(true, "Configuration cloned successfully", clonedConfig);
            } catch (Exception e) {
                return new ConfigurationCloneResult(false, "Clone failed: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Get configuration diff between two versions
     */
    public ConfigurationDiff getConfigurationDiff(GuildModerationConfig config1, GuildModerationConfig config2) {
        ConfigurationDiffCalculator calculator = new ConfigurationDiffCalculator();
        return calculator.calculateDiff(config1, config2);
    }
    
    /**
     * Get configuration history for a guild
     */
    public List<ConfigurationHistoryEntry> getConfigurationHistory(String guildId, int limit) {
        return persistence.getConfigurationHistory(guildId, limit);
    }
    
    /**
     * Restore configuration from backup
     */
    public CompletableFuture<ConfigurationRestoreResult> restoreFromBackup(String guildId, String backupId, String restoredBy) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                GuildModerationConfig backup = backupManager.getBackup(guildId, backupId);
                if (backup == null) {
                    return new ConfigurationRestoreResult(false, "Backup not found", null);
                }
                
                // Create current backup before restoring
                GuildModerationConfig currentConfig = configCache.get(guildId);
                if (currentConfig != null) {
                    backupManager.createBackup(currentConfig, "pre_restore_" + Instant.now().toEpochMilli());
                }
                
                // Restore configuration
                GuildModerationConfig restoredConfig = GuildModerationConfig.builder(guildId)
                    .copyFrom(backup)
                    .withUpdatedBy(restoredBy)
                    .build();
                
                // Save restored configuration
                boolean saved = persistence.saveConfiguration(restoredConfig);
                if (!saved) {
                    return new ConfigurationRestoreResult(false, "Failed to save restored configuration", null);
                }
                
                // Update cache
                configCache.put(guildId, restoredConfig);
                
                // Notify listeners
                notifyConfigurationChanged(guildId, currentConfig, restoredConfig, restoredBy);
                
                return new ConfigurationRestoreResult(true, "Configuration restored successfully", restoredConfig);
            } catch (Exception e) {
                return new ConfigurationRestoreResult(false, "Restore failed: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Get available configuration templates
     */
    public List<ConfigurationTemplate> getAvailableTemplates() {
        return templateManager.getAvailableTemplates();
    }
    
    /**
     * Register configuration change listener
     */
    public void addConfigurationChangeListener(String guildId, ConfigurationChangeListener listener) {
        changeListeners.computeIfAbsent(guildId, k -> new ArrayList<>()).add(listener);
    }
    
    /**
     * Remove configuration change listener
     */
    public void removeConfigurationChangeListener(String guildId, ConfigurationChangeListener listener) {
        List<ConfigurationChangeListener> listeners = changeListeners.get(guildId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Validate configuration without saving
     */
    public ValidationResult validateConfiguration(GuildModerationConfig config) {
        return validator.validateConfiguration(config);
    }
    
    /**
     * Get configuration statistics
     */
    public ConfigurationStatistics getConfigurationStatistics(String guildId) {
        return persistence.getConfigurationStatistics(guildId);
    }
    
    // Private helper methods
    
    private GuildModerationConfig createDefaultConfiguration(String guildId) {
        // Create a basic GuildModerationConfig instead of using CustomizableModerationSystem
        return GuildModerationConfig.builder(guildId).build();
    }
    
    private void notifyConfigurationChanged(String guildId, GuildModerationConfig oldConfig, GuildModerationConfig newConfig, String modifiedBy) {
        List<ConfigurationChangeListener> listeners = changeListeners.get(guildId);
        if (listeners != null) {
            ConfigurationChangeEvent event = new ConfigurationChangeEvent(guildId, oldConfig, newConfig, modifiedBy, Instant.now());
            listeners.forEach(listener -> {
                try {
                    listener.onConfigurationChanged(event);
                } catch (Exception e) {
                    // Log error but don't fail the operation
                    System.err.println("Error notifying configuration change listener: " + e.getMessage());
                }
            });
        }
    }
    
    private ConfigurationExporter getExporter(ExportFormat format) {
        switch (format) {
            case JSON: return new JsonConfigurationExporter();
            case YAML: return new YamlConfigurationExporter();
            case XML: return new XmlConfigurationExporter();
            case BINARY: return new BinaryConfigurationExporter();
            default: throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }
    
    private ConfigurationImporter getImporter(ImportFormat format) {
        switch (format) {
            case JSON: return new JsonConfigurationImporter();
            case YAML: return new YamlConfigurationImporter();
            case XML: return new XmlConfigurationImporter();
            case BINARY: return new BinaryConfigurationImporter();
            default: throw new IllegalArgumentException("Unsupported import format: " + format);
        }
    }
    
    private GuildModerationConfig applyImportOptions(GuildModerationConfig config, ImportOptions options) {
        // For now, return a copy of the config since the main GuildModerationConfig
        // doesn't have the granular configuration methods
        return GuildModerationConfig.builder(config.getGuildId())
            .copyFrom(config)
            .build();
    }
    
    private GuildModerationConfig cloneConfigurationInternal(GuildModerationConfig source, String targetGuildId, CloneOptions options) {
        // Clone the configuration to the new guild ID
        return GuildModerationConfig.builder(targetGuildId)
            .copyFrom(source)
            .build();
    }
}

/**
 * Configuration validation system
 */
class ConfigurationValidator {
    
    public ValidationResult validateConfiguration(GuildModerationConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Use the built-in validation from GuildModerationConfig
        List<String> configErrors = config.validate();
        errors.addAll(configErrors);
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

}

/**
 * Configuration persistence layer
 */
class ConfigurationPersistence {
    
    private final String configDirectory = "config/moderation";
    
    public GuildModerationConfig loadConfiguration(String guildId) {
        try {
            Path configPath = Paths.get(configDirectory, guildId + ".json");
            if (!Files.exists(configPath)) {
                return null;
            }
            
            String json = Files.readString(configPath);
            return deserializeConfiguration(json);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load configuration for guild " + guildId, e);
        }
    }
    
    public boolean saveConfiguration(GuildModerationConfig config) {
        try {
            Path configDir = Paths.get(configDirectory);
            Files.createDirectories(configDir);
            
            Path configPath = configDir.resolve(config.getGuildId() + ".json");
            String json = serializeConfiguration(config);
            Files.writeString(configPath, json);
            
            // Save to history
            saveToHistory(config);
            
            return true;
        } catch (Exception e) {
            System.err.println("Failed to save configuration for guild " + config.getGuildId() + ": " + e.getMessage());
            return false;
        }
    }
    
    public List<ConfigurationHistoryEntry> getConfigurationHistory(String guildId, int limit) {
        try {
            Path historyPath = Paths.get(configDirectory, "history", guildId);
            if (!Files.exists(historyPath)) {
                return new ArrayList<>();
            }
            
            return Files.list(historyPath)
                .filter(Files::isRegularFile)
                .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                .limit(limit)
                .map(this::loadHistoryEntry)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Failed to load configuration history for guild " + guildId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public ConfigurationStatistics getConfigurationStatistics(String guildId) {
        // Placeholder implementation
        return new ConfigurationStatistics(guildId, 0, Instant.now(), Instant.now(), 0, 0);
    }
    
    private void saveToHistory(GuildModerationConfig config) {
        try {
            Path historyDir = Paths.get(configDirectory, "history", config.getGuildId());
            Files.createDirectories(historyDir);
            
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            Path historyPath = historyDir.resolve(timestamp + ".json");
            
            ConfigurationHistoryEntry entry = new ConfigurationHistoryEntry(
                timestamp,
                config,
                config.getUpdatedBy(),
                Instant.now(),
                "Configuration update"
            );
            
            String json = serializeHistoryEntry(entry);
            Files.writeString(historyPath, json);
        } catch (Exception e) {
            System.err.println("Failed to save configuration history: " + e.getMessage());
        }
    }
    
    private ConfigurationHistoryEntry loadHistoryEntry(Path path) {
        try {
            String json = Files.readString(path);
            return deserializeHistoryEntry(json);
        } catch (Exception e) {
            System.err.println("Failed to load history entry: " + e.getMessage());
            return null;
        }
    }
    
    private String serializeConfiguration(GuildModerationConfig config) {
        // Placeholder JSON serialization
        return "{\"guildId\":\"" + config.getGuildId() + "\"}";
    }
    
    private GuildModerationConfig deserializeConfiguration(String json) {
        // Placeholder JSON deserialization
        return GuildModerationConfig.builder("placeholder").build();
    }
    
    private String serializeHistoryEntry(ConfigurationHistoryEntry entry) {
        // Placeholder JSON serialization
        return "{\"id\":\"" + entry.getId() + "\"}";
    }
    
    private ConfigurationHistoryEntry deserializeHistoryEntry(String json) {
        // Placeholder JSON deserialization
        return new ConfigurationHistoryEntry("placeholder", null, "system", Instant.now(), "placeholder");
    }
}

/**
 * Configuration template management
 */
class ConfigurationTemplateManager {
    
    private final Map<String, ConfigurationTemplate> templates = new ConcurrentHashMap<>();
    
    public ConfigurationTemplateManager() {
        loadDefaultTemplates();
    }
    
    public ConfigurationTemplate getTemplate(String templateId) {
        return templates.get(templateId);
    }
    
    public List<ConfigurationTemplate> getAvailableTemplates() {
        return new ArrayList<>(templates.values());
    }
    
    public GuildModerationConfig applyTemplate(String guildId, ConfigurationTemplate template, Map<String, Object> customizations) {
        // Apply template with customizations
        GuildModerationConfig.Builder builder = GuildModerationConfig.builder(guildId);
        
        // Apply template configuration using available methods from main GuildModerationConfig
        // Since the main GuildModerationConfig doesn't have these granular config methods,
        // we'll create a basic configuration and apply customizations
        
        // Apply customizations
        applyCustomizations(builder, customizations);
        
        return builder.build();
    }
    
    private void loadDefaultTemplates() {
        // Basic template
        templates.put("basic", new ConfigurationTemplate(
            "basic",
            "Basic Moderation",
            "Basic moderation setup with essential features",
            TemplateCategory.BASIC,
            createBasicFilterConfig(),
            createBasicRules(),
            createBasicThresholdConfig(),
            createBasicActionConfig(),
            createBasicUIConfig()
        ));
        
        // Strict template
        templates.put("strict", new ConfigurationTemplate(
            "strict",
            "Strict Moderation",
            "Strict moderation with low tolerance for violations",
            TemplateCategory.STRICT,
            createStrictFilterConfig(),
            createStrictRules(),
            createStrictThresholdConfig(),
            createStrictActionConfig(),
            createBasicUIConfig()
        ));
        
        // Lenient template
        templates.put("lenient", new ConfigurationTemplate(
            "lenient",
            "Lenient Moderation",
            "Lenient moderation with higher tolerance",
            TemplateCategory.LENIENT,
            createLenientFilterConfig(),
            createLenientRules(),
            createLenientThresholdConfig(),
            createLenientActionConfig(),
            createBasicUIConfig()
        ));
        
        // Gaming community template
        templates.put("gaming", new ConfigurationTemplate(
            "gaming",
            "Gaming Community",
            "Optimized for gaming communities",
            TemplateCategory.COMMUNITY,
            createGamingFilterConfig(),
            createGamingRules(),
            createGamingThresholdConfig(),
            createGamingActionConfig(),
            createGamingUIConfig()
        ));
    }
    
    private void applyCustomizations(GuildModerationConfig.Builder builder, Map<String, Object> customizations) {
        // Apply customizations to the builder
        // This would involve parsing the customizations map and applying changes
        // Placeholder implementation
    }
    
    // Template creation methods (placeholder implementations)
    private FilterConfig createBasicFilterConfig() {
        return new FilterConfig.Builder().build();
    }
    
    private List<CustomRule> createBasicRules() {
        return new ArrayList<>();
    }
    
    private ThresholdConfig createBasicThresholdConfig() {
        return new ThresholdConfig(new HashMap<>(), true, true);
    }
    
    private ActionConfig createBasicActionConfig() {
        return new ActionConfig(new HashMap<>(), true, true, new ArrayList<>());
    }
    
    private UIConfig createBasicUIConfig() {
        return new UIConfig.Builder().build();
    }
    
    private FilterConfig createStrictFilterConfig() {
        return new FilterConfig.Builder().build();
    }
    
    private List<CustomRule> createStrictRules() {
        return new ArrayList<>();
    }
    
    private ThresholdConfig createStrictThresholdConfig() {
        return new ThresholdConfig(new HashMap<>(), true, true);
    }
    
    private ActionConfig createStrictActionConfig() {
        return new ActionConfig(new HashMap<>(), true, true, new ArrayList<>());
    }
    
    private FilterConfig createLenientFilterConfig() {
        return new FilterConfig.Builder().build();
    }
    
    private List<CustomRule> createLenientRules() {
        return new ArrayList<>();
    }
    
    private ThresholdConfig createLenientThresholdConfig() {
        return new ThresholdConfig(new HashMap<>(), true, true);
    }
    
    private ActionConfig createLenientActionConfig() {
        return new ActionConfig(new HashMap<>(), true, true, new ArrayList<>());
    }
    
    private FilterConfig createGamingFilterConfig() {
        return new FilterConfig.Builder().build();
    }
    
    private List<CustomRule> createGamingRules() {
        return new ArrayList<>();
    }
    
    private ThresholdConfig createGamingThresholdConfig() {
        return new ThresholdConfig(new HashMap<>(), true, true);
    }
    
    private ActionConfig createGamingActionConfig() {
        return new ActionConfig(new HashMap<>(), true, true, new ArrayList<>());
    }
    
    private UIConfig createGamingUIConfig() {
        return new UIConfig.Builder().build();
    }
}

/**
 * Configuration migration system
 */
class ConfigurationMigrationManager {
    
    private static final String CURRENT_VERSION = "1.0";
    
    public GuildModerationConfig migrateIfNeeded(GuildModerationConfig config) {
        // Check if migration is needed
        String configVersion = getConfigVersion(config);
        if (CURRENT_VERSION.equals(configVersion)) {
            return config;
        }
        
        // Perform migration
        return performMigration(config, configVersion, CURRENT_VERSION);
    }
    
    private String getConfigVersion(GuildModerationConfig config) {
        // Extract version from custom settings or default to "1.0"
        return config.getCustomSettings().getOrDefault("version", "1.0").toString();
    }
    
    private GuildModerationConfig performMigration(GuildModerationConfig config, String fromVersion, String toVersion) {
        // Placeholder migration logic
        Map<String, Object> customSettings = new HashMap<>(config.getCustomSettings());
        customSettings.put("version", toVersion);
        customSettings.put("migrated_from", fromVersion);
        customSettings.put("migration_timestamp", Instant.now().toString());
        
        return GuildModerationConfig.builder(config.getGuildId())
            .copyFrom(config)
            .build();
    }
}

/**
 * Configuration backup management
 */
class ConfigurationBackupManager {
    
    private final String backupDirectory = "config/moderation/backups";
    
    public void createBackup(GuildModerationConfig config, String backupId) {
        try {
            Path backupDir = Paths.get(backupDirectory, config.getGuildId());
            Files.createDirectories(backupDir);
            
            Path backupPath = backupDir.resolve(backupId + ".json");
            String json = serializeConfiguration(config);
            Files.writeString(backupPath, json);
        } catch (Exception e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }
    
    public GuildModerationConfig getBackup(String guildId, String backupId) {
        try {
            Path backupPath = Paths.get(backupDirectory, guildId, backupId + ".json");
            if (!Files.exists(backupPath)) {
                return null;
            }
            
            String json = Files.readString(backupPath);
            return deserializeConfiguration(json);
        } catch (Exception e) {
            System.err.println("Failed to load backup: " + e.getMessage());
            return null;
        }
    }
    
    public List<String> getAvailableBackups(String guildId) {
        try {
            Path backupDir = Paths.get(backupDirectory, guildId);
            if (!Files.exists(backupDir)) {
                return new ArrayList<>();
            }
            
            return Files.list(backupDir)
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString().replace(".json", ""))
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Failed to list backups: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private String serializeConfiguration(GuildModerationConfig config) {
        // Placeholder JSON serialization
        return "{\"guildId\":\"" + config.getGuildId() + "\"}";
    }
    
    private GuildModerationConfig deserializeConfiguration(String json) {
        // Placeholder JSON deserialization
        return GuildModerationConfig.builder("placeholder").build();
    }
}

// Supporting classes and enums

class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
    }
    
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
}

class ConfigurationSaveResult {
    private final boolean success;
    private final String message;
    private final GuildModerationConfig savedConfig;
    
    public ConfigurationSaveResult(boolean success, String message, GuildModerationConfig savedConfig) {
        this.success = success;
        this.message = message;
        this.savedConfig = savedConfig;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public GuildModerationConfig getSavedConfig() { return savedConfig; }
}

class ConfigurationExportResult {
    private final boolean success;
    private final String message;
    private final byte[] data;
    private final Map<String, Object> metadata;
    
    public ConfigurationExportResult(boolean success, String message, byte[] data, Map<String, Object> metadata) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public byte[] getData() { return data; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
}

class ConfigurationImportResult {
    private final boolean success;
    private final String message;
    private final GuildModerationConfig importedConfig;
    private final List<String> warnings;
    
    public ConfigurationImportResult(boolean success, String message, GuildModerationConfig importedConfig, List<String> warnings) {
        this.success = success;
        this.message = message;
        this.importedConfig = importedConfig;
        this.warnings = new ArrayList<>(warnings);
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public GuildModerationConfig getImportedConfig() { return importedConfig; }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
}

class ConfigurationCloneResult {
    private final boolean success;
    private final String message;
    private final GuildModerationConfig clonedConfig;
    
    public ConfigurationCloneResult(boolean success, String message, GuildModerationConfig clonedConfig) {
        this.success = success;
        this.message = message;
        this.clonedConfig = clonedConfig;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public GuildModerationConfig getClonedConfig() { return clonedConfig; }
}

class ConfigurationRestoreResult {
    private final boolean success;
    private final String message;
    private final GuildModerationConfig restoredConfig;
    
    public ConfigurationRestoreResult(boolean success, String message, GuildModerationConfig restoredConfig) {
        this.success = success;
        this.message = message;
        this.restoredConfig = restoredConfig;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public GuildModerationConfig getRestoredConfig() { return restoredConfig; }
}

// ConfigurationTemplate class moved to separate file: ConfigurationTemplate.java
// TemplateCategory enum moved to separate file: TemplateCategory.java

class ConfigurationHistoryEntry {
    private final String id;
    private final GuildModerationConfig configuration;
    private final String modifiedBy;
    private final Instant timestamp;
    private final String description;
    
    public ConfigurationHistoryEntry(String id, GuildModerationConfig configuration, String modifiedBy, Instant timestamp, String description) {
        this.id = id;
        this.configuration = configuration;
        this.modifiedBy = modifiedBy;
        this.timestamp = timestamp;
        this.description = description;
    }
    
    // Getters
    public String getId() { return id; }
    public GuildModerationConfig getConfiguration() { return configuration; }
    public String getModifiedBy() { return modifiedBy; }
    public Instant getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
}

class ConfigurationStatistics {
    private final String guildId;
    private final int totalConfigurations;
    private final Instant firstConfigured;
    private final Instant lastModified;
    private final int totalRules;
    private final int activeFilters;
    
    public ConfigurationStatistics(String guildId, int totalConfigurations, Instant firstConfigured,
                                  Instant lastModified, int totalRules, int activeFilters) {
        this.guildId = guildId;
        this.totalConfigurations = totalConfigurations;
        this.firstConfigured = firstConfigured;
        this.lastModified = lastModified;
        this.totalRules = totalRules;
        this.activeFilters = activeFilters;
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public int getTotalConfigurations() { return totalConfigurations; }
    public Instant getFirstConfigured() { return firstConfigured; }
    public Instant getLastModified() { return lastModified; }
    public int getTotalRules() { return totalRules; }
    public int getActiveFilters() { return activeFilters; }
}

class ConfigurationChangeEvent {
    private final String guildId;
    private final GuildModerationConfig oldConfig;
    private final GuildModerationConfig newConfig;
    private final String modifiedBy;
    private final Instant timestamp;
    
    public ConfigurationChangeEvent(String guildId, GuildModerationConfig oldConfig, GuildModerationConfig newConfig,
                                   String modifiedBy, Instant timestamp) {
        this.guildId = guildId;
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
        this.modifiedBy = modifiedBy;
        this.timestamp = timestamp;
    }
    
    // Getters
    public String getGuildId() { return guildId; }
    public GuildModerationConfig getOldConfig() { return oldConfig; }
    public GuildModerationConfig getNewConfig() { return newConfig; }
    public String getModifiedBy() { return modifiedBy; }
    public Instant getTimestamp() { return timestamp; }
}

interface ConfigurationChangeListener {
    void onConfigurationChanged(ConfigurationChangeEvent event);
}

class ConfigurationDiff {
    private final List<ConfigurationChange> changes;
    
    public ConfigurationDiff(List<ConfigurationChange> changes) {
        this.changes = new ArrayList<>(changes);
    }
    
    public List<ConfigurationChange> getChanges() {
        return new ArrayList<>(changes);
    }
}

// ConfigurationChange class moved to separate file: ConfigurationChange.java

// ChangeType enum moved to separate file: ChangeType.java

class ConfigurationDiffCalculator {
    public ConfigurationDiff calculateDiff(GuildModerationConfig config1, GuildModerationConfig config2) {
        List<ConfigurationChange> changes = new ArrayList<>();
        // Placeholder implementation
        return new ConfigurationDiff(changes);
    }
}

// Export/Import related classes

// ExportFormat and ImportFormat enums moved to separate files

// ExportOptions class moved to separate file: ExportOptions.java

// ImportOptions class moved to separate file: ImportOptions.java



class ExportedConfiguration {
    private final byte[] data;
    private final Map<String, Object> metadata;
    
    public ExportedConfiguration(byte[] data, Map<String, Object> metadata) {
        this.data = data;
        this.metadata = new HashMap<>(metadata);
    }
    
    public byte[] getData() { return data; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
}

// Placeholder exporter/importer interfaces and implementations

interface ConfigurationExporter {
    ExportedConfiguration export(GuildModerationConfig config, ExportOptions options) throws Exception;
}

interface ConfigurationImporter {
    GuildModerationConfig importConfiguration(String guildId, byte[] data, ImportOptions options) throws Exception;
}

class JsonConfigurationExporter implements ConfigurationExporter {
    public ExportedConfiguration export(GuildModerationConfig config, ExportOptions options) throws Exception {
        // Placeholder JSON export
        String json = "{\"guildId\":\"" + config.getGuildId() + "\"}";
        return new ExportedConfiguration(json.getBytes(), new HashMap<>());
    }
}

class YamlConfigurationExporter implements ConfigurationExporter {
    public ExportedConfiguration export(GuildModerationConfig config, ExportOptions options) throws Exception {
        // Placeholder YAML export
        String yaml = "guildId: " + config.getGuildId();
        return new ExportedConfiguration(yaml.getBytes(), new HashMap<>());
    }
}

class XmlConfigurationExporter implements ConfigurationExporter {
    public ExportedConfiguration export(GuildModerationConfig config, ExportOptions options) throws Exception {
        // Placeholder XML export
        String xml = "<config><guildId>" + config.getGuildId() + "</guildId></config>";
        return new ExportedConfiguration(xml.getBytes(), new HashMap<>());
    }
}

class BinaryConfigurationExporter implements ConfigurationExporter {
    public ExportedConfiguration export(GuildModerationConfig config, ExportOptions options) throws Exception {
        // Placeholder binary export
        return new ExportedConfiguration(new byte[0], new HashMap<>());
    }
}

class JsonConfigurationImporter implements ConfigurationImporter {
    public GuildModerationConfig importConfiguration(String guildId, byte[] data, ImportOptions options) throws Exception {
        // Placeholder JSON import
        return GuildModerationConfig.builder(guildId).build();
    }
}

class YamlConfigurationImporter implements ConfigurationImporter {
    public GuildModerationConfig importConfiguration(String guildId, byte[] data, ImportOptions options) throws Exception {
        // Placeholder YAML import
        return GuildModerationConfig.builder(guildId).build();
    }
}

class XmlConfigurationImporter implements ConfigurationImporter {
    public GuildModerationConfig importConfiguration(String guildId, byte[] data, ImportOptions options) throws Exception {
        // Placeholder XML import
        return GuildModerationConfig.builder(guildId).build();
    }
}

class BinaryConfigurationImporter implements ConfigurationImporter {
    public GuildModerationConfig importConfiguration(String guildId, byte[] data, ImportOptions options) throws Exception {
        // Placeholder binary import
        return GuildModerationConfig.builder(guildId).build();
    }
}

// Missing classes and enums

class FilterConfig {
    public static class Builder {
        public FilterConfig build() {
            return new FilterConfig();
        }
    }
}

class ThresholdConfig {
    private final Map<String, Object> thresholds;
    private final boolean enabled;
    private final boolean autoScale;
    
    public ThresholdConfig(Map<String, Object> thresholds, boolean enabled, boolean autoScale) {
        this.thresholds = thresholds;
        this.enabled = enabled;
        this.autoScale = autoScale;
    }
}

class ActionConfig {
    private final Map<String, Object> actions;
    private final boolean enabled;
    private final boolean autoExecute;
    private final List<String> customActions;
    
    public ActionConfig(Map<String, Object> actions, boolean enabled, boolean autoExecute, List<String> customActions) {
        this.actions = actions;
        this.enabled = enabled;
        this.autoExecute = autoExecute;
        this.customActions = customActions;
    }
}

class UIConfig {
    public static class Builder {
        public UIConfig build() {
            return new UIConfig();
        }
    }
}

class CustomRule {
    // Placeholder implementation
}

class ConfigurationTemplate {
    private final String id;
    private final String name;
    private final String description;
    private final TemplateCategory category;
    private final FilterConfig filterConfig;
    private final List<CustomRule> rules;
    private final ThresholdConfig thresholdConfig;
    private final ActionConfig actionConfig;
    private final UIConfig uiConfig;
    
    public ConfigurationTemplate(String id, String name, String description, TemplateCategory category,
                               FilterConfig filterConfig, List<CustomRule> rules, ThresholdConfig thresholdConfig,
                               ActionConfig actionConfig, UIConfig uiConfig) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.filterConfig = filterConfig;
        this.rules = rules;
        this.thresholdConfig = thresholdConfig;
        this.actionConfig = actionConfig;
        this.uiConfig = uiConfig;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TemplateCategory getCategory() { return category; }
}

enum TemplateCategory {
    BASIC, STRICT, LENIENT, COMMUNITY
}

enum ExportFormat {
    JSON, YAML, XML, BINARY
}

enum ImportFormat {
    JSON, YAML, XML, BINARY
}

class ExportOptions {
    // Placeholder implementation
}

class ImportOptions {
    // Placeholder implementation
}

class CloneOptions {
    // Placeholder implementation
}

class ConfigurationChange {
    // Placeholder implementation
}

enum ChangeType {
    ADDED, MODIFIED, REMOVED
}