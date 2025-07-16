package com.axion.bot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optimized Database Manager with connection pooling and performance monitoring
 * Provides significant performance improvements over the original DatabaseManager
 */
public class OptimizedDatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedDatabaseManager.class);
    
    private HikariDataSource dataSource;
    private final String databaseUrl;
    private final MeterRegistry meterRegistry;
    
    // Performance metrics
    private final Timer connectionTimer;
    private final Counter connectionCounter;
    private final Counter errorCounter;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    
    // Prepared statement cache
    private final ConcurrentHashMap<String, PreparedStatement> statementCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHED_STATEMENTS = 100;
    
    public OptimizedDatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        this.meterRegistry = new SimpleMeterRegistry();
        
        // Initialize metrics
        this.connectionTimer = Timer.builder("database.connection.time")
                .description("Time taken to get database connection")
                .register(meterRegistry);
        
        this.connectionCounter = Counter.builder("database.connection.count")
                .description("Number of database connections created")
                .register(meterRegistry);
        
        this.errorCounter = Counter.builder("database.error.count")
                .description("Number of database errors")
                .register(meterRegistry);
        
        // Register active connections gauge
        Gauge.builder("database.connections.active", activeConnections, AtomicInteger::get)
                .description("Number of active database connections")
                .register(meterRegistry);
    }
    
    /**
     * Initialize HikariCP connection pool with optimized settings
     */
    public void connect() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Basic connection settings
            config.setJdbcUrl(databaseUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            
            // Pool configuration for optimal performance
            config.setMaximumPoolSize(20);           // Maximum connections
            config.setMinimumIdle(5);                // Minimum idle connections
            config.setConnectionTimeout(30000);      // 30 seconds
            config.setIdleTimeout(600000);           // 10 minutes
            config.setMaxLifetime(1800000);          // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute
            
            // Performance optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            // SQLite specific optimizations
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "NORMAL");
            config.addDataSourceProperty("cache_size", "10000");
            config.addDataSourceProperty("temp_store", "MEMORY");
            
            // Pool name for monitoring
            config.setPoolName("AxionBotPool");
            
            // Health check
            config.setConnectionTestQuery("SELECT 1");
            
            dataSource = new HikariDataSource(config);
            
            // Test connection
            try (Connection testConnection = getConnection()) {
                if (testConnection != null) {
                    logger.info("✅ Optimized database connection pool initialized successfully");
                    logger.info("📊 Pool settings: Max={}, Min={}, Timeout={}ms", 
                            config.getMaximumPoolSize(), 
                            config.getMinimumIdle(), 
                            config.getConnectionTimeout());
                    
                    // Initialize database tables
                    initializeTables();
                } else {
                    throw new SQLException("Failed to establish test connection");
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize database connection pool", e);
            errorCounter.increment();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Get connection from pool with performance monitoring
     */
    public Connection getConnection() {
        try {
            return connectionTimer.recordCallable(() -> {
                try {
                    if (dataSource == null || dataSource.isClosed()) {
                        logger.warn("⚠️ DataSource is null or closed, attempting to reconnect");
                        connect();
                    }
                    
                    Connection connection = dataSource.getConnection();
                    if (connection != null) {
                        connectionCounter.increment();
                        activeConnections.incrementAndGet();
                        
                        // Wrap connection to track when it's closed
                        return new ConnectionWrapper(connection, () -> activeConnections.decrementAndGet());
                    }
                    
                    throw new SQLException("Failed to get connection from pool");
                    
                } catch (SQLException e) {
                    logger.error("❌ Failed to get database connection", e);
                    errorCounter.increment();
                    throw new RuntimeException("Database connection failed", e);
                }
            });
        } catch (Exception e) {
            logger.error("❌ Failed to record connection timing", e);
            errorCounter.increment();
            throw new RuntimeException("Database connection failed", e);
        }
    }
    
    /**
     * Get cached prepared statement for better performance
     */
    public PreparedStatement getCachedStatement(String sql) throws SQLException {
        if (statementCache.size() >= MAX_CACHED_STATEMENTS) {
            // Clear cache if it gets too large
            statementCache.clear();
            logger.debug("🧹 Cleared prepared statement cache (reached max size)");
        }
        
        return statementCache.computeIfAbsent(sql, key -> {
            try {
                return getConnection().prepareStatement(key);
            } catch (SQLException e) {
                logger.error("Failed to create cached prepared statement", e);
                errorCounter.increment();
                return null;
            }
        });
    }
    
    /**
     * Initialize database tables with optimized schema
     */
    private void initializeTables() {
        String[] createTableStatements = {
            // Warnings table with index
            """
            CREATE TABLE IF NOT EXISTS warnings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                reason TEXT NOT NULL,
                moderator_id TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )""",
            "CREATE INDEX IF NOT EXISTS idx_warnings_user_guild ON warnings(user_id, guild_id)",
            
            // Server config table with index
            """
            CREATE TABLE IF NOT EXISTS server_config (
                guild_id TEXT PRIMARY KEY,
                moderation_level TEXT DEFAULT 'standard',
                auto_moderation BOOLEAN DEFAULT true,
                spam_protection BOOLEAN DEFAULT true,
                toxic_detection BOOLEAN DEFAULT true,
                link_protection BOOLEAN DEFAULT false,
                max_messages_per_minute INTEGER DEFAULT 10,
                max_links_per_message INTEGER DEFAULT 3
            )""",
            
            // Moderation logs with indexes
            """
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
            )""",
            "CREATE INDEX IF NOT EXISTS idx_moderation_logs_user_guild ON moderation_logs(user_id, guild_id)",
            "CREATE INDEX IF NOT EXISTS idx_moderation_logs_timestamp ON moderation_logs(timestamp)",
            
            // User violations with index
            """
            CREATE TABLE IF NOT EXISTS user_violations (
                user_id TEXT PRIMARY KEY,
                guild_id TEXT NOT NULL,
                violation_count INTEGER DEFAULT 0,
                last_violation DATETIME,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""",
            "CREATE INDEX IF NOT EXISTS idx_user_violations_guild ON user_violations(guild_id)",
            
            // Temp bans with index
            """
            CREATE TABLE IF NOT EXISTS temp_bans (
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                expires_at DATETIME NOT NULL,
                reason TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (user_id, guild_id)
            )""",
            "CREATE INDEX IF NOT EXISTS idx_temp_bans_expires ON temp_bans(expires_at)",
            
            // User languages with index
            """
            CREATE TABLE IF NOT EXISTS user_languages (
                user_id TEXT PRIMARY KEY,
                language_code TEXT NOT NULL DEFAULT 'en',
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""",
            
            // Tickets with indexes
            """
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
            )""",
            "CREATE INDEX IF NOT EXISTS idx_tickets_user_guild ON tickets(user_id, guild_id)",
            "CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status)",
            "CREATE INDEX IF NOT EXISTS idx_tickets_thread ON tickets(thread_id)",
            
            // Ticket messages with index
            """
            CREATE TABLE IF NOT EXISTS ticket_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ticket_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                message_id TEXT NOT NULL,
                content TEXT NOT NULL,
                is_staff_message BOOLEAN DEFAULT false,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)
            )""",
            "CREATE INDEX IF NOT EXISTS idx_ticket_messages_ticket ON ticket_messages(ticket_id)",
            
            // Ticket categories
            """
            CREATE TABLE IF NOT EXISTS ticket_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guild_id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                emoji TEXT,
                staff_role_id TEXT,
                auto_assign BOOLEAN DEFAULT false,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""",
            "CREATE INDEX IF NOT EXISTS idx_ticket_categories_guild ON ticket_categories(guild_id)",
            
            // Ticket config
            """
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
            )""",
            
            // User activities table with indexes
            """
            CREATE TABLE IF NOT EXISTS user_activities (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                guild_id TEXT NOT NULL,
                channel_id TEXT,
                activity_type TEXT NOT NULL,
                content TEXT,
                target_message_id TEXT,
                severity REAL DEFAULT 0.0,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )""",
            "CREATE INDEX IF NOT EXISTS idx_user_activities_user_guild ON user_activities(user_id, guild_id)",
            "CREATE INDEX IF NOT EXISTS idx_user_activities_timestamp ON user_activities(timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_user_activities_type ON user_activities(activity_type)"
        };
        
        try (Connection connection = getConnection()) {
            for (String sql : createTableStatements) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.execute();
                }
            }
            logger.info("✅ Database tables and indexes initialized successfully");
        } catch (SQLException e) {
            logger.error("❌ Failed to initialize database tables", e);
            errorCounter.increment();
            throw new RuntimeException("Database table initialization failed", e);
        }
    }
    
    /**
     * Check if database connection is healthy
     */
    public boolean isConnected() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                return false;
            }
            
            try (Connection connection = dataSource.getConnection()) {
                return connection != null && !connection.isClosed();
            }
        } catch (SQLException e) {
            logger.debug("Database health check failed", e);
            return false;
        }
    }
    
    /**
     * Get recent user activities from the database
     * @param userId The user ID to get activities for
     * @return List of recent UserActivity objects
     */
    public List<com.axion.bot.moderation.UserActivity> getRecentUserActivity(String userId) {
        List<com.axion.bot.moderation.UserActivity> activities = new ArrayList<>();
        
        String sql = """
            SELECT user_id, content, activity_type, target_message_id, severity, timestamp
            FROM user_activities 
            WHERE user_id = ? 
            ORDER BY timestamp DESC 
            LIMIT 50
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String activityTypeStr = rs.getString("activity_type");
                    com.axion.bot.moderation.ActivityType activityType;
                    
                    try {
                        activityType = com.axion.bot.moderation.ActivityType.valueOf(activityTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        activityType = com.axion.bot.moderation.ActivityType.MESSAGE; // Default fallback
                    }
                    
                    com.axion.bot.moderation.UserActivity activity = new com.axion.bot.moderation.UserActivity(
                        rs.getString("user_id"),
                        rs.getString("content"),
                        rs.getTimestamp("timestamp").toInstant(),
                        activityType,
                        rs.getString("target_message_id")
                    );
                    
                    activity.setSeverity(rs.getDouble("severity"));
                    activities.add(activity);
                }
            }
            
        } catch (SQLException e) {
            logger.error("❌ Failed to get recent user activity for user: {}", userId, e);
            errorCounter.increment();
        }
        
        return activities;
    }
    
    /**
     * Save user activity to the database
     * @param userId The user ID
     * @param guildId The guild ID
     * @param channelId The channel ID (optional)
     * @param activity The UserActivity object to save
     */
    public void saveUserActivity(String userId, String guildId, String channelId, com.axion.bot.moderation.UserActivity activity) {
        String sql = """
            INSERT INTO user_activities (user_id, guild_id, channel_id, activity_type, content, target_message_id, severity, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setString(3, channelId);
            stmt.setString(4, activity.getType().name());
            stmt.setString(5, activity.getContent());
            stmt.setString(6, activity.getTargetMessageId());
            stmt.setDouble(7, activity.getSeverity());
            stmt.setTimestamp(8, java.sql.Timestamp.from(activity.getTimestamp()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("❌ Failed to save user activity for user: {}", userId, e);
            errorCounter.increment();
        }
    }
    
    /**
     * Log a moderation action to the database
     * @param userId The user ID who was moderated
     * @param username The username of the user
     * @param moderatorId The ID of the moderator (or "SYSTEM" for automated actions)
     * @param moderatorName The name of the moderator
     * @param action The moderation action taken
     * @param reason The reason for the action
     * @param guildId The guild ID where the action occurred
     * @param channelId The channel ID (optional)
     * @param messageId The message ID (optional)
     * @param severity The severity level (1-5)
     * @param automated Whether this was an automated action
     */
    public void logModerationAction(String userId, String username, String moderatorId, 
                                   String moderatorName, String action, String reason, 
                                   String guildId, String channelId, String messageId, 
                                   int severity, boolean automated) {
        String sql = """
            INSERT INTO moderation_logs 
            (user_id, username, moderator_id, moderator_name, action, reason, guild_id, 
             channel_id, message_id, severity, automated, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, moderatorId);
            stmt.setString(4, moderatorName);
            stmt.setString(5, action);
            stmt.setString(6, reason);
            stmt.setString(7, guildId);
            stmt.setString(8, channelId);
            stmt.setString(9, messageId);
            stmt.setInt(10, severity);
            stmt.setBoolean(11, automated);
            
            stmt.executeUpdate();
            
            logger.debug("✅ Logged moderation action: {} for user {} by {}", action, userId, moderatorName);
            
        } catch (SQLException e) {
            logger.error("❌ Failed to log moderation action for user: {}", userId, e);
            errorCounter.increment();
        }
    }
    
    /**
     * Get performance metrics
     */
    public DatabaseMetrics getMetrics() {
        return new DatabaseMetrics(
            connectionCounter.count(),
            errorCounter.count(),
            activeConnections.get(),
            dataSource != null ? dataSource.getHikariPoolMXBean().getTotalConnections() : 0,
            dataSource != null ? dataSource.getHikariPoolMXBean().getIdleConnections() : 0
        );
    }
    
    /**
     * Gracefully shutdown the connection pool
     */
    public void disconnect() {
        try {
            // Clear statement cache
            statementCache.clear();
            
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                logger.info("✅ Database connection pool closed successfully");
            }
        } catch (Exception e) {
            logger.error("❌ Error closing database connection pool", e);
        }
    }
    
    /**
     * Database performance metrics
     */
    public static class DatabaseMetrics {
        public final double totalConnections;
        public final double totalErrors;
        public final int activeConnections;
        public final int poolTotalConnections;
        public final int poolIdleConnections;
        
        public DatabaseMetrics(double totalConnections, double totalErrors, int activeConnections, 
                             int poolTotalConnections, int poolIdleConnections) {
            this.totalConnections = totalConnections;
            this.totalErrors = totalErrors;
            this.activeConnections = activeConnections;
            this.poolTotalConnections = poolTotalConnections;
            this.poolIdleConnections = poolIdleConnections;
        }
        
        @Override
        public String toString() {
            return String.format("DatabaseMetrics{total=%.0f, errors=%.0f, active=%d, pool=%d/%d}",
                    totalConnections, totalErrors, activeConnections, poolIdleConnections, poolTotalConnections);
        }
    }
}