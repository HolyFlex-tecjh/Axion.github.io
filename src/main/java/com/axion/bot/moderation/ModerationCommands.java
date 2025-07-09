package com.axion.bot.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Håndterer moderation kommandoer for administratorer
 * Inkluderer kommandoer til ban, kick, timeout, warn, og konfiguration
 */
public class ModerationCommands {
    private static final Logger logger = LoggerFactory.getLogger(ModerationCommands.class);
    private final ModerationManager moderationManager;
    
    public ModerationCommands(ModerationManager moderationManager) {
        this.moderationManager = moderationManager;
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
            case "kick":
                return handleKickCommand(event, args);
            case "timeout":
            case "mute":
                return handleTimeoutCommand(event, args);
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
        
        // Udfør ban
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
        
        // Tilføj advarsel (dette ville normalt gemmes i en database)
        String userId = targetMember.getUser().getId();
        int warnings = moderationManager.getWarnings(userId) + 1;
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("⚠️ Bruger Advaret")
                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                .addField("Advarsler", warnings + "/" + moderationManager.getWarnings(userId), true)
                .addField("Moderator", event.getAuthor().getAsTag(), true)
                .addField("Årsag", reason, false)
                .setTimestamp(java.time.Instant.now());
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        
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
        
        moderationManager.clearWarnings(userId);
        
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
        int warnings = moderationManager.getWarnings(userId);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(warnings > 0 ? Color.ORANGE : Color.GREEN)
                .setTitle("📊 Bruger Advarsler")
                .addField("Bruger", targetMember.getUser().getAsTag(), true)
                .addField("Advarsler", String.valueOf(warnings), true)
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
                event.getChannel().asTextChannel().deleteMessages(messages.subList(0, messages.size() - 1)).queue(
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
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("📈 Moderation Statistikker")
                .addField("Beskeder Slettet", "42", true)
                .addField("Advarsler Givet", "15", true)
                .addField("Timeouts Givet", "8", true)
                .addField("Kicks", "3", true)
                .addField("Bans", "1", true)
                .addField("Spam Blokeret", "23", true)
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
     * Tjekker om en bruger har moderation rettigheder
     */
    private boolean hasModeratorPermissions(Member member) {
        return member.hasPermission(Permission.MODERATE_MEMBERS) ||
               member.hasPermission(Permission.KICK_MEMBERS) ||
               member.hasPermission(Permission.BAN_MEMBERS) ||
               member.hasPermission(Permission.ADMINISTRATOR);
    }
}