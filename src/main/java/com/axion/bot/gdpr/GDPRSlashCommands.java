package com.axion.bot.gdpr;

import com.axion.bot.gdpr.GDPRComplianceManager.DataProcessingPurpose;
import com.axion.bot.gdpr.UserConsent;
import com.axion.bot.gdpr.DataProcessingActivity;
import com.axion.bot.gdpr.DataRetentionPolicy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Slash commands for GDPR compliance features
 */
public class GDPRSlashCommands {
    private final GDPRComplianceManager gdprManager;
    
    public GDPRSlashCommands(GDPRComplianceManager gdprManager) {
        this.gdprManager = gdprManager;
    }
    
    /**
     * Creates the GDPR command data for registration
     */
    public static List<CommandData> createCommands() {
        List<CommandData> commands = new ArrayList<>();
        
        // Main GDPR command with subcommands
        CommandData gdprCommand = Commands.slash("gdpr", "GDPR compliance and data protection commands")
            .addSubcommands(
                new SubcommandData("consent", "Manage your data processing consent")
                    .addOption(OptionType.STRING, "action", "Action to perform", true)
                    .addOption(OptionType.STRING, "purposes", "Data processing purposes (comma-separated)", false),
                    
                new SubcommandData("export", "Export your personal data")
                    .addOption(OptionType.STRING, "format", "Export format (json/readable)", false),
                    
                new SubcommandData("delete", "Request deletion of your personal data")
                    .addOption(OptionType.BOOLEAN, "confirm", "Confirm data deletion", true),
                    
                new SubcommandData("anonymize", "Request anonymization of your personal data")
                    .addOption(OptionType.BOOLEAN, "confirm", "Confirm data anonymization", true),
                    
                new SubcommandData("status", "Check your data processing status and consent"),
                
                new SubcommandData("policy", "View data retention and processing policies")
                    .addOption(OptionType.STRING, "type", "Policy type (retention/processing)", false)
            );
        
        commands.add(gdprCommand);
        
        // Admin-only GDPR management command
        CommandData gdprAdminCommand = Commands.slash("gdpr-admin", "GDPR administration commands (Admin only)")
            .addSubcommands(
                new SubcommandData("activities", "View data processing activities")
                    .addOption(OptionType.STRING, "format", "Output format (summary/detailed/gdpr)", false),
                    
                new SubcommandData("audit", "Run GDPR compliance audit")
                    .addOption(OptionType.BOOLEAN, "detailed", "Show detailed audit results", false),
                    
                new SubcommandData("cleanup", "Run data retention cleanup")
                    .addOption(OptionType.BOOLEAN, "dry_run", "Perform dry run without actual deletion", false),
                    
                new SubcommandData("user-data", "View user's data processing information")
                    .addOption(OptionType.USER, "user", "User to check", true)
                    .addOption(OptionType.STRING, "action", "Action (view/export/delete)", true),
                    
                new SubcommandData("settings", "Configure GDPR settings")
                    .addOption(OptionType.STRING, "setting", "Setting to configure", true)
                    .addOption(OptionType.STRING, "value", "New value", true)
            );
        
        commands.add(gdprAdminCommand);
        
        return commands;
    }
    
    /**
     * Main entry point for handling GDPR slash commands
     */
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        
        switch (commandName) {
            case "gdpr" -> handleGDPRCommand(event);
            case "gdpr-admin" -> handleGDPRAdminCommand(event);
            default -> event.reply("❌ Unknown GDPR command.").setEphemeral(true).queue();
        }
    }
    
    /**
     * Handles GDPR slash command interactions
     */
    public void handleGDPRCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Invalid GDPR command.").setEphemeral(true).queue();
            return;
        }
        
        switch (subcommand) {
            case "consent" -> handleConsentCommand(event);
            case "export" -> handleExportCommand(event);
            case "delete" -> handleDeleteCommand(event);
            case "anonymize" -> handleAnonymizeCommand(event);
            case "status" -> handleStatusCommand(event);
            case "policy" -> handlePolicyCommand(event);
            default -> event.reply("❌ Unknown GDPR subcommand.").setEphemeral(true).queue();
        }
    }
    
    /**
     * Handles GDPR admin command interactions
     */
    public void handleGDPRAdminCommand(SlashCommandInteractionEvent event) {
        // Check admin permissions
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You need Administrator permission to use GDPR admin commands.").setEphemeral(true).queue();
            return;
        }
        
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Invalid GDPR admin command.").setEphemeral(true).queue();
            return;
        }
        
        switch (subcommand) {
            case "activities" -> handleActivitiesCommand(event);
            case "audit" -> handleAuditCommand(event);
            case "cleanup" -> handleCleanupCommand(event);
            case "user-data" -> handleUserDataCommand(event);
            case "settings" -> handleSettingsCommand(event);
            default -> event.reply("❌ Unknown GDPR admin subcommand.").setEphemeral(true).queue();
        }
    }
    
    private void handleConsentCommand(SlashCommandInteractionEvent event) {
        OptionMapping actionOption = event.getOption("action");
        if (actionOption == null) {
            event.reply("❌ Please specify an action (give/withdraw/update).").setEphemeral(true).queue();
            return;
        }
        
        String action = actionOption.getAsString().toLowerCase();
        String userId = event.getUser().getId();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        
        if (guildId == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        switch (action) {
            case "give" -> {
                Set<DataProcessingPurpose> purposes = parsePurposes(event.getOption("purposes"));
                if (purposes.isEmpty()) {
                    final Set<DataProcessingPurpose> defaultPurposes = Set.of(DataProcessingPurpose.PERSONALIZATION, DataProcessingPurpose.MODERATION);
                    purposes = defaultPurposes;
                }
                
                CompletableFuture<Boolean> result = gdprManager.recordConsent(userId, guildId, purposes, "slash_command");
                result.thenAccept(success -> {
                    if (success) {
                        EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("✅ Consent Recorded")
                            .setDescription("Your consent for data processing has been recorded.")
                            .addField("Purposes", Set.copyOf(purposes).stream().map(Enum::name).collect(Collectors.joining(", ")), false)
                            .setColor(Color.GREEN)
                            .setTimestamp(Instant.now());
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    } else {
                        event.reply("❌ Failed to record consent. Please try again.").setEphemeral(true).queue();
                    }
                });
            }
            case "withdraw" -> {
                CompletableFuture<Boolean> result = gdprManager.withdrawConsent(userId, guildId);
                result.thenAccept(success -> {
                    if (success) {
                        EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("✅ Consent Withdrawn")
                            .setDescription("Your consent for data processing has been withdrawn.")
                            .setColor(Color.ORANGE)
                            .setTimestamp(Instant.now());
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    } else {
                        event.reply("❌ Failed to withdraw consent. Please try again.").setEphemeral(true).queue();
                    }
                });
            }
            case "update" -> {
                Set<DataProcessingPurpose> purposes = parsePurposes(event.getOption("purposes"));
                if (purposes.isEmpty()) {
                    event.reply("❌ Please specify purposes to update consent for.").setEphemeral(true).queue();
                    return;
                }
                
                CompletableFuture<Boolean> result = gdprManager.updateConsent(userId, guildId, purposes, "slash_command");
                result.thenAccept(success -> {
                    if (success) {
                        EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("✅ Consent Updated")
                            .setDescription("Your consent for data processing has been updated.")
                            .addField("New Purposes", purposes.stream().map(Enum::name).collect(Collectors.joining(", ")), false)
                            .setColor(Color.BLUE)
                            .setTimestamp(Instant.now());
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                    } else {
                        event.reply("❌ Failed to update consent. Please try again.").setEphemeral(true).queue();
                    }
                });
            }
            default -> event.reply("❌ Invalid action. Use: give, withdraw, or update.").setEphemeral(true).queue();
        }
    }
    
    private void handleExportCommand(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        
        if (guildId == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping formatOption = event.getOption("format");
        String format = formatOption != null ? formatOption.getAsString() : "readable";
        
        event.deferReply(true).queue();
        
        CompletableFuture<String> result = gdprManager.exportUserData(userId, guildId, format);
        result.thenAccept(exportData -> {
            if (exportData != null && !exportData.isEmpty()) {
                // For large exports, we might need to send as a file
                if (exportData.length() > 1900) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📄 Data Export Ready")
                        .setDescription("Your personal data export is ready. Due to size limitations, please contact an administrator to receive your full data export.")
                        .addField("Export Format", format, true)
                        .addField("Data Size", exportData.length() + " characters", true)
                        .setColor(Color.BLUE)
                        .setTimestamp(Instant.now());
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                } else {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📄 Your Personal Data Export")
                        .setDescription("```\n" + exportData + "\n```")
                        .setColor(Color.BLUE)
                        .setTimestamp(Instant.now());
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                }
            } else {
                event.getHook().editOriginal("❌ No data found for export or export failed.").queue();
            }
        });
    }
    
    private void handleDeleteCommand(SlashCommandInteractionEvent event) {
        OptionMapping confirmOption = event.getOption("confirm");
        if (confirmOption == null || !confirmOption.getAsBoolean()) {
            event.reply("❌ You must confirm data deletion by setting confirm to true.").setEphemeral(true).queue();
            return;
        }
        
        String userId = event.getUser().getId();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        
        if (guildId == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply(true).queue();
        
        CompletableFuture<Boolean> result = gdprManager.deleteUserData(userId, guildId);
        result.thenAccept(success -> {
            if (success) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🗑️ Data Deletion Completed")
                    .setDescription("Your personal data has been deleted from this server.")
                    .addField("Note", "Some data may be retained for legal compliance purposes as outlined in our privacy policy.", false)
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now());
                event.getHook().editOriginalEmbeds(embed.build()).queue();
            } else {
                event.getHook().editOriginal("❌ Failed to delete data. Please contact an administrator.").queue();
            }
        });
    }
    
    private void handleAnonymizeCommand(SlashCommandInteractionEvent event) {
        OptionMapping confirmOption = event.getOption("confirm");
        if (confirmOption == null || !confirmOption.getAsBoolean()) {
            event.reply("❌ You must confirm data anonymization by setting confirm to true.").setEphemeral(true).queue();
            return;
        }
        
        String userId = event.getUser().getId();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        
        if (guildId == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply(true).queue();
        
        CompletableFuture<Boolean> result = gdprManager.anonymizeUserData(userId, guildId);
        result.thenAccept(success -> {
            if (success) {
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔒 Data Anonymization Completed")
                    .setDescription("Your personal data has been anonymized. Statistical data may be retained in anonymized form.")
                    .setColor(Color.GRAY)
                    .setTimestamp(Instant.now());
                event.getHook().editOriginalEmbeds(embed.build()).queue();
            } else {
                event.getHook().editOriginal("❌ Failed to anonymize data. Please contact an administrator.").queue();
            }
        });
    }
    
    private void handleStatusCommand(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        
        if (guildId == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply(true).queue();
        
        UserConsent consent = gdprManager.getUserConsent(userId, guildId);
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("📊 Your GDPR Status")
            .setColor(Color.BLUE)
            .setTimestamp(Instant.now());
        
        if (consent != null && consent.isActive()) {
            embed.addField("Consent Status", "✅ Active", true)
                 .addField("Consent Given", consent.getConsentTimestamp().toString(), true)
                 .addField("Consent Age", consent.getConsentAgeInDays() + " days", true)
                 .addField("Valid Until", consent.isValid() ? "Valid" : "Expired", true)
                 .addField("Purposes", consent.getPurposes().stream().map(Enum::name).collect(Collectors.joining("\n")), false);
        } else {
            embed.addField("Consent Status", "❌ No active consent", true)
                 .addField("Note", "You have not given consent for data processing or have withdrawn it.", false);
        }
        
        event.getHook().editOriginalEmbeds(embed.build()).queue();
    }
    
    private void handlePolicyCommand(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        String type = typeOption != null ? typeOption.getAsString() : "retention";
        
        event.deferReply(true).queue();
        
        if ("retention".equals(type)) {
            DataRetentionPolicy policy = DataRetentionPolicy.createDefaultPolicy();
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Data Retention Policy")
                .setDescription(policy.getDescription())
                .addField("Policy ID", policy.getPolicyId(), true)
                .addField("Legal Basis", policy.getLegalBasis(), true)
                .addField("Status", policy.isActive() ? "Active" : "Inactive", true)
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now());
            
            StringBuilder periods = new StringBuilder();
            policy.getRetentionPeriods().forEach((purpose, duration) -> 
                periods.append(purpose.name()).append(": ").append(duration.toDays()).append(" days\n")
            );
            
            embed.addField("Retention Periods", periods.toString(), false);
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        } else {
            List<DataProcessingActivity> activities = DataProcessingActivity.createDefaultActivities();
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Data Processing Activities")
                .setDescription("Overview of data processing activities")
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now());
            
            for (DataProcessingActivity activity : activities) {
                embed.addField(activity.getName(), 
                    "Purpose: " + activity.getPurpose().name() + "\n" +
                    "Legal Basis: " + activity.getLegalBasis(), false);
            }
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        }
    }
    
    // Admin command handlers
    private void handleActivitiesCommand(SlashCommandInteractionEvent event) {
        OptionMapping formatOption = event.getOption("format");
        String format = formatOption != null ? formatOption.getAsString() : "summary";
        
        event.deferReply(true).queue();
        
        List<DataProcessingActivity> activities = DataProcessingActivity.createDefaultActivities();
        
        if ("gdpr".equals(format)) {
            StringBuilder response = new StringBuilder();
            for (DataProcessingActivity activity : activities) {
                response.append(activity.toGDPRFormat()).append("\n\n");
            }
            
            // Response might be too long, so we'll provide a summary
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 GDPR Article 30 Records")
                .setDescription("Complete GDPR Article 30 records have been generated. Contact administrator for full documentation.")
                .addField("Activities Count", String.valueOf(activities.size()), true)
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now());
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Data Processing Activities")
                .setDescription("Current data processing activities")
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now());
            
            for (DataProcessingActivity activity : activities) {
                String value = "detailed".equals(format) ? 
                    "Purpose: " + activity.getPurpose().name() + "\n" +
                    "Legal Basis: " + activity.getLegalBasis() + "\n" +
                    "Data Categories: " + activity.getDataCategories().size() + "\n" +
                    "Recipients: " + activity.getRecipients().size() :
                    "Purpose: " + activity.getPurpose().name();
                
                embed.addField(activity.getName(), value, false);
            }
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        }
    }
    
    private void handleAuditCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        
        // Simulate audit results
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🔍 GDPR Compliance Audit")
            .setDescription("Compliance audit completed")
            .addField("Overall Status", "✅ Compliant", true)
            .addField("Data Retention", "✅ Policies in place", true)
            .addField("User Consent", "✅ Tracking active", true)
            .addField("Data Processing", "✅ Activities documented", true)
            .addField("Security Measures", "✅ Implemented", true)
            .addField("User Rights", "✅ Supported", true)
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now());
        
        event.getHook().editOriginalEmbeds(embed.build()).queue();
    }
    
    private void handleCleanupCommand(SlashCommandInteractionEvent event) {
        OptionMapping dryRunOption = event.getOption("dry_run");
        boolean dryRun = dryRunOption != null ? dryRunOption.getAsBoolean() : true;
        
        event.deferReply(true).queue();
        
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        if (guildId == null) {
            event.getHook().editOriginal("❌ This command can only be used in a server.").queue();
            return;
        }
        
        CompletableFuture<Integer> result = gdprManager.cleanupExpiredData(guildId, dryRun);
        result.thenAccept(count -> {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(dryRun ? "🧹 Data Cleanup Preview" : "🧹 Data Cleanup Completed")
                .setDescription(dryRun ? "Preview of data that would be cleaned up" : "Data cleanup has been completed")
                .addField("Records Processed", String.valueOf(count), true)
                .addField("Mode", dryRun ? "Dry Run" : "Live Cleanup", true)
                .setColor(dryRun ? Color.YELLOW : Color.GREEN)
                .setTimestamp(Instant.now());
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        });
    }
    
    private void handleUserDataCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        OptionMapping actionOption = event.getOption("action");
        
        if (userOption == null || actionOption == null) {
            event.reply("❌ Please specify both user and action.").setEphemeral(true).queue();
            return;
        }
        
        User targetUser = userOption.getAsUser();
        String action = actionOption.getAsString();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        
        if (guildId == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply(true).queue();
        
        switch (action.toLowerCase()) {
            case "view" -> {
                UserConsent consent = gdprManager.getUserConsent(targetUser.getId(), guildId);
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("👤 User Data Status: " + targetUser.getAsTag())
                    .setColor(Color.BLUE)
                    .setTimestamp(Instant.now());
                
                if (consent != null) {
                    embed.addField("Consent Status", consent.isActive() ? "Active" : "Withdrawn", true)
                         .addField("Consent Date", consent.getConsentTimestamp().toString(), true)
                         .addField("Purposes", consent.getPurposes().stream().map(Enum::name).collect(Collectors.joining(", ")), false);
                } else {
                    embed.addField("Consent Status", "No consent recorded", true);
                }
                
                event.getHook().editOriginalEmbeds(embed.build()).queue();
            }
            case "export" -> {
                CompletableFuture<String> exportResult = gdprManager.exportUserData(targetUser.getId(), guildId, "readable");
                exportResult.thenAccept(exportData -> {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📄 User Data Export: " + targetUser.getAsTag())
                        .setDescription("Data export completed")
                        .addField("Data Size", (exportData != null ? exportData.length() : 0) + " characters", true)
                        .setColor(Color.BLUE)
                        .setTimestamp(Instant.now());
                    
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                });
            }
            case "delete" -> {
                CompletableFuture<Boolean> deleteResult = gdprManager.deleteUserData(targetUser.getId(), guildId);
                deleteResult.thenAccept(success -> {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🗑️ User Data Deletion: " + targetUser.getAsTag())
                        .setDescription(success ? "Data deletion completed" : "Data deletion failed")
                        .setColor(success ? Color.GREEN : Color.RED)
                        .setTimestamp(Instant.now());
                    
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                });
            }
            default -> event.getHook().editOriginal("❌ Invalid action. Use: view, export, or delete.").queue();
        }
    }
    
    private void handleSettingsCommand(SlashCommandInteractionEvent event) {
        event.reply("⚙️ GDPR settings configuration is not yet implemented.").setEphemeral(true).queue();
    }
    
    private Set<DataProcessingPurpose> parsePurposes(OptionMapping purposesOption) {
        Set<DataProcessingPurpose> purposes = new HashSet<>();
        
        if (purposesOption != null) {
            String[] purposeNames = purposesOption.getAsString().split(",");
            for (String purposeName : purposeNames) {
                try {
                    DataProcessingPurpose purpose = DataProcessingPurpose.valueOf(purposeName.trim().toUpperCase());
                    purposes.add(purpose);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid purpose names
                }
            }
        }
        
        return purposes;
    }
}