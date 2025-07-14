/**
 * Event Bus - Central event management system
 * Implements Observer pattern for loose coupling between components
 */
class EventBus {
    constructor() {
        this.events = new Map();
        this.onceEvents = new Map();
    }

    /**
     * Subscribe to an event
     * @param {string} event - Event name
     * @param {Function} callback - Callback function
     * @param {Object} context - Context for callback execution
     */
    on(event, callback, context = null) {
        if (!this.events.has(event)) {
            this.events.set(event, []);
        }
        
        this.events.get(event).push({ callback, context });
    }

    /**
     * Subscribe to an event once
     * @param {string} event - Event name
     * @param {Function} callback - Callback function
     * @param {Object} context - Context for callback execution
     */
    once(event, callback, context = null) {
        if (!this.onceEvents.has(event)) {
            this.onceEvents.set(event, []);
        }
        
        this.onceEvents.get(event).push({ callback, context });
    }

    /**
     * Unsubscribe from an event
     * @param {string} event - Event name
     * @param {Function} callback - Callback function to remove
     */
    off(event, callback) {
        if (this.events.has(event)) {
            const listeners = this.events.get(event);
            const index = listeners.findIndex(listener => listener.callback === callback);
            if (index !== -1) {
                listeners.splice(index, 1);
            }
        }
    }

    /**
     * Emit an event
     * @param {string} event - Event name
     * @param {*} data - Data to pass to listeners
     */
    emit(event, data = null) {
        // Handle regular events
        if (this.events.has(event)) {
            const listeners = this.events.get(event);
            listeners.forEach(({ callback, context }) => {
                try {
                    if (context) {
                        callback.call(context, data);
                    } else {
                        callback(data);
                    }
                } catch (error) {
                    console.error(`Error in event listener for '${event}':`, error);
                }
            });
        }

        // Handle once events
        if (this.onceEvents.has(event)) {
            const listeners = this.onceEvents.get(event);
            listeners.forEach(({ callback, context }) => {
                try {
                    if (context) {
                        callback.call(context, data);
                    } else {
                        callback(data);
                    }
                } catch (error) {
                    console.error(`Error in once event listener for '${event}':`, error);
                }
            });
            // Clear once events after execution
            this.onceEvents.delete(event);
        }
    }

    /**
     * Remove all listeners for an event
     * @param {string} event - Event name
     */
    removeAllListeners(event) {
        this.events.delete(event);
        this.onceEvents.delete(event);
    }

    /**
     * Clear all events
     */
    clear() {
        this.events.clear();
        this.onceEvents.clear();
    }

    /**
     * Get all event names
     * @returns {Array} Array of event names
     */
    getEventNames() {
        return [...new Set([...this.events.keys(), ...this.onceEvents.keys()])];
    }

    /**
     * Get listener count for an event
     * @param {string} event - Event name
     * @returns {number} Number of listeners
     */
    getListenerCount(event) {
        const regularCount = this.events.has(event) ? this.events.get(event).length : 0;
        const onceCount = this.onceEvents.has(event) ? this.onceEvents.get(event).length : 0;
        return regularCount + onceCount;
    }
}

// Create global event bus instance
window.EventBus = new EventBus();

export default EventBus;