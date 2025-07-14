package com.axion.bot.moderation;

/**
 * Options for importing moderation configurations
 */
public class ImportOptions {
    private final boolean importFilters;
    private final boolean importRules;
    private final boolean importThresholds;
    private final boolean importActions;
    private final boolean importUI;
    private final boolean importCustomSettings;
    private final boolean overwriteExisting;
    
    public ImportOptions(boolean importFilters, boolean importRules, boolean importThresholds,
                        boolean importActions, boolean importUI, boolean importCustomSettings, boolean overwriteExisting) {
        this.importFilters = importFilters;
        this.importRules = importRules;
        this.importThresholds = importThresholds;
        this.importActions = importActions;
        this.importUI = importUI;
        this.importCustomSettings = importCustomSettings;
        this.overwriteExisting = overwriteExisting;
    }
    
    public boolean isImportFilters() { return importFilters; }
    public boolean isImportRules() { return importRules; }
    public boolean isImportThresholds() { return importThresholds; }
    public boolean isImportActions() { return importActions; }
    public boolean isImportUI() { return importUI; }
    public boolean isImportCustomSettings() { return importCustomSettings; }
    public boolean isOverwriteExisting() { return overwriteExisting; }
}