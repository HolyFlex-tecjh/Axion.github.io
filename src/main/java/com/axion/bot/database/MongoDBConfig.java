package com.axion.bot.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDB configuration and connection management
 */
public class MongoDBConfig {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBConfig.class);
    
    private static final String CONNECTION_STRING = "mongodb+srv://Shadowcrushers:Is6Wpf0XdNI13G4x@axionbot.8rvwqwr.mongodb.net/?retryWrites=true&w=majority&appName=AxionBot";
    private static final String DATABASE_NAME = "axionbot";
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    /**
     * Initialize MongoDB connection
     */
    public static void initialize() {
        try {
            ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DATABASE_NAME);
            
            // Test connection
            database.runCommand(new org.bson.Document("ping", 1));
            logger.info("Successfully connected to MongoDB Atlas");
            
        } catch (Exception e) {
            logger.error("Failed to connect to MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("MongoDB connection failed", e);
        }
    }
    
    /**
     * Get the MongoDB database instance
     * @return MongoDatabase instance
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            initialize();
        }
        return database;
    }
    
    /**
     * Get the MongoDB client instance
     * @return MongoClient instance
     */
    public static MongoClient getClient() {
        if (mongoClient == null) {
            initialize();
        }
        return mongoClient;
    }
    
    /**
     * Close MongoDB connection
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed");
        }
    }
    
    /**
     * Check if MongoDB is connected
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        try {
            if (database != null) {
                database.runCommand(new org.bson.Document("ping", 1));
                return true;
            }
        } catch (Exception e) {
            logger.warn("MongoDB connection check failed: {}", e.getMessage());
        }
        return false;
    }
}