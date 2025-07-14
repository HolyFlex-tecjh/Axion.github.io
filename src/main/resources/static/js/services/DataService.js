/**
 * Data Service - Handles API calls and data management
 */
class DataService {
    constructor(options = {}) {
        this.options = {
            baseUrl: '/api',
            timeout: 10000,
            retryAttempts: 3,
            retryDelay: 1000,
            cache: true,
            cacheTimeout: 5 * 60 * 1000, // 5 minutes
            ...options
        };
        
        this.cache = new Map();
        this.pendingRequests = new Map();
        this.interceptors = {
            request: [],
            response: []
        };
        
        // Mock data for development
        this.mockMode = options.mockMode || false;
        this.initializeMockData();
    }

    /**
     * Initialize mock data for development
     */
    initializeMockData() {
        this.mockData = {
            guilds: [
                {
                    id: '123456789',
                    name: 'Test Server',
                    icon: 'https://cdn.discordapp.com/icons/123456789/icon.png',
                    memberCount: 1250,
                    owner: true
                },
                {
                    id: '987654321',
                    name: 'Another Server',
                    icon: null,
                    memberCount: 850,
                    owner: false
                }
            ],
            configuration: {
                guildId: '123456789',
                automod: {
                    enabled: true,
                    spamDetection: true,
                    linkFiltering: false,
                    capsFilter: true,
                    profanityFilter: true
                },
                actions: {
                    deleteMessage: true,
                    warnUser: true,
                    muteUser: false,
                    kickUser: false,
                    banUser: false
                },
                thresholds: {
                    spamMessages: 5,
                    spamTimeWindow: 10,
                    capsPercentage: 70,
                    maxMentions: 5
                },
                customRules: [
                    {
                        id: 'rule1',
                        name: 'No External Links',
                        pattern: 'https?://(?!discord\.gg|discord\.com)',
                        action: 'delete',
                        enabled: true
                    }
                ]
            },
            analytics: {
                totalMessages: 15420,
                moderatedMessages: 342,
                activeUsers: 89,
                warnings: 156,
                timeouts: 23,
                bans: 5,
                chartData: {
                    messages: [120, 150, 180, 200, 175, 190, 210],
                    modActions: [12, 8, 15, 20, 18, 14, 16],
                    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
                }
            }
        };
    }

    /**
     * Add request interceptor
     * @param {Function} interceptor - Interceptor function
     */
    addRequestInterceptor(interceptor) {
        this.interceptors.request.push(interceptor);
    }

    /**
     * Add response interceptor
     * @param {Function} interceptor - Interceptor function
     */
    addResponseInterceptor(interceptor) {
        this.interceptors.response.push(interceptor);
    }

    /**
     * Apply request interceptors
     * @param {Object} config - Request configuration
     */
    async applyRequestInterceptors(config) {
        let modifiedConfig = { ...config };
        
        for (const interceptor of this.interceptors.request) {
            modifiedConfig = await interceptor(modifiedConfig);
        }
        
        return modifiedConfig;
    }

    /**
     * Apply response interceptors
     * @param {Response} response - Fetch response
     */
    async applyResponseInterceptors(response) {
        let modifiedResponse = response;
        
        for (const interceptor of this.interceptors.response) {
            modifiedResponse = await interceptor(modifiedResponse);
        }
        
        return modifiedResponse;
    }

    /**
     * Generate cache key
     * @param {string} url - Request URL
     * @param {Object} options - Request options
     */
    getCacheKey(url, options = {}) {
        const key = `${options.method || 'GET'}:${url}`;
        if (options.body) {
            return `${key}:${JSON.stringify(options.body)}`;
        }
        return key;
    }

    /**
     * Check if cache entry is valid
     * @param {Object} entry - Cache entry
     */
    isCacheValid(entry) {
        return Date.now() - entry.timestamp < this.options.cacheTimeout;
    }

    /**
     * Make HTTP request with caching and retry logic
     * @param {string} url - Request URL
     * @param {Object} options - Request options
     */
    async request(url, options = {}) {
        // Apply request interceptors
        const config = await this.applyRequestInterceptors({
            url,
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        const fullUrl = config.url.startsWith('http') ? config.url : `${this.options.baseUrl}${config.url}`;
        const cacheKey = this.getCacheKey(fullUrl, config);

        // Check cache for GET requests
        if (config.method === 'GET' && this.options.cache) {
            const cached = this.cache.get(cacheKey);
            if (cached && this.isCacheValid(cached)) {
                return cached.data;
            }
        }

        // Check for pending request
        if (this.pendingRequests.has(cacheKey)) {
            return this.pendingRequests.get(cacheKey);
        }

        // Mock mode for development
        if (this.mockMode) {
            const mockPromise = this.handleMockRequest(config.url, config);
            this.pendingRequests.set(cacheKey, mockPromise);
            
            try {
                const result = await mockPromise;
                this.pendingRequests.delete(cacheKey);
                
                // Cache GET requests
                if (config.method === 'GET' && this.options.cache) {
                    this.cache.set(cacheKey, {
                        data: result,
                        timestamp: Date.now()
                    });
                }
                
                return result;
            } catch (error) {
                this.pendingRequests.delete(cacheKey);
                throw error;
            }
        }

        // Real API request with retry logic
        const requestPromise = this.makeRequestWithRetry(fullUrl, config);
        this.pendingRequests.set(cacheKey, requestPromise);

        try {
            const result = await requestPromise;
            this.pendingRequests.delete(cacheKey);
            
            // Cache GET requests
            if (config.method === 'GET' && this.options.cache) {
                this.cache.set(cacheKey, {
                    data: result,
                    timestamp: Date.now()
                });
            }
            
            return result;
        } catch (error) {
            this.pendingRequests.delete(cacheKey);
            throw error;
        }
    }

    /**
     * Make request with retry logic
     * @param {string} url - Request URL
     * @param {Object} config - Request configuration
     */
    async makeRequestWithRetry(url, config) {
        let lastError;
        
        for (let attempt = 0; attempt <= this.options.retryAttempts; attempt++) {
            try {
                const controller = new AbortController();
                const timeoutId = setTimeout(() => controller.abort(), this.options.timeout);
                
                const response = await fetch(url, {
                    ...config,
                    signal: controller.signal
                });
                
                clearTimeout(timeoutId);
                
                // Apply response interceptors
                const processedResponse = await this.applyResponseInterceptors(response);
                
                if (!processedResponse.ok) {
                    throw new Error(`HTTP ${processedResponse.status}: ${processedResponse.statusText}`);
                }
                
                return await processedResponse.json();
                
            } catch (error) {
                lastError = error;
                
                // Don't retry on certain errors
                if (error.name === 'AbortError' || 
                    (error.message && error.message.includes('HTTP 4'))) {
                    break;
                }
                
                // Wait before retry
                if (attempt < this.options.retryAttempts) {
                    await new Promise(resolve => 
                        setTimeout(resolve, this.options.retryDelay * Math.pow(2, attempt))
                    );
                }
            }
        }
        
        throw lastError;
    }

    /**
     * Handle mock requests for development
     * @param {string} url - Request URL
     * @param {Object} config - Request configuration
     */
    async handleMockRequest(url, config) {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 200 + Math.random() * 300));
        
        // Route mock requests
        if (url.includes('/guilds')) {
            return this.mockData.guilds;
        }
        
        if (url.includes('/configuration')) {
            if (config.method === 'POST' || config.method === 'PUT') {
                // Simulate saving configuration
                const updates = JSON.parse(config.body);
                Object.assign(this.mockData.configuration, updates);
                return { success: true, message: 'Configuration saved successfully' };
            }
            return this.mockData.configuration;
        }
        
        if (url.includes('/analytics')) {
            return this.mockData.analytics;
        }
        
        if (url.includes('/test')) {
            // Simulate configuration test
            return {
                success: Math.random() > 0.3,
                results: [
                    { test: 'Spam Detection', passed: true, message: 'Working correctly' },
                    { test: 'Link Filtering', passed: Math.random() > 0.5, message: 'Test result' },
                    { test: 'Custom Rules', passed: true, message: 'All rules validated' }
                ]
            };
        }
        
        throw new Error(`Mock endpoint not found: ${url}`);
    }

    // API Methods

    /**
     * Get list of guilds
     */
    async getGuilds() {
        return this.request('/guilds');
    }

    /**
     * Get guild configuration
     * @param {string} guildId - Guild ID
     */
    async getGuildConfiguration(guildId) {
        return this.request(`/guilds/${guildId}/configuration`);
    }

    /**
     * Save guild configuration
     * @param {string} guildId - Guild ID
     * @param {Object} configuration - Configuration data
     */
    async saveGuildConfiguration(guildId, configuration) {
        return this.request(`/guilds/${guildId}/configuration`, {
            method: 'POST',
            body: JSON.stringify(configuration)
        });
    }

    /**
     * Test guild configuration
     * @param {string} guildId - Guild ID
     * @param {Object} configuration - Configuration to test
     */
    async testConfiguration(guildId, configuration) {
        return this.request(`/guilds/${guildId}/test`, {
            method: 'POST',
            body: JSON.stringify(configuration)
        });
    }

    /**
     * Get analytics data
     * @param {string} guildId - Guild ID
     * @param {Object} options - Query options
     */
    async getAnalytics(guildId, options = {}) {
        const params = new URLSearchParams(options);
        return this.request(`/guilds/${guildId}/analytics?${params}`);
    }

    /**
     * Export configuration
     * @param {string} guildId - Guild ID
     */
    async exportConfiguration(guildId) {
        return this.request(`/guilds/${guildId}/export`);
    }

    /**
     * Import configuration
     * @param {string} guildId - Guild ID
     * @param {Object} configuration - Configuration to import
     */
    async importConfiguration(guildId, configuration) {
        return this.request(`/guilds/${guildId}/import`, {
            method: 'POST',
            body: JSON.stringify(configuration)
        });
    }

    /**
     * Get moderation logs
     * @param {string} guildId - Guild ID
     * @param {Object} filters - Log filters
     */
    async getModerationLogs(guildId, filters = {}) {
        const params = new URLSearchParams(filters);
        return this.request(`/guilds/${guildId}/logs?${params}`);
    }

    /**
     * Get user statistics
     * @param {string} guildId - Guild ID
     * @param {string} userId - User ID
     */
    async getUserStats(guildId, userId) {
        return this.request(`/guilds/${guildId}/users/${userId}/stats`);
    }

    // Utility Methods

    /**
     * Clear cache
     * @param {string} pattern - Cache key pattern (optional)
     */
    clearCache(pattern = null) {
        if (pattern) {
            const regex = new RegExp(pattern);
            for (const [key] of this.cache) {
                if (regex.test(key)) {
                    this.cache.delete(key);
                }
            }
        } else {
            this.cache.clear();
        }
    }

    /**
     * Get cache statistics
     */
    getCacheStats() {
        const entries = Array.from(this.cache.values());
        const validEntries = entries.filter(entry => this.isCacheValid(entry));
        
        return {
            total: this.cache.size,
            valid: validEntries.length,
            expired: this.cache.size - validEntries.length,
            memoryUsage: JSON.stringify(entries).length
        };
    }

    /**
     * Set mock mode
     * @param {boolean} enabled - Whether to enable mock mode
     */
    setMockMode(enabled) {
        this.mockMode = enabled;
    }

    /**
     * Update mock data
     * @param {string} key - Data key
     * @param {*} value - Data value
     */
    updateMockData(key, value) {
        this.mockData[key] = value;
    }

    /**
     * Get pending requests count
     */
    getPendingRequestsCount() {
        return this.pendingRequests.size;
    }

    /**
     * Cancel all pending requests
     */
    cancelPendingRequests() {
        this.pendingRequests.clear();
    }

    /**
     * Health check
     */
    async healthCheck() {
        try {
            await this.request('/health');
            return true;
        } catch (error) {
            return false;
        }
    }

    /**
     * Destroy the data service and clean up resources
     */
    destroy() {
        // Cancel all pending requests
        this.cancelPendingRequests();
        
        // Clear cache
        this.clearCache();
        
        // Reset state
        this.baseUrl = null;
        this.options = null;
        this.mockData = null;
    }
}

export default DataService;