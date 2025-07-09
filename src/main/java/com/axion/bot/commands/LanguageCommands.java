package com.axion.bot.commands;

import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Map;

/**
 * Håndterer sprog-relaterede kommandoer for Axion Bot
 */
public class LanguageCommands {
    private static final Logger logger = LoggerFactory.getLogger(LanguageCommands.class);
    private static final UserLanguageManager userLanguageManager = UserLanguageManager.getInstance();
    
    /**
     * Håndterer /setlanguage kommandoen
     */
    public static void handleSetLanguage(SlashCommandInteractionEvent event) {
        try {
            OptionMapping languageOption = event.getOption("language");
            if (languageOption == null) {
                event.reply("❌ Please specify a language code!").setEphemeral(true).queue();
                return;
            }
            
            String languageCode = languageOption.getAsString().toLowerCase();
            String userId = event.getUser().getId();
            
            TranslationManager tm = TranslationManager.getInstance();
            
            if (!tm.isLanguageSupported(languageCode)) {
                String message = tm.translate("language.invalid", languageCode).replace("{0}", languageCode);
                event.reply("❌ " + message).setEphemeral(true).queue();
                return;
            }
            
            // Gem brugerens sprogvalg
            userLanguageManager.setUserLanguage(userId, languageCode);
            
            String languageName = tm.getLanguageName(languageCode);
            String message = tm.translate("language.changed", languageCode).replace("{0}", languageName);
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌍 " + tm.translate("language.set", languageCode))
                .setDescription("✅ " + message)
                .setColor(Color.GREEN)
                .setFooter("Axion Bot", null);
            
            event.replyEmbeds(embed.build()).queue();
            logger.info("User {} changed language to: {}", event.getUser().getAsTag(), languageCode);
            
        } catch (Exception e) {
            logger.error("Error handling setlanguage command", e);
            event.reply("❌ An error occurred while changing language.").setEphemeral(true).queue();
        }
    }
    
    /**
     * Håndterer /languages kommandoen
     */
    public static void handleListLanguages(SlashCommandInteractionEvent event) {
        try {
            String userId = event.getUser().getId();
            String userLang = userLanguageManager.getUserLanguage(userId);
            
            TranslationManager tm = TranslationManager.getInstance();
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌍 " + tm.translate("language.available", userLang))
                .setColor(Color.BLUE)
                .setFooter("Axion Bot", null);
            
            StringBuilder languages = new StringBuilder();
            Map<String, String> supportedLanguages = tm.getSupportedLanguages();
            
            for (Map.Entry<String, String> entry : supportedLanguages.entrySet()) {
                String code = entry.getKey();
                String name = entry.getValue();
                String flag = getLanguageFlag(code);
                
                if (code.equals(userLang)) {
                    languages.append("**").append(flag).append(" `").append(code).append("` - ").append(name).append(" ✅**\n");
                } else {
                    languages.append(flag).append(" `").append(code).append("` - ").append(name).append("\n");
                }
            }
            
            embed.setDescription(languages.toString());
            embed.addField("💡 How to use", "Use `/setlanguage <code>` to change your language", false);
            
            event.replyEmbeds(embed.build()).queue();
            
        } catch (Exception e) {
            logger.error("Error handling languages command", e);
            event.reply("❌ An error occurred while listing languages.").setEphemeral(true).queue();
        }
    }
    
    /**
     * Håndterer /resetlanguage kommandoen
     */
    public static void handleResetLanguage(SlashCommandInteractionEvent event) {
        try {
            String userId = event.getUser().getId();
            userLanguageManager.resetUserLanguage(userId);
            
            TranslationManager tm = TranslationManager.getInstance();
            String message = tm.translate("language.reset.success", "en");
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌍 " + tm.translate("language.reset", "en"))
                .setDescription("✅ " + message)
                .setColor(Color.GREEN)
                .setFooter("Axion Bot", null);
            
            event.replyEmbeds(embed.build()).queue();
            logger.info("User {} reset language to default", event.getUser().getAsTag());
            
        } catch (Exception e) {
            logger.error("Error handling resetlanguage command", e);
            event.reply("❌ An error occurred while resetting language.").setEphemeral(true).queue();
        }
    }
    
    /**
     * Henter brugerens sprogindstilling
     */
    public static String getUserLanguage(String userId) {
        return userLanguageManager.getUserLanguage(userId);
    }
    
    /**
     * Henter flag emoji for et sprog
     */
    private static String getLanguageFlag(String languageCode) {
        switch (languageCode) {
            case "en": return "🇺🇸";
            case "da": return "🇩🇰";
            case "de": return "🇩🇪";
            case "fr": return "🇫🇷";
            case "es": return "🇪🇸";
            case "it": return "🇮🇹";
            case "pt": return "🇵🇹";
            case "ru": return "🇷🇺";
            case "zh": return "🇨🇳";
            case "ja": return "🇯🇵";
            case "ko": return "🇰🇷";
            case "hi": return "🇮🇳";
            case "ar": return "🇸🇦";
            case "tr": return "🇹🇷";
            case "nl": return "🇳🇱";
            case "pl": return "🇵🇱";
            case "uk": return "🇺🇦";
            case "th": return "🇹🇭";
            case "vi": return "🇻🇳";
            case "id": return "🇮🇩";
            case "ms": return "🇲🇾";
            case "he": return "🇮🇱";
            case "fa": return "🇮🇷";
            case "bn": return "🇧🇩";
            case "ur": return "🇵🇰";
            case "ta": return "🇱🇰";
            case "te": return "🇮🇳";
            case "ml": return "🇮🇳";
            case "kn": return "🇮🇳";
            case "gu": return "🇮🇳";
            case "pa": return "🇮🇳";
            case "mr": return "🇮🇳";
            case "ne": return "🇳🇵";
            case "si": return "🇱🇰";
            case "my": return "🇲🇲";
            case "km": return "🇰🇭";
            case "lo": return "🇱🇦";
            case "bo": return "🇨🇳";
            case "mn": return "🇲🇳";
            case "sw": return "🇹🇿";
            case "el": return "🇬🇷";
            case "sl": return "🇸🇮";
            default: return "🌍";
        }
    }
}