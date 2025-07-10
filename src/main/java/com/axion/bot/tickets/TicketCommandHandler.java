package com.axion.bot.tickets;

import com.axion.bot.translation.TranslationManager;
import com.axion.bot.translation.UserLanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Håndterer ticket kommandoer og interaktioner
 */
public class TicketCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(TicketCommandHandler.class);
    private final TicketManager ticketManager;
    private final TranslationManager translationManager;
    private final UserLanguageManager userLanguageManager;
    
    // Farver
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color WARNING_COLOR = new Color(251, 191, 36);
    
    // Emojis
    private static final String SUCCESS_EMOJI = "\u2705";
    private static final String ERROR_EMOJI = "\u274C";
    private static final String TICKET_EMOJI = "\uD83C\uDFAB";
    private static final String SETTINGS_EMOJI = "\u2699\uFE0F";
    
    public TicketCommandHandler(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
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
     * Håndterer ticket slash kommandoer
     */
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String subcommandName = event.getSubcommandName();
        
        switch (commandName) {
            case "ticket":
                handleTicketCommand(event, subcommandName);
                break;
            case "ticketconfig":
                handleTicketConfigCommand(event, subcommandName);
                break;
            default:
                logger.warn("Ukendt ticket kommando: {}", commandName);
        }
    }

    /**
     * Håndterer ticket kommandoer
     */
    private void handleTicketCommand(SlashCommandInteractionEvent event, String subcommand) {
        switch (subcommand) {
            case "create":
                handleCreateTicket(event);
                break;
            case "close":
                handleCloseTicket(event);
                break;
            case "assign":
                handleAssignTicket(event);
                break;
            case "priority":
                handleSetPriority(event);
                break;
            case "list":
                handleListTickets(event);
                break;
            case "info":
                handleTicketInfo(event);
                break;
            default:
                sendErrorMessage(event, "Ukendt ticket kommando: " + subcommand);
        }
    }

    /**
     * Håndterer oprettelse af ticket
     */
    private void handleCreateTicket(SlashCommandInteractionEvent event) {
        OptionMapping categoryOption = event.getOption("category");
        OptionMapping subjectOption = event.getOption("subject");
        OptionMapping descriptionOption = event.getOption("description");
        
        String category = categoryOption != null ? categoryOption.getAsString() : "general";
        String subject = subjectOption != null ? subjectOption.getAsString() : "Support anmodning";
        String description = descriptionOption != null ? descriptionOption.getAsString() : null;
        
        Guild guild = event.getGuild();
        User user = event.getUser();
        
        if (guild == null) {
            sendErrorMessage(event, translate("error.guild_only", user.getId()));
            return;
        }

        // Tjek om brugeren allerede har for mange åbne tickets
        List<Ticket> userTickets = ticketManager.getTicketService().getUserOpenTickets(user.getId(), guild.getId());
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        String userId = user.getId();
        
        if (userTickets.size() >= config.getMaxTicketsPerUser()) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(ERROR_EMOJI + " " + translate("ticket.create.error.max_tickets", userId))
                .setDescription(translate("ticket.create.error.max_tickets_desc", userId, userTickets.size(), config.getMaxTicketsPerUser()))
                .setColor(ERROR_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        // Opret ticket
        Optional<Ticket> ticketOpt = ticketManager.createTicket(guild, user, category, subject, description);
        
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ThreadChannel thread = guild.getThreadChannelById(ticket.getThreadId());
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " " + translate("ticket.create.title", userId))
                .setDescription(translate("ticket.create.description", userId, 
                    thread != null ? thread.getAsMention() : translate("ticket.create.error.thread_not_found", userId)))
                .addField(translate("ticket.create.id", userId), "`" + ticket.getTicketId() + "`", true)
                .addField(translate("ticket.create.category", userId), ticket.getCategory(), true)
                .addField(translate("ticket.create.subject", userId), ticket.getSubject(), false)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            sendErrorMessage(event, translate("ticket.create.error.failed", userId));
        }
    }

    /**
     * Håndterer lukning af ticket
     */
    private void handleCloseTicket(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String userId = user.getId();
        
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : translate("ticket.close.no_reason", userId);
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, translate("ticket.close.error.not_in_ticket", userId));
            return;
        }

        // Find ticket baseret på thread ID
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, translate("ticket.close.error.not_found", userId));
            return;
        }

        Ticket ticket = ticketOpt.get();
        Member member = event.getMember();
        
        // Tjek rettigheder - kun ticket ejer eller staff kan lukke
        if (!ticket.getUserId().equals(user.getId()) && 
            (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId()))) {
            sendErrorMessage(event, translate("ticket.close.error.no_permission", userId));
            return;
        }

        // Luk ticket
        if (ticketManager.closeTicket(ticket.getTicketId(), user, reason)) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " " + translate("ticket.close.title", userId))
                .setDescription(translate("ticket.close.description", userId))
                .addField(translate("ticket.close.closed_by", userId), user.getAsMention(), true)
                .addField(translate("ticket.close.reason", userId), reason, true)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            sendErrorMessage(event, translate("ticket.close.error.failed", userId));
        }
    }

    /**
     * Håndterer tildeling af ticket
     */
    private void handleAssignTicket(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String userId = user.getId();
        
        OptionMapping staffOption = event.getOption("staff");
        User staff = staffOption != null ? staffOption.getAsUser() : event.getUser();
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, translate("ticket.assign.error.not_in_ticket", userId));
            return;
        }

        // Tjek staff rettigheder
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            sendErrorMessage(event, translate("ticket.assign.error.no_permission", userId));
            return;
        }

        // Find ticket
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, translate("ticket.assign.error.not_found", userId));
            return;
        }

        Ticket ticket = ticketOpt.get();
        
        // Tildel ticket
        if (ticketManager.assignTicket(ticket.getTicketId(), staff)) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " " + translate("ticket.assign.title", userId))
                .setDescription(translate("ticket.assign.description", userId, staff.getAsMention()))
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            sendErrorMessage(event, translate("ticket.assign.error.failed", userId));
        }
    }

    /**
     * Håndterer ændring af prioritet
     */
    private void handleSetPriority(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String userId = user.getId();
        
        OptionMapping priorityOption = event.getOption("priority");
        if (priorityOption == null) {
            sendErrorMessage(event, translate("ticket.priority.error.no_priority", userId));
            return;
        }
        
        String priorityStr = priorityOption.getAsString();
        TicketPriority priority = TicketPriority.fromString(priorityStr);
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, translate("ticket.priority.error.not_in_ticket", userId));
            return;
        }

        // Tjek staff rettigheder
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            sendErrorMessage(event, translate("ticket.priority.error.no_permission", userId));
            return;
        }

        // Find ticket
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, translate("ticket.priority.error.not_found", userId));
            return;
        }

        Ticket ticket = ticketOpt.get();
        
        // Ændre prioritet
        if (ticketManager.setTicketPriority(ticket.getTicketId(), priority, user)) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " " + translate("ticket.priority.title", userId))
                .setDescription(translate("ticket.priority.description", userId, priority))
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            sendErrorMessage(event, translate("ticket.priority.error.failed", userId));
        }
    }

    /**
     * Håndterer liste over tickets
     */
    private void handleListTickets(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        OptionMapping statusOption = event.getOption("status");
        User user = event.getUser();
        String userId = user.getId();
        
        Guild guild = event.getGuild();
        if (guild == null) {
            sendErrorMessage(event, translate("error.guild_only", userId));
            return;
        }

        // Tjek rettigheder
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, guild.getId())) {
            sendErrorMessage(event, translate("ticket.list.error.no_permission", userId));
            return;
        }

        boolean includeClosed = statusOption != null && "all".equals(statusOption.getAsString());
        List<Ticket> tickets;
        
        if (userOption != null) {
            User targetUser = userOption.getAsUser();
            tickets = includeClosed ? 
                ticketManager.getTicketService().getGuildTickets(guild.getId(), true).stream()
                    .filter(t -> t.getUserId().equals(targetUser.getId()))
                    .toList() :
                ticketManager.getTicketService().getUserOpenTickets(targetUser.getId(), guild.getId());
        } else {
            tickets = ticketManager.getTicketService().getGuildTickets(guild.getId(), includeClosed);
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(TICKET_EMOJI + " " + translate("ticket.list.title", userId))
            .setColor(INFO_COLOR)
            .setTimestamp(Instant.now());

        if (tickets.isEmpty()) {
            embed.setDescription(translate("ticket.list.no_tickets", userId));
        } else {
            StringBuilder description = new StringBuilder();
            int count = 0;
            
            for (Ticket ticket : tickets) {
                if (count >= 10) {
                    description.append("\n").append(translate("ticket.list.more_tickets", userId, tickets.size() - count));
                    break;
                }
                
                User ticketUser = event.getJDA().getUserById(ticket.getUserId());
                String userName = ticketUser != null ? ticketUser.getName() : translate("ticket.list.unknown_user", userId);
                
                description.append(String.format(
                    "%s **%s** - %s\n" +
                    "└ %s | %s | <#%s>\n\n",
                    ticket.getStatusEmoji(),
                    ticket.getSubject(),
                    userName,
                    ticket.getPriority(),
                    ticket.getCategory(),
                    ticket.getThreadId()
                ));
                
                count++;
            }
            
            embed.setDescription(description.toString());
            embed.setFooter(translate("ticket.list.total", userId, tickets.size()));
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer ticket info
     */
    private void handleTicketInfo(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String userId = user.getId();
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, translate("ticket.info.error.not_in_ticket", userId));
            return;
        }

        // Find ticket
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, translate("ticket.info.error.not_found", userId));
            return;
        }

        Ticket ticket = ticketOpt.get();
        User ticketUser = event.getJDA().getUserById(ticket.getUserId());
        User assignedStaff = ticket.getAssignedStaffId() != null ? 
            event.getJDA().getUserById(ticket.getAssignedStaffId()) : null;
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(TICKET_EMOJI + " " + translate("ticket.info.title", userId))
            .addField(translate("ticket.info.id", userId), "`" + ticket.getTicketId() + "`", true)
            .addField(translate("ticket.info.status", userId), ticket.getStatus().toString(), true)
            .addField(translate("ticket.info.priority", userId), ticket.getPriority().toString(), true)
            .addField(translate("ticket.info.category", userId), ticket.getCategory(), true)
            .addField(translate("ticket.info.created_by", userId), ticketUser != null ? ticketUser.getAsMention() : translate("ticket.info.unknown", userId), true)
            .addField(translate("ticket.info.assigned_to", userId), assignedStaff != null ? assignedStaff.getAsMention() : translate("ticket.info.none", userId), true)
            .addField(translate("ticket.info.subject", userId), ticket.getSubject(), false)
            .setColor(INFO_COLOR)
            .setTimestamp(Instant.now())
            .setFooter(translate("ticket.info.created_at", userId, ticket.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        if (ticket.getDescription() != null && !ticket.getDescription().trim().isEmpty()) {
            embed.addField(translate("ticket.info.description", userId), ticket.getDescription(), false);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer ticket konfiguration kommandoer
     */
    private void handleTicketConfigCommand(SlashCommandInteractionEvent event, String subcommand) {
        User user = event.getUser();
        String userId = user.getId();
        
        // Tjek administrator rettigheder
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            sendErrorMessage(event, translate("config.error.admin_required", userId));
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            sendErrorMessage(event, translate("error.guild_only", userId));
            return;
        }

        switch (subcommand) {
            case "setup":
                handleSetupConfig(event, guild);
                break;
            case "view":
                handleViewConfig(event, guild);
                break;
            case "enable":
                handleToggleConfig(event, guild, true);
                break;
            case "disable":
                handleToggleConfig(event, guild, false);
                break;
            default:
                sendErrorMessage(event, translate("config.error.unknown_command", userId, subcommand));
        }
    }

    /**
     * Håndterer setup af ticket konfiguration
     */
    private void handleSetupConfig(SlashCommandInteractionEvent event, Guild guild) {
        OptionMapping categoryOption = event.getOption("category");
        OptionMapping staffRoleOption = event.getOption("staff_role");
        OptionMapping maxTicketsOption = event.getOption("max_tickets");
        User user = event.getUser();
        String userId = user.getId();
        
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        boolean updated = false;
        
        if (categoryOption != null) {
            Category category = categoryOption.getAsChannel().asCategory();
            config.setSupportCategoryId(category.getId());
            updated = true;
        }
        
        if (staffRoleOption != null) {
            Role staffRole = staffRoleOption.getAsRole();
            config.setStaffRoleId(staffRole.getId());
            updated = true;
        }
        
        if (maxTicketsOption != null) {
            int maxTickets = (int) maxTicketsOption.getAsLong();
            config.setMaxTicketsPerUser(maxTickets);
            updated = true;
        }
        
        if (updated) {
            if (ticketManager.getTicketService().saveTicketConfig(config)) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(SUCCESS_EMOJI + " " + translate("config.setup.title", userId))
                    .setDescription(translate("config.setup.description", userId))
                    .setColor(SUCCESS_COLOR)
                    .setTimestamp(Instant.now());
                
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            } else {
                sendErrorMessage(event, translate("config.setup.error.save_failed", userId));
            }
        } else {
            sendErrorMessage(event, translate("config.setup.error.no_changes", userId));
        }
    }

    /**
     * Håndterer visning af konfiguration
     */
    private void handleViewConfig(SlashCommandInteractionEvent event, Guild guild) {
        User user = event.getUser();
        String userId = user.getId();
        
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        Category category = config.getSupportCategoryId() != null ? 
            guild.getCategoryById(config.getSupportCategoryId()) : null;
        Role staffRole = config.getStaffRoleId() != null ? 
            guild.getRoleById(config.getStaffRoleId()) : null;
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(SETTINGS_EMOJI + " " + translate("config.view.title", userId))
            .addField(translate("config.view.status", userId), config.isEnabled() ? translate("config.view.enabled", userId) : translate("config.view.disabled", userId), true)
            .addField(translate("config.view.category", userId), category != null ? category.getName() : translate("config.view.not_set", userId), true)
            .addField(translate("config.view.staff_role", userId), staffRole != null ? staffRole.getAsMention() : translate("config.view.not_set", userId), true)
            .addField(translate("config.view.max_tickets", userId), String.valueOf(config.getMaxTicketsPerUser()), true)
            .addField(translate("config.view.auto_close", userId), String.valueOf(config.getAutoCloseInactiveHours()), true)
            .setColor(INFO_COLOR)
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer aktivering/deaktivering af ticket system
     */
    private void handleToggleConfig(SlashCommandInteractionEvent event, Guild guild, boolean enable) {
        User user = event.getUser();
        String userId = user.getId();
        
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        config.setEnabled(enable);
        
        if (ticketManager.getTicketService().saveTicketConfig(config)) {
            String titleKey = enable ? "config.toggle.enabled_title" : "config.toggle.disabled_title";
            String descKey = enable ? "config.toggle.enabled_desc" : "config.toggle.disabled_desc";
            
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " " + translate(titleKey, userId))
                .setDescription(translate(descKey, userId))
                .setColor(enable ? SUCCESS_COLOR : WARNING_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            sendErrorMessage(event, translate("config.toggle.error.save_failed", userId));
        }
    }

    /**
     * Sender fejlbesked
     */
    private void sendErrorMessage(SlashCommandInteractionEvent event, String message) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(ERROR_EMOJI + " Fejl")
            .setDescription(message)
            .setColor(ERROR_COLOR)
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer button interaktioner
     */
    public void handleButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        
        if (buttonId.startsWith("close_ticket_")) {
            String ticketId = buttonId.substring("close_ticket_".length());
            handleCloseTicketButton(event, ticketId);
        } else if (buttonId.startsWith("assign_ticket_")) {
            String ticketId = buttonId.substring("assign_ticket_".length());
            handleAssignTicketButton(event, ticketId);
        } else if (buttonId.startsWith("priority_ticket_")) {
            String ticketId = buttonId.substring("priority_ticket_".length());
            handlePriorityTicketButton(event, ticketId);
        }
    }

    /**
     * Håndterer luk ticket knap
     */
    private void handleCloseTicketButton(ButtonInteractionEvent event, String ticketId) {
        User user = event.getUser();
        String userId = user.getId();
        
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicket(ticketId);
        if (ticketOpt.isEmpty()) {
            event.reply(translate("ticket.close.error.not_found", userId)).setEphemeral(true).queue();
            return;
        }

        Ticket ticket = ticketOpt.get();
        Member member = event.getMember();
        
        // Tjek rettigheder
        if (!ticket.getUserId().equals(user.getId()) && 
            (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId()))) {
            event.reply(translate("ticket.close.error.no_permission", userId)).setEphemeral(true).queue();
            return;
        }

        if (ticketManager.closeTicket(ticketId, user, translate("ticket.close.button_reason", userId))) {
            event.reply(translate("ticket.close.success", userId)).setEphemeral(true).queue();
        } else {
            event.reply(translate("ticket.close.error.failed", userId)).setEphemeral(true).queue();
        }
    }

    /**
     * Håndterer tildel ticket knap
     */
    private void handleAssignTicketButton(ButtonInteractionEvent event, String ticketId) {
        User user = event.getUser();
        String userId = user.getId();
        Member member = event.getMember();
        
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            event.reply(translate("ticket.assign.error.no_permission", userId)).setEphemeral(true).queue();
            return;
        }

        if (ticketManager.assignTicket(ticketId, user)) {
            event.reply(translate("ticket.assign.button_success", userId)).setEphemeral(true).queue();
        } else {
            event.reply(translate("ticket.assign.error.failed", userId)).setEphemeral(true).queue();
        }
    }

    /**
     * Håndterer prioritet ticket knap
     */
    private void handlePriorityTicketButton(ButtonInteractionEvent event, String ticketId) {
        User user = event.getUser();
        String userId = user.getId();
        Member member = event.getMember();
        
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            event.reply(translate("ticket.priority.error.no_permission", userId)).setEphemeral(true).queue();
            return;
        }

        // Opret dropdown menu til prioritet valg
        StringSelectMenu priorityMenu = StringSelectMenu.create("priority_select_" + ticketId)
            .setPlaceholder(translate("ticket.priority.select_placeholder", userId))
            .addOption(translate("ticket.priority.low", userId), "low", "🟢 " + translate("ticket.priority.low_desc", userId))
            .addOption(translate("ticket.priority.medium", userId), "medium", "🟡 " + translate("ticket.priority.medium_desc", userId))
            .addOption(translate("ticket.priority.high", userId), "high", "🟠 " + translate("ticket.priority.high_desc", userId))
            .addOption(translate("ticket.priority.urgent", userId), "urgent", "🔴 " + translate("ticket.priority.urgent_desc", userId))
            .build();

        event.reply(translate("ticket.priority.select_prompt", userId))
            .addComponents(ActionRow.of(priorityMenu))
            .setEphemeral(true)
            .queue();
    }

    /**
     * Håndterer prioritet dropdown
     */
    public void handleStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        User user = event.getUser();
        String userId = user.getId();
        
        if (componentId.startsWith("priority_select_")) {
            String ticketId = componentId.substring("priority_select_".length());
            String priorityValue = event.getValues().get(0);
            TicketPriority priority = TicketPriority.fromString(priorityValue);
            
            if (ticketManager.setTicketPriority(ticketId, priority, user)) {
                event.reply(translate("ticket.priority.changed_success", userId, priority)).setEphemeral(true).queue();
            } else {
                event.reply(translate("ticket.priority.error.failed", userId)).setEphemeral(true).queue();
            }
        }
    }
}