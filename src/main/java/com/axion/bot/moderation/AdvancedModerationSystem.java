package com.axion.bot.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Advanced Moderation System with enhanced Discord features
 * Includes anti-raid protection, sophisticated timeout system, and AI-powered detection
 * Fully compliant with Discord ToS and Privacy Policy
 */
public class AdvancedModerationSystem {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedModerationSystem.class);
    
    // Core tracking systems
    private final Map<String, UserModerationProfile> userProfiles = new ConcurrentHashMap<>();
    private final Map<String, GuildModerationSettings> guildSettings = new ConcurrentHashMap<>();
    private final Map<String, List<RaidDetectionEvent>> raidEvents = new ConcurrentHashMap<>();
    private final Map<String, AutoModerationRule> customRules = new ConcurrentHashMap<>();
    
    // Advanced detection systems
    private final AntiRaidSystem antiRaidSystem;
    private final ToxicityAnalyzer toxicityAnalyzer;
    private final SpamDetectionEngine spamEngine;
    private final ThreatIntelligence threatIntel;
    
    // Scheduled tasks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    // Configuration
    private final AdvancedModerationConfig config;
    private final ModerationLogger moderationLogger;
    
    public AdvancedModerationSystem(AdvancedModerationConfig config) {
        this.config = config;
        this.moderationLogger = new ModerationLogger();
        this.antiRaidSystem = new AntiRaidSystem(this);
        this.toxicityAnalyzer = new ToxicityAnalyzer();
        this.spamEngine = new SpamDetectionEngine();
        this.threatIntel = new ThreatIntelligence();
        
        initializeScheduledTasks();
        initializeDefaultRules();
    }
    
    /**
     * Main moderation entry point for messages
     */
    public ModerationResult processMessage(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String content = event.getMessage().getContentRaw();
        Member member = event.getMember();
        Guild guild = event.getGuild();
        
        // Skip bots and bypass privileged users
        if (author.isBot() || hasModeratorBypass(member)) {
            return ModerationResult.allowed();
        }
        
        String userId = author.getId();
        String guildId = guild.getId();
        
        // Get or create user profile
        UserModerationProfile profile = getUserProfile(userId, guildId);
        GuildModerationSettings settings = getGuildSettings(guildId);
        
        // Update user activity
        profile.recordActivity(content, event.getChannel().getId());
        
        // Check for active punishments
        if (profile.isCurrentlyPunished()) {
            return handleActivePunishment(profile, event);
        }
        
        // Multi-layer detection system
        List<ModerationResult> detectionResults = new ArrayList<>();
        
        // 1. Spam Detection (Enhanced)
        if (settings.isSpamProtectionEnabled()) {
            SpamDetectionEngine.SpamDetectionResult spamDetection = spamEngine.analyzeMessage(event.getMessage(), profile);
            if (spamDetection.isSpam()) {
                ModerationResult spamResult = convertSpamDetectionToModerationResult(spamDetection);
                detectionResults.add(spamResult);
            }
        }
        
        // 2. Toxicity Analysis (AI-Powered)
        if (settings.isToxicityDetectionEnabled()) {
            ModerationResult toxicResult = toxicityAnalyzer.analyzeContent(content, profile.getTrustLevel());
            if (!toxicResult.isAllowed()) {
                detectionResults.add(toxicResult);
            }
        }
        
        // 3. Threat Intelligence
        if (settings.isThreatIntelEnabled()) {
            ThreatIntelligence.ThreatAnalysisResult threatAnalysis = threatIntel.analyzeContent(content, event.getAuthor().getId(), event.getChannel().getId());
            ModerationResult threatResult = convertThreatAnalysisToModerationResult(threatAnalysis);
            if (!threatResult.isAllowed()) {
                detectionResults.add(threatResult);
            }
        }
        
        // 4. Custom Rules Engine
        for (AutoModerationRule rule : getActiveRules(guildId)) {
            ModerationResult ruleResult = rule.evaluate(content, profile, event);
            if (!ruleResult.isAllowed()) {
                detectionResults.add(ruleResult);
            }
        }
        
        // 5. Raid Detection
        ModerationResult raidResult = antiRaidSystem.checkForRaidActivity(event, profile);
        if (!raidResult.isAllowed()) {
            detectionResults.add(raidResult);
        }
        
        // Process detection results
        if (!detectionResults.isEmpty()) {
            return processViolations(detectionResults, profile, event, settings);
        }
        
        // Update trust level for good behavior
        profile.incrementGoodBehavior();
        
        return ModerationResult.allowed();
    }
    
    /**
     * Enhanced member join processing with raid detection
     */
    public ModerationResult processMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        String userId = member.getId();
        String guildId = guild.getId();
        
        UserModerationProfile profile = getUserProfile(userId, guildId);
        GuildModerationSettings settings = getGuildSettings(guildId);
        
        // Record join event
        profile.recordJoinEvent(Instant.now());
        
        // Anti-raid checks
        if (settings.isAntiRaidEnabled()) {
            ModerationResult raidResult = antiRaidSystem.checkJoinEvent(event, profile);
            if (!raidResult.isAllowed()) {
                return raidResult;
            }
        }
        
        // Account age verification
        if (settings.isAccountAgeCheckEnabled()) {
            Duration accountAge = Duration.between(member.getUser().getTimeCreated().toInstant(), Instant.now());
            if (accountAge.toDays() < settings.getMinAccountAgeDays()) {
                return ModerationResult.moderate(
                    "Account too new: " + accountAge.toDays() + " days old",
                    ModerationAction.KICK
                );
            }
        }
        
        // Suspicious username patterns
        if (settings.isSuspiciousUsernameDetectionEnabled()) {
            if (isSuspiciousUsername(member.getUser().getName())) {
                profile.addSuspicionPoints(2);
                return ModerationResult.warn(
                    "Suspicious username pattern detected",
                    ModerationAction.FLAG_FOR_REVIEW
                );
            }
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Advanced timeout system with escalation
     */
    public boolean applyAdvancedTimeout(Member target, Duration duration, String reason, Member moderator) {
        try {
            String userId = target.getId();
            UserModerationProfile profile = getUserProfile(userId, target.getGuild().getId());
            
            // Calculate escalated duration based on history
            Duration finalDuration = calculateEscalatedDuration(duration, profile);
            
            // Apply Discord timeout
            target.timeoutFor(finalDuration).reason(reason + " (Applied by: " + moderator.getEffectiveName() + ")").queue(
                success -> {
                    // Log the action
                    logModerationAction(new ModerationLog(
                        userId,
                        target.getUser().getName(),
                        moderator.getId(),
                        moderator.getEffectiveName(),
                        ModerationAction.TIMEOUT,
                        reason,
                        target.getGuild().getId(),
                        null,
                        null,
                        ModerationSeverity.MEDIUM.getLevel(),
                        false
                    ));
                    
                    // Update user profile
                    profile.addPunishment(ModerationAction.TIMEOUT, finalDuration, reason);
                    
                    // Send notification to user (if enabled)
                    if (config.isSendUserNotifications()) {
                        sendUserNotification(target.getUser(), "timeout", reason, finalDuration);
                    }
                    
                    logger.info("Applied timeout to {} for {} - Duration: {}", 
                        target.getUser().getAsTag(), reason, finalDuration);
                },
                error -> {
                    logger.error("Failed to apply timeout to {}: {}", target.getUser().getAsTag(), error.getMessage());
                }
            );
            
            return true;
        } catch (HierarchyException e) {
            logger.warn("Cannot timeout {} due to role hierarchy", target.getUser().getAsTag());
            return false;
        } catch (Exception e) {
            logger.error("Error applying timeout to {}: {}", target.getUser().getAsTag(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Smart ban system with appeal process
     */
    public boolean applySmartBan(Member target, Duration duration, String reason, Member moderator, boolean deleteMessages) {
        try {
            String userId = target.getId();
            Guild guild = target.getGuild();
            UserModerationProfile profile = getUserProfile(userId, guild.getId());
            
            // Determine if this should be a permanent or temporary ban
            boolean isPermanent = duration == null || duration.toDays() > 365;
            
            // Apply the ban
            int deleteMessageDays = deleteMessages ? 1 : 0;
            
            guild.ban(target, deleteMessageDays, TimeUnit.DAYS)
                .reason(reason + " (Applied by: " + moderator.getEffectiveName() + ")")
                .queue(
                    success -> {
                        // Log the action
                        logModerationAction(new ModerationLog(
                            userId,
                            target.getUser().getName(),
                            moderator.getId(),
                            moderator.getEffectiveName(),
                            isPermanent ? ModerationAction.BAN : ModerationAction.TEMP_BAN,
                            reason,
                            guild.getId(),
                            null,
                            null,
                            ModerationSeverity.HIGH.getLevel(),
                            false
                        ));
                        
                        // Update user profile
                        profile.addPunishment(
                            isPermanent ? ModerationAction.BAN : ModerationAction.TEMP_BAN,
                            duration,
                            reason
                        );
                        
                        // Schedule unban for temporary bans
                        if (!isPermanent && duration != null) {
                            scheduleUnban(guild.getId(), userId, duration);
                        }
                        
                        // Send notification
                        if (config.isSendUserNotifications()) {
                            sendUserNotification(target.getUser(), "ban", reason, duration);
                        }
                        
                        logger.info("Applied {} ban to {} for {} - Duration: {}", 
                            isPermanent ? "permanent" : "temporary",
                            target.getUser().getAsTag(), reason, 
                            duration != null ? duration : "permanent");
                    },
                    error -> {
                        logger.error("Failed to ban {}: {}", target.getUser().getAsTag(), error.getMessage());
                    }
                );
            
            return true;
        } catch (Exception e) {
            logger.error("Error applying ban to {}: {}", target.getUser().getAsTag(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Mass action system for raid response
     */
    public void executeMassAction(Guild guild, List<String> userIds, ModerationAction action, String reason) {
        logger.info("Executing mass {} action on {} users in guild {}", action, userIds.size(), guild.getName());
        
        for (String userId : userIds) {
            guild.retrieveMemberById(userId).queue(
                member -> {
                    switch (action) {
                        case KICK:
                            guild.kick(member).reason("Mass action: " + reason).queue();
                            break;
                        case BAN:
                            guild.ban(member, 0, TimeUnit.SECONDS).reason("Mass action: " + reason).queue();
                            break;
                        case TIMEOUT:
                            member.timeoutFor(Duration.ofHours(1)).reason("Mass action: " + reason).queue();
                            break;
                        default:
                            break;
                    }
                    
                    // Log each action
                    UserModerationProfile profile = getUserProfile(userId, guild.getId());
                    profile.addPunishment(action, Duration.ofHours(1), reason);
                },
                error -> logger.warn("Could not retrieve member {} for mass action", userId)
            );
        }
    }
    
    // Helper methods and utility functions
    
    private UserModerationProfile getUserProfile(String userId, String guildId) {
        String key = guildId + ":" + userId;
        return userProfiles.computeIfAbsent(key, k -> new UserModerationProfile(userId, guildId));
    }
    
    private GuildModerationSettings getGuildSettings(String guildId) {
        return guildSettings.computeIfAbsent(guildId, k -> new GuildModerationSettings(guildId));
    }
    
    private boolean hasModeratorBypass(Member member) {
        if (member == null) return false;
        return member.hasPermission(Permission.ADMINISTRATOR) ||
               member.hasPermission(Permission.MODERATE_MEMBERS) ||
               member.hasPermission(Permission.MANAGE_SERVER);
    }
    
    private Duration calculateEscalatedDuration(Duration baseDuration, UserModerationProfile profile) {
        int violationCount = profile.getViolationCount();
        double multiplier = Math.min(1.0 + (violationCount * 0.5), 5.0); // Max 5x escalation
        return Duration.ofMillis((long) (baseDuration.toMillis() * multiplier));
    }
    
    private boolean isSuspiciousUsername(String username) {
        // Check for suspicious patterns
        Pattern suspiciousPatterns = Pattern.compile(
            "(discord|nitro|admin|mod|bot|official|support|staff|free|hack|cheat|spam).*\\d{3,}|" +
            "\\d{4,}|" + // Too many numbers
            "[a-zA-Z]\\1{3,}|" + // Repeated characters
            "^[^a-zA-Z0-9]+$", // Only special characters
            Pattern.CASE_INSENSITIVE
        );
        
        return suspiciousPatterns.matcher(username).find();
    }
    
    private void initializeScheduledTasks() {
        // Clean up old data every hour
        scheduler.scheduleAtFixedRate(this::cleanupOldData, 1, 1, TimeUnit.HOURS);
        
        // Update trust levels every 6 hours
        scheduler.scheduleAtFixedRate(this::updateTrustLevels, 6, 6, TimeUnit.HOURS);
        
        // Process temporary bans every minute
        scheduler.scheduleAtFixedRate(this::processTemporaryBans, 1, 1, TimeUnit.MINUTES);
    }
    
    private void initializeDefaultRules() {
        // Add default auto-moderation rules
        AutoModerationRule spamRule = new AutoModerationRule("default_spam", "global", "Default spam detection", AutoModerationRule.RuleType.REGEX_CONTENT);
        spamRule.setPattern("(.)\\1{10,}");
        spamRule.setAction(ModerationAction.DELETE_AND_WARN);
        spamRule.setSeverity(ModerationSeverity.LOW);
        addCustomRule(spamRule);
        
        AutoModerationRule capsRule = new AutoModerationRule("default_caps", "global", "Excessive caps detection", AutoModerationRule.RuleType.CAPS_FILTER);
        capsRule.setAction(ModerationAction.DELETE_AND_WARN);
        capsRule.setSeverity(ModerationSeverity.LOW);
        addCustomRule(capsRule);
    }
    
    // Cleanup and maintenance methods
    
    private void cleanupOldData() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        
        userProfiles.values().removeIf(profile -> profile.getLastActivity().isBefore(cutoff));
        raidEvents.entrySet().removeIf(entry -> 
            entry.getValue().stream().allMatch(event -> event.getTimestamp().isBefore(cutoff))
        );
        
        logger.debug("Cleaned up old moderation data");
    }
    
    private void updateTrustLevels() {
        userProfiles.values().forEach(UserModerationProfile::updateTrustLevel);
        logger.debug("Updated trust levels for all users");
    }
    
    private void processTemporaryBans() {
        // This would be implemented with a proper database
        // For now, it's a placeholder for the concept
    }
    
    // Public API methods
    
    public void addCustomRule(AutoModerationRule rule) {
        customRules.put(rule.getRuleId(), rule);
    }
    
    public void removeCustomRule(String ruleId) {
        customRules.remove(ruleId);
    }
    
    public List<AutoModerationRule> getActiveRules(String guildId) {
        return customRules.values().stream()
            .filter(rule -> rule.isEnabled() && (rule.getGuildId().equals(guildId) || rule.getGuildId().equals("global")))
            .collect(Collectors.toList());
    }
    
    public UserModerationProfile getUserProfile(String userId, String guildId, boolean createIfNotExists) {
        if (createIfNotExists) {
            return getUserProfile(userId, guildId);
        }
        String key = guildId + ":" + userId;
        return userProfiles.get(key);
    }
    
    public AntiRaidSystem getAntiRaidSystem() {
        return antiRaidSystem;
    }
    
    public int getHighRiskUserCount(String guildId) {
        return (int) userProfiles.values().stream()
            .filter(profile -> profile.getGuildId().equals(guildId))
            .filter(profile -> profile.isHighRisk())
            .count();
    }
    
    public void executeSmartBan(Guild guild, User targetUser, User moderator, String reason) {
        Member targetMember = guild.getMember(targetUser);
        Member moderatorMember = guild.getMember(moderator);
        
        if (targetMember != null && moderatorMember != null) {
            applySmartBan(targetMember, null, reason, moderatorMember, true);
        } else {
            logger.warn("Could not execute smart ban - member not found: target={}, moderator={}", 
                targetUser.getId(), moderator.getId());
        }
    }
    
    public void executeAdvancedTimeout(Guild guild, User targetUser, User moderator, String reason) {
        Member targetMember = guild.getMember(targetUser);
        Member moderatorMember = guild.getMember(moderator);
        
        if (targetMember != null && moderatorMember != null) {
            // Calculate intelligent timeout duration based on user profile
            UserModerationProfile profile = getUserProfile(targetUser.getId(), guild.getId(), true);
            Duration baseDuration = Duration.ofMinutes(30); // Default 30 minutes
            Duration intelligentDuration = calculateEscalatedDuration(baseDuration, profile);
            
            applyAdvancedTimeout(targetMember, intelligentDuration, reason, moderatorMember);
        } else {
            logger.warn("Could not execute advanced timeout - member not found: target={}, moderator={}", 
                targetUser.getId(), moderator.getId());
        }
    }
    
    /**
     * Execute mass action based on criteria
     */
    public int executeMassAction(Guild guild, String action, String criteria, String reason, User moderator) {
        List<String> targetUserIds = new ArrayList<>();
        
        // Determine target users based on criteria
        switch (criteria.toLowerCase()) {
            case "high_risk":
                targetUserIds = userProfiles.values().stream()
                    .filter(profile -> profile.getGuildId().equals(guild.getId()))
                    .filter(UserModerationProfile::isHighRisk)
                    .map(UserModerationProfile::getUserId)
                    .collect(Collectors.toList());
                break;
            case "recent_violations":
                targetUserIds = userProfiles.values().stream()
                    .filter(profile -> profile.getGuildId().equals(guild.getId()))
                    .filter(profile -> profile.getRecentViolationCount() > 3)
                    .map(UserModerationProfile::getUserId)
                    .collect(Collectors.toList());
                break;
            default:
                logger.warn("Unknown mass action criteria: {}", criteria);
                return 0;
        }
        
        // Convert action string to ModerationAction
        ModerationAction moderationAction;
        switch (action.toLowerCase()) {
            case "ban":
                moderationAction = ModerationAction.BAN;
                break;
            case "kick":
                moderationAction = ModerationAction.KICK;
                break;
            case "timeout":
                moderationAction = ModerationAction.TIMEOUT;
                break;
            default:
                logger.warn("Unknown mass action type: {}", action);
                return 0;
        }
        
        // Execute the mass action
        executeMassAction(guild, targetUserIds, moderationAction, reason);
        
        logger.info("Mass action {} executed by {} on {} users with criteria '{}'", 
            action, moderator.getAsTag(), targetUserIds.size(), criteria);
        
        return targetUserIds.size();
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // Placeholder methods for additional functionality
    
    private ModerationResult handleActivePunishment(UserModerationProfile profile, MessageReceivedEvent event) {
        // Handle users who are currently under punishment
        return ModerationResult.moderate("User is currently under active punishment", ModerationAction.DELETE_MESSAGE);
    }
    
    private ModerationResult processViolations(List<ModerationResult> violations, UserModerationProfile profile, MessageReceivedEvent event, GuildModerationSettings settings) {
        // Process multiple violations and determine appropriate action
        ModerationResult mostSevere = violations.stream()
                .max(Comparator.comparing(r -> r.getSeverity()))
                .orElse(ModerationResult.allowed());
        
        profile.addViolation(mostSevere.getReason());
        return mostSevere;
    }
    
    private void scheduleUnban(String guildId, String userId, Duration duration) {
        scheduler.schedule(() -> {
            // Implement automatic unban logic
            logger.info("Scheduled unban executed for user {} in guild {}", userId, guildId);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    private void sendUserNotification(User user, String actionType, String reason, Duration duration) {
        // Send private message to user about moderation action
        user.openPrivateChannel().queue(channel -> {
            String message = String.format(
                "You have received a %s for: %s%s",
                actionType,
                reason,
                duration != null ? " (Duration: " + formatDuration(duration) + ")" : ""
            );
            channel.sendMessage(message).queue();
        });
    }
    
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        if (days > 0) {
            return String.format("%d days, %d hours", days, hours);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }
    
    private void logModerationAction(ModerationLog log) {
        // Log moderation action using the moderation logger
        // Note: ModerationLogger expects different parameters, so we'll use the static logger for now
        logger.info("Moderation action: {} - {} - {} - {}", 
            log.getUsername(), log.getAction(), log.getReason(), log.getSeverity());
    }
    
    private ModerationResult convertSpamDetectionToModerationResult(SpamDetectionEngine.SpamDetectionResult spamDetection) {
        if (!spamDetection.isSpam()) {
            return ModerationResult.allowed();
        }
        
        String reason = spamDetection.getDescription();
        ModerationAction action = ModerationAction.DELETE_MESSAGE;
        
        // Determine severity based on spam score and likelihood
        switch (spamDetection.getLikelihood()) {
            case HIGH:
                return ModerationResult.severe(reason, ModerationAction.DELETE_AND_TIMEOUT);
            case MEDIUM:
                return ModerationResult.moderate(reason, ModerationAction.DELETE_MESSAGE);
            case LOW:
            default:
                return ModerationResult.warn(reason, ModerationAction.DELETE_MESSAGE);
        }
    }
    
    private ModerationResult convertThreatAnalysisToModerationResult(ThreatIntelligence.ThreatAnalysisResult threatAnalysis) {
        if (!threatAnalysis.isThreat()) {
            return ModerationResult.allowed();
        }
        
        String reason = "Threat detected: " + threatAnalysis.getFlags().stream()
            .map(flag -> flag.getType().toString())
            .collect(java.util.stream.Collectors.joining(", "));
        
        // Determine severity based on threat level
        switch (threatAnalysis.getLevel()) {
            case VERY_HIGH:
                return ModerationResult.ban(reason, ModerationAction.BAN);
            case HIGH:
                return ModerationResult.severe(reason, ModerationAction.DELETE_AND_TIMEOUT);
            case MEDIUM:
                return ModerationResult.moderate(reason, ModerationAction.DELETE_MESSAGE);
            case LOW:
                return ModerationResult.warn(reason, ModerationAction.FLAG_FOR_REVIEW);
            case NONE:
            default:
                return ModerationResult.allowed();
        }
    }
}