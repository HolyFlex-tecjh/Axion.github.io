package com.axion.bot.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    
    // Spam detection
    private final Map<String, List<Instant>> userMessageTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> userWarnings = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userMessageHistory = new ConcurrentHashMap<>();
    private final Map<String, Instant> userLastViolation = new ConcurrentHashMap<>();
    
    // Advanced tracking
    private final Map<String, Integer> userViolationCount = new ConcurrentHashMap<>();
    private final Map<String, List<ModerationLog>> moderationLogs = new ConcurrentHashMap<>();
    private final Map<String, Instant> tempBans = new ConcurrentHashMap<>();
    
    // Configuration
    private final ModerationConfig config;
    
    // Filters
    private final List<Pattern> bannedWords = new ArrayList<>();
    private final List<Pattern> suspiciousPatterns = new ArrayList<>();
    private final List<Pattern> advancedSpamPatterns = new ArrayList<>();
    private final Set<String> suspiciousFileExtensions = new HashSet<>();
    
    public ModerationManager(ModerationConfig config) {
        this.config = config;
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
        if (author.isBot() || (member != null && member.hasPermission(Permission.ADMINISTRATOR))) {
            return ModerationResult.allowed();
        }
        
        String userId = author.getId();
        
        // Tjek for temp ban
        if (isTempBanned(userId)) {
            return ModerationResult.ban("Bruger er midlertidigt bannet", ModerationAction.BAN);
        }
        
        // Opdater besked historik
        updateMessageHistory(userId, content);
        
        // Tjek spam med forbedret detection
        if (config.isSpamProtectionEnabled()) {
            ModerationResult spamResult = checkAdvancedSpam(userId, content, event);
            if (!spamResult.isAllowed()) {
                logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    spamResult.getAction(), spamResult.getReason(), event.getGuild().getId(), 
                    event.getChannel().getId(), event.getMessage().getId(), spamResult.getSeverity(), true);
                return spamResult;
            }
        }
        
        // Tjek toxic content
        if (config.isToxicDetectionEnabled()) {
            ModerationResult toxicResult = checkToxicContent(content);
            if (!toxicResult.isAllowed()) {
                logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    toxicResult.getAction(), toxicResult.getReason(), event.getGuild().getId(), 
                    event.getChannel().getId(), event.getMessage().getId(), toxicResult.getSeverity(), true);
                return toxicResult;
            }
        }
        
        // Tjek attachments
        ModerationResult attachmentResult = checkAttachments(event);
        if (!attachmentResult.isAllowed()) {
            logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                attachmentResult.getAction(), attachmentResult.getReason(), event.getGuild().getId(), 
                event.getChannel().getId(), event.getMessage().getId(), attachmentResult.getSeverity(), true);
            return attachmentResult;
        }
        
        // Tjek custom filters
        ModerationResult customResult = checkCustomFilters(content);
        if (!customResult.isAllowed()) {
            logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                customResult.getAction(), customResult.getReason(), event.getGuild().getId(), 
                event.getChannel().getId(), event.getMessage().getId(), customResult.getSeverity(), true);
            return customResult;
        }
        
        // Tjek link protection
        if (config.isLinkProtectionEnabled()) {
            ModerationResult linkResult = checkLinkProtection(content);
            if (!linkResult.isAllowed()) {
                logModerationAction(userId, author.getName(), "SYSTEM", "Auto-Moderation", 
                    linkResult.getAction(), linkResult.getReason(), event.getGuild().getId(), 
                    event.getChannel().getId(), event.getMessage().getId(), linkResult.getSeverity(), true);
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
            incrementViolationCount(userId);
            return escalateBasedOnViolations(userId, "Spam detected: For mange beskeder per minut");
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
            incrementViolationCount(userId);
            return escalateBasedOnViolations(userId, "Spam detected: Identiske beskeder");
        }
        
        // Tjek for avancerede spam mønstre
        for (Pattern pattern : advancedSpamPatterns) {
            if (pattern.matcher(content).find()) {
                incrementViolationCount(userId);
                return escalateBasedOnViolations(userId, "Spam detected: Mistænkeligt mønster");
            }
        }
        
        // Tjek for hurtig typing (meget lange beskeder på kort tid)
        if (content.length() > 200 && messageTimes.size() >= 2) {
            Instant lastMessage = messageTimes.get(messageTimes.size() - 2);
            if (Duration.between(lastMessage, now).toSeconds() < 5) {
                return ModerationResult.warn("Mulig spam: Meget hurtig typing", ModerationAction.DELETE_MESSAGE);
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
                return ModerationResult.warn("Upassende sprog detekteret", ModerationAction.DELETE_MESSAGE);
            }
        }
        
        // Tjek for mistænkelige mønstre
        for (Pattern pattern : suspiciousPatterns) {
            if (pattern.matcher(content).find()) {
                return ModerationResult.warn("Mistænkeligt mønster detekteret", ModerationAction.DELETE_MESSAGE);
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
            return ModerationResult.warn("For mange links i besked", ModerationAction.DELETE_MESSAGE);
        }
        
        // Tjek for mistænkelige domæner
        Pattern suspiciousLinks = Pattern.compile("(bit\\.ly|tinyurl|t\\.co|discord\\.gg/[a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE);
        if (suspiciousLinks.matcher(content).find()) {
            return ModerationResult.warn("Mistænkeligt link detekteret", ModerationAction.DELETE_MESSAGE);
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
                    return ModerationResult.warn("Mistænkelig fil type: " + extension, ModerationAction.DELETE_MESSAGE);
                }
            }
            
            // Tjek fil størrelse (over 50MB)
            if (attachment.getSize() > 50 * 1024 * 1024) {
                return ModerationResult.warn("Fil for stor: " + attachment.getSize() + " bytes", ModerationAction.DELETE_MESSAGE);
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
    
    /**
     * Øger violation count for en bruger
     */
    private void incrementViolationCount(String userId) {
        int currentCount = userViolationCount.getOrDefault(userId, 0);
        userViolationCount.put(userId, currentCount + 1);
        userLastViolation.put(userId, Instant.now());
    }
    
    /**
     * Eskalerer baseret på antal violations
     */
    private ModerationResult escalateBasedOnViolations(String userId, String reason) {
        int violations = userViolationCount.getOrDefault(userId, 0);
        
        if (violations >= 5) {
            // Temp ban for 24 timer
            tempBans.put(userId, Instant.now().plus(24, ChronoUnit.HOURS));
            return ModerationResult.ban(reason + " (5+ violations - 24h temp ban)", ModerationAction.BAN);
        } else if (violations >= 3) {
            return ModerationResult.moderate(reason + " (3+ violations)", ModerationAction.DELETE_AND_TIMEOUT);
        } else {
            return ModerationResult.warn(reason, ModerationAction.DELETE_MESSAGE);
        }
    }
    
    /**
     * Tjekker om en bruger er temp banned
     */
    private boolean isTempBanned(String userId) {
        Instant banExpiry = tempBans.get(userId);
        if (banExpiry != null) {
            if (Instant.now().isAfter(banExpiry)) {
                tempBans.remove(userId);
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
      * Logger moderation actions
      */
     private void logModerationAction(String userId, String username, String moderatorId, String moderatorName,
                                    ModerationAction action, String reason, String guildId, String channelId,
                                    String messageId, int severity, boolean automated) {
         ModerationLog log = new ModerationLog(
             userId, username, moderatorId, moderatorName, action, reason,
             guildId, channelId, messageId, severity, automated, Instant.now()
         );
         
         List<ModerationLog> userLogs = moderationLogs.computeIfAbsent(userId, k -> new ArrayList<>());
         userLogs.add(log);
         
         // Hold kun de sidste 100 logs per bruger
         if (userLogs.size() > 100) {
             userLogs.remove(0);
         }
         
         logger.info("Moderation action logged: {} - {} - {} - {}", username, action, reason, severity);
     }
    
    /**
     * Får moderation logs for en bruger
     */
    public List<ModerationLog> getModerationLogs(String userId) {
        return moderationLogs.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Fjerner temp ban for en bruger
     */
    public void removeTempBan(String userId) {
        tempBans.remove(userId);
        logger.info("Temp ban fjernet for bruger: {}", userId);
    }
    
    /**
     * Får temp ban status for en bruger
     */
    public Instant getTempBanExpiry(String userId) {
        return tempBans.get(userId);
    }
    
    /**
     * Nulstiller violation count for en bruger
     */
    public void resetViolationCount(String userId) {
        userViolationCount.remove(userId);
        userLastViolation.remove(userId);
        logger.info("Violation count nulstillet for bruger: {}", userId);
    }
    
    /**
      * Får violation count for en bruger
      */
     public int getViolationCount(String userId) {
         return userViolationCount.getOrDefault(userId, 0);
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
         return (int) userViolationCount.values().stream()
                 .filter(count -> count > 0)
                 .count();
     }
     
     /**
      * Får antal aktive temp bans
      */
     public int getActiveTempBansCount() {
         Instant now = Instant.now();
         return (int) tempBans.values().stream()
                 .filter(expiry -> expiry.isAfter(now))
                 .count();
     }
     
     /**
      * Tilføjer en temp ban
      */
     public void addTempBan(String userId, int hours) {
         Instant expiry = Instant.now().plus(hours, ChronoUnit.HOURS);
         tempBans.put(userId, expiry);
         logger.info("Temp ban tilføjet for bruger {} i {} timer", userId, hours);
     }
     
     /**
      * Får alle aktive temp bans
      */
     public Map<String, Instant> getActiveTempBans() {
         Instant now = Instant.now();
         return tempBans.entrySet().stream()
                 .filter(entry -> entry.getValue().isAfter(now))
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
     }
     
     /**
      * Rydder op i udløbne temp bans
      */
     public void cleanupExpiredTempBans() {
         Instant now = Instant.now();
         tempBans.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
     }
     
     /**
      * Får moderation statistikker for en guild
      */
     public ModerationStats getModerationStats(String guildId) {
         return new ModerationStats(
             getTotalTrackedUsers(),
             getActiveViolationsCount(),
             getActiveTempBansCount(),
             moderationLogs.values().stream()
                     .mapToInt(List::size)
                     .sum()
         );
     }
     
     /**
      * Henter seneste moderation logs
      */
     public List<ModerationLog> getRecentModerationLogs(int limit) {
         return moderationLogs.values().stream()
             .flatMap(List::stream)
             .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
             .limit(limit)
             .collect(Collectors.toList());
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
    public void clearWarnings(String userId) {
        userWarnings.remove(userId);
        logger.info("Fjernede advarsler for bruger: {}", userId);
    }
    
    /**
     * Får antal advarsler for en bruger
     */
    public int getWarnings(String userId) {
        return userWarnings.getOrDefault(userId, 0);
    }
    
    /**
     * Tilføjer en advarsel til en bruger
     */
    public void addWarning(String userId, String reason) {
        int currentWarnings = userWarnings.getOrDefault(userId, 0);
        userWarnings.put(userId, currentWarnings + 1);
        logger.info("Tilføjet advarsel til bruger {}: {} (Total: {})", userId, reason, currentWarnings + 1);
    }
    
    /**
     * Får antal advarsler for en bruger (alias for getWarnings)
     */
    public int getWarningCount(String userId) {
        return getWarnings(userId);
    }
}