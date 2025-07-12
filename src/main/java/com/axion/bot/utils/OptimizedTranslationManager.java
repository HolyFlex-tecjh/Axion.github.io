package com.axion.bot.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optimized Translation Manager with smart caching and performance monitoring
 * Provides significant performance improvements over the original TranslationManager
 */
public class OptimizedTranslationManager {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedTranslationManager.class);
    
    // Cache configuration
    private static final int MAX_CACHE_SIZE = 10000;
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private static final Duration STATS_REFRESH = Duration.ofMinutes(5);
    
    // Language resources
    private final Map<String, Properties> languageProperties = new ConcurrentHashMap<>();
    private final Set<String> supportedLanguages = new HashSet<>();
    private final String defaultLanguage;
    
    // Smart caching with Caffeine
    private final Cache<String, String> translationCache;
    private final Cache<String, Properties> propertiesCache;
    
    // Performance metrics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong translationRequests = new AtomicLong(0);
    private final AtomicLong fallbackUsages = new AtomicLong(0);
    
    // Pattern for parameter replacement
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{(\\d+)\\}");
    
    public OptimizedTranslationManager(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        
        // Initialize caches with optimal settings
        this.translationCache = Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(CACHE_TTL)
                .recordStats()
                .build();
        
        this.propertiesCache = Caffeine.newBuilder()
                .maximumSize(50) // Fewer language files
                .expireAfterWrite(Duration.ofHours(6)) // Longer TTL for properties
                .recordStats()
                .build();
        
        // Load supported languages
        loadSupportedLanguages();
        
        // Preload default language
        loadLanguageProperties(defaultLanguage);
        
        logger.info("✅ Optimized Translation Manager initialized with {} supported languages", 
                supportedLanguages.size());
        logger.info("📊 Cache settings: Max size={}, TTL={}", MAX_CACHE_SIZE, CACHE_TTL);
    }
    
    /**
     * Get translation with smart caching and fallback support
     */
    public String translate(String language, String key, Object... params) {
        translationRequests.incrementAndGet();
        
        // Create cache key
        String cacheKey = createCacheKey(language, key, params);
        
        // Try cache first
        String cached = translationCache.getIfPresent(cacheKey);
        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached;
        }
        
        cacheMisses.incrementAndGet();
        
        // Get translation and cache it
        String translation = getTranslationInternal(language, key, params);
        translationCache.put(cacheKey, translation);
        
        return translation;
    }
    
    /**
     * Internal translation logic with fallback support
     */
    private String getTranslationInternal(String language, String key, Object... params) {
        // Normalize language code
        String normalizedLang = normalizeLanguageCode(language);
        
        // Try primary language
        String translation = getTranslationFromLanguage(normalizedLang, key);
        
        // Fallback to default language if not found
        if (translation == null && !normalizedLang.equals(defaultLanguage)) {
            translation = getTranslationFromLanguage(defaultLanguage, key);
            if (translation != null) {
                fallbackUsages.incrementAndGet();
                logger.debug("🔄 Used fallback translation for key '{}' from {} to {}", 
                        key, normalizedLang, defaultLanguage);
            }
        }
        
        // Final fallback to key itself
        if (translation == null) {
            translation = key;
            logger.warn("⚠️ No translation found for key '{}' in languages {} or {}", 
                    key, normalizedLang, defaultLanguage);
        }
        
        // Apply parameter formatting if needed
        if (params != null && params.length > 0) {
            translation = formatTranslation(translation, params);
        }
        
        return translation;
    }
    
    /**
     * Get translation from specific language properties
     */
    private String getTranslationFromLanguage(String language, String key) {
        Properties properties = getLanguageProperties(language);
        if (properties != null) {
            return properties.getProperty(key);
        }
        return null;
    }
    
    /**
     * Get language properties with caching
     */
    private Properties getLanguageProperties(String language) {
        // Try cache first
        Properties cached = propertiesCache.getIfPresent(language);
        if (cached != null) {
            return cached;
        }
        
        // Load from resources
        Properties properties = loadLanguageProperties(language);
        if (properties != null) {
            propertiesCache.put(language, properties);
        }
        
        return properties;
    }
    
    /**
     * Load language properties from resources
     */
    private Properties loadLanguageProperties(String language) {
        if (languageProperties.containsKey(language)) {
            return languageProperties.get(language);
        }
        
        String resourcePath = "/translations/" + language + ".properties";
        
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                logger.debug("📁 Translation file not found: {}", resourcePath);
                return null;
            }
            
            Properties properties = new Properties();
            // Load with UTF-8 encoding
            properties.load(new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8));
            
            languageProperties.put(language, properties);
            logger.debug("✅ Loaded {} translations for language '{}'", 
                    properties.size(), language);
            
            return properties;
            
        } catch (IOException e) {
            logger.error("❌ Failed to load translation file: {}", resourcePath, e);
            return null;
        }
    }
    
    /**
     * Load list of supported languages from resources
     */
    private void loadSupportedLanguages() {
        // Common language codes to check
        String[] commonLanguages = {
            "en", "da", "de", "es", "fr", "it", "pt", "ru", "zh", "ja", "ko",
            "ar", "hi", "tr", "pl", "nl", "sv", "no", "fi", "cs", "hu",
            "ro", "bg", "hr", "sk", "sl", "et", "lv", "lt", "mt", "ga",
            "eu", "ca", "gl", "cy", "is", "mk", "sq", "sr", "bs", "me",
            "th", "vi", "id", "ms", "tl", "sw", "am", "bn", "gu", "kn",
            "ml", "mr", "ne", "or", "pa", "si", "ta", "te", "ur", "my",
            "km", "lo", "ka", "hy", "az", "kk", "ky", "mn", "uz", "tk"
        };
        
        for (String lang : commonLanguages) {
            String resourcePath = "/translations/" + lang + ".properties";
            try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    supportedLanguages.add(lang);
                }
            } catch (IOException e) {
                // Ignore, language not supported
            }
        }
        
        logger.info("🌍 Detected supported languages: {}", supportedLanguages);
    }
    
    /**
     * Format translation with parameters using optimized approach
     */
    private String formatTranslation(String translation, Object... params) {
        if (params == null || params.length == 0) {
            return translation;
        }
        
        try {
            // Use MessageFormat for complex formatting
            if (translation.contains("{") && translation.contains("}")) {
                return MessageFormat.format(translation, params);
            }
            
            // Simple parameter replacement for basic cases
            String result = translation;
            Matcher matcher = PARAM_PATTERN.matcher(translation);
            
            while (matcher.find()) {
                int paramIndex = Integer.parseInt(matcher.group(1));
                if (paramIndex < params.length) {
                    String replacement = String.valueOf(params[paramIndex]);
                    result = result.replace(matcher.group(0), replacement);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            logger.warn("⚠️ Failed to format translation '{}' with params {}: {}", 
                    translation, Arrays.toString(params), e.getMessage());
            return translation;
        }
    }
    
    /**
     * Create cache key for translation
     */
    private String createCacheKey(String language, String key, Object... params) {
        if (params == null || params.length == 0) {
            return language + ":" + key;
        }
        
        StringBuilder sb = new StringBuilder(language.length() + key.length() + 32);
        sb.append(language).append(":").append(key);
        
        for (Object param : params) {
            sb.append(":").append(param != null ? param.toString() : "null");
        }
        
        return sb.toString();
    }
    
    /**
     * Normalize language code (e.g., "en-US" -> "en")
     */
    private String normalizeLanguageCode(String language) {
        if (language == null || language.isEmpty()) {
            return defaultLanguage;
        }
        
        // Extract primary language code
        String normalized = language.toLowerCase().split("[-_]")[0];
        
        // Check if supported, otherwise use default
        return supportedLanguages.contains(normalized) ? normalized : defaultLanguage;
    }
    
    /**
     * Check if language is supported
     */
    public boolean isLanguageSupported(String language) {
        return supportedLanguages.contains(normalizeLanguageCode(language));
    }
    
    /**
     * Get all supported languages
     */
    public Set<String> getSupportedLanguages() {
        return new HashSet<>(supportedLanguages);
    }
    
    /**
     * Preload translations for a language to improve performance
     */
    public void preloadLanguage(String language) {
        String normalized = normalizeLanguageCode(language);
        Properties properties = loadLanguageProperties(normalized);
        if (properties != null) {
            logger.info("🚀 Preloaded {} translations for language '{}'", 
                    properties.size(), normalized);
        }
    }
    
    /**
     * Clear caches (useful for testing or memory management)
     */
    public void clearCaches() {
        translationCache.invalidateAll();
        propertiesCache.invalidateAll();
        logger.info("🧹 Translation caches cleared");
    }
    
    /**
     * Get performance metrics
     */
    public TranslationMetrics getMetrics() {
        CacheStats translationStats = translationCache.stats();
        CacheStats propertiesStats = propertiesCache.stats();
        
        return new TranslationMetrics(
            translationRequests.get(),
            cacheHits.get(),
            cacheMisses.get(),
            fallbackUsages.get(),
            translationStats.hitRate(),
            translationCache.estimatedSize(),
            propertiesCache.estimatedSize(),
            supportedLanguages.size()
        );
    }
    
    /**
     * Log performance statistics
     */
    public void logPerformanceStats() {
        TranslationMetrics metrics = getMetrics();
        logger.info("📊 Translation Performance Stats: {}", metrics);
    }
    
    /**
     * Translation performance metrics
     */
    public static class TranslationMetrics {
        public final long totalRequests;
        public final long cacheHits;
        public final long cacheMisses;
        public final long fallbackUsages;
        public final double hitRate;
        public final long translationCacheSize;
        public final long propertiesCacheSize;
        public final int supportedLanguages;
        
        public TranslationMetrics(long totalRequests, long cacheHits, long cacheMisses, 
                                long fallbackUsages, double hitRate, long translationCacheSize, 
                                long propertiesCacheSize, int supportedLanguages) {
            this.totalRequests = totalRequests;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.fallbackUsages = fallbackUsages;
            this.hitRate = hitRate;
            this.translationCacheSize = translationCacheSize;
            this.propertiesCacheSize = propertiesCacheSize;
            this.supportedLanguages = supportedLanguages;
        }
        
        @Override
        public String toString() {
            return String.format(
                "TranslationMetrics{requests=%d, hits=%d, misses=%d, fallbacks=%d, hitRate=%.2f%%, " +
                "translationCache=%d, propertiesCache=%d, languages=%d}",
                totalRequests, cacheHits, cacheMisses, fallbackUsages, 
                hitRate * 100, translationCacheSize, propertiesCacheSize, supportedLanguages
            );
        }
    }
}