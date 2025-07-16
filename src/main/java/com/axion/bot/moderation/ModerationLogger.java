package com.axion.bot.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Advanced moderation logging system
 * Provides comprehensive logging for all moderation actions
 * Supports multiple log channels and detailed audit trails
 */
public class ModerationLogger {
    private final Map<String, String> guildLogChannels = new ConcurrentHashMap<>();
    private final Map<String, String> guildAuditChannels = new ConcurrentHashMap<>();
    private final Queue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, List<LogEntry>> recentLogs = new ConcurrentHashMap<>();
    
    // Log retention settings
    private static final int MAX_RECENT_LOGS = 1000;
    private static final long LOG_RETENTION_HOURS = 24;
    
    /**
     * Set the moderation log channel for a guild
     */
    public void setLogChannel(String guildId, String channelId) {
        guildLogChannels.put(guildId, channelId);
    }
    
    /**
     * Set the audit log channel for a guild
     */
    public void setAuditChannel(String guildId, String channelId) {
        guildAuditChannels.put(guildId, channelId);
    }
    
    /**
     * Log a moderation action
     */
    public void logModerationAction(Guild guild, User target, User moderator, 
                                  ModerationAction action, String reason, 
                                  ModerationSeverity severity, boolean automated) {
        LogEntry entry = new LogEntry(
            guild.getId(),
            target != null ? target.getId() : null,
            moderator != null ? moderator.getId() : null,
            action,
            reason,
            severity,
            automated,
            Instant.now()
        );
        
        // Add to queue and recent logs
        logQueue.offer(entry);
        addToRecentLogs(guild.getId(), entry);
        
        // Send to log channel
        sendToLogChannel(guild, entry, target, moderator);
        
        // Send to audit channel if it's a severe action
        if (severity == ModerationSeverity.HIGH || severity == ModerationSeverity.VERY_HIGH) {
            sendToAuditChannel(guild, entry, target, moderator);
        }
    }
    
    /**
     * Log an anti-raid action
     */
    public void logAntiRaidAction(Guild guild, String action, String details, int affectedUsers) {
        LogEntry entry = new LogEntry(
            guild.getId(),
            null, // No specific target
            null, // System action
            ModerationAction.SYSTEM_ACTION,
            String.format("Anti-Raid: %s - %s (Affected: %d users)", action, details, affectedUsers),
            ModerationSeverity.HIGH,
            true,
            Instant.now()
        );
        
        logQueue.offer(entry);
        addToRecentLogs(guild.getId(), entry);
        sendToAuditChannel(guild, entry, null, null);
    }
    
    /**
     * Log a spam detection event
     */
    public void logSpamDetection(Guild guild, User user, String spamType, 
                               double confidence, String details) {
        String reason = String.format("Spam Detection: %s (Confidence: %.2f%%) - %s", 
            spamType, confidence * 100, details);
        
        LogEntry entry = new LogEntry(
            guild.getId(),
            user.getId(),
            null, // Automated
            ModerationAction.DELETE_MESSAGE,
            reason,
            confidence > 0.8 ? ModerationSeverity.MEDIUM : ModerationSeverity.LOW,
            true,
            Instant.now()
        );
        
        logQueue.offer(entry);
        addToRecentLogs(guild.getId(), entry);
        sendToLogChannel(guild, entry, user, null);
    }
    
    /**
     * Log a toxicity detection event
     */
    public void logToxicityDetection(Guild guild, User user, String toxicityType, 
                                   double severity, String content) {
        String reason = String.format("Toxicity Detection: %s (Severity: %.2f) - Content flagged", 
            toxicityType, severity);
        
        ModerationSeverity modSeverity;
        if (severity >= 0.8) modSeverity = ModerationSeverity.VERY_HIGH;
        else if (severity >= 0.6) modSeverity = ModerationSeverity.HIGH;
        else if (severity >= 0.4) modSeverity = ModerationSeverity.MEDIUM;
        else modSeverity = ModerationSeverity.LOW;
        
        LogEntry entry = new LogEntry(
            guild.getId(),
            user.getId(),
            null, // Automated
            ModerationAction.DELETE_AND_WARN,
            reason,
            modSeverity,
            true,
            Instant.now()
        );
        
        logQueue.offer(entry);
        addToRecentLogs(guild.getId(), entry);
        sendToLogChannel(guild, entry, user, null);
        
        if (modSeverity == ModerationSeverity.HIGH || modSeverity == ModerationSeverity.VERY_HIGH) {
            sendToAuditChannel(guild, entry, user, null);
        }
    }
    
    /**
     * Log a threat detection event
     */
    public void logThreatDetection(Guild guild, User user, String threatType, 
                                 String threatLevel, String details) {
        String reason = String.format("Threat Detection: %s (%s) - %s", 
            threatType, threatLevel, details);
        
        ModerationSeverity severity;
        switch (threatLevel.toUpperCase()) {
            case "CRITICAL":
                severity = ModerationSeverity.VERY_HIGH;
                break;
            case "HIGH":
                severity = ModerationSeverity.HIGH;
                break;
            case "MEDIUM":
                severity = ModerationSeverity.MEDIUM;
                break;
            default:
                severity = ModerationSeverity.LOW;
                break;
        }
        
        LogEntry entry = new LogEntry(
            guild.getId(),
            user.getId(),
            null, // Automated
            ModerationAction.DELETE_AND_TIMEOUT,
            reason,
            severity,
            true,
            Instant.now()
        );
        
        logQueue.offer(entry);
        addToRecentLogs(guild.getId(), entry);
        sendToLogChannel(guild, entry, user, null);
        sendToAuditChannel(guild, entry, user, null);
    }
    
    /**
     * Send log entry to the moderation log channel
     */
    private void sendToLogChannel(Guild guild, LogEntry entry, User target, User moderator) {
        String channelId = guildLogChannels.get(guild.getId());
        if (channelId == null) return;
        
        net.dv8tion.jda.api.entities.channel.concrete.TextChannel logChannel = guild.getTextChannelById(channelId);
        if (logChannel == null) return;
        
        EmbedBuilder embed = createLogEmbed(entry, target, moderator, false);
        
        try {
            logChannel.sendMessageEmbeds(embed.build()).queue(
                success -> {},
                error -> System.err.println("Failed to send log message: " + error.getMessage())
            );
        } catch (Exception e) {
            System.err.println("Error sending to log channel: " + e.getMessage());
        }
    }
    
    /**
     * Send log entry to the audit channel
     */
    private void sendToAuditChannel(Guild guild, LogEntry entry, User target, User moderator) {
        String channelId = guildAuditChannels.get(guild.getId());
        if (channelId == null) return;
        
        net.dv8tion.jda.api.entities.channel.concrete.TextChannel auditChannel = guild.getTextChannelById(channelId);
        if (auditChannel == null) return;
        
        EmbedBuilder embed = createLogEmbed(entry, target, moderator, true);
        
        try {
            auditChannel.sendMessageEmbeds(embed.build()).queue(
                success -> {},
                error -> System.err.println("Failed to send audit message: " + error.getMessage())
            );
        } catch (Exception e) {
            System.err.println("Error sending to audit channel: " + e.getMessage());
        }
    }
    
    /**
     * Create an embed for logging
     */
    private EmbedBuilder createLogEmbed(LogEntry entry, User target, User moderator, boolean isAudit) {
        EmbedBuilder embed = new EmbedBuilder();
        
        // Set color based on severity
        Color color;
        switch (entry.getSeverity()) {
            case VERY_HIGH:
                color = Color.RED;
                break;
            case HIGH:
                color = Color.ORANGE;
                break;
            case MEDIUM:
                color = Color.YELLOW;
                break;
            case LOW:
                color = Color.GREEN;
                break;
            default:
                color = Color.GRAY;
                break;
        }
        embed.setColor(color);
        
        // Set title
        String title = isAudit ? "🚨 Audit Log" : "📋 Moderation Log";
        embed.setTitle(title + " - " + entry.getAction().toString().replace("_", " "));
        
        // Add fields
        if (target != null) {
            embed.addField("Target", target.getAsMention() + " (" + target.getAsTag() + ")", true);
            embed.addField("Target ID", target.getId(), true);
        }
        
        if (moderator != null) {
            embed.addField("Moderator", moderator.getAsMention() + " (" + moderator.getAsTag() + ")", true);
        } else if (entry.isAutomated()) {
            embed.addField("Moderator", "🤖 Automated System", true);
        }
        
        embed.addField("Reason", entry.getReason(), false);
        embed.addField("Severity", entry.getSeverity().toString(), true);
        embed.addField("Type", entry.isAutomated() ? "Automated" : "Manual", true);
        
        // Add timestamp
        embed.setTimestamp(entry.getTimestamp());
        embed.setFooter("Moderation System", null);
        
        return embed;
    }
    
    /**
     * Add entry to recent logs with cleanup
     */
    private void addToRecentLogs(String guildId, LogEntry entry) {
        List<LogEntry> logs = recentLogs.computeIfAbsent(guildId, k -> new ArrayList<>());
        
        synchronized (logs) {
            logs.add(entry);
            
            // Remove old entries
            Instant cutoff = Instant.now().minusSeconds(LOG_RETENTION_HOURS * 3600);
            logs.removeIf(log -> log.getTimestamp().isBefore(cutoff));
            
            // Limit size
            if (logs.size() > MAX_RECENT_LOGS) {
                logs.subList(0, logs.size() - MAX_RECENT_LOGS).clear();
            }
        }
    }
    
    /**
     * Get recent logs for a guild
     */
    public List<LogEntry> getRecentLogs(String guildId) {
        List<LogEntry> logs = recentLogs.get(guildId);
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }
    
    /**
     * Get recent logs for a specific user
     */
    public List<LogEntry> getRecentLogsForUser(String guildId, String userId) {
        List<LogEntry> allLogs = getRecentLogs(guildId);
        return allLogs.stream()
            .filter(log -> userId.equals(log.getTargetId()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Get logs by action type
     */
    public List<LogEntry> getLogsByAction(String guildId, ModerationAction action) {
        List<LogEntry> allLogs = getRecentLogs(guildId);
        return allLogs.stream()
            .filter(log -> log.getAction() == action)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Get automated vs manual action statistics
     */
    public Map<String, Integer> getActionStatistics(String guildId) {
        List<LogEntry> logs = getRecentLogs(guildId);
        Map<String, Integer> stats = new HashMap<>();
        
        int automated = 0;
        int manual = 0;
        Map<ModerationAction, Integer> actionCounts = new HashMap<>();
        Map<ModerationSeverity, Integer> severityCounts = new HashMap<>();
        
        for (LogEntry log : logs) {
            if (log.isAutomated()) {
                automated++;
            } else {
                manual++;
            }
            
            actionCounts.merge(log.getAction(), 1, Integer::sum);
            severityCounts.merge(log.getSeverity(), 1, Integer::sum);
        }
        
        stats.put("total", logs.size());
        stats.put("automated", automated);
        stats.put("manual", manual);
        
        return stats;
    }
    
    /**
     * Clear logs for a guild
     */
    public void clearLogs(String guildId) {
        recentLogs.remove(guildId);
    }
    
    /**
     * Log entry data class
     */
    public static class LogEntry {
        private final String guildId;
        private final String targetId;
        private final String moderatorId;
        private final ModerationAction action;
        private final String reason;
        private final ModerationSeverity severity;
        private final boolean automated;
        private final Instant timestamp;
        
        public LogEntry(String guildId, String targetId, String moderatorId, 
                       ModerationAction action, String reason, ModerationSeverity severity, 
                       boolean automated, Instant timestamp) {
            this.guildId = guildId;
            this.targetId = targetId;
            this.moderatorId = moderatorId;
            this.action = action;
            this.reason = reason;
            this.severity = severity;
            this.automated = automated;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getGuildId() { return guildId; }
        public String getTargetId() { return targetId; }
        public String getModeratorId() { return moderatorId; }
        public ModerationAction getAction() { return action; }
        public String getReason() { return reason; }
        public ModerationSeverity getSeverity() { return severity; }
        public boolean isAutomated() { return automated; }
        public Instant getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s - %s (%s)", 
                DateTimeFormatter.ISO_INSTANT.format(timestamp),
                action, reason, severity, automated ? "Auto" : "Manual");
        }
    }
}

// Using stub classes from ModerationManager.java