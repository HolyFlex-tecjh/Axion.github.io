package com.axion.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registrerer slash kommandoer for Axion Bot
 */
public class SlashCommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandRegistrar.class);
    
    /**
     * Registrerer alle slash kommandoer globalt
     */
    public static void registerGlobalCommands(JDA jda) {
        logger.info("Registrerer globale slash kommandoer...");
        
        jda.updateCommands().addCommands(
            // Grundlæggende kommandoer
            Commands.slash("ping", "Teste om botten svarer"),
            
            Commands.slash("hello", "Få en hilsen fra botten"),
            
            Commands.slash("info", "Vis information om botten"),
            
            Commands.slash("help", "Vis tilgængelige kommandoer"),
            
            Commands.slash("time", "Vis nuværende tid"),
            
            Commands.slash("uptime", "Vis hvor længe botten har kørt"),
            
            Commands.slash("modhelp", "Vis moderation kommandoer"),
            
            Commands.slash("invite", "Få invite link til at tilføje botten til din server"),
            
            Commands.slash("support", "Få support og hjælp med botten"),
            
            Commands.slash("about", "Detaljeret information om botten"),
            
            // Debug kommandoer (kun for udviklere)
            Commands.slash("listcommands", "Vis alle registrerede slash kommandoer"),
            
            Commands.slash("forcesync", "Force synkroniser slash kommandoer"),
            
            // Moderation kommandoer
            Commands.slash("ban", "Ban en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal bannes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til ban", false)
                ),
            
            Commands.slash("kick", "Kick en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal kickes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til kick", false)
                ),
            
            Commands.slash("timeout", "Giv en bruger timeout")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal have timeout", true),
                    new OptionData(OptionType.INTEGER, "duration", "Varighed i minutter (1-10080)", true)
                        .setMinValue(1)
                        .setMaxValue(10080), // Max 7 dage
                    new OptionData(OptionType.STRING, "reason", "Årsag til timeout", false)
                ),
            
            Commands.slash("warn", "Advar en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal advares", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til advarsel", true)
                ),
            
            Commands.slash("unwarn", "Fjern alle advarsler fra en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal fjernes", true)
                ),
            
            Commands.slash("warnings", "Vis antal advarsler for en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal vises", true)
                ),
            
            Commands.slash("purge", "Slet et antal beskeder fra kanalen")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "amount", "Antal beskeder at slette (1-100)", true)
                        .setMinValue(1)
                        .setMaxValue(100)
                ),
            
            Commands.slash("modconfig", "Konfigurer moderation indstillinger")
                .addOptions(
                    new OptionData(OptionType.STRING, "level", "Moderation niveau", false)
                        .addChoice("Mild", "mild")
                        .addChoice("Standard", "standard")
                        .addChoice("Streng", "strict")
                ),
            
            Commands.slash("modstats", "Vis moderation statistikker"),
            
            Commands.slash("addfilter", "Tilføj et ord til custom filteret")
                .addOptions(
                    new OptionData(OptionType.STRING, "word", "Ordet der skal filtreres", true)
                ),
            
            // Sprog kommandoer
            Commands.slash("setlanguage", "Skift dit sprog")
                .addOptions(
                    new OptionData(OptionType.STRING, "language", "Sprogkode (f.eks. da, en, de)", true)
                ),
            
            Commands.slash("languages", "Vis tilgængelige sprog"),
            
            Commands.slash("resetlanguage", "Nulstil dit sprog til standard (engelsk)"),
            
            // Udvikler kommandoer (kun for udviklere)
            Commands.slash("devinfo", "Vis udvikler information (kun for udviklere)"),
            
            Commands.slash("devstats", "Vis detaljerede bot statistikker (kun for udviklere)")
        ).queue(
            success -> logger.info("Slash kommandoer registreret succesfuldt!"),
            error -> logger.error("Fejl ved registrering af slash kommandoer: {}", error.getMessage())
        );
    }
    
    // Fjernet duplikeret metode registerGuildCommands for at undgå fejl om dublet-signaturer.
    
    /**
     * Registrerer slash kommandoer for en specifik guild (hurtigere opdatering)
     */
    public static void registerGuildCommands(JDA jda, String guildId) {
        logger.info("Registrerer guild-specifikke slash kommandoer for guild: {}", guildId);
        
        jda.getGuildById(guildId).updateCommands().addCommands(
            // Samme kommandoer som globale, men opdateres øjeblikkeligt for denne guild
            Commands.slash("ping", "Teste om botten svarer"),
            Commands.slash("hello", "Få en hilsen fra botten"),
            Commands.slash("info", "Vis information om botten"),
            Commands.slash("help", "Vis tilgængelige kommandoer"),
            Commands.slash("time", "Vis nuværende tid"),
            Commands.slash("uptime", "Vis hvor længe botten har kørt"),
            Commands.slash("modhelp", "Vis moderation kommandoer"),
            Commands.slash("invite", "Få invite link til at tilføje botten til din server"),
            Commands.slash("support", "Få support og hjælp med botten"),
            Commands.slash("about", "Detaljeret information om botten"),
            
            // Debug kommandoer (kun for udviklere)
            Commands.slash("listcommands", "Vis alle registrerede slash kommandoer"),
            Commands.slash("forcesync", "Force synkroniser slash kommandoer"),
            
            Commands.slash("ban", "Ban en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal bannes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til ban", false)
                ),
            
            Commands.slash("kick", "Kick en bruger fra serveren")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal kickes", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til kick", false)
                ),
            
            Commands.slash("timeout", "Giv en bruger timeout")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal have timeout", true),
                    new OptionData(OptionType.INTEGER, "duration", "Varighed i minutter (1-10080)", true)
                        .setMinValue(1)
                        .setMaxValue(10080),
                    new OptionData(OptionType.STRING, "reason", "Årsag til timeout", false)
                ),
            
            Commands.slash("warn", "Advar en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren der skal advares", true),
                    new OptionData(OptionType.STRING, "reason", "Årsag til advarsel", true)
                ),
            
            Commands.slash("unwarn", "Fjern alle advarsler fra en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal fjernes", true)
                ),
            
            Commands.slash("warnings", "Vis antal advarsler for en bruger")
                .addOptions(
                    new OptionData(OptionType.USER, "user", "Brugeren hvis advarsler skal vises", true)
                ),
            
            Commands.slash("purge", "Slet et antal beskeder fra kanalen")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "amount", "Antal beskeder at slette (1-100)", true)
                        .setMinValue(1)
                        .setMaxValue(100)
                ),
            
            Commands.slash("modconfig", "Konfigurer moderation indstillinger")
                .addOptions(
                    new OptionData(OptionType.STRING, "level", "Moderation niveau", false)
                        .addChoice("Mild", "mild")
                        .addChoice("Standard", "standard")
                        .addChoice("Streng", "strict")
                ),
            
            Commands.slash("modstats", "Vis moderation statistikker"),
            
            Commands.slash("addfilter", "Tilføj et ord til custom filteret")
                .addOptions(
                    new OptionData(OptionType.STRING, "word", "Ordet der skal filtreres", true)
                ),
            
            // Sprog kommandoer
            Commands.slash("setlanguage", "Skift dit sprog")
                .addOptions(
                    new OptionData(OptionType.STRING, "language", "Sprogkode (f.eks. da, en, de)", true)
                ),
            
            Commands.slash("languages", "Vis tilgængelige sprog"),
            
            Commands.slash("resetlanguage", "Nulstil dit sprog til standard (engelsk)")
        ).queue(
            success -> logger.info("Guild slash kommandoer registreret succesfuldt for guild: {}", guildId),
            error -> logger.error("Fejl ved registrering af guild slash kommandoer: {}", error.getMessage())
        );
    }
    
    /**
     * Sletter alle globale slash kommandoer
     */
    public static void clearGlobalCommands(JDA jda) {
        logger.info("Sletter alle globale slash kommandoer...");
        
        jda.updateCommands().queue(
            success -> logger.info("Globale slash kommandoer slettet succesfuldt!"),
            error -> logger.error("Fejl ved sletning af globale slash kommandoer: {}", error.getMessage())
        );
    }
    
    /**
     * Sletter alle guild-specifikke slash kommandoer
     */
    public static void clearGuildCommands(JDA jda, String guildId) {
        logger.info("Sletter guild slash kommandoer for guild: {}", guildId);
        
        jda.getGuildById(guildId).updateCommands().queue(
            success -> logger.info("Guild slash kommandoer slettet succesfuldt for guild: {}", guildId),
            error -> logger.error("Fejl ved sletning af guild slash kommandoer: {}", error.getMessage())
        );
    }
    
    /**
     * Dynamically refreshes all slash commands by clearing and re-registering them
     * This method provides better control over the sync process for forcesync command
     */
    public static void refreshAllCommands(JDA jda, java.util.function.Consumer<Boolean> callback) {
        logger.info("Starting dynamic refresh of all slash commands...");
        
        // First clear all existing commands
        jda.updateCommands().queue(
            clearSuccess -> {
                logger.info("Successfully cleared existing commands, now re-registering...");
                
                // Re-register all commands
                registerGlobalCommands(jda);
                
                // Wait a moment then verify
                java.util.concurrent.ScheduledExecutorService scheduler = 
                    java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
                
                scheduler.schedule(() -> {
                    jda.retrieveCommands().queue(
                        commands -> {
                            logger.info("Command refresh completed successfully. Total commands: {}", commands.size());
                            callback.accept(true);
                            scheduler.shutdown();
                        },
                        error -> {
                            logger.error("Failed to verify refreshed commands: {}", error.getMessage());
                            callback.accept(false);
                            scheduler.shutdown();
                        }
                    );
                }, 2, java.util.concurrent.TimeUnit.SECONDS);
            },
            clearError -> {
                logger.error("Failed to clear existing commands during refresh: {}", clearError.getMessage());
                callback.accept(false);
            }
        );
    }
    
    /**
     * Gets the total count of registered commands for verification purposes
     */
    public static void getCommandCount(JDA jda, java.util.function.Consumer<Integer> callback) {
        jda.retrieveCommands().queue(
            commands -> callback.accept(commands.size()),
            error -> {
                logger.error("Failed to retrieve command count: {}", error.getMessage());
                callback.accept(-1);
            }
        );
    }
}