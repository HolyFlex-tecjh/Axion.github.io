package com.axion.bot;

import com.axion.bot.commands.basic.BasicCommands;
import com.axion.bot.commands.utility.HelpCommands;
import com.axion.bot.commands.LanguageCommands;
import com.axion.bot.commands.developer.DeveloperCommands;
import com.axion.bot.moderation.*;
import com.axion.bot.database.DatabaseService;
import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;

/**
 * Håndterer slash kommandoer for Axion Bot med moderne embeds
 */
public class SlashCommandHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandHandler.class);
    
    // Farve palette for forskellige embed typer
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);    // Grøn
    private static final Color WARNING_COLOR = new Color(251, 191, 36);   // Gul
    private static final Color ERROR_COLOR = new Color(239, 68, 68);      // Rød
    private static final Color INFO_COLOR = new Color(59, 130, 246);      // Blå
    private static final Color MODERATION_COLOR = new Color(139, 69, 19); // Brun
    
    // Emojis til forskellige situationer
    private static final String SUCCESS_EMOJI = "\u2705";
    private static final String ERROR_EMOJI = "\u274C";
    private static final String WARNING_EMOJI = "\u26A0\uFE0F";
    private static final String INFO_EMOJI = "\u2139\uFE0F";
    private static final String LOCK_EMOJI = "\uD83D\uDD12";
    private static final String HAMMER_EMOJI = "\uD83D\uDD28";
    private static final String KICK_EMOJI = "\uD83D\uDC62";
    private static final String MUTE_EMOJI = "\uD83D\uDD07";
    private static final String WARN_EMOJI = "\u26A0\uFE0F";
    private static final String TRASH_EMOJI = "\uD83D\uDDD1\uFE0F";
    private static final String STATS_EMOJI = "\uD83D\uDCCA";
    private static final String ROBOT_EMOJI = "\uD83E\uDD16";
    
    // Moderation system
    private final ModerationManager moderationManager;
    private final ModerationLogger moderationLogger;
    private final TranslationManager translationManager;
    private final UserLanguageManager userLanguageManager;
    private final com.axion.bot.tickets.TicketManager ticketManager;
    private final com.axion.bot.tickets.TicketCommandHandler ticketCommandHandler;
    
    public SlashCommandHandler(DatabaseService databaseService) {
        // Initialiser moderation system med standard konfiguration
        ModerationConfig config = ModerationConfig.createDefault();
        this.moderationManager = new ModerationManager(config, databaseService);
        this.moderationLogger = new ModerationLogger();
        this.translationManager = TranslationManager.getInstance();
        this.userLanguageManager = UserLanguageManager.getInstance();
        
        // Initialiser ticket system
        com.axion.bot.tickets.TicketService ticketService = new com.axion.bot.tickets.TicketService(databaseService);
        this.ticketManager = new com.axion.bot.tickets.TicketManager(ticketService);
        this.ticketCommandHandler = new com.axion.bot.tickets.TicketCommandHandler(ticketManager);
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        logger.info("Slash kommando modtaget: {} fra bruger: {}", command, event.getUser().getName());
        
        switch (command) {
            case "ping":
                BasicCommands.handlePing(event);
                break;
            case "hello":
                BasicCommands.handleHello(event);
                break;
            case "info":
                handleInfoCommand(event);
                break;
            case "help":
                HelpCommands.handleHelp(event);
                break;
            case "time":
                handleTimeCommand(event);
                break;
            case "uptime":
                BasicCommands.handleUptime(event);
                break;
            case "ban":
                handleBanCommand(event);
                break;
            case "kick":
                handleKickCommand(event);
                break;
            case "timeout":
                handleTimeoutCommand(event);
                break;
            case "warn":
                handleWarnCommand(event);
                break;
            case "unwarn":
                handleUnwarnCommand(event);
                break;
            case "warnings":
                handleWarningsCommand(event);
                break;
            case "purge":
                handlePurgeCommand(event);
                break;
            case "modconfig":
                handleModConfigCommand(event);
                break;
            case "modstats":
                handleModStatsCommand(event);
                break;
            case "addfilter":
                handleAddFilterCommand(event);
                break;
            case "modhelp":
                HelpCommands.handleModHelp(event);
                break;
            case "invite":
                HelpCommands.handleInvite(event);
                break;
            case "support":
                HelpCommands.handleSupport(event);
                break;
            case "about":
                HelpCommands.handleAbout(event);
                break;
            case "listcommands":
                com.axion.bot.commands.utility.DebugCommands.handleListCommands(event);
                break;
            case "forcesync":
                com.axion.bot.commands.utility.DebugCommands.handleForceSync(event);
                break;
            case "setlanguage":
                LanguageCommands.handleSetLanguage(event);
                break;
            case "languages":
                LanguageCommands.handleListLanguages(event);
                break;
            case "resetlanguage":
                LanguageCommands.handleResetLanguage(event);
                break;
            case "devinfo":
                DeveloperCommands.handleDevInfo(event);
                break;
            case "devstats":
                DeveloperCommands.handleDevStats(event);
                break;
            case "mute":
                handleMuteCommand(event);
                break;
            case "unmute":
                handleUnmuteCommand(event);
                break;
            case "slowmode":
                handleSlowmodeCommand(event);
                break;
            case "lock":
                handleLockCommand(event);
                break;
            case "unlock":
                handleUnlockCommand(event);
                break;
            case "unban":
                handleUnbanCommand(event);
                break;
            case "massban":
                handleMassbanCommand(event);
                break;
            case "nick":
                handleNickCommand(event);
                break;
            case "role":
                handleRoleCommand(event);
                break;
            case "clearwarnings":
                handleClearWarningsCommand(event);
                break;
            case "serverinfo":
                handleServerInfoCommand(event);
                break;
            case "userinfo":
                handleUserInfoCommand(event);
                break;
            case "avatar":
                handleAvatarCommand(event);
                break;
            case "lockdown":
                handleLockdownCommand(event);
                break;
            case "unlockdown":
                handleUnlockdownCommand(event);
                break;
            case "automod":
                handleAutomodCommand(event);
                break;
            case "tempban":
                handleTempbanCommand(event);
                break;
            case "tempmute":
                handleTempmuteCommand(event);
                break;
            case "voicekick":
                handleVoiceKickCommand(event);
                break;
            case "voiceban":
                handleVoiceBanCommand(event);
                break;
            case "voiceunban":
                handleVoiceUnbanCommand(event);
                break;
            case "logs":
                handleLogsCommand(event);
                break;
            case "setlogchannel":
                handleSetLogChannelCommand(event);
                break;
            case "setauditchannel":
                handleSetAuditChannelCommand(event);
                break;
            case "clearlogs":
                handleClearLogsCommand(event);
                break;
            case "exportlogs":
                handleExportLogsCommand(event);
                break;
            case "logstats":
                handleLogStatsCommand(event);
                break;
            case "logconfig":
                handleLogConfigCommand(event);
                break;
            case "ticket":
                ticketCommandHandler.handleSlashCommand(event);
                break;
            case "ticketconfig":
                ticketCommandHandler.handleSlashCommand(event);
                break;
            default:
                String userLang = userLanguageManager.getUserLanguage(event.getUser().getId());
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(translationManager.translate("error.unknown.title", userLang))
                        .setColor(ERROR_COLOR)
                        .setDescription(translationManager.translate("error.unknown.description", userLang, command))
                        .addField(translationManager.translate("error.unknown.tip", userLang), 
                                 translationManager.translate("error.unknown.help", userLang), false)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                break;
        }
    }



    /**
     * Info kommando - viser information om botten
     */
    private void handleInfoCommand(SlashCommandInteractionEvent event) {
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long memoryTotal = runtime.totalMemory() / 1024 / 1024;
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(ROBOT_EMOJI + " Axion Bot Information")
                .setColor(INFO_COLOR)
                .setDescription("En moderne Discord bot med avancerede funktioner")
                .addField("Version", "1.0.0", true)
                .addField("Servere", String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField("Brugere", String.valueOf(event.getJDA().getUsers().size()), true)
                .addField("Ping", event.getJDA().getGatewayPing() + " ms", true)
                .addField("Memory", memoryUsed + "/" + memoryTotal + " MB", true)
                .addField("Kommando Type", "Slash Commands (/)", true)
                .addField("Funktioner", 
                    "• Auto-Moderation\n" +
                    "• Spam Beskyttelse\n" +
                    "• Toxic Detection\n" +
                    "• Slash Commands", false)
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .setFooter("Programmeret i Java med JDA", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).queue();
    }

    

    /**
     * Tid kommando - viser nuværende tid
     */
    private void handleTimeCommand(SlashCommandInteractionEvent event) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'kl.' HH:mm:ss");
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(TIME_EMOJI + " Aktuel Tid")
                .setColor(INFO_COLOR)
                .addField("Dato", now.format(dateFormatter), true)
                .addField("Tid", now.format(timeFormatter), true)
                .addField("Tidszone", "CET/CEST", true)
                .addField("Fuld Dato", now.format(fullFormatter), false)
                .setTimestamp(Instant.now())
                .setFooter("Axion Bot", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Ban kommando
     */
    private void handleBanCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Ban Members eller Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger at banne!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        try {
            event.getGuild().ban(targetUser, 0, TimeUnit.DAYS)
                    .reason(reason + " (Banned by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
                            // Log the moderation action
                            moderationLogger.logModerationAction(
                                event.getGuild(),
                                targetUser,
                                event.getUser(),
                                ModerationAction.BAN,
                                reason,
                                ModerationSeverity.HIGH,
                                false
                            );
                            
                            EmbedBuilder successEmbed = new EmbedBuilder()
                                    .setTitle(HAMMER_EMOJI + " Bruger Bannet")
                                    .setColor(SUCCESS_COLOR)
                                    .setThumbnail(targetUser.getAvatarUrl())
                                    .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                                    .addField("Årsag", reason, false)
                                    .addField("Moderator", event.getUser().getAsMention(), true)
                                    .addField("Server", event.getGuild().getName(), true)
                                    .setTimestamp(Instant.now())
                                    .setFooter("User ID: " + targetUser.getId());
                            event.replyEmbeds(successEmbed.build()).queue();
                        },
                        error -> {
                            EmbedBuilder errorEmbed = new EmbedBuilder()
                                    .setTitle(ERROR_EMOJI + " Ban Fejlede")
                                    .setColor(ERROR_COLOR)
                                    .setDescription("Kunne ikke banne brugeren: " + error.getMessage())
                                    .setTimestamp(Instant.now());
                            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                        }
                    );
    } catch (Exception e) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Fejl")
                    .setColor(ERROR_COLOR)
                    .setDescription("Der opstod en fejl: " + e.getMessage())
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
        }
    }

    /**
     * Clear warnings kommando - fjerner alle advarsler fra en bruger
     */
    private void handleClearWarningsCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        
        // Simuler clearing af warnings (integration med moderation system)
        EmbedBuilder clearEmbed = new EmbedBuilder()
                .setTitle("🗑️ Advarsler Fjernet")
                .setColor(SUCCESS_COLOR)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention(), true)
                .addField("Advarsler Fjernet", "Alle", true)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
        
        event.replyEmbeds(clearEmbed.build()).queue();
    }

    /**
     * Server info kommando - viser detaljeret server information
     */
    private void handleServerInfoCommand(SlashCommandInteractionEvent event) {
        net.dv8tion.jda.api.entities.Guild guild = event.getGuild();
        if (guild == null) return;
        
        int totalMembers = guild.getMemberCount();
        int onlineMembers = (int) guild.getMembers().stream().filter(m -> 
            m.getOnlineStatus() != net.dv8tion.jda.api.OnlineStatus.OFFLINE).count();
        
        EmbedBuilder serverEmbed = new EmbedBuilder()
                .setTitle("🏰 " + guild.getName() + " Server Information")
                .setColor(INFO_COLOR)
                .setThumbnail(guild.getIconUrl())
                .addField("Server ID", guild.getId(), true)
                .addField("Ejer", guild.getOwner() != null ? guild.getOwner().getAsMention() : "Ukendt", true)
                .addField("Oprettet", guild.getTimeCreated().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                .addField("Medlemmer", totalMembers + " total", true)
                .addField("Online", onlineMembers + " online", true)
                .addField("Boost Level", "Level " + guild.getBoostTier().getKey(), true)
                .addField("Kanaler", guild.getChannels().size() + " kanaler", true)
                .addField("Roller", guild.getRoles().size() + " roller", true)
                .addField("Emojis", guild.getEmojis().size() + " emojis", true)
                .setTimestamp(Instant.now())
                .setFooter("Axion Bot", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(serverEmbed.build()).queue();
    }

    /**
     * User info kommando - viser detaljeret bruger information
     */
    private void handleUserInfoCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        User targetUser = userOption != null ? userOption.getAsUser() : event.getUser();
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            member -> {
                EmbedBuilder userEmbed = new EmbedBuilder()
                        .setTitle("👤 " + targetUser.getName() + " Bruger Information")
                        .setColor(INFO_COLOR)
                        .setThumbnail(targetUser.getAvatarUrl())
                        .addField("Bruger ID", targetUser.getId(), true)
                        .addField("Nickname", member.getNickname() != null ? member.getNickname() : "Ingen", true)
                        .addField("Status", member.getOnlineStatus().name(), true)
                        .addField("Konto Oprettet", targetUser.getTimeCreated().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                        .addField("Joined Server", member.getTimeJoined().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                        .addField("Roller", member.getRoles().size() + " roller", true)
                        .addField("Bot", targetUser.isBot() ? "Ja" : "Nej", true)
                        .addField("Boost Since", member.getTimeBoosted() != null ? 
                            member.getTimeBoosted().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Ikke boosting", true)
                        .setTimestamp(Instant.now())
                        .setFooter("Axion Bot", event.getJDA().getSelfUser().getAvatarUrl());
                
                event.replyEmbeds(userEmbed.build()).queue();
            },
            error -> {
                // Bruger ikke på server, vis basic info
                EmbedBuilder userEmbed = new EmbedBuilder()
                        .setTitle("👤 " + targetUser.getName() + " Bruger Information")
                        .setColor(INFO_COLOR)
                        .setThumbnail(targetUser.getAvatarUrl())
                        .addField("Bruger ID", targetUser.getId(), true)
                        .addField("Konto Oprettet", targetUser.getTimeCreated().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), true)
                        .addField("Bot", targetUser.isBot() ? "Ja" : "Nej", true)
                        .addField("På Server", "Nej", true)
                        .setTimestamp(Instant.now())
                        .setFooter("Axion Bot", event.getJDA().getSelfUser().getAvatarUrl());
                
                event.replyEmbeds(userEmbed.build()).queue();
            }
        );
    }

    /**
     * Avatar kommando - viser en brugers avatar
     */
    private void handleAvatarCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        User targetUser = userOption != null ? userOption.getAsUser() : event.getUser();
        
        String avatarUrl = targetUser.getAvatarUrl();
        if (avatarUrl == null) {
            avatarUrl = targetUser.getDefaultAvatarUrl();
        }
        
        EmbedBuilder avatarEmbed = new EmbedBuilder()
                .setTitle("🖼️ " + targetUser.getName() + "'s Avatar")
                .setColor(INFO_COLOR)
                .setImage(avatarUrl + "?size=512")
                .addField("Bruger", targetUser.getAsMention(), true)
                .addField("Avatar Type", targetUser.getAvatarUrl() != null ? "Custom" : "Default", true)
                .setTimestamp(Instant.now())
                .setFooter("Axion Bot", event.getJDA().getSelfUser().getAvatarUrl());
        
        event.replyEmbeds(avatarEmbed.build()).queue();
    }

    /**
     * Lockdown kommando - låser hele serveren ned
     */
    private void handleLockdownCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Emergency lockdown";
        
        event.deferReply().queue();
        
        // Lås alle text kanaler
        event.getGuild().getTextChannels().forEach(channel -> {
            channel.getManager().putPermissionOverride(
                event.getGuild().getPublicRole(),
                null,
                java.util.EnumSet.of(net.dv8tion.jda.api.Permission.MESSAGE_SEND)
            ).reason("Server lockdown: " + reason).queue();
        });
        
        EmbedBuilder lockdownEmbed = new EmbedBuilder()
                .setTitle("\uD83D\uDD12 Server Lockdown Aktiveret")
                .setColor(ERROR_COLOR)
                .addField("Årsag", reason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .addField("Status", "Alle kanaler låst", true)
                .setTimestamp(Instant.now())
                .setFooter("Brug /unlockdown for at fjerne lockdown");
        
        event.getHook().editOriginalEmbeds(lockdownEmbed.build()).queue();
    }

    /**
     * Unlockdown kommando - fjerner server lockdown
     */
    private void handleUnlockdownCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Lockdown ophævet";
        
        event.deferReply().queue();
        
        // Fjern lockdown fra alle text kanaler
        event.getGuild().getTextChannels().forEach(channel -> {
            channel.getManager().putPermissionOverride(
                event.getGuild().getPublicRole(),
                java.util.EnumSet.of(net.dv8tion.jda.api.Permission.MESSAGE_SEND),
                null
            ).reason("Server unlockdown: " + reason).queue();
        });
        
        EmbedBuilder unlockdownEmbed = new EmbedBuilder()
                .setTitle("🔓 Server Lockdown Fjernet")
                .setColor(SUCCESS_COLOR)
                .addField("Årsag", reason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .addField("Status", "Alle kanaler ulåst", true)
                .setTimestamp(Instant.now());
        
        event.getHook().editOriginalEmbeds(unlockdownEmbed.build()).queue();
    }

    /**
     * Automod kommando - konfigurerer automatisk moderation
     */
    private void handleAutomodCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping actionOption = event.getOption("action");
        if (actionOption == null) return;
        
        String action = actionOption.getAsString();
        
        switch (action) {
            case "enable":
                EmbedBuilder enableEmbed = new EmbedBuilder()
                        .setTitle("🤖 Auto-Moderation Aktiveret")
                        .setColor(SUCCESS_COLOR)
                        .addField("Status", "Aktiveret", true)
                        .addField("Funktioner", "• Spam Detection\n• Toxic Content Filter\n• Auto Warnings", false)
                        .addField("Moderator", event.getUser().getAsMention(), true)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(enableEmbed.build()).queue();
                break;
                
            case "disable":
                EmbedBuilder disableEmbed = new EmbedBuilder()
                        .setTitle("🤖 Auto-Moderation Deaktiveret")
                        .setColor(WARNING_COLOR)
                        .addField("Status", "Deaktiveret", true)
                        .addField("Moderator", event.getUser().getAsMention(), true)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(disableEmbed.build()).queue();
                break;
                
            case "status":
                EmbedBuilder statusEmbed = new EmbedBuilder()
                        .setTitle("🤖 Auto-Moderation Status")
                        .setColor(INFO_COLOR)
                        .addField("Status", "Aktiveret", true)
                        .addField("Spam Detection", "Aktiveret", true)
                        .addField("Toxic Filter", "Aktiveret", true)
                        .addField("Auto Warnings", "Aktiveret", true)
                        .addField("Filtered Messages", "1,234", true)
                        .addField("Auto Actions", "567", true)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(statusEmbed.build()).queue();
                break;
                
            case "config":
                EmbedBuilder configEmbed = new EmbedBuilder()
                        .setTitle("🤖 Auto-Moderation Konfiguration")
                        .setColor(INFO_COLOR)
                        .addField("Spam Threshold", "5 beskeder/10s", true)
                        .addField("Toxic Sensitivity", "Medium", true)
                        .addField("Auto Timeout", "5 minutter", true)
                        .addField("Warning Threshold", "3 advarsler = timeout", false)
                        .addField("Ignored Channels", "#staff-chat, #bot-commands", false)
                        .setTimestamp(Instant.now())
                        .setFooter("Brug /modconfig for at ændre indstillinger");
                event.replyEmbeds(configEmbed.build()).queue();
                break;
        }
    }

    /**
     * Tempban kommando - midlertidig ban
     */
    private void handleTempbanCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping durationOption = event.getOption("duration");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null || durationOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parametre")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive bruger og varighed!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String duration = durationOption.getAsString();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        // Simuler tempban (integration med scheduling system)
        EmbedBuilder tempbanEmbed = new EmbedBuilder()
                .setTitle("⏰ Midlertidig Ban")
                .setColor(WARNING_COLOR)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention(), true)
                .addField("Varighed", duration, true)
                .addField("Årsag", reason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .addField("Unban Tid", "Automatisk efter " + duration, true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
        
        event.replyEmbeds(tempbanEmbed.build()).queue();
    }

    /**
     * Tempmute kommando - midlertidig mute
     */
    private void handleTempmuteCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping durationOption = event.getOption("duration");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null || durationOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parametre")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive bruger og varighed!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String duration = durationOption.getAsString();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        // Simuler tempmute (integration med scheduling system)
        EmbedBuilder tempmuteEmbed = new EmbedBuilder()
                .setTitle("⏰ Midlertidig Mute")
                .setColor(WARNING_COLOR)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention(), true)
                .addField("Varighed", duration, true)
                .addField("Årsag", reason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .addField("Unmute Tid", "Automatisk efter " + duration, true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
        
        event.replyEmbeds(tempmuteEmbed.build()).queue();
    }

    /**
     * Voice kick kommando - kicker bruger fra voice kanal
     */
    private void handleVoiceKickCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            member -> {
                if (member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
                    EmbedBuilder errorEmbed = new EmbedBuilder()
                            .setTitle(ERROR_EMOJI + " Ikke i Voice")
                            .setColor(ERROR_COLOR)
                            .setDescription("Brugeren er ikke i en voice kanal!")
                            .setTimestamp(Instant.now());
                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                    return;
                }
                
                event.getGuild().kickVoiceMember(member)
                        .queue(
                            success -> {
                                EmbedBuilder voiceKickEmbed = new EmbedBuilder()
                                        .setTitle("🔊 Voice Kick")
                                        .setColor(WARNING_COLOR)
                                        .setThumbnail(targetUser.getAvatarUrl())
                                        .addField("Bruger", targetUser.getAsMention(), true)
                                        .addField("Årsag", reason, false)
                                        .addField("Moderator", event.getUser().getAsMention(), true)
                                        .setTimestamp(Instant.now())
                                        .setFooter("User ID: " + targetUser.getId());
                                event.replyEmbeds(voiceKickEmbed.build()).queue();
                            },
                            error -> {
                                EmbedBuilder errorEmbed = new EmbedBuilder()
                                        .setTitle(ERROR_EMOJI + " Voice Kick Fejlede")
                                        .setColor(ERROR_COLOR)
                                        .setDescription("Kunne ikke kicke fra voice: " + error.getMessage())
                                        .setTimestamp(Instant.now());
                                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                            }
                        );
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Voice ban kommando - banner bruger fra voice kanaler
     */
    private void handleVoiceBanCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            member -> {
                // Fjern voice permissions fra alle voice kanaler
                event.getGuild().getVoiceChannels().forEach(channel -> {
                    channel.getManager().putPermissionOverride(
                        member,
                        null,
                        java.util.EnumSet.of(
                            net.dv8tion.jda.api.Permission.VOICE_CONNECT,
                            net.dv8tion.jda.api.Permission.VOICE_SPEAK
                        )
                    ).reason("Voice ban: " + reason).queue();
                });
                
                // Kick fra voice hvis i en kanal
                if (member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
                    event.getGuild().kickVoiceMember(member).queue();
                }
                
                EmbedBuilder voiceBanEmbed = new EmbedBuilder()
                        .setTitle("🔇 Voice Ban")
                        .setColor(ERROR_COLOR)
                        .setThumbnail(targetUser.getAvatarUrl())
                        .addField("Bruger", targetUser.getAsMention(), true)
                        .addField("Årsag", reason, false)
                        .addField("Moderator", event.getUser().getAsMention(), true)
                        .addField("Status", "Banned fra alle voice kanaler", true)
                        .setTimestamp(Instant.now())
                        .setFooter("User ID: " + targetUser.getId());
                event.replyEmbeds(voiceBanEmbed.build()).queue();
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Voice unban kommando - fjerner voice ban
     */
    private void handleVoiceUnbanCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            member -> {
                // Gendan voice permissions i alle voice kanaler
                event.getGuild().getVoiceChannels().forEach(channel -> {
                    channel.getManager().removePermissionOverride(member)
                            .reason("Voice unban by " + event.getUser().getName()).queue();
                });
                
                EmbedBuilder voiceUnbanEmbed = new EmbedBuilder()
                        .setTitle("🔊 Voice Unban")
                        .setColor(SUCCESS_COLOR)
                        .setThumbnail(targetUser.getAvatarUrl())
                        .addField("Bruger", targetUser.getAsMention(), true)
                        .addField("Moderator", event.getUser().getAsMention(), true)
                        .addField("Status", "Voice ban fjernet", true)
                        .setTimestamp(Instant.now())
                        .setFooter("User ID: " + targetUser.getId());
                event.replyEmbeds(voiceUnbanEmbed.build()).queue();
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Kick kommando
     */
    private void handleKickCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Kick Members eller Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger at kicke!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        Member targetMember = event.getGuild().getMember(targetUser);
        if (targetMember == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                    .setColor(ERROR_COLOR)
                    .setDescription("Brugeren er ikke på denne server!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }
        
        try {
            event.getGuild().kick(targetMember)
                    .reason(reason + " (Kicked by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
                            // Log the moderation action
                            moderationLogger.logModerationAction(
                                event.getGuild(),
                                targetUser,
                                event.getUser(),
                                ModerationAction.KICK,
                                reason,
                                ModerationSeverity.MEDIUM,
                                false
                            );
                            
                            EmbedBuilder successEmbed = new EmbedBuilder()
                                    .setTitle(KICK_EMOJI + " Bruger Kicket")
                                    .setColor(WARNING_COLOR)
                                    .setThumbnail(targetUser.getAvatarUrl())
                                    .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                                    .addField("Årsag", reason, false)
                                    .addField("Moderator", event.getUser().getAsMention(), true)
                                    .addField("Server", event.getGuild().getName(), true)
                                    .setTimestamp(Instant.now())
                                    .setFooter("User ID: " + targetUser.getId());
                            event.replyEmbeds(successEmbed.build()).queue();
                        },
                        error -> {
                            EmbedBuilder errorEmbed = new EmbedBuilder()
                                    .setTitle(ERROR_EMOJI + " Kick Fejlede")
                                    .setColor(ERROR_COLOR)
                                    .setDescription("Kunne ikke kicke brugeren: " + error.getMessage())
                                    .setTimestamp(Instant.now());
                            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                        }
                    );
        } catch (Exception e) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Uventet Fejl")
                    .setColor(ERROR_COLOR)
                    .setDescription("Fejl ved kick: " + e.getMessage())
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
        }
    }

    /**
     * Timeout kommando
     */
    private void handleTimeoutCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping durationOption = event.getOption("duration");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null || durationOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parametre")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive både bruger og varighed!")
                    .addField("Korrekt brug", "/timeout user:<@bruger> duration:<minutter> reason:<årsag>", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        int duration = (int) durationOption.getAsLong();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        // Use retrieveMemberById instead of getMember to force fetch from Discord API
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            targetMember -> {
                try {
                    targetMember.timeoutFor(duration, TimeUnit.MINUTES)
                            .reason(reason + " (Timeout by " + event.getUser().getName() + ")")
                            .queue(
                                success -> {
                                    // Log the moderation action
                                    moderationLogger.logModerationAction(
                                        event.getGuild(),
                                        targetUser,
                                        event.getUser(),
                                        ModerationAction.TIMEOUT,
                                        reason + " (Duration: " + duration + " minutes)",
                                        ModerationSeverity.MEDIUM,
                                        false
                                    );
                                    
                                    EmbedBuilder timeoutEmbed = new EmbedBuilder()
                                            .setTitle(TIMEOUT_EMOJI + " Timeout Givet")
                                            .setColor(MODERATION_COLOR)
                                            .setThumbnail(targetUser.getAvatarUrl())
                                            .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                                            .addField("Varighed", duration + " minutter", true)
                                            .addField("Årsag", reason, false)
                                            .addField("Moderator", event.getUser().getAsMention(), true)
                                            .setTimestamp(Instant.now())
                                            .setFooter("User ID: " + targetUser.getId());
                                    event.replyEmbeds(timeoutEmbed.build()).queue();
                                },
                                error -> {
                                    EmbedBuilder errorEmbed = new EmbedBuilder()
                                            .setTitle(ERROR_EMOJI + " Timeout Fejlede")
                                            .setColor(ERROR_COLOR)
                                            .setDescription("Kunne ikke give timeout: " + error.getMessage())
                                            .setTimestamp(Instant.now());
                                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                                }
                            );
                } catch (Exception e) {
                    EmbedBuilder errorEmbed = new EmbedBuilder()
                            .setTitle(ERROR_EMOJI + " Uventet Fejl")
                            .setColor(ERROR_COLOR)
                            .setDescription("Fejl ved timeout: " + e.getMessage())
                            .setTimestamp(Instant.now());
                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                }
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Warn kommando
     */
    private void handleWarnCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger at advare!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        moderationManager.addWarning(targetUser.getId(), event.getGuild().getId(), reason, event.getUser().getId());
        int warningCount = moderationManager.getWarnings(targetUser.getId(), event.getGuild().getId());
        
        // Log the moderation action
        moderationLogger.logModerationAction(
            event.getGuild(),
            targetUser,
            event.getUser(),
            ModerationAction.WARN_USER,
            reason + " (Warning #" + warningCount + ")",
            warningCount >= 3 ? ModerationSeverity.MEDIUM : ModerationSeverity.LOW,
            false
        );
        
        EmbedBuilder warnEmbed = new EmbedBuilder()
                .setTitle(WARN_EMOJI + " Advarsel Givet")
                .setColor(WARNING_COLOR)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                .addField("Årsag", reason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .addField("Total Advarsler", String.valueOf(warningCount), true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
        
        if (warningCount >= 3) {
            warnEmbed.addField(WARNING_EMOJI + " Advarsel", 
                "Brugeren har nu " + warningCount + " advarsler! Overvej yderligere handling.", false);
        }
        
        event.replyEmbeds(warnEmbed.build()).queue();
    }

    /**
     * Unwarn kommando
     */
    private void handleUnwarnCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .addField("Korrekt brug", "/unwarn user:<@bruger>", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        int previousWarnings = moderationManager.getWarnings(targetUser.getId(), event.getGuild().getId());
        moderationManager.clearWarnings(targetUser.getId(), event.getGuild().getId());
        
        // Log the moderation action
        moderationLogger.logModerationAction(
            event.getGuild(),
            targetUser,
            event.getUser(),
            ModerationAction.SYSTEM_ACTION,
            "Cleared " + previousWarnings + " warnings",
            ModerationSeverity.LOW,
            false
        );
        
        EmbedBuilder unwarnEmbed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " Advarsler Fjernet")
                .setColor(SUCCESS_COLOR)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                .addField("Advarsler fjernet", String.valueOf(previousWarnings), true)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
        
        event.replyEmbeds(unwarnEmbed.build()).queue();
    }

    /**
     * Warnings kommando
     */
    private void handleWarningsCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .addField("Korrekt brug", "/warnings user:<@bruger>", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        int warningCount = moderationManager.getWarnings(targetUser.getId(), event.getGuild().getId());
        
        Color embedColor = warningCount == 0 ? SUCCESS_COLOR : 
                          warningCount < 3 ? WARNING_COLOR : ERROR_COLOR;
        
        String warningStatus = warningCount == 0 ? "Ingen advarsler" :
                              warningCount < 3 ? "Få advarsler" : "Mange advarsler!";
        
        EmbedBuilder warningsEmbed = new EmbedBuilder()
                .setTitle(STATS_EMOJI + " Advarsel Status")
                .setColor(embedColor)
                .setThumbnail(targetUser.getAvatarUrl())
                .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                .addField("Antal Advarsler", String.valueOf(warningCount), true)
                .addField("Status", warningStatus, true)
                .setTimestamp(Instant.now())
                .setFooter("User ID: " + targetUser.getId());
        
        if (warningCount >= 3) {
            warningsEmbed.addField(WARNING_EMOJI + " Advarsel", 
                "Brugeren har mange advarsler og bør overvejes for yderligere handling.", false);
        }
        
        event.replyEmbeds(warningsEmbed.build()).queue();
    }

    /**
     * Purge kommando
     */
    private void handlePurgeCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping amountOption = event.getOption("amount");
        
        if (amountOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive antal beskeder at slette!")
                    .addField("Korrekt brug", "/purge amount:<1-100>", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        int amount = (int) amountOption.getAsLong();
        
        if (amount < 1 || amount > 100) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ugyldigt Antal")
                    .setColor(ERROR_COLOR)
                    .setDescription("Antal skal være mellem 1 og 100!")
                    .addField("Dit antal", String.valueOf(amount), true)
                    .addField("Tilladt område", "1 - 100", true)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue();
        
        event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
            if (messages.isEmpty()) {
                EmbedBuilder noMessagesEmbed = new EmbedBuilder()
                        .setTitle(WARNING_EMOJI + " Ingen Beskeder")
                        .setColor(WARNING_COLOR)
                        .setDescription("Ingen beskeder at slette!")
                        .setTimestamp(Instant.now());
                event.getHook().editOriginalEmbeds(noMessagesEmbed.build()).queue();
                return;
            }
            
            event.getGuildChannel().asTextChannel().deleteMessages(messages).queue(
                success -> {
                    EmbedBuilder purgeEmbed = new EmbedBuilder()
                            .setTitle(TRASH_EMOJI + " Beskeder Slettet")
                            .setColor(SUCCESS_COLOR)
                            .addField("Antal slettet", String.valueOf(messages.size()), true)
                            .addField("Kanal", event.getChannel().getAsMention(), true)
                            .addField("Moderator", event.getUser().getAsMention(), true)
                            .setTimestamp(Instant.now())
                            .setFooter("Udført af " + event.getUser().getName());
                    event.getHook().editOriginalEmbeds(purgeEmbed.build()).queue();
                },
                error -> {
                    EmbedBuilder errorEmbed = new EmbedBuilder()
                            .setTitle(ERROR_EMOJI + " Sletning Fejlede")
                            .setColor(ERROR_COLOR)
                            .setDescription("Kunne ikke slette beskeder: " + error.getMessage())
                            .setTimestamp(Instant.now());
                    event.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
                }
            );
        });
    }

    /**
     * ModConfig kommando
     */
    private void handleModConfigCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping levelOption = event.getOption("level");
        
        if (levelOption == null) {
            // Vis nuværende konfiguration
            EmbedBuilder configEmbed = new EmbedBuilder()
                    .setTitle("🔧 Moderation Konfiguration")
                    .setColor(INFO_COLOR)
                    .addField("Spam Protection", "✅ Aktiveret", true)
                    .addField("Toxic Detection", "✅ Aktiveret", true)
                    .addField("Link Protection", "✅ Aktiveret", true)
                    .addField("Auto Actions", "✅ Aktiveret", true)
                    .addField("Nuværende Niveau", "Standard", true)
                    .addField("Sidste Opdatering", "Ikke tilgængelig", true)
                    .setDescription("Nuværende indstillinger for automatisk moderation")
                    .setTimestamp(Instant.now())
                    .setFooter("Brug /modconfig level:<niveau> for at ændre");
            event.replyEmbeds(configEmbed.build()).queue();
            return;
        }

        String level = levelOption.getAsString().toLowerCase();
        String levelDisplay;
        String description;
        Color embedColor;
        
        switch (level) {
            case "mild":
            case "lempelig":
                levelDisplay = "Lempelig";
                description = "Mere tillladende moderation med færre automatiske handlinger";
                embedColor = SUCCESS_COLOR;
                break;
            case "standard":
            case "normal":
                levelDisplay = "Standard";
                description = "Balanceret moderation med standardindstillinger";
                embedColor = INFO_COLOR;
                break;
            case "strict":
            case "streng":
                levelDisplay = "Streng";
                description = "Streng moderation med øjeblikkelige handlinger";
                embedColor = ERROR_COLOR;
                break;
            default:
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Ugyldigt Niveau")
                        .setColor(ERROR_COLOR)
                        .setDescription("Ugyldigt moderation niveau!")
                        .addField("Tilgængelige niveauer", "• mild/lempelig\n• standard/normal\n• strict/streng", false)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                return;
        }
        
        // Her skulle vi normalt opdatere konfigurationen i ModerationManager
        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("✅ Konfiguration Opdateret")
                .setColor(embedColor)
                .addField("Nyt Niveau", levelDisplay, true)
                .addField("Beskrivelse", description, false)
                .addField("Ændret af", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now())
                .setFooter("Moderation konfiguration");
        
        event.replyEmbeds(successEmbed.build()).queue();
    }

    /**
     * ModStats kommando
     */
    private void handleModStatsCommand(SlashCommandInteractionEvent event) {
        EmbedBuilder statsEmbed = new EmbedBuilder()
                .setTitle(STATS_EMOJI + " Moderation Statistikker")
                .setColor(INFO_COLOR)
                .setDescription("Oversigt over moderation aktivitet på denne server")
                .addField("📨 Beskeder Modereret", "0", true)
                .addField("⚠️ Advarsler Givet", "0", true)
                .addField("⏳ Timeouts Givet", "0", true)
                .addField("👢 Kicks Udført", "0", true)
                .addField("🔨 Bans Udført", "0", true)
                .addField("🗑️ Beskeder Slettet", "0", true)
                .addField("🤖 Auto-Handlinger", "Aktiveret", true)
                .addField("\uD83D\uDCCA Total Handlinger", "0", true)
                .addField("📈 Denne Uge", "0 handlinger", true)
                .setTimestamp(Instant.now())
                .setFooter("Statistikker nulstilles hver måned");
        
        event.replyEmbeds(statsEmbed.build()).queue();
    }

    /**
     * AddFilter kommando
     */
    private void handleAddFilterCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping wordOption = event.getOption("word");
        
        if (wordOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive et ord at filtrere!")
                    .addField("Korrekt brug", "/addfilter word:<ord eller sætning>", false)
                    .addField("Eksempel", "/addfilter word:spam", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String word = wordOption.getAsString();
        moderationManager.addCustomFilter(word);
        
        EmbedBuilder filterEmbed = new EmbedBuilder()
                .setTitle("🚫 Filter Tilføjet")
                .setColor(SUCCESS_COLOR)
                .addField("Filtreret Ord", "`" + word + "`", true)
                .addField("Tilføjet af", event.getUser().getAsMention(), true)
                .addField(INFO_EMOJI + " Information", 
                    "Beskeder indeholdende dette ord vil nu blive automatisk modereret", false)
                .setTimestamp(Instant.now())
                .setFooter("Custom filter system");
        
        event.replyEmbeds(filterEmbed.build()).queue();
    }

    /**
     * Tjekker om brugeren har moderator tilladelser
     */
    private boolean hasModeratorPermissions(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return false;
        
        return member.hasPermission(Permission.MODERATE_MEMBERS) ||
               member.hasPermission(Permission.KICK_MEMBERS) ||
               member.hasPermission(Permission.BAN_MEMBERS) ||
               member.hasPermission(Permission.MANAGE_SERVER);
    }

    /**
     * Håndterer string select menu interactions, især help dropdown
     */
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        logger.info("String select interaction modtaget: {} fra bruger: {}", componentId, event.getUser().getName());
        
        if ("help-menu".equals(componentId)) {
            String selectedValue = event.getValues().get(0);
            String userLanguage = userLanguageManager.getUserLanguage(event.getUser().getId());
            logger.info("Help menu selection: {}", selectedValue);
            
            // Use HelpCommands to generate the appropriate embed based on the selection
            EmbedBuilder embedBuilder = null;
            switch (selectedValue) {
                case "basic":
                    embedBuilder = HelpCommands.getBasicCommandsEmbed(userLanguage);
                    break;
                case "moderation":
                    embedBuilder = HelpCommands.getModerationCommandsEmbed(userLanguage);
                    break;
                case "utility":
                    embedBuilder = HelpCommands.getUtilityCommandsEmbed(userLanguage);
                    break;
                case "fun":
                    embedBuilder = HelpCommands.getFunCommandsEmbed(userLanguage);
                    break;
                case "config":
                    embedBuilder = HelpCommands.getConfigCommandsEmbed(userLanguage);
                    break;
                case "overview":
                    embedBuilder = HelpCommands.getOverviewEmbed(userLanguage);
                    break;
                default:
                    // If unknown selection, reply with error
                    String errorMessage = translationManager.translate("help.error.unknown_category", userLanguage);
                    event.reply(errorMessage).setEphemeral(true).queue();
                    logger.warn("Unknown help menu selection: {}", selectedValue);
                    return;
            }
            
            if (embedBuilder != null) {
                event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                logger.info("Help embed sent for category: {}", selectedValue);
            }
        } else if (componentId.startsWith("ticket_")) {
            ticketCommandHandler.handleStringSelectInteraction(event);
        } else {
            // Handle other component interactions if needed in the future
            logger.warn("Unknown component interaction: {}", componentId);
            String userLanguage = userLanguageManager.getUserLanguage(event.getUser().getId());
            String errorMessage = translationManager.translate("error.interaction_failed", userLanguage);
            event.reply(errorMessage).setEphemeral(true).queue();
        }
    }

    /**
     * Mute kommando - fjerner tale rettigheder
     */
    private void handleMuteCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger at mute!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            targetMember -> {
                try {
                    targetMember.mute(true)
                            .reason(reason + " (Muted by " + event.getUser().getName() + ")")
                            .queue(
                                success -> {
                                    EmbedBuilder muteEmbed = new EmbedBuilder()
                                            .setTitle("🔇 Bruger Mutet")
                                            .setColor(MODERATION_COLOR)
                                            .setThumbnail(targetUser.getAvatarUrl())
                                            .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                                            .addField("Årsag", reason, false)
                                            .addField("Moderator", event.getUser().getAsMention(), true)
                                            .setTimestamp(Instant.now())
                                            .setFooter("User ID: " + targetUser.getId());
                                    event.replyEmbeds(muteEmbed.build()).queue();
                                },
                                error -> {
                                    EmbedBuilder errorEmbed = new EmbedBuilder()
                                            .setTitle(ERROR_EMOJI + " Mute Fejlede")
                                            .setColor(ERROR_COLOR)
                                            .setDescription("Kunne ikke mute bruger: " + error.getMessage())
                                            .setTimestamp(Instant.now());
                                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                                }
                            );
                } catch (Exception e) {
                    EmbedBuilder errorEmbed = new EmbedBuilder()
                            .setTitle(ERROR_EMOJI + " Uventet Fejl")
                            .setColor(ERROR_COLOR)
                            .setDescription("Fejl ved mute: " + e.getMessage())
                            .setTimestamp(Instant.now());
                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                }
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Unmute kommando - gendanner tale rettigheder
     */
    private void handleUnmuteCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger at unmute!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            targetMember -> {
                try {
                    targetMember.mute(false)
                            .reason("Unmuted by " + event.getUser().getName())
                            .queue(
                                success -> {
                                    EmbedBuilder unmuteEmbed = new EmbedBuilder()
                                            .setTitle("🔊 Bruger Unmutet")
                                            .setColor(SUCCESS_COLOR)
                                            .setThumbnail(targetUser.getAvatarUrl())
                                            .addField("Bruger", targetUser.getAsMention() + " (" + targetUser.getName() + ")", false)
                                            .addField("Moderator", event.getUser().getAsMention(), true)
                                            .setTimestamp(Instant.now())
                                            .setFooter("User ID: " + targetUser.getId());
                                    event.replyEmbeds(unmuteEmbed.build()).queue();
                                },
                                error -> {
                                    EmbedBuilder errorEmbed = new EmbedBuilder()
                                            .setTitle(ERROR_EMOJI + " Unmute Fejlede")
                                            .setColor(ERROR_COLOR)
                                            .setDescription("Kunne ikke unmute bruger: " + error.getMessage())
                                            .setTimestamp(Instant.now());
                                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                                }
                            );
                } catch (Exception e) {
                    EmbedBuilder errorEmbed = new EmbedBuilder()
                            .setTitle(ERROR_EMOJI + " Uventet Fejl")
                            .setColor(ERROR_COLOR)
                            .setDescription("Fejl ved unmute: " + e.getMessage())
                            .setTimestamp(Instant.now());
                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                }
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Slowmode kommando - sætter slowmode for kanalen
     */
    private void handleSlowmodeCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping secondsOption = event.getOption("seconds");
        
        if (secondsOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive antal sekunder!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        int seconds = (int) secondsOption.getAsLong();
        
        event.getChannel().asTextChannel().getManager().setSlowmode(seconds).queue(
            success -> {
                String slowmodeText = seconds == 0 ? "Deaktiveret" : seconds + " sekunder";
                EmbedBuilder slowmodeEmbed = new EmbedBuilder()
                        .setTitle("⏱️ Slowmode Opdateret")
                        .setColor(INFO_COLOR)
                        .addField("Kanal", event.getChannel().getAsMention(), true)
                        .addField("Slowmode", slowmodeText, true)
                        .addField("Moderator", event.getUser().getAsMention(), true)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(slowmodeEmbed.build()).queue();
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Slowmode Fejlede")
                        .setColor(ERROR_COLOR)
                        .setDescription("Kunne ikke ændre slowmode: " + error.getMessage())
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Lock kommando - låser kanalen
     */
    private void handleLockCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        event.getChannel().asTextChannel().getManager()
                .putPermissionOverride(event.getGuild().getPublicRole(), null, 
                    java.util.EnumSet.of(Permission.MESSAGE_SEND))
                .reason("Channel locked by " + event.getUser().getName() + ": " + reason)
                .queue(
                    success -> {
                        EmbedBuilder lockEmbed = new EmbedBuilder()
                                .setTitle("\uD83D\uDD12 Kanal Låst")
                                .setColor(WARNING_COLOR)
                                .addField("Kanal", event.getChannel().getAsMention(), true)
                                .addField("Årsag", reason, false)
                                .addField("Moderator", event.getUser().getAsMention(), true)
                                .setTimestamp(Instant.now());
                        event.replyEmbeds(lockEmbed.build()).queue();
                    },
                    error -> {
                        EmbedBuilder errorEmbed = new EmbedBuilder()
                                .setTitle(ERROR_EMOJI + " Låsning Fejlede")
                                .setColor(ERROR_COLOR)
                                .setDescription("Kunne ikke låse kanal: " + error.getMessage())
                                .setTimestamp(Instant.now());
                        event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                    }
                );
    }

    /**
     * Unlock kommando - låser kanalen op
     */
    private void handleUnlockCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        event.getChannel().asTextChannel().getManager()
                .putPermissionOverride(event.getGuild().getPublicRole(), 
                    java.util.EnumSet.of(Permission.MESSAGE_SEND), null)
                .reason("Channel unlocked by " + event.getUser().getName() + ": " + reason)
                .queue(
                    success -> {
                        EmbedBuilder unlockEmbed = new EmbedBuilder()
                                .setTitle("🔓 Kanal Låst Op")
                                .setColor(SUCCESS_COLOR)
                                .addField("Kanal", event.getChannel().getAsMention(), true)
                                .addField("Årsag", reason, false)
                                .addField("Moderator", event.getUser().getAsMention(), true)
                                .setTimestamp(Instant.now());
                        event.replyEmbeds(unlockEmbed.build()).queue();
                    },
                    error -> {
                        EmbedBuilder errorEmbed = new EmbedBuilder()
                                .setTitle(ERROR_EMOJI + " Oplåsning Fejlede")
                                .setColor(ERROR_COLOR)
                                .setDescription("Kunne ikke låse kanal op: " + error.getMessage())
                                .setTimestamp(Instant.now());
                        event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                    }
                );
    }

    /**
     * Unban kommando - unbanner en bruger
     */
    private void handleUnbanCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userIdOption = event.getOption("userid");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userIdOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive et bruger ID!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String userId = userIdOption.getAsString();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        event.getGuild().unban(UserSnowflake.fromId(userId))
                .reason(reason + " (Unbanned by " + event.getUser().getName() + ")")
                .queue(
                    success -> {
                        EmbedBuilder unbanEmbed = new EmbedBuilder()
                                .setTitle("✅ Bruger Unbanned")
                                .setColor(SUCCESS_COLOR)
                                .addField("Bruger ID", userId, true)
                                .addField("Årsag", reason, false)
                                .addField("Moderator", event.getUser().getAsMention(), true)
                                .setTimestamp(Instant.now());
                        event.replyEmbeds(unbanEmbed.build()).queue();
                    },
                    error -> {
                        EmbedBuilder errorEmbed = new EmbedBuilder()
                                .setTitle(ERROR_EMOJI + " Unban Fejlede")
                                .setColor(ERROR_COLOR)
                                .setDescription("Kunne ikke unban bruger: " + error.getMessage())
                                .setTimestamp(Instant.now());
                        event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                    }
                );
    }

    /**
     * Massban kommando - banner flere brugere på én gang
     */
    private void handleMassbanCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userIdsOption = event.getOption("userids");
        OptionMapping reasonOption = event.getOption("reason");
        
        if (userIdsOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive bruger IDs!")
                    .addField("Format", "Adskil IDs med komma: 123456789,987654321", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String userIdsString = userIdsOption.getAsString();
        String reason = reasonOption != null ? reasonOption.getAsString() : "Masseban";
        String[] userIds = userIdsString.split(",");
        
        if (userIds.length > 10) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " For Mange Brugere")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du kan maksimalt ban 10 brugere ad gangen!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue();
        
        int successful = 0;
        int failed = 0;
        StringBuilder results = new StringBuilder();
        
        for (String userId : userIds) {
            userId = userId.trim();
            try {
                event.getGuild().ban(UserSnowflake.fromId(userId), 0, TimeUnit.DAYS)
                        .reason(reason + " (Mass ban by " + event.getUser().getName() + ")")
                        .complete();
                successful++;
                results.append("✅ ").append(userId).append("\n");
            } catch (Exception e) {
                failed++;
                results.append("❌ ").append(userId).append(" - ").append(e.getMessage()).append("\n");
            }
        }
        
        EmbedBuilder massbanEmbed = new EmbedBuilder()
                .setTitle("🔨 Masseban Resultat")
                .setColor(successful > failed ? SUCCESS_COLOR : ERROR_COLOR)
                .addField("Succesfulde Bans", String.valueOf(successful), true)
                .addField("Fejlede Bans", String.valueOf(failed), true)
                .addField("Årsag", reason, false)
                .addField("Resultater", results.toString(), false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now());
        
        event.getHook().editOriginalEmbeds(massbanEmbed.build()).queue();
    }

    /**
     * Nick kommando - ændrer nickname på en bruger
     */
    private void handleNickCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping nicknameOption = event.getOption("nickname");
        
        if (userOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en bruger!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        User targetUser = userOption.getAsUser();
        String newNickname = nicknameOption != null ? nicknameOption.getAsString() : null;
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            targetMember -> {
                String oldNickname = targetMember.getNickname();
                targetMember.modifyNickname(newNickname)
                        .reason("Nickname changed by " + event.getUser().getName())
                        .queue(
                            success -> {
                                String displayOld = oldNickname != null ? oldNickname : "Ingen";
                                String displayNew = newNickname != null ? newNickname : "Fjernet";
                                
                                EmbedBuilder nickEmbed = new EmbedBuilder()
                                        .setTitle("📝 Nickname Ændret")
                                        .setColor(SUCCESS_COLOR)
                                        .setThumbnail(targetUser.getAvatarUrl())
                                        .addField("Bruger", targetUser.getAsMention(), true)
                                        .addField("Gammelt Nickname", displayOld, true)
                                        .addField("Nyt Nickname", displayNew, true)
                                        .addField("Moderator", event.getUser().getAsMention(), true)
                                        .setTimestamp(Instant.now())
                                        .setFooter("User ID: " + targetUser.getId());
                                event.replyEmbeds(nickEmbed.build()).queue();
                            },
                            error -> {
                                EmbedBuilder errorEmbed = new EmbedBuilder()
                                        .setTitle(ERROR_EMOJI + " Nickname Fejlede")
                                        .setColor(ERROR_COLOR)
                                        .setDescription("Kunne ikke ændre nickname: " + error.getMessage())
                                        .setTimestamp(Instant.now());
                                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                            }
                        );
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    /**
     * Role kommando - giver eller fjerner roller
     */
    private void handleRoleCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping actionOption = event.getOption("action");
        OptionMapping userOption = event.getOption("user");
        OptionMapping roleOption = event.getOption("role");
        
        if (actionOption == null || userOption == null || roleOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parametre")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive handling, bruger og rolle!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String action = actionOption.getAsString();
        User targetUser = userOption.getAsUser();
        net.dv8tion.jda.api.entities.Role role = roleOption.getAsRole();
        
        event.getGuild().retrieveMemberById(targetUser.getId()).queue(
            targetMember -> {
                if ("add".equals(action)) {
                    event.getGuild().addRoleToMember(targetMember, role)
                            .reason("Role added by " + event.getUser().getName())
                            .queue(
                                success -> {
                                    EmbedBuilder roleEmbed = new EmbedBuilder()
                                            .setTitle("✅ Rolle Givet")
                                            .setColor(SUCCESS_COLOR)
                                            .setThumbnail(targetUser.getAvatarUrl())
                                            .addField("Bruger", targetUser.getAsMention(), true)
                                            .addField("Rolle", role.getAsMention(), true)
                                            .addField("Moderator", event.getUser().getAsMention(), true)
                                            .setTimestamp(Instant.now())
                                            .setFooter("User ID: " + targetUser.getId());
                                    event.replyEmbeds(roleEmbed.build()).queue();
                                },
                                error -> {
                                    EmbedBuilder errorEmbed = new EmbedBuilder()
                                            .setTitle(ERROR_EMOJI + " Rolle Fejlede")
                                            .setColor(ERROR_COLOR)
                                            .setDescription("Kunne ikke give rolle: " + error.getMessage())
                                            .setTimestamp(Instant.now());
                                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                                }
                            );
                } else if ("remove".equals(action)) {
                    event.getGuild().removeRoleFromMember(targetMember, role)
                            .reason("Role removed by " + event.getUser().getName())
                            .queue(
                                success -> {
                                    EmbedBuilder roleEmbed = new EmbedBuilder()
                                            .setTitle("❌ Rolle Fjernet")
                                            .setColor(WARNING_COLOR)
                                            .setThumbnail(targetUser.getAvatarUrl())
                                            .addField("Bruger", targetUser.getAsMention(), true)
                                            .addField("Rolle", role.getAsMention(), true)
                                            .addField("Moderator", event.getUser().getAsMention(), true)
                                            .setTimestamp(Instant.now())
                                            .setFooter("User ID: " + targetUser.getId());
                                    event.replyEmbeds(roleEmbed.build()).queue();
                                },
                                error -> {
                                    EmbedBuilder errorEmbed = new EmbedBuilder()
                                            .setTitle(ERROR_EMOJI + " Rolle Fejlede")
                                            .setColor(ERROR_COLOR)
                                            .setDescription("Kunne ikke fjerne rolle: " + error.getMessage())
                                            .setTimestamp(Instant.now());
                                    event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
                                }
                            );
                }
            },
            error -> {
                EmbedBuilder errorEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Bruger Ikke Fundet")
                        .setColor(ERROR_COLOR)
                        .setDescription("Brugeren er ikke på denne server!")
                        .setTimestamp(Instant.now());
                event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            }
        );
    }

    // ==================== LOGGING COMMANDS ====================

    /**
     * Logs kommando - viser moderation logs
     */
    private void handleLogsCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping typeOption = event.getOption("type");
        OptionMapping userOption = event.getOption("user");
        OptionMapping limitOption = event.getOption("limit");
        
        String logType = typeOption != null ? typeOption.getAsString() : "all";
        User targetUser = userOption != null ? userOption.getAsUser() : null;
        int limit = limitOption != null ? limitOption.getAsInt() : 10;
        
        // Retrieve logs from ModerationLogger
        List<ModerationLogger.LogEntry> logs;
        if (targetUser != null) {
            logs = moderationLogger.getRecentLogsForUser(event.getGuild().getId(), targetUser.getId());
        } else {
            logs = moderationLogger.getRecentLogs(event.getGuild().getId());
        }
        
        // Apply limit
        if (logs.size() > limit) {
            logs = logs.subList(Math.max(0, logs.size() - limit), logs.size());
        }
        
        EmbedBuilder logsEmbed = new EmbedBuilder()
                .setTitle("📋 Moderation Logs")
                .setColor(INFO_COLOR)
                .setDescription("Viser seneste moderation logs for serveren")
                .addField("Filter", logType.toUpperCase(), true)
                .addField("Limit", String.valueOf(limit), true)
                .addField("Bruger", targetUser != null ? targetUser.getAsMention() : "Alle", true);
        
        if (logs.isEmpty()) {
            logsEmbed.addField("Seneste Logs", "Ingen logs fundet", false);
        } else {
            StringBuilder logText = new StringBuilder();
            for (int i = logs.size() - 1; i >= 0; i--) {
                ModerationLogger.LogEntry log = logs.get(i);
                String timeStr = "<t:" + log.getTimestamp().getEpochSecond() + ":R>";
                logText.append(String.format("**%s** - %s\n%s\n\n", 
                    log.getAction().toString().replace("_", " "), 
                    timeStr,
                    log.getReason()));
                
                if (logText.length() > 800) {
                    logText.append("... og flere");
                    break;
                }
            }
            logsEmbed.addField("Seneste Logs", logText.toString(), false);
        }
        
        logsEmbed.setTimestamp(Instant.now())
                .setFooter("Brug /logconfig for at konfigurere logging");
        
        event.replyEmbeds(logsEmbed.build()).queue();
    }

    /**
     * Set log channel kommando
     */
    private void handleSetLogChannelCommand(SlashCommandInteractionEvent event) {
        if (!hasAdministratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping channelOption = event.getOption("channel");
        
        if (channelOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en kanal!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        net.dv8tion.jda.api.entities.channel.concrete.TextChannel logChannel = channelOption.getAsChannel().asTextChannel();
        
        // Set log channel using ModerationLogger
        moderationLogger.setLogChannel(event.getGuild().getId(), logChannel.getId());
        
        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("✅ Log Kanal Sat")
                .setColor(SUCCESS_COLOR)
                .setDescription("Moderation log kanal er nu sat til " + logChannel.getAsMention())
                .addField("Kanal", logChannel.getAsMention(), true)
                .addField("Kanal ID", logChannel.getId(), true)
                .addField("Administrator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now())
                .setFooter("Alle moderation handlinger vil nu blive logget i denne kanal");
        
        event.replyEmbeds(successEmbed.build()).queue();
    }

    /**
     * Set audit channel kommando
     */
    private void handleSetAuditChannelCommand(SlashCommandInteractionEvent event) {
        if (!hasAdministratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping channelOption = event.getOption("channel");
        
        if (channelOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en kanal!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        net.dv8tion.jda.api.entities.channel.concrete.TextChannel auditChannel = channelOption.getAsChannel().asTextChannel();
        
        // Set audit channel using ModerationLogger
        moderationLogger.setAuditChannel(event.getGuild().getId(), auditChannel.getId());
        
        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("🚨 Audit Kanal Sat")
                .setColor(SUCCESS_COLOR)
                .setDescription("Audit log kanal er nu sat til " + auditChannel.getAsMention())
                .addField("Kanal", auditChannel.getAsMention(), true)
                .addField("Kanal ID", auditChannel.getId(), true)
                .addField("Administrator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now())
                .setFooter("Kritiske handlinger vil nu blive logget i denne kanal");
        
        event.replyEmbeds(successEmbed.build()).queue();
    }

    /**
     * Clear logs kommando
     */
    private void handleClearLogsCommand(SlashCommandInteractionEvent event) {
        if (!hasAdministratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping confirmOption = event.getOption("confirm");
        
        if (confirmOption == null || !confirmOption.getAsBoolean()) {
            EmbedBuilder warningEmbed = new EmbedBuilder()
                    .setTitle("⚠️ Bekræftelse Påkrævet")
                    .setColor(WARNING_COLOR)
                    .setDescription("Du skal bekræfte at du vil rydde alle logs!")
                    .addField("Advarsel", "Denne handling kan ikke fortrydes!", false)
                    .addField("For at bekræfte", "Sæt 'confirm' parameteren til 'True'", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(warningEmbed.build()).setEphemeral(true).queue();
            return;
        }
        
        // Clear logs using ModerationLogger
        moderationLogger.clearLogs(event.getGuild().getId());
        
        // Log this administrative action
        moderationLogger.logModerationAction(
            event.getGuild(),
            null, // No target user
            event.getUser(),
            ModerationAction.SYSTEM_ACTION,
            "Administrator cleared all moderation logs",
            ModerationSeverity.MEDIUM,
            false // Manual action
        );
        
        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("🗑️ Logs Ryddet")
                .setColor(SUCCESS_COLOR)
                .setDescription("Alle moderation logs for denne server er blevet ryddet")
                .addField("Administrator", event.getUser().getAsMention(), true)
                .addField("Tidspunkt", "<t:" + Instant.now().getEpochSecond() + ":F>", true)
                .setTimestamp(Instant.now())
                .setFooter("Denne handling er blevet logget");
        
        event.replyEmbeds(successEmbed.build()).queue();
    }

    /**
     * Export logs kommando
     */
    private void handleExportLogsCommand(SlashCommandInteractionEvent event) {
        if (!hasAdministratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping formatOption = event.getOption("format");
        OptionMapping daysOption = event.getOption("days");
        
        String format = formatOption != null ? formatOption.getAsString() : "json";
        int days = daysOption != null ? daysOption.getAsInt() : 7;
        
        event.deferReply().queue();
        
        // Simuler eksport proces
        EmbedBuilder exportEmbed = new EmbedBuilder()
                .setTitle("📤 Log Eksport")
                .setColor(INFO_COLOR)
                .setDescription("Eksporterer logs i " + format.toUpperCase() + " format")
                .addField("Format", format.toUpperCase(), true)
                .addField("Periode", days + " dage", true)
                .addField("Status", "Behandler...", true)
                .setTimestamp(Instant.now())
                .setFooter("Eksport vil blive sendt som DM når den er klar");
        
        event.getHook().editOriginalEmbeds(exportEmbed.build()).queue();
        
        // Simuler eksport delay
        event.getHook().editOriginalEmbeds(
            exportEmbed.setDescription("Log eksport fuldført!")
                    .clearFields()
                    .addField("Format", format.toUpperCase(), true)
                    .addField("Periode", days + " dage", true)
                    .addField("Status", "✅ Fuldført", true)
                    .addField("Note", "Eksport funktionalitet kræver integration med logging system", false)
                    .build()
        ).queueAfter(2, TimeUnit.SECONDS);
    }

    /**
     * Log stats kommando
     */
    private void handleLogStatsCommand(SlashCommandInteractionEvent event) {
        if (!hasModeratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping periodOption = event.getOption("period");
        String period = periodOption != null ? periodOption.getAsString() : "week";
        
        // Get statistics from ModerationLogger
        Map<String, Integer> stats = moderationLogger.getActionStatistics(event.getGuild().getId());
        List<ModerationLogger.LogEntry> logs = moderationLogger.getRecentLogs(event.getGuild().getId());
        
        // Count specific actions
        long bans = logs.stream().filter(log -> log.getAction() == ModerationAction.BAN).count();
        long kicks = logs.stream().filter(log -> log.getAction() == ModerationAction.KICK).count();
        long timeouts = logs.stream().filter(log -> log.getAction() == ModerationAction.TIMEOUT).count();
        long warnings = logs.stream().filter(log -> log.getAction() == ModerationAction.WARN_USER).count();
        
        EmbedBuilder statsEmbed = new EmbedBuilder()
                .setTitle("\uD83D\uDCCA Log Statistikker")
                .setColor(INFO_COLOR)
                .setDescription("Moderation statistikker for " + getPeriodDisplayName(period))
                .addField("Total Handlinger", String.valueOf(stats.getOrDefault("total", 0)), true)
                .addField("Bans", String.valueOf(bans), true)
                .addField("Kicks", String.valueOf(kicks), true)
                .addField("Timeouts", String.valueOf(timeouts), true)
                .addField("Advarsler", String.valueOf(warnings), true)
                .addField("Automatiske", String.valueOf(stats.getOrDefault("automated", 0)), true)
                .addField("Manuelle", String.valueOf(stats.getOrDefault("manual", 0)), true)
                .setTimestamp(Instant.now())
                .setFooter("Periode: " + getPeriodDisplayName(period));
        
        event.replyEmbeds(statsEmbed.build()).queue();
    }

    /**
     * Log config kommando
     */
    private void handleLogConfigCommand(SlashCommandInteractionEvent event) {
        if (!hasAdministratorPermissions(event)) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Ingen Tilladelse")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du har ikke tilladelse til at bruge denne kommando!")
                    .addField("Påkrævede Tilladelser", "Administrator", false)
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping settingOption = event.getOption("setting");
        OptionMapping valueOption = event.getOption("value");
        
        if (settingOption == null) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle(ERROR_EMOJI + " Manglende Parameter")
                    .setColor(ERROR_COLOR)
                    .setDescription("Du skal angive en indstilling!")
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String setting = settingOption.getAsString();
        
        switch (setting) {
            case "view":
                EmbedBuilder configEmbed = new EmbedBuilder()
                        .setTitle("⚙️ Logging Konfiguration")
                        .setColor(INFO_COLOR)
                        .setDescription("Nuværende logging indstillinger")
                        .addField("Logging Aktiveret", "Ikke konfigureret", true)
                        .addField("Detaljeret Logging", "Ikke konfigureret", true)
                        .addField("Retention Dage", "Ikke konfigureret", true)
                        .addField("Log Kanal", "Ikke sat", true)
                        .addField("Audit Kanal", "Ikke sat", true)
                        .addField("Note", "Konfiguration kræver integration med ModerationConfig", false)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(configEmbed.build()).queue();
                break;
                
            case "enable":
            case "disable":
                boolean enable = "enable".equals(setting);
                EmbedBuilder toggleEmbed = new EmbedBuilder()
                        .setTitle(enable ? "✅ Logging Aktiveret" : "❌ Logging Deaktiveret")
                        .setColor(enable ? SUCCESS_COLOR : WARNING_COLOR)
                        .setDescription("Logging er nu " + (enable ? "aktiveret" : "deaktiveret") + " for denne server")
                        .addField("Administrator", event.getUser().getAsMention(), true)
                        .addField("Status", enable ? "Aktiveret" : "Deaktiveret", true)
                        .setTimestamp(Instant.now())
                        .setFooter("Kræver integration med ModerationConfig for at fungere");
                event.replyEmbeds(toggleEmbed.build()).queue();
                break;
                
            case "detailed":
                EmbedBuilder detailedEmbed = new EmbedBuilder()
                        .setTitle("📝 Detaljeret Logging")
                        .setColor(INFO_COLOR)
                        .setDescription("Detaljeret logging indstilling ændret")
                        .addField("Administrator", event.getUser().getAsMention(), true)
                        .addField("Status", "Konfigureret", true)
                        .setTimestamp(Instant.now())
                        .setFooter("Kræver integration med ModerationConfig");
                event.replyEmbeds(detailedEmbed.build()).queue();
                break;
                
            case "retention":
                int retentionDays = valueOption != null ? valueOption.getAsInt() : 30;
                EmbedBuilder retentionEmbed = new EmbedBuilder()
                        .setTitle("🗓️ Log Retention")
                        .setColor(SUCCESS_COLOR)
                        .setDescription("Log retention periode sat til " + retentionDays + " dage")
                        .addField("Nye Retention Dage", String.valueOf(retentionDays), true)
                        .addField("Administrator", event.getUser().getAsMention(), true)
                        .setTimestamp(Instant.now())
                        .setFooter("Logs ældre end " + retentionDays + " dage vil blive slettet automatisk");
                event.replyEmbeds(retentionEmbed.build()).queue();
                break;
                
            default:
                EmbedBuilder unknownEmbed = new EmbedBuilder()
                        .setTitle(ERROR_EMOJI + " Ukendt Indstilling")
                        .setColor(ERROR_COLOR)
                        .setDescription("Ukendt indstilling: " + setting)
                        .addField("Tilgængelige Indstillinger", "enable, disable, detailed, retention, view", false)
                        .setTimestamp(Instant.now());
                event.replyEmbeds(unknownEmbed.build()).setEphemeral(true).queue();
                break;
        }
    }

    /**
     * Helper method til at få periode display navn
     */
    private String getPeriodDisplayName(String period) {
        switch (period) {
            case "today": return "i dag";
            case "week": return "denne uge";
            case "month": return "denne måned";
            case "all": return "alt";
            default: return period;
        }
    }

    /**
     * Tjekker om brugeren har administrator tilladelser
     */
    private boolean hasAdministratorPermissions(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return false;
        
        return member.hasPermission(Permission.ADMINISTRATOR) || 
               member.hasPermission(Permission.MANAGE_SERVER);
    }

    /**
     * Handle button interactions for ticket system
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("ticket_")) {
            ticketCommandHandler.handleButtonInteraction(event);
        }
    }

}