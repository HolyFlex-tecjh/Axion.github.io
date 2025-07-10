package com.axion.bot.commands.moderation;

import com.axion.bot.moderation.ModerationManager;
import com.axion.bot.moderation.ModerationLog;
import com.axion.bot.utils.CommandUtils;
import com.axion.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Avancerede moderation kommandoer med nye features
 */
public class AdvancedModerationCommands {
    
    private final ModerationManager moderationManager;
    
    public AdvancedModerationCommands(ModerationManager moderationManager) {
        this.moderationManager = moderationManager;
    }
    
    /**
     * Temp ban kommando
     */
    public void handleTempBan(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping durationOption = event.getOption("duration");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null || durationOption == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Manglende Parameter", 
                "Du skal angive både bruger og varighed")
                .addField("Korrekt brug", "/tempban user:<@bruger> duration:<timer> reason:<årsag>", false)
                .build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        int hours = durationOption.getAsInt();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        if (hours < 1 || hours > 168) { // Max 1 uge
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ugyldig Varighed", 
                "Varighed skal være mellem 1 og 168 timer (1 uge)").build()).setEphemeral(true).queue();
            return;
        }
        
        // Check hierarchy permissions
        Member targetMember = event.getGuild().getMember(targetUser);
        if (targetMember != null) {
            Member selfMember = event.getGuild().getSelfMember();
            Member moderator = event.getMember();
            
            if (!selfMember.canInteract(targetMember)) {
                event.replyEmbeds(EmbedUtils.createErrorEmbed("Hierarki Fejl", 
                    "Jeg kan ikke banne denne bruger da de har en højere eller lige rolle som mig.").build()).setEphemeral(true).queue();
                return;
            }
            
            if (moderator != null && !moderator.canInteract(targetMember)) {
                event.replyEmbeds(EmbedUtils.createErrorEmbed("Hierarki Fejl", 
                    "Du kan ikke banne denne bruger da de har en højere eller lige rolle som dig.").build()).setEphemeral(true).queue();
                return;
            }
        }
        
        try {
            // Implementer temp ban logik
            Instant expiry = Instant.now().plus(hours, ChronoUnit.HOURS);
            
            event.getGuild().ban(UserSnowflake.fromId(targetUser.getId()), 0, TimeUnit.SECONDS)
                    .reason(reason + " (Temp ban: " + hours + "h by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
                            EmbedBuilder tempBanEmbed = EmbedUtils.createModerationEmbed("Midlertidig Ban Udført", 
                                "Brugeren er blevet midlertidigt bannet", targetUser, event.getUser())
                                    .addField("Varighed", hours + " timer", true)
                                    .addField("Udløber", formatTimestamp(expiry), true)
                                    .addField("Årsag", reason, false)
                                    .setColor(Color.ORANGE);
                            tempBanEmbed.setTitle("⏰ Midlertidig Ban Udført");
                            event.replyEmbeds(tempBanEmbed.build()).queue();
                        },
                        error -> event.replyEmbeds(EmbedUtils.createErrorEmbed("Temp Ban Fejlede", 
                            "Kunne ikke temp banne brugeren: " + error.getMessage()).build()).setEphemeral(true).queue()
                    );
        } catch (Exception e) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Uventet Fejl", 
                "Fejl ved temp ban: " + e.getMessage()).build()).setEphemeral(true).queue();
        }
    }
    
    /**
     * Moderation logs kommando
     */
    public void handleModerationLogs(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Manglende Parameter", 
                "Du skal angive en bruger")
                .addField("Korrekt brug", "/modlogs user:<@bruger>", false)
                .build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        List<ModerationLog> logs = moderationManager.getModerationLogs(targetUser.getId(), event.getGuild().getId());
        
        EmbedBuilder logsEmbed = new EmbedBuilder()
                .setTitle("📋 Moderation Logs for " + targetUser.getName())
                .setThumbnail(targetUser.getAvatarUrl())
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now());
        
        if (logs.isEmpty()) {
            logsEmbed.setDescription("Ingen moderation logs fundet for denne bruger.");
        } else {
            logsEmbed.addField("Total Logs", String.valueOf(logs.size()), true);
            logsEmbed.addField("Violation Count", String.valueOf(moderationManager.getViolationCount(targetUser.getId(), event.getGuild().getId())), true);
            
            // Vis de sidste 5 logs
            StringBuilder logText = new StringBuilder();
            int count = Math.min(5, logs.size());
            for (int i = logs.size() - count; i < logs.size(); i++) {
                ModerationLog log = logs.get(i);
                logText.append("**").append(log.getFormattedTimestamp()).append("**\n")
                       .append(log.getShortDescription()).append("\n\n");
            }
            
            logsEmbed.addField("Seneste Logs (" + count + "/" + logs.size() + ")", logText.toString(), false);
        }
        
        event.replyEmbeds(logsEmbed.build()).queue();
    }
    
    /**
     * Reset violations kommando
     */
    public void handleResetViolations(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Manglende Parameter", 
                "Du skal angive en bruger")
                .addField("Korrekt brug", "/resetviolations user:<@bruger>", false)
                .build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        int oldCount = moderationManager.getViolationCount(targetUser.getId(), event.getGuild().getId());
        moderationManager.resetViolationCount(targetUser.getId(), event.getGuild().getId());
        
        EmbedBuilder resetEmbed = new EmbedBuilder()
                .setTitle("🔄 Violations Nulstillet")
                .setDescription("Violation count er blevet nulstillet for " + targetUser.getAsMention())
                .addField("Tidligere Count", String.valueOf(oldCount), true)
                .addField("Ny Count", "0", true)
                .addField("Nulstillet af", event.getUser().getAsMention(), true)
                .setColor(Color.GREEN)
                .setTimestamp(Instant.now());
        
        event.replyEmbeds(resetEmbed.build()).queue();
    }
    
    /**
     * Moderation stats kommando
     */
    public void handleModerationStats(SlashCommandInteractionEvent event) {
        if (!CommandUtils.hasModeratorPermissions(event)) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Ingen Tilladelse", 
                CommandUtils.getNoPermissionMessage()).build()).setEphemeral(true).queue();
            return;
        }
        
        // Samle statistikker
        int totalUsers = moderationManager.getTotalTrackedUsers();
        int activeViolations = moderationManager.getActiveViolationsCount();
        int tempBansActive = moderationManager.getActiveTempBansCount();
        
        EmbedBuilder statsEmbed = new EmbedBuilder()
                .setTitle("📊 Moderation Statistikker")
                .setColor(Color.CYAN)
                .setTimestamp(Instant.now())
                .addField("Sporede Brugere", String.valueOf(totalUsers), true)
                .addField("Aktive Violations", String.valueOf(activeViolations), true)
                .addField("Aktive Temp Bans", String.valueOf(tempBansActive), true)
                .setFooter("Axion Moderation System", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(statsEmbed.build()).queue();
    }
    
    /**
     * Formaterer timestamp til læsbar streng
     */
    private String formatTimestamp(Instant timestamp) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(timestamp);
    }
}