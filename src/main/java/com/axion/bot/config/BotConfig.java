package com.axion.bot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

/**
 * Central konfiguration for Axion Bot
 */
public class BotConfig {
    
    // Bot information
    public static final String BOT_NAME = "Axion Bot";
    public static final String BOT_VERSION = "1.0.0";
    public static final String BOT_DESCRIPTION = "En avanceret Discord bot med moderation og utility funktioner";
    
    // Command prefixes
    public static final String SLASH_PREFIX = "/";
    
    // Moderation settings
    public static final int MAX_WARNING_COUNT = 5;
    public static final int AUTO_BAN_WARNING_THRESHOLD = 3;
    public static final long TIMEOUT_MAX_DURATION_MINUTES = 2419200; // 28 dage
    
    // Message limits
    public static final int MAX_PURGE_AMOUNT = 100;
    public static final int MIN_PURGE_AMOUNT = 1;
    
    // Embed settings
    public static final String DEFAULT_FOOTER = "Axion Bot";
    public static final String GITHUB_URL = "https://github.com/axion-bot";
    public static final String SUPPORT_SERVER = "https://discord.gg/axion";
    public static final String INVITE_URL = "https://discord.com/api/oauth2/authorize?client_id=YOUR_BOT_ID&permissions=1375845727494&scope=bot%20applications.commands";
    public static final String WEBSITE_URL = "https://axion-bot.github.io";
    
    // Bot invite permissions (calculated value for all needed permissions)
    public static final long INVITE_PERMISSIONS = 1375845727494L; // Administrator, Moderate Members, Kick, Ban, etc.
    
    // Feature flags
    public static final boolean AUTO_MODERATION_ENABLED = true;
    public static final boolean SPAM_PROTECTION_ENABLED = true;
    public static final boolean TOXIC_DETECTION_ENABLED = true;
    public static final boolean LINK_PROTECTION_ENABLED = true;
    
    // Development settings
    public static final boolean DEBUG_MODE = false;
    public static final boolean LOG_COMMANDS = true;
    
    // Developer configuration - Loaded from config.properties
    public static final List<String> DEVELOPER_IDS;
    public static final String BOT_OWNER_ID;
    
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);
    
    // Static initialization block to load developer configuration
    static {
        Properties properties = new Properties();
        List<String> tempDeveloperIds = new ArrayList<>();
        String tempOwnerId = "396446785545043974"; // Default fallback
        
        try (InputStream input = BotConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                
                // Load bot owner ID
                String ownerIdProperty = properties.getProperty("bot.owner.id");
                if (ownerIdProperty != null && !ownerIdProperty.trim().isEmpty()) {
                    tempOwnerId = ownerIdProperty.trim();
                }
                
                // Load developer IDs (comma separated)
                String developerIdsProperty = properties.getProperty("bot.developers.ids");
                if (developerIdsProperty != null && !developerIdsProperty.trim().isEmpty()) {
                    String[] ids = developerIdsProperty.split(",");
                    for (String id : ids) {
                        String trimmedId = id.trim();
                        if (!trimmedId.isEmpty()) {
                            tempDeveloperIds.add(trimmedId);
                        }
                    }
                } else {
                    // Fallback to default developer IDs if not configured
                    tempDeveloperIds.addAll(Arrays.asList(
                        "396446785545043974", // Default developer ID
                        "277166958649278464"  // Example additional developer
                    ));
                }
                
                logger.info("Loaded {} developer(s) from configuration", tempDeveloperIds.size());
            } else {
                logger.warn("config.properties not found, using default developer configuration");
                tempDeveloperIds.addAll(Arrays.asList(
                    "396446785545043974",
                    "277166958649278464"
                ));
            }
        } catch (IOException e) {
            logger.error("Error loading developer configuration from config.properties", e);
            tempDeveloperIds.addAll(Arrays.asList(
                "396446785545043974",
                "277166958649278464"
            ));
        }
        
        DEVELOPER_IDS = tempDeveloperIds;
        BOT_OWNER_ID = tempOwnerId;
    }
    
    /**
     * Tjekker om en bruger er en udvikler
     * @param userId Discord bruger ID
     * @return true hvis brugeren er en udvikler
     */
    public static boolean isDeveloper(String userId) {
        return DEVELOPER_IDS.contains(userId);
    }
    
    /**
     * Tjekker om en bruger er bot ejeren
     * @param userId Discord bruger ID
     * @return true hvis brugeren er bot ejeren
     */
    public static boolean isBotOwner(String userId) {
        return BOT_OWNER_ID.equals(userId);
    }
    
    /**
     * Får alle udvikler IDs
     * @return Liste af udvikler Discord IDs
     */
    public static List<String> getDeveloperIds() {
        return DEVELOPER_IDS;
    }
    
    /**
     * Får bot ejer ID
     * @return Bot ejer Discord ID
     */
    public static String getBotOwner() {
        return BOT_OWNER_ID;
    }
    
    private BotConfig() {
        // Utility class - ingen instantiation
    }
}
