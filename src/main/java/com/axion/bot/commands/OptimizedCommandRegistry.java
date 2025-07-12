package com.axion.bot.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Optimized Command Registry with performance monitoring and smart caching
 * Provides significant performance improvements over traditional command handling
 */
public class OptimizedCommandRegistry {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedCommandRegistry.class);
    
    // Command storage
    private final Map<String, CommandHandler> commands = new ConcurrentHashMap<>();
    private final Map<String, CommandMetadata> commandMetadata = new ConcurrentHashMap<>();
    private final Map<CommandCategory, Set<String>> categorizedCommands = new ConcurrentHashMap<>();
    
    // Performance caching
    private final Cache<String, CommandHandler> handlerCache;
    private final Cache<String, Boolean> permissionCache;
    
    // Performance metrics
    private final AtomicLong commandExecutions = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong permissionChecks = new AtomicLong(0);
    private final Map<String, AtomicLong> commandUsageStats = new ConcurrentHashMap<>();
    
    // Command execution hooks
    private final List<Consumer<CommandExecutionContext>> preExecutionHooks = new ArrayList<>();
    private final List<Consumer<CommandExecutionContext>> postExecutionHooks = new ArrayList<>();
    
    public OptimizedCommandRegistry() {
        // Initialize caches with optimal settings
        this.handlerCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .build();
        
        this.permissionCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
        
        // Initialize command categories
        for (CommandCategory category : CommandCategory.values()) {
            categorizedCommands.put(category, ConcurrentHashMap.newKeySet());
        }
        
        logger.info("✅ Optimized Command Registry initialized");
    }
    
    /**
     * Register a command with metadata
     */
    public void registerCommand(String name, CommandHandler handler, CommandMetadata metadata) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be null or empty");
        }
        
        if (handler == null) {
            throw new IllegalArgumentException("Command handler cannot be null");
        }
        
        String normalizedName = name.toLowerCase().trim();
        
        // Check for duplicate registration
        if (commands.containsKey(normalizedName)) {
            logger.warn("⚠️ Overriding existing command: {}", normalizedName);
        }
        
        commands.put(normalizedName, handler);
        commandMetadata.put(normalizedName, metadata != null ? metadata : new CommandMetadata());
        
        // Add to category
        CommandCategory category = metadata != null ? metadata.getCategory() : CommandCategory.GENERAL;
        categorizedCommands.get(category).add(normalizedName);
        
        // Initialize usage stats
        commandUsageStats.put(normalizedName, new AtomicLong(0));
        
        logger.debug("📝 Registered command '{}' in category {}", normalizedName, category);
    }
    
    /**
     * Register a simple command without metadata
     */
    public void registerCommand(String name, CommandHandler handler) {
        registerCommand(name, handler, new CommandMetadata());
    }
    
    /**
     * Execute a command with performance monitoring
     */
    public boolean executeCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName().toLowerCase();
        commandExecutions.incrementAndGet();
        
        // Try cache first
        CommandHandler handler = handlerCache.getIfPresent(commandName);
        if (handler != null) {
            cacheHits.incrementAndGet();
        } else {
            cacheMisses.incrementAndGet();
            handler = commands.get(commandName);
            if (handler != null) {
                handlerCache.put(commandName, handler);
            }
        }
        
        if (handler == null) {
            logger.debug("❓ Unknown command: {}", commandName);
            return false;
        }
        
        // Update usage stats
        commandUsageStats.get(commandName).incrementAndGet();
        
        // Create execution context
        CommandExecutionContext context = new CommandExecutionContext(
            event, commandName, getCommandMetadata(commandName)
        );
        
        try {
            // Pre-execution hooks
            for (Consumer<CommandExecutionContext> hook : preExecutionHooks) {
                hook.accept(context);
            }
            
            // Check permissions
            if (!checkPermissions(context)) {
                logger.debug("🚫 Permission denied for command '{}' by user {}", 
                        commandName, event.getUser().getId());
                return false;
            }
            
            // Execute command
            long startTime = System.currentTimeMillis();
            handler.handle(event);
            long executionTime = System.currentTimeMillis() - startTime;
            
            context.setExecutionTime(executionTime);
            context.setSuccess(true);
            
            logger.debug("✅ Command '{}' executed successfully in {}ms", commandName, executionTime);
            
        } catch (Exception e) {
            context.setSuccess(false);
            context.setError(e);
            logger.error("❌ Error executing command '{}'", commandName, e);
        } finally {
            // Post-execution hooks
            for (Consumer<CommandExecutionContext> hook : postExecutionHooks) {
                hook.accept(context);
            }
        }
        
        return true;
    }
    
    /**
     * Check command permissions with caching
     */
    private boolean checkPermissions(CommandExecutionContext context) {
        permissionChecks.incrementAndGet();
        
        CommandMetadata metadata = context.getMetadata();
        if (metadata.getRequiredPermissions().isEmpty()) {
            return true; // No permissions required
        }
        
        String userId = context.getEvent().getUser().getId();
        String guildId = context.getEvent().getGuild() != null ? 
                context.getEvent().getGuild().getId() : "dm";
        
        String permissionKey = String.format("%s:%s:%s", 
                context.getCommandName(), userId, guildId);
        
        // Check cache first
        Boolean cached = permissionCache.getIfPresent(permissionKey);
        if (cached != null) {
            return cached;
        }
        
        // Perform permission check
        boolean hasPermission = performPermissionCheck(context);
        
        // Cache result
        permissionCache.put(permissionKey, hasPermission);
        
        return hasPermission;
    }
    
    /**
     * Perform actual permission check
     */
    private boolean performPermissionCheck(CommandExecutionContext context) {
        // Implementation depends on your permission system
        // This is a placeholder that always returns true
        // You should implement your actual permission logic here
        return true;
    }
    
    /**
     * Get command metadata
     */
    public CommandMetadata getCommandMetadata(String commandName) {
        return commandMetadata.getOrDefault(commandName.toLowerCase(), new CommandMetadata());
    }
    
    /**
     * Get commands by category
     */
    public Set<String> getCommandsByCategory(CommandCategory category) {
        return new HashSet<>(categorizedCommands.getOrDefault(category, Collections.emptySet()));
    }
    
    /**
     * Get all registered commands
     */
    public Set<String> getAllCommands() {
        return new HashSet<>(commands.keySet());
    }
    
    /**
     * Check if command exists
     */
    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName.toLowerCase());
    }
    
    /**
     * Unregister a command
     */
    public boolean unregisterCommand(String commandName) {
        String normalizedName = commandName.toLowerCase();
        
        CommandHandler removed = commands.remove(normalizedName);
        if (removed != null) {
            commandMetadata.remove(normalizedName);
            commandUsageStats.remove(normalizedName);
            
            // Remove from categories
            for (Set<String> categoryCommands : categorizedCommands.values()) {
                categoryCommands.remove(normalizedName);
            }
            
            // Clear from caches
            handlerCache.invalidate(normalizedName);
            
            logger.debug("🗑️ Unregistered command: {}", normalizedName);
            return true;
        }
        
        return false;
    }
    
    /**
     * Add pre-execution hook
     */
    public void addPreExecutionHook(Consumer<CommandExecutionContext> hook) {
        preExecutionHooks.add(hook);
    }
    
    /**
     * Add post-execution hook
     */
    public void addPostExecutionHook(Consumer<CommandExecutionContext> hook) {
        postExecutionHooks.add(hook);
    }
    
    /**
     * Get command usage statistics
     */
    public Map<String, Long> getUsageStatistics() {
        Map<String, Long> stats = new HashMap<>();
        commandUsageStats.forEach((command, count) -> stats.put(command, count.get()));
        return stats;
    }
    
    /**
     * Get most used commands
     */
    public List<Map.Entry<String, Long>> getMostUsedCommands(int limit) {
        return commandUsageStats.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicLong>comparingByValue(
                        (a, b) -> Long.compare(b.get(), a.get())))
                .limit(limit)
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().get()))
                .toList();
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        handlerCache.invalidateAll();
        permissionCache.invalidateAll();
        logger.info("🧹 Command registry caches cleared");
    }
    
    /**
     * Get performance metrics
     */
    public CommandRegistryMetrics getMetrics() {
        return new CommandRegistryMetrics(
            commands.size(),
            commandExecutions.get(),
            cacheHits.get(),
            cacheMisses.get(),
            permissionChecks.get(),
            handlerCache.stats().hitRate(),
            permissionCache.stats().hitRate()
        );
    }
    
    /**
     * Log performance statistics
     */
    public void logPerformanceStats() {
        CommandRegistryMetrics metrics = getMetrics();
        logger.info("📊 Command Registry Performance: {}", metrics);
        
        // Log top commands
        List<Map.Entry<String, Long>> topCommands = getMostUsedCommands(5);
        if (!topCommands.isEmpty()) {
            logger.info("🏆 Top Commands: {}", topCommands);
        }
    }
    
    /**
     * Command handler interface
     */
    @FunctionalInterface
    public interface CommandHandler {
        void handle(SlashCommandInteractionEvent event) throws Exception;
    }
    
    /**
     * Command categories
     */
    public enum CommandCategory {
        GENERAL,
        MODERATION,
        UTILITY,
        FUN,
        MUSIC,
        ADMIN,
        DEVELOPER,
        TICKET,
        ECONOMY,
        SOCIAL
    }
    
    /**
     * Command metadata
     */
    public static class CommandMetadata {
        private String description = "No description provided";
        private CommandCategory category = CommandCategory.GENERAL;
        private Set<String> requiredPermissions = new HashSet<>();
        private boolean adminOnly = false;
        private boolean guildOnly = true;
        private long cooldownMs = 0;
        
        // Getters and setters
        public String getDescription() { return description; }
        public CommandMetadata setDescription(String description) { 
            this.description = description; return this; 
        }
        
        public CommandCategory getCategory() { return category; }
        public CommandMetadata setCategory(CommandCategory category) { 
            this.category = category; return this; 
        }
        
        public Set<String> getRequiredPermissions() { return requiredPermissions; }
        public CommandMetadata setRequiredPermissions(Set<String> permissions) { 
            this.requiredPermissions = permissions; return this; 
        }
        
        public boolean isAdminOnly() { return adminOnly; }
        public CommandMetadata setAdminOnly(boolean adminOnly) { 
            this.adminOnly = adminOnly; return this; 
        }
        
        public boolean isGuildOnly() { return guildOnly; }
        public CommandMetadata setGuildOnly(boolean guildOnly) { 
            this.guildOnly = guildOnly; return this; 
        }
        
        public long getCooldownMs() { return cooldownMs; }
        public CommandMetadata setCooldownMs(long cooldownMs) { 
            this.cooldownMs = cooldownMs; return this; 
        }
    }
    
    /**
     * Command execution context
     */
    public static class CommandExecutionContext {
        private final SlashCommandInteractionEvent event;
        private final String commandName;
        private final CommandMetadata metadata;
        private long executionTime;
        private boolean success;
        private Exception error;
        
        public CommandExecutionContext(SlashCommandInteractionEvent event, 
                                     String commandName, CommandMetadata metadata) {
            this.event = event;
            this.commandName = commandName;
            this.metadata = metadata;
        }
        
        // Getters and setters
        public SlashCommandInteractionEvent getEvent() { return event; }
        public String getCommandName() { return commandName; }
        public CommandMetadata getMetadata() { return metadata; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Exception getError() { return error; }
        public void setError(Exception error) { this.error = error; }
    }
    
    /**
     * Performance metrics
     */
    public static class CommandRegistryMetrics {
        public final int totalCommands;
        public final long totalExecutions;
        public final long cacheHits;
        public final long cacheMisses;
        public final long permissionChecks;
        public final double handlerCacheHitRate;
        public final double permissionCacheHitRate;
        
        public CommandRegistryMetrics(int totalCommands, long totalExecutions, 
                                    long cacheHits, long cacheMisses, long permissionChecks,
                                    double handlerCacheHitRate, double permissionCacheHitRate) {
            this.totalCommands = totalCommands;
            this.totalExecutions = totalExecutions;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.permissionChecks = permissionChecks;
            this.handlerCacheHitRate = handlerCacheHitRate;
            this.permissionCacheHitRate = permissionCacheHitRate;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CommandRegistryMetrics{commands=%d, executions=%d, cacheHits=%d, " +
                "cacheMisses=%d, permissions=%d, handlerHitRate=%.2f%%, permissionHitRate=%.2f%%}",
                totalCommands, totalExecutions, cacheHits, cacheMisses, permissionChecks,
                handlerCacheHitRate * 100, permissionCacheHitRate * 100
            );
        }
    }
}