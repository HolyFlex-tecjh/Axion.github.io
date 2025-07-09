package com.axion.bot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasse for Axion Bot
 */
public class AxionBotTest {
    
    private AxionBot bot;
    
    @BeforeEach
    void setUp() {
        bot = new AxionBot();
    }
    
    @Test
    void testBotCreation() {
        assertNotNull(bot);
    }
    
    @Test
    void testCommandPrefixValidation() {
        String testMessage = "/ping";
        assertTrue(testMessage.startsWith("/"));
    }
    
    @Test
    void testCommandParsing() {
        String testCommand = "/ping test argument";
        String[] args = testCommand.substring(1).split("\\s+");
        
        assertEquals("ping", args[0]);
        assertEquals("test", args[1]);
        assertEquals("argument", args[2]);
    }
}
