package com.axion.bot.commands.developer;

import com.axion.bot.config.BotConfig;
import com.axion.bot.translation.TranslationManager;
import com.axion.bot.commands.LanguageCommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;

/**
 * Håndterer udvikler-specifikke kommandoer
 */
public class DeveloperCommands {
    private static final Logger logger = LoggerFactory.getLogger(DeveloperCommands.class);
    
    /**
     * Håndterer /devinfo kommando - viser udvikler information
     */
    public static void handleDevInfo(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        
        // Check if user is a developer
        if (!BotConfig.isDeveloper(userId)) {
            String userLang = LanguageCommands.getUserLanguage(userId);
            TranslationManager tm = TranslationManager.getInstance();
            event.reply(tm.translate("dev.permissions.denied", userLang))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String userLang = LanguageCommands.getUserLanguage(userId);
        TranslationManager tm = TranslationManager.getInstance();
        List<String> developers = BotConfig.getDeveloperIds();
        String botOwner = BotConfig.getBotOwner();
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔧 " + tm.translate("dev.title", userLang))
                .setColor(Color.BLUE)
                .setDescription(tm.translate("dev.description", userLang))
                .addField(tm.translate("dev.owner", userLang), "<@" + botOwner + ">", false)
                .addField(tm.translate("dev.developers", userLang), String.valueOf(developers.size()), true)
                .addField("Debug Mode", BotConfig.DEBUG_MODE ? "✅ " + tm.translate("general.enabled", userLang) : "❌ " + tm.translate("general.disabled", userLang), true)
                .addField("Command Logging", BotConfig.LOG_COMMANDS ? "✅ " + tm.translate("general.enabled", userLang) : "❌ " + tm.translate("general.disabled", userLang), true)
                .setFooter("Axion Bot Developer Panel", null)
                .setTimestamp(java.time.Instant.now());
        
        // Add developer list (only if few enough to display)
        if (developers.size() <= 10) {
            StringBuilder devList = new StringBuilder();
            for (int i = 0; i < developers.size(); i++) {
                devList.append(i + 1).append(". <@").append(developers.get(i)).append(">\n");
            }
            embed.addField(tm.translate("dev.developers", userLang), devList.toString(), false);
        } else {
            embed.addField(tm.translate("dev.developers", userLang), "Too many developers to display (" + developers.size() + " total)", false);
        }
        
        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue();
        
        logger.info("Developer {} used devinfo command", event.getUser().getAsTag());
    }
    
    /**
     * Håndterer /devstats kommando - viser detaljerede bot statistikker
     */
    public static void handleDevStats(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        
        // Check if user is a developer
        if (!BotConfig.isDeveloper(userId)) {
            String userLang = LanguageCommands.getUserLanguage(userId);
            TranslationManager tm = TranslationManager.getInstance();
            event.reply(tm.translate("dev.permissions.denied", userLang))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String userLang = LanguageCommands.getUserLanguage(userId);
        TranslationManager tm = TranslationManager.getInstance();
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = totalMemory - freeMemory;
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 " + tm.translate("dev.stats.title", userLang))
                .setColor(Color.GREEN)
                .setDescription(tm.translate("dev.stats.description", userLang))
                .addField(tm.translate("dev.servers", userLang), String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField(tm.translate("dev.users", userLang), String.valueOf(event.getJDA().getUsers().size()), true)
                .addField(tm.translate("dev.channels", userLang), String.valueOf(event.getJDA().getTextChannels().size() + event.getJDA().getVoiceChannels().size()), true)
                .addField(tm.translate("dev.memory", userLang), usedMemory + "MB / " + totalMemory + "MB", true)
                .addField(tm.translate("dev.version", userLang), BotConfig.BOT_VERSION, true)
                .addField(tm.translate("dev.java.version", userLang), System.getProperty("java.version"), true)
                .addField(tm.translate("dev.ping", userLang), event.getJDA().getGatewayPing() + "ms", true)
                .addField(tm.translate("dev.shard", userLang), "Shard " + event.getJDA().getShardInfo().getShardId() + "/" + event.getJDA().getShardInfo().getShardTotal(), true)
                .setFooter("Axion Bot Developer Panel", null)
                .setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue();
        
        logger.info("Developer {} used devstats command", event.getUser().getAsTag());
    }
    
    /**
     * Tjekker om en bruger er en udvikler
     */
    public static boolean isDeveloper(String userId) {
        return BotConfig.isDeveloper(userId);
    }
    
    /**
     * Tjekker om en bruger er bot ejeren
     */
    public static boolean isBotOwner(String userId) {
        return BotConfig.isBotOwner(userId);
    }
}