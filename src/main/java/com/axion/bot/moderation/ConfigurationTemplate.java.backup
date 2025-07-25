package com.axion.bot.moderation;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration template for moderation settings
 */
public class ConfigurationTemplate {
    private final String id;
    private final String name;
    private final String description;
    private final TemplateCategory category;
    private final FilterConfig filterConfig;
    private final List<CustomRule> customRules;
    private final ThresholdConfig thresholdConfig;
    private final ActionConfig actionConfig;
    private final UIConfig uiConfig;
    
    public ConfigurationTemplate(String id, String name, String description, TemplateCategory category,
                                FilterConfig filterConfig, List<CustomRule> customRules,
                                ThresholdConfig thresholdConfig, ActionConfig actionConfig, UIConfig uiConfig) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.filterConfig = filterConfig;
        this.customRules = new ArrayList<>(customRules);
        this.thresholdConfig = thresholdConfig;
        this.actionConfig = actionConfig;
        this.uiConfig = uiConfig;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TemplateCategory getCategory() { return category; }
    public FilterConfig getFilterConfig() { return filterConfig; }
    public List<CustomRule> getCustomRules() { return new ArrayList<>(customRules); }
    public ThresholdConfig getThresholdConfig() { return thresholdConfig; }
    public ActionConfig getActionConfig() { return actionConfig; }
    public UIConfig getUiConfig() { return uiConfig; }
}