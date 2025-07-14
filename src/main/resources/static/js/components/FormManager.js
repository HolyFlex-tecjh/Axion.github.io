/**
 * Form Manager Component - Handles form validation, auto-save, and state management
 */
import Component from '../core/Component.js';

class FormManager extends Component {
    constructor(element, options = {}) {
        super(element, options);
        
        this.forms = new Map();
        this.validators = new Map();
        this.autoSaveTimers = new Map();
        this.formData = new Map();
        this.isDirty = false;
    }

    get defaultOptions() {
        return {
            ...super.defaultOptions,
            autoSave: true,
            autoSaveDelay: 2000,
            validateOnChange: true,
            validateOnBlur: true,
            showValidationErrors: true,
            persistData: true,
            storageKey: 'moderation-dashboard-forms'
        };
    }

    getWatchPaths() {
        return ['currentConfig', 'ui.loading'];
    }

    async init() {
        await super.init();
        
        // Load persisted form data
        if (this.options.persistData) {
            this.loadPersistedData();
        }
        
        // Setup default validators
        this.setupDefaultValidators();
        
        // Register forms
        this.registerForms();
        
        // Setup auto-save
        if (this.options.autoSave) {
            this.setupAutoSave();
        }
    }

    /**
     * Setup default validators
     */
    setupDefaultValidators() {
        // Required field validator
        this.addValidator('required', (value, element) => {
            const isEmpty = !value || (typeof value === 'string' && value.trim() === '');
            return {
                valid: !isEmpty,
                message: 'This field is required'
            };
        });

        // Number range validator
        this.addValidator('range', (value, element) => {
            const min = parseFloat(element.getAttribute('min'));
            const max = parseFloat(element.getAttribute('max'));
            const numValue = parseFloat(value);
            
            if (isNaN(numValue)) {
                return { valid: false, message: 'Must be a valid number' };
            }
            
            if (!isNaN(min) && numValue < min) {
                return { valid: false, message: `Must be at least ${min}` };
            }
            
            if (!isNaN(max) && numValue > max) {
                return { valid: false, message: `Must be at most ${max}` };
            }
            
            return { valid: true };
        });

        // Pattern validator
        this.addValidator('pattern', (value, element) => {
            const pattern = element.getAttribute('pattern');
            if (!pattern) return { valid: true };
            
            const regex = new RegExp(pattern);
            return {
                valid: regex.test(value),
                message: 'Invalid format'
            };
        });

        // Custom rule pattern validator
        this.addValidator('rulePattern', (value, element) => {
            if (!value) return { valid: true };
            
            try {
                new RegExp(value);
                return { valid: true };
            } catch (error) {
                return {
                    valid: false,
                    message: 'Invalid regular expression pattern'
                };
            }
        });

        // Percentage validator
        this.addValidator('percentage', (value, element) => {
            const numValue = parseFloat(value);
            return {
                valid: !isNaN(numValue) && numValue >= 0 && numValue <= 100,
                message: 'Must be a percentage between 0 and 100'
            };
        });
    }

    /**
     * Register forms in the dashboard
     */
    registerForms() {
        const forms = this.findAll('form, .form-section');
        
        forms.forEach(form => {
            const formId = form.id || `form-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
            if (!form.id) form.id = formId;
            
            this.registerForm(formId, {
                element: form,
                fields: this.getFormFields(form),
                validators: this.getFormValidators(form)
            });
        });
    }

    /**
     * Register a form
     * @param {string} formId - Form identifier
     * @param {Object} config - Form configuration
     */
    registerForm(formId, config) {
        this.forms.set(formId, {
            id: formId,
            element: config.element,
            fields: config.fields || [],
            validators: config.validators || [],
            isValid: true,
            errors: new Map(),
            lastSaved: null,
            ...config
        });

        // Setup form event listeners
        this.setupFormListeners(formId);
        
        // Initialize form data
        this.initializeFormData(formId);
    }

    /**
     * Get form fields
     * @param {HTMLElement} form - Form element
     */
    getFormFields(form) {
        return Array.from(form.querySelectorAll(
            'input, select, textarea, [data-field]'
        )).map(field => ({
            element: field,
            name: field.name || field.getAttribute('data-field'),
            type: field.type || 'text',
            required: field.hasAttribute('required'),
            validators: this.getFieldValidators(field)
        }));
    }

    /**
     * Get field validators
     * @param {HTMLElement} field - Field element
     */
    getFieldValidators(field) {
        const validators = [];
        
        if (field.hasAttribute('required')) {
            validators.push('required');
        }
        
        if (field.hasAttribute('min') || field.hasAttribute('max')) {
            validators.push('range');
        }
        
        if (field.hasAttribute('pattern')) {
            validators.push('pattern');
        }
        
        if (field.classList.contains('rule-pattern')) {
            validators.push('rulePattern');
        }
        
        if (field.classList.contains('percentage')) {
            validators.push('percentage');
        }
        
        // Custom validators from data attribute
        const customValidators = field.getAttribute('data-validators');
        if (customValidators) {
            validators.push(...customValidators.split(',').map(v => v.trim()));
        }
        
        return validators;
    }

    /**
     * Get form validators
     * @param {HTMLElement} form - Form element
     */
    getFormValidators(form) {
        const validators = [];
        
        // Custom form-level validators
        const customValidators = form.getAttribute('data-validators');
        if (customValidators) {
            validators.push(...customValidators.split(',').map(v => v.trim()));
        }
        
        return validators;
    }

    /**
     * Setup form event listeners
     * @param {string} formId - Form identifier
     */
    setupFormListeners(formId) {
        const form = this.forms.get(formId);
        if (!form) return;

        // Input change events
        form.fields.forEach(field => {
            if (this.options.validateOnChange) {
                field.element.addEventListener('input', () => {
                    this.validateField(formId, field.name);
                    this.markFormDirty(formId);
                    this.scheduleAutoSave(formId);
                });
            }
            
            if (this.options.validateOnBlur) {
                field.element.addEventListener('blur', () => {
                    this.validateField(formId, field.name);
                });
            }
            
            // Special handling for checkboxes and radios
            if (field.type === 'checkbox' || field.type === 'radio') {
                field.element.addEventListener('change', () => {
                    this.markFormDirty(formId);
                    this.scheduleAutoSave(formId);
                });
            }
        });

        // Form submission
        form.element.addEventListener('submit', (event) => {
            event.preventDefault();
            this.handleFormSubmit(formId);
        });
    }

    /**
     * Initialize form data
     * @param {string} formId - Form identifier
     */
    initializeFormData(formId) {
        const form = this.forms.get(formId);
        if (!form) return;

        const data = {};
        
        form.fields.forEach(field => {
            if (field.name) {
                data[field.name] = this.getFieldValue(field.element);
            }
        });
        
        this.formData.set(formId, data);
    }

    /**
     * Get field value
     * @param {HTMLElement} field - Field element
     */
    getFieldValue(field) {
        switch (field.type) {
            case 'checkbox':
                return field.checked;
            case 'radio':
                return field.checked ? field.value : null;
            case 'number':
            case 'range':
                return field.value ? parseFloat(field.value) : null;
            default:
                return field.value;
        }
    }

    /**
     * Set field value
     * @param {HTMLElement} field - Field element
     * @param {*} value - Value to set
     */
    setFieldValue(field, value) {
        switch (field.type) {
            case 'checkbox':
                field.checked = Boolean(value);
                break;
            case 'radio':
                field.checked = field.value === value;
                break;
            default:
                field.value = value || '';
        }
        
        // Trigger change event
        field.dispatchEvent(new Event('change', { bubbles: true }));
    }

    /**
     * Add validator
     * @param {string} name - Validator name
     * @param {Function} validator - Validator function
     */
    addValidator(name, validator) {
        this.validators.set(name, validator);
    }

    /**
     * Validate field
     * @param {string} formId - Form identifier
     * @param {string} fieldName - Field name
     */
    validateField(formId, fieldName) {
        const form = this.forms.get(formId);
        if (!form) return true;

        const field = form.fields.find(f => f.name === fieldName);
        if (!field) return true;

        const value = this.getFieldValue(field.element);
        const errors = [];

        // Run field validators
        field.validators.forEach(validatorName => {
            const validator = this.validators.get(validatorName);
            if (validator) {
                const result = validator(value, field.element);
                if (!result.valid) {
                    errors.push(result.message);
                }
            }
        });

        // Update field errors
        if (errors.length > 0) {
            form.errors.set(fieldName, errors);
            this.showFieldErrors(field.element, errors);
        } else {
            form.errors.delete(fieldName);
            this.clearFieldErrors(field.element);
        }

        // Update form validity
        form.isValid = form.errors.size === 0;
        
        return errors.length === 0;
    }

    /**
     * Validate entire form
     * @param {string} formId - Form identifier
     */
    validateForm(formId) {
        const form = this.forms.get(formId);
        if (!form) return false;

        let isValid = true;
        
        // Validate all fields
        form.fields.forEach(field => {
            if (field.name && !this.validateField(formId, field.name)) {
                isValid = false;
            }
        });

        // Run form-level validators
        const formData = this.getFormData(formId);
        form.validators.forEach(validatorName => {
            const validator = this.validators.get(validatorName);
            if (validator) {
                const result = validator(formData, form.element);
                if (!result.valid) {
                    isValid = false;
                    this.showFormError(formId, result.message);
                }
            }
        });

        form.isValid = isValid;
        return isValid;
    }

    /**
     * Show field errors
     * @param {HTMLElement} field - Field element
     * @param {Array} errors - Error messages
     */
    showFieldErrors(field, errors) {
        if (!this.options.showValidationErrors) return;

        // Remove existing error display
        this.clearFieldErrors(field);

        // Add error class
        field.classList.add('is-invalid');

        // Create error display
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.textContent = errors[0]; // Show first error
        
        // Insert after field
        field.parentNode.insertBefore(errorDiv, field.nextSibling);
    }

    /**
     * Clear field errors
     * @param {HTMLElement} field - Field element
     */
    clearFieldErrors(field) {
        field.classList.remove('is-invalid');
        
        // Remove error display
        const errorDiv = field.parentNode.querySelector('.invalid-feedback');
        if (errorDiv) {
            errorDiv.remove();
        }
    }

    /**
     * Show form error
     * @param {string} formId - Form identifier
     * @param {string} message - Error message
     */
    showFormError(formId, message) {
        const form = this.forms.get(formId);
        if (!form || !this.options.showValidationErrors) return;

        // Create or update form error display
        let errorDiv = form.element.querySelector('.form-error');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-danger form-error';
            form.element.insertBefore(errorDiv, form.element.firstChild);
        }
        
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }

    /**
     * Clear form errors
     * @param {string} formId - Form identifier
     */
    clearFormErrors(formId) {
        const form = this.forms.get(formId);
        if (!form) return;

        const errorDiv = form.element.querySelector('.form-error');
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
    }

    /**
     * Get form data
     * @param {string} formId - Form identifier
     */
    getFormData(formId) {
        const form = this.forms.get(formId);
        if (!form) return {};

        const data = {};
        
        form.fields.forEach(field => {
            if (field.name) {
                data[field.name] = this.getFieldValue(field.element);
            }
        });
        
        return data;
    }

    /**
     * Set form data
     * @param {string} formId - Form identifier
     * @param {Object} data - Form data
     */
    setFormData(formId, data) {
        const form = this.forms.get(formId);
        if (!form) return;

        form.fields.forEach(field => {
            if (field.name && data.hasOwnProperty(field.name)) {
                this.setFieldValue(field.element, data[field.name]);
            }
        });
        
        // Update cached form data
        this.formData.set(formId, { ...data });
        
        // Clear dirty flag
        this.isDirty = false;
    }

    /**
     * Mark form as dirty
     * @param {string} formId - Form identifier
     */
    markFormDirty(formId) {
        this.isDirty = true;
        this.emit('formDirty', { formId });
    }

    /**
     * Schedule auto-save
     * @param {string} formId - Form identifier
     */
    scheduleAutoSave(formId) {
        if (!this.options.autoSave) return;

        // Clear existing timer
        if (this.autoSaveTimers.has(formId)) {
            clearTimeout(this.autoSaveTimers.get(formId));
        }

        // Schedule new auto-save
        const timer = setTimeout(() => {
            this.autoSave(formId);
        }, this.options.autoSaveDelay);
        
        this.autoSaveTimers.set(formId, timer);
    }

    /**
     * Auto-save form
     * @param {string} formId - Form identifier
     */
    async autoSave(formId) {
        if (!this.isDirty) return;

        try {
            const data = this.getFormData(formId);
            
            // Persist data locally
            if (this.options.persistData) {
                this.persistFormData(formId, data);
            }
            
            // Update state
            if (window.StateManager) {
                window.StateManager.setState('currentConfig', data);
            }
            
            // Emit auto-save event
            this.emit('autoSave', { formId, data });
            
            // Update last saved timestamp
            const form = this.forms.get(formId);
            if (form) {
                form.lastSaved = Date.now();
            }
            
            this.isDirty = false;
            
        } catch (error) {
            console.error('Auto-save failed:', error);
        }
    }

    /**
     * Handle form submission
     * @param {string} formId - Form identifier
     */
    async handleFormSubmit(formId) {
        // Validate form
        if (!this.validateForm(formId)) {
            this.emit('formInvalid', { formId });
            return;
        }

        const data = this.getFormData(formId);
        
        try {
            // Emit submit event
            this.emit('formSubmit', { formId, data });
            
            // Clear dirty flag
            this.isDirty = false;
            
            // Update last saved
            const form = this.forms.get(formId);
            if (form) {
                form.lastSaved = Date.now();
            }
            
        } catch (error) {
            console.error('Form submission failed:', error);
            this.emit('formError', { formId, error });
        }
    }

    /**
     * Setup auto-save functionality
     */
    setupAutoSave() {
        // Save on page unload
        window.addEventListener('beforeunload', (event) => {
            if (this.isDirty) {
                event.preventDefault();
                event.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
                
                // Try to save immediately
                this.forms.forEach((form, formId) => {
                    this.autoSave(formId);
                });
            }
        });
    }

    /**
     * Persist form data to localStorage
     * @param {string} formId - Form identifier
     * @param {Object} data - Form data
     */
    persistFormData(formId, data) {
        try {
            const key = `${this.options.storageKey}-${formId}`;
            localStorage.setItem(key, JSON.stringify({
                data,
                timestamp: Date.now()
            }));
        } catch (error) {
            console.warn('Failed to persist form data:', error);
        }
    }

    /**
     * Load persisted form data
     */
    loadPersistedData() {
        try {
            const keys = Object.keys(localStorage).filter(key => 
                key.startsWith(this.options.storageKey)
            );
            
            keys.forEach(key => {
                const formId = key.replace(`${this.options.storageKey}-`, '');
                const stored = JSON.parse(localStorage.getItem(key));
                
                if (stored && stored.data) {
                    this.formData.set(formId, stored.data);
                }
            });
        } catch (error) {
            console.warn('Failed to load persisted form data:', error);
        }
    }

    /**
     * Clear persisted data
     * @param {string} formId - Form identifier (optional)
     */
    clearPersistedData(formId = null) {
        try {
            if (formId) {
                const key = `${this.options.storageKey}-${formId}`;
                localStorage.removeItem(key);
            } else {
                const keys = Object.keys(localStorage).filter(key => 
                    key.startsWith(this.options.storageKey)
                );
                keys.forEach(key => localStorage.removeItem(key));
            }
        } catch (error) {
            console.warn('Failed to clear persisted data:', error);
        }
    }

    /**
     * Reset form
     * @param {string} formId - Form identifier
     */
    resetForm(formId) {
        const form = this.forms.get(formId);
        if (!form) return;

        // Reset form element
        if (form.element.tagName === 'FORM') {
            form.element.reset();
        } else {
            // Manual reset for non-form elements
            form.fields.forEach(field => {
                switch (field.type) {
                    case 'checkbox':
                    case 'radio':
                        field.element.checked = false;
                        break;
                    default:
                        field.element.value = '';
                }
            });
        }

        // Clear errors
        form.errors.clear();
        this.clearFormErrors(formId);
        form.fields.forEach(field => this.clearFieldErrors(field.element));

        // Clear cached data
        this.formData.delete(formId);
        this.clearPersistedData(formId);
        
        // Clear dirty flag
        this.isDirty = false;
        
        // Emit reset event
        this.emit('formReset', { formId });
    }

    /**
     * Get form validation status
     * @param {string} formId - Form identifier
     */
    getFormStatus(formId) {
        const form = this.forms.get(formId);
        if (!form) return null;

        return {
            isValid: form.isValid,
            errors: Array.from(form.errors.entries()),
            isDirty: this.isDirty,
            lastSaved: form.lastSaved
        };
    }

    /**
     * Get all forms status
     */
    getAllFormsStatus() {
        const status = {};
        
        this.forms.forEach((form, formId) => {
            status[formId] = this.getFormStatus(formId);
        });
        
        return status;
    }

    onStateChange(path, newValue) {
        if (path === 'currentConfig' && newValue) {
            // Update forms with new configuration
            this.forms.forEach((form, formId) => {
                this.setFormData(formId, newValue);
            });
        }
    }

    async beforeDestroy() {
        // Clear auto-save timers
        this.autoSaveTimers.forEach(timer => clearTimeout(timer));
        this.autoSaveTimers.clear();
        
        // Save any pending changes
        if (this.isDirty) {
            this.forms.forEach((form, formId) => {
                this.autoSave(formId);
            });
        }
        
        // Clear forms
        this.forms.clear();
        this.formData.clear();
    }
}

export default FormManager;