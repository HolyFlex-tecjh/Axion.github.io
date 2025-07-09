package com.axion.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Hovedklassen for Axion Bot
 * En moderne Discord bot implementeret i Java med JDA
 * Bruger KUN slash commands (/) - ingen prefix commands (!)
 */
public class AxionBot {
    private static final Logger logger = LoggerFactory.getLogger(AxionBot.class);
    private JDA jda;
    private String token;

    public AxionBot() {
        loadConfiguration();
    }

    /**
     * Indlæser konfiguration fra config.properties filen
     */
    private void loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.warn("config.properties fil ikke fundet. Bruger miljøvariabler.");
                token = System.getenv("DISCORD_TOKEN");
                return;
            }
            properties.load(input);
            token = properties.getProperty("discord.token");
        } catch (IOException ex) {
            logger.error("Fejl ved indlæsning af konfiguration", ex);
            System.exit(1);
        }
    }

    /**
     * Starter botten
     */
    public void start() {
        if (token == null || token.isEmpty()) {
            logger.error("Discord token ikke fundet! Tilføj dit token til config.properties eller som miljøvariabel DISCORD_TOKEN");
            System.exit(1);
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MODERATION
                    )
                    .setActivity(Activity.playing("Axion Bot v1.0 | /help for kommandoer"))
                    .addEventListeners(
                            new CommandHandler(),     // Auto-moderation
                            new SlashCommandHandler() // Slash commands (/)
                    )
                    .build();

            jda.awaitReady();
            logger.info("Axion Bot er startet og klar til brug!");
            logger.info("Bot er tilsluttet {} servere", jda.getGuilds().size());
            logger.info("Botten bruger KUN slash commands (/) - skriv /help for hjælp");
            
            // Registrer slash kommandoer
            SlashCommandRegistrar.registerGlobalCommands(jda);
            logger.info("Slash kommandoer registreret!");

        } catch (Exception e) {
            logger.error("Fejl ved start af bot", e);
            System.exit(1);
        }
    }

    /**
     * Stopper botten
     */
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            logger.info("Axion Bot er lukket ned");
        }
    }

    /**
     * Main metode - indgangspunkt for programmet
     */
    public static void main(String[] args) {
        logger.info("Starter Axion Bot...");
        
        AxionBot bot = new AxionBot();
        
        // Tilføj shutdown hook for at lukke botten pænt ned
        Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
        
        bot.start();
    }
}
