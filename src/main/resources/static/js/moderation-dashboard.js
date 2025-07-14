/**
 * Moderation Dashboard Main Module
 * Handles core dashboard functionality and initialization
 */

class ModerationDashboard {
    constructor() {
        this.currentGuildId = null;
        this.currentConfig = null;
        this.charts = {};
        this.autoSaveTimeout = null;
        
        this.init();
    }

    async init() {
        await this.waitForDOM();
        this.setupEventListeners();
        this.loadInitialData();
        this.initializeCharts();
        this.setupAutoSave();
    }

    waitForDOM() {
        return new Promise(resolve => {
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', resolve);
            } else {
                resolve();
            }
        });
    }

    setupEventListeners() {
        // Navigation
        document.addEventListener('click', this.handleNavigation.bind(this));
        
        // Form changes
        document.addEventListener('change', this.handleFormChange.bind(this));
        document.addEventListener('input', this.handleFormInput.bind(this));
        
        // Button clicks
        document.addEventListener('click', this.handleButtonClick.bind(this));
        
        // Modal events
        document.addEventListener('click', this.handleModalEvents.bind(this));
        
        // Range sliders
        this.setupRangeSliders();
    }

    handleNavigation(event) {
        const navItem = event.target.closest('.nav-item');
        if (!navItem) return;
        
        event.preventDefault();
        const section = navItem.getAttribute('data-section');
        if (section) {
            this.showSection(section);
            this.updateActiveNav(navItem);
        }
    }

    handleFormChange(event) {
        if (event.target.matches('input, select, textarea')) {
            this.scheduleAutoSave();
        }
    }

    handleFormInput(event) {
        if (event.target.type === 'range') {
            this.updateRangeDisplay(event.target);
        }
    }

    handleButtonClick(event) {
        const button = event.target.closest('button');
        if (!button) return;

        const action = button.getAttribute('data-action');
        switch (action) {
            case 'save-config':
                this.saveConfiguration();
                break;
            case 'test-config':
                this.runConfigurationTest();
                break;
            case 'export-config':
                this.exportConfiguration();
                break;
            case 'add-rule':
                this.showAddRuleModal();
                break;
            case 'delete-rule':
                this.deleteRule(button.getAttribute('data-index'));
                break;
            case 'toggle-rule':
                this.toggleRule(button.getAttribute('data-index'));
                break;
        }
    }

    handleModalEvents(event) {
        if (event.target.classList.contains('modal')) {
            this.closeModal(event.target);
        }
        
        if (event.target.classList.contains('close') || 
            event.target.closest('.close')) {
            const modal = event.target.closest('.modal');
            if (modal) this.closeModal(modal);
        }
    }

    setupRangeSliders() {
        const ranges = document.querySelectorAll('input[type="range"]');
        ranges.forEach(range => {
            this.updateRangeDisplay(range);
            range.addEventListener('input', this.throttle((e) => {
                this.updateRangeDisplay(e.target);
            }, 16)); // ~60fps
        });
    }

    updateRangeDisplay(range) {
        const display = document.getElementById(range.id + 'Display');
        if (display) {
            display.textContent = range.value;
        }
    }

    showSection(sectionId) {
        // Hide all sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.classList.remove('active');
        });
        
        // Show target section
        const targetSection = document.getElementById(sectionId);
        if (targetSection) {
            targetSection.classList.add('active');
        }
    }

    updateActiveNav(activeItem) {
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        activeItem.classList.add('active');
    }

    async loadInitialData() {
        try {
            await this.loadGuildList();
            
            // Load first guild if available
            const guildSelect = document.getElementById('guildSelect');
            if (guildSelect && guildSelect.options.length > 1) {
                guildSelect.selectedIndex = 1;
                await this.loadGuildConfiguration(guildSelect.value);
            }
        } catch (error) {
            console.error('Failed to load initial data:', error);
            this.showAlert('Failed to load initial data', 'danger');
        }
    }

    async loadGuildList() {
        try {
            const response = await fetch('/api/moderation/guilds');
            const guilds = await response.json();
            
            const guildSelect = document.getElementById('guildSelect');
            if (guildSelect) {
                guildSelect.innerHTML = '<option value="">Select a guild...</option>';
                guilds.forEach(guild => {
                    const option = document.createElement('option');
                    option.value = guild.id;
                    option.textContent = guild.name;
                    guildSelect.appendChild(option);
                });
                
                guildSelect.addEventListener('change', (e) => {
                    if (e.target.value) {
                        this.loadGuildConfiguration(e.target.value);
                    }
                });
            }
        } catch (error) {
            console.error('Failed to load guild list:', error);
            // Use mock data for development
            this.loadMockGuildList();
        }
    }

    loadMockGuildList() {
        const guildSelect = document.getElementById('guildSelect');
        if (guildSelect) {
            const mockGuilds = [
                { id: '123456789', name: 'Test Server 1' },
                { id: '987654321', name: 'Test Server 2' },
                { id: '456789123', name: 'Development Server' }
            ];
            
            guildSelect.innerHTML = '<option value="">Select a guild...</option>';
            mockGuilds.forEach(guild => {
                const option = document.createElement('option');
                option.value = guild.id;
                option.textContent = guild.name;
                guildSelect.appendChild(option);
            });
            
            guildSelect.addEventListener('change', (e) => {
                if (e.target.value) {
                    this.loadGuildConfiguration(e.target.value);
                }
            });
        }
    }

    async loadGuildConfiguration(guildId) {
        this.currentGuildId = guildId;
        this.showLoading(true);
        
        try {
            const response = await fetch(`/api/moderation/config/guild/${guildId}`);
            const config = await response.json();
            
            this.currentConfig = config;
            this.populateConfigurationForm(config);
            this.updateMetrics(config);
            
        } catch (error) {
            console.error('Failed to load guild configuration:', error);
            // Use mock data for development
            this.loadMockConfiguration();
        } finally {
            this.showLoading(false);
        }
    }

    loadMockConfiguration() {
        const mockConfig = {
            guildId: this.currentGuildId,
            enabled: true,
            filters: {
                spam: {
                    enabled: true,
                    threshold: 0.7,
                    maxMessages: 5,
                    timeWindow: '1m',
                    checkDuplicates: true,
                    checkRapidTyping: true
                },
                toxicity: {
                    enabled: true,
                    threshold: 0.6,
                    languages: ['en'],
                    useAI: true,
                    checkSentiment: true,
                    checkContext: false
                },
                link: {
                    enabled: false,
                    maxLinks: 2,
                    blockedDomains: [],
                    checkShorteners: true,
                    checkReputation: true
                },
                word: {
                    enabled: false,
                    bannedWords: [],
                    useRegex: false,
                    caseSensitive: false,
                    wholeWordsOnly: true
                }
            },
            actions: {
                warn: { enabled: true, message: 'Please follow server rules' },
                mute: { enabled: true, duration: '10m' },
                kick: { enabled: false, reason: 'Violation of server rules' },
                ban: { enabled: false, reason: 'Severe violation', deleteMessages: false }
            },
            customRules: [],
            thresholds: {
                violations: {
                    warn: 1,
                    mute: 3,
                    kick: 5,
                    ban: 10
                },
                timeWindow: '24h'
            },
            ui: {
                theme: 'light',
                language: 'en',
                layout: 'default'
            },
            settings: {
                logChannel: null,
                moderatorRole: null
            }
        };
        
        this.currentConfig = mockConfig;
        this.populateConfigurationForm(mockConfig);
        this.updateMetrics(mockConfig);
    }

    populateConfigurationForm(config) {
        if (!config) return;
        
        // Basic settings
        this.setElementValue('moderationEnabled', config.enabled);
        
        // Filters
        this.populateFilterSettings(config.filters);
        
        // Actions
        this.populateActionSettings(config.actions);
        
        // Custom rules
        this.populateCustomRules(config.customRules);
        
        // Thresholds
        this.populateThresholds(config.thresholds);
        
        // UI settings
        this.populateUISettings(config.ui);
        
        // General settings
        this.populateGeneralSettings(config.settings);
    }

    populateFilterSettings(filters) {
        if (!filters) return;
        
        Object.keys(filters).forEach(filterType => {
            const filter = filters[filterType];
            const prefix = filterType.charAt(0).toUpperCase() + filterType.slice(1);
            
            this.setElementValue(`${filterType}FilterEnabled`, filter.enabled);
            
            if (filter.threshold !== undefined) {
                this.setElementValue(`${filterType}Threshold`, filter.threshold);
            }
            
            // Specific filter settings
            Object.keys(filter).forEach(key => {
                if (key !== 'enabled') {
                    this.setElementValue(`${filterType}${key.charAt(0).toUpperCase() + key.slice(1)}`, filter[key]);
                }
            });
        });
    }

    populateActionSettings(actions) {
        if (!actions) return;
        
        Object.keys(actions).forEach(actionType => {
            const action = actions[actionType];
            this.setElementValue(`action${actionType}`, action.enabled);
            
            Object.keys(action).forEach(key => {
                if (key !== 'enabled') {
                    this.setElementValue(`${actionType}${key.charAt(0).toUpperCase() + key.slice(1)}`, action[key]);
                }
            });
        });
    }

    populateCustomRules(rules) {
        const container = document.getElementById('customRulesList');
        if (!container || !rules) return;
        
        const fragment = document.createDocumentFragment();
        
        rules.forEach((rule, index) => {
            const ruleElement = this.createCustomRuleElement(rule, index);
            fragment.appendChild(ruleElement);
        });
        
        container.innerHTML = '';
        container.appendChild(fragment);
    }

    populateThresholds(thresholds) {
        if (!thresholds) return;
        
        if (thresholds.violations) {
            Object.keys(thresholds.violations).forEach(type => {
                this.setElementValue(`threshold${type.charAt(0).toUpperCase() + type.slice(1)}`, thresholds.violations[type]);
            });
        }
        
        this.setElementValue('thresholdTimeWindow', thresholds.timeWindow);
    }

    populateUISettings(ui) {
        if (!ui) return;
        
        this.setElementValue('uiTheme', ui.theme);
        this.setElementValue('uiLanguage', ui.language);
        this.setElementValue('dashboardLayout', ui.layout);
    }

    populateGeneralSettings(settings) {
        if (!settings) return;
        
        this.setElementValue('logChannel', settings.logChannel);
        this.setElementValue('moderatorRole', settings.moderatorRole);
    }

    setElementValue(elementId, value) {
        const element = document.getElementById(elementId);
        if (!element) return;
        
        if (element.type === 'checkbox') {
            element.checked = Boolean(value);
        } else if (element.tagName === 'SELECT' && Array.isArray(value)) {
            Array.from(element.options).forEach(option => {
                option.selected = value.includes(option.value);
            });
        } else if (element.tagName === 'TEXTAREA' && Array.isArray(value)) {
            element.value = value.join('\n');
        } else {
            element.value = value || '';
        }
        
        // Update range display if applicable
        if (element.type === 'range') {
            this.updateRangeDisplay(element);
        }
    }

    createCustomRuleElement(rule, index) {
        const div = document.createElement('div');
        div.className = 'card mb-3';
        div.innerHTML = `
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <h6 class="card-title">${rule.name}</h6>
                        <p class="card-text text-muted">${rule.description}</p>
                        <div class="d-flex gap-2">
                            <span class="badge bg-primary">${rule.type}</span>
                            <span class="badge ${rule.enabled ? 'bg-success' : 'bg-secondary'}">
                                ${rule.enabled ? 'Enabled' : 'Disabled'}
                            </span>
                        </div>
                    </div>
                    <div class="d-flex gap-2">
                        <button class="btn btn-sm btn-outline-primary" data-action="toggle-rule" data-index="${index}">
                            <i class="fas ${rule.enabled ? 'fa-pause' : 'fa-play'}"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" data-action="delete-rule" data-index="${index}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
        return div;
    }

    updateMetrics(config) {
        if (!config) return;
        
        // Calculate metrics
        const enabledFilters = Object.values(config.filters || {}).filter(f => f.enabled).length;
        const enabledActions = Object.values(config.actions || {}).filter(a => a.enabled).length;
        const customRulesCount = (config.customRules || []).length;
        
        // Update stat cards
        this.updateStatCard('filtersCount', enabledFilters);
        this.updateStatCard('actionsCount', enabledActions);
        this.updateStatCard('rulesCount', customRulesCount);
        this.updateStatCard('configStatus', config.enabled ? 'Active' : 'Inactive');
    }

    updateStatCard(cardId, value) {
        const card = document.getElementById(cardId);
        if (card) {
            const numberElement = card.querySelector('.stat-number');
            if (numberElement) {
                numberElement.textContent = value;
            }
        }
    }

    initializeCharts() {
        // Use IntersectionObserver for lazy loading
        const chartContainers = document.querySelectorAll('.chart-container canvas');
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    this.loadChart(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        });
        
        chartContainers.forEach(canvas => observer.observe(canvas));
    }

    loadChart(canvas) {
        const chartId = canvas.id;
        
        if (chartId === 'activityChart') {
            this.charts.activity = new Chart(canvas, {
                type: 'line',
                data: {
                    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                    datasets: [{
                        label: 'Violations Detected',
                        data: [12, 19, 3, 5, 2, 3, 7],
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.1)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        } else if (chartId === 'analyticsChart') {
            this.charts.analytics = new Chart(canvas, {
                type: 'doughnut',
                data: {
                    labels: ['Spam', 'Toxicity', 'Links', 'Words'],
                    datasets: [{
                        data: [30, 25, 20, 25],
                        backgroundColor: [
                            '#FF6384',
                            '#36A2EB',
                            '#FFCE56',
                            '#4BC0C0'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
        }
    }

    setupAutoSave() {
        this.autoSave = this.debounce(() => {
            if (this.currentGuildId && this.currentConfig) {
                this.saveConfiguration();
            }
        }, 5000);
    }

    scheduleAutoSave() {
        this.autoSave();
    }

    async saveConfiguration() {
        if (!this.currentGuildId) {
            this.showAlert('Please select a guild first', 'warning');
            return;
        }
        
        try {
            const config = this.collectConfigurationFromForm();
            
            const response = await fetch(`/api/moderation/config/guild/${this.currentGuildId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(config)
            });
            
            if (response.ok) {
                const result = await response.json();
                this.currentConfig = result;
                this.showAlert('Configuration saved successfully', 'success');
            } else {
                this.showAlert('Failed to save configuration', 'danger');
            }
            
        } catch (error) {
            console.error('Failed to save configuration:', error);
            this.showAlert('Failed to save configuration', 'danger');
        }
    }

    collectConfigurationFromForm() {
        // Implementation similar to original but more modular
        return {
            guildId: this.currentGuildId,
            enabled: this.getElementValue('moderationEnabled', 'boolean'),
            filters: this.collectFilters(),
            actions: this.collectActions(),
            customRules: this.currentConfig?.customRules || [],
            thresholds: this.collectThresholds(),
            ui: this.collectUISettings(),
            settings: this.collectGeneralSettings()
        };
    }

    collectFilters() {
        const filters = {};
        const filterTypes = ['spam', 'toxicity', 'link', 'word'];
        
        filterTypes.forEach(type => {
            filters[type] = {
                enabled: this.getElementValue(`${type}FilterEnabled`, 'boolean')
            };
            
            // Add type-specific settings
            if (type === 'spam') {
                Object.assign(filters[type], {
                    threshold: this.getElementValue(`${type}Threshold`, 'number'),
                    maxMessages: this.getElementValue(`${type}MaxMessages`, 'number'),
                    timeWindow: this.getElementValue(`${type}TimeWindow`),
                    checkDuplicates: this.getElementValue(`${type}CheckDuplicates`, 'boolean'),
                    checkRapidTyping: this.getElementValue(`${type}CheckRapidTyping`, 'boolean')
                });
            }
            // Add other filter types as needed
        });
        
        return filters;
    }

    collectActions() {
        const actions = {};
        const actionTypes = ['warn', 'mute', 'kick', 'ban'];
        
        actionTypes.forEach(type => {
            actions[type] = {
                enabled: this.getElementValue(`action${type}`, 'boolean')
            };
            
            // Add type-specific settings
            if (type === 'warn') {
                actions[type].message = this.getElementValue(`${type}Message`);
            } else if (type === 'mute') {
                actions[type].duration = this.getElementValue(`${type}Duration`);
            }
            // Add other action types as needed
        });
        
        return actions;
    }

    collectThresholds() {
        return {
            violations: {
                warn: this.getElementValue('thresholdWarn', 'number'),
                mute: this.getElementValue('thresholdMute', 'number'),
                kick: this.getElementValue('thresholdKick', 'number'),
                ban: this.getElementValue('thresholdBan', 'number')
            },
            timeWindow: this.getElementValue('thresholdTimeWindow')
        };
    }

    collectUISettings() {
        return {
            theme: this.getElementValue('uiTheme'),
            language: this.getElementValue('uiLanguage'),
            layout: this.getElementValue('dashboardLayout')
        };
    }

    collectGeneralSettings() {
        return {
            logChannel: this.getElementValue('logChannel'),
            moderatorRole: this.getElementValue('moderatorRole')
        };
    }

    getElementValue(elementId, type = 'string') {
        const element = document.getElementById(elementId);
        if (!element) return type === 'boolean' ? false : type === 'number' ? 0 : '';
        
        switch (type) {
            case 'boolean':
                return element.type === 'checkbox' ? element.checked : Boolean(element.value);
            case 'number':
                return parseFloat(element.value) || 0;
            case 'array':
                return element.value ? element.value.split('\n').filter(v => v.trim()) : [];
            default:
                return element.value || '';
        }
    }

    async runConfigurationTest() {
        const userId = this.getElementValue('testUserId');
        const content = this.getElementValue('testContent');
        
        if (!userId || !content) {
            this.showAlert('Please enter both user ID and test message', 'warning');
            return;
        }
        
        if (!this.currentGuildId) {
            this.showAlert('Please select a guild first', 'warning');
            return;
        }
        
        try {
            const response = await fetch(`/api/moderation/config/guild/${this.currentGuildId}/test`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: userId,
                    content: content,
                    configuration: this.collectConfigurationFromForm()
                })
            });
            
            const result = await response.json();
            this.displayTestResults(result);
            
        } catch (error) {
            console.error('Failed to run test:', error);
            this.showAlert('Failed to run configuration test', 'danger');
        }
    }

    displayTestResults(result) {
        const testResults = document.getElementById('testResults');
        if (!testResults) return;
        
        if (result.success) {
            const violations = result.violations || [];
            const actions = result.actions || [];
            
            let html = '<div class="test-result-success">';
            
            if (violations.length > 0) {
                html += '<h6 class="text-danger">Violations Detected:</h6><ul>';
                violations.forEach(violation => {
                    html += `<li><strong>${violation.type}:</strong> ${violation.reason} (Confidence: ${(violation.confidence * 100).toFixed(1)}%)</li>`;
                });
                html += '</ul>';
            } else {
                html += '<p class="text-success"><i class="fas fa-check-circle me-2"></i>No violations detected</p>';
            }
            
            if (actions.length > 0) {
                html += '<h6 class="text-warning">Actions Triggered:</h6><ul>';
                actions.forEach(action => {
                    html += `<li><strong>${action.type}:</strong> ${action.reason}</li>`;
                });
                html += '</ul>';
            }
            
            html += '</div>';
            testResults.innerHTML = html;
        } else {
            testResults.innerHTML = `<div class="alert alert-danger">Test failed: ${result.message}</div>`;
        }
    }

    exportConfiguration() {
        if (!this.currentConfig) {
            this.showAlert('No configuration to export', 'warning');
            return;
        }
        
        const dataStr = JSON.stringify(this.currentConfig, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });
        
        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = `moderation-config-${this.currentGuildId}.json`;
        link.click();
        
        this.showAlert('Configuration exported successfully', 'success');
    }

    showAddRuleModal() {
        const modal = document.getElementById('addRuleModal');
        if (modal) {
            modal.style.display = 'block';
            modal.classList.add('show');
        }
    }

    closeModal(modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
    }

    toggleRule(index) {
        if (this.currentConfig?.customRules?.[index]) {
            this.currentConfig.customRules[index].enabled = !this.currentConfig.customRules[index].enabled;
            this.populateCustomRules(this.currentConfig.customRules);
            this.updateMetrics(this.currentConfig);
        }
    }

    deleteRule(index) {
        if (confirm('Are you sure you want to delete this rule?')) {
            if (this.currentConfig?.customRules) {
                this.currentConfig.customRules.splice(index, 1);
                this.populateCustomRules(this.currentConfig.customRules);
                this.updateMetrics(this.currentConfig);
            }
        }
    }

    showAlert(message, type = 'info') {
        const alertContainer = document.getElementById('alertContainer') || document.body;
        
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        alertContainer.appendChild(alert);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 5000);
    }

    showLoading(show = true) {
        const loader = document.getElementById('loadingSpinner');
        if (loader) {
            loader.style.display = show ? 'block' : 'none';
        }
    }

    // Utility functions
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
}

// Initialize dashboard when DOM is ready
const dashboard = new ModerationDashboard();

// Export for global access if needed
window.ModerationDashboard = dashboard;