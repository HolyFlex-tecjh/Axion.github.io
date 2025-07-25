package com.axion.bot.tickets;

import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Håndterer ticket operationer og thread management
 */
public class TicketManager {
    private static final Logger logger = LoggerFactory.getLogger(TicketManager.class);
    private final TicketService ticketService;
    private final TranslationManager translationManager;
    private final UserLanguageManager userLanguageManager;
    
    // Farver til embeds
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    
    // Emojis
    private static final String TICKET_EMOJI = "\uD83C\uDFAB";
    private static final String LOCK_EMOJI = "\uD83D\uDD12";
    private static final String STAFF_EMOJI = "\uD83D\uDC68\u200D\uD83D\uDCBC";
    
    public TicketManager(TicketService ticketService) {
        this.ticketService = ticketService;
        this.translationManager = TranslationManager.getInstance();
        this.userLanguageManager = UserLanguageManager.getInstance();
    }
    
    /**
     * Helper method to get translated text for a user
     */
    private String translate(String key, String userId, Object... params) {
        String userLanguage = userLanguageManager.getUserLanguage(userId);
        return translationManager.translate(key, userLanguage, params);
    }

    /**
     * Opretter en ny ticket som thread
     */
    public Optional<Ticket> createTicket(Guild guild, User user, String category, String subject, String description) {
        try {
            // Tjek ticket konfiguration
            Optional<TicketConfig> configOpt = ticketService.getTicketConfig(guild.getId());
            TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
            
            if (!config.isEnabled()) {
                logger.warn("Ticket system er deaktiveret for guild: {}", guild.getId());
                return Optional.empty();
            }

            // Tjek om brugeren har for mange åbne tickets
            List<Ticket> userTickets = ticketService.getUserOpenTickets(user.getId(), guild.getId());
            if (userTickets.size() >= config.getMaxTicketsPerUser()) {
                logger.warn("Bruger {} har for mange åbne tickets: {}", user.getId(), userTickets.size());
                return Optional.empty();
            }

            // Find support kategori
            Category supportCategory = null;
            if (config.getSupportCategoryId() != null) {
                supportCategory = guild.getCategoryById(config.getSupportCategoryId());
            }

            // Opret ticket ID
            String ticketId = generateTicketId();
            
            // Opret thread kanal
            String threadName = String.format("%s ticket-%s", TICKET_EMOJI, ticketId.substring(0, 8));
            
            TextChannel parentChannel;
            if (supportCategory != null && !supportCategory.getTextChannels().isEmpty()) {
                parentChannel = supportCategory.getTextChannels().get(0);
            } else {
                // Find første tilgængelige text kanal
                parentChannel = guild.getTextChannels().stream()
                    .filter(channel -> guild.getSelfMember().hasPermission(channel, Permission.CREATE_PRIVATE_THREADS))
                    .findFirst()
                    .orElse(null);
            }

            if (parentChannel == null) {
                logger.error("Kunne ikke finde en passende kanal til at oprette thread i guild: {}", guild.getId());
                return Optional.empty();
            }

            // Opret privat thread
            ThreadChannel thread = parentChannel.createThreadChannel(threadName, true) // true = private thread
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
                .complete();

            // Slet den automatiske thread oprettelsesbesked
            thread.getHistory().retrievePast(1).queue(messages -> {
                if (!messages.isEmpty()) {
                    Message message = messages.get(0);
                    // Tjek om beskeden kan slettes (undgå RECIPIENT_ADD og andre system beskeder)
                    if (message.getType().canDelete()) {
                        message.delete().queue(
                            success -> logger.debug("Thread oprettelsesbesked slettet for ticket: {}", ticketId),
                            error -> logger.debug("Kunne ikke slette thread oprettelsesbesked: {}", error.getMessage())
                        );
                    } else {
                        logger.debug("Springer over sletning af system besked type: {} for ticket: {}", message.getType(), ticketId);
                    }
                }
            });

            // Tilføj bruger til thread
            thread.addThreadMember(user).queue();
            
            // Tilføj staff hvis konfigureret - automatisk for private threads
            if (config.getStaffRoleId() != null) {
                Role staffRole = guild.getRoleById(config.getStaffRoleId());
                if (staffRole != null) {
                    logger.debug("Fandt staff rolle: {} for ticket: {}", staffRole.getName(), ticketId);
                    
                    // Load alle medlemmer med staff rollen
                    guild.loadMembers().onSuccess(members -> {
                        List<Member> staffMembers = guild.getMembersWithRoles(staffRole);
                        logger.debug("Fandt {} staff medlemmer for ticket: {}", staffMembers.size(), ticketId);
                        
                        staffMembers.forEach(member -> {
                            thread.addThreadMember(member.getUser()).queue(
                                success -> logger.debug("Tilføjede staff medlem {} til ticket: {}", member.getUser().getName(), ticketId),
                                failure -> logger.warn("Kunne ikke tilføje staff medlem {} til ticket {}: {}", member.getUser().getName(), ticketId, failure.getMessage())
                            );
                        });
                    }).onError(loadError -> {
                        logger.warn("Kunne ikke loade medlemmer for guild {}: {}", guild.getId(), loadError.getMessage());
                        // Fallback til cached medlemmer
                        List<Member> staffMembers = guild.getMembersWithRoles(staffRole);
                        logger.debug("Fallback: Fandt {} cached staff medlemmer for ticket: {}", staffMembers.size(), ticketId);
                        
                        staffMembers.forEach(member -> {
                            thread.addThreadMember(member.getUser()).queue(
                                success -> logger.debug("Tilføjede cached staff medlem {} til ticket: {}", member.getUser().getName(), ticketId),
                                failure -> logger.warn("Kunne ikke tilføje cached staff medlem {} til ticket {}: {}", member.getUser().getName(), ticketId, failure.getMessage())
                            );
                        });
                    });
                } else {
                    logger.warn("Staff rolle med ID {} ikke fundet i guild: {}", config.getStaffRoleId(), guild.getId());
                }
            } else {
                logger.debug("Ingen staff rolle konfigureret for guild: {}", guild.getId());
            }
            
            // Tilføj bot selv til private thread
            thread.addThreadMember(guild.getSelfMember().getUser()).queue();

            // Opret ticket objekt
            Ticket ticket = new Ticket(ticketId, user.getId(), guild.getId(), thread.getId(), category, subject, description);
            
            // Gem ticket i database
            if (!ticketService.createTicket(ticket)) {
                logger.error("Kunne ikke gemme ticket i database: {}", ticketId);
                thread.delete().queue();
                return Optional.empty();
            }

            // Send velkomstbesked
            sendWelcomeMessage(thread, ticket, user, config);
            
            logger.info("Ticket oprettet: {} for bruger: {} i guild: {}", ticketId, user.getId(), guild.getId());
            return Optional.of(ticket);
            
        } catch (Exception e) {
            logger.error("Fejl ved oprettelse af ticket: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Lukker en ticket og sletter den permanent
     */
    public boolean closeTicket(String ticketId, User closedBy, String reason) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
            if (ticketOpt.isEmpty()) {
                logger.warn("Ticket ikke fundet: {}", ticketId);
                return false;
            }

            Ticket ticket = ticketOpt.get();
            if (ticket.isClosed()) {
                logger.warn("Ticket er allerede lukket: {}", ticketId);
                return false;
            }

            // Find thread og slet den
            Guild guild = closedBy.getJDA().getGuildById(ticket.getGuildId());
            if (guild != null) {
                ThreadChannel thread = guild.getThreadChannelById(ticket.getThreadId());
                if (thread != null) {
                    try {
                        // Send lukkebesked
                        sendCloseMessage(thread, ticket, closedBy, reason);
                        
                        // Slet thread øjeblikkeligt
                        thread.delete().queue(
                            success -> {
                                logger.info("Thread slettet for ticket: {}", ticketId);
                                // Slet ticket fra database efter thread er slettet
                                if (!ticketService.deleteTicket(ticketId)) {
                                    logger.error("Kunne ikke slette ticket fra database: {}", ticketId);
                                }
                            },
                            error -> {
                                logger.warn("Kunne ikke slette thread for ticket {}: {}", ticketId, error.getMessage());
                                // Slet ticket fra database selvom thread sletning fejlede
                                if (!ticketService.deleteTicket(ticketId)) {
                                    logger.error("Kunne ikke slette ticket fra database: {}", ticketId);
                                }
                            }
                        );
                    } catch (Exception e) {
                        logger.warn("Kunne ikke sende lukkebesked eller slette thread for ticket {}: {}", ticketId, e.getMessage());
                        // Slet ticket fra database selvom thread operationer fejlede
                        if (!ticketService.deleteTicket(ticketId)) {
                            logger.error("Kunne ikke slette ticket fra database: {}", ticketId);
                        }
                    }
                } else {
                    logger.warn("Thread ikke fundet for ticket: {} - sletter ticket fra database", ticketId);
                    // Slet ticket fra database selvom thread ikke findes
                    if (!ticketService.deleteTicket(ticketId)) {
                        logger.error("Kunne ikke slette ticket fra database: {}", ticketId);
                    }
                }
            } else {
                logger.warn("Guild ikke fundet for ticket: {} - sletter ticket fra database", ticketId);
                // Slet ticket fra database selvom guild ikke findes
                if (!ticketService.deleteTicket(ticketId)) {
                    logger.error("Kunne ikke slette ticket fra database: {}", ticketId);
                }
            }

            logger.info("Ticket lukket og slettet: {} af bruger: {}", ticketId, closedBy.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("Fejl ved lukning af ticket: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Tildeler en ticket til en staff medlem
     */
    public boolean assignTicket(String ticketId, User staff) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
            if (ticketOpt.isEmpty()) {
                return false;
            }

            Ticket ticket = ticketOpt.get();
            ticket.setAssignedStaffId(staff.getId());
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            
            boolean updated = ticketService.updateTicket(ticket);
            if (updated) {
                // Send besked i thread
                Guild guild = staff.getJDA().getGuildById(ticket.getGuildId());
                if (guild != null) {
                    ThreadChannel thread = guild.getThreadChannelById(ticket.getThreadId());
                    if (thread != null) {
                        try {
                            String userId = staff.getId();
                            
                            EmbedBuilder embed = new EmbedBuilder()
                                .setTitle(STAFF_EMOJI + " " + translate("ticket.assign.title", userId))
                                .setDescription(translate("ticket.assign.description", userId, staff.getAsMention()))
                                .setColor(INFO_COLOR)
                                .setTimestamp(Instant.now());
                            
                            thread.sendMessageEmbeds(embed.build()).queue(
                                success -> logger.debug("Tildelingsbesked sendt for ticket: {}", ticketId),
                                error -> logger.warn("Kunne ikke sende tildelingsbesked for ticket {}: {}", ticketId, error.getMessage())
                            );
                        } catch (Exception e) {
                            logger.warn("Fejl ved sending af tildelingsbesked for ticket {}: {}", ticketId, e.getMessage());
                        }
                    } else {
                        logger.warn("Thread ikke fundet for ticket: {} ved tildeling", ticketId);
                    }
                } else {
                    logger.warn("Guild ikke fundet for ticket: {} ved tildeling", ticketId);
                }
            }
            
            return updated;
            
        } catch (Exception e) {
            logger.error("Fejl ved tildeling af ticket: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Ændrer ticket prioritet
     */
    public boolean setTicketPriority(String ticketId, TicketPriority priority, User changedBy) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicket(ticketId);
            if (ticketOpt.isEmpty()) {
                return false;
            }

            Ticket ticket = ticketOpt.get();
            ticket.setPriority(priority);
            
            boolean updated = ticketService.updateTicket(ticket);
            if (updated) {
                // Update the original welcome message embed with new priority
                Guild guild = changedBy.getJDA().getGuildById(ticket.getGuildId());
                if (guild != null) {
                    ThreadChannel thread = guild.getThreadChannelById(ticket.getThreadId());
                    if (thread != null) {
                        try {
                            updateWelcomeMessagePriority(thread, ticket, changedBy);
                        } catch (Exception e) {
                            logger.warn("Fejl ved opdatering af velkomstbesked for ticket {}: {}", ticketId, e.getMessage());
                        }
                    } else {
                        logger.warn("Thread ikke fundet for ticket: {} ved prioritetsændring", ticketId);
                    }
                } else {
                    logger.warn("Guild ikke fundet for ticket: {} ved prioritetsændring", ticketId);
                }
            }
            
            return updated;
            
        } catch (Exception e) {
            logger.error("Fejl ved ændring af ticket prioritet: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sender velkomstbesked til ny ticket
     */
    private void sendWelcomeMessage(ThreadChannel thread, Ticket ticket, User user, TicketConfig config) {
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

        // Tilføj knapper
        ActionRow buttons = ActionRow.of(
            Button.danger("close_ticket_" + ticket.getTicketId(), translate("ticket.welcome.buttons.close", userId)).withEmoji(Emoji.fromUnicode("\uD83D\uDD12")),
            Button.secondary("assign_ticket_" + ticket.getTicketId(), translate("ticket.welcome.buttons.assign", userId)).withEmoji(Emoji.fromUnicode("\uD83D\uDC68\u200D\uD83D\uDCBC")),
            Button.primary("priority_ticket_" + ticket.getTicketId(), translate("ticket.welcome.buttons.priority", userId)).withEmoji(Emoji.fromUnicode("\uD83D\uDCCA"))
        );

        thread.sendMessageEmbeds(embed.build()).setComponents(buttons).queue();
    }

    /**
     * Sender lukkebesked til ticket
     */
    private void sendCloseMessage(ThreadChannel thread, Ticket ticket, User closedBy, String reason) {
        String userId = closedBy.getId();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(LOCK_EMOJI + " " + translate("ticket.close.title", userId))
            .setDescription(translate("ticket.close.description", userId))
            .addField(translate("ticket.close.closed_by", userId), closedBy.getAsMention(), true)
            .addField(translate("ticket.close.reason", userId), reason != null ? reason : translate("ticket.close.no_reason", userId), true)
            .addField(translate("ticket.close.duration", userId), calculateTicketDuration(ticket, userId), true)
            .setColor(ERROR_COLOR)
            .setTimestamp(Instant.now());

        thread.sendMessageEmbeds(embed.build()).queue(
            success -> logger.debug("Lukkebesked sendt for ticket: {}", ticket.getTicketId()),
            error -> logger.warn("Kunne ikke sende lukkebesked for ticket {}: {}", ticket.getTicketId(), error.getMessage())
        );
    }

    /**
     * Updates the welcome message embed with new priority
     */
    private void updateWelcomeMessagePriority(ThreadChannel thread, Ticket ticket, User changedBy) {
        try {
            String userId = changedBy.getId();
            String priorityFieldName = translate("ticket.welcome.priority", userId);
            logger.info("Attempting to update priority field '{}' for ticket {} (user language: {})", 
                priorityFieldName, ticket.getTicketId(), userLanguageManager.getUserLanguage(userId));
            
            // Get the first message in the thread (welcome message)
            thread.getHistory().retrievePast(1).queue(messages -> {
                logger.info("Retrieved {} messages from thread for ticket {}", messages.size(), ticket.getTicketId());
                if (!messages.isEmpty()) {
                    Message welcomeMessage = messages.get(0);
                    logger.info("Welcome message has {} embeds for ticket {}", welcomeMessage.getEmbeds().size(), ticket.getTicketId());
                    if (!welcomeMessage.getEmbeds().isEmpty()) {
                        MessageEmbed originalEmbed = welcomeMessage.getEmbeds().get(0);
                        logger.info("Original embed has {} fields for ticket {}", originalEmbed.getFields().size(), ticket.getTicketId());
                        
                        // Create a completely new embed to ensure proper update
                        EmbedBuilder newEmbed = new EmbedBuilder()
                            .setTitle(originalEmbed.getTitle())
                            .setDescription(originalEmbed.getDescription())
                            .setColor(originalEmbed.getColor())
                            .setThumbnail(originalEmbed.getThumbnail() != null ? originalEmbed.getThumbnail().getUrl() : null)
                            .setFooter(originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : null,
                                     originalEmbed.getFooter() != null ? originalEmbed.getFooter().getIconUrl() : null)
                            .setTimestamp(originalEmbed.getTimestamp());
                        
                        // Add all fields, updating the priority field
                        String priorityText = ticket.getPriority().toString();
                        logger.info("New priority text: '{}' for ticket {}", priorityText, ticket.getTicketId());
                        
                        boolean fieldFound = false;
                        for (MessageEmbed.Field field : originalEmbed.getFields()) {
                            String fieldName = field.getName();
                            if (fieldName != null) {
                                // Normalize both strings for comparison (remove extra whitespace, normalize Unicode)
                                String normalizedFieldName = fieldName.trim().replaceAll("\\s+", " ");
                                String normalizedPriorityName = priorityFieldName.trim().replaceAll("\\s+", " ");
                                
                                logger.info("Comparing field '{}' (normalized: '{}') against '{}' (normalized: '{}') for ticket {}", 
                                    fieldName, normalizedFieldName, priorityFieldName, normalizedPriorityName, ticket.getTicketId());
                                
                                if (normalizedFieldName.equals(normalizedPriorityName) || 
                                    normalizedFieldName.equalsIgnoreCase(normalizedPriorityName) ||
                                    fieldName.contains("Priority") || fieldName.contains("Prioritet")) {
                                    newEmbed.addField(fieldName, priorityText, field.isInline());
                                    fieldFound = true;
                                    logger.info("✅ Successfully matched and updated priority field '{}' for ticket {}", fieldName, ticket.getTicketId());
                                } else {
                                    newEmbed.addField(fieldName, field.getValue(), field.isInline());
                                }
                            } else {
                                newEmbed.addField(fieldName, field.getValue(), field.isInline());
                            }
                        }
                        
                        if (!fieldFound) {
                            logger.error("❌ Priority field '{}' not found in embed for ticket {}. Available fields:", priorityFieldName, ticket.getTicketId());
                            for (MessageEmbed.Field field : originalEmbed.getFields()) {
                                logger.error("  - '{}' (length: {}, bytes: {})", field.getName(), 
                                    field.getName() != null ? field.getName().length() : 0,
                                    field.getName() != null ? java.util.Arrays.toString(field.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8)) : "null");
                            }
                            logger.error("Expected priority field name: '{}' (length: {}, bytes: {})", 
                                priorityFieldName, priorityFieldName.length(), 
                                java.util.Arrays.toString(priorityFieldName.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        }
                        
                        // Update the message with the new embed
                        welcomeMessage.editMessageEmbeds(newEmbed.build()).queue(
                            success -> logger.info("✅ Successfully updated welcome message priority for ticket: {}", ticket.getTicketId()),
                            error -> logger.error("❌ Failed to update welcome message for ticket {}: {}", ticket.getTicketId(), error.getMessage())
                        );
                    } else {
                        logger.warn("Welcome message has no embeds for ticket {}", ticket.getTicketId());
                    }
                } else {
                    logger.warn("No messages found in thread for ticket {}", ticket.getTicketId());
                }
            }, error -> logger.error("Failed to retrieve messages for ticket {}: {}", ticket.getTicketId(), error.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating welcome message for ticket {}: {}", ticket.getTicketId(), e.getMessage(), e);
        }
    }



    /**
     * Beregner ticket varighed
     */
    private String calculateTicketDuration(Ticket ticket, String userId) {
        if (ticket.getClosedAt() == null) {
            return translate("ticket.duration.unknown", userId);
        }
        
        long minutes = java.time.Duration.between(ticket.getCreatedAt(), ticket.getClosedAt()).toMinutes();
        
        if (minutes < 60) {
            return translate("ticket.duration.minutes", userId, minutes);
        } else if (minutes < 1440) {
            return translate("ticket.duration.hours", userId, (minutes / 60));
        } else {
            return translate("ticket.duration.days", userId, (minutes / 1440));
        }
    }

    /**
     * Genererer unikt ticket ID
     */
    private String generateTicketId() {
        return "TKT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Tjekker om en bruger har staff rettigheder
     */
    public boolean hasStaffPermissions(Member member, String guildId) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        Optional<TicketConfig> configOpt = ticketService.getTicketConfig(guildId);
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

    /**
     * Henter ticket service
     */
    public TicketService getTicketService() {
        return ticketService;
    }
}