package com.axion.bot.events;

import com.axion.bot.config.BotConfig;
import com.axion.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Håndterer Discord events
 */
public class EventListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("{} er nu online og klar!", BotConfig.BOT_NAME);
        logger.info("Bot er forbundet til {} servere", event.getJDA().getGuilds().size());
        
        // Set bot status
        event.getJDA().getPresence().setActivity(
            net.dv8tion.jda.api.entities.Activity.playing("Moderation • /help")
        );
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        logger.info("Tilføjet til ny server: {} (ID: {})", 
            event.getGuild().getName(), event.getGuild().getId());
        
        // Send velkommen besked til owner hvis muligt
        if (event.getGuild().getOwner() != null) {
            EmbedBuilder welcomeEmbed = EmbedUtils.createSuccessEmbed(
                "Tak for at tilføje " + BotConfig.BOT_NAME + "!",
                "Botten er nu klar til brug på din server!"
            )
            .addField("Kom i gang", "Brug `/help` for at se alle tilgængelige kommandoer", false)
            .addField("Moderation", "Brug `/modhelp` for moderation kommandoer", false)
            .addField("Support", "Har du brug for hjælp? Besøg vores support server", false)
            .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
            
            event.getGuild().getOwner().getUser().openPrivateChannel().queue(
                channel -> channel.sendMessageEmbeds(welcomeEmbed.build()).queue(),
                error -> logger.warn("Kunne ikke sende velkommen besked til server owner")
            );
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        logger.debug("Ny bruger tilsluttet server {}: {}", 
            event.getGuild().getName(), event.getUser().getName());
        
        // Her kunne du tilføje auto-rolle eller velkommen beskeder
        // For nu logges det bare
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignorer bot beskeder
        if (event.getAuthor().isBot()) return;
        
        // Basic auto-moderation kunne implementeres her
        // For nu implementeres det ikke, da vi fokuserer på kommandoer
        
        if (BotConfig.DEBUG_MODE) {
            logger.debug("Besked modtaget fra {}: {}", 
                event.getAuthor().getName(), event.getMessage().getContentRaw());
        }
    }
}
