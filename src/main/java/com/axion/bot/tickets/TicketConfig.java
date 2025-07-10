package com.axion.bot.tickets;

import java.time.LocalDateTime;

/**
 * Konfiguration for ticket systemet per guild
 */
public class TicketConfig {
    private final String guildId;
    private boolean enabled;
    private String supportCategoryId;
    private String staffRoleId;
    private String adminRoleId;
    private String transcriptChannelId;
    private int maxTicketsPerUser;
    private int autoCloseInactiveHours;
    private String welcomeMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketConfig(String guildId) {
        this.guildId = guildId;
        this.enabled = true;
        this.maxTicketsPerUser = 3;
        this.autoCloseInactiveHours = 72; // 3 dage
        this.welcomeMessage = "Tak for at oprette en ticket! Et medlem af vores support team vil hjælpe dig snarest.";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getGuildId() { return guildId; }
    public boolean isEnabled() { return enabled; }
    public String getSupportCategoryId() { return supportCategoryId; }
    public String getStaffRoleId() { return staffRoleId; }
    public String getAdminRoleId() { return adminRoleId; }
    public String getTranscriptChannelId() { return transcriptChannelId; }
    public int getMaxTicketsPerUser() { return maxTicketsPerUser; }
    public int getAutoCloseInactiveHours() { return autoCloseInactiveHours; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSupportCategoryId(String supportCategoryId) {
        this.supportCategoryId = supportCategoryId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStaffRoleId(String staffRoleId) {
        this.staffRoleId = staffRoleId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAdminRoleId(String adminRoleId) {
        this.adminRoleId = adminRoleId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setTranscriptChannelId(String transcriptChannelId) {
        this.transcriptChannelId = transcriptChannelId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setMaxTicketsPerUser(int maxTicketsPerUser) {
        this.maxTicketsPerUser = Math.max(1, Math.min(10, maxTicketsPerUser));
        this.updatedAt = LocalDateTime.now();
    }

    public void setAutoCloseInactiveHours(int autoCloseInactiveHours) {
        this.autoCloseInactiveHours = Math.max(1, autoCloseInactiveHours);
        this.updatedAt = LocalDateTime.now();
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Opretter standard konfiguration for en guild
     */
    public static TicketConfig createDefault(String guildId) {
        TicketConfig config = new TicketConfig(guildId);
        config.setWelcomeMessage(
            "🎫 **Velkommen til din support ticket!**\n\n" +
            "Tak fordi du kontaktede os. Et medlem af vores support team vil hjælpe dig snarest muligt.\n\n" +
            "**Hvad kan du forvente:**\n" +
            "• Svar inden for 24 timer\n" +
            "• Professionel og venlig service\n" +
            "• Løsning af dit problem\n\n" +
            "**Vigtige noter:**\n" +
            "• Vær venlig og respektfuld\n" +
            "• Giv så mange detaljer som muligt\n" +
            "• Tickets lukkes automatisk efter 72 timer uden aktivitet\n\n" +
            "Du kan lukke denne ticket når som helst med `/ticket close`"
        );
        return config;
    }

    /**
     * Validerer konfigurationen
     */
    public boolean isValid() {
        return guildId != null && !guildId.trim().isEmpty() &&
               maxTicketsPerUser > 0 && maxTicketsPerUser <= 10 &&
               autoCloseInactiveHours > 0 && autoCloseInactiveHours <= 168; // Max 1 uge
    }

    /**
     * Tjekker om ticket systemet er korrekt konfigureret
     */
    public boolean isProperlyConfigured() {
        return isValid() && 
               enabled && 
               supportCategoryId != null && 
               !supportCategoryId.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format(
            "TicketConfig{guildId='%s', enabled=%s, supportCategoryId='%s', staffRoleId='%s', " +
            "maxTicketsPerUser=%d, autoCloseInactiveHours=%d}",
            guildId, enabled, supportCategoryId, staffRoleId, 
            maxTicketsPerUser, autoCloseInactiveHours
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TicketConfig that = (TicketConfig) obj;
        return guildId != null ? guildId.equals(that.guildId) : that.guildId == null;
    }

    @Override
    public int hashCode() {
        return guildId != null ? guildId.hashCode() : 0;
    }
}