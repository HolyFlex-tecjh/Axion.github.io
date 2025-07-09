// Website Configuration
const AxionConfig = {
    // Site Information
    site: {
        name: 'Axion Bot',
        description: 'Den ultimative Discord bot til moderation og underholdning',
        url: 'https://axion-bot.com',
        version: '2.1.0',
        author: 'Axion Bot Team',
        logo: 'assets/axion-logo.svg'
    },
    
    // Bot Statistics
    stats: {
        servers: 50000,
        users: 2500000,
        commands: 150,
        uptime: 99.9,
        categories: 12
    },
    
    // Features Configuration
    features: {
        moderation: {
            enabled: true,
            autoMod: true,
            customFilters: true,
            punishmentSystem: true
        },
        music: {
            enabled: true,
            platforms: ['YouTube', 'Spotify', 'SoundCloud'],
            qualityOptions: ['128kbps', '192kbps', '320kbps'],
            playlists: true
        },
        economy: {
            enabled: true,
            currency: 'Axion Coins',
            gambling: true,
            shop: true
        },
        fun: {
            enabled: true,
            games: ['Trivia', 'Slots', 'Blackjack', 'Roulette'],
            memes: true,
            jokes: true
        }
    },
    
    // API Configuration
    api: {
        baseUrl: 'https://api.axion-bot.com',
        version: 'v1',
        endpoints: {
            auth: '/auth',
            commands: '/commands',
            guilds: '/guilds',
            users: '/users',
            stats: '/stats'
        }
    },
    
    // Theme Configuration
    theme: {
        primaryColor: '#5865f2',
        secondaryColor: '#8b5cf6',
        accentColor: '#00d4aa',
        darkMode: true,
        animations: true,
        particles: true
    },
    
    // Social Links
    social: {
        discord: 'https://discord.gg/axionbot',
        twitter: 'https://twitter.com/axionbot',
        github: 'https://github.com/axionbot',
        youtube: 'https://youtube.com/axionbot'
    },
    
    // Legal Links
    legal: {
        privacy: '/privacy',
        terms: '/terms',
        cookies: '/cookies'
    },
    
    // Performance Settings
    performance: {
        lazyLoading: true,
        caching: true,
        compression: true,
        minification: true,
        serviceWorker: true
    },
    
    // Analytics
    analytics: {
        enabled: true,
        googleAnalytics: 'GA_MEASUREMENT_ID',
        heatmaps: true,
        userFeedback: true
    },
    
    // Localization
    localization: {
        defaultLanguage: 'da',
        supportedLanguages: ['da', 'en', 'de', 'fr', 'es', 'it', 'ru', 'ja', 'ko', 'zh'],
        fallbackLanguage: 'en'
    },
    
    // Command Categories
    commandCategories: [
        {
            id: 'moderation',
            name: 'Moderation',
            icon: 'fas fa-shield-alt',
            color: '#e74c3c',
            description: 'Værktøjer til server moderation og administration'
        },
        {
            id: 'music',
            name: 'Musik',
            icon: 'fas fa-music',
            color: '#9b59b6',
            description: 'Musik afspilning og queue management'
        },
        {
            id: 'fun',
            name: 'Sjov',
            icon: 'fas fa-smile',
            color: '#f39c12',
            description: 'Sjove kommandoer og spil'
        },
        {
            id: 'utility',
            name: 'Værktøjer',
            icon: 'fas fa-tools',
            color: '#3498db',
            description: 'Nyttige værktøjer og funktioner'
        },
        {
            id: 'economy',
            name: 'Økonomi',
            icon: 'fas fa-coins',
            color: '#27ae60',
            description: 'Økonomi system og gambling'
        },
        {
            id: 'info',
            name: 'Information',
            icon: 'fas fa-info-circle',
            color: '#34495e',
            description: 'Server og bruger information'
        }
    ],
    
    // Pricing Plans
    pricing: {
        free: {
            name: 'Gratis',
            price: 0,
            features: ['Basis kommandoer', 'Musik afspilning', 'Basis moderation'],
            limits: {
                servers: 1,
                commands: 50,
                storage: '100MB'
            }
        },
        premium: {
            name: 'Premium',
            price: 29,
            features: ['Alle kommandoer', 'Prioritet support', 'Avanceret moderation', 'Tilpassede kommandoer'],
            limits: {
                servers: 10,
                commands: 'unlimited',
                storage: '1GB'
            }
        },
        enterprise: {
            name: 'Enterprise',
            price: 99,
            features: ['Alle Premium funktioner', 'Dedikeret support', 'API adgang', 'Branding'],
            limits: {
                servers: 'unlimited',
                commands: 'unlimited',
                storage: '10GB'
            }
        }
    },
    
    // Error Messages
    errors: {
        generic: 'Der opstod en fejl. Prøv igen senere.',
        network: 'Netværksfejl. Tjek din internetforbindelse.',
        unauthorized: 'Du har ikke tilladelse til at udføre denne handling.',
        notFound: 'Den anmodede ressource blev ikke fundet.',
        rateLimit: 'Du har nået grænsen for forespørgsler. Prøv igen senere.'
    },
    
    // Success Messages
    success: {
        saved: 'Indstillinger gemt succesfuldt!',
        updated: 'Opdateret succesfuldt!',
        deleted: 'Slettet succesfuldt!',
        invited: 'Bot tilføjet til server succesfuldt!'
    },
    
    // Loading Messages
    loading: {
        default: 'Indlæser...',
        commands: 'Indlæser kommandoer...',
        stats: 'Indlæser statistikker...',
        servers: 'Indlæser servere...'
    }
};

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AxionConfig;
} else {
    window.AxionConfig = AxionConfig;
}
