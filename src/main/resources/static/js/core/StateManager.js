/**
 * State Manager - Centralized state management with reactive updates
 * Implements Redux-like pattern for predictable state management
 */
class StateManager {
    constructor() {
        this.state = {
            currentGuildId: null,
            currentConfig: null,
            guilds: [],
            charts: {},
            ui: {
                activeSection: 'overview',
                loading: false,
                alerts: []
            },
            filters: {
                spam: { enabled: false, threshold: 0.7 },
                toxicity: { enabled: false, threshold: 0.6 },
                links: { enabled: false },
                words: { enabled: false, list: [] }
            },
            actions: {
                warn: { enabled: true },
                mute: { enabled: false, duration: '10m' },
                kick: { enabled: false },
                ban: { enabled: false }
            },
            customRules: [],
            thresholds: {
                violations: 3,
                timeWindow: '1h'
            },
            analytics: {
                totalViolations: 0,
                recentActivity: []
            }
        };
        
        this.subscribers = new Map();
        this.middleware = [];
        this.history = [];
        this.maxHistorySize = 50;
    }

    /**
     * Subscribe to state changes
     * @param {string} path - State path to watch (e.g., 'ui.loading')
     * @param {Function} callback - Callback function
     * @param {Object} options - Subscription options
     */
    subscribe(path, callback, options = {}) {
        const { immediate = false, deep = false } = options;
        
        if (!this.subscribers.has(path)) {
            this.subscribers.set(path, []);
        }
        
        const subscription = { callback, deep, id: Date.now() + Math.random() };
        this.subscribers.get(path).push(subscription);
        
        // Call immediately with current value if requested
        if (immediate) {
            const currentValue = this.getState(path);
            callback(currentValue, null, path);
        }
        
        // Return unsubscribe function
        return () => this.unsubscribe(path, subscription.id);
    }

    /**
     * Unsubscribe from state changes
     * @param {string} path - State path
     * @param {string} subscriptionId - Subscription ID
     */
    unsubscribe(path, subscriptionId) {
        if (this.subscribers.has(path)) {
            const subscriptions = this.subscribers.get(path);
            const index = subscriptions.findIndex(sub => sub.id === subscriptionId);
            if (index !== -1) {
                subscriptions.splice(index, 1);
            }
        }
    }

    /**
     * Get state value by path
     * @param {string} path - State path (e.g., 'ui.loading')
     * @returns {*} State value
     */
    getState(path = '') {
        if (!path) return this.state;
        
        return path.split('.').reduce((obj, key) => {
            return obj && obj[key] !== undefined ? obj[key] : undefined;
        }, this.state);
    }

    /**
     * Set state value
     * @param {string} path - State path
     * @param {*} value - New value
     * @param {Object} options - Update options
     */
    setState(path, value, options = {}) {
        const { silent = false, merge = false } = options;
        const oldValue = this.getState(path);
        
        // Create action for history
        const action = {
            type: 'SET_STATE',
            path,
            value,
            oldValue,
            timestamp: Date.now()
        };
        
        // Apply middleware
        const processedAction = this.applyMiddleware(action);
        if (!processedAction) return; // Middleware can cancel the action
        
        // Update state
        this.updateStateByPath(path, value, merge);
        
        // Add to history
        this.addToHistory(processedAction);
        
        // Notify subscribers
        if (!silent) {
            this.notifySubscribers(path, value, oldValue);
        }
        
        // Emit global state change event
        if (window.EventBus) {
            window.EventBus.emit('state:changed', { path, value, oldValue });
        }
    }

    /**
     * Update state by path
     * @param {string} path - State path
     * @param {*} value - New value
     * @param {boolean} merge - Whether to merge objects
     */
    updateStateByPath(path, value, merge = false) {
        const keys = path.split('.');
        const lastKey = keys.pop();
        
        // Navigate to parent object
        const parent = keys.reduce((obj, key) => {
            if (!obj[key]) obj[key] = {};
            return obj[key];
        }, this.state);
        
        // Set value
        if (merge && typeof parent[lastKey] === 'object' && typeof value === 'object') {
            parent[lastKey] = { ...parent[lastKey], ...value };
        } else {
            parent[lastKey] = value;
        }
    }

    /**
     * Notify subscribers of state changes
     * @param {string} path - Changed path
     * @param {*} newValue - New value
     * @param {*} oldValue - Old value
     */
    notifySubscribers(path, newValue, oldValue) {
        // Notify exact path subscribers
        if (this.subscribers.has(path)) {
            this.subscribers.get(path).forEach(({ callback }) => {
                try {
                    callback(newValue, oldValue, path);
                } catch (error) {
                    console.error(`Error in state subscriber for '${path}':`, error);
                }
            });
        }
        
        // Notify parent path subscribers with deep watching
        const pathParts = path.split('.');
        for (let i = pathParts.length - 1; i > 0; i--) {
            const parentPath = pathParts.slice(0, i).join('.');
            if (this.subscribers.has(parentPath)) {
                this.subscribers.get(parentPath)
                    .filter(sub => sub.deep)
                    .forEach(({ callback }) => {
                        try {
                            callback(this.getState(parentPath), null, parentPath);
                        } catch (error) {
                            console.error(`Error in deep state subscriber for '${parentPath}':`, error);
                        }
                    });
            }
        }
    }

    /**
     * Add middleware
     * @param {Function} middleware - Middleware function
     */
    addMiddleware(middleware) {
        this.middleware.push(middleware);
    }

    /**
     * Apply middleware to action
     * @param {Object} action - Action object
     * @returns {Object|null} Processed action or null to cancel
     */
    applyMiddleware(action) {
        return this.middleware.reduce((acc, middleware) => {
            if (!acc) return null;
            try {
                return middleware(acc, this.state);
            } catch (error) {
                console.error('Error in middleware:', error);
                return acc;
            }
        }, action);
    }

    /**
     * Add action to history
     * @param {Object} action - Action object
     */
    addToHistory(action) {
        this.history.push(action);
        if (this.history.length > this.maxHistorySize) {
            this.history.shift();
        }
    }

    /**
     * Get state history
     * @returns {Array} History array
     */
    getHistory() {
        return [...this.history];
    }

    /**
     * Reset state to initial values
     */
    reset() {
        const oldState = { ...this.state };
        this.state = {
            currentGuildId: null,
            currentConfig: null,
            guilds: [],
            charts: {},
            ui: {
                activeSection: 'overview',
                loading: false,
                alerts: []
            },
            filters: {
                spam: { enabled: false, threshold: 0.7 },
                toxicity: { enabled: false, threshold: 0.6 },
                links: { enabled: false },
                words: { enabled: false, list: [] }
            },
            actions: {
                warn: { enabled: true },
                mute: { enabled: false, duration: '10m' },
                kick: { enabled: false },
                ban: { enabled: false }
            },
            customRules: [],
            thresholds: {
                violations: 3,
                timeWindow: '1h'
            },
            analytics: {
                totalViolations: 0,
                recentActivity: []
            }
        };
        
        // Notify all subscribers
        this.subscribers.forEach((subscriptions, path) => {
            const newValue = this.getState(path);
            const oldValue = this.getStateFromObject(oldState, path);
            subscriptions.forEach(({ callback }) => {
                try {
                    callback(newValue, oldValue, path);
                } catch (error) {
                    console.error(`Error in state subscriber for '${path}':`, error);
                }
            });
        });
    }

    /**
     * Get state value from object by path
     * @param {Object} obj - Object to search
     * @param {string} path - Path to value
     * @returns {*} Value
     */
    getStateFromObject(obj, path) {
        return path.split('.').reduce((current, key) => {
            return current && current[key] !== undefined ? current[key] : undefined;
        }, obj);
    }

    /**
     * Batch multiple state updates
     * @param {Function} updateFn - Function that performs multiple setState calls
     */
    batch(updateFn) {
        const originalNotify = this.notifySubscribers;
        const batchedNotifications = new Map();
        
        // Temporarily override notification to batch them
        this.notifySubscribers = (path, newValue, oldValue) => {
            batchedNotifications.set(path, { newValue, oldValue });
        };
        
        try {
            updateFn();
        } finally {
            // Restore original notification method
            this.notifySubscribers = originalNotify;
            
            // Send all batched notifications
            batchedNotifications.forEach(({ newValue, oldValue }, path) => {
                this.notifySubscribers(path, newValue, oldValue);
            });
        }
    }
}

// Create global state manager instance
window.StateManager = new StateManager();

export default StateManager;