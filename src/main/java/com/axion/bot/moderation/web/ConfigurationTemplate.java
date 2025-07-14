package com.axion.bot.moderation.web;

/**
 * Web DTO for configuration template
 */
public class ConfigurationTemplate {
    private String id;
    private String name;
    private String description;
    private String category;
    
    public ConfigurationTemplate() {}
    
    public ConfigurationTemplate(String id, String name, String description, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}