/**
 * Base Component Class - Foundation for all UI components
 * Implements component lifecycle and reactive updates
 */
class Component {
    constructor(element, options = {}) {
        this.element = typeof element === 'string' ? document.querySelector(element) : element;
        this.options = { ...this.defaultOptions, ...options };
        this.state = {};
        this.subscriptions = [];
        this.children = new Map();
        this.isDestroyed = false;
        
        if (!this.element) {
            throw new Error(`Component element not found: ${element}`);
        }
        
        this.init();
    }

    /**
     * Default options for the component
     */
    get defaultOptions() {
        return {
            autoRender: true,
            bindEvents: true,
            reactive: true
        };
    }

    /**
     * Initialize the component
     */
    async init() {
        try {
            await this.beforeMount();
            
            if (this.options.bindEvents) {
                this.bindEvents();
            }
            
            if (this.options.reactive) {
                this.setupReactivity();
            }
            
            if (this.options.autoRender) {
                await this.render();
            }
            
            await this.mounted();
            
            // Mark component as initialized
            this.element.setAttribute('data-component-initialized', 'true');
            
        } catch (error) {
            console.error(`Error initializing component:`, error);
            this.handleError(error);
        }
    }

    /**
     * Lifecycle hook - called before component is mounted
     */
    async beforeMount() {
        // Override in subclasses
    }

    /**
     * Lifecycle hook - called after component is mounted
     */
    async mounted() {
        // Override in subclasses
    }

    /**
     * Lifecycle hook - called before component is destroyed
     */
    async beforeDestroy() {
        // Override in subclasses
    }

    /**
     * Lifecycle hook - called after component is destroyed
     */
    async destroyed() {
        // Override in subclasses
    }

    /**
     * Render the component
     */
    async render() {
        const template = await this.template();
        if (template) {
            this.element.innerHTML = template;
            await this.afterRender();
        }
    }

    /**
     * Get component template
     * @returns {string|Promise<string>} HTML template
     */
    async template() {
        // Override in subclasses
        return '';
    }

    /**
     * Called after render
     */
    async afterRender() {
        // Override in subclasses
    }

    /**
     * Bind event listeners
     */
    bindEvents() {
        // Override in subclasses
    }

    /**
     * Setup reactivity with state manager
     */
    setupReactivity() {
        if (!window.StateManager) return;
        
        const watchPaths = this.getWatchPaths();
        watchPaths.forEach(path => {
            const unsubscribe = window.StateManager.subscribe(
                path,
                (newValue, oldValue) => this.onStateChange(path, newValue, oldValue),
                { deep: true }
            );
            this.subscriptions.push(unsubscribe);
        });
    }

    /**
     * Get state paths to watch
     * @returns {Array<string>} Array of state paths
     */
    getWatchPaths() {
        // Override in subclasses
        return [];
    }

    /**
     * Handle state changes
     * @param {string} path - State path that changed
     * @param {*} newValue - New value
     * @param {*} oldValue - Old value
     */
    async onStateChange(path, newValue, oldValue) {
        if (this.isDestroyed) return;
        
        try {
            await this.render();
        } catch (error) {
            console.error(`Error handling state change in component:`, error);
            this.handleError(error);
        }
    }

    /**
     * Update component state
     * @param {Object} newState - New state object
     */
    setState(newState) {
        const oldState = { ...this.state };
        this.state = { ...this.state, ...newState };
        this.onLocalStateChange(this.state, oldState);
    }

    /**
     * Handle local state changes
     * @param {Object} newState - New state
     * @param {Object} oldState - Old state
     */
    async onLocalStateChange(newState, oldState) {
        if (this.options.autoRender) {
            await this.render();
        }
    }

    /**
     * Add child component
     * @param {string} name - Child component name
     * @param {Component} component - Child component instance
     */
    addChild(name, component) {
        this.children.set(name, component);
    }

    /**
     * Get child component
     * @param {string} name - Child component name
     * @returns {Component|null} Child component
     */
    getChild(name) {
        return this.children.get(name) || null;
    }

    /**
     * Remove child component
     * @param {string} name - Child component name
     */
    removeChild(name) {
        const child = this.children.get(name);
        if (child) {
            child.destroy();
            this.children.delete(name);
        }
    }

    /**
     * Find element within component
     * @param {string} selector - CSS selector
     * @returns {Element|null} Found element
     */
    find(selector) {
        return this.element.querySelector(selector);
    }

    /**
     * Find all elements within component
     * @param {string} selector - CSS selector
     * @returns {NodeList} Found elements
     */
    findAll(selector) {
        return this.element.querySelectorAll(selector);
    }

    /**
     * Add event listener to component element
     * @param {string} event - Event name
     * @param {string} selector - CSS selector (optional)
     * @param {Function} handler - Event handler
     */
    on(event, selector, handler) {
        if (typeof selector === 'function') {
            handler = selector;
            selector = null;
        }
        
        if (selector) {
            this.element.addEventListener(event, (e) => {
                if (e.target.matches(selector) || e.target.closest(selector)) {
                    handler.call(this, e);
                }
            });
        } else {
            this.element.addEventListener(event, handler.bind(this));
        }
    }

    /**
     * Emit custom event
     * @param {string} eventName - Event name
     * @param {*} detail - Event detail
     */
    emit(eventName, detail = null) {
        const event = new CustomEvent(eventName, {
            detail,
            bubbles: true,
            cancelable: true
        });
        this.element.dispatchEvent(event);
        
        // Also emit through global event bus if available
        if (window.EventBus) {
            window.EventBus.emit(`component:${eventName}`, {
                component: this,
                detail
            });
        }
    }

    /**
     * Show component
     */
    show() {
        this.element.style.display = '';
        this.element.classList.remove('d-none', 'hidden');
        this.emit('show');
    }

    /**
     * Hide component
     */
    hide() {
        this.element.style.display = 'none';
        this.emit('hide');
    }

    /**
     * Toggle component visibility
     */
    toggle() {
        if (this.element.style.display === 'none') {
            this.show();
        } else {
            this.hide();
        }
    }

    /**
     * Add CSS class
     * @param {string} className - CSS class name
     */
    addClass(className) {
        this.element.classList.add(className);
    }

    /**
     * Remove CSS class
     * @param {string} className - CSS class name
     */
    removeClass(className) {
        this.element.classList.remove(className);
    }

    /**
     * Toggle CSS class
     * @param {string} className - CSS class name
     */
    toggleClass(className) {
        this.element.classList.toggle(className);
    }

    /**
     * Check if component has CSS class
     * @param {string} className - CSS class name
     * @returns {boolean} True if has class
     */
    hasClass(className) {
        return this.element.classList.contains(className);
    }

    /**
     * Handle component errors
     * @param {Error} error - Error object
     */
    handleError(error) {
        console.error(`Component error:`, error);
        this.emit('error', error);
        
        if (window.EventBus) {
            window.EventBus.emit('component:error', {
                component: this,
                error
            });
        }
    }

    /**
     * Destroy component and cleanup
     */
    async destroy() {
        if (this.isDestroyed) return;
        
        try {
            await this.beforeDestroy();
            
            // Destroy all children
            this.children.forEach(child => child.destroy());
            this.children.clear();
            
            // Unsubscribe from state changes
            this.subscriptions.forEach(unsubscribe => unsubscribe());
            this.subscriptions = [];
            
            // Remove element
            if (this.element && this.element.parentNode) {
                this.element.parentNode.removeChild(this.element);
            }
            
            this.isDestroyed = true;
            
            await this.destroyed();
            
            this.emit('destroyed');
            
        } catch (error) {
            console.error(`Error destroying component:`, error);
        }
    }

    /**
     * Get component info for debugging
     * @returns {Object} Component info
     */
    getDebugInfo() {
        return {
            element: this.element,
            options: this.options,
            state: this.state,
            children: Array.from(this.children.keys()),
            subscriptions: this.subscriptions.length,
            isDestroyed: this.isDestroyed
        };
    }
}

export default Component;