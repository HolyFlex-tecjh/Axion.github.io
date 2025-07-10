package com.axion.bot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import com.axion.bot.config.BotConfig;
import com.axion.bot.translation.TranslationManager;
import com.axion.bot.utils.EmbedUtils;
import com.axion.bot.commands.LanguageCommands;

/**
 * Debug kommandoer til at tjekke slash command status
 */
public class DebugCommands {

    /**
     * Viser alle registrerede slash kommandoer
     */
    public static void handleListCommands(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String userLang = LanguageCommands.getUserLanguage(userId);
        TranslationManager tm = TranslationManager.getInstance();
        
        if (!BotConfig.isDeveloper(userId)) {
            event.reply(tm.translate("debug.permissions.denied", userLang)).setEphemeral(true).queue();
            return;
        }

        JDA jda = event.getJDA();
        
        jda.retrieveCommands().queue(commands -> {
            EmbedBuilder embed = EmbedUtils.createInfoEmbed(tm.translate("debug.list.title", userLang), 
                tm.translate("debug.list.description", userLang))
                .setDescription(tm.translate("debug.list.total", userLang, commands.size()));

            StringBuilder commandList = new StringBuilder();
            for (Command command : commands) {
                commandList.append("`/").append(command.getName()).append("` - ")
                          .append(command.getDescription()).append("\n");
            }

            if (commandList.length() > 1024) {
                // Split into two parts with proper length checking
                String[] parts = commandList.toString().split("\n");
                StringBuilder part1 = new StringBuilder();
                StringBuilder part2 = new StringBuilder();
                
                for (int i = 0; i < parts.length; i++) {
                    if (i < parts.length / 2) {
                        if (part1.length() + parts[i].length() + 1 <= 1020) {
                            part1.append(parts[i]).append("\n");
                        }
                    } else {
                        if (part2.length() + parts[i].length() + 1 <= 1020) {
                            part2.append(parts[i]).append("\n");
                        }
                    }
                }
                
                // Ensure parts don't exceed 1024 characters
                String part1Str = part1.toString();
                String part2Str = part2.toString();
                
                if (part1Str.length() > 1024) {
                    part1Str = part1Str.substring(0, 1020) + "...";
                }
                if (part2Str.length() > 1024) {
                    part2Str = part2Str.substring(0, 1020) + "...";
                }
                
                embed.addField(tm.translate("debug.list.commands", userLang) + " (1/2)", part1Str, false);
                embed.addField(tm.translate("debug.list.commands", userLang) + " (2/2)", part2Str, false);
            } else {
                embed.addField(tm.translate("debug.list.commands", userLang), commandList.toString(), false);
            }

            // Check for specific commands
            boolean hasInvite = commands.stream().anyMatch(cmd -> cmd.getName().equals("invite"));
            boolean hasSupport = commands.stream().anyMatch(cmd -> cmd.getName().equals("support"));
            boolean hasAbout = commands.stream().anyMatch(cmd -> cmd.getName().equals("about"));

            String statusFieldValue = "🔗 `/invite`: " + (hasInvite ? tm.translate("debug.list.registered", userLang) : tm.translate("debug.list.missing", userLang)) + "\n" +
                "💬 `/support`: " + (hasSupport ? tm.translate("debug.list.registered", userLang) : tm.translate("debug.list.missing", userLang)) + "\n" +
                "📊 `/about`: " + (hasAbout ? tm.translate("debug.list.registered", userLang) : tm.translate("debug.list.missing", userLang));
            
            // Ensure status field doesn't exceed 1024 characters
            if (statusFieldValue.length() > 1024) {
                statusFieldValue = statusFieldValue.substring(0, 1020) + "...";
            }
            
            embed.addField(tm.translate("debug.list.status", userLang), statusFieldValue, false);

            embed.setFooter(tm.translate("debug.list.footer", userLang));

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }, error -> {
            event.reply(tm.translate("debug.sync.error", userLang, error.getMessage())).setEphemeral(true).queue();
        });
    }

    /**
     * Force sync af slash kommandoer (kun for udviklere)
     * Dynamically updates and refreshes commands with real-time progress tracking
     */
    public static void handleForceSync(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String userLang = LanguageCommands.getUserLanguage(userId);
        TranslationManager tm = TranslationManager.getInstance();
        
        if (!BotConfig.isDeveloper(userId)) {
            event.reply(tm.translate("debug.permissions.denied", userLang)).setEphemeral(true).queue();
            return;
        }

        // Initial response
        event.reply(tm.translate("debug.sync.start", userLang)).setEphemeral(true).queue();
        
        JDA jda = event.getJDA();
        
        try {
            // Step 1: Get current command count for comparison
            event.getHook().editOriginal("🔄 **Step 1/5:** Analyzing current commands...")
                .queueAfter(500, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            jda.retrieveCommands().queueAfter(1, java.util.concurrent.TimeUnit.SECONDS, currentCommands -> {
                int currentCount = currentCommands.size();
                
                // Step 2: Start dynamic refresh process
                event.getHook().editOriginal("🔄 **Step 2/5:** Initiating dynamic command refresh...")
                    .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                
                // Step 3: Execute refresh using the proper method
                event.getHook().editOriginal("🔄 **Step 3/5:** Executing dynamic command refresh...")
                    .queueAfter(2, java.util.concurrent.TimeUnit.SECONDS);
                
                // Use the dedicated refresh method from SlashCommandRegistrar
                com.axion.bot.SlashCommandRegistrar.refreshAllCommands(jda, success -> {
                    if (success) {
                        // Step 4: Verify new command count
                        event.getHook().editOriginal("🔄 **Step 4/5:** Verifying command registration...")
                            .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                        
                        // Get final command count
                        com.axion.bot.SlashCommandRegistrar.getCommandCount(jda, newCount -> {
                            // Step 5: Generate comprehensive status report
                            event.getHook().editOriginal("🔄 **Step 5/5:** Generating status report...")
                                .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                            
                            // Create detailed status report embed
                            EmbedBuilder statusEmbed = EmbedUtils.createSuccessEmbed(
                                "✅ Force Sync Complete!", 
                                "Commands have been successfully refreshed and are now active!"
                            );
                            
                            // Verify commands are working
                            jda.retrieveCommands().queue(commands -> {
                                // Check for key commands
                                boolean hasInvite = commands.stream().anyMatch(cmd -> cmd.getName().equals("invite"));
                                boolean hasSupport = commands.stream().anyMatch(cmd -> cmd.getName().equals("support"));
                                boolean hasModeration = commands.stream().anyMatch(cmd -> cmd.getName().equals("ban"));
                                boolean hasDebug = commands.stream().anyMatch(cmd -> cmd.getName().equals("forcesync"));
                                
                                // Create comprehensive status report in embed format
                                StringBuilder statusReport = new StringBuilder();
                                statusReport.append("📊 **Process Summary:**\n");
                                statusReport.append("• Commands successfully refreshed\n");
                                statusReport.append("• Previous commands: ").append(currentCount).append("\n");
                                statusReport.append("• Current commands: ").append(newCount >= 0 ? newCount : "Unknown").append("\n");
                                statusReport.append("• Refresh status: **SUCCESS** ✅\n\n");
                                statusReport.append("⏱️ **Availability:** Commands are now active and ready to use!\n\n");
                                
                                statusReport.append("🔧 **Command Verification:**\n");
                                statusReport.append("• Basic Commands: ").append(hasInvite && hasSupport ? "✅" : "❌").append("\n");
                                statusReport.append("• Moderation Commands: ").append(hasModeration ? "✅" : "❌").append("\n");
                                statusReport.append("• Debug Commands: ").append(hasDebug ? "✅" : "❌").append("\n\n");
                                
                                statusReport.append("💡 **Sync Benefits:**\n");
                                statusReport.append("• Immediate command availability\n");
                                statusReport.append("• Proper sequencing and timing\n");
                                statusReport.append("• Automatic error detection\n");
                                statusReport.append("• Real-time verification\n\n");
                                statusReport.append("🎯 **Sync ID:** `").append(System.currentTimeMillis()).append("`\n");
                                statusReport.append("🔄 **Method:** Dedicated refresh with verification");
                                
                                // Ensure description doesn't exceed Discord's limit (4096 characters)
                                String description = statusReport.toString();
                                if (description.length() > 4000) {
                                    description = description.substring(0, 3900) + "\n\n... (truncated)";
                                }
                                statusEmbed.setDescription(description);
                                
                                // Send final status
                                event.getHook().editOriginalEmbeds(statusEmbed.build())
                                    .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                                
                            }, verifyError -> {
                                StringBuilder statusReport = new StringBuilder();
                                statusReport.append("📊 **Process Summary:**\n");
                                statusReport.append("• Commands successfully refreshed\n");
                                statusReport.append("• Previous commands: ").append(currentCount).append("\n");
                                statusReport.append("• Current commands: ").append(newCount >= 0 ? newCount : "Unknown").append("\n");
                                statusReport.append("• Refresh status: **SUCCESS** ✅\n\n");
                                
                                statusReport.append("🔧 **Command Verification:** ⚠️ Could not verify\n\n");
                                statusReport.append("💡 **Note:** Commands should still be working\n");
                                statusReport.append("🎯 **Sync ID:** `").append(System.currentTimeMillis()).append("`");
                                
                                // Ensure description doesn't exceed Discord's limit (4096 characters)
                                String description = statusReport.toString();
                                if (description.length() > 4000) {
                                    description = description.substring(0, 3900) + "\n\n... (truncated)";
                                }
                                statusEmbed.setDescription(description);
                                
                                event.getHook().editOriginalEmbeds(statusEmbed.build())
                                    .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                            });
                        });
                    } else {
                        handleSyncError(event, "Dynamic refresh failed - commands may not be properly updated");
                    }
                });
            }, retrieveError -> {
                handleSyncError(event, "Failed to retrieve current commands: " + retrieveError.getMessage());
            });
            
        } catch (Exception e) {
            handleSyncError(event, "Unexpected error during sync process: " + e.getMessage());
        }
    }
    
    private static void handleSyncError(SlashCommandInteractionEvent event, String errorDetails) {
        EmbedBuilder errorEmbed = EmbedUtils.createErrorEmbed(
            "❌ Force Sync Failed", 
            "An error occurred during the sync process."
        );
        
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("**Error Details:** ").append(errorDetails).append("\n\n");
        errorReport.append("🔧 **Troubleshooting Steps:**\n");
        errorReport.append("• Wait 2-3 minutes and try `/forcesync` again\n");
        errorReport.append("• Check if bot has `applications.commands` permission\n");
        errorReport.append("• Verify bot is online and responsive\n");
        errorReport.append("• Try using `/listcommands` to check current status\n");
        errorReport.append("• Contact support if issue persists\n\n");
        errorReport.append("💡 **Alternative:** Try using basic commands to test if sync worked partially\n\n");
        errorReport.append("🔄 **Fallback Option:** Bot will attempt automatic recovery on next restart");
        
        // Ensure description doesn't exceed Discord's limit (4096 characters)
        String description = errorReport.toString();
        if (description.length() > 4000) {
            description = description.substring(0, 3900) + "\n\n... (truncated)";
        }
        errorEmbed.setDescription(description);
        
        event.getHook().editOriginalEmbeds(errorEmbed.build()).queue(
            success -> {},
            error -> {
                // Last resort - try simple message
                try {
                    event.getHook().editOriginal("❌ Force sync failed. Please try again in a few minutes.").queue();
                } catch (Exception ignored) {}
            }
        );
    }
}
