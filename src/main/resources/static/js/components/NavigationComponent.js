/**
 * Navigation Component - Handles sidebar navigation
 */
import Component from '../core/Component.js';

class NavigationComponent extends Component {
    constructor(element, options = {}) {
        super(element, options);
        this.activeSection = 'overview';
    }

    get defaultOptions() {
        return {
            ...super.defaultOptions,
            sections: [
                { id: 'overview', label: 'Overview', icon: 'fas fa-tachometer-alt' },
                { id: 'filters', label: 'Content Filters', icon: 'fas fa-filter' },
                { id: 'actions', label: 'Actions', icon: 'fas fa-gavel' },
                { id: 'rules', label: 'Custom Rules', icon: 'fas fa-list-ul' },
                { id: 'thresholds', label: 'Thresholds', icon: 'fas fa-chart-line' },
                { id: 'testing', label: 'Testing', icon: 'fas fa-vial' },
                { id: 'analytics', label: 'Analytics', icon: 'fas fa-chart-bar' },
                { id: 'settings', label: 'Settings', icon: 'fas fa-cog' }
            ]
        };
    }

    getWatchPaths() {
        return ['ui.activeSection'];
    }

    async beforeMount() {
        // Set initial active section from state
        if (window.StateManager) {
            this.activeSection = window.StateManager.getState('ui.activeSection') || 'overview';
        }
    }

    bindEvents() {
        this.on('click', '.nav-item', this.handleNavClick);
    }

    handleNavClick(event) {
        event.preventDefault();
        
        const navItem = event.target.closest('.nav-item');
        const sectionId = navItem.getAttribute('data-section');
        
        if (sectionId && sectionId !== this.activeSection) {
            this.navigateToSection(sectionId);
        }
    }

    navigateToSection(sectionId) {
        // Update state
        if (window.StateManager) {
            window.StateManager.setState('ui.activeSection', sectionId);
        }
        
        // Update local state
        this.setState({ activeSection: sectionId });
        
        // Emit navigation event
        this.emit('navigate', { section: sectionId });
        
        // Update URL hash without page reload
        if (history.pushState) {
            history.pushState(null, null, `#${sectionId}`);
        } else {
            location.hash = sectionId;
        }
    }

    onStateChange(path, newValue) {
        if (path === 'ui.activeSection') {
            this.activeSection = newValue;
            this.updateActiveNavItem();
        }
    }

    updateActiveNavItem() {
        // Remove active class from all items
        this.findAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        
        // Add active class to current section
        const activeItem = this.find(`[data-section="${this.activeSection}"]`);
        if (activeItem) {
            activeItem.classList.add('active');
        }
    }

    async template() {
        const guildInfo = window.StateManager ? window.StateManager.getState('currentGuildId') : null;
        
        return `
            <div class="text-center mb-4">
                <h3 class="text-gradient">Axion Dashboard</h3>
                <p class="text-muted">Moderation Control</p>
                ${guildInfo ? `<small class="text-muted">Guild: ${guildInfo}</small>` : ''}
            </div>
            
            <div class="nav flex-column">
                ${this.options.sections.map(section => `
                    <a href="#" class="nav-item ${section.id === this.activeSection ? 'active' : ''}" 
                       data-section="${section.id}">
                        <i class="${section.icon} me-2"></i>${section.label}
                    </a>
                `).join('')}
            </div>
            
            <div class="mt-auto pt-4">
                <div class="nav-footer">
                    <small class="text-muted d-block text-center">
                        Axion Bot v2.0
                    </small>
                </div>
            </div>
        `;
    }

    async afterRender() {
        this.updateActiveNavItem();
        
        // Add smooth scroll animation
        this.findAll('.nav-item').forEach(item => {
            item.addEventListener('mouseenter', () => {
                item.style.transform = 'translateX(5px)';
            });
            
            item.addEventListener('mouseleave', () => {
                if (!item.classList.contains('active')) {
                    item.style.transform = 'translateX(0)';
                }
            });
        });
    }

    // Public API methods
    
    /**
     * Get current active section
     * @returns {string} Active section ID
     */
    getActiveSection() {
        return this.activeSection;
    }

    /**
     * Set active section programmatically
     * @param {string} sectionId - Section ID to activate
     */
    setActiveSection(sectionId) {
        if (this.options.sections.find(s => s.id === sectionId)) {
            this.navigateToSection(sectionId);
        }
    }

    /**
     * Add new navigation section
     * @param {Object} section - Section configuration
     */
    addSection(section) {
        if (!this.options.sections.find(s => s.id === section.id)) {
            this.options.sections.push(section);
            this.render();
        }
    }

    /**
     * Remove navigation section
     * @param {string} sectionId - Section ID to remove
     */
    removeSection(sectionId) {
        const index = this.options.sections.findIndex(s => s.id === sectionId);
        if (index !== -1) {
            this.options.sections.splice(index, 1);
            
            // If removing active section, switch to first available
            if (this.activeSection === sectionId && this.options.sections.length > 0) {
                this.navigateToSection(this.options.sections[0].id);
            }
            
            this.render();
        }
    }

    /**
     * Update section configuration
     * @param {string} sectionId - Section ID
     * @param {Object} updates - Updates to apply
     */
    updateSection(sectionId, updates) {
        const section = this.options.sections.find(s => s.id === sectionId);
        if (section) {
            Object.assign(section, updates);
            this.render();
        }
    }

    /**
     * Enable/disable section
     * @param {string} sectionId - Section ID
     * @param {boolean} enabled - Whether section is enabled
     */
    setSectionEnabled(sectionId, enabled) {
        const navItem = this.find(`[data-section="${sectionId}"]`);
        if (navItem) {
            if (enabled) {
                navItem.classList.remove('disabled');
                navItem.style.pointerEvents = '';
                navItem.style.opacity = '';
            } else {
                navItem.classList.add('disabled');
                navItem.style.pointerEvents = 'none';
                navItem.style.opacity = '0.5';
                
                // If disabling active section, switch to first enabled
                if (this.activeSection === sectionId) {
                    const firstEnabled = this.options.sections.find(s => s.id !== sectionId);
                    if (firstEnabled) {
                        this.navigateToSection(firstEnabled.id);
                    }
                }
            }
        }
    }

    /**
     * Highlight section (e.g., for notifications)
     * @param {string} sectionId - Section ID
     * @param {string} type - Highlight type ('info', 'warning', 'error')
     */
    highlightSection(sectionId, type = 'info') {
        const navItem = this.find(`[data-section="${sectionId}"]`);
        if (navItem) {
            navItem.classList.add(`highlight-${type}`);
            
            // Auto-remove highlight after 3 seconds
            setTimeout(() => {
                navItem.classList.remove(`highlight-${type}`);
            }, 3000);
        }
    }
}

export default NavigationComponent;