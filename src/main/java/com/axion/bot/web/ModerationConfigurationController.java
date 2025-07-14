package com.axion.bot.web;

import com.axion.bot.moderation.*;
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
     * Starts the HTTP server on the specified port
     */
    public void startServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Create contexts for all endpoints
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Serve static files from resources directory
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }
                
                try (InputStream is = getClass().getResourceAsStream("/static" + path)) {
                    if (is == null) {
                        String response = "404 (Not Found)\n";
                        exchange.sendResponseHeaders(404, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    } else {
                        byte[] response = is.readAllBytes();
                        exchange.sendResponseHeaders(200, response.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response);
                        }
                    }
                }
            }
        });
        server.createContext("/api/moderation/config/guild", new GuildConfigHandler());
        server.createContext("/api/moderation/config/filters/options", new FilterOptionsHandler());
        server.createContext("/api/moderation/config/actions/options", new ActionOptionsHandler());
        server.createContext("/api/moderation/config/rules/conditions", new RuleConditionsHandler());
        server.createContext("/api/moderation/config/ui/options", new UIOptionsHandler());
        server.createContext("/api/moderation/config/templates", new TemplatesHandler());
        server.createContext("/api/moderation/guilds", new GuildsHandler());
        server.createContext("/api/moderation/stats", new StatsHandler());
        server.createContext("/api/moderation/logs", new LogsHandler());
        
        server.setExecutor(null); // Use default executor
        server.start();
        
        System.out.println("Moderation Configuration Server started on port " + port);
        System.out.println("Access the dashboard at: http://localhost:" + port + "/moderation-dashboard.html");
    }
    
    /**
     * Stops the HTTP server
     */
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Moderation Configuration Server stopped");
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
                        Map<String, Object> config = objectMapper.readValue(configData, Map.class);
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
            Map<String, Object> config = objectMapper.readValue(requestBody, Map.class);
            
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
            Map<String, Object> testRequest = objectMapper.readValue(requestBody, Map.class);
            
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

    /**
     * Guilds Handler - Returns list of available guilds
     */
    private class GuildsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            try (Connection conn = databaseService.getConnection()) {
                String sql = "SELECT DISTINCT guild_id FROM moderation_configs ORDER BY guild_id";
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(sql);
                    List<Map<String, Object>> guilds = new ArrayList<>();
                    
                    while (rs.next()) {
                        String guildId = rs.getString("guild_id");
                        guilds.add(Map.of(
                            "id", guildId,
                            "name", "Guild " + guildId, // In real implementation, fetch from Discord API
                            "hasConfig", true
                        ));
                    }
                    
                    // Add some default guilds for demo purposes
                    if (guilds.isEmpty()) {
                        guilds.add(Map.of(
                            "id", "123456789012345678",
                            "name", "Demo Server 1",
                            "hasConfig", false
                        ));
                        guilds.add(Map.of(
                            "id", "987654321098765432",
                            "name", "Demo Server 2",
                            "hasConfig", false
                        ));
                    }
                    
                    sendResponse(exchange, 200, Map.of("guilds", guilds));
                }
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Database error"));
            }
        }
    }

    /**
     * Stats Handler - Returns moderation statistics
     */
    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            String guildId = extractGuildIdFromQuery(exchange.getRequestURI().getQuery());
            
            try (Connection conn = databaseService.getConnection()) {
                // Create moderation_logs table if it doesn't exist
                String createLogsTable = """
                    CREATE TABLE IF NOT EXISTS moderation_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        guild_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        action_type TEXT NOT NULL,
                        reason TEXT,
                        moderator_id TEXT,
                        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        details TEXT
                    )
                """;
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createLogsTable);
                }
                
                // Get statistics
                Map<String, Object> stats = new HashMap<>();
                
                if (guildId != null) {
                    // Guild-specific stats
                    String sql = """
                        SELECT action_type, COUNT(*) as count 
                        FROM moderation_logs 
                        WHERE guild_id = ? AND timestamp >= datetime('now', '-30 days')
                        GROUP BY action_type
                    """;
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, guildId);
                        ResultSet rs = stmt.executeQuery();
                        
                        Map<String, Integer> actionCounts = new HashMap<>();
                        while (rs.next()) {
                            actionCounts.put(rs.getString("action_type"), rs.getInt("count"));
                        }
                        
                        stats.put("actionCounts", actionCounts);
                        stats.put("totalActions", actionCounts.values().stream().mapToInt(Integer::intValue).sum());
                    }
                } else {
                    // Global stats
                    stats.put("totalGuilds", getTotalGuilds(conn));
                    stats.put("totalConfigurations", getTotalConfigurations(conn));
                    stats.put("activeFilters", getActiveFilters(conn));
                }
                
                sendResponse(exchange, 200, stats);
                
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Database error"));
            }
        }
    }

    /**
     * Logs Handler - Returns moderation logs
     */
    private class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }
            
            String guildId = extractGuildIdFromQuery(exchange.getRequestURI().getQuery());
            
            try (Connection conn = databaseService.getConnection()) {
                String sql = """
                    SELECT id, user_id, action_type, reason, moderator_id, timestamp, details
                    FROM moderation_logs 
                    WHERE guild_id = ? 
                    ORDER BY timestamp DESC 
                    LIMIT 100
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, guildId != null ? guildId : "demo");
                    ResultSet rs = stmt.executeQuery();
                    
                    List<Map<String, Object>> logs = new ArrayList<>();
                    while (rs.next()) {
                        logs.add(Map.of(
                            "id", rs.getInt("id"),
                            "userId", rs.getString("user_id"),
                            "actionType", rs.getString("action_type"),
                            "reason", rs.getString("reason"),
                            "moderatorId", rs.getString("moderator_id"),
                            "timestamp", rs.getTimestamp("timestamp").getTime(),
                            "details", rs.getString("details")
                        ));
                    }
                    
                    // Add demo data if no logs exist
                    if (logs.isEmpty()) {
                        logs.add(Map.of(
                            "id", 1,
                            "userId", "123456789",
                            "actionType", "warn",
                            "reason", "Spam detected",
                            "moderatorId", "987654321",
                            "timestamp", System.currentTimeMillis() - 3600000,
                            "details", "Automatic moderation action"
                        ));
                    }
                    
                    sendResponse(exchange, 200, Map.of("logs", logs));
                }
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
                sendResponse(exchange, 500, Map.of("error", "Database error"));
            }
        }
    }

    // Utility methods

    private String extractGuildIdFromQuery(String query) {
        if (query == null) return null;
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "guildId".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    private int getTotalGuilds(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT guild_id) FROM moderation_configs";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getTotalConfigurations(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM moderation_configs";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getActiveFilters(Connection conn) throws SQLException {
        // This would count active filters across all configurations
        // For now, return a demo value
        return 42;
    }

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
        Map<String, Object> config = (Map<String, Object>) testRequest.get("configuration");
        
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
        Map<String, Object> filters = (Map<String, Object>) template.get("filters");
        
        // Enable all filters with strict settings
        ((Map<String, Object>) filters.get("spam")).put("enabled", true);
        ((Map<String, Object>) filters.get("spam")).put("threshold", 0.5);
        ((Map<String, Object>) filters.get("toxicity")).put("enabled", true);
        ((Map<String, Object>) filters.get("toxicity")).put("threshold", 0.4);
        ((Map<String, Object>) filters.get("link")).put("enabled", true);
        ((Map<String, Object>) filters.get("word")).put("enabled", true);
        
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String createCommunityTemplate() {
        Map<String, Object> template = createDefaultConfig("template");
        Map<String, Object> filters = (Map<String, Object>) template.get("filters");
        
        // Balanced settings
        ((Map<String, Object>) filters.get("spam")).put("enabled", true);
        ((Map<String, Object>) filters.get("toxicity")).put("enabled", true);
        
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String createGamingTemplate() {
        Map<String, Object> template = createDefaultConfig("template");
        Map<String, Object> filters = (Map<String, Object>) template.get("filters");
        
        // Gaming-optimized settings
        ((Map<String, Object>) filters.get("spam")).put("enabled", true);
        ((Map<String, Object>) filters.get("spam")).put("maxMessages", 8); // Allow more messages for gaming
        ((Map<String, Object>) filters.get("link")).put("enabled", true);
        ((Map<String, Object>) filters.get("link")).put("maxLinks", 3); // Allow more links for gaming content
        
        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            return "{}";
        }
    }
}