/**
 * Logger Utility
 * Structured logging system for the Moderation Dashboard
 */

export default class Logger {
    constructor(options = {}) {
        this.level = options.level || 'info';
        this.prefix = options.prefix || 'Dashboard';
        this.enableConsole = options.enableConsole !== false;
        this.enableStorage = options.enableStorage || false;
        this.maxStorageEntries = options.maxStorageEntries || 1000;
        this.storageKey = options.storageKey || 'dashboard_logs';
        
        this.levels = {
            debug: 0,
            info: 1,
            warn: 2,
            error: 3
        };
        
        this.colors = {
            debug: '#6c757d',
            info: '#007bff',
            warn: '#ffc107',
            error: '#dc3545'
        };
        
        this.logs = [];
        this.loadStoredLogs();
    }
    
    /**
     * Set log level
     * @param {string} level - Log level (debug, info, warn, error)
     */
    setLevel(level) {
        if (this.levels.hasOwnProperty(level)) {
            this.level = level;
        }
    }
    
    /**
     * Check if level should be logged
     * @param {string} level - Level to check
     * @returns {boolean} Should log
     */
    shouldLog(level) {
        return this.levels[level] >= this.levels[this.level];
    }
    
    /**
     * Format log message
     * @param {string} level - Log level
     * @param {string} message - Log message
     * @param {*} data - Additional data
     * @returns {Object} Formatted log entry
     */
    formatMessage(level, message, data = null) {
        return {
            timestamp: new Date().toISOString(),
            level: level.toUpperCase(),
            prefix: this.prefix,
            message,
            data,
            stack: level === 'error' ? new Error().stack : null
        };
    }
    
    /**
     * Log to console
     * @param {Object} logEntry - Log entry
     */
    logToConsole(logEntry) {
        if (!this.enableConsole) return;
        
        const { timestamp, level, prefix, message, data } = logEntry;
        const color = this.colors[level.toLowerCase()];
        const timeStr = new Date(timestamp).toLocaleTimeString();
        
        const args = [
            `%c[${timeStr}] ${prefix} ${level}:%c ${message}`,
            `color: ${color}; font-weight: bold;`,
            'color: inherit;'
        ];
        
        if (data !== null) {
            args.push(data);
        }
        
        const consoleMethod = level.toLowerCase() === 'error' ? 'error' :
                             level.toLowerCase() === 'warn' ? 'warn' :
                             level.toLowerCase() === 'debug' ? 'debug' : 'log';
        
        console[consoleMethod](...args);
    }
    
    /**
     * Store log entry
     * @param {Object} logEntry - Log entry
     */
    storeLog(logEntry) {
        this.logs.push(logEntry);
        
        // Limit stored logs
        if (this.logs.length > this.maxStorageEntries) {
            this.logs = this.logs.slice(-this.maxStorageEntries);
        }
        
        if (this.enableStorage) {
            this.saveLogsToStorage();
        }
    }
    
    /**
     * Save logs to localStorage
     */
    saveLogsToStorage() {
        try {
            const logsToStore = this.logs.slice(-100); // Store only last 100 logs
            localStorage.setItem(this.storageKey, JSON.stringify(logsToStore));
        } catch (error) {
            console.warn('Failed to save logs to storage:', error);
        }
    }
    
    /**
     * Load logs from localStorage
     */
    loadStoredLogs() {
        if (!this.enableStorage) return;
        
        try {
            const stored = localStorage.getItem(this.storageKey);
            if (stored) {
                this.logs = JSON.parse(stored);
            }
        } catch (error) {
            console.warn('Failed to load logs from storage:', error);
        }
    }
    
    /**
     * Clear stored logs
     */
    clearLogs() {
        this.logs = [];
        if (this.enableStorage) {
            localStorage.removeItem(this.storageKey);
        }
    }
    
    /**
     * Get stored logs
     * @param {Object} filters - Filter options
     * @returns {Array} Filtered logs
     */
    getLogs(filters = {}) {
        let filteredLogs = [...this.logs];
        
        if (filters.level) {
            const minLevel = this.levels[filters.level];
            filteredLogs = filteredLogs.filter(log => 
                this.levels[log.level.toLowerCase()] >= minLevel
            );
        }
        
        if (filters.since) {
            const since = new Date(filters.since);
            filteredLogs = filteredLogs.filter(log => 
                new Date(log.timestamp) >= since
            );
        }
        
        if (filters.search) {
            const search = filters.search.toLowerCase();
            filteredLogs = filteredLogs.filter(log => 
                log.message.toLowerCase().includes(search) ||
                (log.data && JSON.stringify(log.data).toLowerCase().includes(search))
            );
        }
        
        return filteredLogs;
    }
    
    /**
     * Export logs
     * @param {string} format - Export format (json, csv, txt)
     * @param {Object} filters - Filter options
     * @returns {string} Exported data
     */
    exportLogs(format = 'json', filters = {}) {
        const logs = this.getLogs(filters);
        
        switch (format.toLowerCase()) {
            case 'csv':
                return this.exportToCsv(logs);
            case 'txt':
                return this.exportToTxt(logs);
            case 'json':
            default:
                return JSON.stringify(logs, null, 2);
        }
    }
    
    /**
     * Export logs to CSV format
     * @param {Array} logs - Logs to export
     * @returns {string} CSV data
     */
    exportToCsv(logs) {
        const headers = ['Timestamp', 'Level', 'Prefix', 'Message', 'Data'];
        const rows = logs.map(log => [
            log.timestamp,
            log.level,
            log.prefix,
            `"${log.message.replace(/"/g, '""')}"`,
            log.data ? `"${JSON.stringify(log.data).replace(/"/g, '""')}"` : ''
        ]);
        
        return [headers, ...rows].map(row => row.join(',')).join('\n');
    }
    
    /**
     * Export logs to text format
     * @param {Array} logs - Logs to export
     * @returns {string} Text data
     */
    exportToTxt(logs) {
        return logs.map(log => {
            let line = `[${log.timestamp}] ${log.prefix} ${log.level}: ${log.message}`;
            if (log.data) {
                line += `\nData: ${JSON.stringify(log.data, null, 2)}`;
            }
            if (log.stack) {
                line += `\nStack: ${log.stack}`;
            }
            return line;
        }).join('\n\n');
    }
    
    /**
     * Log debug message
     * @param {string} message - Message to log
     * @param {*} data - Additional data
     */
    debug(message, data = null) {
        if (!this.shouldLog('debug')) return;
        
        const logEntry = this.formatMessage('debug', message, data);
        this.logToConsole(logEntry);
        this.storeLog(logEntry);
    }
    
    /**
     * Log info message
     * @param {string} message - Message to log
     * @param {*} data - Additional data
     */
    info(message, data = null) {
        if (!this.shouldLog('info')) return;
        
        const logEntry = this.formatMessage('info', message, data);
        this.logToConsole(logEntry);
        this.storeLog(logEntry);
    }
    
    /**
     * Log warning message
     * @param {string} message - Message to log
     * @param {*} data - Additional data
     */
    warn(message, data = null) {
        if (!this.shouldLog('warn')) return;
        
        const logEntry = this.formatMessage('warn', message, data);
        this.logToConsole(logEntry);
        this.storeLog(logEntry);
    }
    
    /**
     * Log error message
     * @param {string} message - Message to log
     * @param {*} data - Additional data
     */
    error(message, data = null) {
        if (!this.shouldLog('error')) return;
        
        const logEntry = this.formatMessage('error', message, data);
        this.logToConsole(logEntry);
        this.storeLog(logEntry);
    }
    
    /**
     * Log performance timing
     * @param {string} operation - Operation name
     * @param {number} startTime - Start time (performance.now())
     * @param {*} data - Additional data
     */
    timing(operation, startTime, data = null) {
        const duration = performance.now() - startTime;
        const message = `${operation} completed in ${duration.toFixed(2)}ms`;
        
        const logData = {
            operation,
            duration,
            ...data
        };
        
        if (duration > 1000) {
            this.warn(message, logData);
        } else {
            this.debug(message, logData);
        }
    }
    
    /**
     * Create a child logger with additional context
     * @param {string} context - Context name
     * @param {Object} options - Additional options
     * @returns {Logger} Child logger
     */
    child(context, options = {}) {
        return new Logger({
            ...options,
            level: this.level,
            prefix: `${this.prefix}:${context}`,
            enableConsole: this.enableConsole,
            enableStorage: this.enableStorage,
            maxStorageEntries: this.maxStorageEntries,
            storageKey: this.storageKey
        });
    }
    
    /**
     * Create performance timer
     * @param {string} operation - Operation name
     * @returns {Function} Timer function
     */
    timer(operation) {
        const startTime = performance.now();
        return (data = null) => {
            this.timing(operation, startTime, data);
        };
    }
    
    /**
     * Log method execution (decorator)
     * @param {string} className - Class name
     * @param {string} methodName - Method name
     * @returns {Function} Decorator function
     */
    logMethod(className, methodName) {
        return (target, propertyKey, descriptor) => {
            const originalMethod = descriptor.value;
            
            descriptor.value = function(...args) {
                const timer = this.timer(`${className}.${methodName}`);
                
                try {
                    const result = originalMethod.apply(this, args);
                    
                    if (result instanceof Promise) {
                        return result
                            .then(res => {
                                timer({ success: true });
                                return res;
                            })
                            .catch(err => {
                                this.error(`${className}.${methodName} failed`, { error: err.message });
                                timer({ success: false, error: err.message });
                                throw err;
                            });
                    } else {
                        timer({ success: true });
                        return result;
                    }
                } catch (error) {
                    this.error(`${className}.${methodName} failed`, { error: error.message });
                    timer({ success: false, error: error.message });
                    throw error;
                }
            };
            
            return descriptor;
        };
    }
}

// Create default logger instance
export const logger = new Logger({
    level: 'info',
    prefix: 'Dashboard',
    enableConsole: true,
    enableStorage: false
});