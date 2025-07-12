package com.axion.bot.tickets;

import com.axion.bot.database.OptimizedDatabaseManager;
import com.axion.bot.optimization.*;
import com.axion.bot.services.TicketService;
import com.axion.bot.services.UserLanguageManager;
import com.axion.bot.models.Ticket;
import com.axion.bot.models.TicketConfig;
import com.axion.bot.models.TicketPriority;
import com.axion.bot.models.TicketStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Optimized Ticket Manager with performance improvements:
 * - Connection pooling for database operations
 * - Smart caching for translations and configurations
 * - Async processing for Discord operations
 * - Object pooling for frequently used objects
 * - Performance monitoring and metrics
 */
public class OptimizedTicketManager {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedTicketManager.class);
    
    // Optimized components
    private final OptimizedDatabaseManager databaseManager;
    private final OptimizedTranslationManager translationManager;
    private final OptimizedAsyncProcessor asyncProcessor;
    private final ObjectPoolManager objectPoolManager;
    private final OptimizedCommandRegistry commandRegistry;
    
    // Services
    private final TicketService ticketService;
    private final UserLanguageManager userLanguageManager;
    
    // Constants
    private static final Color SUCCESS_COLOR = new Color(0, 255, 0);
    private static final Color ERROR_COLOR = new Color(255, 0, 0);
    private static final Color INFO_COLOR = new Color(0, 123, 255);
    
    private static final String TICKET_EMOJI = "🎫";
    private static final String STAFF_EMOJI = "👨‍💼";
    private static final String LOCK_EMOJI = "🔒";
    
    // Cache keys
    private static final String CACHE_KEY_TICKET_CONFIG = "ticket_config_%s";
    private static final String CACHE_KEY_USER_TICKETS = "user_tickets_%s_%s";
    
    public OptimizedTicketManager(OptimizedDatabaseManager databaseManager,
                                 OptimizedTranslationManager translationManager,
                                 OptimizedAsyncProcessor asyncProcessor,
                                 ObjectPoolManager objectPoolManager,
                                 OptimizedCommandRegistry commandRegistry,
                                 TicketService ticketService,
                                 UserLanguageManager userLanguageManager) {
        this.databaseManager = databaseManager;
        this.translationManager = translationManager;
        this.asyncProcessor = asyncProcessor;
        this.objectPoolManager = objectPoolManager;
        this.commandRegistry = commandRegistry;
        this.ticketService = ticketService;
        this.userLanguageManager = userLanguageManager;
        
        logger.info("OptimizedTicketManager initialized with performance monitoring");
    }
    
    /**
     * Creates a new ticket with optimized performance
     */
    public CompletableFuture<Boolean> createTicket(User user, Guild guild, String category, 
                                                   String subject, String description, 
                                                   TicketPriority priority) {
        return asyncProcessor.submitGeneralTask(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Check if user can create tickets (with caching)
                if (!canUserCreateTicket(user.getId(), guild.getId())) {
                    logger.warn("User {} cannot create ticket in guild {}", user.getId(), guild.getId());
                    return false;
                }
                
                // Get ticket configuration (cached)
                Optional<TicketConfig> configOpt = getCachedTicketConfig(guild.getId());
                if (configOpt.isEmpty()) {
                    logger.warn("No ticket configuration found for guild: {}", guild.getId());
                    return false;
                }
                
                TicketConfig config = configOpt.get();
                
                // Find support category
                Category supportCategory = findSupportCategory(guild, config);
                if (supportCategory == null) {
                    logger.warn("Support category not found for guild: {}", guild.getId());
                    return false;
                }
                
                // Generate ticket ID and create ticket object
                String ticketId = generateTicketId();
                Ticket ticket = createTicketObject(ticketId, user, guild, category, subject, description, priority);
                
                // Create thread asynchronously
                return createTicketThreadAsync(ticket, user, guild, supportCategory, config)
                    .thenCompose(threadCreated -> {
                        if (threadCreated) {
                            // Save ticket to database asynchronously
                            return asyncProcessor.submitDatabaseTask(() -> {
                                boolean saved = ticketService.saveTicket(ticket);
                                if (saved) {
                                    // Invalidate user tickets cache
                                    invalidateUserTicketsCache(user.getId(), guild.getId());
                                    logger.info("Ticket created successfully: {} for user: {} in {}ms", 
                                        ticketId, user.getId(), System.currentTimeMillis() - startTime);
                                }
                                return saved;
                            });
                        }
                        return CompletableFuture.completedFuture(false);
                    })
                    .get(30, TimeUnit.SECONDS); // Timeout after 30 seconds
                    
            } catch (Exception e) {
                logger.error("Error creating ticket for user {}: {}", user.getId(), e.getMessage(), e);
                return false;
            }
        });
    }
    
    /**
     * Closes a ticket with optimized performance
     */
    public CompletableFuture<Boolean> closeTicket(String ticketId, User closedBy, String reason) {
        return asyncProcessor.submitGeneralTask(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
                if (ticketOpt.isEmpty()) {
                    logger.warn("Ticket not found: {}", ticketId);
                    return false;
                }
                
                Ticket ticket = ticketOpt.get();
                if (ticket.isClosed()) {
                    logger.warn("Ticket already closed: {}", ticketId);
                    return false;
                }
                
                // Close ticket asynchronously
                return closeTicketAsync(ticket, closedBy, reason)
                    .thenCompose(closed -> {
                        if (closed) {
                            // Update database asynchronously
                            return asyncProcessor.submitDatabaseTask(() -> {
                                boolean deleted = ticketService.deleteTicket(ticketId);
                                if (deleted) {
                                    // Invalidate caches
                                    invalidateUserTicketsCache(ticket.getUserId(), ticket.getGuildId());
                                    logger.info("Ticket closed and deleted: {} by user: {} in {}ms", 
                                        ticketId, closedBy.getId(), System.currentTimeMillis() - startTime);
                                }
                                return deleted;
                            });
                        }
                        return CompletableFuture.completedFuture(false);
                    })
                    .get(30, TimeUnit.SECONDS);
                    
            } catch (Exception e) {
                logger.error("Error closing ticket {}: {}", ticketId, e.getMessage(), e);
                return false;
            }
        });
    }
    
    /**
     * Assigns a ticket to staff with optimized performance
     */
    public CompletableFuture<Boolean> assignTicket(String ticketId, User staff) {
        return asyncProcessor.submitGeneralTask(() -> {
            try {
                Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
                if (ticketOpt.isEmpty()) {
                    return false;
                }
                
                Ticket ticket = ticketOpt.get();
                ticket.setAssignedStaffId(staff.getId());
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                
                // Update database asynchronously
                return asyncProcessor.submitDatabaseTask(() -> {
                    boolean updated = ticketService.updateTicket(ticket);
                    if (updated) {
                        // Send assignment message asynchronously
                        sendAssignmentMessageAsync(ticket, staff);
                    }
                    return updated;
                }).get(15, TimeUnit.SECONDS);
                
            } catch (Exception e) {
                logger.error("Error assigning ticket {}: {}", ticketId, e.getMessage(), e);
                return false;
            }
        });
    }
    
    /**
     * Sets ticket priority with optimized performance
     */
    public CompletableFuture<Boolean> setTicketPriority(String ticketId, TicketPriority priority, User changedBy) {
        return asyncProcessor.submitGeneralTask(() -> {
            try {
                Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
                if (ticketOpt.isEmpty()) {
                    return false;
                }
                
                Ticket ticket = ticketOpt.get();
                ticket.setPriority(priority);
                
                // Update database asynchronously
                return asyncProcessor.submitDatabaseTask(() -> {
                    boolean updated = ticketService.updateTicket(ticket);
                    if (updated) {
                        // Update welcome message asynchronously
                        updateWelcomeMessagePriorityAsync(ticket, changedBy);
                    }
                    return updated;
                }).get(15, TimeUnit.SECONDS);
                
            } catch (Exception e) {
                logger.error("Error setting ticket priority {}: {}", ticketId, e.getMessage(), e);
                return false;
            }
        });
    }
    
    /**
     * Checks if user can create ticket (with caching)
     */
    private boolean canUserCreateTicket(String userId, String guildId) {
        String cacheKey = String.format(CACHE_KEY_USER_TICKETS, userId, guildId);
        
        // Try to get from cache first
        List<Ticket> userTickets = (List<Ticket>) translationManager.getFromCache(cacheKey);
        if (userTickets == null) {
            // Load from database and cache
            userTickets = ticketService.getUserTickets(userId, guildId);
            translationManager.putInCache(cacheKey, userTickets, 5); // Cache for 5 minutes
        }
        
        // Check if user has reached ticket limit
        long openTickets = userTickets.stream()
            .filter(ticket -> !ticket.isClosed())
            .count();
            
        Optional<TicketConfig> configOpt = getCachedTicketConfig(guildId);
        if (configOpt.isPresent()) {
            int maxTickets = configOpt.get().getMaxTicketsPerUser();
            return openTickets < maxTickets;
        }
        
        return openTickets < 3; // Default limit
    }
    
    /**
     * Gets cached ticket configuration
     */
    private Optional<TicketConfig> getCachedTicketConfig(String guildId) {
        String cacheKey = String.format(CACHE_KEY_TICKET_CONFIG, guildId);
        
        TicketConfig config = (TicketConfig) translationManager.getFromCache(cacheKey);
        if (config == null) {
            Optional<TicketConfig> configOpt = ticketService.getTicketConfig(guildId);
            if (configOpt.isPresent()) {
                config = configOpt.get();
                translationManager.putInCache(cacheKey, config, 10); // Cache for 10 minutes
            }
            return configOpt;
        }
        
        return Optional.of(config);
    }
    
    /**
     * Creates ticket thread asynchronously
     */
    private CompletableFuture<Boolean> createTicketThreadAsync(Ticket ticket, User user, Guild guild, 
                                                              Category supportCategory, TicketConfig config) {
        return asyncProcessor.submitNetworkTask(() -> {
            try {
                String threadName = String.format("%s-%s", ticket.getCategory(), ticket.getTicketId());
                
                ThreadChannel thread = supportCategory.createThreadChannel(threadName)
                    .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS)
                    .complete();
                
                if (thread != null) {
                    ticket.setThreadId(thread.getId());
                    
                    // Add user and staff to thread
                    thread.addThreadMember(user).queue();
                    addStaffToThread(thread, guild, config);
                    
                    // Send welcome message
                    sendWelcomeMessageAsync(thread, ticket, user, config);
                    
                    return true;
                }
                
                return false;
                
            } catch (Exception e) {
                logger.error("Error creating thread for ticket {}: {}", ticket.getTicketId(), e.getMessage(), e);
                return false;
            }
        });
    }
    
    /**
     * Sends welcome message asynchronously
     */
    private void sendWelcomeMessageAsync(ThreadChannel thread, Ticket ticket, User user, TicketConfig config) {
        asyncProcessor.submitNetworkTask(() -> {
            try {
                StringBuilder embedBuilder = objectPoolManager.borrowStringBuilder();
                try {
                    String userId = user.getId();
                    
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(TICKET_EMOJI + " " + translate("ticket.welcome.title", userId))
                        .setDescription(config.getWelcomeMessage())
                        .addField(translate("ticket.welcome.ticket_id", userId), "`" + ticket.getTicketId() + "`", true)
                        .addField(translate("ticket.welcome.category", userId), ticket.getCategory(), true)
                        .addField(translate("ticket.welcome.priority", userId), ticket.getPriority().toString(), true)
                        .addField(translate("ticket.welcome.subject", userId), ticket.getSubject(), false)
                        .setColor(SUCCESS_COLOR)
                        .setThumbnail(user.getAvatarUrl())
                        .setTimestamp(Instant.now())
                        .setFooter(translate("ticket.welcome.created_by", userId, user.getName()), user.getAvatarUrl());
                    
                    if (ticket.getDescription() != null && !ticket.getDescription().trim().isEmpty()) {
                        embed.addField(translate("ticket.welcome.description", userId), ticket.getDescription(), false);
                    }
                    
                    // Create buttons
                    ActionRow buttons = ActionRow.of(
                        Button.danger("close_ticket_" + ticket.getTicketId(), 
                            translate("ticket.welcome.buttons.close", userId))
                            .withEmoji(Emoji.fromUnicode("🔒")),
                        Button.secondary("assign_ticket_" + ticket.getTicketId(), 
                            translate("ticket.welcome.buttons.assign", userId))
                            .withEmoji(Emoji.fromUnicode("👨‍💼")),
                        Button.primary("priority_ticket_" + ticket.getTicketId(), 
                            translate("ticket.welcome.buttons.priority", userId))
                            .withEmoji(Emoji.fromUnicode("📊"))
                    );
                    
                    thread.sendMessageEmbeds(embed.build()).setComponents(buttons).queue(
                        success -> logger.debug("Welcome message sent for ticket: {}", ticket.getTicketId()),
                        error -> logger.warn("Failed to send welcome message for ticket {}: {}", 
                            ticket.getTicketId(), error.getMessage())
                    );
                    
                } finally {
                    objectPoolManager.returnStringBuilder(embedBuilder);
                }
                
            } catch (Exception e) {
                logger.error("Error sending welcome message for ticket {}: {}", 
                    ticket.getTicketId(), e.getMessage(), e);
            }
            
            return null;
        });
    }
    
    /**
     * Optimized translation with caching
     */
    private String translate(String key, String userId, Object... params) {
        return translationManager.translate(key, userLanguageManager.getUserLanguage(userId), params);
    }
    
    /**
     * Invalidates user tickets cache
     */
    private void invalidateUserTicketsCache(String userId, String guildId) {
        String cacheKey = String.format(CACHE_KEY_USER_TICKETS, userId, guildId);
        translationManager.invalidateCache(cacheKey);
    }
    
    // Additional helper methods would be implemented here...
    // (Placeholder methods for compilation)
    
    private CompletableFuture<Boolean> closeTicketAsync(Ticket ticket, User closedBy, String reason) {
        return CompletableFuture.completedFuture(true);
    }
    
    private void sendAssignmentMessageAsync(Ticket ticket, User staff) {
        // Implementation would go here
    }
    
    private void updateWelcomeMessagePriorityAsync(Ticket ticket, User changedBy) {
        // Implementation would go here
    }
    
    private Category findSupportCategory(Guild guild, TicketConfig config) {
        return guild.getCategoryById(config.getCategoryId());
    }
    
    private Ticket createTicketObject(String ticketId, User user, Guild guild, String category, 
                                     String subject, String description, TicketPriority priority) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        ticket.setUserId(user.getId());
        ticket.setGuildId(guild.getId());
        ticket.setCategory(category);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(Instant.now());
        return ticket;
    }
    
    private void addStaffToThread(ThreadChannel thread, Guild guild, TicketConfig config) {
        // Implementation would go here
    }
    
    private String generateTicketId() {
        return "TKT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public boolean hasStaffPermissions(Member member, String guildId) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        
        Optional<TicketConfig> configOpt = getCachedTicketConfig(guildId);
        if (configOpt.isPresent()) {
            TicketConfig config = configOpt.get();
            
            if (config.getStaffRoleId() != null) {
                return member.getRoles().stream()
                    .anyMatch(role -> role.getId().equals(config.getStaffRoleId()));
            }
            
            if (config.getAdminRoleId() != null) {
                return member.getRoles().stream()
                    .anyMatch(role -> role.getId().equals(config.getAdminRoleId()));
            }
        }
        
        return member.hasPermission(Permission.MANAGE_CHANNEL);
    }
    
    public TicketService getTicketService() {
        return ticketService;
    }
}