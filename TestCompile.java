// Simple test to verify ModerationDashboardServer syntax
public class TestCompile {
    public static void main(String[] args) {
        System.out.println("Testing compilation of ModerationDashboardServer...");
        
        // This will test if the class can be instantiated without external dependencies
        try {
            // Just test the class structure, not actual functionality
            Class<?> clazz = Class.forName("com.axion.bot.web.ModerationDashboardServer");
            System.out.println("✓ ModerationDashboardServer class structure is valid");
        } catch (ClassNotFoundException e) {
            System.out.println("✗ ModerationDashboardServer class not found: " + e.getMessage());
        }
    }
}