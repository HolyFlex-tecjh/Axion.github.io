package com.axion.bot;

import com.axion.bot.database.OptimizedDatabaseManager;
import com.axion.bot.optimization.*;
import com.axion.bot.services.TicketService;
import com.axion.bot.services.UserLanguageManager;
import com.axion.bot.tickets.OptimizedTicketManager;
import com.axion.bot.models.Ticket;
import com.axion.bot.models.TicketPriority;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive performance test suite for Axion Bot optimizations
 * 
 * This test suite validates:
 * - Database connection pooling performance
 * - Translation caching efficiency
 * - Async processing capabilities
 * - Object pooling effectiveness
 * - Overall system performance under load
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceTestSuite {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestSuite.class);
    
    private static OptimizedDatabaseManager databaseManager;
    private static OptimizedTranslationManager translationManager;
    private static OptimizedAsyncProcessor asyncProcessor;
    private static ObjectPoolManager objectPoolManager;
    private static OptimizedCommandRegistry commandRegistry;
    private static OptimizedTicketManager ticketManager;
    
    // Test configuration
    private static final int CONCURRENT_USERS = 50;
    private static final int OPERATIONS_PER_USER = 20;
    private static final int WARMUP_ITERATIONS = 10;
    
    @BeforeAll
    static void setupPerformanceTest() {
        logger.info("Setting up performance test environment...");
        
        try {
            // Initialize optimized components
            databaseManager = new OptimizedDatabaseManager("jdbc:sqlite:test_performance.db");
            translationManager = new OptimizedTranslationManager();
            asyncProcessor = new OptimizedAsyncProcessor();
            objectPoolManager = new ObjectPoolManager();
            commandRegistry = new OptimizedCommandRegistry(translationManager);
            
            // Mock services for testing
            TicketService ticketService = new TicketService(databaseManager);
            UserLanguageManager userLanguageManager = new UserLanguageManager(databaseManager);
            
            ticketManager = new OptimizedTicketManager(
                databaseManager,
                translationManager,
                asyncProcessor,
                objectPoolManager,
                commandRegistry,
                ticketService,
                userLanguageManager
            );
            
            logger.info("Performance test environment initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to setup performance test environment", e);
            throw new RuntimeException(e);
        }
    }
    
    @AfterAll
    static void teardownPerformanceTest() {
        logger.info("Tearing down performance test environment...");
        
        try {
            if (asyncProcessor != null) {
                asyncProcessor.shutdown();
            }
            if (databaseManager != null) {
                databaseManager.close();
            }
            
            logger.info("Performance test environment cleaned up successfully");
            
        } catch (Exception e) {
            logger.error("Error during performance test cleanup", e);
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Database Connection Pool Performance Test")
    void testDatabaseConnectionPoolPerformance() {
        logger.info("Starting database connection pool performance test...");
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try (var connection = databaseManager.getConnection()) {
                connection.createStatement().execute("SELECT 1");
            } catch (Exception e) {
                logger.warn("Warmup iteration {} failed", i, e);
            }
        }
        
        // Performance test
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        List<Future<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                    long operationStart = System.nanoTime();
                    
                    try (var connection = databaseManager.getConnection()) {
                        // Simulate database operation
                        var stmt = connection.createStatement();
                        var rs = stmt.executeQuery("SELECT COUNT(*) FROM tickets");
                        rs.next();
                        rs.getInt(1);
                        
                        long operationTime = System.nanoTime() - operationStart;
                        totalTime.addAndGet(operationTime);
                        successCount.incrementAndGet();
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        logger.warn("Database operation failed", e);
                    }
                }
                return null;
            }));
        }
        
        // Wait for completion
        futures.forEach(future -> {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Future execution failed", e);
            }
        });
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        // Calculate metrics
        long totalTestTime = endTime - startTime;
        double avgOperationTime = totalTime.get() / (double) successCount.get() / 1_000_000; // Convert to ms
        double operationsPerSecond = (successCount.get() * 1000.0) / totalTestTime;
        double errorRate = (errorCount.get() * 100.0) / (successCount.get() + errorCount.get());
        
        logger.info("=== Database Connection Pool Performance Results ===");
        logger.info("Total operations: {}", successCount.get() + errorCount.get());
        logger.info("Successful operations: {}", successCount.get());
        logger.info("Failed operations: {}", errorCount.get());
        logger.info("Error rate: {:.2f}%", errorRate);
        logger.info("Average operation time: {:.2f}ms", avgOperationTime);
        logger.info("Operations per second: {:.2f}", operationsPerSecond);
        logger.info("Total test time: {}ms", totalTestTime);
        logger.info("Active connections: {}", databaseManager.getActiveConnections());
        logger.info("Idle connections: {}", databaseManager.getIdleConnections());
        
        // Assertions
        Assertions.assertTrue(errorRate < 5.0, "Error rate should be less than 5%");
        Assertions.assertTrue(avgOperationTime < 100.0, "Average operation time should be less than 100ms");
        Assertions.assertTrue(operationsPerSecond > 50.0, "Should handle at least 50 operations per second");
    }
    
    @Test
    @Order(2)
    @DisplayName("Translation Cache Performance Test")
    void testTranslationCachePerformance() {
        logger.info("Starting translation cache performance test...");
        
        String[] testKeys = {
            "ticket.welcome.title",
            "ticket.welcome.description", 
            "ticket.close.title",
            "ticket.assign.title",
            "command.ping.response",
            "error.permission.denied",
            "success.operation.completed"
        };
        
        String[] languages = {"en", "da", "de", "fr", "es"};
        
        // Warmup cache
        for (String key : testKeys) {
            for (String lang : languages) {
                translationManager.translate(key, lang);
            }
        }
        
        // Performance test
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger cacheHits = new AtomicInteger(0);
        AtomicInteger cacheMisses = new AtomicInteger(0);
        
        List<Future<Void>> futures = new ArrayList<>();
        Random random = new Random();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                    long operationStart = System.nanoTime();
                    
                    String key = testKeys[random.nextInt(testKeys.length)];
                    String lang = languages[random.nextInt(languages.length)];
                    
                    String result = translationManager.translate(key, lang);
                    
                    long operationTime = System.nanoTime() - operationStart;
                    totalTime.addAndGet(operationTime);
                    
                    if (result != null && !result.equals(key)) {
                        cacheHits.incrementAndGet();
                    } else {
                        cacheMisses.incrementAndGet();
                    }
                }
                return null;
            }));
        }
        
        // Wait for completion
        futures.forEach(future -> {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Future execution failed", e);
            }
        });
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        // Calculate metrics
        long totalTestTime = endTime - startTime;
        double avgOperationTime = totalTime.get() / (double) (cacheHits.get() + cacheMisses.get()) / 1_000_000;
        double cacheHitRate = (cacheHits.get() * 100.0) / (cacheHits.get() + cacheMisses.get());
        double operationsPerSecond = ((cacheHits.get() + cacheMisses.get()) * 1000.0) / totalTestTime;
        
        logger.info("=== Translation Cache Performance Results ===");
        logger.info("Total translations: {}", cacheHits.get() + cacheMisses.get());
        logger.info("Cache hits: {}", cacheHits.get());
        logger.info("Cache misses: {}", cacheMisses.get());
        logger.info("Cache hit rate: {:.2f}%", cacheHitRate);
        logger.info("Average operation time: {:.4f}ms", avgOperationTime);
        logger.info("Operations per second: {:.2f}", operationsPerSecond);
        logger.info("Cache size: {}", translationManager.getCacheSize());
        
        // Assertions
        Assertions.assertTrue(cacheHitRate > 80.0, "Cache hit rate should be above 80%");
        Assertions.assertTrue(avgOperationTime < 1.0, "Average translation time should be less than 1ms");
        Assertions.assertTrue(operationsPerSecond > 1000.0, "Should handle at least 1000 translations per second");
    }
    
    @Test
    @Order(3)
    @DisplayName("Async Processing Performance Test")
    void testAsyncProcessingPerformance() {
        logger.info("Starting async processing performance test...");
        
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger failedTasks = new AtomicInteger(0);
        AtomicLong totalExecutionTime = new AtomicLong(0);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Submit tasks to different thread pools
        for (int i = 0; i < CONCURRENT_USERS * OPERATIONS_PER_USER; i++) {
            final int taskId = i;
            
            CompletableFuture<Void> future = asyncProcessor.submitGeneralTask(() -> {
                long taskStart = System.nanoTime();
                
                try {
                    // Simulate work
                    Thread.sleep(10 + (taskId % 50)); // 10-60ms of work
                    
                    long taskTime = System.nanoTime() - taskStart;
                    totalExecutionTime.addAndGet(taskTime);
                    completedTasks.incrementAndGet();
                    
                } catch (Exception e) {
                    failedTasks.incrementAndGet();
                    logger.warn("Task {} failed", taskId, e);
                }
                
                return null;
            });
            
            futures.add(future);
        }
        
        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .orTimeout(60, TimeUnit.SECONDS)
            .join();
        
        long endTime = System.currentTimeMillis();
        
        // Calculate metrics
        long totalTestTime = endTime - startTime;
        double avgTaskTime = totalExecutionTime.get() / (double) completedTasks.get() / 1_000_000;
        double tasksPerSecond = (completedTasks.get() * 1000.0) / totalTestTime;
        double failureRate = (failedTasks.get() * 100.0) / (completedTasks.get() + failedTasks.get());
        
        logger.info("=== Async Processing Performance Results ===");
        logger.info("Total tasks: {}", completedTasks.get() + failedTasks.get());
        logger.info("Completed tasks: {}", completedTasks.get());
        logger.info("Failed tasks: {}", failedTasks.get());
        logger.info("Failure rate: {:.2f}%", failureRate);
        logger.info("Average task execution time: {:.2f}ms", avgTaskTime);
        logger.info("Tasks per second: {:.2f}", tasksPerSecond);
        logger.info("Total test time: {}ms", totalTestTime);
        
        // Assertions
        Assertions.assertTrue(failureRate < 1.0, "Failure rate should be less than 1%");
        Assertions.assertTrue(tasksPerSecond > 100.0, "Should handle at least 100 tasks per second");
        Assertions.assertTrue(avgTaskTime < 100.0, "Average task time should be reasonable");
    }
    
    @Test
    @Order(4)
    @DisplayName("Object Pool Performance Test")
    void testObjectPoolPerformance() {
        logger.info("Starting object pool performance test...");
        
        AtomicInteger borrowOperations = new AtomicInteger(0);
        AtomicInteger returnOperations = new AtomicInteger(0);
        AtomicLong totalBorrowTime = new AtomicLong(0);
        AtomicLong totalReturnTime = new AtomicLong(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<Future<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                    // Test StringBuilder pool
                    long borrowStart = System.nanoTime();
                    StringBuilder sb = objectPoolManager.borrowStringBuilder();
                    long borrowTime = System.nanoTime() - borrowStart;
                    totalBorrowTime.addAndGet(borrowTime);
                    borrowOperations.incrementAndGet();
                    
                    // Use the StringBuilder
                    sb.append("Test string ").append(j).append(" for performance testing");
                    
                    // Return to pool
                    long returnStart = System.nanoTime();
                    objectPoolManager.returnStringBuilder(sb);
                    long returnTime = System.nanoTime() - returnStart;
                    totalReturnTime.addAndGet(returnTime);
                    returnOperations.incrementAndGet();
                }
                return null;
            }));
        }
        
        // Wait for completion
        futures.forEach(future -> {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Future execution failed", e);
            }
        });
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        // Calculate metrics
        long totalTestTime = endTime - startTime;
        double avgBorrowTime = totalBorrowTime.get() / (double) borrowOperations.get() / 1_000_000;
        double avgReturnTime = totalReturnTime.get() / (double) returnOperations.get() / 1_000_000;
        double operationsPerSecond = ((borrowOperations.get() + returnOperations.get()) * 1000.0) / totalTestTime;
        
        logger.info("=== Object Pool Performance Results ===");
        logger.info("Borrow operations: {}", borrowOperations.get());
        logger.info("Return operations: {}", returnOperations.get());
        logger.info("Average borrow time: {:.4f}ms", avgBorrowTime);
        logger.info("Average return time: {:.4f}ms", avgReturnTime);
        logger.info("Operations per second: {:.2f}", operationsPerSecond);
        logger.info("StringBuilder pool size: {}", objectPoolManager.getPoolSize("StringBuilder"));
        logger.info("StringBuilder pool hits: {}", objectPoolManager.getPoolHits("StringBuilder"));
        logger.info("StringBuilder pool misses: {}", objectPoolManager.getPoolMisses("StringBuilder"));
        
        // Assertions
        Assertions.assertTrue(avgBorrowTime < 0.1, "Borrow time should be very fast (< 0.1ms)");
        Assertions.assertTrue(avgReturnTime < 0.1, "Return time should be very fast (< 0.1ms)");
        Assertions.assertTrue(operationsPerSecond > 10000.0, "Should handle at least 10k operations per second");
    }
    
    @Test
    @Order(5)
    @DisplayName("End-to-End Performance Test")
    void testEndToEndPerformance() {
        logger.info("Starting end-to-end performance test...");
        
        // This test simulates a complete ticket workflow
        AtomicInteger successfulWorkflows = new AtomicInteger(0);
        AtomicInteger failedWorkflows = new AtomicInteger(0);
        AtomicLong totalWorkflowTime = new AtomicLong(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(10); // Reduced concurrency for E2E test
        List<Future<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 20; i++) { // Reduced iterations for E2E test
            final int workflowId = i;
            
            futures.add(executor.submit(() -> {
                long workflowStart = System.nanoTime();
                
                try {
                    // Simulate complete ticket workflow
                    // 1. Translation lookup
                    String title = translationManager.translate("ticket.welcome.title", "en");
                    
                    // 2. Database operations
                    try (var connection = databaseManager.getConnection()) {
                        var stmt = connection.prepareStatement("SELECT COUNT(*) FROM tickets WHERE user_id = ?");
                        stmt.setString(1, "user_" + workflowId);
                        stmt.executeQuery();
                    }
                    
                    // 3. Object pool usage
                    StringBuilder sb = objectPoolManager.borrowStringBuilder();
                    sb.append("Ticket workflow ").append(workflowId);
                    objectPoolManager.returnStringBuilder(sb);
                    
                    // 4. Async processing
                    CompletableFuture<String> asyncResult = asyncProcessor.submitGeneralTask(() -> {
                        return "Async result for workflow " + workflowId;
                    });
                    
                    asyncResult.get(5, TimeUnit.SECONDS);
                    
                    long workflowTime = System.nanoTime() - workflowStart;
                    totalWorkflowTime.addAndGet(workflowTime);
                    successfulWorkflows.incrementAndGet();
                    
                } catch (Exception e) {
                    failedWorkflows.incrementAndGet();
                    logger.warn("Workflow {} failed", workflowId, e);
                }
                
                return null;
            }));
        }
        
        // Wait for completion
        futures.forEach(future -> {
            try {
                future.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Future execution failed", e);
            }
        });
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        // Calculate metrics
        long totalTestTime = endTime - startTime;
        double avgWorkflowTime = totalWorkflowTime.get() / (double) successfulWorkflows.get() / 1_000_000;
        double workflowsPerSecond = (successfulWorkflows.get() * 1000.0) / totalTestTime;
        double failureRate = (failedWorkflows.get() * 100.0) / (successfulWorkflows.get() + failedWorkflows.get());
        
        logger.info("=== End-to-End Performance Results ===");
        logger.info("Total workflows: {}", successfulWorkflows.get() + failedWorkflows.get());
        logger.info("Successful workflows: {}", successfulWorkflows.get());
        logger.info("Failed workflows: {}", failedWorkflows.get());
        logger.info("Failure rate: {:.2f}%", failureRate);
        logger.info("Average workflow time: {:.2f}ms", avgWorkflowTime);
        logger.info("Workflows per second: {:.2f}", workflowsPerSecond);
        logger.info("Total test time: {}ms", totalTestTime);
        
        // Final system metrics
        logger.info("\n=== Final System Metrics ===");
        logger.info("Database - Active: {}, Idle: {}, Total: {}", 
            databaseManager.getActiveConnections(),
            databaseManager.getIdleConnections(),
            databaseManager.getTotalConnections());
        logger.info("Translation Cache - Size: {}, Hit Rate: {:.2f}%", 
            translationManager.getCacheSize(),
            translationManager.getCacheHitRate() * 100);
        logger.info("Object Pools - StringBuilder: {}", 
            objectPoolManager.getPoolSize("StringBuilder"));
        
        // Assertions
        Assertions.assertTrue(failureRate < 5.0, "E2E failure rate should be less than 5%");
        Assertions.assertTrue(avgWorkflowTime < 1000.0, "Average workflow time should be less than 1 second");
        Assertions.assertTrue(workflowsPerSecond > 5.0, "Should handle at least 5 workflows per second");
    }
}