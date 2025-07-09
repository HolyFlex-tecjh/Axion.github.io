package com.axion.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasse for EmbedUtils
 */
public class EmbedUtilsTest {

    @Test
    public void testCreateSuccessEmbed() {
        EmbedBuilder embed = EmbedUtils.createSuccessEmbed("Test Title", "Test Description");
        
        assertNotNull(embed);
        assertEquals(EmbedUtils.SUCCESS_COLOR, embed.build().getColor());
        assertTrue(embed.build().getTitle().contains("Test Title"));
        assertEquals("Test Description", embed.build().getDescription());
    }

    @Test
    public void testCreateErrorEmbed() {
        EmbedBuilder embed = EmbedUtils.createErrorEmbed("Error Title", "Error Description");
        
        assertNotNull(embed);
        assertEquals(EmbedUtils.ERROR_COLOR, embed.build().getColor());
        assertTrue(embed.build().getTitle().contains("Error Title"));
        assertEquals("Error Description", embed.build().getDescription());
    }

    @Test
    public void testCreateWarningEmbed() {
        EmbedBuilder embed = EmbedUtils.createWarningEmbed("Warning Title", "Warning Description");
        
        assertNotNull(embed);
        assertEquals(EmbedUtils.WARNING_COLOR, embed.build().getColor());
        assertTrue(embed.build().getTitle().contains("Warning Title"));
        assertEquals("Warning Description", embed.build().getDescription());
    }

    @Test
    public void testCreateInfoEmbed() {
        EmbedBuilder embed = EmbedUtils.createInfoEmbed("Info Title", "Info Description");
        
        assertNotNull(embed);
        assertEquals(EmbedUtils.INFO_COLOR, embed.build().getColor());
        assertTrue(embed.build().getTitle().contains("Info Title"));
        assertEquals("Info Description", embed.build().getDescription());
    }

    @Test
    public void testEmojiConstants() {
        assertNotNull(EmbedUtils.SUCCESS_EMOJI);
        assertNotNull(EmbedUtils.ERROR_EMOJI);
        assertNotNull(EmbedUtils.WARNING_EMOJI);
        assertNotNull(EmbedUtils.INFO_EMOJI);
        assertNotNull(EmbedUtils.PING_EMOJI);
        assertNotNull(EmbedUtils.HELLO_EMOJI);
        
        // Test at emojis ikke er tomme
        assertFalse(EmbedUtils.SUCCESS_EMOJI.isEmpty());
        assertFalse(EmbedUtils.ERROR_EMOJI.isEmpty());
    }

    @Test
    public void testColorConstants() {
        assertNotNull(EmbedUtils.PRIMARY_COLOR);
        assertNotNull(EmbedUtils.SUCCESS_COLOR);
        assertNotNull(EmbedUtils.WARNING_COLOR);
        assertNotNull(EmbedUtils.ERROR_COLOR);
        assertNotNull(EmbedUtils.INFO_COLOR);
        assertNotNull(EmbedUtils.MODERATION_COLOR);
    }
}
