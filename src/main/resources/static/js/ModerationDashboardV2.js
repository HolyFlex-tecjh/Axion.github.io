/**
 * Moderation Dashboard V2 - Refactored modular architecture
 */
// Import core modules
import EventBus from './core/EventBus.js';
import StateManager from './core/StateManager.js';
import Component from './core/Component.js';

// Import components
import NavigationComponent from './components/NavigationComponent.js';
import SectionManager from './components/SectionManager.js';
import FormManager from './components/FormManager.js';

// Import services
import DataService from './services/DataService.js';
import ChartService from './services/ChartService.js';

// Import configuration and utilities
import DashboardConfig from './config/DashboardConfig.js';
import Utils from './utils/Utils.js';
import { logger } from './utils/Logger.js';

class ModerationDashboardV2 {
    constructor(options = {}) {
        // Merge options with default configuration
        this.config = Utils.deepMerge({}, DashboardConfig, options);
        
        this.container = null;
        this.initialized = false;
        this.destroyed = false;
        this.components = new Map();
        this.services = new Map();
        
        // Initialize logger
        this.logger = logger.child('ModerationDashboardV2', {
            level: this.config.development.logLevel,
            enableStorage: this.config.development.debugMode
        });
        
        // Performance monitoring
        this.performanceMetrics = {
            initTime: null,
            loadTime: null,
            renderTime: null
        };
        
        // Bind methods with throttling/debouncing
        this.handleNavigation = this.handleNavigation.bind(this);
        this.handleFormSubmit = this.handleFormSubmit.bind(this);
        this.handleAutoSave = Utils.debounce(this.handleAutoSave.bind(this), this.config.forms.autoSave.debounceDelay);
        this.handleDataLoad = this.handleDataLoad.bind(this);
        this.handleDataError = this.handleDataError.bind(this);
        this.handleChartUpdate = this.handleChartUpdate.bind(this);
        this.handleStateChange = this.handleStateChange.bind(this);
        this.handleResize = Utils.throttle(this.handleResize.bind(this), this.config.performance.throttleDelay);
        this.handleBeforeUnload = this.handleBeforeUnload.bind(this);
        
        this.logger.info('Dashboard instance created', { config: this.config });
    }

    /**
     * Initialize the dashboard
     */
    async init() {
        if (this.initialized) {
            this.logger.warn('Dashboard already initialized');
            return;
        }
        
        const initTimer = this.logger.timer('Dashboard initialization');
        
        try {
            this.logger.info('Starting dashboard initialization');
            
            // Get container element
            this.container = document.getElementById(this.config.containerId || 'dashboard-container');
            if (!this.container) {
                throw new Error(`Container element with ID '${this.config.containerId || 'dashboard-container'}' not found`);
            }
            
            // Show loading indicator
            this.showLoading();
            
            // Initialize core systems
            await this.initializeCore();
            this.logger.debug('Core systems initialized');
            
            // Initialize services
            await this.initializeServices();
            this.logger.debug('Services initialized');
            
            // Initialize components
            await this.initializeComponents();
            this.logger.debug('Components initialized');
            
            // Setup event listeners
            this.setupEventListeners();
            this.logger.debug('Event listeners setup');
            
            // Load initial data
            await this.loadInitialData();
            this.logger.debug('Initial data loaded');
            
            // Setup sections
            this.setupSections();
            this.logger.debug('Sections registered');
            
            // Initialize charts
            await this.initializeCharts();
            this.logger.debug('Charts initialized');
            
            // Setup auto-save
            if (this.config.forms.autoSave.enabled) {
                this.setupAutoSave();
                this.logger.debug('Auto-save setup');
            }
            
            // Hide loading indicator
            this.hideLoading();
            
            // Mark as initialized
            this.initialized = true;
            
            initTimer({ success: true });
            this.logger.info('Dashboard V2 initialized successfully');
            
            // Emit initialization complete event
            this.eventBus.emit('dashboard:initialized', {
                timestamp: Date.now(),
                version: '2.0',
                dashboard: this
            });
            
        } catch (error) {
            initTimer({ success: false, error: error.message });
            this.logger.error('Failed to initialize dashboard', { error: error.message, stack: error.stack });
            this.handleInitializationError(error);
            this.hideLoading();
            throw error;
        }
    }

    /**
     * Initialize core systems
     */
    async initializeCore() {
        // Initialize EventBus
        this.eventBus = new EventBus();
        window.EventBus = this.eventBus;
        this.logger.debug('EventBus initialized');
        
        // Initialize StateManager with configuration
        this.stateManager = new StateManager({
            enableHistory: this.config.development.debugMode,
            maxHistorySize: 50
        });
        window.StateManager = this.stateManager;
        this.logger.debug('StateManager initialized');
        
        // Set initial state from configuration
        this.stateManager.setState({
            ui: {
                theme: this.config.ui.theme.default,
                loading: true,
                activeSection: this.config.sections.default
            },
            config: this.config
        });
        
        // Setup error handling
        this.setupErrorHandling();
        
        this.logger.debug('Core systems initialized');
    }

    /**
     * Initialize services
     */
    async initializeServices() {
        // Initialize DataService with configuration
        this.dataService = new DataService({
            baseUrl: this.config.api.baseUrl,
            timeout: this.config.api.timeout,
            retryAttempts: this.config.api.retryAttempts,
            retryDelay: this.config.api.retryDelay,
            mockMode: this.config.development.mockMode,
            cache: this.config.cache,
            endpoints: this.config.api.endpoints
        });
        this.services.set('data', this.dataService);
        this.logger.debug('DataService initialized');
        
        // Initialize ChartService with configuration
        this.chartService = new ChartService({
            theme: this.config.ui.theme.default,
            colors: this.config.charts.colors,
            themes: this.config.charts.themes,
            responsive: this.config.charts.responsive,
            animation: this.config.charts.animation
        });
        this.services.set('charts', this.chartService);
        this.logger.debug('ChartService initialized');
        
        this.logger.debug('Services initialized');
    }

    /**
     * Initialize components
     */
    async initializeComponents() {
        // Initialize NavigationComponent
        this.navigationComponent = new NavigationComponent({
            containerId: 'sidebar-nav',
            eventBus: this.eventBus,
            stateManager: this.stateManager,
            sections: this.config.sections.available,
            defaultSection: this.config.sections.default
        });
        this.components.set('navigation', this.navigationComponent);
        this.logger.debug('NavigationComponent created');
        
        // Initialize SectionManager
        this.sectionManager = new SectionManager({
            containerId: 'main-content',
            eventBus: this.eventBus,
            stateManager: this.stateManager,
            animations: this.config.ui.animations,
            lazyLoading: this.config.sections.lazy
        });
        this.components.set('sections', this.sectionManager);
        this.logger.debug('SectionManager created');
        
        // Initialize FormManager
        this.formManager = new FormManager({
            eventBus: this.eventBus,
            stateManager: this.stateManager,
            autoSave: this.config.forms.autoSave,
            validation: this.config.forms.validation
        });
        this.components.set('forms', this.formManager);
        this.logger.debug('FormManager created');
        
        // Initialize all components
        for (const [name, component] of this.components) {
            try {
                await component.init();
                this.logger.debug(`${name} component initialized`);
            } catch (error) {
                this.logger.error(`Failed to initialize ${name} component`, { error: error.message });
                throw error;
            }
        }
        
        this.logger.debug('Components initialized');
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Navigation events
        this.eventBus.on('navigate', this.handleNavigation.bind(this));
        
        // Form events
        this.eventBus.on('formSubmit', this.handleFormSubmit.bind(this));
        this.eventBus.on('autoSave', this.handleAutoSave.bind(this));
        
        // Data events
        this.eventBus.on('dataLoaded', this.handleDataLoaded.bind(this));
        this.eventBus.on('dataError', this.handleDataError.bind(this));
        
        // Chart events
        this.eventBus.on('chart:created', this.handleChartCreated.bind(this));
        this.eventBus.on('chart:error', this.handleChartError.bind(this));
        
        // State changes
        this.stateManager.subscribe('ui.activeSection', this.handleSectionChange.bind(this));
        this.stateManager.subscribe('currentGuildId', this.handleGuildChange.bind(this));
        
        // Window events
        window.addEventListener('resize', this.throttle(this.handleResize.bind(this), 250));
        window.addEventListener('beforeunload', this.handleBeforeUnload.bind(this));
        
        // Page visibility events for performance optimization
        document.addEventListener('visibilitychange', this.handleVisibilityChange.bind(this));
        
        console.log('Event listeners setup complete');
    }

    /**
     * Handle page visibility changes for performance optimization
     */
    handleVisibilityChange() {
        const isVisible = !document.hidden;
        this.stateManager.setState('ui.pageVisible', isVisible);
        
        if (isVisible) {
            // Resume updates when page becomes visible
            this.eventBus.emit('dashboard:resumed');
        } else {
            // Pause updates when page is hidden
            this.eventBus.emit('dashboard:paused');
        }
    }

    /**
     * Destroy the dashboard and clean up resources
     */
    destroy() {
        if (this.destroyed) {
            return;
        }

        this.logger.info('Destroying dashboard...');

        try {
            // Clear any intervals or timeouts
            if (this.autoSaveTimeout) {
                clearTimeout(this.autoSaveTimeout);
                this.autoSaveTimeout = null;
            }

            // Destroy all components
            for (const [name, component] of this.components) {
                if (component && typeof component.destroy === 'function') {
                    component.destroy();
                }
            }
            this.components.clear();

            // Destroy all charts
            if (this.chartService) {
                const chartIds = this.chartService.getChartIds();
                chartIds.forEach(chartId => {
                    this.chartService.destroyChart(chartId);
                });
            }

            // Clear services
            this.services.clear();

            // Remove event listeners
            window.removeEventListener('resize', this.handleResize);
            window.removeEventListener('beforeunload', this.handleBeforeUnload);
            document.removeEventListener('visibilitychange', this.handleVisibilityChange);

            // Clear state
            if (this.stateManager) {
                this.stateManager.clear();
            }

            // Mark as destroyed
            this.destroyed = true;
            this.initialized = false;

            // Emit destroy event
            if (this.eventBus) {
                this.eventBus.emit('dashboard:destroyed');
                this.eventBus.removeAllListeners();
            }

            this.logger.info('Dashboard destroyed successfully');

        } catch (error) {
            this.logger.error('Error during dashboard destruction', { error: error.message });
        }
    }

    /**
     * Load initial data
     */
    async loadInitialData() {
        try {
            this.showLoading('Loading dashboard data...');
            
            // Load guilds
            const guilds = await this.dataService.getGuilds();
            this.stateManager.setState('guilds', guilds);
            
            // Set default guild if available
            if (guilds.length > 0) {
                const defaultGuild = guilds.find(g => g.owner) || guilds[0];
                this.stateManager.setState('currentGuildId', defaultGuild.id);
                
                // Load guild configuration
                const config = await this.dataService.getGuildConfiguration(defaultGuild.id);
                this.stateManager.setState('currentConfig', config);
            }
            
            this.hideLoading();
            
            this.eventBus.emit('dataLoaded', {
                guilds,
                timestamp: Date.now()
            });
            
        } catch (error) {
            this.hideLoading();
            console.error('Failed to load initial data:', error);
            this.showAlert('Failed to load dashboard data. Using offline mode.', 'warning');
            
            this.eventBus.emit('dataError', { error });
        }
    }

    /**
     * Setup sections
     */
    setupSections() {
        if (!this.sectionManager) return;

        // Register sections
        const sections = [
            {
                id: 'overview',
                template: this.getOverviewTemplate.bind(this),
                onShow: this.handleOverviewShow.bind(this)
            },
            {
                id: 'filters',
                template: this.getFiltersTemplate.bind(this),
                onShow: this.handleFiltersShow.bind(this)
            },
            {
                id: 'actions',
                template: this.getActionsTemplate.bind(this),
                onShow: this.handleActionsShow.bind(this)
            },
            {
                id: 'rules',
                template: this.getRulesTemplate.bind(this),
                onShow: this.handleRulesShow.bind(this)
            },
            {
                id: 'thresholds',
                template: this.getThresholdsTemplate.bind(this),
                onShow: this.handleThresholdsShow.bind(this)
            },
            {
                id: 'testing',
                template: this.getTestingTemplate.bind(this),
                onShow: this.handleTestingShow.bind(this)
            },
            {
                id: 'analytics',
                template: this.getAnalyticsTemplate.bind(this),
                onShow: this.handleAnalyticsShow.bind(this)
            },
            {
                id: 'settings',
                template: this.getSettingsTemplate.bind(this),
                onShow: this.handleSettingsShow.bind(this)
            }
        ];

        sections.forEach(section => {
            this.sectionManager.registerSection(section.id, section);
        });
        
        console.log('Sections registered');
    }

    /**
     * Initialize charts
     */
    async initializeCharts() {
        // Charts will be initialized when analytics section is shown
        console.log('Chart initialization deferred to section load');
    }

    /**
     * Setup auto-save
     */
    setupAutoSave() {
        if (!this.options.autoSave) return;
        
        // Auto-save is handled by FormManager
        console.log('Auto-save enabled');
    }

    /**
     * Setup error handling
     */
    setupErrorHandling() {
        // Global error handler
        window.addEventListener('error', (event) => {
            console.error('Global error:', event.error);
            this.handleError(event.error);
        });
        
        // Unhandled promise rejection handler
        window.addEventListener('unhandledrejection', (event) => {
            console.error('Unhandled promise rejection:', event.reason);
            this.handleError(event.reason);
        });
    }

    // Event Handlers

    handleNavigation(data) {
        console.log('Navigation:', data.section);
    }

    async handleFormSubmit(data) {
        try {
            this.showLoading('Saving configuration...');
            
            const guildId = this.stateManager.getState('currentGuildId');
            if (!guildId) {
                throw new Error('No guild selected');
            }
            
            await this.dataService.saveGuildConfiguration(guildId, data.data);
            
            this.hideLoading();
            this.showAlert('Configuration saved successfully!', 'success');
            
        } catch (error) {
            this.hideLoading();
            this.showAlert('Failed to save configuration: ' + error.message, 'danger');
        }
    }

    handleAutoSave(data) {
        console.log('Auto-save:', data.formId);
        // Auto-save feedback could be shown here
    }

    handleDataLoaded(data) {
        console.log('Data loaded:', data);
    }

    handleDataError(data) {
        console.error('Data error:', data.error);
    }

    handleChartCreated(data) {
        console.log('Chart created:', data.chartId);
    }

    handleChartError(data) {
        console.error('Chart error:', data);
    }

    handleSectionChange(newSection) {
        console.log('Section changed to:', newSection);
    }

    async handleGuildChange(newGuildId) {
        if (!newGuildId) return;
        
        try {
            this.showLoading('Loading guild configuration...');
            
            const config = await this.dataService.getGuildConfiguration(newGuildId);
            this.stateManager.setState('currentConfig', config);
            
            this.hideLoading();
            
        } catch (error) {
            this.hideLoading();
            this.showAlert('Failed to load guild configuration', 'warning');
        }
    }

    handleResize() {
        // Resize charts
        if (this.chartService) {
            this.chartService.resizeAllCharts();
        }
        
        // Emit resize event for components
        this.eventBus.emit('window:resize', {
            width: window.innerWidth,
            height: window.innerHeight
        });
    }

    handleBeforeUnload(event) {
        // Check for unsaved changes
        if (this.formManager) {
            const status = this.formManager.getAllFormsStatus();
            const hasUnsaved = Object.values(status).some(s => s && s.isDirty);
            
            if (hasUnsaved) {
                event.preventDefault();
                event.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
            }
        }
    }

    handleError(error) {
        // Log error
        console.error('Dashboard error:', error);
        
        // Show user-friendly error message
        this.showAlert('An unexpected error occurred. Please refresh the page.', 'danger');
        
        // Emit error event
        this.eventBus.emit('dashboard:error', { error });
    }

    handleInitializationError(error) {
        // Show initialization error
        const container = document.getElementById(this.options.containerId);
        if (container) {
            container.innerHTML = `
                <div class="alert alert-danger m-4">
                    <h4>Dashboard Initialization Failed</h4>
                    <p>The dashboard could not be initialized. Please refresh the page and try again.</p>
                    <details>
                        <summary>Error Details</summary>
                        <pre>${error.message}</pre>
                    </details>
                    <button class="btn btn-primary mt-3" onclick="location.reload()">
                        Refresh Page
                    </button>
                </div>
            `;
        }
    }

    // Section Templates (simplified versions)

    getOverviewTemplate() {
        return `
            <div class="section-header">
                <h2>Dashboard Overview</h2>
                <p class="text-muted">Monitor your server's moderation activity</p>
            </div>
            <div class="row">
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-value" id="total-messages">-</div>
                        <div class="stat-label">Total Messages</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-value" id="moderated-messages">-</div>
                        <div class="stat-label">Moderated</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-value" id="active-users">-</div>
                        <div class="stat-label">Active Users</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-value" id="warnings-issued">-</div>
                        <div class="stat-label">Warnings</div>
                    </div>
                </div>
            </div>
        `;
    }

    getFiltersTemplate() {
        return `
            <div class="section-header">
                <h2>Content Filters</h2>
                <p class="text-muted">Configure automatic content moderation</p>
            </div>
            <form id="filters-form">
                <div class="card">
                    <div class="card-body">
                        <h5>Spam Detection</h5>
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="spam-detection" name="spamDetection">
                            <label class="form-check-label" for="spam-detection">
                                Enable spam detection
                            </label>
                        </div>
                    </div>
                </div>
            </form>
        `;
    }

    getActionsTemplate() {
        return `
            <div class="section-header">
                <h2>Moderation Actions</h2>
                <p class="text-muted">Configure automatic actions for violations</p>
            </div>
            <form id="actions-form">
                <!-- Actions form content -->
            </form>
        `;
    }

    getRulesTemplate() {
        return `
            <div class="section-header">
                <h2>Custom Rules</h2>
                <p class="text-muted">Create custom moderation rules</p>
            </div>
            <div id="custom-rules-container">
                <!-- Rules will be populated here -->
            </div>
        `;
    }

    getThresholdsTemplate() {
        return `
            <div class="section-header">
                <h2>Thresholds</h2>
                <p class="text-muted">Configure moderation sensitivity</p>
            </div>
            <form id="thresholds-form">
                <!-- Thresholds form content -->
            </form>
        `;
    }

    getTestingTemplate() {
        return `
            <div class="section-header">
                <h2>Configuration Testing</h2>
                <p class="text-muted">Test your moderation configuration</p>
            </div>
            <div id="testing-container">
                <button class="btn btn-primary" id="run-test">Run Test</button>
                <div id="test-results" class="mt-3"></div>
            </div>
        `;
    }

    getAnalyticsTemplate() {
        return `
            <div class="section-header">
                <h2>Analytics</h2>
                <p class="text-muted">View moderation statistics and trends</p>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5>Message Activity</h5>
                            <canvas id="messages-chart"></canvas>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5>Moderation Actions</h5>
                            <canvas id="actions-chart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    getSettingsTemplate() {
        return `
            <div class="section-header">
                <h2>Settings</h2>
                <p class="text-muted">Configure dashboard preferences</p>
            </div>
            <form id="settings-form">
                <!-- Settings form content -->
            </form>
        `;
    }

    // Section Show Handlers

    async handleOverviewShow() {
        // Load and display overview stats
        const analytics = await this.dataService.getAnalytics(
            this.stateManager.getState('currentGuildId')
        );
        
        if (analytics) {
            document.getElementById('total-messages').textContent = analytics.totalMessages || 0;
            document.getElementById('moderated-messages').textContent = analytics.moderatedMessages || 0;
            document.getElementById('active-users').textContent = analytics.activeUsers || 0;
            document.getElementById('warnings-issued').textContent = analytics.warnings || 0;
        }
    }

    handleFiltersShow() {
        // Populate filters form with current configuration
        const config = this.stateManager.getState('currentConfig');
        if (config && config.automod) {
            const spamDetection = document.getElementById('spam-detection');
            if (spamDetection) {
                spamDetection.checked = config.automod.spamDetection || false;
            }
        }
    }

    handleActionsShow() {
        // Populate actions form
    }

    handleRulesShow() {
        // Populate custom rules
    }

    handleThresholdsShow() {
        // Populate thresholds form
    }

    handleTestingShow() {
        // Setup testing interface
        const runTestBtn = document.getElementById('run-test');
        if (runTestBtn) {
            runTestBtn.addEventListener('click', this.runConfigurationTest.bind(this));
        }
    }

    async handleAnalyticsShow() {
        // Initialize charts
        await this.initializeAnalyticsCharts();
    }

    handleSettingsShow() {
        // Populate settings form
    }

    // Utility Methods

    async initializeAnalyticsCharts() {
        try {
            const analytics = await this.dataService.getAnalytics(
                this.stateManager.getState('currentGuildId')
            );
            
            if (!analytics || !analytics.chartData) return;

            // Messages chart
            await this.chartService.createChart('messages-chart', '#messages-chart', {
                type: 'line',
                data: {
                    labels: analytics.chartData.labels,
                    datasets: [{
                        label: 'Messages',
                        data: analytics.chartData.messages,
                        borderColor: '#007bff',
                        backgroundColor: 'rgba(0, 123, 255, 0.1)',
                        tension: 0.4
                    }]
                }
            });

            // Actions chart
            await this.chartService.createChart('actions-chart', '#actions-chart', {
                type: 'bar',
                data: {
                    labels: analytics.chartData.labels,
                    datasets: [{
                        label: 'Mod Actions',
                        data: analytics.chartData.modActions,
                        backgroundColor: '#28a745'
                    }]
                }
            });
            
        } catch (error) {
            console.error('Failed to initialize analytics charts:', error);
        }
    }

    async runConfigurationTest() {
        try {
            this.showLoading('Running configuration test...');
            
            const guildId = this.stateManager.getState('currentGuildId');
            const config = this.stateManager.getState('currentConfig');
            
            const results = await this.dataService.testConfiguration(guildId, config);
            
            this.hideLoading();
            this.displayTestResults(results);
            
        } catch (error) {
            this.hideLoading();
            this.showAlert('Test failed: ' + error.message, 'danger');
        }
    }

    displayTestResults(results) {
        const container = document.getElementById('test-results');
        if (!container) return;
        
        container.innerHTML = `
            <div class="alert alert-${results.success ? 'success' : 'warning'}">
                <h5>Test ${results.success ? 'Passed' : 'Failed'}</h5>
                ${results.results.map(result => `
                    <div class="d-flex align-items-center mb-2">
                        <i class="fas fa-${result.passed ? 'check text-success' : 'times text-danger'} me-2"></i>
                        <strong>${result.test}:</strong> ${result.message}
                    </div>
                `).join('')}
            </div>
        `;
    }

    /**
     * UI Utility Functions
     */
    
    showAlert(message, type = 'info', duration = null) {
        const alertContainer = document.getElementById('alert-container');
        if (!alertContainer) {
            this.logger.warn('Alert container not found');
            return;
        }
        
        const alertId = Utils.generateId('alert');
        const alert = document.createElement('div');
        alert.id = alertId;
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${Utils.sanitizeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        alertContainer.appendChild(alert);
        
        // Auto-dismiss after configured duration
        const dismissDuration = duration || this.config.ui.notifications.duration;
        if (dismissDuration > 0) {
            setTimeout(() => {
                if (alert.parentNode) {
                    alert.remove();
                }
            }, dismissDuration);
        }
        
        // Limit number of visible alerts
        const alerts = alertContainer.querySelectorAll('.alert');
        if (alerts.length > this.config.ui.notifications.maxVisible) {
            alerts[0].remove();
        }
        
        this.logger.debug('Alert shown', { type, message, duration: dismissDuration });
    }
    
    showLoading(message = 'Loading...') {
        this.stateManager.setState('ui.loading', true);
        this.stateManager.setState('ui.loadingMessage', message);
        
        // Show loading overlay
        const loadingElement = document.querySelector('.loading-overlay');
        if (loadingElement) {
            loadingElement.style.display = 'flex';
            const messageElement = loadingElement.querySelector('.loading-message');
            if (messageElement) {
                messageElement.textContent = message;
            }
        }
        this.logger.debug('Loading indicator shown', { message });
    }

    hideLoading() {
        this.stateManager.setState('ui.loading', false);
        
        // Hide loading overlay
        const loadingElement = document.querySelector('.loading-overlay');
        if (loadingElement) {
            // Ensure minimum display time for better UX
            setTimeout(() => {
                loadingElement.style.display = 'none';
            }, this.config.ui.loading?.minDisplayTime || 100);
        }
        this.logger.debug('Loading indicator hidden');
    }
    
    /**
     * Error Handling
     */
    
    handleError(error, context = 'general') {
        this.logger.error(`Error in ${context}`, { 
            error: error.message, 
            stack: error.stack,
            context 
        });
        
        // Show user-friendly error message
        if (this.config.errorHandling?.showUserFriendlyMessages) {
            const userMessage = this.getUserFriendlyErrorMessage(error);
            this.showAlert(userMessage, 'danger');
        }
        
        // Emit error event
        this.eventBus.emit('dashboard:error', { error, context });
    }
    
    getUserFriendlyErrorMessage(error) {
        // Map technical errors to user-friendly messages
        const errorMessages = {
            'NetworkError': 'Unable to connect to the server. Please check your internet connection.',
            'TimeoutError': 'The request took too long to complete. Please try again.',
            'ValidationError': 'Please check your input and try again.',
            'AuthenticationError': 'You need to log in to access this feature.',
            'AuthorizationError': 'You don\'t have permission to perform this action.',
            'NotFoundError': 'The requested resource was not found.'
        };
        
        return errorMessages[error.name] || 'An unexpected error occurred. Please try again.';
    }



    throttle(func, delay) {
        let timeoutId;
        let lastExecTime = 0;
        
        return function (...args) {
            const currentTime = Date.now();
            
            if (currentTime - lastExecTime > delay) {
                func.apply(this, args);
                lastExecTime = currentTime;
            } else {
                clearTimeout(timeoutId);
                timeoutId = setTimeout(() => {
                    func.apply(this, args);
                    lastExecTime = Date.now();
                }, delay - (currentTime - lastExecTime));
            }
        };
    }

    /**
     * Destroy the dashboard
     */
    async destroy() {
        if (this.destroyed) return;
        
        try {
            // Destroy components
            for (const [name, component] of this.components) {
                if (component && typeof component.destroy === 'function') {
                    await component.destroy();
                }
            }
            this.components.clear();
            
            // Destroy services
            for (const [name, service] of this.services) {
                if (service && typeof service.destroy === 'function') {
                    await service.destroy();
                }
            }
            this.services.clear();
            
            // Clear global references
            if (window.EventBus === this.eventBus) {
                delete window.EventBus;
            }
            if (window.StateManager === this.stateManager) {
                delete window.StateManager;
            }
            
            this.destroyed = true;
            console.log('Dashboard destroyed');
            
        } catch (error) {
            console.error('Error during dashboard destruction:', error);
        }
    }

    /**
     * Get dashboard status
     */
    getStatus() {
        return {
            initialized: this.initialized,
            destroyed: this.destroyed,
            components: Array.from(this.components.keys()),
            services: Array.from(this.services.keys()),
            state: this.stateManager ? this.stateManager.getState() : null
        };
    }
}

export default ModerationDashboardV2;