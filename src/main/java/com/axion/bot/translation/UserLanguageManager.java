package com.axion.bot.translation;

import com.axion.bot.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Håndterer brugerspecifikke sprogindstillinger for Axion Bot
 */
public class UserLanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(UserLanguageManager.class);
    private static UserLanguageManager instance;
    private final Map<String, String> userLanguages = new ConcurrentHashMap<>();
    private DatabaseService databaseService;
    
    private UserLanguageManager() {
        // Cache will be loaded when DatabaseService is set
    }
    
    /**
     * Henter singleton instansen
     */
    public static synchronized UserLanguageManager getInstance() {
        if (instance == null) {
            instance = new UserLanguageManager();
        }
        return instance;
    }
    
    /**
     * Sætter DatabaseService og indlæser cache
     */
    public void setDatabaseService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        loadUserLanguagesFromDatabase();
    }
    
    /**
     * Sætter sprog for en bruger
     */
    public void setUserLanguage(String userId, String languageCode) {
        if (TranslationManager.getInstance().isLanguageSupported(languageCode)) {
            userLanguages.put(userId, languageCode);
            if (databaseService != null) {
                databaseService.setUserLanguage(userId, languageCode);
            }
            logger.info("Set language for user {} to: {}", userId, languageCode);
        } else {
            logger.warn("Attempted to set unsupported language {} for user {}", languageCode, userId);
        }
    }
    
    /**
     * Henter sprog for en bruger
     */
    public String getUserLanguage(String userId) {
        // First check cache
        String cachedLanguage = userLanguages.get(userId);
        if (cachedLanguage != null) {
            return cachedLanguage;
        }
        
        // If not in cache and database is available, check database
        if (databaseService != null) {
            String dbLanguage = databaseService.getUserLanguage(userId);
            if (dbLanguage != null && !dbLanguage.equals("en")) {
                userLanguages.put(userId, dbLanguage); // Cache it
                return dbLanguage;
            }
        }
        
        return "en"; // Default language
    }
    
    /**
     * Fjerner sprogindstilling for en bruger (reset til standard)
     */
    public void resetUserLanguage(String userId) {
        userLanguages.remove(userId);
        if (databaseService != null) {
            databaseService.removeUserLanguage(userId);
        }
        logger.info("Reset language for user {} to default", userId);
    }
    
    /**
     * Henter alle brugerspecifikke sprogindstillinger
     */
    public Map<String, String> getAllUserLanguages() {
        return new ConcurrentHashMap<>(userLanguages);
    }
    
    /**
     * Henter statistikker over sprogbrug
     */
    public Map<String, Integer> getLanguageUsageStats() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        
        for (String language : userLanguages.values()) {
            stats.put(language, stats.getOrDefault(language, 0) + 1);
        }
        
        return stats;
    }
    
    /**
     * Henter det samlede antal brugere med sprogindstillinger
     */
    public int getTotalUsers() {
        return userLanguages.size();
    }
    
    /**
     * Indlæser brugerspecifikke sprogindstillinger fra database
     */
    private void loadUserLanguagesFromDatabase() {
        if (databaseService == null) {
            logger.warn("DatabaseService not available, cannot load user languages");
            return;
        }
        
        try {
            Map<String, String> dbLanguages = databaseService.getAllUserLanguages();
            
            for (Map.Entry<String, String> entry : dbLanguages.entrySet()) {
                String userId = entry.getKey();
                String languageCode = entry.getValue();
                
                if (TranslationManager.getInstance().isLanguageSupported(languageCode)) {
                    userLanguages.put(userId, languageCode);
                } else {
                    logger.warn("Skipping unsupported language {} for user {}", languageCode, userId);
                }
            }
            
            logger.info("Loaded {} user language settings from database", userLanguages.size());
            
        } catch (Exception e) {
            logger.error("Error loading user languages from database: {}", e.getMessage());
        }
    }
    

    
    /**
     * Rydder op i gamle eller ugyldige sprogindstillinger
     */
    public void cleanup() {
        // Fjern ugyldige sprog fra cache og database
        userLanguages.entrySet().removeIf(entry -> {
            if (!TranslationManager.getInstance().isLanguageSupported(entry.getValue())) {
                logger.info("Removing invalid language {} for user {}", entry.getValue(), entry.getKey());
                if (databaseService != null) {
                    databaseService.removeUserLanguage(entry.getKey());
                }
                return true;
            }
            return false;
        });
    }
}