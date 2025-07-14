package com.axion.bot.web;

import com.axion.bot.database.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * REST API Controller for Moderation Configuration Management
 * Provides endpoints for the moderation dashboard
 */
public class ModerationConfigurationController {
    private static final Logger logger = Logger.getLogger(ModerationConfigurationController.class.getName());
    
    private final DatabaseService databaseService;
    private final ObjectMapper objectMapper;
    private HttpServer server;
    
    public ModerationConfigurationController(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.objectMapper = new ObjectMapper();
        initializeDatabase();
    }
    
    /**
     * Start the web server for the API
     */
    public void startServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // API endpoints
            server.createContext("/api/moderation/config/guild", new GuildConfigHandler());
            server.createContext("/api/moderation/config/filters/options", new FilterOptionsHandler());
            server.createContext("/api/moderation/config/actions/options", new ActionOptionsHandler());
            server.createContext("/api/moderation/config/rules/conditions", new RuleConditionsHandler());
            server.createContext("/api/moderation/config/ui/options", new UIOptionsHandler());
            server.createContext("/api/moderation/config/templates", new TemplatesHandler());
            
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            
            logger.info("Moderation API server started on port " + port);
        } catch (IOException e) {
            logger.severe("Failed to start API server: " + e.getMessage());
        }
    }
    
    /**
     * Stop the web server
     */
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            logger.info("Moderation API server stopped");
        }
    }
    
    /**
     * Initialize database tables for moderation configurations
     */
    private void initializeDatabase() {
        try (Connection conn = databaseService.getConnection()) {
            // Create moderation_configs table
            String createConfigsTable = """
                CREATE TABLE IF NOT EXISTS moderation_configs (
                    guild_id TEXT PRIMARY KEY,
                    config_data TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    modified_by TEXT,
                    version INTEGER DEFAULT 1
                )
            """;
            
            // Create config_backups table
            String createBackupsTable = """
                CREATE TABLE IF NOT EXISTS moderation_config_backups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    guild_id TEXT NOT NULL,
                    config_data TEXT NOT NULL,
                    backup_reason TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    version INTEGER
                )
            """;
            
            // Create config_templates table
            String createTemplatesTable = """
                CREATE TABLE IF NOT EXISTS moderation_config_templates (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT,
                    category TEXT,
                    template_data TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_system BOOLEAN DEFAULT FALSE
                )
            """;
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createConfigsTable);
                stmt.execute(createBackupsTable);
                stmt.execute(createTemplatesTable);
                
                // Insert default templates
                insertDefaultTemplates(conn);
                
                logger.info("Moderation configuration database tables initialized");
            }
        } catch (SQLException e) {
            logger.severe("Failed to initialize moderation configuration database: " + e.getMessage());
        }
    }
    
    /**
     * Insert default configuration templates
     */
    private void insertDefaultTemplates(Connection conn) throws SQLException {
        String insertTemplate = """
            INSERT OR IGNORE INTO moderation_config_templates 
            (id, name, description, category, template_data, is_system) 
            VALUES (?, ?, ?, ?, ?, TRUE)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(insertTemplate)) {
            // Strict Moderation Template
            stmt.setString(1, "strict");
            stmt.setString(2, "Strict Moderation");
            stmt.setString(3, "High security settings for large servers");
            stmt.setString(4, "Security");
            stmt.setString(5, createStrictTemplate());
            stmt.executeUpdate();
            
            // Community Friendly Template
            stmt.setString(1, "community");
            stmt.setString(2, "Community Friendly");
            stmt.setString(3, "Balanced settings for community servers");
            stmt.setString(4, "Community");
            stmt.setString(5, createCommunityTemplate());
            stmt.executeUpdate();
            
            // Gaming Server Template
            stmt.setString(1, "gaming");
            stmt.setString(2, "Gaming Server");
            stmt.setString(3, "Optimized for gaming communities");
            stmt.setString(4, "Gaming");
            stmt.setString(5, createGamingTemplate());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Guild Configuration Handler
     */
    private class GuildConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            try {
                if ("GET".equals(method)) {
                    handleGetConfig(exchange, path);
                } else if ("PUT".equals(method)) {
                    handleSaveConfig(exchange, path);
                } else if ("POST".equals(method) && path.endsWith("/test")) {
                    handleTestConfig(exchange, path);
                } else {
                    sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                }
            } catch (Exception e) {
                logger.severe("Error handling request: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Internal server error"));
            }
        }
        
        private void handleGetConfig(HttpExchange exchange, String path) throws IOException {
            String guildId = extractGuildId(path);
            if (guildId == null) {
                sendResponse(exchange, 400, Map.of("error", "Invalid guild ID"));
                return;
            }
            
            try (Connection conn = databaseService.getConnection()) {
                String sql = "SELECT config_data FROM moderation_configs WHERE guild_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, guildId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        String configData = rs.getString("config_data");
                        Map<String, Object> config = objectMapper.readValue(configData, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        sendResponse(exchange, 200, config);
                    } else {
                        // Return default configuration
                        Map<String, Object> defaultConfig = createDefaultConfig(guildId);
                        sendResponse(exchange, 200, defaultConfig);
                    }
                }
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Database error"));
            }
        }
        
        private void handleSaveConfig(HttpExchange exchange, String path) throws IOException {
            String guildId = extractGuildId(path);
            if (guildId == null) {
                sendResponse(exchange, 400, Map.of("error", "Invalid guild ID"));
                return;
            }
            
            // Read request body
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> config = objectMapper.readValue(requestBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            try (Connection conn = databaseService.getConnection()) {
                String sql = """
                    INSERT OR REPLACE INTO moderation_configs 
                    (guild_id, config_data, updated_at, modified_by, version) 
                    VALUES (?, ?, CURRENT_TIMESTAMP, ?, 
                        COALESCE((SELECT version + 1 FROM moderation_configs WHERE guild_id = ?), 1))
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, guildId);
                    stmt.setString(2, objectMapper.writeValueAsString(config));
                    stmt.setString(3, "dashboard"); // Could be extracted from auth
                    stmt.setString(4, guildId);
                    
                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        sendResponse(exchange, 200, Map.of("success", true, "message", "Configuration saved"));
                    } else {
                        sendResponse(exchange, 500, Map.of("error", "Failed to save configuration"));
                    }
                }
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Database error"));
            }
        }
        
        private void handleTestConfig(HttpExchange exchange, String path) throws IOException {
            String guildId = extractGuildId(path.replace("/test", ""));
            if (guildId == null) {
                sendResponse(exchange, 400, Map.of("error", "Invalid guild ID"));
                return;
            }
            
            // Read test request
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> testRequest = objectMapper.readValue(requestBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            // Simulate moderation test
            Map<String, Object> testResult = simulateModerationTest(testRequest);
            sendResponse(exchange, 200, testResult);
        }
    }
    
    /**
     * Filter Options Handler
     */
    private class FilterOptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            Map<String, Object> options = Map.of(
                "spamTimeWindows", List.of(
                    Map.of("value", "30s", "label", "30 seconds"),
                    Map.of("value", "1m", "label", "1 minute"),
                    Map.of("value", "5m", "label", "5 minutes"),
                    Map.of("value", "10m", "label", "10 minutes")
                ),
                "toxicityLanguages", List.of(
                    Map.of("value", "en", "label", "English"),
                    Map.of("value", "es", "label", "Spanish"),
                    Map.of("value", "fr", "label", "French"),
                    Map.of("value", "de", "label", "German")
                )
            );
            
            sendResponse(exchange, 200, options);
        }
    }
    
    /**
     * Action Options Handler
     */
    private class ActionOptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            Map<String, Object> options = Map.of(
                "muteDurations", List.of(
                    Map.of("value", "5m", "label", "5 minutes"),
                    Map.of("value", "10m", "label", "10 minutes"),
                    Map.of("value", "30m", "label", "30 minutes"),
                    Map.of("value", "1h", "label", "1 hour"),
                    Map.of("value", "24h", "label", "24 hours")
                )
            );
            
            sendResponse(exchange, 200, options);
        }
    }
    
    /**
     * Rule Conditions Handler
     */
    private class RuleConditionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            Map<String, Object> options = Map.of(
                "ruleTypes", List.of(
                    Map.of("value", "content", "label", "Content Filter"),
                    Map.of("value", "user", "label", "User Behavior"),
                    Map.of("value", "channel", "label", "Channel Specific"),
                    Map.of("value", "time", "label", "Time Based")
                )
            );
            
            sendResponse(exchange, 200, options);
        }
    }
    
    /**
     * UI Options Handler
     */
    private class UIOptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            Map<String, Object> options = Map.of(
                "themes", List.of(
                    Map.of("value", "light", "label", "Light"),
                    Map.of("value", "dark", "label", "Dark"),
                    Map.of("value", "auto", "label", "Auto")
                ),
                "languages", List.of(
                    Map.of("value", "en", "label", "English"),
                    Map.of("value", "es", "label", "Español"),
                    Map.of("value", "fr", "label", "Français"),
                    Map.of("value", "de", "label", "Deutsch")
                ),
                "layouts", List.of(
                    Map.of("value", "default", "label", "Default"),
                    Map.of("value", "compact", "label", "Compact"),
                    Map.of("value", "expanded", "label", "Expanded")
                )
            );
            
            sendResponse(exchange, 200, options);
        }
    }
    
    /**
     * Templates Handler
     */
    private class TemplatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            try (Connection conn = databaseService.getConnection()) {
                String sql = "SELECT id, name, description, category FROM moderation_config_templates ORDER BY category, name";
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(sql);
                    List<Map<String, Object>> templates = new ArrayList<>();
                    
                    while (rs.next()) {
                        templates.add(Map.of(
                            "id", rs.getString("id"),
                            "name", rs.getString("name"),
                            "description", rs.getString("description"),
                            "category", rs.getString("category")
                        ));
                    }
                    
                    sendResponse(exchange, 200, Map.of("templates", templates));
                }
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Database error"));
            }
        }
    }
    
    // Utility methods
    
    private String extractGuildId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("guild".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    private Map<String, Object> createDefaultConfig(String guildId) {
        return Map.of(
            "guildId", guildId,
            "enabled", true,
            "filters", Map.of(
                "spam", Map.of(
                    "enabled", false,
                    "threshold", 0.7,
                    "maxMessages", 5,
                    "timeWindow", "1m",
                    "checkDuplicates", true,
                    "checkRapidTyping", true
                ),
                "toxicity", Map.of(
                    "enabled", false,
                    "threshold", 0.6,
                    "languages", List.of("en"),
                    "useAI", true,
                    "checkSentiment", false,
                    "checkContext", false
                ),
                "link", Map.of(
                    "enabled", false,
                    "maxLinks", 2,
                    "blockedDomains", List.of(),
                    "checkShorteners", false,
                    "checkReputation", false
                ),
                "word", Map.of(
                    "enabled", false,
                    "bannedWords", List.of(),
                    "useRegex", false,
                    "caseSensitive", false,
                    "wholeWordsOnly", true
                )
            ),
            "actions", Map.of(
                "warn", Map.of("enabled", true, "message", "Please follow server rules"),
                "mute", Map.of("enabled", true, "duration", "10m"),
                "kick", Map.of("enabled", false, "reason", "Violation of server rules"),
                "ban", Map.of("enabled", false, "reason", "Severe violation", "deleteMessages", true)
            ),
            "customRules", List.of(),
            "thresholds", Map.of(
                "violations", Map.of(
                    "warn", 1,
                    "mute", 3,
                    "kick", 5,
                    "ban", 10
                ),
                "timeWindow", "24h"
            ),
            "ui", Map.of(
                "theme", "light",
                "language", "en",
                "layout", "default"
            ),
            "settings", Map.of(
                "logChannel", null,
                "moderatorRole", null
            )
        );
    }
    
    private Map<String, Object> simulateModerationTest(Map<String, Object> testRequest) {
        String content = (String) testRequest.get("content");
        
        List<Map<String, Object>> violations = new ArrayList<>();
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Simulate spam detection
        if (content.toLowerCase().contains("spam") || content.toLowerCase().contains("buy now")) {
            violations.add(Map.of(
                "type", "spam",
                "reason", "Detected promotional content",
                "confidence", 0.85
            ));
            actions.add(Map.of(
                "type", "warn",
                "reason", "Spam content detected"
            ));
        }
        
        // Simulate toxicity detection
        if (content.toLowerCase().contains("hate") || content.toLowerCase().contains("toxic")) {
            violations.add(Map.of(
                "type", "toxicity",
                "reason", "Toxic language detected",
                "confidence", 0.92
            ));
            actions.add(Map.of(
                "type", "mute",
                "reason", "Toxic behavior"
            ));
        }
        
        return Map.of(
            "success", true,
            "violations", violations,
            "actions", actions,
            "timestamp", System.currentTimeMillis()
        );
    }
    
    private String createStrictTemplate() {
        Map<String, Object> template = createDefaultConfig("template");
        Map<String, Object> filters = castToMap(template.get("filters"));
        
        // Enable all filters with strict settings
        castToMap(filters.get("spam")).put("enabled", true);
        castToMap(filters.get("spam")).put("threshold", 0.5);
        castToMap(filters.get("toxicity")).put("enabled", true);
        castToMap(filters.get("toxicity")).put("threshold", 0.4);
        castToMap(filters.get("link")).put("enabled", true);
        castToMap(filters.get("word")).put("enabled", true);
        
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String createCommunityTemplate() {
        Map<String, Object> template = createDefaultConfig("template");
        Map<String, Object> filters = castToMap(template.get("filters"));
        
        // Balanced settings
        castToMap(filters.get("spam")).put("enabled", true);
        castToMap(filters.get("toxicity")).put("enabled", true);
        
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String createGamingTemplate() {
        Map<String, Object> template = createDefaultConfig("template");
        Map<String, Object> filters = castToMap(template.get("filters"));
        
        // Gaming-optimized settings
        castToMap(filters.get("spam")).put("enabled", true);
        castToMap(filters.get("spam")).put("maxMessages", 8); // Allow more messages for gaming
        castToMap(filters.get("link")).put("enabled", true);
        castToMap(filters.get("link")).put("maxLinks", 3); // Allow more links for gaming content
        
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}