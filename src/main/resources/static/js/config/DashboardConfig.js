/**
 * Dashboard Configuration
 * Centralized configuration for the Moderation Dashboard
 */

export default {
    // API Configuration
    api: {
        baseUrl: '/api/v1',
        timeout: 30000,
        retryAttempts: 3,
        retryDelay: 1000,
        endpoints: {
            guilds: '/guilds',
            config: '/guilds/{guildId}/config',
            analytics: '/guilds/{guildId}/analytics',
            logs: '/guilds/{guildId}/logs',
            test: '/guilds/{guildId}/test'
        }
    },

    // Cache Configuration
    cache: {
        enabled: true,
        defaultTtl: 300000, // 5 minutes
        maxSize: 100,
        strategies: {
            guilds: { ttl: 600000 }, // 10 minutes
            config: { ttl: 300000 }, // 5 minutes
            analytics: { ttl: 60000 }, // 1 minute
            logs: { ttl: 30000 } // 30 seconds
        }
    },

    // UI Configuration
    ui: {
        theme: {
            default: 'light',
            available: ['light', 'dark', 'auto']
        },
        animations: {
            enabled: true,
            duration: 300,
            easing: 'ease-in-out'
        },
        notifications: {
            duration: 5000,
            position: 'top-right',
            maxVisible: 5
        },
        loading: {
            minDisplayTime: 500,
            spinnerDelay: 200
        }
    },

    // Form Configuration
    forms: {
        autoSave: {
            enabled: true,
            debounceDelay: 1000,
            storageKey: 'moderation_dashboard_autosave'
        },
        validation: {
            realTime: true,
            showErrors: true,
            highlightFields: true
        }
    },

    // Chart Configuration
    charts: {
        responsive: true,
        maintainAspectRatio: false,
        animation: {
            duration: 750,
            easing: 'easeInOutQuart'
        },
        colors: {
            primary: '#007bff',
            secondary: '#6c757d',
            success: '#28a745',
            danger: '#dc3545',
            warning: '#ffc107',
            info: '#17a2b8',
            light: '#f8f9fa',
            dark: '#343a40'
        },
        themes: {
            light: {
                backgroundColor: '#ffffff',
                textColor: '#333333',
                gridColor: '#e9ecef',
                borderColor: '#dee2e6'
            },
            dark: {
                backgroundColor: '#2d3748',
                textColor: '#ffffff',
                gridColor: '#4a5568',
                borderColor: '#718096'
            }
        }
    },

    // Section Configuration
    sections: {
        default: 'overview',
        available: [
            'overview',
            'content-filters',
            'actions',
            'custom-rules',
            'thresholds',
            'testing',
            'analytics',
            'settings'
        ],
        lazy: {
            enabled: true,
            preload: ['overview', 'content-filters']
        }
    },

    // Development Configuration
    development: {
        mockMode: true,
        debugMode: false,
        logLevel: 'info', // 'debug', 'info', 'warn', 'error'
        enablePerformanceMonitoring: true
    },

    // Feature Flags
    features: {
        analytics: true,
        customRules: true,
        bulkActions: true,
        exportData: true,
        realTimeUpdates: false,
        advancedFilters: true,
        userPermissions: false
    },

    // Performance Configuration
    performance: {
        debounceDelay: 300,
        throttleDelay: 100,
        virtualScrolling: {
            enabled: false,
            itemHeight: 50,
            bufferSize: 10
        },
        lazyLoading: {
            enabled: true,
            threshold: 0.1,
            rootMargin: '50px'
        }
    },

    // Security Configuration
    security: {
        csrfProtection: true,
        sanitizeInput: true,
        validateResponses: true,
        maxRequestSize: 1048576, // 1MB
        allowedFileTypes: ['json', 'csv', 'txt']
    },

    // Accessibility Configuration
    accessibility: {
        enabled: true,
        announceChanges: true,
        keyboardNavigation: true,
        highContrast: false,
        reducedMotion: false
    },

    // Localization Configuration
    i18n: {
        defaultLocale: 'en',
        fallbackLocale: 'en',
        available: ['en'],
        dateFormat: 'YYYY-MM-DD HH:mm:ss',
        numberFormat: {
            decimal: '.',
            thousands: ','
        }
    },

    // Storage Configuration
    storage: {
        prefix: 'moderation_dashboard_',
        version: '2.0.0',
        migrations: {
            enabled: true,
            autoCleanup: true
        }
    },

    // Error Handling Configuration
    errorHandling: {
        showUserFriendlyMessages: true,
        logErrors: true,
        reportErrors: false,
        retryFailedRequests: true,
        fallbackToCache: true
    }
};