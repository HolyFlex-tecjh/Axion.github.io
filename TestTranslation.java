import com.axion.bot.translation.TranslationManager;

public class TestTranslation {
    public static void main(String[] args) {
        System.out.println("Testing TranslationManager...");
        
        TranslationManager tm = TranslationManager.getInstance();
        
        // Test English translation
        String englishTest = tm.translate("help.support.title", "en");
        System.out.println("English (help.support.title): " + englishTest);
        
        // Test Danish translation
        String danishTest = tm.translate("help.support.title", "da");
        System.out.println("Danish (help.support.title): " + danishTest);
        
        // Test key that doesn't exist
        String missingTest = tm.translate("missing.key", "en");
        System.out.println("Missing key: " + missingTest);
        
        // Test available languages
        var languages = tm.getSupportedLanguages();
        System.out.println("Supported languages count: " + languages.size());
        System.out.println("Danish available: " + languages.containsKey("da"));
        System.out.println("English available: " + languages.containsKey("en"));
    }
}
