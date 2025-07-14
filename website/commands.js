// Enhanced Commands Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // Performance optimizations
    const debounce = (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    };
    
    // Enhanced search with highlighting
    const searchInput = document.getElementById('commandSearch');
    const permissionFilter = document.getElementById('permissionFilter');
    const popularityFilter = document.getElementById('popularityFilter');
    const favoritesFilter = document.getElementById('favoritesFilter');
    const commandItems = document.querySelectorAll('.command-item');
    const categoryItems = document.querySelectorAll('.category-item');
    const searchResults = document.querySelector('.search-results');
    
    // Search with debouncing for better performance
    if (searchInput) {
        const debouncedSearch = debounce(filterCommands, 300);
        searchInput.addEventListener('input', debouncedSearch);
        
        // Enhanced search with keyboard navigation
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault();
                navigateSearchResults(e.key === 'ArrowDown' ? 1 : -1);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                selectSearchResult();
            } else if (e.key === 'Escape') {
                clearSearch();
            }
        });
    }
    
    // Enhanced filtering with visual feedback
    const filterCommands = () => {
        const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
        const permissionValue = permissionFilter ? permissionFilter.value : '';
        const popularityValue = popularityFilter ? popularityFilter.value : '';
        const showFavoritesOnly = favoritesFilter ? favoritesFilter.classList.contains('active') : false;
        
        let visibleCount = 0;
        const results = [];
        
        commandItems.forEach(item => {
            const commandText = item.textContent.toLowerCase();
            const commandName = item.querySelector('.command-syntax')?.textContent.toLowerCase() || '';
            const commandDesc = item.querySelector('.command-description')?.textContent.toLowerCase() || '';
            const permission = item.getAttribute('data-permission') || '';
            const popularity = item.getAttribute('data-popularity') || '';
            const category = item.getAttribute('data-category') || '';
            const isFavorite = item.querySelector('.favorite-btn.active') !== null;
            
            let shouldShow = true;
            let relevanceScore = 0;
            
            // Search filter with relevance scoring
            if (searchTerm) {
                if (commandName.includes(searchTerm)) {
                    relevanceScore += 10;
                } else if (commandDesc.includes(searchTerm)) {
                    relevanceScore += 5;
                } else if (commandText.includes(searchTerm)) {
                    relevanceScore += 1;
                } else {
                    shouldShow = false;
                }
            }
            
            // Other filters
            if (permissionValue && permission !== permissionValue) shouldShow = false;
            if (popularityValue && popularity !== popularityValue) shouldShow = false;
            if (showFavoritesOnly && !isFavorite) shouldShow = false;
            
            if (shouldShow) {
                visibleCount++;
                results.push({ item, relevanceScore });
                item.style.display = 'block';
                
                // Highlight search terms
                if (searchTerm) {
                    highlightSearchTerm(item, searchTerm);
                } else {
                    removeHighlight(item);
                }
            } else {
                item.style.display = 'none';
            }
        });
        
        // Sort by relevance
        if (searchTerm) {
            results.sort((a, b) => b.relevanceScore - a.relevanceScore);
            const container = document.querySelector('.commands-grid');
            if (container) {
                results.forEach(({ item }) => container.appendChild(item));
            }
        }
        
        // Update results counter
        updateResultsCounter(visibleCount);
        
        // Show/hide empty state
        showEmptyState(visibleCount === 0);
    };
    
    // Highlight search terms
    const highlightSearchTerm = (item, term) => {
        const nameElement = item.querySelector('.command-syntax');
        const descElement = item.querySelector('.command-description');
        
        if (nameElement) {
            nameElement.innerHTML = highlightText(nameElement.textContent, term);
        }
        if (descElement) {
            descElement.innerHTML = highlightText(descElement.textContent, term);
        }
    };
    
    // Remove highlights
    const removeHighlight = (item) => {
        const nameElement = item.querySelector('.command-syntax');
        const descElement = item.querySelector('.command-description');
        
        if (nameElement) {
            nameElement.innerHTML = nameElement.textContent;
        }
        if (descElement) {
            descElement.innerHTML = descElement.textContent;
        }
    };
    
    // Highlight text utility
    const highlightText = (text, term) => {
        const regex = new RegExp(`(${term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
        return text.replace(regex, '<mark>$1</mark>');
    };
    
    // Update results counter
    const updateResultsCounter = (count) => {
        const counter = document.querySelector('.results-counter');
        if (counter) {
            counter.textContent = `${count} commands found`;
        }
    };
    
    // Show empty state
    const showEmptyState = (show) => {
        const emptyState = document.querySelector('.empty-state');
        if (emptyState) {
            emptyState.style.display = show ? 'block' : 'none';
        }
    };
    
    // Enhanced category navigation with smooth scrolling
    categoryItems.forEach(item => {
        item.addEventListener('click', function() {
            const category = this.getAttribute('data-category');
            
            // Update active category with smooth transition
            categoryItems.forEach(cat => {
                cat.classList.remove('active');
                cat.setAttribute('aria-selected', 'false');
            });
            this.classList.add('active');
            this.setAttribute('aria-selected', 'true');
            
            // Filter commands by category
            filterByCategory(category);
            
            // Scroll to commands section
            const commandsSection = document.querySelector('.commands-section');
            if (commandsSection) {
                commandsSection.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
    
    // Enhanced filter by category
    const filterByCategory = (category) => {
        commandItems.forEach(item => {
            const itemCategory = item.getAttribute('data-category') || 'all';
            const shouldShow = category === 'all' || itemCategory === category;
            
            if (shouldShow) {
                item.style.display = 'block';
                item.classList.add('fade-in');
            } else {
                item.style.display = 'none';
                item.classList.remove('fade-in');
            }
        });
        
        // Update URL without reload
        const url = new URL(window.location);
        if (category === 'all') {
            url.searchParams.delete('category');
        } else {
            url.searchParams.set('category', category);
        }
        window.history.pushState({}, '', url);
    };
    
    // Enhanced favorites system with persistence
    const favoritesManager = {
        favorites: JSON.parse(localStorage.getItem('axion-favorites') || '[]'),
        
        toggle: function(commandId) {
            const index = this.favorites.indexOf(commandId);
            if (index > -1) {
                this.favorites.splice(index, 1);
            } else {
                this.favorites.push(commandId);
            }
            this.save();
            this.updateUI();
        },
        
        save: function() {
            localStorage.setItem('axion-favorites', JSON.stringify(this.favorites));
        },
        
        updateUI: function() {
            document.querySelectorAll('.favorite-btn').forEach(btn => {
                const commandId = btn.getAttribute('data-command-id');
                const isFavorite = this.favorites.includes(commandId);
                btn.classList.toggle('active', isFavorite);
                btn.setAttribute('aria-pressed', isFavorite);
                btn.querySelector('i').className = isFavorite ? 'fas fa-heart' : 'far fa-heart';
            });
            
            // Update favorites count
            const favoritesCount = document.querySelector('.favorites-count');
            if (favoritesCount) {
                favoritesCount.textContent = this.favorites.length;
            }
        }
    };
    
    // Initialize favorites
    favoritesManager.updateUI();
    
    // Favorite button handlers
    document.querySelectorAll('.favorite-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const commandId = this.getAttribute('data-command-id');
            favoritesManager.toggle(commandId);
            
            // Add visual feedback
            this.classList.add('pulse');
            setTimeout(() => this.classList.remove('pulse'), 300);
        });
    });
    
    // Enhanced command item interactions
    commandItems.forEach(item => {
        // Click to expand/collapse
        item.addEventListener('click', function() {
            const isExpanded = this.classList.contains('expanded');
            
            // Close other expanded items
            commandItems.forEach(otherItem => {
                if (otherItem !== this) {
                    otherItem.classList.remove('expanded');
                    otherItem.setAttribute('aria-expanded', 'false');
                }
            });
            
            // Toggle current item
            this.classList.toggle('expanded');
            this.setAttribute('aria-expanded', !isExpanded);
            
            // Scroll into view if expanding
            if (!isExpanded) {
                setTimeout(() => {
                    this.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }, 300);
            }
        });
        
        // Keyboard navigation
        item.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });
        
        // Copy command functionality
        const copyBtn = item.querySelector('.copy-btn');
        if (copyBtn) {
            copyBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                const commandText = this.getAttribute('data-command');
                
                navigator.clipboard.writeText(commandText).then(() => {
                    // Show success feedback
                    this.classList.add('copied');
                    this.innerHTML = '<i class="fas fa-check"></i> Copied!';
                    
                    setTimeout(() => {
                        this.classList.remove('copied');
                        this.innerHTML = '<i class="fas fa-copy"></i> Copy';
                    }, 2000);
                }).catch(err => {
                    console.error('Failed to copy:', err);
                });
            });
        }
    });
    
    // Filter handlers with visual feedback
    if (permissionFilter) {
        permissionFilter.addEventListener('change', function() {
            this.classList.add('filter-active');
            filterCommands();
        });
    }
    
    if (popularityFilter) {
        popularityFilter.addEventListener('change', function() {
            this.classList.add('filter-active');
            filterCommands();
        });
    }
    
    if (favoritesFilter) {
        favoritesFilter.addEventListener('click', function() {
            this.classList.toggle('active');
            filterCommands();
        });
    }
    
    // URL parameter handling
    const urlParams = new URLSearchParams(window.location.search);
    const categoryParam = urlParams.get('category');
    const searchParam = urlParams.get('search');
    
    if (categoryParam) {
        const categoryBtn = document.querySelector(`[data-category="${categoryParam}"]`);
        if (categoryBtn) {
            categoryBtn.click();
        }
    }
    
    if (searchParam && searchInput) {
        searchInput.value = searchParam;
        filterCommands();
    }
    
    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            if (searchInput) {
                searchInput.focus();
            }
        }
        
        // Escape to clear search
        if (e.key === 'Escape' && searchInput && searchInput.value) {
            searchInput.value = '';
            filterCommands();
        }
    });
    
    // Initialize counters animation
    const counters = document.querySelectorAll('[data-target]');
    if (counters.length > 0) {
        const counterObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !entry.target.classList.contains('animated')) {
                    entry.target.classList.add('animated');
                    animateCounter(entry.target);
                }
            });
        });
        
        counters.forEach(counter => counterObserver.observe(counter));
    }
    
    // Counter animation function
    function animateCounter(element) {
        const target = parseInt(element.getAttribute('data-target'));
        const duration = 2000;
        const start = 0;
        const increment = target / (duration / 16);
        let current = start;
        
        function update() {
            current += increment;
            if (current < target) {
                element.textContent = Math.floor(current);
                requestAnimationFrame(update);
            } else {
                element.textContent = target;
            }
        }
        
        update();
    }
    
    console.log('Enhanced commands page loaded successfully!');
});
