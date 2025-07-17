package com.axion.bot.moderation;

import com.axion.bot.database.DatabaseService;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Hovedklasse for moderation systemet
 * Håndterer auto-moderation, spam beskyttelse, toxic chat detection og custom filters
 */
public class ModerationManager {
    private static final Logger logger = LoggerFactory.getLogger(ModerationManager.class);
    
    // Spam detection (keeping some in-memory for performance)
    private final Map<String, List<Instant>> userMessageTimes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userMessageHistory = new ConcurrentHashMap<>();
    
    // Configuration
    private final ModerationConfig config;
    private final DatabaseService databaseService;
    
    // Filters
    private final List<Pattern> bannedWords = new ArrayList<>();
    private final List<Pattern> suspiciousPatterns = new ArrayList<>();
    private final List<Pattern> advancedSpamPatterns = new ArrayList<>();
    private final Set<String> suspiciousFileExtensions = new HashSet<>();
    
    public ModerationManager(ModerationConfig config, DatabaseService databaseService) {
        this.config = config;
        this.databaseService = databaseService;
        initializeFilters();
        initializeAdvancedPatterns();
        initializeSuspiciousFileExtensions();
    }
    
    /**
     * Initialiserer spam og toxic content filters
     */
    private void initializeFilters() {
        // Tilføj almindelige bannede ord (eksempler)
        bannedWords.add(Pattern.compile("\\b(spam|scam|hack|phishing|malware)\\b", Pattern.CASE_INSENSITIVE));
        bannedWords.add(Pattern.compile("\\b(free\\s+nitro|discord\\s+nitro\\s+free)\\b", Pattern.CASE_INSENSITIVE));
        bannedWords.add(Pattern.compile("\\b(click\\s+here|download\\s+now|limited\\s+time)\\b", Pattern.CASE_INSENSITIVE));
        
        // Tilføj mistænkelige mønstre
        suspiciousPatterns.add(Pattern.compile("[A-Z]{10,}", Pattern.CASE_INSENSITIVE)); // Mange store bogstaver
        suspiciousPatterns.add(Pattern.compile("(.)\\1{5,}", Pattern.CASE_INSENSITIVE)); // Gentagne karakterer
        suspiciousPatterns.add(Pattern.compile("@everyone|@here", Pattern.CASE_INSENSITIVE)); // Mass mentions
        suspiciousPatterns.add(Pattern.compile("\\b\\d{4}\\s*-\\s*\\d{4}\\s*-\\s*\\d{4}\\s*-\\s*\\d{4}\\b")); // Credit card patterns
    }
    
    /**
     * Initialiserer avancerede spam detection mønstre
     */
    private void initializeAdvancedPatterns() {
        // Avancerede spam mønstre
        advancedSpamPatterns.add(Pattern.compile("(.)\\1{3,}", Pattern.CASE_INSENSITIVE)); // Gentagne karakterer
        advancedSpamPatterns.add(Pattern.compile("[!@#$%^&*()]{5,}", Pattern.CASE_INSENSITIVE)); // Mange specialtegn
        advancedSpamPatterns.add(Pattern.compile("\\b(join|click|visit|check)\\s+(this|my|our)\\s+(server|link|website)\\b", Pattern.CASE_INSENSITIVE));
        advancedSpamPatterns.add(Pattern.compile("\\b(dm\\s+me|private\\s+message)\\b", Pattern.CASE_INSENSITIVE));
        advancedSpamPatterns.add(Pattern.compile("\\b(buy|sell|trade)\\s+(cheap|fast|instant)\\b", Pattern.CASE_INSENSITIVE));
    }
    
    /**
     * Initialiserer mistænkelige fil extensions
     */
    private void initializeSuspiciousFileExtensions() {
        suspiciousFileExtensions.add(".exe");
        suspiciousFileExtensions.add(".bat");
        suspiciousFileExtensions.add(".cmd");
        suspiciousFileExtensions.add(".scr");
        suspiciousFileExtensions.add(".pif");
        suspiciousFileExtensions.add(".com");
        suspiciousFileExtensions.add(".jar");
        suspiciousFileExtensions.add(".vbs");
        suspiciousFileExtensions.add(".js");
        suspiciousFileExtensions.add(".ps1");
    }
    
    /**
     * Hovedmetode til at moderere en besked
     */
    public ModerationResult moderateMessage(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String content = event.getMessage().getContentRaw();
        Member member = event.getMember();
        
        // Skip bots og admins
        if (author.isBot() || (member != null && member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR))) {
            return ModerationResult.allowed();
        }
        
        String userId = author.getId();
        
        // Tjek for temp ban
        if (databaseService.isTempBanned(userId, event.getGuild().getId())) {
            return ModerationResult.ban("Bruger er midlertidigt bannet", ModerationAction.BAN);
        }
        
        // Opdater besked historik
        updateMessageHistory(userId, content);
        
        // Tjek spam med forbedret detection
        if (config.isSpamProtectionEnabled()) {
            ModerationResult spamResult = checkAdvancedSpam(userId, content, event);
            if (!spamResult.isAllowed()) {
                databaseService.logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    "SPAM_DETECTION", spamResult.getReason(), event.getGuild().getId(), event.getChannel().getId(), 
                    event.getMessage().getId(), ModerationSeverity.HIGH.ordinal(), true);
                return spamResult;
            }
        }
        
        // Tjek toxic content
        if (config.isToxicDetectionEnabled()) {
            ModerationResult toxicResult = checkToxicContent(content);
            if (!toxicResult.isAllowed()) {
                databaseService.logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    "TOXIC_CONTENT", toxicResult.getReason(), event.getGuild().getId(), event.getChannel().getId(), 
                    event.getMessage().getId(), ModerationSeverity.HIGH.ordinal(), true);
                return toxicResult;
            }
        }
        
        // Tjek attachments
        ModerationResult attachmentResult = checkAttachments(event);
        if (!attachmentResult.isAllowed()) {
                databaseService.logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    "ATTACHMENT_CHECK", attachmentResult.getReason(), event.getGuild().getId(), event.getChannel().getId(), 
                    event.getMessage().getId(), ModerationSeverity.HIGH.ordinal(), true);
                return attachmentResult;
            }
        
        // Tjek custom filters
        ModerationResult customResult = checkCustomFilters(content);
        if (!customResult.isAllowed()) {
                databaseService.logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    "CUSTOM_FILTER", customResult.getReason(), event.getGuild().getId(), event.getChannel().getId(), 
                    event.getMessage().getId(), ModerationSeverity.HIGH.ordinal(), true);
                return customResult;
            }
        
        // Tjek link protection
        if (config.isLinkProtectionEnabled()) {
            ModerationResult linkResult = checkLinkProtection(content);
            if (!linkResult.isAllowed()) {
                databaseService.logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    "LINK_PROTECTION", linkResult.getReason(), event.getGuild().getId(), event.getChannel().getId(), 
                    event.getMessage().getId(), ModerationSeverity.HIGH.ordinal(), true);
                return linkResult;
            }
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Avanceret spam detection med forbedret logik
     */
    private ModerationResult checkAdvancedSpam(String userId, String content, MessageReceivedEvent event) {
        Instant now = Instant.now();
        
        // Få brugerens besked tider
        List<Instant> messageTimes = userMessageTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        
        // Fjern gamle beskeder (ældre end 1 minut)
        messageTimes.removeIf(time -> Duration.between(time, now).toMinutes() >= 1);
        
        // Tilføj nuværende besked tid
        messageTimes.add(now);
        
        // Tjek besked frekvens
        if (messageTimes.size() > config.getMaxMessagesPerMinute()) {
            databaseService.incrementViolationCount(userId, event.getGuild().getId());
            return escalateBasedOnViolations(userId, event.getGuild().getId(), "Spam detected: For mange beskeder per minut");
        }
        
        // Tjek for identiske beskeder
        List<String> recentMessages = userMessageHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        recentMessages.add(content);
        if (recentMessages.size() > 10) {
            recentMessages.remove(0); // Hold kun de sidste 10 beskeder
        }
        
        long identicalCount = recentMessages.stream()
                .filter(msg -> msg.equals(content))
                .count();
        
        if (identicalCount >= 3) {
            databaseService.incrementViolationCount(userId, event.getGuild().getId());
            return escalateBasedOnViolations(userId, event.getGuild().getId(), "Spam detected: Identiske beskeder");
        }
        
        // Tjek for avancerede spam mønstre
        for (Pattern pattern : advancedSpamPatterns) {
            if (pattern.matcher(content).find()) {
                databaseService.incrementViolationCount(userId, event.getGuild().getId());
                return escalateBasedOnViolations(userId, event.getGuild().getId(), "Spam detected: Mistænkeligt mønster");
            }
        }
        
        // Tjek for hurtig typing (meget lange beskeder på kort tid)
        if (content.length() > 200 && messageTimes.size() >= 2) {
            Instant lastMessage = messageTimes.get(messageTimes.size() - 2);
            if (Duration.between(lastMessage, now).toSeconds() < 5) {
                return ModerationResult.moderate("Mulig spam: Meget hurtig typing", ModerationAction.DELETE_MESSAGE);
            }
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Tjekker for toxic indhold
     */
    private ModerationResult checkToxicContent(String content) {
        // Tjek for bannede ord
        for (Pattern pattern : bannedWords) {
            if (pattern.matcher(content).find()) {
                return ModerationResult.moderate("Upassende sprog detekteret", ModerationAction.DELETE_MESSAGE);
            }
        }
        
        // Tjek for mistænkelige mønstre
        for (Pattern pattern : suspiciousPatterns) {
            if (pattern.matcher(content).find()) {
                return ModerationResult.moderate("Mistænkeligt mønster detekteret", ModerationAction.DELETE_MESSAGE);
            }
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Tjekker custom filters
     */
    private ModerationResult checkCustomFilters(String content) {
        // Implementer custom filter logik her
        // Dette kan udvides med database-baserede filters
        return ModerationResult.allowed();
    }
    
    /**
     * Tjekker for link protection
     */
    private ModerationResult checkLinkProtection(String content) {
        // Tæl antal links i beskeden
        long linkCount = Pattern.compile("https?://[^\\s]+")
                .matcher(content)
                .results()
                .count();
        
        if (linkCount > config.getMaxLinksPerMessage()) {
            return ModerationResult.moderate("For mange links i besked", ModerationAction.DELETE_MESSAGE);
        }
        
        // Tjek for mistænkelige domæner
        Pattern suspiciousLinks = Pattern.compile("(bit\\.ly|tinyurl|t\\.co|discord\\.gg/[a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE);
        if (suspiciousLinks.matcher(content).find()) {
            return ModerationResult.moderate("Mistænkeligt link detekteret", ModerationAction.DELETE_MESSAGE);
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Tjekker attachments for mistænkelige filer
     */
    private ModerationResult checkAttachments(MessageReceivedEvent event) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        
        for (Message.Attachment attachment : attachments) {
            String fileName = attachment.getFileName().toLowerCase();
            
            // Tjek for mistænkelige fil extensions
            for (String extension : suspiciousFileExtensions) {
                if (fileName.endsWith(extension)) {
                    return ModerationResult.moderate("Mistænkelig fil type: " + extension, ModerationAction.DELETE_MESSAGE);
                }
            }
            
            // Tjek fil størrelse (over 50MB)
            if (attachment.getSize() > 50 * 1024 * 1024) {
                return ModerationResult.moderate("Fil for stor: " + attachment.getSize() + " bytes", ModerationAction.DELETE_MESSAGE);
            }
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Opdaterer besked historik for en bruger
     */
    private void updateMessageHistory(String userId, String content) {
        List<String> history = userMessageHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        history.add(content);
        
        // Hold kun de sidste 20 beskeder
        if (history.size() > 20) {
            history.remove(0);
        }
    }
    
    // incrementViolationCount method moved to DatabaseService
    
    /**
     * Eskalerer baseret på antal violations
     */
    private ModerationResult escalateBasedOnViolations(String userId, String guildId, String reason) {
        int violations = databaseService.getViolationCount(userId, guildId);
        
        if (violations >= 5) {
            // Temp ban for 24 timer
            databaseService.addTempBan(userId, guildId, Instant.now().plus(24, ChronoUnit.HOURS), reason);
            return ModerationResult.ban(reason + " (5+ violations - 24h temp ban)", ModerationAction.BAN);
        } else if (violations >= 3) {
            return ModerationResult.moderate(reason + " (3+ violations)", ModerationAction.DELETE_AND_TIMEOUT);
        } else {
            return ModerationResult.moderate(reason, ModerationAction.DELETE_MESSAGE);
        }
    }
    
    // isTempBanned method moved to DatabaseService
    
    // logModerationAction method moved to DatabaseService
    
    /**
     * Får moderation logs for en bruger
     */
    public List<ModerationLog> getModerationLogs(String userId, String guildId) {
        return databaseService.getModerationLogs(userId, guildId, 100);
    }
    
    /**
     * Fjerner temp ban for en bruger
     */
    public void removeTempBan(String userId, String guildId) {
        databaseService.removeTempBan(userId, guildId);
        logger.info("Temp ban fjernet for bruger: {}", userId);
    }
    
    /**
     * Flagger en bruger til manuel gennemgang
     */
    private void flagUserForReview(Guild guild, Member member, String reason) {
        if (member == null) return;
        
        String userId = member.getUser().getId();
        String username = member.getUser().getName();
        
        // Log handlingen
        databaseService.logModerationAction(userId, username, "SYSTEM", "AutoMod", 
                          "FLAG_FOR_REVIEW", reason, guild.getId(), "", "", ModerationSeverity.LOW.ordinal(), true);
        
        logger.warn("User flagged for review: {} (ID: {}) - Reason: {}", username, userId, reason);
    }
    
    /**
     * Får temp ban status for en bruger
     */
    public boolean getTempBanStatus(String userId, String guildId) {
        return databaseService.isTempBanned(userId, guildId);
    }
    
    /**
     * Nulstiller violation count for en bruger
     */
    public void resetViolationCount(String userId, String guildId) {
        databaseService.resetViolationCount(userId, guildId);
        logger.info("Violation count nulstillet for bruger: {}", userId);
    }
    
    /**
      * Får violation count for en bruger
      */
     public int getViolationCount(String userId, String guildId) {
         return databaseService.getViolationCount(userId, guildId);
     }
     
     /**
      * Får total antal sporede brugere
      */
     public int getTotalTrackedUsers() {
         return userMessageTimes.size();
     }
     
     /**
      * Får antal brugere med aktive violations
      */
     public int getActiveViolationsCount() {
         // This would need a new database query to count active violations
         return 0; // Placeholder - implement in DatabaseService if needed
     }
     
     /**
      * Får antal aktive temp bans
      */
     public int getActiveTempBansCount() {
         // This would need a new database query to count active temp bans
         return 0; // Placeholder - implement in DatabaseService if needed
     }
     
     /**
      * Tilføjer en temp ban
      */
     public void addTempBan(String userId, String guildId, int hours, String reason) {
         Instant expiry = Instant.now().plus(hours, ChronoUnit.HOURS);
         databaseService.addTempBan(userId, guildId, expiry, reason);
         logger.info("Temp ban tilføjet for bruger {} i {} timer", userId, hours);
     }
     
     /**
     * Får alle aktive temp bans
     */
    public Map<String, Instant> getActiveTempBans() {
        return databaseService.getActiveTempBans();
    }
     
     /**
      * Rydder op i udløbne temp bans
      */
     public void cleanupExpiredTempBans() {
         databaseService.cleanupExpiredTempBans();
     }
     
     /**
      * Får moderation statistikker for en guild
      */
     public ModerationStats getModerationStats(String guildId) {
         // Create a new ModerationStats with default values
         ModerationStats stats = new ModerationStats(0, 0, 0, 0);
         // Set any default values if needed
         return stats;
     }
     
     /**
      * Henter seneste moderation logs
      */
     public List<ModerationLog> getRecentModerationLogs(String guildId, int limit) {
         // This would need a new database query to get recent logs for guild
         return new ArrayList<>(); // Placeholder - implement in DatabaseService if needed
     }
    
    /**
     * Udfører moderation handling
     */
    public void executeModerationAction(MessageReceivedEvent event, ModerationResult result) {
        if (result.isAllowed()) {
            return;
        }
        
        Member member = event.getMember();
        Guild guild = event.getGuild();
        
        switch (result.getAction()) {
            case DELETE_MESSAGE:
                event.getMessage().delete().queue(
                    success -> logger.info("Slettet besked fra {}: {}", event.getAuthor().getName(), result.getReason()),
                    error -> logger.error("Kunne ikke slette besked", error)
                );
                
                // Send advarsel til brugeren
                event.getAuthor().openPrivateChannel().queue(channel -> 
                    channel.sendMessage("⚠️ Din besked blev slettet: " + result.getReason()).queue()
                );
                break;
                
            case TIMEOUT:
                if (member != null && guild.getSelfMember().canInteract(member)) {
                    member.timeoutFor(Duration.ofMinutes(5)).queue(
                        success -> logger.info("Timeout givet til {}: {}", member.getUser().getName(), result.getReason()),
                        error -> logger.error("Kunne ikke give timeout", error)
                    );
                }
                break;
                
            case KICK:
                if (member != null && guild.getSelfMember().canInteract(member)) {
                    guild.kick(member).reason(result.getReason()).queue(
                        success -> logger.info("Kicked {}: {}", member.getUser().getName(), result.getReason()),
                        error -> logger.error("Kunne ikke kicke bruger", error)
                    );
                }
                break;
                
            case BAN:
                if (member != null && guild.getSelfMember().canInteract(member)) {
                    guild.ban(member, 0, TimeUnit.SECONDS).reason(result.getReason()).queue(
                        success -> logger.info("Banned {}: {}", member.getUser().getName(), result.getReason()),
                        error -> logger.error("Kunne ikke banne bruger", error)
                    );
                }
                break;
                
            case TEMP_BAN:
                addTempBan(event.getAuthor().getId(), event.getGuild().getId(), 24, result.getReason());
                if (member != null && guild.getSelfMember().canInteract(member)) {
                    guild.ban(member, 0, TimeUnit.SECONDS).reason(result.getReason()).queue(
                        success -> logger.info("Temp banned {}: {}", member.getUser().getName(), result.getReason()),
                        error -> logger.error("Kunne ikke temp banne bruger", error)
                    );
                }
                break;
                
            case FLAG_FOR_REVIEW:
                flagUserForReview(event.getGuild(), event.getMember(), result.getReason());
                break;
                
            case LOG_ONLY:
                logger.info("LOG_ONLY moderation action for user {}: {}", event.getAuthor().getName(), result.getReason());
                break;
            case WARN_USER:
                event.getAuthor().openPrivateChannel().queue(channel ->
                    channel.sendMessage("⚠️ Advarsel: " + result.getReason()).queue()
                );
                logger.info("WARN_USER moderation action for user {}: {}", event.getAuthor().getName(), result.getReason());
                break;
            case DELETE_AND_TIMEOUT:
                event.getMessage().delete().queue(
                    success -> logger.info("Slettet besked fra {}: {}", event.getAuthor().getName(), result.getReason()),
                    error -> logger.error("Kunne ikke slette besked", error)
                );
                if (member != null && guild.getSelfMember().canInteract(member)) {
                    member.timeoutFor(Duration.ofMinutes(5)).queue(
                        success -> logger.info("Timeout givet til {}: {}", member.getUser().getName(), result.getReason()),
                        error -> logger.error("Kunne ikke give timeout", error)
                    );
                }
                break;
            case DELETE_AND_WARN:
                event.getMessage().delete().queue(
                    success -> logger.info("Slettet besked fra {}: {}", event.getAuthor().getName(), result.getReason()),
                    error -> logger.error("Kunne ikke slette besked", error)
                );
                event.getAuthor().openPrivateChannel().queue(channel ->
                    channel.sendMessage("⚠️ Din besked blev slettet og du har fået en advarsel: " + result.getReason()).queue()
                );
                break;
            case NONE:
                // Ingen handling
                logger.info("NONE moderation action for user {}: {}", event.getAuthor().getName(), result.getReason());
                break;
            case SYSTEM_ACTION:
                // System handling - automatisk handling
                logger.info("SYSTEM_ACTION moderation action for user {}: {}", event.getAuthor().getName(), result.getReason());
                break;
            default:
                break;
        }
    }
    
    /**
     * Tilføjer custom filter
     */
    public void addCustomFilter(String pattern) {
        try {
            bannedWords.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
            logger.info("Tilføjet custom filter: {}", pattern);
        } catch (Exception e) {
            logger.error("Ugyldig regex pattern: {}", pattern, e);
        }
    }
    
    /**
     * Fjerner advarsler for en bruger
     */
    public void clearWarnings(String userId, String guildId) {
        databaseService.clearWarnings(userId, guildId);
        logger.info("Fjernede advarsler for bruger: {}", userId);
    }
    
    /**
     * Får antal advarsler for en bruger
     */
    public int getWarnings(String userId, String guildId) {
        return databaseService.getWarningCount(userId, guildId);
    }
    
    /**
     * Tilføjer en advarsel til en bruger
     */
    public void addWarning(String userId, String guildId, String reason, String moderatorId) {
        databaseService.addWarning(userId, guildId, reason, moderatorId);
        int newCount = databaseService.getWarningCount(userId, guildId);
        logger.info("Tilføjet advarsel til bruger {}: {} (Total: {})", userId, reason, newCount);
    }
    
    /**
     * Får antal advarsler for en bruger (alias for getWarnings)
     */
    public int getWarningCount(String userId, String guildId) {
        return getWarnings(userId, guildId);
    }
}

// Stub classes for compilation
// Duplicate MessageReceivedEvent class removed - using earlier definition

// Additional stub classes for JDA slash commands

enum OptionType {
    STRING, INTEGER, BOOLEAN, USER, CHANNEL, ROLE, MENTIONABLE, NUMBER, ATTACHMENT
}

class CommandData {
    public CommandData addSubcommands(SubcommandData... subcommands) { return this; }
}

class Commands {
    public static CommandData slash(String name, String description) { return new CommandData(); }
}

class SubcommandData {
    public SubcommandData(String name, String description) {}
    public SubcommandData addOption(OptionType type, String name, String description) { return this; }
    public SubcommandData addOption(OptionType type, String name, String description, boolean required) { return this; }
}

// Removed stub User class - using real JDA User class instead

// Removed stub classes for Member, Guild, Message, MessageChannel, TextChannel, PrivateChannel, 
// Mentions, GuildChannel, MessageEmbed, Permission, Logger, LoggerFactory - using real JDA classes instead

// Removed stub RestAction and AuditableRestAction classes - using real JDA classes instead

// Duplicate classes removed - using earlier definitions

// Removed duplicate class definitions - using separate class files instead

// ModerationStats and ModerationConfig classes removed - using separate class files

// ModerationLog and UserModerationProfile classes removed - using separate class files

// DatabaseService class removed - using separate class file

// AdvancedModerationCommands and AdvancedModerationSystem classes removed - using separate class files

// Removed stub EmbedBuilder and Color classes - using real JDA classes instead

// Removed stub ModerationDashboard class - using separate class file instead

// Removed stub SlashCommandInteractionEvent, OptionMapping, ReplyCallbackAction classes - using real JDA classes instead

// Removed stub MessageHistory class - using real JDA MessageHistory class instead

// AntiRaidSystem class removed - using separate class file

class RaidStatus {
    private boolean active;
    private java.time.Instant startTime;
    private String type;
    private boolean enhancedVerification;
    
    public RaidStatus() {
        this.active = false;
        this.startTime = null;
        this.type = null;
        this.enhancedVerification = false;
    }
    
    public boolean isActive() { return active; }
    public java.time.Instant getStartTime() { return startTime; }
    public void setActive(boolean active) { this.active = active; }
    public void setStartTime(java.time.Instant startTime) { this.startTime = startTime; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isEnhancedVerification() { return enhancedVerification; }
    public void setEnhancedVerification(boolean enhancedVerification) { this.enhancedVerification = enhancedVerification; }
}

// Removed stub MessageReceivedEvent class - using real JDA MessageReceivedEvent class instead