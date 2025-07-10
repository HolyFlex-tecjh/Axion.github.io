package com.axion.bot.tickets;

import com.axion.bot.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service klasse til håndtering af ticket database operationer
 */
public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    private final DatabaseService databaseService;

    public TicketService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Opretter en ny ticket i databasen
     */
    public boolean createTicket(Ticket ticket) {
        String sql = """
            INSERT INTO tickets (ticket_id, user_id, guild_id, thread_id, category, priority, 
                               status, subject, description, assigned_staff_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null - kan ikke oprette ticket");
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ticket.getTicketId());
            stmt.setString(2, ticket.getUserId());
            stmt.setString(3, ticket.getGuildId());
            stmt.setString(4, ticket.getThreadId());
            stmt.setString(5, ticket.getCategory());
            stmt.setString(6, ticket.getPriority().name());
            stmt.setString(7, ticket.getStatus().name());
            stmt.setString(8, ticket.getSubject());
            stmt.setString(9, ticket.getDescription());
            stmt.setString(10, ticket.getAssignedStaffId());
            stmt.setTimestamp(11, Timestamp.valueOf(ticket.getCreatedAt()));
            stmt.setTimestamp(12, Timestamp.valueOf(ticket.getUpdatedAt()));

            int rowsAffected = stmt.executeUpdate();
            logger.info("Ticket oprettet: {} for bruger: {}", ticket.getTicketId(), ticket.getUserId());
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Fejl ved oprettelse af ticket: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Henter en ticket baseret på ticket ID
     */
    public Optional<Ticket> getTicket(String ticketId) {
        String sql = "SELECT * FROM tickets WHERE ticket_id = ?";
        
        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return Optional.empty();
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ticketId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af ticket: {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Henter en ticket baseret på thread ID
     */
    public Optional<Ticket> getTicketByThreadId(String threadId) {
        String sql = "SELECT * FROM tickets WHERE thread_id = ?";
        
        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return Optional.empty();
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, threadId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af ticket via thread ID: {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Henter alle åbne tickets for en bruger
     */
    public List<Ticket> getUserOpenTickets(String userId, String guildId) {
        String sql = "SELECT * FROM tickets WHERE user_id = ? AND guild_id = ? AND status != 'CLOSED' ORDER BY created_at DESC";
        List<Ticket> tickets = new ArrayList<>();
        
        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return tickets;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af bruger tickets: {}", e.getMessage());
        }
        
        return tickets;
    }

    /**
     * Opdaterer en ticket
     */
    public boolean updateTicket(Ticket ticket) {
        String sql = """
            UPDATE tickets SET category = ?, priority = ?, status = ?, subject = ?, 
                             description = ?, assigned_staff_id = ?, updated_at = ?, 
                             closed_at = ?, closed_by = ?, close_reason = ?
            WHERE ticket_id = ?
            """;

        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ticket.getCategory());
            stmt.setString(2, ticket.getPriority().name());
            stmt.setString(3, ticket.getStatus().name());
            stmt.setString(4, ticket.getSubject());
            stmt.setString(5, ticket.getDescription());
            stmt.setString(6, ticket.getAssignedStaffId());
            stmt.setTimestamp(7, Timestamp.valueOf(ticket.getUpdatedAt()));
            stmt.setTimestamp(8, ticket.getClosedAt() != null ? Timestamp.valueOf(ticket.getClosedAt()) : null);
            stmt.setString(9, ticket.getClosedBy());
            stmt.setString(10, ticket.getCloseReason());
            stmt.setString(11, ticket.getTicketId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Fejl ved opdatering af ticket: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Henter alle tickets for en guild
     */
    public List<Ticket> getGuildTickets(String guildId, boolean includeClosedTickets) {
        String sql = includeClosedTickets ? 
            "SELECT * FROM tickets WHERE guild_id = ? ORDER BY created_at DESC" :
            "SELECT * FROM tickets WHERE guild_id = ? AND status != 'CLOSED' ORDER BY created_at DESC";
        
        List<Ticket> tickets = new ArrayList<>();
        
        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return tickets;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af guild tickets: {}", e.getMessage());
        }
        
        return tickets;
    }

    /**
     * Mapper ResultSet til Ticket objekt
     */
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket(
            rs.getString("ticket_id"),
            rs.getString("user_id"),
            rs.getString("guild_id"),
            rs.getString("thread_id"),
            rs.getString("category"),
            rs.getString("subject"),
            rs.getString("description")
        );

        ticket.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        ticket.setAssignedStaffId(rs.getString("assigned_staff_id"));
        
        Timestamp closedAt = rs.getTimestamp("closed_at");
        if (closedAt != null && rs.getString("closed_by") != null) {
            ticket.close(rs.getString("closed_by"), rs.getString("close_reason"));
        }

        return ticket;
    }

    /**
     * Henter ticket konfiguration for en guild
     */
    public Optional<TicketConfig> getTicketConfig(String guildId) {
        String sql = "SELECT * FROM ticket_config WHERE guild_id = ?";
        
        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return Optional.empty();
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, guildId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TicketConfig config = new TicketConfig(guildId);
                    config.setEnabled(rs.getBoolean("enabled"));
                    config.setSupportCategoryId(rs.getString("support_category_id"));
                    config.setStaffRoleId(rs.getString("staff_role_id"));
                    config.setAdminRoleId(rs.getString("admin_role_id"));
                    config.setTranscriptChannelId(rs.getString("transcript_channel_id"));
                    config.setMaxTicketsPerUser(rs.getInt("max_tickets_per_user"));
                    config.setAutoCloseInactiveHours(rs.getInt("auto_close_inactive_hours"));
                    config.setWelcomeMessage(rs.getString("welcome_message"));
                    return Optional.of(config);
                }
            }
        } catch (SQLException e) {
            logger.error("Fejl ved hentning af ticket config: {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Gemmer ticket konfiguration
     */
    public boolean saveTicketConfig(TicketConfig config) {
        String sql = """
            INSERT OR REPLACE INTO ticket_config 
            (guild_id, enabled, support_category_id, staff_role_id, admin_role_id, 
             transcript_channel_id, max_tickets_per_user, auto_close_inactive_hours, 
             welcome_message, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection connection = databaseService.getConnection();
        if (connection == null) {
            logger.error("Database forbindelse er null");
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, config.getGuildId());
            stmt.setBoolean(2, config.isEnabled());
            stmt.setString(3, config.getSupportCategoryId());
            stmt.setString(4, config.getStaffRoleId());
            stmt.setString(5, config.getAdminRoleId());
            stmt.setString(6, config.getTranscriptChannelId());
            stmt.setInt(7, config.getMaxTicketsPerUser());
            stmt.setInt(8, config.getAutoCloseInactiveHours());
            stmt.setString(9, config.getWelcomeMessage());
            stmt.setTimestamp(10, Timestamp.valueOf(config.getUpdatedAt()));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Fejl ved gemning af ticket config: {}", e.getMessage());
            return false;
        }
    }
}