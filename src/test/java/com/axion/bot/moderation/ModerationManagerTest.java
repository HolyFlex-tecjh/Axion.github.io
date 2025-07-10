package com.axion.bot.moderation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.axion.bot.database.DatabaseService;

/**
 * Test klasse for ModerationManager
 * Tester forskellige moderation scenarier og konfigurationer
 */
public class ModerationManagerTest {
    
    private ModerationManager moderationManager;
    private ModerationConfig config;
    private DatabaseService databaseService;
    
    @BeforeEach
    void setUp() {
        config = ModerationConfig.createDefault();
        databaseService = null; // Mock or null for testing
        moderationManager = new ModerationManager(config, databaseService);
    }
    
    @Test
    void testModerationConfigCreation() {
        // Test standard konfiguration
        ModerationConfig defaultConfig = ModerationConfig.createDefault();
        assertTrue(defaultConfig.isSpamProtectionEnabled());
        assertTrue(defaultConfig.isToxicDetectionEnabled());
        assertTrue(defaultConfig.isLinkProtectionEnabled());
        assertEquals(5, defaultConfig.getMaxMessagesPerMinute());
        assertEquals(3, defaultConfig.getMaxWarningsBeforeBan());
        
        // Test streng konfiguration
        ModerationConfig strictConfig = ModerationConfig.createStrict();
        assertTrue(strictConfig.isAutoBanEnabled());
        assertEquals(3, strictConfig.getMaxMessagesPerMinute());
        assertEquals(2, strictConfig.getMaxWarningsBeforeBan());
        
        // Test mild konfiguration
        ModerationConfig lenientConfig = ModerationConfig.createLenient();
        assertFalse(lenientConfig.isToxicDetectionEnabled());
        assertEquals(8, lenientConfig.getMaxMessagesPerMinute());
        assertEquals(5, lenientConfig.getMaxWarningsBeforeBan());
    }
    
    @Test
    void testModerationResultCreation() {
        // Test tilladt resultat
        ModerationResult allowed = ModerationResult.allowed();
        assertTrue(allowed.isAllowed());
        assertEquals(ModerationAction.NONE, allowed.getAction());
        assertEquals(0, allowed.getSeverity());
        
        // Test advarsel resultat
        ModerationResult warning = ModerationResult.warn("Test advarsel", ModerationAction.DELETE_MESSAGE);
        assertFalse(warning.isAllowed());
        assertEquals("Test advarsel", warning.getReason());
        assertEquals(ModerationAction.DELETE_MESSAGE, warning.getAction());
        assertEquals(1, warning.getSeverity());
        assertTrue(warning.isWarning());
        assertFalse(warning.isSevere());
        
        // Test ban resultat
        ModerationResult ban = ModerationResult.ban("Test ban", ModerationAction.BAN);
        assertFalse(ban.isAllowed());
        assertEquals(5, ban.getSeverity());
        assertTrue(ban.requiresImmediateAction());
        assertTrue(ban.isSevere());
    }
    
    @Test
    void testModerationActionProperties() {
        // Test message deletion actions
        assertTrue(ModerationAction.DELETE_MESSAGE.involvesMessageDeletion());
        assertTrue(ModerationAction.DELETE_AND_WARN.involvesMessageDeletion());
        assertFalse(ModerationAction.WARN_USER.involvesMessageDeletion());
        
        // Test user discipline actions
        assertTrue(ModerationAction.BAN.involvesUserDiscipline());
        assertTrue(ModerationAction.TIMEOUT.involvesUserDiscipline());
        assertFalse(ModerationAction.DELETE_MESSAGE.involvesUserDiscipline());
        
        // Test permanent actions
        assertTrue(ModerationAction.BAN.isPermanent());
        assertFalse(ModerationAction.TIMEOUT.isPermanent());
        
        // Test server removal
        assertTrue(ModerationAction.BAN.removesFromServer());
        assertTrue(ModerationAction.KICK.removesFromServer());
        assertFalse(ModerationAction.TIMEOUT.removesFromServer());
    }
    
    @Test
    void testModerationActionEscalation() {
        assertEquals(ModerationAction.DELETE_MESSAGE, ModerationAction.NONE.escalate());
        assertEquals(ModerationAction.DELETE_AND_WARN, ModerationAction.DELETE_MESSAGE.escalate());
        assertEquals(ModerationAction.DELETE_AND_TIMEOUT, ModerationAction.WARN_USER.escalate());
        assertEquals(ModerationAction.KICK, ModerationAction.TIMEOUT.escalate());
        assertEquals(ModerationAction.BAN, ModerationAction.KICK.escalate());
        assertEquals(ModerationAction.BAN, ModerationAction.BAN.escalate()); // Kan ikke eskalere højere
    }
    
    @Test
    void testModerationActionFromSeverity() {
        assertEquals(ModerationAction.NONE, ModerationAction.fromSeverity(0));
        assertEquals(ModerationAction.DELETE_MESSAGE, ModerationAction.fromSeverity(1));
        assertEquals(ModerationAction.DELETE_AND_WARN, ModerationAction.fromSeverity(2));
        assertEquals(ModerationAction.DELETE_AND_TIMEOUT, ModerationAction.fromSeverity(3));
        assertEquals(ModerationAction.KICK, ModerationAction.fromSeverity(4));
        assertEquals(ModerationAction.BAN, ModerationAction.fromSeverity(5));
    }
    
    @Test
    void testModerationResultCombination() {
        ModerationResult allowed = ModerationResult.allowed();
        ModerationResult warning = ModerationResult.warn("Warning", ModerationAction.DELETE_MESSAGE);
        ModerationResult severe = ModerationResult.severe("Severe", ModerationAction.KICK);
        
        // Kombiner tilladt med advarsel
        ModerationResult combined1 = allowed.combineWith(warning);
        assertEquals(warning, combined1);
        
        // Kombiner advarsel med alvorlig
        ModerationResult combined2 = warning.combineWith(severe);
        assertEquals(severe, combined2);
        
        // Kombiner to tilladte
        ModerationResult combined3 = allowed.combineWith(allowed);
        assertTrue(combined3.isAllowed());
    }
    
    @Test
    void testCustomFilterAddition() {
        // Test tilføjelse af custom filter
        moderationManager.addCustomFilter("testword");
        
        // Test ugyldig regex
        moderationManager.addCustomFilter("[invalid regex");
        // Skulle ikke kaste exception, men logge fejl
    }
    
    @Test
    void testWarningsManagement() {
        String userId = "123456789";
        String guildId = "987654321";
        
        // Test initial warnings
        assertEquals(0, moderationManager.getWarnings(userId, guildId));
        
        // Test clear warnings
        moderationManager.clearWarnings(userId, guildId);
        assertEquals(0, moderationManager.getWarnings(userId, guildId));
    }
    
    @Test
    void testModerationResultDisplayMessages() {
        ModerationResult warning = ModerationResult.warn("Test warning", ModerationAction.DELETE_MESSAGE);
        assertTrue(warning.getDisplayMessage().contains("⚠️"));
        assertTrue(warning.getDisplayMessage().contains("Test warning"));
        
        ModerationResult severe = ModerationResult.severe("Test severe", ModerationAction.KICK);
        assertTrue(severe.getDisplayMessage().contains("🚨"));
        
        ModerationResult ban = ModerationResult.ban("Test ban", ModerationAction.BAN);
        assertTrue(ban.getDisplayMessage().contains("🔴"));
    }
    
    @Test
    void testModerationActionMessages() {
        String username = "TestUser";
        String reason = "Test reason";
        
        // Test user messages
        assertTrue(ModerationAction.DELETE_MESSAGE.getUserMessage().contains("slettet"));
        assertTrue(ModerationAction.BAN.getUserMessage().contains("bannet"));
        
        // Test admin messages
        String adminMessage = ModerationAction.BAN.getAdminMessage(username, reason);
        assertTrue(adminMessage.contains(username));
        assertTrue(adminMessage.contains(reason));
        assertTrue(adminMessage.contains("🔨"));
    }
    
    @Test
    void testConfigurationToString() {
        String configString = config.toString();
        assertTrue(configString.contains("ModerationConfig"));
        assertTrue(configString.contains("spamProtection"));
        assertTrue(configString.contains("toxicDetection"));
    }
    
    @Test
    void testModerationResultEquality() {
        ModerationResult result1 = ModerationResult.warn("Test", ModerationAction.DELETE_MESSAGE);
        ModerationResult result2 = ModerationResult.warn("Test", ModerationAction.DELETE_MESSAGE);
        ModerationResult result3 = ModerationResult.warn("Different", ModerationAction.DELETE_MESSAGE);
        
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
    }
}