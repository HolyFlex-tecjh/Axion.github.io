// Enhanced Navigation functionality
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all components
    initNavigation();
    initAnimations();
    initCommandTabs();
    initCounters();
    initParticles();
    initScrollEffects();
    initFormHandlers();
    initHeroEffects();
    initEnhancedAnimations();
    
    // Enhanced loading animation
    document.body.style.opacity = '0';
    document.body.style.transform = 'translateY(20px)';
    setTimeout(() => {
        document.body.style.transition = 'all 1s cubic-bezier(0.4, 0, 0.2, 1)';
        document.body.style.opacity = '1';
        document.body.style.transform = 'translateY(0)';
    }, 100);
    
    // Initialize authentication
    initAuth();
});

function initHeroEffects() {
    // Animated counter for hero stats
    const statNumbers = document.querySelectorAll('.stat-number');
    
    statNumbers.forEach(stat => {
        const target = parseInt(stat.getAttribute('data-target'));
        const isDecimal = stat.getAttribute('data-target').includes('.');
        let current = 0;
        const increment = target / 100;
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            
            if (isDecimal) {
                stat.textContent = current.toFixed(1) + '%';
            } else if (target >= 1000000) {
                stat.textContent = (current / 1000000).toFixed(1) + 'M+';
            } else if (target >= 1000) {
                stat.textContent = Math.floor(current / 1000) + 'K+';
            } else {
                stat.textContent = Math.floor(current);
            }
        }, 30);
    });

    // Enhanced particle effect for hero
    createHeroParticles();
}

function createHeroParticles() {
    const hero = document.querySelector('.hero');
    if (!hero) return;

    // Create floating particles
    for (let i = 0; i < 50; i++) {
        const particle = document.createElement('div');
        particle.className = 'floating-particle';
        particle.style.cssText = `
            position: absolute;
            width: ${Math.random() * 4 + 2}px;
            height: ${Math.random() * 4 + 2}px;
            background: rgba(88, 101, 242, ${Math.random() * 0.6 + 0.2});
            border-radius: 50%;
            left: ${Math.random() * 100}%;
            top: ${Math.random() * 100}%;
            animation: float ${Math.random() * 10 + 10}s ease-in-out infinite;
            animation-delay: ${Math.random() * 5}s;
            pointer-events: none;
            z-index: 1;
        `;
        hero.appendChild(particle);
    }
}

function initEnhancedAnimations() {
    // Enhanced button ripple effect
    document.querySelectorAll('.btn').forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.6);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s linear;
                pointer-events: none;
            `;
            
            this.appendChild(ripple);
            setTimeout(() => ripple.remove(), 600);
        });
    });

    // Enhanced hover effects for cards
    document.querySelectorAll('.feature-card, .pricing-card').forEach(card => {
        card.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;
            
            this.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-8px)`;
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateY(0)';
        });
    });
}

function initNavigation() {
    const navbar = document.querySelector('.navbar');
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    
    // Mobile menu toggle
    if (hamburger) {
        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
            document.body.classList.toggle('menu-open');
        });
    }

    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
                
                // Close mobile menu if open
                if (navMenu.classList.contains('active')) {
                    hamburger.classList.remove('active');
                    navMenu.classList.remove('active');
                    document.body.classList.remove('menu-open');
                }
            }
        });
    });

    // Enhanced navbar background on scroll with parallax
    let lastScrollTop = 0;
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const scrollProgress = Math.min(scrollTop / 500, 1);
        
        if (scrollTop > 50) {
            navbar.classList.add('scrolled');
            navbar.style.backgroundColor = `rgba(15, 15, 35, ${0.9 + scrollProgress * 0.1})`;
        } else {
            navbar.classList.remove('scrolled');
            navbar.style.backgroundColor = 'rgba(15, 15, 35, 0.9)';
        }
        
        // Hide/show navbar based on scroll direction (only on mobile)
        if (window.innerWidth <= 768) {
            if (scrollTop > lastScrollTop && scrollTop > 200) {
                navbar.style.transform = 'translateY(-100%)';
            } else {
                navbar.style.transform = 'translateY(0)';
            }
        }
        
        // Parallax effect for hero background
        const hero = document.querySelector('.hero');
        if (hero && scrollTop < hero.offsetHeight) {
            const parallaxSpeed = scrollTop * 0.5;
            hero.style.transform = `translateY(${parallaxSpeed}px)`;
        }
        
        lastScrollTop = scrollTop;
    });
}

function initAnimations() {
    // Enhanced intersection observer for animations
    const observerOptions = {
        threshold: 0.15,
        rootMargin: '0px 0px -100px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const element = entry.target;
                
                // Add staggered animation for child elements
                if (element.classList.contains('features-grid')) {
                    const cards = element.querySelectorAll('.feature-card');
                    cards.forEach((card, index) => {
                        setTimeout(() => {
                            card.style.opacity = '1';
                            card.style.transform = 'translateY(0) scale(1)';
                        }, index * 200);
                    });
                } else if (element.classList.contains('pricing-grid')) {
                    const cards = element.querySelectorAll('.pricing-card');
                    cards.forEach((card, index) => {
                        setTimeout(() => {
                            card.style.opacity = '1';
                            card.style.transform = 'translateY(0) scale(1)';
                        }, index * 200);
                    });
                } else {
                    element.style.opacity = '1';
                    element.style.transform = 'translateY(0)';
                }
            }
        });
    }, observerOptions);

    // Observe elements for animation
    document.querySelectorAll('.feature-card, .pricing-card, .section-header, .hero-content').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(50px)';
        el.style.transition = 'opacity 0.8s cubic-bezier(0.4, 0, 0.2, 1), transform 0.8s cubic-bezier(0.4, 0, 0.2, 1)';
    });
    
    document.querySelectorAll('.features-grid, .pricing-grid, .section-header, .hero-content').forEach(el => {
        observer.observe(el);
    });
}

function initCommandTabs() {
    const categoryTabs = document.querySelectorAll('.category-tab');
    const commandGroups = document.querySelectorAll('.command-group');
    const searchInput = document.getElementById('commandSearch');
    const permissionFilter = document.getElementById('permissionFilter');
    const popularityFilter = document.getElementById('popularityFilter');
    const favoritesToggle = document.getElementById('favoritesToggle');
    
    let favorites = JSON.parse(localStorage.getItem('axion-favorites') || '[]');
    let allCommands = [];
    
    // Initialize favorites
    updateFavoriteButtons();
    
    // Collect all commands for search
    function collectAllCommands() {
        allCommands = [];
        document.querySelectorAll('.command-item').forEach(item => {
            const syntax = item.querySelector('.command-syntax').textContent.replace('$', '');
            const description = item.querySelector('.command-description').textContent;
            const permission = item.getAttribute('data-permission');
            const popularity = item.getAttribute('data-popularity');
            const category = item.closest('.command-group').id;
            allCommands.push({
                element: item,
                syntax,
                description,
                permission,
                popularity,
                category,
                searchText: (syntax + ' ' + description).toLowerCase()
            });
        });
    }
    
    // Initialize
    collectAllCommands();
    
    // Category tab switching
    categoryTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const category = this.getAttribute('data-category');
            
            // Remove active class from all tabs and groups
            categoryTabs.forEach(t => t.classList.remove('active'));
            commandGroups.forEach(g => g.classList.remove('active'));
            
            // Add active class to clicked tab and corresponding group
            this.classList.add('active');
            const targetGroup = document.getElementById(category);
            if (targetGroup) {
                targetGroup.classList.add('active');
                
                // Animate command items with enhanced stagger
                const commandItems = targetGroup.querySelectorAll('.command-item');
                commandItems.forEach((item, index) => {
                    item.style.opacity = '0';
                    item.style.transform = 'translateY(30px) rotateX(10deg)';
                    setTimeout(() => {
                        item.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
                        item.style.opacity = '1';
                        item.style.transform = 'translateY(0) rotateX(0deg)';
                    }, index * 150);
                });
            }
        });
    });
    
    // Search functionality
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                performSearch();
            }, 300);
        });
    }
    
    // Filter functionality
    if (permissionFilter) {
        permissionFilter.addEventListener('change', performSearch);
    }
    
    if (popularityFilter) {
        popularityFilter.addEventListener('change', performSearch);
    }
    
    // Favorites toggle
    if (favoritesToggle) {
        favoritesToggle.addEventListener('click', function() {
            this.classList.toggle('active');
            performSearch();
        });
    }
    
    // Perform search and filtering
    function performSearch() {
        const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
        const permissionValue = permissionFilter ? permissionFilter.value : '';
        const popularityValue = popularityFilter ? popularityFilter.value : '';
        const showFavoritesOnly = favoritesToggle ? favoritesToggle.classList.contains('active') : false;
        
        let hasResults = false;
        
        commandGroups.forEach(group => {
            const commands = group.querySelectorAll('.command-item');
            let groupHasVisibleCommands = false;
            
            commands.forEach(command => {
                const commandData = allCommands.find(cmd => cmd.element === command);
                if (!commandData) return;
                
                let shouldShow = true;
                
                // Search filter
                if (searchTerm && !commandData.searchText.includes(searchTerm)) {
                    shouldShow = false;
                }
                
                // Permission filter
                if (permissionValue && commandData.permission !== permissionValue) {
                    shouldShow = false;
                }
                
                // Popularity filter
                if (popularityValue && commandData.popularity !== popularityValue) {
                    shouldShow = false;
                }
                
                // Favorites filter
                if (showFavoritesOnly) {
                    const commandName = command.querySelector('.favorite-btn').getAttribute('data-command');
                    if (!favorites.includes(commandName)) {
                        shouldShow = false;
                    }
                }
                
                if (shouldShow) {
                    command.style.display = 'block';
                    groupHasVisibleCommands = true;
                    hasResults = true;
                } else {
                    command.style.display = 'none';
                }
            });
            
            // Show/hide category tabs based on visible commands
            const categoryTab = document.querySelector(`[data-category="${group.id}"]`);
            if (categoryTab) {
                if (groupHasVisibleCommands) {
                    categoryTab.style.display = 'flex';
                } else {
                    categoryTab.style.display = 'none';
                }
            }
        });
        
        // Show no results message
        showNoResultsMessage(!hasResults);
    }
    
    // Show/hide no results message
    function showNoResultsMessage(show) {
        let noResultsEl = document.querySelector('.no-results-message');
        
        if (show && !noResultsEl) {
            noResultsEl = document.createElement('div');
            noResultsEl.className = 'no-results-message';
            noResultsEl.innerHTML = `
                <div class="no-results-content">
                    <i class="fas fa-search"></i>
                    <h3>Ingen kommandoer fundet</h3>
                    <p>Prøv at justere dine søgekriterier eller filtre</p>
                </div>
            `;
            document.querySelector('.commands-list').appendChild(noResultsEl);
        } else if (!show && noResultsEl) {
            noResultsEl.remove();
        }
    }
    
    // Favorite functionality
    document.addEventListener('click', function(e) {
        if (e.target.closest('.favorite-btn')) {
            e.preventDefault();
            const btn = e.target.closest('.favorite-btn');
            const commandName = btn.getAttribute('data-command');
            
            if (favorites.includes(commandName)) {
                favorites = favorites.filter(fav => fav !== commandName);
                btn.classList.remove('active');
                btn.querySelector('i').className = 'far fa-heart';
            } else {
                favorites.push(commandName);
                btn.classList.add('active');
                btn.querySelector('i').className = 'fas fa-heart';
            }
            
            localStorage.setItem('axion-favorites', JSON.stringify(favorites));
            updateFavoritesCount();
        }
    });
    
    // Copy functionality with enhanced feedback
    document.addEventListener('click', function(e) {
        if (e.target.closest('.copy-btn')) {
            e.preventDefault();
            const btn = e.target.closest('.copy-btn');
            const commandText = btn.getAttribute('data-command');
            
            navigator.clipboard.writeText(commandText).then(() => {
                // Visual feedback
                const originalHTML = btn.innerHTML;
                btn.innerHTML = '<i class="fas fa-check"></i>';
                btn.style.background = 'rgba(16, 185, 129, 0.3)';
                btn.style.borderColor = '#10b981';
                btn.style.color = '#10b981';
                
                setTimeout(() => {
                    btn.innerHTML = originalHTML;
                    btn.style.background = '';
                    btn.style.borderColor = '';
                    btn.style.color = '';
                }, 1500);
                
                // Show toast notification
                showToast('Kommando kopieret til udklipsholder!', 'success');
            }).catch(() => {
                showToast('Kunne ikke kopiere kommando', 'error');
            });
        }
    });
    
    // Update favorite buttons based on stored favorites
    function updateFavoriteButtons() {
        document.querySelectorAll('.favorite-btn').forEach(btn => {
            const commandName = btn.getAttribute('data-command');
            if (favorites.includes(commandName)) {
                btn.classList.add('active');
                btn.querySelector('i').className = 'fas fa-heart';
            } else {
                btn.classList.remove('active');
                btn.querySelector('i').className = 'far fa-heart';
            }
        });
        updateFavoritesCount();
    }
    
    // Update favorites count in toggle button
    function updateFavoritesCount() {
        if (favoritesToggle) {
            const count = favorites.length;
            const text = count > 0 ? `Favoritter (${count})` : 'Vis favoritter';
            favoritesToggle.innerHTML = `<i class="fas fa-heart"></i> ${text}`;
        }
    }
    
    // Enhanced command item hover effects
    document.querySelectorAll('.command-item').forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px) scale(1.02)';
            this.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
            
            // Show preview
            showCommandPreview(this);
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
    
    // Command preview functionality
    function showCommandPreview(commandItem) {
        const previewContent = document.querySelector('.preview-content');
        if (!previewContent) return;
        
        const syntax = commandItem.querySelector('.command-syntax').textContent.replace('$', '');
        const description = commandItem.querySelector('.command-description').textContent;
        const category = commandItem.closest('.command-group').id;
        
        // Generate preview based on command type
        let previewHTML = generatePreviewHTML(syntax, description, category);
        
        previewContent.innerHTML = previewHTML;
        
        // Add entrance animation
        const previewEl = previewContent.querySelector('.preview-discord-mockup');
        if (previewEl) {
            previewEl.style.opacity = '0';
            previewEl.style.transform = 'translateY(20px)';
            setTimeout(() => {
                previewEl.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
                previewEl.style.opacity = '1';
                previewEl.style.transform = 'translateY(0)';
            }, 50);
        }
    }
    
    function generatePreviewHTML(syntax, description, category) {
        const command = syntax.trim();
        
        // Command-specific previews
        const previews = {
            'ban @bruger [årsag]': {
                command: '!ban @ToxicUser Spam og harassment',
                response: `✅ **Bruger banned**\n📝 **Årsag:** Spam og harassment\n⏰ **Tidspunkt:** ${new Date().toLocaleTimeString('da-DK')}\n👮 **Moderator:** <@123456789>`,
                embed: {
                    title: 'Moderation Log',
                    fields: [
                        { name: 'Handling', value: 'Ban' },
                        { name: 'Varighed', value: 'Permanent' },
                        { name: 'Kan anke', value: 'Ja' }
                    ]
                }
            },
            'play [sang/url]': {
                command: '!play Bohemian Rhapsody',
                response: `🎵 **Nu afspiller**\n**${getRandomSong()}**\n⏱️ Længde: 5:55\n👤 Anmodet af: <@987654321>\n🔊 Lydstyrke: 75%`,
                embed: {
                    title: 'Musik Kø',
                    fields: [
                        { name: 'Position', value: '1/12' },
                        { name: 'Estimeret tid', value: '23:45' },
                        { name: 'Loop mode', value: 'Off' }
                    ]
                }
            },
            'serverinfo': {
                command: '!serverinfo',
                response: `📊 **Server Information**`,
                embed: {
                    title: 'Axion Test Server',
                    fields: [
                        { name: '👥 Medlemmer', value: '1,247' },
                        { name: '📅 Oprettet', value: '15. Jan 2022' },
                        { name: '🚀 Boost Level', value: 'Level 2' },
                        { name: '💬 Kanaler', value: '45 Total' },
                        { name: '🎭 Roller', value: '23 Roller' },
                        { name: '😀 Emojis', value: '156 Custom' }
                    ]
                }
            },
            'balance [@bruger]': {
                command: '!balance',
                response: `💰 **Din balance**`,
                embed: {
                    title: 'Økonomi Oversigt',
                    fields: [
                        { name: '💵 Kontanter', value: '12,350 coins' },
                        { name: '🏦 Bank', value: '45,670 coins' },
                        { name: '💎 Total formue', value: '58,020 coins' },
                        { name: '📈 Ranking', value: '#42 på serveren' }
                    ]
                }
            },
            'meme [kategori]': {
                command: '!meme dank',
                response: `😂 **Random Dank Meme**\n⭐ Rating: 9.2/10\n👍 42 likes | 💬 12 kommentarer\n📱 Fra: r/dankmemes`,
                embed: {
                    title: 'When you realize it\'s Friday',
                    fields: [
                        { name: '🎯 Kategori', value: 'Dank Memes' },
                        { name: '🔥 Hotness', value: '🔥🔥🔥🔥🔥' }
                    ]
                }
            }
        };
        
        const preview = previews[command] || {
            command: `!${command}`,
            response: `✅ **Kommando udført**\n📝 ${description}`,
            embed: {
                title: 'Kommando Response',
                fields: [
                    { name: 'Status', value: 'Successful' },
                    { name: 'Kategori', value: category.charAt(0).toUpperCase() + category.slice(1) }
                ]
            }
        };
        
        return `
            <div class="preview-discord-mockup">
                <div class="preview-user-message">
                    <div class="preview-avatar"></div>
                    <div>
                        <div class="preview-username">Bruger</div>
                        <div class="preview-command">${preview.command}</div>
                    </div>
                </div>
                <div class="preview-bot-response">
                    <div class="preview-bot-avatar">A</div>
                    <div>
                        <div class="preview-bot-username">
                            Axion Bot
                            <span class="preview-bot-tag">BOT</span>
                        </div>
                        <div class="preview-response-text">${preview.response}</div>
                        ${preview.embed ? `
                            <div class="preview-embed">
                                <div class="preview-embed-title">${preview.embed.title}</div>
                                ${preview.embed.fields.map(field => `
                                    <div class="preview-embed-field">
                                        <div class="preview-embed-field-name">${field.name}</div>
                                        <div class="preview-embed-field-value">${field.value}</div>
                                    </div>
                                `).join('')}
                            </div>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }
    
    function getRandomSong() {
        const songs = [
            'Queen - Bohemian Rhapsody',
            'The Beatles - Yesterday',
            'Led Zeppelin - Stairway to Heaven',
            'Pink Floyd - Wish You Were Here',
            'AC/DC - Back in Black',
            'Nirvana - Smells Like Teen Spirit',
            'Radiohead - Creep',
            'The Rolling Stones - Paint It Black'
        ];
        return songs[Math.floor(Math.random() * songs.length)];
    }
}

// Toast notification system
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 100);
    
    // Remove after delay
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function initCounters() {
    let countersAnimated = false;
    
    function animateCounters() {
        if (countersAnimated) return;
        countersAnimated = true;
        
        const counters = document.querySelectorAll('.stat-number');
        counters.forEach(counter => {
            const target = parseInt(counter.textContent.replace(/[^\d]/g, ''));
            const suffix = counter.textContent.replace(/[\d,]/g, '');
            const duration = 2000;
            const increment = target / (duration / 16);
            let current = 0;
            
            const updateCounter = () => {
                current += increment;
                if (current < target) {
                    counter.textContent = Math.ceil(current).toLocaleString() + suffix;
                    requestAnimationFrame(updateCounter);
                } else {
                    counter.textContent = target.toLocaleString() + suffix;
                }
            };
            
            updateCounter();
        });
    }

    const heroObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounters();
            }
        });
    });

    const heroSection = document.querySelector('.hero-stats');
    if (heroSection) {
        heroObserver.observe(heroSection);
    }
}

function initParticles() {
    const hero = document.querySelector('.hero');
    if (!hero) return;
    
    const particleCount = 80;
    
    for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        
        const size = Math.random() * 4 + 2;
        const x = Math.random() * 100;
        const y = Math.random() * 100;
        const duration = Math.random() * 20 + 10;
        const delay = Math.random() * 20;
        
        particle.style.cssText = `
            position: absolute;
            width: ${size}px;
            height: ${size}px;
            background: rgba(255, 255, 255, ${Math.random() * 0.5 + 0.2});
            border-radius: 50%;
            left: ${x}%;
            top: ${y}%;
            animation: floatParticle ${duration}s infinite linear;
            animation-delay: ${delay}s;
            pointer-events: none;
            z-index: 0;
        `;
        
        hero.appendChild(particle);
    }
}

function initScrollEffects() {
    // Parallax effect for hero section
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        const parallaxElements = document.querySelectorAll('.hero::before');
        
        parallaxElements.forEach(element => {
            const speed = 0.5;
            element.style.transform = `translateY(${scrolled * speed}px)`;
        });
    });
    
    // Progress indicator
    const progressBar = document.createElement('div');
    progressBar.className = 'scroll-progress';
    progressBar.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 0%;
        height: 3px;
        background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
        z-index: 9999;
        transition: width 0.3s ease;
    `;
    document.body.appendChild(progressBar);
    
    window.addEventListener('scroll', () => {
        const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrollPercent = (scrollTop / scrollHeight) * 100;
        progressBar.style.width = scrollPercent + '%';
    });
}

function initFormHandlers() {
    // Discord bot invite functionality
    const inviteButtons = document.querySelectorAll('a[href="#"]');
    inviteButtons.forEach(button => {
        if (button.textContent.includes('Tilføj til Discord') || button.textContent.includes('Tilføj til Server')) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                
                // Add loading effect
                const originalText = this.innerHTML;
                this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Åbner Discord...';
                this.style.pointerEvents = 'none';
                
                setTimeout(() => {
                    // Replace with your actual bot invite URL
                    const botInviteURL = 'https://discord.com/api/oauth2/authorize?client_id=YOUR_BOT_ID&permissions=8&scope=bot%20applications.commands';
                    window.open(botInviteURL, '_blank');
                    
                    this.innerHTML = originalText;
                    this.style.pointerEvents = 'auto';
                }, 1000);
            });
        }
    });

    // Copy command to clipboard with enhanced feedback
    document.querySelectorAll('.command-syntax').forEach(command => {
        command.style.cursor = 'pointer';
        command.title = 'Klik for at kopiere kommando';
        
        command.addEventListener('click', function() {
            navigator.clipboard.writeText(this.textContent).then(() => {
                // Create floating notification
                const notification = document.createElement('div');
                notification.textContent = 'Kopieret! ✓';
                notification.style.cssText = `
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    background: var(--accent-color);
                    color: white;
                    padding: 12px 20px;
                    border-radius: 8px;
                    font-weight: 600;
                    z-index: 10000;
                    animation: slideInRight 0.3s ease;
                `;
                
                document.body.appendChild(notification);
                
                setTimeout(() => {
                    notification.style.animation = 'slideOutRight 0.3s ease forwards';
                    setTimeout(() => notification.remove(), 300);
                }, 2000);
            });
        });
    });
}

// Initialize auth-related functionality
function initAuth() {
    checkUserAuth();
    setupAuthEventListeners();
}

function checkUserAuth() {
    const token = localStorage.getItem('axion-auth-token');
    const userStr = localStorage.getItem('axion-user');
    
    if (token && userStr) {
        const user = JSON.parse(userStr);
        showAuthenticatedState(user);
    } else {
        showUnauthenticatedState();
    }
}

function showAuthenticatedState(user) {
    const navButtons = document.querySelector('.nav-buttons');
    const userMenu = document.getElementById('userMenu');
    const userAvatar = document.getElementById('userAvatar');
    const userName = document.getElementById('userName');
    
    if (navButtons) {
        navButtons.classList.add('authenticated');
    }
    
    if (userMenu) {
        userMenu.style.display = 'flex';
    }
    
    if (userAvatar && user.avatar) {
        userAvatar.src = user.avatar;
    }
    
    if (userName) {
        userName.textContent = user.username;
    }
    
    // Update invite button to go to dashboard if user has servers with bot
    const inviteBtn = document.getElementById('inviteBtn');
    if (inviteBtn && user.guilds && user.guilds.some(g => g.botPresent)) {
        inviteBtn.href = 'dashboard.html';
        inviteBtn.innerHTML = '<i class="fas fa-tachometer-alt"></i> Dashboard';
    }
}

function showUnauthenticatedState() {
    const navButtons = document.querySelector('.nav-buttons');
    const userMenu = document.getElementById('userMenu');
    
    if (navButtons) {
        navButtons.classList.remove('authenticated');
    }
    
    if (userMenu) {
        userMenu.style.display = 'none';
    }
}

function setupAuthEventListeners() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            handleLogout();
        });
    }
    
    // Setup invite button for non-authenticated users
    const inviteBtn = document.getElementById('inviteBtn');
    if (inviteBtn) {
        inviteBtn.addEventListener('click', function(e) {
            const token = localStorage.getItem('axion-auth-token');
            if (!token && this.getAttribute('href') === '#invite') {
                e.preventDefault();
                // Redirect to login with return URL
                window.location.href = 'login.html?redirect=invite';
            }
        });
    }
}

function handleLogout() {
    // Show confirmation
    if (confirm('Er du sikker på at du vil logge ud?')) {
        localStorage.removeItem('axion-auth-token');
        localStorage.removeItem('axion-user');
        localStorage.removeItem('axion-login-time');
        
        // Show logout animation
        showToast('Du er blevet logget ud', 'info');
        
        // Reset UI
        setTimeout(() => {
            showUnauthenticatedState();
        }, 500);
    }
}

// Add enhanced CSS animations
const enhancedStyles = document.createElement('style');
enhancedStyles.textContent = `
    @keyframes floatParticle {
        0% { transform: translateY(0px) rotate(0deg); opacity: 1; }
        100% { transform: translateY(-100vh) rotate(360deg); opacity: 0; }
    }
    
    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    @media (max-width: 768px) {
        .nav-menu.active {
            display: flex;
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            flex-direction: column;
            padding: 20px;
            box-shadow: var(--shadow-xl);
            border-top: 1px solid var(--border-color);
            animation: slideDown 0.3s ease;
        }
        
        @keyframes slideDown {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .hamburger.active span:nth-child(1) {
            transform: rotate(45deg) translate(5px, 5px);
        }
        
        .hamburger.active span:nth-child(2) {
            opacity: 0;
        }
        
        .hamburger.active span:nth-child(3) {
            transform: rotate(-45deg) translate(7px, -6px);
        }
    }
    
    .pricing-card.featured {
        animation: pulse 2s infinite;
    }
    
    @keyframes pulse {
        0% { box-shadow: var(--shadow-lg); }
        50% { box-shadow: var(--shadow-2xl); }
        100% { box-shadow: var(--shadow-lg); }
    }
    
    .feature-card:hover .feature-icon {
        transform: scale(1.1) rotate(5deg);
    }
    
    .hero-buttons .btn:hover {
        transform: translateY(-3px) scale(1.02);
    }
    
    .stat {
        animation: bounceIn 1s ease 0.5s both;
    }
    
    @keyframes bounceIn {
        0% { transform: scale(0.3); opacity: 0; }
        50% { transform: scale(1.05); }
        70% { transform: scale(0.9); }
        100% { transform: scale(1); opacity: 1; }
    }
    
    .discord-mockup {
        animation: float 6s ease-in-out infinite;
    }
    
    .message {
        animation: messageSlide 0.6s ease-out;
    }
    
    .message:nth-child(even) {
        animation-delay: 0.3s;
    }
`;

document.head.appendChild(enhancedStyles);

// Performance monitoring and optimization
if ('performance' in window) {
    window.addEventListener('load', function() {
        setTimeout(() => {
            const perfData = performance.getEntriesByType('navigation')[0];
            if (perfData.loadEventEnd - perfData.loadEventStart > 3000) {
                console.warn('Page load time is slow:', perfData.loadEventEnd - perfData.loadEventStart + 'ms');
            }
        }, 0);
    });
}

// Easter egg: Konami code with enhanced effects
let konamiCode = [];
const konamiSequence = [
    'ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown',
    'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight',
    'KeyB', 'KeyA'
];

document.addEventListener('keydown', function(e) {
    konamiCode.push(e.code);
    if (konamiCode.length > konamiSequence.length) {
        konamiCode.shift();
    }
    
    if (konamiCode.join(',') === konamiSequence.join(',')) {
        // Enhanced easter egg
        document.body.style.filter = 'hue-rotate(180deg) saturate(2)';
        document.body.style.animation = 'rainbow 3s linear';
        
        // Add confetti effect
        for (let i = 0; i < 50; i++) {
            const confetti = document.createElement('div');
            confetti.style.cssText = `
                position: fixed;
                width: 10px;
                height: 10px;
                background: hsl(${Math.random() * 360}, 70%, 60%);
                left: ${Math.random() * 100}%;
                top: -10px;
                z-index: 10000;
                animation: confettiFall 3s linear forwards;
            `;
            document.body.appendChild(confetti);
            
            setTimeout(() => confetti.remove(), 3000);
        }
        
        setTimeout(() => {
            document.body.style.filter = 'none';
            document.body.style.animation = 'none';
        }, 3000);
        
        konamiCode = [];
    }
});    // Keyboard navigation
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchInput = document.getElementById('commandSearch');
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
        
        // Escape to clear search
        if (e.key === 'Escape') {
            const searchInput = document.getElementById('commandSearch');
            if (searchInput && searchInput === document.activeElement) {
                searchInput.value = '';
                searchInput.blur();
                performSearch();
            }
        }
    });
    
    // Add keyboard shortcut hint to search
    const searchInput = document.getElementById('commandSearch');
    if (searchInput) {
        searchInput.placeholder = 'Søg efter kommandoer... (Ctrl+K)';
    }
