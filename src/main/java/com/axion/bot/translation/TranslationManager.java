package com.axion.bot.translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Translation Manager for Axion Bot
 * Handles internationalization and localization for multiple languages
 */
public class TranslationManager {
    private static final Logger logger = LoggerFactory.getLogger(TranslationManager.class);
    
    private static TranslationManager instance;
    private final Map<String, Properties> translations = new ConcurrentHashMap<>();
    private String defaultLanguage = "en";
    
    // Supported languages with their display names
    private static final Map<String, String> SUPPORTED_LANGUAGES = createSupportedLanguagesMap();
    
    private static Map<String, String> createSupportedLanguagesMap() {
        Map<String, String> languages = new HashMap<>();
        languages.put("en", "English");
        languages.put("ar", "العربية (Arabic)");
        languages.put("bg", "Български (Bulgarian)");
        languages.put("bn", "বাংলা (Bengali)");
        languages.put("bo", "བོད་ཡིག (Tibetan)");
        languages.put("cs", "Čeština (Czech)");
        languages.put("da", "Dansk (Danish)");
        languages.put("de", "Deutsch (German)");
        languages.put("el", "Ελληνικά (Greek)");
        languages.put("es", "Español (Spanish)");
        languages.put("fa", "فارسی (Persian)");
        languages.put("fi", "Suomi (Finnish)");
        languages.put("fr", "Français (French)");
        languages.put("gu", "ગુજરાતી (Gujarati)");
        languages.put("he", "עברית (Hebrew)");
        languages.put("hi", "हिन्दी (Hindi)");
        languages.put("hr", "Hrvatski (Croatian)");
        languages.put("hu", "Magyar (Hungarian)");
        languages.put("id", "Bahasa Indonesia (Indonesian)");
        languages.put("it", "Italiano (Italian)");
        languages.put("ja", "日本語 (Japanese)");
        languages.put("km", "ខ្មែរ (Khmer)");
        languages.put("kn", "ಕನ್ನಡ (Kannada)");
        languages.put("ko", "한국어 (Korean)");
        languages.put("lo", "ລາວ (Lao)");
        languages.put("ml", "മലയാളം (Malayalam)");
        languages.put("mn", "Монгол (Mongolian)");
        languages.put("mr", "मराठी (Marathi)");
        languages.put("ms", "Bahasa Melayu (Malay)");
        languages.put("my", "မြန်မာ (Myanmar)");
        languages.put("ne", "नेपाली (Nepali)");
        languages.put("nl", "Nederlands (Dutch)");
        languages.put("no", "Norsk (Norwegian)");
        languages.put("pa", "ਪੰਜਾਬੀ (Punjabi)");
        languages.put("pl", "Polski (Polish)");
        languages.put("pt", "Português (Portuguese)");
        languages.put("ro", "Română (Romanian)");
        languages.put("ru", "Русский (Russian)");
        languages.put("si", "සිංහල (Sinhala)");
        languages.put("sk", "Slovenčina (Slovak)");
        languages.put("sl", "Slovenščina (Slovenian)");
        languages.put("sr", "Српски (Serbian)");
        languages.put("sv", "Svenska (Swedish)");
        languages.put("sw", "Kiswahili (Swahili)");
        languages.put("ta", "தமிழ் (Tamil)");
        languages.put("te", "తెలుగు (Telugu)");
        languages.put("th", "ไทย (Thai)");
        languages.put("tr", "Türkçe (Turkish)");
        languages.put("uk", "Українська (Ukrainian)");
        languages.put("ur", "اردو (Urdu)");
        languages.put("vi", "Tiếng Việt (Vietnamese)");
        languages.put("zh", "中文 (Chinese)");
        return Collections.unmodifiableMap(languages);
    }
    
    private TranslationManager() {
        loadAllTranslations();
    }
    
    public static synchronized TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }
        return instance;
    }
    
    /**
     * Load all translation files
     */
    private void loadAllTranslations() {
        for (String langCode : SUPPORTED_LANGUAGES.keySet()) {
            loadLanguage(langCode);
        }
        logger.info("Loaded {} language translations", translations.size());
    }
    
    /**
     * Load a specific language file
     */
    private void loadLanguage(String langCode) {
        try {
            String fileName = "/translations/" + langCode + ".properties";
            InputStream inputStream = getClass().getResourceAsStream(fileName);
            
            if (inputStream != null) {
                Properties props = new Properties();
                // Use UTF-8 encoding to properly handle emojis and Unicode characters
                try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
                translations.put(langCode, props);
                logger.debug("Loaded translation for language: {}", langCode);
            } else {
                logger.warn("Translation file not found: {}", fileName);
                // Create empty properties for missing languages
                translations.put(langCode, new Properties());
            }
        } catch (IOException e) {
            logger.error("Error loading translation for language: {}", langCode, e);
            translations.put(langCode, new Properties());
        }
    }
    
    /**
     * Get translated text for a specific key and language
     * @param key The translation key
     * @param langCode The language code
     * @param params Optional parameters for string formatting
     * @return Translated text
     */
    public String translate(String key, String langCode, Object... params) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        
        Properties langProps = translations.get(langCode);
        String translation;
        
        if (langProps != null && langProps.containsKey(key)) {
            translation = langProps.getProperty(key);
        } else {
            // Fallback to default language
            Properties defaultProps = translations.get(defaultLanguage);
            if (defaultProps != null && defaultProps.containsKey(key)) {
                translation = defaultProps.getProperty(key);
                logger.debug("Using fallback translation for key '{}' in language '{}'", key, langCode);
            } else {
                // Return the key itself if no translation found
                translation = key;
                logger.warn("No translation found for key '{}' in any language", key);
            }
        }
        
        // Replace parameters if provided
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                translation = translation.replace("{" + i + "}", String.valueOf(params[i]));
            }
        }
        
        return translation;
    }
    
    /**
     * Get all supported languages
     */
    public Map<String, String> getSupportedLanguages() {
        return new HashMap<>(SUPPORTED_LANGUAGES);
    }
    
    /**
     * Check if a language is supported
     */
    public boolean isLanguageSupported(String langCode) {
        return SUPPORTED_LANGUAGES.containsKey(langCode);
    }
    
    /**
     * Get default language
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    
    /**
     * Set default language
     */
    public void setDefaultLanguage(String langCode) {
        if (isLanguageSupported(langCode)) {
            this.defaultLanguage = langCode;
            logger.info("Default language set to: {}", langCode);
        } else {
            logger.warn("Attempted to set unsupported language as default: {}", langCode);
        }
    }
    
    /**
     * Reload all translations
     */
    public void reloadTranslations() {
        translations.clear();
        loadAllTranslations();
        logger.info("All translations reloaded");
    }
    
    /**
     * Get language name by code
     */
    public String getLanguageName(String langCode) {
        return SUPPORTED_LANGUAGES.getOrDefault(langCode, "Unknown Language");
    }
}