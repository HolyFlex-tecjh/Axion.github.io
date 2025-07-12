package com.axion.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Optimized Async Processor with thread pools and performance monitoring
 * Provides efficient handling of concurrent operations with proper resource management
 */
public class OptimizedAsyncProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedAsyncProcessor.class);
    
    // Thread pools for different types of operations
    private final ExecutorService generalExecutor;
    private final ExecutorService databaseExecutor;
    private final ExecutorService networkExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    
    // Performance metrics
    private final AtomicLong tasksSubmitted = new AtomicLong(0);
    private final AtomicLong tasksCompleted = new AtomicLong(0);
    private final AtomicLong tasksFailed = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    // Configuration
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 1000;
    
    public OptimizedAsyncProcessor() {
        // General purpose thread pool
        this.generalExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new NamedThreadFactory("General"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // Database operations thread pool (smaller, optimized for I/O)
        this.databaseExecutor = new ThreadPoolExecutor(
            Math.max(2, CORE_POOL_SIZE / 2),
            CORE_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY / 2),
            new NamedThreadFactory("Database"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // Network operations thread pool (optimized for I/O)
        this.networkExecutor = new ThreadPoolExecutor(
            Math.max(2, CORE_POOL_SIZE / 2),
            CORE_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY / 2),
            new NamedThreadFactory("Network"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // Scheduled operations
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            Math.max(1, CORE_POOL_SIZE / 4),
            new NamedThreadFactory("Scheduled")
        );
        
        logger.info("✅ Optimized Async Processor initialized with {} core threads", CORE_POOL_SIZE);
        logger.info("📊 Thread pools: General={}, Database={}, Network={}, Scheduled={}",
                getPoolInfo(generalExecutor), getPoolInfo(databaseExecutor),
                getPoolInfo(networkExecutor), ((ThreadPoolExecutor) scheduledExecutor).getCorePoolSize());
    }
    
    /**
     * Execute a general task asynchronously
     */
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return executeAsync(task, generalExecutor, "General");
    }
    
    /**
     * Execute a database operation asynchronously
     */
    public CompletableFuture<Void> executeDatabaseAsync(Runnable task) {
        return executeAsync(task, databaseExecutor, "Database");
    }
    
    /**
     * Execute a network operation asynchronously
     */
    public CompletableFuture<Void> executeNetworkAsync(Runnable task) {
        return executeAsync(task, networkExecutor, "Network");
    }
    
    /**
     * Execute a task with return value asynchronously
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return supplyAsync(supplier, generalExecutor, "General");
    }
    
    /**
     * Execute a database operation with return value asynchronously
     */
    public <T> CompletableFuture<T> supplyDatabaseAsync(Supplier<T> supplier) {
        return supplyAsync(supplier, databaseExecutor, "Database");
    }
    
    /**
     * Execute a network operation with return value asynchronously
     */
    public <T> CompletableFuture<T> supplyNetworkAsync(Supplier<T> supplier) {
        return supplyAsync(supplier, networkExecutor, "Network");
    }
    
    /**
     * Schedule a task to run after a delay
     */
    public ScheduledFuture<?> scheduleAsync(Runnable task, long delay, TimeUnit unit) {
        tasksSubmitted.incrementAndGet();
        
        return scheduledExecutor.schedule(() -> {
            long startTime = System.currentTimeMillis();
            try {
                task.run();
                tasksCompleted.incrementAndGet();
            } catch (Exception e) {
                tasksFailed.incrementAndGet();
                logger.error("❌ Scheduled task failed", e);
            } finally {
                totalExecutionTime.addAndGet(System.currentTimeMillis() - startTime);
            }
        }, delay, unit);
    }
    
    /**
     * Schedule a task to run at fixed rate
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, 
                                                  long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(() -> {
            tasksSubmitted.incrementAndGet();
            long startTime = System.currentTimeMillis();
            try {
                task.run();
                tasksCompleted.incrementAndGet();
            } catch (Exception e) {
                tasksFailed.incrementAndGet();
                logger.error("❌ Scheduled task failed", e);
            } finally {
                totalExecutionTime.addAndGet(System.currentTimeMillis() - startTime);
            }
        }, initialDelay, period, unit);
    }
    
    /**
     * Schedule a task to run with fixed delay
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, 
                                                     long delay, TimeUnit unit) {
        return scheduledExecutor.scheduleWithFixedDelay(() -> {
            tasksSubmitted.incrementAndGet();
            long startTime = System.currentTimeMillis();
            try {
                task.run();
                tasksCompleted.incrementAndGet();
            } catch (Exception e) {
                tasksFailed.incrementAndGet();
                logger.error("❌ Scheduled task failed", e);
            } finally {
                totalExecutionTime.addAndGet(System.currentTimeMillis() - startTime);
            }
        }, initialDelay, delay, unit);
    }
    
    /**
     * Execute multiple tasks in parallel and wait for all to complete
     */
    @SafeVarargs
    public final CompletableFuture<Void> executeAllAsync(Supplier<CompletableFuture<Void>>... tasks) {
        CompletableFuture<Void>[] futures = new CompletableFuture[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            futures[i] = tasks[i].get();
        }
        return CompletableFuture.allOf(futures);
    }
    
    /**
     * Execute multiple tasks and return the first completed result
     */
    @SafeVarargs
    public final <T> CompletableFuture<T> executeAnyAsync(Supplier<CompletableFuture<T>>... tasks) {
        CompletableFuture<T>[] futures = new CompletableFuture[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            futures[i] = tasks[i].get();
        }
        return CompletableFuture.anyOf(futures).thenApply(result -> (T) result);
    }
    
    /**
     * Execute a task with timeout
     */
    public <T> CompletableFuture<T> executeWithTimeout(Supplier<T> supplier, 
                                                      long timeout, TimeUnit unit) {
        CompletableFuture<T> future = supplyAsync(supplier);
        
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        scheduledExecutor.schedule(() -> {
            if (!future.isDone()) {
                timeoutFuture.completeExceptionally(
                    new TimeoutException("Task timed out after " + timeout + " " + unit)
                );
                future.cancel(true);
            }
        }, timeout, unit);
        
        return future.applyToEither(timeoutFuture, result -> result);
    }
    
    /**
     * Execute a task with retry logic
     */
    public <T> CompletableFuture<T> executeWithRetry(Supplier<T> supplier, int maxRetries, 
                                                    long retryDelay, TimeUnit unit) {
        return executeWithRetry(supplier, maxRetries, retryDelay, unit, 0);
    }
    
    private <T> CompletableFuture<T> executeWithRetry(Supplier<T> supplier, int maxRetries, 
                                                     long retryDelay, TimeUnit unit, int attempt) {
        return supplyAsync(supplier)
            .handle((result, throwable) -> {
                if (throwable == null) {
                    return CompletableFuture.completedFuture(result);
                }
                
                if (attempt >= maxRetries) {
                    CompletableFuture<T> failed = new CompletableFuture<>();
                    failed.completeExceptionally(throwable);
                    return failed;
                }
                
                logger.debug("🔄 Retrying task (attempt {}/{}) after error: {}", 
                        attempt + 1, maxRetries, throwable.getMessage());
                
                return scheduleAsync(() -> {}, retryDelay, unit)
                    .thenCompose(v -> executeWithRetry(supplier, maxRetries, retryDelay, unit, attempt + 1));
            })
            .thenCompose(future -> future);
    }
    
    /**
     * Execute a batch of tasks with controlled concurrency
     */
    public <T> CompletableFuture<Void> executeBatch(Iterable<Supplier<T>> tasks, 
                                                   int concurrency, 
                                                   Consumer<T> resultHandler) {
        Semaphore semaphore = new Semaphore(concurrency);
        CompletableFuture<Void> allTasks = CompletableFuture.completedFuture(null);
        
        for (Supplier<T> task : tasks) {
            allTasks = allTasks.thenCompose(v -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return CompletableFuture.failedFuture(e);
                }
                
                return supplyAsync(task)
                    .whenComplete((result, throwable) -> {
                        try {
                            if (throwable == null && resultHandler != null) {
                                resultHandler.accept(result);
                            }
                        } finally {
                            semaphore.release();
                        }
                    })
                    .thenApply(result -> null);
            });
        }
        
        return allTasks;
    }
    
    /**
     * Internal method to execute tasks with monitoring
     */
    private CompletableFuture<Void> executeAsync(Runnable task, ExecutorService executor, String poolName) {
        tasksSubmitted.incrementAndGet();
        
        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                task.run();
                tasksCompleted.incrementAndGet();
            } catch (Exception e) {
                tasksFailed.incrementAndGet();
                logger.error("❌ Task failed in {} pool", poolName, e);
                throw e;
            } finally {
                totalExecutionTime.addAndGet(System.currentTimeMillis() - startTime);
            }
        }, executor);
    }
    
    /**
     * Internal method to supply values with monitoring
     */
    private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, ExecutorService executor, String poolName) {
        tasksSubmitted.incrementAndGet();
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                T result = supplier.get();
                tasksCompleted.incrementAndGet();
                return result;
            } catch (Exception e) {
                tasksFailed.incrementAndGet();
                logger.error("❌ Task failed in {} pool", poolName, e);
                throw e;
            } finally {
                totalExecutionTime.addAndGet(System.currentTimeMillis() - startTime);
            }
        }, executor);
    }
    
    /**
     * Get thread pool information
     */
    private String getPoolInfo(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor tpe) {
            return String.format("%d/%d", tpe.getCorePoolSize(), tpe.getMaximumPoolSize());
        }
        return "N/A";
    }
    
    /**
     * Get performance metrics
     */
    public AsyncProcessorMetrics getMetrics() {
        ThreadPoolExecutor generalTpe = (ThreadPoolExecutor) generalExecutor;
        ThreadPoolExecutor databaseTpe = (ThreadPoolExecutor) databaseExecutor;
        ThreadPoolExecutor networkTpe = (ThreadPoolExecutor) networkExecutor;
        ThreadPoolExecutor scheduledTpe = (ThreadPoolExecutor) scheduledExecutor;
        
        return new AsyncProcessorMetrics(
            tasksSubmitted.get(),
            tasksCompleted.get(),
            tasksFailed.get(),
            totalExecutionTime.get(),
            generalTpe.getActiveCount(),
            databaseTpe.getActiveCount(),
            networkTpe.getActiveCount(),
            scheduledTpe.getActiveCount(),
            generalTpe.getQueue().size(),
            databaseTpe.getQueue().size(),
            networkTpe.getQueue().size(),
            scheduledTpe.getQueue().size()
        );
    }
    
    /**
     * Log performance statistics
     */
    public void logPerformanceStats() {
        AsyncProcessorMetrics metrics = getMetrics();
        logger.info("📊 Async Processor Performance: {}", metrics);
    }
    
    /**
     * Graceful shutdown
     */
    public void shutdown() {
        logger.info("🛑 Shutting down Async Processor...");
        
        shutdownExecutor(generalExecutor, "General");
        shutdownExecutor(databaseExecutor, "Database");
        shutdownExecutor(networkExecutor, "Network");
        shutdownExecutor(scheduledExecutor, "Scheduled");
        
        logger.info("✅ Async Processor shutdown completed");
    }
    
    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("⚠️ {} executor did not terminate gracefully, forcing shutdown", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("⚠️ Interrupted while waiting for {} executor shutdown", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Named thread factory for better debugging
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicLong threadNumber = new AtomicLong(1);
        
        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = "AxionBot-" + namePrefix + "-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
    
    /**
     * Performance metrics
     */
    public static class AsyncProcessorMetrics {
        public final long tasksSubmitted;
        public final long tasksCompleted;
        public final long tasksFailed;
        public final long totalExecutionTime;
        public final int generalActiveThreads;
        public final int databaseActiveThreads;
        public final int networkActiveThreads;
        public final int scheduledActiveThreads;
        public final int generalQueueSize;
        public final int databaseQueueSize;
        public final int networkQueueSize;
        public final int scheduledQueueSize;
        
        public AsyncProcessorMetrics(long tasksSubmitted, long tasksCompleted, long tasksFailed,
                                   long totalExecutionTime, int generalActiveThreads, 
                                   int databaseActiveThreads, int networkActiveThreads,
                                   int scheduledActiveThreads, int generalQueueSize,
                                   int databaseQueueSize, int networkQueueSize, int scheduledQueueSize) {
            this.tasksSubmitted = tasksSubmitted;
            this.tasksCompleted = tasksCompleted;
            this.tasksFailed = tasksFailed;
            this.totalExecutionTime = totalExecutionTime;
            this.generalActiveThreads = generalActiveThreads;
            this.databaseActiveThreads = databaseActiveThreads;
            this.networkActiveThreads = networkActiveThreads;
            this.scheduledActiveThreads = scheduledActiveThreads;
            this.generalQueueSize = generalQueueSize;
            this.databaseQueueSize = databaseQueueSize;
            this.networkQueueSize = networkQueueSize;
            this.scheduledQueueSize = scheduledQueueSize;
        }
        
        public double getSuccessRate() {
            return tasksSubmitted > 0 ? (double) tasksCompleted / tasksSubmitted * 100 : 0;
        }
        
        public double getAverageExecutionTime() {
            return tasksCompleted > 0 ? (double) totalExecutionTime / tasksCompleted : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "AsyncProcessorMetrics{submitted=%d, completed=%d, failed=%d, successRate=%.1f%%, " +
                "avgTime=%.1fms, active=[G:%d,D:%d,N:%d,S:%d], queued=[G:%d,D:%d,N:%d,S:%d]}",
                tasksSubmitted, tasksCompleted, tasksFailed, getSuccessRate(), getAverageExecutionTime(),
                generalActiveThreads, databaseActiveThreads, networkActiveThreads, scheduledActiveThreads,
                generalQueueSize, databaseQueueSize, networkQueueSize, scheduledQueueSize
            );
        }
    }
}