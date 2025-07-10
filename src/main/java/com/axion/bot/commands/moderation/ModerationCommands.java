package com.axion.bot.commands.moderation;

import com.axion.bot.config.BotConfig;
import com.axion.bot.moderation.ModerationManager;
import com.axion.bot.utils.CommandUtils;
import com.axion.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.concurrent.TimeUnit;

/**
 * Håndterer moderation kommandoer med embeds
 */
public class ModerationCommands {
    
    private final ModerationManager moderationManager;
    
    public ModerationCommands(ModerationManager moderationManager) {
        this.moderationManager = moderationManager;
    }

    /**
     * Ban kommando
     */
    public void handleBan(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Manglende Parameter", 
                CommandUtils.getMissingParameterMessage("en bruger at banne"))
                .addField("Korrekt brug", "/ban user:<@bruger> reason:<årsag>", false)
                .build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        try {
            event.getGuild().ban(UserSnowflake.fromId(targetUser.getId()), 0, TimeUnit.SECONDS)
                    .reason(reason + " (Banned by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
                            EmbedBuilder banEmbed = EmbedUtils.createModerationEmbed("Ban Udført", 
                                "Brugeren er blevet permanent bannet fra serveren", targetUser, event.getUser())
                                    .addField("Årsag", reason, false)
                                    .setColor(EmbedUtils.ERROR_COLOR);
                            banEmbed.setTitle(EmbedUtils.HAMMER_EMOJI + " Ban Udført");
                            event.replyEmbeds(banEmbed.build()).queue();
                        },
                        error -> event.replyEmbeds(EmbedUtils.createErrorEmbed("Ban Fejlede", 
                            "Kunne ikke banne brugeren: " + error.getMessage()).build()).setEphemeral(true).queue()
                    );
        } catch (Exception e) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Uventet Fejl", 
                "Fejl ved ban: " + e.getMessage()).build()).setEphemeral(true).queue();
        }
    }

    /**
     * Kick kommando
     */
    public void handleKick(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Manglende Parameter", 
                CommandUtils.getMissingParameterMessage("en bruger at kicke"))
                .addField("Korrekt brug", "/kick user:<@bruger> reason:<årsag>", false)
                .build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        Member targetMember = event.getGuild().getMember(targetUser);
        if (targetMember == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Bruger Ikke Fundet", 
                "Brugeren er ikke på denne server!").build()).setEphemeral(true).queue();
            return;
        }
        
        try {
            event.getGuild().kick(targetMember)
                    .reason(reason + " (Kicked by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
                            EmbedBuilder kickEmbed = EmbedUtils.createModerationEmbed("Kick Udført", 
                                "Brugeren er blevet kicket fra serveren", targetUser, event.getUser())
                                    .addField("Årsag", reason, false)
                                    .setColor(EmbedUtils.WARNING_COLOR);
                            kickEmbed.setTitle(EmbedUtils.KICK_EMOJI + " Kick Udført");
                            event.replyEmbeds(kickEmbed.build()).queue();
                        },
                        error -> event.replyEmbeds(EmbedUtils.createErrorEmbed("Kick Fejlede", 
                            "Kunne ikke kicke brugeren: " + error.getMessage()).build()).setEphemeral(true).queue()
                    );
        } catch (Exception e) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Uventet Fejl", 
                "Fejl ved kick: " + e.getMessage()).build()).setEphemeral(true).queue();
        }
    }

    /**
     * Warn kommando
     */
    public void handleWarn(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Manglende Parameter", 
                CommandUtils.getMissingParameterMessage("en bruger at advare"))
                .addField("Korrekt brug", "/warn user:<@bruger> reason:<årsag>", false)
                .build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        moderationManager.addWarning(targetUser.getId(), event.getGuild().getId(), reason, event.getUser().getId());
        int warningCount = moderationManager.getWarnings(targetUser.getId(), event.getGuild().getId());
        
        EmbedBuilder warnEmbed = EmbedUtils.createModerationEmbed("Advarsel Givet", 
            "Brugeren har modtaget en advarsel", targetUser, event.getUser())
                .addField("Årsag", reason, false)
                .addField("Total Advarsler", String.valueOf(warningCount), true)
                .setColor(EmbedUtils.WARNING_COLOR);
        warnEmbed.setTitle(EmbedUtils.WARN_EMOJI + " Advarsel Givet");
        
        if (warningCount >= BotConfig.AUTO_BAN_WARNING_THRESHOLD) {
            warnEmbed.addField(EmbedUtils.WARNING_EMOJI + " Advarsel", 
                "Brugeren har nu " + warningCount + " advarsler! Overvej yderligere handling.", false);
        }
        
        event.replyEmbeds(warnEmbed.build()).queue();
    }
}
