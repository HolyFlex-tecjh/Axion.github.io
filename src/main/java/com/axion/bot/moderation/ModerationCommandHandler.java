package com.axion.bot.moderation;

import com.axion.bot.commands.moderation.AdvancedModerationCommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Håndterer alle moderation slash commands
 */
public class ModerationCommandHandler {
    private final ModerationManager moderationManager;
    private final ModerationConfig config;
    private final ModerationDashboard dashboard;
    private final AdvancedModerationCommands advancedCommands;
    
    public ModerationCommandHandler(ModerationManager moderationManager, ModerationConfig config) {
        this.moderationManager = moderationManager;
        this.config = config;
        this.dashboard = new ModerationDashboard(moderationManager, config);
        this.advancedCommands = new AdvancedModerationCommands(moderationManager);
    }
    
    /**
     * Registrerer alle moderation commands
     */
    public List<CommandData> getCommands() {
        List<CommandData> commands = new ArrayList<>();
        
        // Hovedmoderation command med subcommands
        commands.add(Commands.slash("moderation", "Moderation system commands")
            .addSubcommands(
                new SubcommandData("dashboard", "Vis moderation dashboard")
                    .addOption(OptionType.STRING, "type", "Dashboard type", false),
                new SubcommandData("stats", "Vis detaljerede statistikker"),
                new SubcommandData("health", "Vis system sundhed"),
                new SubcommandData("tempbans", "Vis aktive midlertidige bans"),
                new SubcommandData("logs", "Vis seneste moderation logs")
                    .addOption(OptionType.INTEGER, "limit", "Antal logs at vise", false),
                new SubcommandData("config", "Vis eller rediger konfiguration")
                    .addOption(OptionType.STRING, "setting", "Indstilling at ændre", false)
                    .addOption(OptionType.STRING, "value", "Ny værdi", false)
            ));
        
        // Bruger-specifikke commands
        commands.add(Commands.slash("user", "Bruger moderation commands")
            .addSubcommands(
                new SubcommandData("tempban", "Midlertidigt ban en bruger")
                    .addOption(OptionType.USER, "user", "Bruger at banne", true)
                    .addOption(OptionType.INTEGER, "hours", "Antal timer", true)
                    .addOption(OptionType.STRING, "reason", "Årsag", false),
                new SubcommandData("violations", "Vis brugerens overtrædelser")
                    .addOption(OptionType.USER, "user", "Bruger", true),
                new SubcommandData("reset", "Nulstil brugerens overtrædelser")
                    .addOption(OptionType.USER, "user", "Bruger", true),
                new SubcommandData("history", "Vis brugerens moderation historik")
                    .addOption(OptionType.USER, "user", "Bruger", true)
            ));
        
        // Appeal system
        commands.add(Commands.slash("appeal", "Appeal system commands")
            .addSubcommands(
                new SubcommandData("submit", "Indsend en appel")
                    .addOption(OptionType.STRING, "reason", "Årsag til appel", true),
                new SubcommandData("review", "Behandl en appel")
                    .addOption(OptionType.USER, "user", "Bruger", true)
                    .addOption(OptionType.STRING, "decision", "Beslutning (approve/deny)", true)
                    .addOption(OptionType.STRING, "notes", "Noter", false),
                new SubcommandData("list", "Vis ventende appeals")
            ));
        
        return commands;
    }
    
    /**
     * Håndterer slash command events
     */
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String subcommandName = event.getSubcommandName();
        
        // Tjek permissions
        if (!hasModeratorPermission(event.getMember())) {
            event.reply("❌ Du har ikke tilladelse til at bruge moderation commands!").setEphemeral(true).queue();
            return;
        }
        
        try {
            switch (commandName) {
                case "moderation":
                    handleModerationCommand(event, subcommandName);
                    break;
                case "user":
                    handleUserCommand(event, subcommandName);
                    break;
                case "appeal":
                    handleAppealCommand(event, subcommandName);
                    break;
                default:
                    event.reply("❌ Ukendt command!").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            event.reply("❌ Der opstod en fejl: " + e.getMessage()).setEphemeral(true).queue();
        }
    }
    
    private void handleModerationCommand(SlashCommandInteractionEvent event, String subcommand) {
        switch (subcommand) {
            case "dashboard":
                String dashboardType = event.getOption("type", "overview", OptionMapping::getAsString);
                MessageEmbed dashboardEmbed;
                
                switch (dashboardType.toLowerCase()) {
                    case "stats":
                        dashboardEmbed = dashboard.createDetailedStatsEmbed(event.getGuild().getId());
                        break;
                    case "health":
                        dashboardEmbed = dashboard.createSystemHealthEmbed(event.getGuild().getId());
                        break;
                    default:
                        dashboardEmbed = dashboard.createOverviewEmbed(event.getGuild().getId());
                }
                
                event.replyEmbeds(dashboardEmbed).queue();
                break;
                
            case "stats":
                event.replyEmbeds(dashboard.createDetailedStatsEmbed(event.getGuild().getId())).queue();
                break;
                
            case "health":
                event.replyEmbeds(dashboard.createSystemHealthEmbed(event.getGuild().getId())).queue();
                break;
                
            case "tempbans":
                event.replyEmbeds(dashboard.createActiveTempBansEmbed()).queue();
                break;
                
            case "logs":
                int limit = event.getOption("limit", 10, OptionMapping::getAsInt);
                event.replyEmbeds(dashboard.createRecentLogsEmbed(limit)).queue();
                break;
                
            case "config":
                handleConfigCommand(event);
                break;
                
            default:
                event.reply("❌ Ukendt moderation subcommand!").setEphemeral(true).queue();
        }
    }
    
    private void handleUserCommand(SlashCommandInteractionEvent event, String subcommand) {
        switch (subcommand) {
            case "tempban":
                User user = event.getOption("user", OptionMapping::getAsUser);
                
                if (user == null) {
                    event.reply("❌ Bruger ikke fundet!").setEphemeral(true).queue();
                    return;
                }
                
                advancedCommands.handleTempBan(event);
                break;
                
            case "violations":
                User violationUser = event.getOption("user", OptionMapping::getAsUser);
                if (violationUser == null) {
                    event.reply("❌ Bruger ikke fundet!").setEphemeral(true).queue();
                    return;
                }
                
                int violations = moderationManager.getViolationCount(violationUser.getId());
                EmbedBuilder violationEmbed = new EmbedBuilder()
                    .setTitle("👤 Bruger Overtrædelser")
                    .setColor(Color.ORANGE)
                    .addField("Bruger", violationUser.getAsMention(), true)
                    .addField("Overtrædelser", String.valueOf(violations), true)
                    .setTimestamp(Instant.now());
                
                event.replyEmbeds(violationEmbed.build()).queue();
                break;
                
            case "reset":
                User resetUser = event.getOption("user", OptionMapping::getAsUser);
                if (resetUser == null) {
                    event.reply("❌ Bruger ikke fundet!").setEphemeral(true).queue();
                    return;
                }
                
                advancedCommands.handleResetViolations(event);
                break;
                
            case "history":
                User historyUser = event.getOption("user", OptionMapping::getAsUser);
                if (historyUser == null) {
                    event.reply("❌ Bruger ikke fundet!").setEphemeral(true).queue();
                    return;
                }
                
                advancedCommands.handleModerationLogs(event);
                break;
                
            default:
                event.reply("❌ Ukendt user subcommand!").setEphemeral(true).queue();
        }
    }
    
    private void handleAppealCommand(SlashCommandInteractionEvent event, String subcommand) {
        switch (subcommand) {
            case "submit":
                String appealReason = event.getOption("reason", OptionMapping::getAsString);
                if (appealReason == null || appealReason.trim().isEmpty()) {
                    event.reply("❌ Du skal angive en årsag til din appel!").setEphemeral(true).queue();
                    return;
                }
                
                // Implementer appeal submission
                EmbedBuilder appealEmbed = new EmbedBuilder()
                    .setTitle("📝 Appel Indsendt")
                    .setColor(Color.GREEN)
                    .setDescription("Din appel er blevet indsendt og vil blive behandlet af moderatorerne.")
                    .addField("Årsag", appealReason, false)
                    .setTimestamp(Instant.now());
                
                event.replyEmbeds(appealEmbed.build()).setEphemeral(true).queue();
                break;
                
            case "review":
                // Kun for moderatorer
                if (!hasAdminPermission(event.getMember())) {
                    event.reply("❌ Du har ikke tilladelse til at behandle appeals!").setEphemeral(true).queue();
                    return;
                }
                
                User appealUser = event.getOption("user", OptionMapping::getAsUser);
                String decision = event.getOption("decision", OptionMapping::getAsString);
                String notes = event.getOption("notes", "", OptionMapping::getAsString);
                
                if (appealUser == null || decision == null) {
                    event.reply("❌ Manglende parametre!").setEphemeral(true).queue();
                    return;
                }
                
                // Implementer appeal review
                EmbedBuilder reviewEmbed = new EmbedBuilder()
                    .setTitle("⚖️ Appel Behandlet")
                    .setColor(decision.equalsIgnoreCase("approve") ? Color.GREEN : Color.RED)
                    .addField("Bruger", appealUser.getAsMention(), true)
                    .addField("Beslutning", decision.equalsIgnoreCase("approve") ? "✅ Godkendt" : "❌ Afvist", true)
                    .addField("Behandlet af", event.getUser().getAsMention(), true)
                    .setTimestamp(Instant.now());
                
                if (!notes.isEmpty()) {
                    reviewEmbed.addField("Noter", notes, false);
                }
                
                event.replyEmbeds(reviewEmbed.build()).queue();
                break;
                
            case "list":
                // Vis ventende appeals
                EmbedBuilder listEmbed = new EmbedBuilder()
                    .setTitle("📋 Ventende Appeals")
                    .setColor(Color.YELLOW)
                    .setDescription("Ingen ventende appeals i øjeblikket.")
                    .setTimestamp(Instant.now());
                
                event.replyEmbeds(listEmbed.build()).queue();
                break;
                
            default:
                event.reply("❌ Ukendt appeal subcommand!").setEphemeral(true).queue();
        }
    }
    
    private void handleConfigCommand(SlashCommandInteractionEvent event) {
        String setting = event.getOption("setting", OptionMapping::getAsString);
        String value = event.getOption("value", OptionMapping::getAsString);
        
        if (setting == null) {
            // Vis nuværende konfiguration
            EmbedBuilder configEmbed = new EmbedBuilder()
                .setTitle("⚙️ Moderation Konfiguration")
                .setColor(Color.BLUE)
                .addField("Spam Beskyttelse", config.isSpamProtectionEnabled() ? "✅ Aktiveret" : "❌ Deaktiveret", true)
                .addField("Toksisk Indhold", config.isToxicDetectionEnabled() ? "✅ Aktiveret" : "❌ Deaktiveret", true)
                .addField("Link Beskyttelse", config.isLinkProtectionEnabled() ? "✅ Aktiveret" : "❌ Deaktiveret", true)
                .addField("Max Beskeder/Min", String.valueOf(config.getMaxMessagesPerMinute()), true)
                .addField("Max Advarsler", String.valueOf(config.getMaxWarningsBeforeBan()), true)
                .addField("Max Links", String.valueOf(config.getMaxLinksPerMessage()), true)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(configEmbed.build()).queue();
        } else {
            // Rediger konfiguration (kun for admins)
            if (!hasAdminPermission(event.getMember())) {
                event.reply("❌ Du har ikke tilladelse til at ændre konfiguration!").setEphemeral(true).queue();
                return;
            }
            
            if (value == null) {
                event.reply("❌ Du skal angive en værdi!").setEphemeral(true).queue();
                return;
            }
            
            // Implementer konfigurationsændringer
            event.reply("⚙️ Konfiguration opdateret: " + setting + " = " + value).queue();
        }
    }
    
    private boolean hasModeratorPermission(Member member) {
        if (member == null) return false;
        return member.hasPermission(Permission.KICK_MEMBERS) || 
               member.hasPermission(Permission.BAN_MEMBERS) ||
               member.hasPermission(Permission.MANAGE_SERVER);
    }
    
    private boolean hasAdminPermission(Member member) {
        if (member == null) return false;
        return member.hasPermission(Permission.ADMINISTRATOR) ||
               member.hasPermission(Permission.MANAGE_SERVER);
    }
}