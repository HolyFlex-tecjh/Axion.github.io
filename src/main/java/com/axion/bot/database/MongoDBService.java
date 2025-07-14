package com.axion.bot.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB service for common database operations
 */
public class MongoDBService {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBService.class);
    
    private final MongoDatabase database;
    
    public MongoDBService() {
        this.database = MongoDBConfig.getDatabase();
    }
    
    /**
     * Get a collection by name
     * @param collectionName the name of the collection
     * @return MongoCollection instance
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
    
    /**
     * Insert a document into a collection
     * @param collectionName the collection name
     * @param document the document to insert
     */
    public void insertDocument(String collectionName, Document document) {
        try {
            getCollection(collectionName).insertOne(document);
            logger.debug("Document inserted into collection: {}", collectionName);
        } catch (Exception e) {
            logger.error("Failed to insert document into {}: {}", collectionName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Find a document by filter
     * @param collectionName the collection name
     * @param filter the filter criteria
     * @return the found document or null
     */
    public Document findDocument(String collectionName, Bson filter) {
        try {
            return getCollection(collectionName).find(filter).first();
        } catch (Exception e) {
            logger.error("Failed to find document in {}: {}", collectionName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Find multiple documents by filter
     * @param collectionName the collection name
     * @param filter the filter criteria
     * @return list of found documents
     */
    public List<Document> findDocuments(String collectionName, Bson filter) {
        try {
            List<Document> documents = new ArrayList<>();
            getCollection(collectionName).find(filter).into(documents);
            return documents;
        } catch (Exception e) {
            logger.error("Failed to find documents in {}: {}", collectionName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Update a document
     * @param collectionName the collection name
     * @param filter the filter criteria
     * @param update the update operations
     * @param upsert whether to create if not exists
     */
    public void updateDocument(String collectionName, Bson filter, Bson update, boolean upsert) {
        try {
            UpdateOptions options = new UpdateOptions().upsert(upsert);
            getCollection(collectionName).updateOne(filter, update, options);
            logger.debug("Document updated in collection: {}", collectionName);
        } catch (Exception e) {
            logger.error("Failed to update document in {}: {}", collectionName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Delete a document
     * @param collectionName the collection name
     * @param filter the filter criteria
     */
    public void deleteDocument(String collectionName, Bson filter) {
        try {
            getCollection(collectionName).deleteOne(filter);
            logger.debug("Document deleted from collection: {}", collectionName);
        } catch (Exception e) {
            logger.error("Failed to delete document from {}: {}", collectionName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Count documents in a collection
     * @param collectionName the collection name
     * @param filter the filter criteria
     * @return the count of documents
     */
    public long countDocuments(String collectionName, Bson filter) {
        try {
            return getCollection(collectionName).countDocuments(filter);
        } catch (Exception e) {
            logger.error("Failed to count documents in {}: {}", collectionName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Check if a document exists
     * @param collectionName the collection name
     * @param filter the filter criteria
     * @return true if document exists, false otherwise
     */
    public boolean documentExists(String collectionName, Bson filter) {
        return findDocument(collectionName, filter) != null;
    }
    
    // Common collection names as constants
    public static final String GUILDS_COLLECTION = "guilds";
    public static final String USERS_COLLECTION = "users";
    public static final String MODERATION_LOGS_COLLECTION = "moderation_logs";
    public static final String CONFIGURATIONS_COLLECTION = "configurations";
    public static final String ANALYTICS_COLLECTION = "analytics";
}