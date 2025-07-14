package com.axion.bot.web;

import com.axion.bot.database.DatabaseService;
import com.axion.bot.database.DatabaseManager;

/**
 * Standalone server for the Moderation Dashboard
 * Can be run independently for testing or as part of the main bot
 */
public class ModerationDashboardServer {
    
    private final ModerationConfigurationController controller;
    private final DatabaseService databaseService;
    
    public ModerationDashboardServer(String databaseUrl) {
        // Initialize database
        DatabaseManager databaseManager = new DatabaseManager(databaseUrl);
        this.databaseService = new DatabaseService(databaseManager);
        
        // Initialize web controller
        this.controller = new ModerationConfigurationController(databaseService);
        
        System.out.println("Moderation Dashboard Server initialized with database: " + databaseUrl);
    }
    
    /**
     * Start the dashboard server
     */
    public void start(int port) {
        try {
            controller.startServer(port);
            System.out.println("Moderation Dashboard Server started on port " + port);
            System.out.println("Dashboard available at: http://localhost:" + port + "/moderation-dashboard.html");
        } catch (Exception e) {
            System.err.println("Failed to start Moderation Dashboard Server: " + e.getMessage());
            throw new RuntimeException("Failed to start server", e);
        }
    }
    
    /**
     * Stop the dashboard server
     */
    public void stop() {
        try {
            controller.stopServer();
            System.out.println("Moderation Dashboard Server stopped");
        } catch (Exception e) {
            System.err.println("Error stopping Moderation Dashboard Server: " + e.getMessage());
        }
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        String databaseUrl = System.getProperty("database.url", "jdbc:sqlite:moderation.db");
        int port = Integer.parseInt(System.getProperty("server.port", "8080"));
        
        System.out.println("Starting Moderation Dashboard Server...");
        System.out.println("Database URL: " + databaseUrl);
        System.out.println("Server Port: " + port);
        
        ModerationDashboardServer server = new ModerationDashboardServer(databaseUrl);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Moderation Dashboard Server...");
            server.stop();
        }));
        
        // Start the server
        server.start(port);
    }
}