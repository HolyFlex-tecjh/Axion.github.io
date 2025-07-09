package com.axion.bot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Håndterer database forbindelser og operationer
 */
public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    
    private Connection connection;
    private final String databaseUrl;

    public DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    /**
     * Åbner database forbindelse
     */
    public void connect() {
        try {
            connection = DriverManager.getConnection(databaseUrl);
            logger.info("Database forbindelse etableret");
            initializeTables();
        } catch (SQLException e) {
            logger.severe("Fejl ved database forbindelse: " + e.getMessage());
        }
    }

    /**
     * Lukker database forbindelse
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database forbindelse lukket");
            }
        } catch (SQLException e) {
            logger.severe("Fejl ved lukning af database: " + e.getMessage());
        }
    }

    /**
     * Initialiserer database tabeller
     */
    private void initializeTables() {
        String createWarningsTable = """
            CREATE TABLE IF NOT EXISTS warnings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                reason TEXT NOT NULL,
                moderator_id TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        String createConfigTable = """
            CREATE TABLE IF NOT EXISTS server_config (
                guild_id TEXT PRIMARY KEY,
                moderation_level TEXT DEFAULT 'standard',
                auto_moderation BOOLEAN DEFAULT true,
                spam_protection BOOLEAN DEFAULT true,
                toxic_detection BOOLEAN DEFAULT true
            )""";

        try (PreparedStatement stmt1 = connection.prepareStatement(createWarningsTable);
             PreparedStatement stmt2 = connection.prepareStatement(createConfigTable)) {
            
            stmt1.execute();
            stmt2.execute();
            logger.info("Database tabeller initialiseret");
            
        } catch (SQLException e) {
            logger.severe("Fejl ved initialisering af tabeller: " + e.getMessage());
        }
    }

    /**
     * Får database forbindelse
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Tjekker om database forbindelse er aktiv
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
