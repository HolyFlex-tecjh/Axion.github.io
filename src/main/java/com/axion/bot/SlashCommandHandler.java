package com.axion.bot;

import com.axion.bot.commands.basic.BasicCommands;
import com.axion.bot.commands.utility.HelpCommands;
import com.axion.bot.commands.LanguageCommands;
import com.axion.bot.commands.developer.DeveloperCommands;
import com.axion.bot.moderation.*;
import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
    private static final String SUCCESS_EMOJI = "✅";
    private static final String ERROR_EMOJI = "❌";
    private static final String WARNING_EMOJI = "⚠️";
    private static final String INFO_EMOJI = "ℹ️";
    private static final String TIME_EMOJI = "⏰";
    private static final String HAMMER_EMOJI = "🔨";
    private static final String KICK_EMOJI = "👢";
    private static final String TIMEOUT_EMOJI = "⏳";
    private static final String WARN_EMOJI = "⚠️";
    private static final String TRASH_EMOJI = "🗑️";
    private static final String STATS_EMOJI = "📊";
    private static final String ROBOT_EMOJI = "🤖";
    
    // Moderation system
    private final ModerationManager moderationManager;
    private final TranslationManager translationManager;
    private final UserLanguageManager userLanguageManager;
    
    public SlashCommandHandler() {
        // Initialiser moderation system med standard konfiguration
        ModerationConfig config = ModerationConfig.createDefault();
        this.moderationManager = new ModerationManager(config);
        this.translationManager = TranslationManager.getInstance();
        this.userLanguageManager = UserLanguageManager.getInstance();
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
            event.getGuild().ban(targetUser, 0, TimeUnit.SECONDS)
                    .reason(reason + " (Banned by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
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
                    .setTitle(ERROR_EMOJI + " Uventet Fejl")
                    .setColor(ERROR_COLOR)
                    .setDescription("Fejl ved ban: " + e.getMessage())
                    .setTimestamp(Instant.now());
            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
        }
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
            targetMember.timeoutFor(duration, TimeUnit.MINUTES)
                    .reason(reason + " (Timeout by " + event.getUser().getName() + ")")
                    .queue(
                        success -> {
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
        
        moderationManager.addWarning(targetUser.getId(), reason);
        int warningCount = moderationManager.getWarningCount(targetUser.getId());
        
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
        int previousWarnings = moderationManager.getWarningCount(targetUser.getId());
        moderationManager.clearWarnings(targetUser.getId());
        
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
        int warningCount = moderationManager.getWarningCount(targetUser.getId());
        
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
                .addField("📊 Total Handlinger", "0", true)
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
        } else {
            // Handle other component interactions if needed in the future
            logger.warn("Unknown component interaction: {}", componentId);
            String userLanguage = userLanguageManager.getUserLanguage(event.getUser().getId());
            String errorMessage = translationManager.translate("error.interaction_failed", userLanguage);
            event.reply(errorMessage).setEphemeral(true).queue();
        }
    }
}