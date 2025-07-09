// Commands Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // Search functionality
    const searchInput = document.getElementById('commandSearch');
    const permissionFilter = document.getElementById('permissionFilter');
    const popularityFilter = document.getElementById('popularityFilter');
    const favoritesFilter = document.getElementById('favoritesFilter');
    const commandItems = document.querySelectorAll('.command-item');
    const categoryItems = document.querySelectorAll('.category-item');
    
    // Search input handler
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            filterCommands();
        });
    }
    
    // Filter handlers
    if (permissionFilter) {
        permissionFilter.addEventListener('change', filterCommands);
    }
    
    if (popularityFilter) {
        popularityFilter.addEventListener('change', filterCommands);
    }
    
    if (favoritesFilter) {
        favoritesFilter.addEventListener('click', function() {
            this.classList.toggle('active');
            filterCommands();
        });
    }
    
    // Category navigation
    categoryItems.forEach(item => {
        item.addEventListener('click', function() {
            const category = this.getAttribute('data-category');
            
            // Update active category
            categoryItems.forEach(cat => cat.classList.remove('active'));
            this.classList.add('active');
            
            // Filter commands by category
            filterByCategory(category);
        });
    });
    
    // Filter commands function
    function filterCommands() {
        const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
        const permissionValue = permissionFilter ? permissionFilter.value : '';
        const popularityValue = popularityFilter ? popularityFilter.value : '';
        const showFavoritesOnly = favoritesFilter ? favoritesFilter.classList.contains('active') : false;
        
        commandItems.forEach(item => {
            const commandText = item.textContent.toLowerCase();
            const permission = item.getAttribute('data-permission') || '';
            const popularity = item.getAttribute('data-popularity') || '';
            const isFavorite = item.querySelector('.favorite-btn.active') !== null;
            
            let shouldShow = true;
            
            // Search filter
            if (searchTerm && !commandText.includes(searchTerm)) {
                shouldShow = false;
            }
            
            // Permission filter
            if (permissionValue && permission !== permissionValue) {
                shouldShow = false;
            }
            
            // Popularity filter
            if (popularityValue && popularity !== popularityValue) {
                shouldShow = false;
            }
            
            // Favorites filter
            if (showFavoritesOnly && !isFavorite) {
                shouldShow = false;
            }
            
            item.style.display = shouldShow ? 'block' : 'none';
        });
    }
    
    // Filter by category function
    function filterByCategory(category) {
        commandItems.forEach(item => {
            if (category === 'all') {
                item.style.display = 'block';
            } else {
                const itemCategory = item.getAttribute('data-category') || 'all';
                item.style.display = itemCategory === category ? 'block' : 'none';
            }
        });
    }
    
    // Favorite button functionality
    document.querySelectorAll('.favorite-btn').forEach(btn => {
        btn.innerHTML = '<i class="far fa-heart"></i>';
        
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            this.classList.toggle('active');
            
            const icon = this.querySelector('i');
            if (this.classList.contains('active')) {
                icon.className = 'fas fa-heart';
                this.style.color = '#ef4444';
            } else {
                icon.className = 'far fa-heart';
                this.style.color = '';
            }
            
            // Save to localStorage
            const command = this.getAttribute('data-command');
            saveFavorite(command, this.classList.contains('active'));
        });
    });
    
    // Copy button functionality
    document.querySelectorAll('.copy-btn').forEach(btn => {
        btn.innerHTML = '<i class="far fa-copy"></i>';
        
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const command = this.getAttribute('data-command');
            
            navigator.clipboard.writeText(command).then(() => {
                // Visual feedback
                const originalIcon = this.innerHTML;
                this.innerHTML = '<i class="fas fa-check"></i>';
                this.style.color = '#10b981';
                
                setTimeout(() => {
                    this.innerHTML = originalIcon;
                    this.style.color = '';
                }, 1000);
                
                // Show toast notification
                showToast('Kommando kopieret til udklipsholder!');
            });
        });
    });
    
    // Command details toggle
    commandItems.forEach(item => {
        item.addEventListener('click', function() {
            const details = this.querySelector('.command-details');
            if (details) {
                const isVisible = details.style.display === 'block';
                details.style.display = isVisible ? 'none' : 'block';
                
                if (!isVisible) {
                    details.style.animation = 'fadeInUp 0.3s ease';
                }
            }
        });
    });
    
    // Save favorite to localStorage
    function saveFavorite(command, isFavorite) {
        let favorites = JSON.parse(localStorage.getItem('axion-favorites') || '[]');
        
        if (isFavorite && !favorites.includes(command)) {
            favorites.push(command);
        } else if (!isFavorite) {
            favorites = favorites.filter(fav => fav !== command);
        }
        
        localStorage.setItem('axion-favorites', JSON.stringify(favorites));
    }
    
    // Load favorites from localStorage
    function loadFavorites() {
        const favorites = JSON.parse(localStorage.getItem('axion-favorites') || '[]');
        
        document.querySelectorAll('.favorite-btn').forEach(btn => {
            const command = btn.getAttribute('data-command');
            if (favorites.includes(command)) {
                btn.classList.add('active');
                const icon = btn.querySelector('i');
                icon.className = 'fas fa-heart';
                btn.style.color = '#ef4444';
            }
        });
    }
    
    // Show toast notification
    function showToast(message) {
        const toast = document.createElement('div');
        toast.className = 'toast-notification';
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: rgba(16, 185, 129, 0.9);
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            backdrop-filter: blur(10px);
            z-index: 10000;
            transform: translateX(100%);
            transition: transform 0.3s ease;
            font-weight: 500;
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.style.transform = 'translateX(0)';
        }, 100);
        
        setTimeout(() => {
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, 3000);
    }
    
    // Smooth scroll for category navigation
    function smoothScrollToCommands() {
        const commandsSection = document.querySelector('.commands-list');
        if (commandsSection) {
            commandsSection.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }
    
    // Initialize favorites on page load
    loadFavorites();
    
    // Animate hero stats counters
    function animateCounter(element, target, duration = 2000) {
        let start = 0;
        const increment = target / (duration / 16);
        
        function updateCounter() {
            start += increment;
            if (start < target) {
                element.textContent = Math.floor(start);
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = target;
            }
        }
        updateCounter();
    }

    // Trigger counter animation when stats come into view
    const statsObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const target = parseInt(entry.target.getAttribute('data-target'));
                if (target) {
                    animateCounter(entry.target, target);
                }
            }
        });
    }, {
        threshold: 0.5
    });

    document.querySelectorAll('.stat-number[data-target]').forEach(stat => {
        statsObserver.observe(stat);
    });

    // Animate popular commands cards
    const popularObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.animation = 'fadeInUp 0.6s ease forwards';
                }, index * 150);
            }
        });
    }, {
        threshold: 0.1
    });
    
    document.querySelectorAll('.popular-card').forEach(card => {
        popularObserver.observe(card);
    });

    // Dynamic particles for hero
    function createParticle() {
        const particle = document.createElement('div');
        particle.style.cssText = `
            position: absolute;
            width: 3px;
            height: 3px;
            background: ${Math.random() > 0.5 ? 'var(--primary-color)' : 'var(--secondary-color)'};
            border-radius: 50%;
            pointer-events: none;
            z-index: 1;
            animation: floatingParticles ${6 + Math.random() * 4}s infinite;
            top: ${Math.random() * 100}%;
            left: ${Math.random() * 100}%;
            opacity: ${0.3 + Math.random() * 0.4};
        `;
        
        const heroParticles = document.querySelector('.hero-particles');
        if (heroParticles) {
            heroParticles.appendChild(particle);
            
            setTimeout(() => {
                if (particle.parentNode) {
                    particle.parentNode.removeChild(particle);
                }
            }, 10000);
        }
    }

    // Create particles periodically
    setInterval(createParticle, 2000);
    
    // Add keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K for search focus
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            if (searchInput) {
                searchInput.focus();
            }
        }
        
        // Escape to clear search
        if (e.key === 'Escape' && searchInput) {
            searchInput.value = '';
            filterCommands();
        }
    });
    
    // Search suggestions (simple implementation)
    if (searchInput) {
        const suggestions = ['ban', 'play', 'meme', 'remind', 'daily', 'rank', 'kick', 'mute', 'queue', 'skip'];
        
        searchInput.addEventListener('focus', function() {
            if (!this.value) {
                showSearchSuggestions(suggestions);
            }
        });
        
        searchInput.addEventListener('blur', function() {
            setTimeout(() => {
                hideSearchSuggestions();
            }, 200);
        });
    }
    
    function showSearchSuggestions(suggestions) {
        const suggestionsContainer = document.querySelector('.search-suggestions');
        if (!suggestionsContainer) return;
        
        suggestionsContainer.innerHTML = suggestions.map(suggestion => 
            `<div class="suggestion-item">${suggestion}</div>`
        ).join('');
        
        suggestionsContainer.style.display = 'block';
        
        // Add click handlers to suggestions
        suggestionsContainer.querySelectorAll('.suggestion-item').forEach(item => {
            item.addEventListener('click', function() {
                if (searchInput) {
                    searchInput.value = this.textContent;
                    filterCommands();
                }
                hideSearchSuggestions();
            });
        });
    }
    
    function hideSearchSuggestions() {
        const suggestionsContainer = document.querySelector('.search-suggestions');
        if (suggestionsContainer) {
            suggestionsContainer.style.display = 'none';
        }
    }
    
    // Animate command cards on scroll
    const commandObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.animation = 'fadeInUp 0.5s ease forwards';
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });
    
    commandItems.forEach(item => {
        commandObserver.observe(item);
    });
});
