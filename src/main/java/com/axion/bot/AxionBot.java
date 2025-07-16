package com.axion.bot;

import com.axion.bot.database.DatabaseManager;
import com.axion.bot.database.DatabaseService;
import com.axion.bot.database.MongoDBConfig;
import com.axion.bot.database.MongoDBService;
import com.axion.bot.translation.UserLanguageManager;
// TODO: Re-enable these imports when classes are fixed
// import com.axion.bot.web.ModerationConfigurationController;
// import com.axion.bot.commands.SlashCommandRegistrar;
// import com.axion.bot.commands.CommandHandler;
// import com.axion.bot.commands.SlashCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hovedklassen for Axion Bot
 * En moderne Discord bot implementeret i Java med JDA
 * Bruger KUN slash commands (/) - ingen prefix commands (!)
 */
public class AxionBot {
    private static final Logger logger = LoggerFactory.getLogger(AxionBot.class);
    private JDA jda;
    private String token;
    private DatabaseManager databaseManager;
    private DatabaseService databaseService;
    private MongoDBService mongoDBService;
    private ScheduledExecutorService activityUpdater;
    // TODO: Re-enable when ModerationConfigurationController is fixed
    // private ModerationConfigurationController webController;

    public AxionBot() {
        loadConfiguration();
        initializeDatabase();
        initializeMongoDB();
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
     * Initialiserer database forbindelse
     */
    private void initializeDatabase() {
        try {
            // Brug SQLite som standard database
            String databaseUrl = System.getenv("DATABASE_URL");
            if (databaseUrl == null || databaseUrl.isEmpty()) {
                databaseUrl = "jdbc:sqlite:axion_bot.db";
                logger.info("Bruger standard SQLite database: axion_bot.db");
            }
            
            databaseManager = new DatabaseManager(databaseUrl);
            databaseManager.connect();
            
            // Kontroller at database forbindelse er etableret
            if (!databaseManager.isConnected()) {
                logger.error("Database forbindelse kunne ikke etableres!");
                throw new RuntimeException("Database forbindelse fejlede");
            }
            
            databaseService = new DatabaseService(databaseManager);
            
            // Initialize UserLanguageManager with DatabaseService
            UserLanguageManager.getInstance().setDatabaseService(databaseService);
            
            logger.info("Database forbindelse etableret succesfuldt");
        } catch (Exception e) {
            logger.error("Fejl ved initialisering af database", e);
            System.exit(1);
        }
    }
    
    /**
     * Initialiserer MongoDB forbindelse
     */
    private void initializeMongoDB() {
        try {
            // Initialize MongoDB connection
            MongoDBConfig.initialize();
            mongoDBService = new MongoDBService();
            
            // Test MongoDB connection
            if (MongoDBConfig.isConnected()) {
                logger.info("MongoDB forbindelse etableret succesfuldt");
            } else {
                logger.warn("MongoDB forbindelse kunne ikke etableres - fortsætter med SQLite");
            }
        } catch (Exception e) {
            logger.warn("Fejl ved initialisering af MongoDB: {} - fortsætter med SQLite", e.getMessage());
            // Don't exit - MongoDB is optional, SQLite is the fallback
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
                    .setActivity(Activity.playing("Starter op..."))
                    // TODO: Add event listeners when classes are fixed
            // .addEventListeners(
            //         new CommandHandler(databaseService, mongoDBService),     // Auto-moderation
            //         new SlashCommandHandler(databaseService) // Slash commands (/)
            // )
                    .build();

            jda.awaitReady();
            logger.info("Axion Bot er startet og klar til brug!");
            logger.info("Bot er tilsluttet {} servere", jda.getGuilds().size());
            logger.info("Botten bruger KUN slash commands (/) - skriv /help for hjælp");
            
            // TODO: Register slash commands when SlashCommandRegistrar is fixed
            // try {
            //     SlashCommandRegistrar.registerGlobalCommands(jda);
            //     logger.info("Slash kommandoer registreret!");
            // } catch (Exception e) {
            //     logger.warn("Kunne ikke registrere slash kommandoer: {}", e.getMessage());
            // }
            
            // Start activity updater der opdaterer hver 30 sekunder
            startActivityUpdater();
            
            // TODO: Start web server when ModerationConfigurationController is fixed
            // startWebServer();

        } catch (Exception e) {
            logger.error("Fejl ved start af bot", e);
            System.exit(1);
        }
    }
    
    /**
     * Starter automatisk opdatering af bot aktivitet
     */
    private void startActivityUpdater() {
        activityUpdater = Executors.newSingleThreadScheduledExecutor();
        
        // Opdater med det samme
        updateBotActivity();
        
        // Planlæg opdateringer hver 30 sekunder
        activityUpdater.scheduleAtFixedRate(this::updateBotActivity, 30, 30, TimeUnit.SECONDS);
        logger.info("Activity updater startet - opdaterer hver 30 sekunder");
    }
    
    /**
     * Starter web server for moderation dashboard
     * TODO: Re-enable when ModerationConfigurationController is fixed
     */
    /*
    private void startWebServer() {
        try {
            webController = new ModerationConfigurationController(databaseService);
            
            // Get port from environment variable or use default
            String portStr = System.getenv("WEB_PORT");
            int port = portStr != null ? Integer.parseInt(portStr) : 8080;
            
            webController.startServer(port);
            logger.info("Moderation dashboard web server startet på port {}", port);
        } catch (Exception e) {
            logger.error("Fejl ved start af web server", e);
            // Don't exit - web server is optional
        }
    }
    */
    
    /**
     * Opdaterer bot aktivitet med aktuel server count
     */
    private void updateBotActivity() {
        if (jda != null) {
            int serverCount = jda.getGuilds().size();
            String activity = String.format("på %d servere", serverCount);
            jda.getPresence().setActivity(Activity.playing(activity));
            logger.debug("Bot aktivitet opdateret: {}", activity);
        }
    }

    /**
     * Stopper botten
     */
    public void shutdown() {
        if (activityUpdater != null && !activityUpdater.isShutdown()) {
            activityUpdater.shutdown();
            try {
                if (!activityUpdater.awaitTermination(5, TimeUnit.SECONDS)) {
                    activityUpdater.shutdownNow();
                }
                logger.info("Activity updater stoppet");
            } catch (InterruptedException e) {
                activityUpdater.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // TODO: Stop web server when webController is implemented
        /*
        if (webController != null) {
            webController.stopServer();
            logger.info("Web server stoppet");
        }
        */
        
        if (jda != null) {
            jda.shutdown();
            logger.info("Axion Bot er lukket ned");
        }
        
        if (databaseManager != null) {
            databaseManager.disconnect();
            logger.info("Database forbindelse lukket");
        }
        
        // Close MongoDB connection
        try {
            MongoDBConfig.close();
        } catch (Exception e) {
            logger.warn("Fejl ved lukning af MongoDB forbindelse: {}", e.getMessage());
        }
    }

    /**
     * Får database manager instans
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Får database service instans
     */
    public DatabaseService getDatabaseService() {
        return databaseService;
    }
    
    /**
     * Får MongoDB service instans
     */
    public MongoDBService getMongoDBService() {
        return mongoDBService;
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
