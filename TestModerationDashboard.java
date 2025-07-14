import com.axion.bot.database.DatabaseService;
import com.axion.bot.database.OptimizedDatabaseManager;
import com.axion.bot.web.ModerationConfigurationController;

/**
 * Simple test class to start only the moderation dashboard web server
 * without the full Discord bot
 */
public class TestModerationDashboard {
    public static void main(String[] args) {
        System.out.println("Starting Moderation Dashboard Test Server...");
        
        try {
            // Initialize database (using SQLite for testing)
            String databaseUrl = "jdbc:sqlite:test_moderation.db";
            OptimizedDatabaseManager databaseManager = new OptimizedDatabaseManager(databaseUrl);
            DatabaseService databaseService = new DatabaseService(databaseManager);
            
            // Initialize web controller
            ModerationConfigurationController webController = new ModerationConfigurationController(databaseService);
            
            // Start web server on port 8080
            int port = 8080;
            webController.startServer(port);
            
            System.out.println("\n=== MODERATION DASHBOARD STARTED ===");
            System.out.println("Dashboard URL: http://localhost:" + port + "/moderation-dashboard.html");
            System.out.println("API Base URL: http://localhost:" + port + "/api/moderation/");
            System.out.println("Database: " + databaseUrl);
            System.out.println("\nPress Ctrl+C to stop the server");
            System.out.println("======================================\n");
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Moderation Dashboard...");
                webController.stopServer();
                databaseManager.disconnect();
                System.out.println("Server stopped.");
            }));
            
            // Keep the application running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Error starting Moderation Dashboard: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}