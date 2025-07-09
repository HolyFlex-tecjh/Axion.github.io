package com.axion.bot.translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Håndterer brugerspecifikke sprogindstillinger for Axion Bot
 */
public class UserLanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(UserLanguageManager.class);
    private static UserLanguageManager instance;
    private final Map<String, String> userLanguages = new ConcurrentHashMap<>();
    private final String DATA_FILE = "user_languages.properties";
    
    private UserLanguageManager() {
        loadUserLanguages();
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
     * Sætter sprog for en bruger
     */
    public void setUserLanguage(String userId, String languageCode) {
        if (TranslationManager.getInstance().isLanguageSupported(languageCode)) {
            userLanguages.put(userId, languageCode);
            saveUserLanguages();
            logger.info("Set language for user {} to: {}", userId, languageCode);
        } else {
            logger.warn("Attempted to set unsupported language {} for user {}", languageCode, userId);
        }
    }
    
    /**
     * Henter sprog for en bruger
     */
    public String getUserLanguage(String userId) {
        return userLanguages.getOrDefault(userId, "en");
    }
    
    /**
     * Fjerner sprogindstilling for en bruger (reset til standard)
     */
    public void resetUserLanguage(String userId) {
        userLanguages.remove(userId);
        saveUserLanguages();
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
     * Indlæser brugerspecifikke sprogindstillinger fra fil
     */
    private void loadUserLanguages() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            logger.info("User languages file not found, starting with empty settings");
            return;
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                if (TranslationManager.getInstance().isLanguageSupported(value)) {
                    userLanguages.put(key, value);
                } else {
                    logger.warn("Skipping unsupported language {} for user {}", value, key);
                }
            }
            
            logger.info("Loaded {} user language settings", userLanguages.size());
            
        } catch (IOException e) {
            logger.error("Error loading user languages from file: {}", e.getMessage());
        }
    }
    
    /**
     * Gemmer brugerspecifikke sprogindstillinger til fil
     */
    private void saveUserLanguages() {
        Properties props = new Properties();
        
        for (Map.Entry<String, String> entry : userLanguages.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        
        try (FileOutputStream fos = new FileOutputStream(DATA_FILE)) {
            props.store(fos, "Axion Bot User Language Settings");
            logger.debug("Saved {} user language settings", userLanguages.size());
            
        } catch (IOException e) {
            logger.error("Error saving user languages to file: {}", e.getMessage());
        }
    }
    
    /**
     * Rydder op i gamle eller ugyldige sprogindstillinger
     */
    public void cleanup() {
        boolean changed = false;
        
        // Fjern ugyldige sprog
        userLanguages.entrySet().removeIf(entry -> {
            if (!TranslationManager.getInstance().isLanguageSupported(entry.getValue())) {
                logger.info("Removing invalid language {} for user {}", entry.getValue(), entry.getKey());
                return true;
            }
            return false;
        });
        
        if (changed) {
            saveUserLanguages();
        }
    }
}