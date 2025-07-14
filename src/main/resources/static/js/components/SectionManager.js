/**
 * Section Manager Component - Handles section display and transitions
 */
import Component from '../core/Component.js';

class SectionManager extends Component {
    constructor(element, options = {}) {
        super(element, options);
        this.sections = new Map();
        this.currentSection = null;
        this.transitionInProgress = false;
    }

    get defaultOptions() {
        return {
            ...super.defaultOptions,
            animationDuration: 300,
            lazyLoad: true,
            preloadNext: true,
            cacheRendered: true,
            transitionType: 'fade' // 'fade', 'slide', 'none'
        };
    }

    getWatchPaths() {
        return ['ui.activeSection'];
    }

    async init() {
        await super.init();
        
        // Listen for navigation events
        if (window.EventBus) {
            window.EventBus.on('navigate', this.handleNavigation.bind(this));
        }
        
        // Initialize with current section from state
        if (window.StateManager) {
            const activeSection = window.StateManager.getState('ui.activeSection');
            if (activeSection) {
                await this.showSection(activeSection);
            }
        }
    }

    /**
     * Register a section with the manager
     * @param {string} id - Section ID
     * @param {Object} config - Section configuration
     */
    registerSection(id, config) {
        this.sections.set(id, {
            id,
            component: null,
            element: null,
            loaded: false,
            cached: false,
            ...config
        });
    }

    /**
     * Unregister a section
     * @param {string} id - Section ID
     */
    unregisterSection(id) {
        const section = this.sections.get(id);
        if (section) {
            if (section.component && typeof section.component.destroy === 'function') {
                section.component.destroy();
            }
            if (section.element) {
                section.element.remove();
            }
            this.sections.delete(id);
        }
    }

    /**
     * Handle navigation events
     * @param {Object} data - Navigation data
     */
    async handleNavigation(data) {
        if (data.section && data.section !== this.currentSection) {
            await this.showSection(data.section);
        }
    }

    /**
     * Show a specific section
     * @param {string} sectionId - Section ID to show
     */
    async showSection(sectionId) {
        if (this.transitionInProgress) {
            return;
        }

        const section = this.sections.get(sectionId);
        if (!section) {
            console.warn(`Section '${sectionId}' not registered`);
            return;
        }

        this.transitionInProgress = true;

        try {
            // Hide current section
            if (this.currentSection) {
                await this.hideSection(this.currentSection);
            }

            // Load section if needed
            if (!section.loaded || (!section.cached && !this.options.cacheRendered)) {
                await this.loadSection(section);
            }

            // Show new section
            await this.displaySection(section);
            
            this.currentSection = sectionId;
            
            // Update state
            if (window.StateManager) {
                window.StateManager.setState('ui.activeSection', sectionId);
            }

            // Preload next section if enabled
            if (this.options.preloadNext) {
                this.preloadNextSection(sectionId);
            }

            // Emit section change event
            this.emit('sectionChanged', { section: sectionId });
            
        } catch (error) {
            console.error(`Error showing section '${sectionId}':`, error);
            this.emit('sectionError', { section: sectionId, error });
        } finally {
            this.transitionInProgress = false;
        }
    }

    /**
     * Load a section
     * @param {Object} section - Section configuration
     */
    async loadSection(section) {
        if (section.loaded && section.cached) {
            return;
        }

        // Show loading indicator
        this.showSectionLoading(section.id);

        try {
            // Create section element if it doesn't exist
            if (!section.element) {
                section.element = document.createElement('div');
                section.element.className = `section section-${section.id}`;
                section.element.style.display = 'none';
                this.element.appendChild(section.element);
            }

            // Load section content
            if (section.template) {
                section.element.innerHTML = await this.renderTemplate(section.template, section.data || {});
            } else if (section.component) {
                // Initialize component if provided
                if (typeof section.component === 'function') {
                    section.componentInstance = new section.component(section.element, section.options || {});
                    await section.componentInstance.init();
                }
            } else if (section.url) {
                // Load content from URL
                const response = await fetch(section.url);
                section.element.innerHTML = await response.text();
            }

            // Run section-specific initialization
            if (section.onLoad) {
                await section.onLoad(section.element);
            }

            section.loaded = true;
            section.cached = this.options.cacheRendered;

        } catch (error) {
            console.error(`Error loading section '${section.id}':`, error);
            section.element.innerHTML = `
                <div class="alert alert-danger">
                    <h5>Error Loading Section</h5>
                    <p>Failed to load ${section.id} section. Please try again.</p>
                    <button class="btn btn-outline-danger btn-sm" onclick="location.reload()">
                        Reload Page
                    </button>
                </div>
            `;
        } finally {
            this.hideSectionLoading(section.id);
        }
    }

    /**
     * Display a section with animation
     * @param {Object} section - Section to display
     */
    async displaySection(section) {
        if (!section.element) {
            return;
        }

        // Apply transition based on type
        switch (this.options.transitionType) {
            case 'fade':
                await this.fadeInSection(section.element);
                break;
            case 'slide':
                await this.slideInSection(section.element);
                break;
            default:
                section.element.style.display = 'block';
                section.element.style.opacity = '1';
        }

        // Run section-specific show logic
        if (section.onShow) {
            await section.onShow(section.element);
        }

        // Trigger component lifecycle if applicable
        if (section.componentInstance && typeof section.componentInstance.onShow === 'function') {
            await section.componentInstance.onShow();
        }
    }

    /**
     * Hide a section with animation
     * @param {string} sectionId - Section ID to hide
     */
    async hideSection(sectionId) {
        const section = this.sections.get(sectionId);
        if (!section || !section.element) {
            return;
        }

        // Run section-specific hide logic
        if (section.onHide) {
            await section.onHide(section.element);
        }

        // Trigger component lifecycle if applicable
        if (section.componentInstance && typeof section.componentInstance.onHide === 'function') {
            await section.componentInstance.onHide();
        }

        // Apply transition based on type
        switch (this.options.transitionType) {
            case 'fade':
                await this.fadeOutSection(section.element);
                break;
            case 'slide':
                await this.slideOutSection(section.element);
                break;
            default:
                section.element.style.display = 'none';
        }
    }

    /**
     * Fade in animation
     * @param {HTMLElement} element - Element to animate
     */
    async fadeInSection(element) {
        return new Promise(resolve => {
            element.style.display = 'block';
            element.style.opacity = '0';
            element.style.transition = `opacity ${this.options.animationDuration}ms ease-in-out`;
            
            requestAnimationFrame(() => {
                element.style.opacity = '1';
                setTimeout(resolve, this.options.animationDuration);
            });
        });
    }

    /**
     * Fade out animation
     * @param {HTMLElement} element - Element to animate
     */
    async fadeOutSection(element) {
        return new Promise(resolve => {
            element.style.transition = `opacity ${this.options.animationDuration}ms ease-in-out`;
            element.style.opacity = '0';
            
            setTimeout(() => {
                element.style.display = 'none';
                resolve();
            }, this.options.animationDuration);
        });
    }

    /**
     * Slide in animation
     * @param {HTMLElement} element - Element to animate
     */
    async slideInSection(element) {
        return new Promise(resolve => {
            element.style.display = 'block';
            element.style.transform = 'translateX(100%)';
            element.style.transition = `transform ${this.options.animationDuration}ms ease-in-out`;
            
            requestAnimationFrame(() => {
                element.style.transform = 'translateX(0)';
                setTimeout(resolve, this.options.animationDuration);
            });
        });
    }

    /**
     * Slide out animation
     * @param {HTMLElement} element - Element to animate
     */
    async slideOutSection(element) {
        return new Promise(resolve => {
            element.style.transition = `transform ${this.options.animationDuration}ms ease-in-out`;
            element.style.transform = 'translateX(-100%)';
            
            setTimeout(() => {
                element.style.display = 'none';
                element.style.transform = 'translateX(0)';
                resolve();
            }, this.options.animationDuration);
        });
    }

    /**
     * Show loading indicator for section
     * @param {string} sectionId - Section ID
     */
    showSectionLoading(sectionId) {
        const loadingElement = document.createElement('div');
        loadingElement.className = 'section-loading';
        loadingElement.innerHTML = `
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading ${sectionId}...</span>
                </div>
                <span class="ms-3">Loading ${sectionId}...</span>
            </div>
        `;
        
        this.element.appendChild(loadingElement);
    }

    /**
     * Hide loading indicator for section
     * @param {string} sectionId - Section ID
     */
    hideSectionLoading(sectionId) {
        const loadingElement = this.element.querySelector('.section-loading');
        if (loadingElement) {
            loadingElement.remove();
        }
    }

    /**
     * Preload next section for better UX
     * @param {string} currentSectionId - Current section ID
     */
    async preloadNextSection(currentSectionId) {
        // Simple logic: preload the next section in registration order
        const sectionIds = Array.from(this.sections.keys());
        const currentIndex = sectionIds.indexOf(currentSectionId);
        const nextIndex = (currentIndex + 1) % sectionIds.length;
        const nextSectionId = sectionIds[nextIndex];
        
        const nextSection = this.sections.get(nextSectionId);
        if (nextSection && !nextSection.loaded) {
            try {
                await this.loadSection(nextSection);
            } catch (error) {
                console.warn(`Failed to preload section '${nextSectionId}':`, error);
            }
        }
    }

    /**
     * Render template with data
     * @param {string|Function} template - Template string or function
     * @param {Object} data - Data to render
     */
    async renderTemplate(template, data) {
        if (typeof template === 'function') {
            return await template(data);
        }
        
        // Simple template rendering (replace {{key}} with data[key])
        return template.replace(/\{\{(\w+)\}\}/g, (match, key) => {
            return data[key] || '';
        });
    }

    /**
     * Get current section
     * @returns {string|null} Current section ID
     */
    getCurrentSection() {
        return this.currentSection;
    }

    /**
     * Get section configuration
     * @param {string} sectionId - Section ID
     * @returns {Object|null} Section configuration
     */
    getSection(sectionId) {
        return this.sections.get(sectionId) || null;
    }

    /**
     * Get all registered sections
     * @returns {Array} Array of section configurations
     */
    getAllSections() {
        return Array.from(this.sections.values());
    }

    /**
     * Refresh current section
     */
    async refreshCurrentSection() {
        if (this.currentSection) {
            const section = this.sections.get(this.currentSection);
            if (section) {
                section.loaded = false;
                section.cached = false;
                await this.showSection(this.currentSection);
            }
        }
    }

    /**
     * Clear section cache
     * @param {string} sectionId - Section ID (optional, clears all if not provided)
     */
    clearCache(sectionId = null) {
        if (sectionId) {
            const section = this.sections.get(sectionId);
            if (section) {
                section.loaded = false;
                section.cached = false;
            }
        } else {
            this.sections.forEach(section => {
                section.loaded = false;
                section.cached = false;
            });
        }
    }

    onStateChange(path, newValue) {
        if (path === 'ui.activeSection' && newValue !== this.currentSection) {
            this.showSection(newValue);
        }
    }

    async beforeDestroy() {
        // Clean up all sections
        this.sections.forEach((section, id) => {
            this.unregisterSection(id);
        });
        
        // Remove event listeners
        if (window.EventBus) {
            window.EventBus.off('navigate', this.handleNavigation);
        }
    }

    /**
     * Destroy the section manager and clean up all resources
     */
    destroy() {
        // Call beforeDestroy for compatibility
        this.beforeDestroy();
        
        // Clear all sections
        this.sections.clear();
        
        // Reset state
        this.currentSection = null;
        this.element = null;
        this.options = null;
        this.stateManager = null;
    }
}

export default SectionManager;