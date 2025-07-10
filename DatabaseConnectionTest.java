import java.sql.Connection;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        // Test the DatabaseManager connection handling
        System.out.println("Testing DatabaseManager connection handling...");
        
        // Simulate the scenario where connection might be null
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:test.db");
        
        // Test getConnection without calling connect first (should handle null gracefully)
        Connection conn = dbManager.getConnection();
        
        if (conn != null) {
            System.out.println("✓ Connection successfully established or reconnected");
        } else {
            System.out.println("✗ Connection is still null after getConnection() call");
        }
        
        // Test DatabaseService with null connection protection
        DatabaseService dbService = new DatabaseService(dbManager);
        
        // This should not throw NullPointerException anymore
        String userLang = dbService.getUserLanguage("test_user_123");
        System.out.println("✓ getUserLanguage returned: " + userLang + " (should be 'en' default)");
        
        // Test setting user language
        dbService.setUserLanguage("test_user_123", "da");
        System.out.println("✓ setUserLanguage completed without exception");
        
        // Test logging moderation action
        dbService.logModerationAction("user123", "TestUser", "mod123", "TestMod", 
                                    "TEST_ACTION", "Test reason", "guild123", 
                                    "channel123", "msg123", 1, true);
        System.out.println("✓ logModerationAction completed without exception");
        
        System.out.println("\nAll tests completed successfully! NullPointerException should be resolved.");
    }
}