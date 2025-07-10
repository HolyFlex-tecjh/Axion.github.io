package com.axion.bot.tickets;

import java.time.LocalDateTime;

/**
 * Repræsenterer en support ticket
 */
public class Ticket {
    private final String ticketId;
    private final String userId;
    private final String guildId;
    private final String threadId;
    private String category;
    private TicketPriority priority;
    private TicketStatus status;
    private String subject;
    private String description;
    private String assignedStaffId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private String closedBy;
    private String closeReason;

    public Ticket(String ticketId, String userId, String guildId, String threadId, 
                  String category, String subject, String description) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.guildId = guildId;
        this.threadId = threadId;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.priority = TicketPriority.MEDIUM;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public String getThreadId() { return threadId; }
    public String getCategory() { return category; }
    public TicketPriority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getAssignedStaffId() { return assignedStaffId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public String getClosedBy() { return closedBy; }
    public String getCloseReason() { return closeReason; }

    // Setters
    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSubject(String subject) {
        this.subject = subject;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAssignedStaffId(String assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
        this.updatedAt = LocalDateTime.now();
    }

    public void close(String closedBy, String closeReason) {
        this.status = TicketStatus.CLOSED;
        this.closedBy = closedBy;
        this.closeReason = closeReason;
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return status == TicketStatus.OPEN;
    }

    public boolean isClosed() {
        return status == TicketStatus.CLOSED;
    }

    public String getPriorityEmoji() {
        return switch (priority) {
            case LOW -> "\uD83D\uDFE2";
            case MEDIUM -> "\uD83D\uDFE1";
            case HIGH -> "\uD83D\uDFE0";
            case URGENT -> "\uD83D\uDD34";
        };
    }

    public String getStatusEmoji() {
        return switch (status) {
            case OPEN -> "\uD83D\uDFE2";
            case IN_PROGRESS -> "\uD83D\uDFE1";
            case WAITING_FOR_USER -> "\u23F3";
            case CLOSED -> "\uD83D\uDD34";
        };
    }
}