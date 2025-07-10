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
                guild_id TEXT NOT NULL,
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
                toxic_detection BOOLEAN DEFAULT true,
                link_protection BOOLEAN DEFAULT false,
                max_messages_per_minute INTEGER DEFAULT 10,
                max_links_per_message INTEGER DEFAULT 3
            )""";

        String createModerationLogsTable = """
            CREATE TABLE IF NOT EXISTS moderation_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                username TEXT NOT NULL,
                moderator_id TEXT NOT NULL,
                moderator_name TEXT NOT NULL,
                action TEXT NOT NULL,
                reason TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                channel_id TEXT,
                message_id TEXT,
                severity INTEGER DEFAULT 1,
                automated BOOLEAN DEFAULT false,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        String createUserViolationsTable = """
            CREATE TABLE IF NOT EXISTS user_violations (
                user_id TEXT PRIMARY KEY,
                guild_id TEXT NOT NULL,
                violation_count INTEGER DEFAULT 0,
                last_violation DATETIME,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        String createTempBansTable = """
            CREATE TABLE IF NOT EXISTS temp_bans (
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                expires_at DATETIME NOT NULL,
                reason TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (user_id, guild_id)
            )""";

        String createUserLanguagesTable = """
            CREATE TABLE IF NOT EXISTS user_languages (
                user_id TEXT PRIMARY KEY,
                language_code TEXT NOT NULL DEFAULT 'en',
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        String createTicketsTable = """
            CREATE TABLE IF NOT EXISTS tickets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ticket_id TEXT UNIQUE NOT NULL,
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                thread_id TEXT UNIQUE NOT NULL,
                category TEXT NOT NULL DEFAULT 'general',
                priority TEXT NOT NULL DEFAULT 'medium',
                status TEXT NOT NULL DEFAULT 'open',
                subject TEXT NOT NULL,
                description TEXT,
                assigned_staff_id TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                closed_at DATETIME,
                closed_by TEXT,
                close_reason TEXT
            )""";

        String createTicketMessagesTable = """
            CREATE TABLE IF NOT EXISTS ticket_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ticket_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                message_id TEXT NOT NULL,
                content TEXT NOT NULL,
                is_staff_message BOOLEAN DEFAULT false,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)
            )""";

        String createTicketCategoriesTable = """
            CREATE TABLE IF NOT EXISTS ticket_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guild_id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                emoji TEXT,
                staff_role_id TEXT,
                auto_assign BOOLEAN DEFAULT false,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        String createTicketConfigTable = """
            CREATE TABLE IF NOT EXISTS ticket_config (
                guild_id TEXT PRIMARY KEY,
                enabled BOOLEAN DEFAULT true,
                support_category_id TEXT,
                staff_role_id TEXT,
                admin_role_id TEXT,
                transcript_channel_id TEXT,
                max_tickets_per_user INTEGER DEFAULT 3,
                auto_close_inactive_hours INTEGER DEFAULT 72,
                welcome_message TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        try (PreparedStatement stmt1 = connection.prepareStatement(createWarningsTable);
             PreparedStatement stmt2 = connection.prepareStatement(createConfigTable);
             PreparedStatement stmt3 = connection.prepareStatement(createModerationLogsTable);
             PreparedStatement stmt4 = connection.prepareStatement(createUserViolationsTable);
             PreparedStatement stmt5 = connection.prepareStatement(createTempBansTable);
             PreparedStatement stmt6 = connection.prepareStatement(createUserLanguagesTable);
             PreparedStatement stmt7 = connection.prepareStatement(createTicketsTable);
             PreparedStatement stmt8 = connection.prepareStatement(createTicketMessagesTable);
             PreparedStatement stmt9 = connection.prepareStatement(createTicketCategoriesTable);
             PreparedStatement stmt10 = connection.prepareStatement(createTicketConfigTable)) {
            
            stmt1.execute();
            stmt2.execute();
            stmt3.execute();
            stmt4.execute();
            stmt5.execute();
            stmt6.execute();
            stmt7.execute();
            stmt8.execute();
            stmt9.execute();
            stmt10.execute();
            logger.info("Database tabeller initialiseret succesfuldt");
            
        } catch (SQLException e) {
            logger.severe("Fejl ved initialisering af tabeller: " + e.getMessage());
        }
    }

    /**
     * Får database forbindelse
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                logger.warning("Database forbindelse er null eller lukket. Forsøger at genoprette forbindelse.");
                connect();
            }
        } catch (SQLException e) {
            logger.severe("Fejl ved tjek af database forbindelse: " + e.getMessage());
            connect();
        }
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
