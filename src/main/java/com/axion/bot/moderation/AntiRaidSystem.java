package com.axion.bot.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Advanced Anti-Raid Protection System
 * Detects and mitigates coordinated attacks on Discord servers
 * Compliant with Discord ToS - uses only legitimate Discord features
 */
public class AntiRaidSystem {
    private static final Logger logger = LoggerFactory.getLogger(AntiRaidSystem.class);
    
    private final AdvancedModerationSystem moderationSystem;
    
    // Raid detection tracking
    private final Map<String, List<JoinEvent>> guildJoinEvents = new ConcurrentHashMap<>();
    private final Map<String, List<MessageEvent>> guildMessageEvents = new ConcurrentHashMap<>();
    private final Map<String, RaidStatus> guildRaidStatus = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> suspiciousUserGroups = new ConcurrentHashMap<>();
    
    // Configuration thresholds
    private static final int RAID_JOIN_THRESHOLD = 10; // users joining within timeframe
    private static final Duration RAID_TIME_WINDOW = Duration.ofMinutes(5);
    private static final int COORDINATED_MESSAGE_THRESHOLD = 5; // similar messages from different users
    private static final Duration MESSAGE_TIME_WINDOW = Duration.ofMinutes(2);
    
    public AntiRaidSystem(AdvancedModerationSystem moderationSystem) {
        this.moderationSystem = moderationSystem;
    }
    
    /**
     * Check for raid activity when a member joins
     */
    public ModerationResult checkJoinEvent(GuildMemberJoinEvent event, UserModerationProfile profile) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        String guildId = guild.getId();
        String userId = member.getId();
        
        Instant now = Instant.now();
        
        // Record join event
        List<JoinEvent> joinEvents = guildJoinEvents.computeIfAbsent(guildId, k -> new ArrayList<>());
        joinEvents.add(new JoinEvent(userId, now, member.getUser().getName(), getAccountAge(member)));
        
        // Clean old events
        cleanOldJoinEvents(joinEvents, now);
        
        // Check for raid patterns
        RaidDetectionResult raidResult = analyzeJoinPattern(joinEvents, guildId);
        
        if (raidResult.isRaidDetected()) {
            return handleRaidDetection(guild, raidResult, member);
        }
        
        // Check for suspicious account characteristics
        if (isSuspiciousAccount(member, profile)) {
            profile.addSuspicionPoints(3);
            
            // If already in raid mode, be more aggressive
            RaidStatus status = guildRaidStatus.get(guildId);
            if (status != null && status.isActive()) {
                return ModerationResult.moderate(
                    "Suspicious account joining during raid alert",
                    ModerationAction.KICK
                );
            }
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Check for coordinated message spam (raid messages)
     */
    public ModerationResult checkForRaidActivity(MessageReceivedEvent event, UserModerationProfile profile) {
        Guild guild = event.getGuild();
        String guildId = guild.getId();
        String userId = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw();
        
        Instant now = Instant.now();
        
        // Record message event
        List<MessageEvent> messageEvents = guildMessageEvents.computeIfAbsent(guildId, k -> new ArrayList<>());
        messageEvents.add(new MessageEvent(userId, content, now, event.getChannel().getId()));
        
        // Clean old events
        cleanOldMessageEvents(messageEvents, now);
        
        // Analyze for coordinated spam
        CoordinatedSpamResult spamResult = analyzeCoordinatedSpam(messageEvents, guildId);
        
        if (spamResult.isCoordinatedSpam()) {
            return handleCoordinatedSpam(guild, spamResult, event);
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Analyze join patterns for raid detection
     */
    private RaidDetectionResult analyzeJoinPattern(List<JoinEvent> joinEvents, String guildId) {
        Instant now = Instant.now();
        
        // Count recent joins
        long recentJoins = joinEvents.stream()
            .filter(event -> Duration.between(event.getTimestamp(), now).compareTo(RAID_TIME_WINDOW) <= 0)
            .count();
        
        if (recentJoins >= RAID_JOIN_THRESHOLD) {
            // Analyze join characteristics
            List<JoinEvent> recentEvents = joinEvents.stream()
                .filter(event -> Duration.between(event.getTimestamp(), now).compareTo(RAID_TIME_WINDOW) <= 0)
                .collect(Collectors.toList());
            
            RaidCharacteristics characteristics = analyzeRaidCharacteristics(recentEvents);
            
            return new RaidDetectionResult(
                true,
                RaidType.JOIN_SPAM,
                characteristics,
                recentEvents.stream().map(JoinEvent::getUserId).collect(Collectors.toList()),
                "Detected " + recentJoins + " joins in " + RAID_TIME_WINDOW.toMinutes() + " minutes"
            );
        }
        
        return new RaidDetectionResult(false, null, null, Collections.emptyList(), null);
    }
    
    /**
     * Analyze message patterns for coordinated spam
     */
    private CoordinatedSpamResult analyzeCoordinatedSpam(List<MessageEvent> messageEvents, String guildId) {
        Instant now = Instant.now();
        
        // Group messages by similarity
        Map<String, List<MessageEvent>> similarMessages = new HashMap<>();
        
        for (MessageEvent event : messageEvents) {
            if (Duration.between(event.getTimestamp(), now).compareTo(MESSAGE_TIME_WINDOW) <= 0) {
                String normalizedContent = normalizeContent(event.getContent());
                similarMessages.computeIfAbsent(normalizedContent, k -> new ArrayList<>()).add(event);
            }
        }
        
        // Check for coordinated spam
        for (Map.Entry<String, List<MessageEvent>> entry : similarMessages.entrySet()) {
            List<MessageEvent> events = entry.getValue();
            
            if (events.size() >= COORDINATED_MESSAGE_THRESHOLD) {
                // Check if messages are from different users
                Set<String> uniqueUsers = events.stream()
                    .map(MessageEvent::getUserId)
                    .collect(Collectors.toSet());
                
                if (uniqueUsers.size() >= 3) { // At least 3 different users
                    return new CoordinatedSpamResult(
                        true,
                        entry.getKey(),
                        new ArrayList<>(uniqueUsers),
                        events.size(),
                        "Coordinated spam detected: " + events.size() + " similar messages from " + uniqueUsers.size() + " users"
                    );
                }
            }
        }
        
        return new CoordinatedSpamResult(false, null, Collections.emptyList(), 0, null);
    }
    
    /**
     * Handle detected raid activity
     */
    private ModerationResult handleRaidDetection(Guild guild, RaidDetectionResult result, Member triggeringMember) {
        String guildId = guild.getId();
        
        logger.warn("RAID DETECTED in guild {}: {}", guild.getName(), result.getReason());
        
        // Activate raid mode
        RaidStatus status = new RaidStatus(true, Instant.now(), result.getRaidType());
        guildRaidStatus.put(guildId, status);
        
        // Determine response based on raid characteristics
        RaidResponse response = determineRaidResponse(result.getCharacteristics());
        
        switch (response) {
            case LOCKDOWN:
                // Implement server lockdown (kick recent suspicious joins)
                moderationSystem.executeMassAction(guild, result.getSuspiciousUsers(), ModerationAction.KICK, "Raid protection - mass kick");
                break;
                
            case ENHANCED_VERIFICATION:
                // Enable enhanced verification for new joins
                status.setEnhancedVerification(true);
                break;
                
            case MONITOR:
                // Just monitor and flag
                logger.info("Raid monitoring activated for guild {}", guild.getName());
                break;
        }
        
        // Handle the triggering member
        if (result.getCharacteristics().isSuspicious()) {
            return ModerationResult.moderate(
                "Account flagged during raid detection",
                ModerationAction.KICK
            );
        }
        
        return ModerationResult.allowed();
    }
    
    /**
     * Handle coordinated spam
     */
    private ModerationResult handleCoordinatedSpam(Guild guild, CoordinatedSpamResult result, MessageReceivedEvent event) {
        String guildId = guild.getId();
        
        logger.warn("COORDINATED SPAM DETECTED in guild {}: {}", guild.getName(), result.getReason());
        
        // Mark users as part of suspicious group
        suspiciousUserGroups.computeIfAbsent(guildId, k -> new HashSet<>()).addAll(result.getInvolvedUsers());
        
        // Apply mass timeout to involved users
        moderationSystem.executeMassAction(guild, result.getInvolvedUsers(), ModerationAction.TIMEOUT, "Coordinated spam protection");
        
        return ModerationResult.moderate(
            "Participating in coordinated spam attack",
        ModerationAction.TIMEOUT
        );
    }
    
    /**
     * Check if an account is suspicious
     */
    private boolean isSuspiciousAccount(Member member, UserModerationProfile profile) {
        // Account age check
        Duration accountAge = getAccountAge(member);
        if (accountAge.toDays() < 7) {
            profile.addSuspicionPoints(2);
        }
        
        // Default avatar check
        if (member.getUser().getAvatarUrl() == null) {
            profile.addSuspicionPoints(1);
        }
        
        // Username patterns
        String username = member.getUser().getName();
        if (username.matches(".*\\d{4,}.*") || // Many numbers
            username.matches("^[a-zA-Z]+\\d+$") || // Letters followed by numbers
            username.length() < 3) { // Very short username
            profile.addSuspicionPoints(1);
        }
        
        return profile.getSuspicionPoints() >= 3;
    }
    
    /**
     * Analyze characteristics of potential raid
     */
    private RaidCharacteristics analyzeRaidCharacteristics(List<JoinEvent> events) {
        int newAccounts = 0;
        int defaultAvatars = 0;
        int suspiciousNames = 0;
        
        for (JoinEvent event : events) {
            if (event.getAccountAge().toDays() < 7) {
                newAccounts++;
            }
            
            String username = event.getUsername();
            if (username.matches(".*\\d{4,}.*") || username.length() < 3) {
                suspiciousNames++;
            }
        }
        
        double newAccountRatio = (double) newAccounts / events.size();
        double suspiciousNameRatio = (double) suspiciousNames / events.size();
        
        boolean isSuspicious = newAccountRatio > 0.7 || suspiciousNameRatio > 0.5;
        
        return new RaidCharacteristics(
            newAccountRatio,
            suspiciousNameRatio,
            (double) defaultAvatars / events.size(),
            isSuspicious
        );
    }
    
    /**
     * Determine appropriate response to raid
     */
    private RaidResponse determineRaidResponse(RaidCharacteristics characteristics) {
        if (characteristics.isSuspicious() && characteristics.getNewAccountRatio() > 0.8) {
            return RaidResponse.LOCKDOWN;
        } else if (characteristics.getSuspiciousNameRatio() > 0.6) {
            return RaidResponse.ENHANCED_VERIFICATION;
        } else {
            return RaidResponse.MONITOR;
        }
    }
    
    /**
     * Get account age for a member
     */
    private Duration getAccountAge(Member member) {
        return Duration.between(member.getUser().getTimeCreated().toInstant(), Instant.now());
    }
    
    /**
     * Normalize message content for similarity comparison
     */
    private String normalizeContent(String content) {
        return content.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
    
    /**
     * Clean old join events
     */
    private void cleanOldJoinEvents(List<JoinEvent> events, Instant now) {
        events.removeIf(event -> Duration.between(event.getTimestamp(), now).toHours() > 24);
    }
    
    /**
     * Clean old message events
     */
    private void cleanOldMessageEvents(List<MessageEvent> events, Instant now) {
        events.removeIf(event -> Duration.between(event.getTimestamp(), now).toHours() > 1);
    }
    
    /**
     * Check if guild is currently under raid alert
     */
    public boolean isGuildUnderRaidAlert(String guildId) {
        RaidStatus status = guildRaidStatus.get(guildId);
        if (status == null || !status.isActive()) {
            return false;
        }
        
        // Auto-deactivate after 30 minutes
        if (Duration.between(status.getStartTime(), Instant.now()).toMinutes() > 30) {
            status.setActive(false);
            return false;
        }
        
        return true;
    }
    
    /**
     * Manually deactivate raid mode
     */
    public void deactivateRaidMode(String guildId) {
        RaidStatus status = guildRaidStatus.get(guildId);
        if (status != null) {
            status.setActive(false);
            logger.info("Raid mode manually deactivated for guild {}", guildId);
        }
    }
    
    /**
     * Get raid status for a guild
     */
    public RaidStatus getRaidStatus(String guildId) {
        return guildRaidStatus.get(guildId);
    }
    
    /**
     * Check if raid is detected for a specific guild
     */
    public boolean isRaidDetected(String guildId) {
        return isGuildUnderRaidAlert(guildId);
    }
    
    /**
     * Activate server lockdown mode
     */
    public void activateServerLockdown(String guildId, String reason) {
        RaidStatus status = guildRaidStatus.computeIfAbsent(guildId, 
            k -> new RaidStatus(false, Instant.now(), RaidType.COORDINATED_ATTACK));
        status.setActive(true);
        status.setEnhancedVerification(true);
        logger.info("Server lockdown activated for guild {} - Reason: {}", guildId, reason);
    }
    
    /**
     * Deactivate server lockdown mode
     */
    public void deactivateServerLockdown(String guildId) {
        RaidStatus status = guildRaidStatus.get(guildId);
        if (status != null) {
            status.setActive(false);
            status.setEnhancedVerification(false);
            logger.info("Server lockdown deactivated for guild {}", guildId);
        }
    }
    
    // Inner classes for data structures
    
    private static class JoinEvent {
        private final String userId;
        private final Instant timestamp;
        private final String username;
        private final Duration accountAge;
        
        public JoinEvent(String userId, Instant timestamp, String username, Duration accountAge) {
            this.userId = userId;
            this.timestamp = timestamp;
            this.username = username;
            this.accountAge = accountAge;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public Instant getTimestamp() { return timestamp; }
        public String getUsername() { return username; }
        public Duration getAccountAge() { return accountAge; }
    }
    
    private static class MessageEvent {
        private final String userId;
        private final String content;
        private final Instant timestamp;
        
        public MessageEvent(String userId, String content, Instant timestamp, String channelId) {
            this.userId = userId;
            this.content = content;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public String getContent() { return content; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    private static class RaidDetectionResult {
        private final boolean raidDetected;
        private final RaidType raidType;
        private final RaidCharacteristics characteristics;
        private final List<String> suspiciousUsers;
        private final String reason;
        
        public RaidDetectionResult(boolean raidDetected, RaidType raidType, RaidCharacteristics characteristics, List<String> suspiciousUsers, String reason) {
            this.raidDetected = raidDetected;
            this.raidType = raidType;
            this.characteristics = characteristics;
            this.suspiciousUsers = suspiciousUsers;
            this.reason = reason;
        }
        
        // Getters
        public boolean isRaidDetected() { return raidDetected; }
        public RaidType getRaidType() { return raidType; }
        public RaidCharacteristics getCharacteristics() { return characteristics; }
        public List<String> getSuspiciousUsers() { return suspiciousUsers; }
        public String getReason() { return reason; }
    }
    
    private static class CoordinatedSpamResult {
        private final boolean coordinatedSpam;
        private final List<String> involvedUsers;
        private final String reason;
        
        public CoordinatedSpamResult(boolean coordinatedSpam, String spamContent, List<String> involvedUsers, int messageCount, String reason) {
            this.coordinatedSpam = coordinatedSpam;
            this.involvedUsers = involvedUsers;
            this.reason = reason;
        }
        
        // Getters
        public boolean isCoordinatedSpam() { return coordinatedSpam; }
        public List<String> getInvolvedUsers() { return involvedUsers; }
        public String getReason() { return reason; }
    }
    
    private static class RaidCharacteristics {
        private final double newAccountRatio;
        private final double suspiciousNameRatio;
        private final boolean suspicious;
        
        public RaidCharacteristics(double newAccountRatio, double suspiciousNameRatio, double defaultAvatarRatio, boolean suspicious) {
            this.newAccountRatio = newAccountRatio;
            this.suspiciousNameRatio = suspiciousNameRatio;
            this.suspicious = suspicious;
        }
        
        // Getters
        public double getNewAccountRatio() { return newAccountRatio; }
        public double getSuspiciousNameRatio() { return suspiciousNameRatio; }
        public boolean isSuspicious() { return suspicious; }
    }
    
    public static class RaidStatus {
        private boolean active;
        private final Instant startTime;
        private final RaidType type;
        private boolean enhancedVerification;
        
        public RaidStatus(boolean active, Instant startTime, RaidType type) {
            this.active = active;
            this.startTime = startTime;
            this.type = type;
            this.enhancedVerification = false;
        }
        
        // Getters and setters
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Instant getStartTime() { return startTime; }
        public RaidType getType() { return type; }
        public boolean isEnhancedVerification() { return enhancedVerification; }
        public void setEnhancedVerification(boolean enhancedVerification) { this.enhancedVerification = enhancedVerification; }
    }
    
    public enum RaidType {
        JOIN_SPAM,
        MESSAGE_SPAM,
        COORDINATED_ATTACK
    }
    
    public enum RaidResponse {
        MONITOR,
        ENHANCED_VERIFICATION,
        LOCKDOWN
    }
}