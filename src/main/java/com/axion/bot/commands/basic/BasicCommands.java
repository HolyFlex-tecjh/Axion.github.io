package com.axion.bot.commands.basic;

import com.axion.bot.config.BotConfig;
import com.axion.bot.utils.EmbedUtils;
import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Håndterer grundlæggende bot kommandoer
 */
public class BasicCommands {
    private static final TranslationManager translationManager = TranslationManager.getInstance();
    private static final UserLanguageManager userLanguageManager = UserLanguageManager.getInstance();

    /**
     * Ping kommando - teste om botten svarer
     */
    public static void handlePing(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();
        
        String status = gatewayPing < 100 ? 
            translationManager.translate("command.ping.excellent", userLang) : 
            gatewayPing < 200 ? 
                translationManager.translate("command.ping.good", userLang) : 
                translationManager.translate("command.ping.high", userLang);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translationManager.translate("command.ping.title", userLang))
                .setColor(EmbedUtils.SUCCESS_COLOR)
                .addField(translationManager.translate("command.ping.gateway", userLang), gatewayPing + " ms", true)
                .addField(translationManager.translate("command.ping.rest", userLang), restPing + " ms", true)
                .addField(translationManager.translate("command.ping.status", userLang), status, true)
                .setTimestamp(Instant.now())
                .setFooter(BotConfig.DEFAULT_FOOTER, event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Hello kommando - hilsen til bruger
     */
    public static void handleHello(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        User user = event.getUser();
        
        EmbedBuilder embed = EmbedUtils.createSuccessEmbed(
                translationManager.translate("command.hello.title", userLang), 
                translationManager.translate("command.hello.message", userLang, user.getName(), BotConfig.BOT_NAME))
                .setThumbnail(user.getAvatarUrl())
                .addField(translationManager.translate("command.hello.user", userLang), user.getAsMention(), true)
                .addField(translationManager.translate("command.hello.created", userLang), user.getTimeCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                .addField(translationManager.translate("command.hello.tip", userLang), translationManager.translate("command.hello.help", userLang), false)
                .setFooter("Bruger ID: " + user.getId());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Info kommando - viser information om botten
     */
    public static void handleInfo(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long usedMemory = totalMemory - (Runtime.getRuntime().freeMemory() / 1024 / 1024);
        
        EmbedBuilder embed = EmbedUtils.createInfoEmbed(
                translationManager.translate("command.info.title", userLang), 
                BotConfig.BOT_DESCRIPTION)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .addField(translationManager.translate("command.info.version", userLang), BotConfig.BOT_VERSION, true)
                .addField(translationManager.translate("command.info.servers", userLang), String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField(translationManager.translate("command.info.users", userLang), String.valueOf(event.getJDA().getUsers().size()), true)
                .addField(translationManager.translate("command.info.ping", userLang), event.getJDA().getGatewayPing() + " ms", true)
                .addField(translationManager.translate("command.info.memory", userLang), usedMemory + "/" + totalMemory + " MB", true)
                .addField(translationManager.translate("command.info.java", userLang), System.getProperty("java.version"), true)
                .addField(translationManager.translate("command.info.features", userLang), translationManager.translate("command.info.features.text", userLang), false)
                .setFooter(translationManager.translate("command.info.footer", userLang));
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Time kommando - viser nuværende tid
     */
    public static void handleTime(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'kl.' HH:mm:ss");
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translationManager.translate("command.time.title", userLang))
                .setColor(EmbedUtils.INFO_COLOR)
                .addField(translationManager.translate("command.time.time", userLang), now.format(timeFormatter), true)
                .addField(translationManager.translate("command.time.date", userLang), now.format(dateFormatter), true)
                .addField(translationManager.translate("command.time.timezone", userLang), translationManager.translate("command.time.timezone.cet", userLang), true)
                .addField(translationManager.translate("command.time.full", userLang), now.format(fullFormatter), false)
                .setTimestamp(Instant.now())
                .setFooter(translationManager.translate("command.time.footer", userLang));
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Uptime kommando - viser hvor længe botten har kørt
     */
    public static void handleUptime(SlashCommandInteractionEvent event) {
        String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
        String uptimeString = getUptimeString();
        
        // Calculate detailed uptime information
        long uptimeMillis = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMillis / 1000;
        
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        // Get start time
        long startTime = System.currentTimeMillis() - uptimeMillis;
        LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), java.time.ZoneId.systemDefault());
        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(translationManager.translate("command.uptime.title", userLang))
                .setColor(EmbedUtils.SUCCESS_COLOR)
                .addField(translationManager.translate("command.uptime.duration", userLang), uptimeString, true)
                .addField(translationManager.translate("command.uptime.started", userLang), startDateTime.format(startFormatter), true)
                .addField(translationManager.translate("command.uptime.detailed", userLang), 
                    translationManager.translate("command.uptime.detailed.format", userLang, 
                        String.valueOf(days), String.valueOf(hours), String.valueOf(minutes), String.valueOf(secs)), false)
                .setTimestamp(Instant.now())
                .setFooter(translationManager.translate("command.uptime.footer", userLang));
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    /**
     * Utility method to get uptime string
     */
    private static String getUptimeString() {
        long uptimeMillis = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMillis / 1000;
        
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder uptime = new StringBuilder();
        if (days > 0) uptime.append(days).append("d ");
        if (hours > 0) uptime.append(hours).append("h ");
        if (minutes > 0) uptime.append(minutes).append("m ");
        uptime.append(secs).append("s");
        
        return uptime.toString();
    }
}
