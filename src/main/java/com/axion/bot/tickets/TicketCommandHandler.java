package com.axion.bot.tickets;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
    
    // Farver
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color INFO_COLOR = new Color(59, 130, 246);
    private static final Color WARNING_COLOR = new Color(251, 191, 36);
    
    // Emojis
    private static final String SUCCESS_EMOJI = "\u2705";
    private static final String ERROR_EMOJI = "\u274C";
    private static final String TICKET_EMOJI = "\uD83C\uDFAB";
    private static final String INFO_EMOJI = "\u2139\uFE0F";
    private static final String SETTINGS_EMOJI = "\u2699\uFE0F";
    
    public TicketCommandHandler(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
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
            sendErrorMessage(event, "Denne kommando kan kun bruges på en server!");
            return;
        }

        // Tjek om brugeren allerede har for mange åbne tickets
        List<Ticket> userTickets = ticketManager.getTicketService().getUserOpenTickets(user.getId(), guild.getId());
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        if (userTickets.size() >= config.getMaxTicketsPerUser()) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(ERROR_EMOJI + " For mange åbne tickets")
                .setDescription(String.format("Du har allerede %d åbne tickets. Maksimum er %d.", 
                    userTickets.size(), config.getMaxTicketsPerUser()))
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
                .setTitle(SUCCESS_EMOJI + " Ticket Oprettet")
                .setDescription(String.format("Din ticket er blevet oprettet! %s", 
                    thread != null ? thread.getAsMention() : "Thread ikke fundet"))
                .addField("Ticket ID", "`" + ticket.getTicketId() + "`", true)
                .addField("Kategori", ticket.getCategory(), true)
                .addField("Emne", ticket.getSubject(), false)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            sendErrorMessage(event, "Kunne ikke oprette ticket. Kontakt en administrator.");
        }
    }

    /**
     * Håndterer lukning af ticket
     */
    private void handleCloseTicket(SlashCommandInteractionEvent event) {
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Ingen årsag angivet";
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, "Denne kommando kan kun bruges i en ticket thread!");
            return;
        }

        // Find ticket baseret på thread ID
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, "Kunne ikke finde ticket for denne thread!");
            return;
        }

        Ticket ticket = ticketOpt.get();
        User user = event.getUser();
        Member member = event.getMember();
        
        // Tjek rettigheder - kun ticket ejer eller staff kan lukke
        if (!ticket.getUserId().equals(user.getId()) && 
            (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId()))) {
            sendErrorMessage(event, "Du har ikke tilladelse til at lukke denne ticket!");
            return;
        }

        // Luk ticket
        if (ticketManager.closeTicket(ticket.getTicketId(), user, reason)) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " Ticket Lukket")
                .setDescription("Ticketen er blevet lukket og vil blive arkiveret om kort tid.")
                .addField("Lukket af", user.getAsMention(), true)
                .addField("Årsag", reason, true)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            sendErrorMessage(event, "Kunne ikke lukke ticket. Prøv igen senere.");
        }
    }

    /**
     * Håndterer tildeling af ticket
     */
    private void handleAssignTicket(SlashCommandInteractionEvent event) {
        OptionMapping staffOption = event.getOption("staff");
        User staff = staffOption != null ? staffOption.getAsUser() : event.getUser();
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, "Denne kommando kan kun bruges i en ticket thread!");
            return;
        }

        // Tjek staff rettigheder
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            sendErrorMessage(event, "Du har ikke tilladelse til at tildele tickets!");
            return;
        }

        // Find ticket
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, "Kunne ikke finde ticket for denne thread!");
            return;
        }

        Ticket ticket = ticketOpt.get();
        
        // Tildel ticket
        if (ticketManager.assignTicket(ticket.getTicketId(), staff)) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " Ticket Tildelt")
                .setDescription(String.format("Ticket er blevet tildelt til %s", staff.getAsMention()))
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            sendErrorMessage(event, "Kunne ikke tildele ticket. Prøv igen senere.");
        }
    }

    /**
     * Håndterer ændring af prioritet
     */
    private void handleSetPriority(SlashCommandInteractionEvent event) {
        OptionMapping priorityOption = event.getOption("priority");
        if (priorityOption == null) {
            sendErrorMessage(event, "Du skal angive en prioritet!");
            return;
        }
        
        String priorityStr = priorityOption.getAsString();
        TicketPriority priority = TicketPriority.fromString(priorityStr);
        
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, "Denne kommando kan kun bruges i en ticket thread!");
            return;
        }

        // Tjek staff rettigheder
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            sendErrorMessage(event, "Du har ikke tilladelse til at ændre prioritet!");
            return;
        }

        // Find ticket
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, "Kunne ikke finde ticket for denne thread!");
            return;
        }

        Ticket ticket = ticketOpt.get();
        
        // Ændre prioritet
        if (ticketManager.setTicketPriority(ticket.getTicketId(), priority, event.getUser())) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " Prioritet Ændret")
                .setDescription(String.format("Ticket prioritet ændret til %s", priority))
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            sendErrorMessage(event, "Kunne ikke ændre prioritet. Prøv igen senere.");
        }
    }

    /**
     * Håndterer liste over tickets
     */
    private void handleListTickets(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        OptionMapping statusOption = event.getOption("status");
        
        Guild guild = event.getGuild();
        if (guild == null) {
            sendErrorMessage(event, "Denne kommando kan kun bruges på en server!");
            return;
        }

        // Tjek rettigheder
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, guild.getId())) {
            sendErrorMessage(event, "Du har ikke tilladelse til at se alle tickets!");
            return;
        }

        boolean includeClosed = statusOption != null && "all".equals(statusOption.getAsString());
        List<Ticket> tickets;
        
        if (userOption != null) {
            User user = userOption.getAsUser();
            tickets = includeClosed ? 
                ticketManager.getTicketService().getGuildTickets(guild.getId(), true).stream()
                    .filter(t -> t.getUserId().equals(user.getId()))
                    .toList() :
                ticketManager.getTicketService().getUserOpenTickets(user.getId(), guild.getId());
        } else {
            tickets = ticketManager.getTicketService().getGuildTickets(guild.getId(), includeClosed);
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(TICKET_EMOJI + " Ticket Oversigt")
            .setColor(INFO_COLOR)
            .setTimestamp(Instant.now());

        if (tickets.isEmpty()) {
            embed.setDescription("Ingen tickets fundet.");
        } else {
            StringBuilder description = new StringBuilder();
            int count = 0;
            
            for (Ticket ticket : tickets) {
                if (count >= 10) {
                    description.append("\n... og ").append(tickets.size() - count).append(" flere");
                    break;
                }
                
                User ticketUser = event.getJDA().getUserById(ticket.getUserId());
                String userName = ticketUser != null ? ticketUser.getName() : "Ukendt bruger";
                
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
            embed.setFooter(String.format("Total: %d tickets", tickets.size()));
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer ticket info
     */
    private void handleTicketInfo(SlashCommandInteractionEvent event) {
        // Tjek om kommandoen køres i en ticket thread
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            sendErrorMessage(event, "Denne kommando kan kun bruges i en ticket thread!");
            return;
        }

        // Find ticket
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicketByThreadId(thread.getId());
        if (ticketOpt.isEmpty()) {
            sendErrorMessage(event, "Kunne ikke finde ticket for denne thread!");
            return;
        }

        Ticket ticket = ticketOpt.get();
        User ticketUser = event.getJDA().getUserById(ticket.getUserId());
        User assignedStaff = ticket.getAssignedStaffId() != null ? 
            event.getJDA().getUserById(ticket.getAssignedStaffId()) : null;
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(TICKET_EMOJI + " Ticket Information")
            .addField("Ticket ID", "`" + ticket.getTicketId() + "`", true)
            .addField("Status", ticket.getStatus().toString(), true)
            .addField("Prioritet", ticket.getPriority().toString(), true)
            .addField("Kategori", ticket.getCategory(), true)
            .addField("Oprettet af", ticketUser != null ? ticketUser.getAsMention() : "Ukendt", true)
            .addField("Tildelt til", assignedStaff != null ? assignedStaff.getAsMention() : "Ingen", true)
            .addField("Emne", ticket.getSubject(), false)
            .setColor(INFO_COLOR)
            .setTimestamp(Instant.now())
            .setFooter("Oprettet " + ticket.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        if (ticket.getDescription() != null && !ticket.getDescription().trim().isEmpty()) {
            embed.addField("Beskrivelse", ticket.getDescription(), false);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer ticket konfiguration kommandoer
     */
    private void handleTicketConfigCommand(SlashCommandInteractionEvent event, String subcommand) {
        // Tjek administrator rettigheder
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            sendErrorMessage(event, "Du skal være administrator for at ændre ticket konfiguration!");
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            sendErrorMessage(event, "Denne kommando kan kun bruges på en server!");
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
                sendErrorMessage(event, "Ukendt konfiguration kommando: " + subcommand);
        }
    }

    /**
     * Håndterer setup af ticket konfiguration
     */
    private void handleSetupConfig(SlashCommandInteractionEvent event, Guild guild) {
        OptionMapping categoryOption = event.getOption("category");
        OptionMapping staffRoleOption = event.getOption("staff_role");
        OptionMapping maxTicketsOption = event.getOption("max_tickets");
        
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
                    .setTitle(SUCCESS_EMOJI + " Konfiguration Opdateret")
                    .setDescription("Ticket konfiguration er blevet opdateret.")
                    .setColor(SUCCESS_COLOR)
                    .setTimestamp(Instant.now());
                
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            } else {
                sendErrorMessage(event, "Kunne ikke gemme konfiguration. Prøv igen senere.");
            }
        } else {
            sendErrorMessage(event, "Ingen ændringer blev foretaget.");
        }
    }

    /**
     * Håndterer visning af konfiguration
     */
    private void handleViewConfig(SlashCommandInteractionEvent event, Guild guild) {
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        Category category = config.getSupportCategoryId() != null ? 
            guild.getCategoryById(config.getSupportCategoryId()) : null;
        Role staffRole = config.getStaffRoleId() != null ? 
            guild.getRoleById(config.getStaffRoleId()) : null;
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(SETTINGS_EMOJI + " Ticket Konfiguration")
            .addField("Status", config.isEnabled() ? "✅ Aktiveret" : "❌ Deaktiveret", true)
            .addField("Support Kategori", category != null ? category.getName() : "Ikke sat", true)
            .addField("Staff Rolle", staffRole != null ? staffRole.getAsMention() : "Ikke sat", true)
            .addField("Max Tickets per Bruger", String.valueOf(config.getMaxTicketsPerUser()), true)
            .addField("Auto-luk efter (timer)", String.valueOf(config.getAutoCloseInactiveHours()), true)
            .setColor(INFO_COLOR)
            .setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Håndterer aktivering/deaktivering af ticket system
     */
    private void handleToggleConfig(SlashCommandInteractionEvent event, Guild guild, boolean enable) {
        Optional<TicketConfig> configOpt = ticketManager.getTicketService().getTicketConfig(guild.getId());
        TicketConfig config = configOpt.orElse(TicketConfig.createDefault(guild.getId()));
        
        config.setEnabled(enable);
        
        if (ticketManager.getTicketService().saveTicketConfig(config)) {
            String status = enable ? "aktiveret" : "deaktiveret";
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(SUCCESS_EMOJI + " Ticket System " + status.substring(0, 1).toUpperCase() + status.substring(1))
                .setDescription("Ticket systemet er blevet " + status + ".")
                .setColor(enable ? SUCCESS_COLOR : WARNING_COLOR)
                .setTimestamp(Instant.now());
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            sendErrorMessage(event, "Kunne ikke ændre ticket system status. Prøv igen senere.");
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
        Optional<Ticket> ticketOpt = ticketManager.getTicketService().getTicket(ticketId);
        if (ticketOpt.isEmpty()) {
            event.reply("Ticket ikke fundet!").setEphemeral(true).queue();
            return;
        }

        Ticket ticket = ticketOpt.get();
        User user = event.getUser();
        Member member = event.getMember();
        
        // Tjek rettigheder
        if (!ticket.getUserId().equals(user.getId()) && 
            (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId()))) {
            event.reply("Du har ikke tilladelse til at lukke denne ticket!").setEphemeral(true).queue();
            return;
        }

        if (ticketManager.closeTicket(ticketId, user, "Lukket via knap")) {
            event.reply("Ticket lukket!").setEphemeral(true).queue();
        } else {
            event.reply("Kunne ikke lukke ticket!").setEphemeral(true).queue();
        }
    }

    /**
     * Håndterer tildel ticket knap
     */
    private void handleAssignTicketButton(ButtonInteractionEvent event, String ticketId) {
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            event.reply("Du har ikke tilladelse til at tildele tickets!").setEphemeral(true).queue();
            return;
        }

        if (ticketManager.assignTicket(ticketId, event.getUser())) {
            event.reply("Du er nu tildelt denne ticket!").setEphemeral(true).queue();
        } else {
            event.reply("Kunne ikke tildele ticket!").setEphemeral(true).queue();
        }
    }

    /**
     * Håndterer prioritet ticket knap
     */
    private void handlePriorityTicketButton(ButtonInteractionEvent event, String ticketId) {
        Member member = event.getMember();
        if (member == null || !ticketManager.hasStaffPermissions(member, event.getGuild().getId())) {
            event.reply("Du har ikke tilladelse til at ændre prioritet!").setEphemeral(true).queue();
            return;
        }

        // Opret dropdown menu til prioritet valg
        StringSelectMenu priorityMenu = StringSelectMenu.create("priority_select_" + ticketId)
            .setPlaceholder("Vælg ny prioritet")
            .addOption("Lav", "low", "🟢 Lav prioritet")
            .addOption("Medium", "medium", "🟡 Medium prioritet")
            .addOption("Høj", "high", "🟠 Høj prioritet")
            .addOption("Akut", "urgent", "🔴 Akut prioritet")
            .build();

        event.reply("Vælg ny prioritet:")
            .addComponents(ActionRow.of(priorityMenu))
            .setEphemeral(true)
            .queue();
    }

    /**
     * Håndterer prioritet dropdown
     */
    public void handleStringSelectInteraction(StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        
        if (componentId.startsWith("priority_select_")) {
            String ticketId = componentId.substring("priority_select_".length());
            String priorityValue = event.getValues().get(0);
            TicketPriority priority = TicketPriority.fromString(priorityValue);
            
            if (ticketManager.setTicketPriority(ticketId, priority, event.getUser())) {
                event.reply("Prioritet ændret til " + priority + "!").setEphemeral(true).queue();
            } else {
                event.reply("Kunne ikke ændre prioritet!").setEphemeral(true).queue();
            }
        }
    }
}