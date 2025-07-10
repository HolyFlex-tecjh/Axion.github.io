package com.axion.bot;

import com.axion.bot.moderation.*;
import com.axion.bot.database.DatabaseService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Håndterer auto-moderation af beskeder
 * Denne klasse håndterer IKKE kommandoer - kun slash commands (/) bruges i Axion Bot
 */
public class CommandHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    
    // Moderation system
    private final ModerationManager moderationManager;

    public CommandHandler(DatabaseService databaseService) {
        // Initialiser moderation system med standard konfiguration
        ModerationConfig config = ModerationConfig.createDefault();
        this.moderationManager = new ModerationManager(config, databaseService);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignorer beskeder fra botter
        if (event.getAuthor().isBot()) {
            return;
        }

        // Kør moderation tjek på alle beskeder
        ModerationResult result = moderationManager.moderateMessage(event);
        if (!result.isAllowed()) {
            logger.debug("Moderation action triggered for message from {}: {}", 
                        event.getAuthor().getName(), result.getReason());
            moderationManager.executeModerationAction(event, result);
        }
    }


}
