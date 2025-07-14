/**
 * Chart Service - Handles chart creation and management
 */
class ChartService {
    constructor(options = {}) {
        this.options = {
            defaultTheme: 'light',
            animationDuration: 300, // Reduced from 750ms to 300ms for better performance
            responsive: true,
            maintainAspectRatio: false,
            ...options
        };
        
        this.charts = new Map();
        this.themes = new Map();
        this.observers = new Map();
        
        this.initializeThemes();
        this.setupResizeObserver();
    }

    /**
     * Initialize chart themes
     */
    initializeThemes() {
        // Light theme
        this.themes.set('light', {
            backgroundColor: 'rgba(255, 255, 255, 0.8)',
            borderColor: '#dee2e6',
            textColor: '#495057',
            gridColor: 'rgba(0, 0, 0, 0.1)',
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
            gradients: {
                primary: ['rgba(0, 123, 255, 0.8)', 'rgba(0, 123, 255, 0.1)'],
                success: ['rgba(40, 167, 69, 0.8)', 'rgba(40, 167, 69, 0.1)'],
                warning: ['rgba(255, 193, 7, 0.8)', 'rgba(255, 193, 7, 0.1)'],
                danger: ['rgba(220, 53, 69, 0.8)', 'rgba(220, 53, 69, 0.1)']
            }
        });

        // Dark theme
        this.themes.set('dark', {
            backgroundColor: 'rgba(33, 37, 41, 0.8)',
            borderColor: '#495057',
            textColor: '#f8f9fa',
            gridColor: 'rgba(255, 255, 255, 0.1)',
            colors: {
                primary: '#0d6efd',
                secondary: '#6c757d',
                success: '#198754',
                danger: '#dc3545',
                warning: '#ffc107',
                info: '#0dcaf0',
                light: '#f8f9fa',
                dark: '#212529'
            },
            gradients: {
                primary: ['rgba(13, 110, 253, 0.8)', 'rgba(13, 110, 253, 0.1)'],
                success: ['rgba(25, 135, 84, 0.8)', 'rgba(25, 135, 84, 0.1)'],
                warning: ['rgba(255, 193, 7, 0.8)', 'rgba(255, 193, 7, 0.1)'],
                danger: ['rgba(220, 53, 69, 0.8)', 'rgba(220, 53, 69, 0.1)']
            }
        });
    }

    /**
     * Setup resize observer for responsive charts
     */
    setupResizeObserver() {
        if (typeof ResizeObserver !== 'undefined') {
            this.resizeObserver = new ResizeObserver(entries => {
                entries.forEach(entry => {
                    const chartId = entry.target.getAttribute('data-chart-id');
                    if (chartId && this.charts.has(chartId)) {
                        this.throttledResize(chartId);
                    }
                });
            });
        }
    }

    /**
     * Throttled resize function
     */
    throttledResize = this.throttle((chartId) => {
        const chart = this.charts.get(chartId);
        if (chart && chart.instance) {
            chart.instance.resize();
        }
    }, 250); // Increased throttle delay from 100ms to 250ms

    /**
     * Create a new chart
     * @param {string} chartId - Unique chart identifier
     * @param {HTMLElement|string} element - Canvas element or selector
     * @param {Object} config - Chart configuration
     */
    async createChart(chartId, element, config) {
        // Ensure Chart.js is loaded
        if (typeof Chart === 'undefined') {
            throw new Error('Chart.js is not loaded');
        }

        // Get canvas element
        const canvas = typeof element === 'string' ? 
            document.querySelector(element) : element;
            
        if (!canvas) {
            throw new Error(`Canvas element not found: ${element}`);
        }

        // Destroy existing chart if it exists
        if (this.charts.has(chartId)) {
            this.destroyChart(chartId);
        }

        // Apply theme and default options
        const finalConfig = this.applyThemeAndDefaults(config);

        try {
            // Create chart instance
            const chartInstance = new Chart(canvas, finalConfig);
            
            // Store chart reference
            this.charts.set(chartId, {
                instance: chartInstance,
                element: canvas,
                config: finalConfig,
                lastUpdate: Date.now()
            });

            // Setup resize observation
            if (this.resizeObserver) {
                canvas.setAttribute('data-chart-id', chartId);
                this.resizeObserver.observe(canvas);
            }

            // Emit chart created event
            this.emit('chartCreated', { chartId, chart: chartInstance });

            return chartInstance;
            
        } catch (error) {
            console.error(`Error creating chart '${chartId}':`, error);
            throw error;
        }
    }

    /**
     * Apply theme and default options to chart config
     * @param {Object} config - Chart configuration
     */
    applyThemeAndDefaults(config) {
        const theme = this.getCurrentTheme();
        
        const defaultConfig = {
            options: {
                responsive: this.options.responsive,
                maintainAspectRatio: this.options.maintainAspectRatio,
                animation: {
                    duration: this.options.animationDuration
                },
                plugins: {
                    legend: {
                        labels: {
                            color: theme.textColor,
                            usePointStyle: true,
                            padding: 20
                        }
                    },
                    tooltip: {
                        backgroundColor: theme.backgroundColor,
                        titleColor: theme.textColor,
                        bodyColor: theme.textColor,
                        borderColor: theme.borderColor,
                        borderWidth: 1,
                        cornerRadius: 8,
                        displayColors: true
                    }
                },
                scales: {
                    x: {
                        grid: {
                            color: theme.gridColor,
                            borderColor: theme.borderColor
                        },
                        ticks: {
                            color: theme.textColor
                        }
                    },
                    y: {
                        grid: {
                            color: theme.gridColor,
                            borderColor: theme.borderColor
                        },
                        ticks: {
                            color: theme.textColor
                        }
                    }
                }
            }
        };

        // Deep merge configurations
        return this.deepMerge(defaultConfig, config);
    }

    /**
     * Get current theme
     */
    getCurrentTheme() {
        const currentTheme = this.options.currentTheme || this.options.defaultTheme;
        return this.themes.get(currentTheme) || this.themes.get(this.options.defaultTheme);
    }

    /**
     * Update chart data
     * @param {string} chartId - Chart identifier
     * @param {Object} newData - New chart data
     * @param {boolean} animate - Whether to animate the update
     */
    updateChart(chartId, newData, animate = true) {
        const chart = this.charts.get(chartId);
        if (!chart || !chart.instance) {
            console.warn(`Chart '${chartId}' not found`);
            return;
        }

        try {
            // Throttle updates to prevent excessive redraws
            const now = Date.now();
            if (chart.lastUpdate && (now - chart.lastUpdate) < 100) {
                return; // Skip update if less than 100ms since last update
            }

            // Update data
            if (newData.labels) {
                chart.instance.data.labels = newData.labels;
            }
            
            if (newData.datasets) {
                chart.instance.data.datasets = newData.datasets;
            }

            // Update chart with reduced animation for better performance
            chart.instance.update(animate ? 'resize' : 'none');
            chart.lastUpdate = now;

            // Emit update event
            this.emit('chartUpdated', { chartId, chart: chart.instance });
            
        } catch (error) {
            console.error(`Error updating chart '${chartId}':`, error);
        }
    }

    /**
     * Destroy a chart
     * @param {string} chartId - Chart identifier
     */
    destroyChart(chartId) {
        const chart = this.charts.get(chartId);
        if (!chart) {
            return;
        }

        try {
            // Stop observing resize
            if (this.resizeObserver && chart.element) {
                this.resizeObserver.unobserve(chart.element);
                chart.element.removeAttribute('data-chart-id');
            }

            // Destroy chart instance
            if (chart.instance) {
                chart.instance.destroy();
            }

            // Remove from registry
            this.charts.delete(chartId);

            // Emit destroy event
            this.emit('chartDestroyed', { chartId });
            
        } catch (error) {
            console.error(`Error destroying chart '${chartId}':`, error);
        }
    }

    /**
     * Get chart instance
     * @param {string} chartId - Chart identifier
     */
    getChart(chartId) {
        const chart = this.charts.get(chartId);
        return chart ? chart.instance : null;
    }

    /**
     * Check if chart exists
     * @param {string} chartId - Chart identifier
     */
    hasChart(chartId) {
        return this.charts.has(chartId);
    }

    /**
     * Get all chart IDs
     */
    getChartIds() {
        return Array.from(this.charts.keys());
    }

    /**
     * Set theme for all charts
     * @param {string} themeName - Theme name
     */
    setTheme(themeName) {
        if (!this.themes.has(themeName)) {
            console.warn(`Theme '${themeName}' not found`);
            return;
        }

        this.options.currentTheme = themeName;
        
        // Update all existing charts
        this.charts.forEach((chart, chartId) => {
            const newConfig = this.applyThemeAndDefaults(chart.config);
            
            // Update chart options
            Object.assign(chart.instance.options, newConfig.options);
            chart.instance.update('none');
        });

        // Emit theme change event
        this.emit('themeChanged', { theme: themeName });
    }

    /**
     * Add custom theme
     * @param {string} name - Theme name
     * @param {Object} theme - Theme configuration
     */
    addTheme(name, theme) {
        this.themes.set(name, theme);
    }

    /**
     * Create gradient
     * @param {HTMLCanvasElement} canvas - Canvas element
     * @param {Array} colors - Color stops
     * @param {string} direction - Gradient direction ('vertical' or 'horizontal')
     */
    createGradient(canvas, colors, direction = 'vertical') {
        const ctx = canvas.getContext('2d');
        const gradient = direction === 'vertical' ?
            ctx.createLinearGradient(0, 0, 0, canvas.height) :
            ctx.createLinearGradient(0, 0, canvas.width, 0);

        colors.forEach((color, index) => {
            gradient.addColorStop(index / (colors.length - 1), color);
        });

        return gradient;
    }

    /**
     * Generate chart colors based on theme
     * @param {number} count - Number of colors needed
     * @param {string} type - Color type ('solid', 'gradient')
     */
    generateColors(count, type = 'solid') {
        const theme = this.getCurrentTheme();
        const baseColors = Object.values(theme.colors);
        const colors = [];

        for (let i = 0; i < count; i++) {
            const colorIndex = i % baseColors.length;
            const baseColor = baseColors[colorIndex];
            
            if (type === 'gradient') {
                // Return gradient configuration
                colors.push({
                    backgroundColor: this.adjustColorOpacity(baseColor, 0.2),
                    borderColor: baseColor,
                    pointBackgroundColor: baseColor,
                    pointBorderColor: '#fff'
                });
            } else {
                colors.push(baseColor);
            }
        }

        return colors;
    }

    /**
     * Adjust color opacity
     * @param {string} color - Color value
     * @param {number} opacity - Opacity (0-1)
     */
    adjustColorOpacity(color, opacity) {
        // Simple implementation for hex colors
        if (color.startsWith('#')) {
            const r = parseInt(color.slice(1, 3), 16);
            const g = parseInt(color.slice(3, 5), 16);
            const b = parseInt(color.slice(5, 7), 16);
            return `rgba(${r}, ${g}, ${b}, ${opacity})`;
        }
        return color;
    }

    /**
     * Export chart as image
     * @param {string} chartId - Chart identifier
     * @param {string} format - Image format ('png', 'jpeg')
     */
    exportChart(chartId, format = 'png') {
        const chart = this.charts.get(chartId);
        if (!chart || !chart.instance) {
            throw new Error(`Chart '${chartId}' not found`);
        }

        return chart.instance.toBase64Image(`image/${format}`, 1.0);
    }

    /**
     * Download chart as image
     * @param {string} chartId - Chart identifier
     * @param {string} filename - Download filename
     * @param {string} format - Image format
     */
    downloadChart(chartId, filename, format = 'png') {
        const dataUrl = this.exportChart(chartId, format);
        
        const link = document.createElement('a');
        link.download = `${filename}.${format}`;
        link.href = dataUrl;
        link.click();
    }

    /**
     * Resize all charts
     */
    resizeAllCharts() {
        this.charts.forEach((chart, chartId) => {
            if (chart.instance) {
                chart.instance.resize();
            }
        });
    }

    /**
     * Destroy all charts
     */
    destroyAllCharts() {
        const chartIds = Array.from(this.charts.keys());
        chartIds.forEach(chartId => this.destroyChart(chartId));
    }

    /**
     * Get chart statistics
     */
    getStats() {
        return {
            totalCharts: this.charts.size,
            themes: this.themes.size,
            lastUpdate: Math.max(...Array.from(this.charts.values()).map(c => c.lastUpdate))
        };
    }

    // Utility Methods

    /**
     * Deep merge objects
     * @param {Object} target - Target object
     * @param {Object} source - Source object
     */
    deepMerge(target, source) {
        const result = { ...target };
        
        for (const key in source) {
            if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
                result[key] = this.deepMerge(result[key] || {}, source[key]);
            } else {
                result[key] = source[key];
            }
        }
        
        return result;
    }

    /**
     * Throttle function
     * @param {Function} func - Function to throttle
     * @param {number} delay - Delay in milliseconds
     */
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
     * Simple event emitter
     * @param {string} event - Event name
     * @param {*} data - Event data
     */
    emit(event, data) {
        if (window.EventBus) {
            window.EventBus.emit(`chart:${event}`, data);
        }
    }

    /**
     * Cleanup resources
     */
    destroy() {
        this.destroyAllCharts();
        
        if (this.resizeObserver) {
            this.resizeObserver.disconnect();
        }
        
        this.charts.clear();
        this.themes.clear();
    }
}

export default ChartService;