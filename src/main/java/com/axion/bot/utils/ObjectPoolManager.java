package com.axion.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Object Pool Manager for efficient memory management and reduced GC overhead
 * Provides pooling for frequently used objects like StringBuilder, MessageEmbed.Builder, etc.
 */
public class ObjectPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ObjectPoolManager.class);
    
    // Pool registry
    private final Map<Class<?>, ObjectPool<?>> pools = new ConcurrentHashMap<>();
    
    // Global metrics
    private final AtomicLong totalBorrows = new AtomicLong(0);
    private final AtomicLong totalReturns = new AtomicLong(0);
    private final AtomicLong totalCreations = new AtomicLong(0);
    
    // Singleton instance
    private static volatile ObjectPoolManager instance;
    private static final Object lock = new Object();
    
    private ObjectPoolManager() {
        initializeCommonPools();
        logger.info("✅ Object Pool Manager initialized with {} pools", pools.size());
    }
    
    /**
     * Get singleton instance
     */
    public static ObjectPoolManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ObjectPoolManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize commonly used object pools
     */
    private void initializeCommonPools() {
        // StringBuilder pool for string operations
        createPool(StringBuilder.class, 
            () -> new StringBuilder(256), 
            sb -> sb.setLength(0), 
            50, 200);
        
        // StringBuffer pool for thread-safe string operations
        createPool(StringBuffer.class, 
            () -> new StringBuffer(256), 
            sb -> sb.setLength(0), 
            20, 100);
        
        logger.debug("📦 Initialized common object pools");
    }
    
    /**
     * Create a new object pool
     */
    public <T> ObjectPool<T> createPool(Class<T> type, 
                                       Supplier<T> factory, 
                                       Consumer<T> resetFunction, 
                                       int initialSize, 
                                       int maxSize) {
        ObjectPool<T> pool = new ObjectPool<>(type, factory, resetFunction, initialSize, maxSize);
        pools.put(type, pool);
        logger.debug("🏗️ Created object pool for {} (initial={}, max={})", 
                type.getSimpleName(), initialSize, maxSize);
        return pool;
    }
    
    /**
     * Get object pool for a specific type
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectPool<T> getPool(Class<T> type) {
        return (ObjectPool<T>) pools.get(type);
    }
    
    /**
     * Borrow an object from the pool
     */
    public <T> T borrow(Class<T> type) {
        ObjectPool<T> pool = getPool(type);
        if (pool != null) {
            totalBorrows.incrementAndGet();
            return pool.borrow();
        }
        
        logger.warn("⚠️ No pool found for type {}, creating new instance", type.getSimpleName());
        totalCreations.incrementAndGet();
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + type.getSimpleName(), e);
        }
    }
    
    /**
     * Return an object to the pool
     */
    public <T> void returnObject(Class<T> type, T object) {
        ObjectPool<T> pool = getPool(type);
        if (pool != null) {
            totalReturns.incrementAndGet();
            pool.returnObject(object);
        }
    }
    
    /**
     * Convenient method to use an object from pool with automatic return
     */
    public <T, R> R usePooled(Class<T> type, java.util.function.Function<T, R> action) {
        T object = borrow(type);
        try {
            return action.apply(object);
        } finally {
            returnObject(type, object);
        }
    }
    
    /**
     * Convenient method to use an object from pool without return value
     */
    public <T> void usePooled(Class<T> type, Consumer<T> action) {
        T object = borrow(type);
        try {
            action.accept(object);
        } finally {
            returnObject(type, object);
        }
    }
    
    /**
     * Get pool statistics
     */
    public PoolManagerMetrics getMetrics() {
        Map<String, PoolMetrics> poolMetrics = new ConcurrentHashMap<>();
        
        pools.forEach((type, pool) -> {
            poolMetrics.put(type.getSimpleName(), pool.getMetrics());
        });
        
        return new PoolManagerMetrics(
            totalBorrows.get(),
            totalReturns.get(),
            totalCreations.get(),
            pools.size(),
            poolMetrics
        );
    }
    
    /**
     * Log performance statistics
     */
    public void logPerformanceStats() {
        PoolManagerMetrics metrics = getMetrics();
        logger.info("📊 Object Pool Manager Performance: {}", metrics);
        
        // Log individual pool stats
        metrics.poolMetrics.forEach((name, poolMetrics) -> {
            logger.info("📦 Pool {}: {}", name, poolMetrics);
        });
    }
    
    /**
     * Clear all pools (useful for testing or memory cleanup)
     */
    public void clearAllPools() {
        pools.values().forEach(ObjectPool::clear);
        logger.info("🧹 All object pools cleared");
    }
    
    /**
     * Shutdown and cleanup
     */
    public void shutdown() {
        clearAllPools();
        pools.clear();
        logger.info("🛑 Object Pool Manager shutdown completed");
    }
    
    /**
     * Individual object pool implementation
     */
    public static class ObjectPool<T> {
        private final Class<T> type;
        private final Supplier<T> factory;
        private final Consumer<T> resetFunction;
        private final int maxSize;
        private final ConcurrentLinkedQueue<T> pool;
        
        // Metrics
        private final AtomicInteger currentSize = new AtomicInteger(0);
        private final AtomicLong borrowCount = new AtomicLong(0);
        private final AtomicLong returnCount = new AtomicLong(0);
        private final AtomicLong createCount = new AtomicLong(0);
        private final AtomicLong resetCount = new AtomicLong(0);
        
        public ObjectPool(Class<T> type, Supplier<T> factory, Consumer<T> resetFunction, 
                         int initialSize, int maxSize) {
            this.type = type;
            this.factory = factory;
            this.resetFunction = resetFunction;
            this.maxSize = maxSize;
            this.pool = new ConcurrentLinkedQueue<>();
            
            // Pre-populate pool
            for (int i = 0; i < initialSize; i++) {
                T object = factory.get();
                pool.offer(object);
                currentSize.incrementAndGet();
                createCount.incrementAndGet();
            }
        }
        
        /**
         * Borrow an object from the pool
         */
        public T borrow() {
            borrowCount.incrementAndGet();
            
            T object = pool.poll();
            if (object != null) {
                currentSize.decrementAndGet();
                return object;
            }
            
            // Create new object if pool is empty
            createCount.incrementAndGet();
            return factory.get();
        }
        
        /**
         * Return an object to the pool
         */
        public void returnObject(T object) {
            if (object == null) {
                return;
            }
            
            returnCount.incrementAndGet();
            
            // Reset object state if reset function is provided
            if (resetFunction != null) {
                try {
                    resetFunction.accept(object);
                    resetCount.incrementAndGet();
                } catch (Exception e) {
                    logger.warn("⚠️ Failed to reset object of type {}: {}", 
                            type.getSimpleName(), e.getMessage());
                    return; // Don't return to pool if reset failed
                }
            }
            
            // Only return to pool if under max size
            if (currentSize.get() < maxSize) {
                pool.offer(object);
                currentSize.incrementAndGet();
            }
        }
        
        /**
         * Get current pool size
         */
        public int size() {
            return currentSize.get();
        }
        
        /**
         * Check if pool is empty
         */
        public boolean isEmpty() {
            return pool.isEmpty();
        }
        
        /**
         * Clear the pool
         */
        public void clear() {
            pool.clear();
            currentSize.set(0);
        }
        
        /**
         * Get pool metrics
         */
        public PoolMetrics getMetrics() {
            return new PoolMetrics(
                type.getSimpleName(),
                currentSize.get(),
                maxSize,
                borrowCount.get(),
                returnCount.get(),
                createCount.get(),
                resetCount.get()
            );
        }
    }
    
    /**
     * Pool metrics for individual pools
     */
    public static class PoolMetrics {
        public final String typeName;
        public final int currentSize;
        public final int maxSize;
        public final long borrowCount;
        public final long returnCount;
        public final long createCount;
        public final long resetCount;
        
        public PoolMetrics(String typeName, int currentSize, int maxSize, 
                          long borrowCount, long returnCount, long createCount, long resetCount) {
            this.typeName = typeName;
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.borrowCount = borrowCount;
            this.returnCount = returnCount;
            this.createCount = createCount;
            this.resetCount = resetCount;
        }
        
        public double getHitRate() {
            return borrowCount > 0 ? (double) (borrowCount - createCount) / borrowCount * 100 : 0;
        }
        
        public double getUtilization() {
            return maxSize > 0 ? (double) currentSize / maxSize * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PoolMetrics{type=%s, size=%d/%d (%.1f%%), borrows=%d, returns=%d, " +
                "creates=%d, resets=%d, hitRate=%.1f%%}",
                typeName, currentSize, maxSize, getUtilization(), borrowCount, returnCount,
                createCount, resetCount, getHitRate()
            );
        }
    }
    
    /**
     * Overall pool manager metrics
     */
    public static class PoolManagerMetrics {
        public final long totalBorrows;
        public final long totalReturns;
        public final long totalCreations;
        public final int totalPools;
        public final Map<String, PoolMetrics> poolMetrics;
        
        public PoolManagerMetrics(long totalBorrows, long totalReturns, long totalCreations,
                                 int totalPools, Map<String, PoolMetrics> poolMetrics) {
            this.totalBorrows = totalBorrows;
            this.totalReturns = totalReturns;
            this.totalCreations = totalCreations;
            this.totalPools = totalPools;
            this.poolMetrics = poolMetrics;
        }
        
        public double getOverallHitRate() {
            return totalBorrows > 0 ? (double) (totalBorrows - totalCreations) / totalBorrows * 100 : 0;
        }
        
        public double getReturnRate() {
            return totalBorrows > 0 ? (double) totalReturns / totalBorrows * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PoolManagerMetrics{pools=%d, borrows=%d, returns=%d, creates=%d, " +
                "hitRate=%.1f%%, returnRate=%.1f%%}",
                totalPools, totalBorrows, totalReturns, totalCreations,
                getOverallHitRate(), getReturnRate()
            );
        }
    }
    
    // Utility methods for common operations
    
    /**
     * Get a StringBuilder from pool
     */
    public static StringBuilder getStringBuilder() {
        return getInstance().borrow(StringBuilder.class);
    }
    
    /**
     * Return a StringBuilder to pool
     */
    public static void returnStringBuilder(StringBuilder sb) {
        getInstance().returnObject(StringBuilder.class, sb);
    }
    
    /**
     * Use a StringBuilder with automatic return
     */
    public static String buildString(Consumer<StringBuilder> builder) {
        return getInstance().usePooled(StringBuilder.class, sb -> {
            builder.accept(sb);
            return sb.toString();
        });
    }
    
    /**
     * Get a StringBuffer from pool
     */
    public static StringBuffer getStringBuffer() {
        return getInstance().borrow(StringBuffer.class);
    }
    
    /**
     * Return a StringBuffer to pool
     */
    public static void returnStringBuffer(StringBuffer sb) {
        getInstance().returnObject(StringBuffer.class, sb);
    }
}