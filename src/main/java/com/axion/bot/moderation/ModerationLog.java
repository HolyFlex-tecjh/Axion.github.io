package com.axion.bot.moderation;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/**
 * Repræsenterer en moderation log entry
 * Bruges til at holde styr på alle moderation handlinger
 */
public class ModerationLog {
    private final String userId;
    private final String username;
    private final String moderatorId;
    private final String moderatorName;
    private final ModerationAction action;
    private final String reason;
    private final Instant timestamp;
    private final String guildId;
    private final String channelId;
    private final String messageId;
    private final int severity;
    private final boolean automated;
    
    public ModerationLog(String userId, String username, String moderatorId, String moderatorName,
                        ModerationAction action, String reason, String guildId, String channelId,
                        String messageId, int severity, boolean automated, Instant timestamp) {
        this.userId = userId;
        this.username = username;
        this.moderatorId = moderatorId;
        this.moderatorName = moderatorName;
        this.action = action;
        this.reason = reason;
        this.timestamp = timestamp;
        this.guildId = guildId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.severity = severity;
        this.automated = automated;
    }
    
    // Alternative constructor for backward compatibility
    public ModerationLog(String userId, String username, String moderatorId, String moderatorName,
                        ModerationAction action, String reason, String guildId, String channelId,
                        String messageId, int severity, boolean automated) {
        this(userId, username, moderatorId, moderatorName, action, reason, guildId, channelId,
             messageId, severity, automated, Instant.now());
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getModeratorId() { return moderatorId; }
    public String getModeratorName() { return moderatorName; }
    public ModerationAction getAction() { return action; }
    public String getReason() { return reason; }
    public Instant getTimestamp() { return timestamp; }
    public String getGuildId() { return guildId; }
    public String getChannelId() { return channelId; }
    public String getMessageId() { return messageId; }
    public int getSeverity() { return severity; }
    public boolean isAutomated() { return automated; }
    
    /**
     * Formaterer timestamp til læsbar streng
     */
    public String getFormattedTimestamp() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(timestamp);
    }
    
    /**
     * Får en kort beskrivelse af log entry
     */
    public String getShortDescription() {
        String automatedText = automated ? "[AUTO]" : "[MANUAL]";
        return String.format("%s %s %s - %s", 
                automatedText, action.name(), action.toString(), reason);
    }
    
    /**
     * Får en detaljeret beskrivelse af log entry
     */
    public String getDetailedDescription() {
        return String.format("**%s** %s\n" +
                "**Bruger:** %s (%s)\n" +
                "**Moderator:** %s (%s)\n" +
                "**Årsag:** %s\n" +
                "**Tidspunkt:** %s\n" +
                "**Alvorlighed:** %d/5\n" +
                "**Type:** %s",
                action.name(), action.toString(),
                username, userId,
                moderatorName, moderatorId,
                reason,
                getFormattedTimestamp(),
                severity,
                automated ? "Automatisk" : "Manuel");
    }
    
    @Override
    public String toString() {
        return String.format("ModerationLog{user=%s, action=%s, reason=%s, timestamp=%s, automated=%s}",
                username, action, reason, getFormattedTimestamp(), automated);
    }
}