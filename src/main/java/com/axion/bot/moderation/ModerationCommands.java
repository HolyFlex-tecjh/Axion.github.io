package com.axion.bot.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced moderation commands with advanced features
 * Integrates with AdvancedModerationSystem for powerful moderation capabilities
 * Includes anti-raid, advanced timeouts, user profiles, and comprehensive logging
 */
public class ModerationCommands {
    private static final Logger logger = LoggerFactory.getLogger(ModerationCommands.class);
    private final ModerationManager moderationManager;
    private final AdvancedModerationSystem advancedSystem;
    private final ModerationLogger moderationLogger;
    
    public ModerationCommands(ModerationManager moderationManager, AdvancedModerationSystem advancedSystem, ModerationLogger moderationLogger) {
        this.moderationManager = moderationManager;
        this.advancedSystem = advancedSystem;
        this.moderationLogger = moderationLogger;
    }
    
    /**
     * Håndterer moderation kommandoer
     */
    public boolean handleCommand(MessageReceivedEvent event, String command, String[] args) {
        Member member = event.getMember();
        if (member == null) {
            return false;
        }
        
        // Tjek om brugeren har moderation rettigheder
        if (!hasModeratorPermissions(member)) {
            event.getChannel().sendMessage("❌ Du har ikke tilladelse til at bruge moderation kommandoer.").queue();
            return true;
        }
        
        switch (command.toLowerCase()) {
            case "ban":
                return handleBanCommand(event, args);
            case "smartban":
                return handleSmartBanCommand(event, args);
            case "kick":
                return handleKickCommand(event, args);
            case "timeout":
            case "mute":
                return handleTimeoutCommand(event, args);
            case "advancedtimeout":
                return handleAdvancedTimeoutCommand(event, args);
            case "warn":
                return handleWarnCommand(event, args);
            case "unwarn":
                return handleUnwarnCommand(event, args);
            case "warnings":
                return handleWarningsCommand(event, args);
            case "purge":
            case "clear":
                return handlePurgeCommand(event, args);
            case "modconfig":
                return handleModConfigCommand(event, args);
            case "modstats":
                return handleModStatsCommand(event, args);
            case "addfilter":
                return handleAddFilterCommand(event, args);
            case "userprofile":
                return handleUserProfileCommand(event, args);
            case "raidstatus":
                return handleRaidStatusCommand(event, args);
            case "lockdown":
                return handleLockdownCommand(event, args);
            case "unlockdown":
                return handleUnlockdownCommand(event, args);
            case "setlogchannel":
                return handleSetLogChannelCommand(event, args);
            case "setauditchannel":
                return handleSetAuditChannelCommand(event, args);
            case "modlogs":
                return handleModLogsCommand(event, args);
            case "massaction":
                return handleMassActionCommand(event, args);
            default:
                return false;
        }
    }
    
    /**
     * Håndterer ban kommando
     */
    private boolean handleBanCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!ban @bruger [årsag]`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger at banne.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Ingen årsag angivet";
        
        // Tjek om vi kan interagere med brugeren
        if (!event.getGuild().getSelfMember().canInteract(targetMember)) {
            event.getChannel().sendMessage("❌ Jeg kan ikke banne denne bruger (højere rolle).").queue();
            return true;
        }
        
        // Udfør ban med logging
        event.getGuild().ban(targetMember, 0, TimeUnit.SECONDS)
                .reason(reason + " (Udført af: " + event.getAuthor().getName() + ")")
                .queue(
                    success -> {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(Color.RED)
                                .setTitle("🔨 Bruger Bannet")
                                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                                .addField("Moderator", event.getAuthor().getAsTag(), true)
                                .addField("Årsag", reason, false)
                                .setTimestamp(java.time.Instant.now());
                        
                        event.getChannel().sendMessageEmbeds(embed.build()).queue();
                        
                        // Log the action
                        moderationLogger.logModerationAction(
                            event.getGuild(), targetMember.getUser(), event.getAuthor(),
                            ModerationAction.BAN, reason, ModerationSeverity.HIGH, false
                        );
                        
                        logger.info("Banned {} by {}: {}", targetMember.getUser().getAsTag(), event.getAuthor().getAsTag(), reason);
                    },
                    error -> {
                        event.getChannel().sendMessage("❌ Kunne ikke banne brugeren: " + error.getMessage()).queue();
                        logger.error("Failed to ban user", error);
                    }
                );
        
        return true;
    }
    
    /**
     * Håndterer kick kommando
     */
    private boolean handleKickCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!kick @bruger [årsag]`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger at kicke.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Ingen årsag angivet";
        
        if (!event.getGuild().getSelfMember().canInteract(targetMember)) {
            event.getChannel().sendMessage("❌ Jeg kan ikke kicke denne bruger (højere rolle).").queue();
            return true;
        }
        
        event.getGuild().kick(targetMember)
                .reason(reason + " (Udført af: " + event.getAuthor().getName() + ")")
                .queue(
                    success -> {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(Color.ORANGE)
                                .setTitle("👢 Bruger Kicket")
                                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                                .addField("Moderator", event.getAuthor().getAsTag(), true)
                                .addField("Årsag", reason, false)
                                .setTimestamp(java.time.Instant.now());
                        
                        event.getChannel().sendMessageEmbeds(embed.build()).queue();
                        
                        // Log the action
                        moderationLogger.logModerationAction(
                            event.getGuild(), targetMember.getUser(), event.getAuthor(),
                            ModerationAction.KICK, reason, ModerationSeverity.MEDIUM, false
                        );
                        
                        logger.info("Kicked {} by {}: {}", targetMember.getUser().getAsTag(), event.getAuthor().getAsTag(), reason);
                    },
                    error -> {
                        event.getChannel().sendMessage("❌ Kunne ikke kicke brugeren: " + error.getMessage()).queue();
                        logger.error("Failed to kick user", error);
                    }
                );
        
        return true;
    }
    
    /**
     * Håndterer timeout kommando
     */
    private boolean handleTimeoutCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 2) {
            event.getChannel().sendMessage("❌ Brug: `!timeout @bruger <minutter> [årsag]`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger at give timeout.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        
        int minutes;
        try {
            minutes = Integer.parseInt(args[1]);
            if (minutes <= 0 || minutes > 10080) { // Max 7 dage
                event.getChannel().sendMessage("❌ Timeout skal være mellem 1 og 10080 minutter (7 dage).").queue();
                return true;
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ Ugyldigt antal minutter.").queue();
            return true;
        }
        
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Ingen årsag angivet";
        
        if (!event.getGuild().getSelfMember().canInteract(targetMember)) {
            event.getChannel().sendMessage("❌ Jeg kan ikke give timeout til denne bruger (højere rolle).").queue();
            return true;
        }
        
        targetMember.timeoutFor(Duration.ofMinutes(minutes))
                .reason(reason + " (Udført af: " + event.getAuthor().getName() + ")")
                .queue(
                    success -> {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(Color.YELLOW)
                                .setTitle("🔇 Bruger Timeout")
                                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                                .addField("Varighed", minutes + " minutter", true)
                                .addField("Moderator", event.getAuthor().getAsTag(), true)
                                .addField("Årsag", reason, false)
                                .setTimestamp(java.time.Instant.now());
                        
                        event.getChannel().sendMessageEmbeds(embed.build()).queue();
                        
                        // Log the action and update user profile
                        moderationLogger.logModerationAction(
                            event.getGuild(), targetMember.getUser(), event.getAuthor(),
                            ModerationAction.TIMEOUT, reason, ModerationSeverity.MEDIUM, false
                        );
                        
                        // Update user moderation profile
        UserModerationProfile profile = advancedSystem.getUserProfile(targetMember.getUser().getId(), event.getGuild().getId(), true);
        profile.setTimeoutStatus(true, Duration.ofMinutes(minutes));
                        
                        logger.info("Timed out {} for {} minutes by {}: {}", targetMember.getUser().getAsTag(), minutes, event.getAuthor().getAsTag(), reason);
                    },
                    error -> {
                        event.getChannel().sendMessage("❌ Kunne ikke give timeout til brugeren: " + error.getMessage()).queue();
                        logger.error("Failed to timeout user", error);
                    }
                );
        
        return true;
    }
    
    /**
     * Håndterer warn kommando
     */
    private boolean handleWarnCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!warn @bruger [årsag]`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger at advare.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Ingen årsag angivet";
        
        // Add warning and update user profile
        String userId = targetMember.getUser().getId();
        UserModerationProfile profile = advancedSystem.getUserProfile(userId, event.getGuild().getId(), true);
        profile.recordViolation(ModerationAction.WARN_USER, reason, ModerationSeverity.LOW, false);
        
        int warnings = profile.getWarningCount();
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("⚠️ Bruger Advaret")
                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                .addField("Advarsler", String.valueOf(warnings), true)
                .addField("Trust Score", String.valueOf(profile.getTrustScore()), true)
                .addField("Moderator", event.getAuthor().getAsTag(), true)
                .addField("Årsag", reason, false)
                .setTimestamp(java.time.Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        
        // Log the action
        moderationLogger.logModerationAction(
            event.getGuild(), targetMember.getUser(), event.getAuthor(),
            ModerationAction.WARN_USER, reason, ModerationSeverity.LOW, false
        );
        
        // Send privat besked til brugeren
        targetMember.getUser().openPrivateChannel().queue(channel -> 
            channel.sendMessage("⚠️ Du har modtaget en advarsel på **" + event.getGuild().getName() + "**: " + reason).queue()
        );
        
        logger.info("Warned {} by {}: {}", targetMember.getUser().getAsTag(), event.getAuthor().getAsTag(), reason);
        return true;
    }
    
    /**
     * Håndterer unwarn kommando
     */
    private boolean handleUnwarnCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!unwarn @bruger`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String userId = targetMember.getUser().getId();
        
        moderationManager.clearWarnings(userId, event.getGuild().getId());
        
        event.getChannel().sendMessage("✅ Fjernede alle advarsler for " + targetMember.getUser().getAsTag()).queue();
        logger.info("Cleared warnings for {} by {}", targetMember.getUser().getAsTag(), event.getAuthor().getAsTag());
        return true;
    }
    
    /**
     * Håndterer warnings kommando
     */
    private boolean handleWarningsCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!warnings @bruger`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String userId = targetMember.getUser().getId();
        UserModerationProfile profile = advancedSystem.getUserProfile(userId, event.getGuild().getId(), true);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(profile.getWarningCount() > 0 ? Color.ORANGE : Color.GREEN)
                .setTitle("📊 Bruger Advarsler")
                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                .addField("Advarsler", String.valueOf(profile.getWarningCount()), true)
                .addField("Trust Score", String.valueOf(profile.getTrustScore()), true)
                .addField("Risk Level", String.format("%.2f", profile.getRiskLevel()), true)
                .addField("Total Violations", String.valueOf(profile.getTotalViolations()), true)
                .addField("Recent Violations (7d)", String.valueOf(profile.getRecentViolationCount()), true)
                .setTimestamp(java.time.Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return true;
    }
    
    /**
     * Håndterer purge kommando
     */
    private boolean handlePurgeCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!purge <antal>`").queue();
            return true;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount <= 0 || amount > 100) {
                event.getChannel().sendMessage("❌ Antal skal være mellem 1 og 100.").queue();
                return true;
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ Ugyldigt antal.").queue();
            return true;
        }
        
        event.getChannel().getHistory().retrievePast(amount + 1).queue(messages -> {
            if (messages.size() > 1) {
                ((TextChannel) event.getChannel()).deleteMessages(messages.subList(0, messages.size() - 1)).queue(
                    success -> event.getChannel().sendMessage("✅ Slettede " + (messages.size() - 1) + " beskeder.").queue(),
                    error -> event.getChannel().sendMessage("❌ Kunne ikke slette beskeder: " + error.getMessage()).queue()
                );
            }
        });
        
        return true;
    }
    
    /**
     * Håndterer modconfig kommando
     */
    private boolean handleModConfigCommand(MessageReceivedEvent event, String[] args) {
        // Dette ville normalt vise og ændre konfiguration
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle("🔧 Moderation Konfiguration")
                .addField("Spam Beskyttelse", "✅ Aktiveret", true)
                .addField("Toxic Detection", "✅ Aktiveret", true)
                .addField("Link Beskyttelse", "✅ Aktiveret", true)
                .addField("Auto-Timeout", "✅ Aktiveret", true)
                .addField("Auto-Ban", "❌ Deaktiveret", true)
                .setTimestamp(java.time.Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return true;
    }
    
    /**
     * Håndterer modstats kommando
     */
    private boolean handleModStatsCommand(MessageReceivedEvent event, String[] args) {
        Map<String, Integer> stats = moderationLogger.getActionStatistics(event.getGuild().getId());
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("📈 Moderation Statistikker")
                .addField("Total Actions", String.valueOf(stats.getOrDefault("total", 0)), true)
                .addField("Automated Actions", String.valueOf(stats.getOrDefault("automated", 0)), true)
                .addField("Manual Actions", String.valueOf(stats.getOrDefault("manual", 0)), true)
                .addField("Anti-Raid Status", advancedSystem.getAntiRaidSystem().isRaidDetected(event.getGuild().getId()) ? "🚨 Active" : "✅ Clear", true)
                .addField("High Risk Users", String.valueOf(advancedSystem.getHighRiskUserCount(event.getGuild().getId())), true)
                .addField("System Status", "🟢 Online", true)
                .setTimestamp(java.time.Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return true;
    }
    
    /**
     * Håndterer addfilter kommando
     */
    private boolean handleAddFilterCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!addfilter <ord/mønster>`").queue();
            return true;
        }
        
        String filter = String.join(" ", args);
        moderationManager.addCustomFilter(filter);
        
        event.getChannel().sendMessage("✅ Tilføjede custom filter: `" + filter + "`").queue();
        return true;
    }
    
    /**
     * Handle smart ban command with advanced analysis
     */
    private boolean handleSmartBanCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!smartban @bruger [årsag]`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger at banne.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Smart ban - automated analysis";
        
        // Use advanced system for smart ban
        advancedSystem.executeSmartBan(event.getGuild(), targetMember.getUser(), event.getAuthor(), reason);
        
        event.getChannel().sendMessage("🤖 Smart ban executed with advanced analysis.").queue();
        return true;
    }
    
    /**
     * Handle advanced timeout with intelligent duration
     */
    private boolean handleAdvancedTimeoutCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!advancedtimeout @bruger [årsag]`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Advanced timeout - intelligent duration";
        
        // Use advanced system for intelligent timeout
        advancedSystem.executeAdvancedTimeout(event.getGuild(), targetMember.getUser(), event.getAuthor(), reason);
        
        event.getChannel().sendMessage("🤖 Advanced timeout applied with intelligent duration calculation.").queue();
        return true;
    }
    
    /**
     * Handle user profile command
     */
    private boolean handleUserProfileCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!userprofile @bruger`").queue();
            return true;
        }
        
        List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
        if (mentionedMembers.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en bruger.").queue();
            return true;
        }
        
        Member targetMember = mentionedMembers.get(0);
        UserModerationProfile profile = advancedSystem.getUserProfile(targetMember.getUser().getId(), event.getGuild().getId(), true);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(profile.isHighRisk() ? Color.RED : profile.isLowRisk() ? Color.GREEN : Color.YELLOW)
                .setTitle("👤 User Moderation Profile")
                .addField("User", targetMember.getUser().getAsTag(), true)
                .addField("Trust Score", String.valueOf(profile.getTrustScore()), true)
                .addField("Risk Level", String.format("%.2f", profile.getRiskLevel()), true)
                .addField("Total Violations", String.valueOf(profile.getTotalViolations()), true)
                .addField("Warnings", String.valueOf(profile.getWarningCount()), true)
                .addField("Timeouts", String.valueOf(profile.getTimeoutCount()), true)
                .addField("Messages", String.valueOf(profile.getMessageCount()), true)
                .addField("Recent Rate", String.format("%.1f msg/hr", profile.getRecentMessageRate()), true)
                .addField("Status", profile.isCurrentlyTimedOut() ? "🔇 Timed Out" : "✅ Active", true)
                .setTimestamp(Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return true;
    }
    
    /**
     * Handle raid status command
     */
    private boolean handleRaidStatusCommand(MessageReceivedEvent event, String[] args) {
        AntiRaidSystem.RaidStatus status = advancedSystem.getAntiRaidSystem().getRaidStatus(event.getGuild().getId());
        
        // Handle null status (no raid activity recorded)
        if (status == null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("🛡️ Anti-Raid Status")
                    .addField("Raid Detected", "✅ NO", true)
                    .addField("Raid Type", "None", true)
                    .addField("Enhanced Verification", "❌ Inactive", true)
                    .addField("Start Time", "N/A", true)
                    .addField("Status", "🟢 Inactive", true)
                    .addField("System", "🟢 Online", true)
                    .setTimestamp(Instant.now());
            
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
            return true;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(status.isActive() ? Color.RED : Color.GREEN)
                .setTitle("🛡️ Anti-Raid Status")
                .addField("Raid Detected", status.isActive() ? "🚨 YES" : "✅ NO", true)
                .addField("Raid Type", status.getType() != null ? status.getType().toString() : "None", true)
                .addField("Enhanced Verification", status.isEnhancedVerification() ? "✅ Active" : "❌ Inactive", true)
                .addField("Start Time", status.getStartTime() != null ? status.getStartTime().toString() : "N/A", true)
                .addField("Status", status.isActive() ? "🔴 Active" : "🟢 Inactive", true)
                .addField("System", "🟢 Online", true)
                .setTimestamp(Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return true;
    }
    
    /**
     * Handle lockdown command
     */
    private boolean handleLockdownCommand(MessageReceivedEvent event, String[] args) {
        if (!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ Kun administratorer kan aktivere lockdown.").queue();
            return true;
        }
        
        String reason = args.length > 0 ? String.join(" ", args) : "Manual lockdown activated";
        advancedSystem.getAntiRaidSystem().activateServerLockdown(event.getGuild().getId(), reason);
        
        event.getChannel().sendMessage("🔒 Server lockdown activated. New members will be restricted.").queue();
        return true;
    }
    
    /**
     * Handle unlockdown command
     */
    private boolean handleUnlockdownCommand(MessageReceivedEvent event, String[] args) {
        if (!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ Kun administratorer kan deaktivere lockdown.").queue();
            return true;
        }
        
        advancedSystem.getAntiRaidSystem().deactivateServerLockdown(event.getGuild().getId());
        
        event.getChannel().sendMessage("🔓 Server lockdown deactivated. Normal operations resumed.").queue();
        return true;
    }
    
    /**
     * Handle set log channel command
     */
    private boolean handleSetLogChannelCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!setlogchannel #channel`").queue();
            return true;
        }
        
        List<net.dv8tion.jda.api.entities.channel.middleman.GuildChannel> mentionedChannels = event.getMessage().getMentions().getChannels();
        if (mentionedChannels.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en kanal.").queue();
            return true;
        }
        
        String channelId = mentionedChannels.get(0).getId();
        moderationLogger.setLogChannel(event.getGuild().getId(), channelId);
        
        event.getChannel().sendMessage("✅ Moderation log kanal sat til " + mentionedChannels.get(0).getAsMention()).queue();
        return true;
    }
    
    /**
     * Handle set audit channel command
     */
    private boolean handleSetAuditChannelCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("❌ Brug: `!setauditchannel #channel`").queue();
            return true;
        }
        
        List<net.dv8tion.jda.api.entities.channel.middleman.GuildChannel> mentionedChannels = event.getMessage().getMentions().getChannels();
        if (mentionedChannels.isEmpty()) {
            event.getChannel().sendMessage("❌ Du skal nævne en kanal.").queue();
            return true;
        }
        
        String channelId = mentionedChannels.get(0).getId();
        moderationLogger.setAuditChannel(event.getGuild().getId(), channelId);
        
        event.getChannel().sendMessage("✅ Audit log kanal sat til " + mentionedChannels.get(0).getAsMention()).queue();
        return true;
    }
    
    /**
     * Handle mod logs command
     */
    private boolean handleModLogsCommand(MessageReceivedEvent event, String[] args) {
        List<ModerationLogger.LogEntry> recentLogs = moderationLogger.getRecentLogs(event.getGuild().getId());
        
        if (recentLogs.isEmpty()) {
            event.getChannel().sendMessage("📋 Ingen moderation logs fundet.").queue();
            return true;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle("📋 Recent Moderation Logs")
                .setTimestamp(Instant.now());
        
        int count = Math.min(10, recentLogs.size());
        for (int i = recentLogs.size() - count; i < recentLogs.size(); i++) {
            ModerationLogger.LogEntry log = recentLogs.get(i);
            embed.addField(
                log.getAction().toString(),
                String.format("%s - %s", log.getReason(), log.isAutomated() ? "Auto" : "Manual"),
                false
            );
        }
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return true;
    }
    
    /**
     * Handle mass action command
     */
    private boolean handleMassActionCommand(MessageReceivedEvent event, String[] args) {
        if (!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ Kun administratorer kan bruge mass actions.").queue();
            return true;
        }
        
        if (args.length < 2) {
            event.getChannel().sendMessage("❌ Brug: `!massaction <action> <criteria>`\nActions: ban, kick, timeout\nCriteria: high_risk, recent_violations").queue();
            return true;
        }
        
        String action = args[0].toLowerCase();
        String criteria = args[1].toLowerCase();
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Mass action executed";
        
        int affected = advancedSystem.executeMassAction(event.getGuild(), action, criteria, reason, event.getAuthor());
        
        event.getChannel().sendMessage(String.format("⚡ Mass action completed. %d users affected.", affected)).queue();
        return true;
    }
    
    /**
     * Tjekker om en bruger har moderation rettigheder
     */
    private boolean hasModeratorPermissions(Member member) {
        return member.hasPermission(net.dv8tion.jda.api.Permission.MODERATE_MEMBERS) ||
               member.hasPermission(net.dv8tion.jda.api.Permission.KICK_MEMBERS) ||
               member.hasPermission(net.dv8tion.jda.api.Permission.BAN_MEMBERS) ||
               member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR);
    }
}

// Using stub classes from ModerationManager.java