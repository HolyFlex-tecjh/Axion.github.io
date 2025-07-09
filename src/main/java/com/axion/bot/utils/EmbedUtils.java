package com.axion.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.time.Instant;

/**
 * Utility klasse til at oprette standardiserede Discord embeds
 */
public class EmbedUtils {
    
    // Farve palette for forskellige embed typer
    public static final Color PRIMARY_COLOR = new Color(88, 101, 242);   // Discord Blurple
    public static final Color SUCCESS_COLOR = new Color(34, 197, 94);    // Grøn
    public static final Color WARNING_COLOR = new Color(251, 191, 36);   // Gul
    public static final Color ERROR_COLOR = new Color(239, 68, 68);      // Rød
    public static final Color INFO_COLOR = new Color(59, 130, 246);      // Blå
    public static final Color MODERATION_COLOR = new Color(139, 69, 19); // Brun
    
    // Emojis til forskellige situationer
    public static final String SUCCESS_EMOJI = "✅";
    public static final String ERROR_EMOJI = "❌";
    public static final String WARNING_EMOJI = "⚠️";
    public static final String INFO_EMOJI = "ℹ️";
    public static final String PING_EMOJI = "🏓";
    public static final String HELLO_EMOJI = "👋";
    public static final String TIME_EMOJI = "⏰";
    public static final String HELP_EMOJI = "📚";
    public static final String SHIELD_EMOJI = "🛡️";
    public static final String HAMMER_EMOJI = "🔨";
    public static final String KICK_EMOJI = "👢";
    public static final String TIMEOUT_EMOJI = "⏳";
    public static final String WARN_EMOJI = "⚠️";
    public static final String TRASH_EMOJI = "🗑️";
    public static final String STATS_EMOJI = "📊";
    public static final String ROBOT_EMOJI = "🤖";

    /**
     * Opretter en success embed
     */
    public static EmbedBuilder createSuccessEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " " + title)
                .setColor(SUCCESS_COLOR)
                .setDescription(description)
                .setTimestamp(Instant.now());
    }

    /**
     * Opretter en error embed
     */
    public static EmbedBuilder createErrorEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(ERROR_EMOJI + " " + title)
                .setColor(ERROR_COLOR)
                .setDescription(description)
                .setTimestamp(Instant.now());
    }

    /**
     * Opretter en warning embed
     */
    public static EmbedBuilder createWarningEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(WARNING_EMOJI + " " + title)
                .setColor(WARNING_COLOR)
                .setDescription(description)
                .setTimestamp(Instant.now());
    }

    /**
     * Opretter en info embed
     */
    public static EmbedBuilder createInfoEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(INFO_EMOJI + " " + title)
                .setColor(INFO_COLOR)
                .setDescription(description)
                .setTimestamp(Instant.now());
    }

    /**
     * Opretter en moderation embed
     */
    public static EmbedBuilder createModerationEmbed(String title, String description, User targetUser, User moderator) {
        return new EmbedBuilder()
                .setTitle(SHIELD_EMOJI + " " + title)
                .setColor(MODERATION_COLOR)
                .setDescription(description)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                .addField("Moderator", moderator.getAsMention(), true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
    }

    /**
     * Tilføjer standard footer til en embed
     */
    public static EmbedBuilder addStandardFooter(EmbedBuilder embed, String footerText) {
        return embed.setFooter(footerText, null);
    }

    /**
     * Tilføjer bruger thumbnail til embed
     */
    public static EmbedBuilder addUserThumbnail(EmbedBuilder embed, User user) {
        return embed.setThumbnail(user.getAvatarUrl());
    }
}
