import com.axion.bot.database.DatabaseManager;
import com.axion.bot.database.DatabaseService;
import com.axion.bot.translation.UserLanguageManager;
import com.axion.bot.translation.TranslationManager;

public class TestUserLanguage {
    public static void main(String[] args) {
        System.out.println("Testing UserLanguageManager...");
        
        try {
            // Initialize database
            DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:test_axion_bot.db");
            dbManager.connect();
            DatabaseService dbService = new DatabaseService(dbManager);
            
            // Initialize UserLanguageManager
            UserLanguageManager ulm = UserLanguageManager.getInstance();
            ulm.setDatabaseService(dbService);
            
            String testUserId = "123456789";
            
            // Test setting user language to English
            System.out.println("Setting user language to English...");
            ulm.setUserLanguage(testUserId, "en");
            
            // Test getting user language
            String userLang = ulm.getUserLanguage(testUserId);
            System.out.println("Retrieved user language: " + userLang);
            
            // Test translation with user language
            TranslationManager tm = TranslationManager.getInstance();
            String translation = tm.translate("help.support.title", userLang);
            System.out.println("Translation in user language (" + userLang + "): " + translation);
            
            // Test changing to Danish
            System.out.println("Setting user language to Danish...");
            ulm.setUserLanguage(testUserId, "da");
            
            userLang = ulm.getUserLanguage(testUserId);
            System.out.println("Retrieved user language: " + userLang);
            
            translation = tm.translate("help.support.title", userLang);
            System.out.println("Translation in user language (" + userLang + "): " + translation);
            
            dbManager.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
