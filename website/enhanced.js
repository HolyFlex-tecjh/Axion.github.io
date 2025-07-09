// Enhanced Axion Bot Website JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // Performance monitoring
    const performance = {
        measurePageLoad: function() {
            if (window.performance && window.performance.timing) {
                const timing = window.performance.timing;
                const loadTime = timing.loadEventEnd - timing.navigationStart;
                console.log('Page Load Time:', loadTime + 'ms');
            }
        },
        
        // Lazy loading for images
        lazyLoadImages: function() {
            const images = document.querySelectorAll('img[data-src]');
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src;
                        img.classList.remove('lazy');
                        imageObserver.unobserve(img);
                    }
                });
            });
            
            images.forEach(img => imageObserver.observe(img));
        },
        
        // Optimize animations for better performance
        reduceMotionForAccessibility: function() {
            const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
            if (prefersReducedMotion) {
                document.documentElement.style.setProperty('--transition-fast', 'none');
                document.documentElement.style.setProperty('--transition-normal', 'none');
                document.documentElement.style.setProperty('--transition-slow', 'none');
            }
        }
    };
    
    // Initialize performance optimizations
    performance.measurePageLoad();
    performance.lazyLoadImages();
    performance.reduceMotionForAccessibility();
    
    // Accessibility improvements
    const accessibility = {
        // Keyboard navigation for interactive elements
        enhanceKeyboardNavigation: function() {
            const focusableElements = document.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
            
            focusableElements.forEach(element => {
                element.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter' || e.key === ' ') {
                        if (element.tagName === 'BUTTON' || element.getAttribute('role') === 'button') {
                            e.preventDefault();
                            element.click();
                        }
                    }
                });
            });
        },
        
        // Enhanced screen reader support
        addAriaLabels: function() {
            // Add aria-labels to interactive elements without text
            const buttons = document.querySelectorAll('button:not([aria-label])');
            buttons.forEach(button => {
                if (!button.textContent.trim()) {
                    const icon = button.querySelector('i');
                    if (icon) {
                        const iconClass = icon.className;
                        if (iconClass.includes('menu')) {
                            button.setAttribute('aria-label', 'Open menu');
                        } else if (iconClass.includes('search')) {
                            button.setAttribute('aria-label', 'Search');
                        } else if (iconClass.includes('close')) {
                            button.setAttribute('aria-label', 'Close');
                        }
                    }
                }
            });
        },
        
        // Skip link for keyboard users
        addSkipLink: function() {
            const skipLink = document.createElement('a');
            skipLink.href = '#main-content';
            skipLink.textContent = 'Skip to main content';
            skipLink.className = 'skip-link';
            skipLink.setAttribute('aria-label', 'Skip to main content');
            document.body.insertBefore(skipLink, document.body.firstChild);
        }
    };
    
    // Initialize accessibility features
    accessibility.enhanceKeyboardNavigation();
    accessibility.addAriaLabels();
    accessibility.addSkipLink();
    
    // Hide loading overlay when page is ready
    const loadingOverlay = document.querySelector('.loading-overlay');
    
    function hideLoader() {
        if (loadingOverlay) {
            loadingOverlay.style.opacity = '0';
            setTimeout(() => {
                loadingOverlay.style.display = 'none';
                document.body.classList.add('loaded');
            }, 500);
        }
    }
    
    // Wait for all critical resources to load
    if (document.readyState === 'complete') {
        setTimeout(hideLoader, 1000);
    } else {
        window.addEventListener('load', () => {
            setTimeout(hideLoader, 1000);
        });
    }
    
    // Enhanced mobile navigation
    const hamburger = document.getElementById('hamburger');
    const navMenu = document.querySelector('.nav-menu');
    
    if (hamburger && navMenu) {
        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
            
            // Prevent body scroll when menu is open
            document.body.style.overflow = navMenu.classList.contains('active') ? 'hidden' : '';
            
            // Update aria attributes
            const isExpanded = navMenu.classList.contains('active');
            hamburger.setAttribute('aria-expanded', isExpanded);
            navMenu.setAttribute('aria-hidden', !isExpanded);
        });
        
        // Close menu when clicking outside
        document.addEventListener('click', function(e) {
            if (!hamburger.contains(e.target) && !navMenu.contains(e.target)) {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
        
        // Close menu on escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && navMenu.classList.contains('active')) {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
                document.body.style.overflow = '';
                hamburger.focus();
            }
        });
    }
    
    // Navbar scroll effect
    const navbar = document.querySelector('.navbar');
    let lastScrollY = window.scrollY;
    
    window.addEventListener('scroll', () => {
        if (window.scrollY > 100) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
        
        // Hide/show navbar on scroll
        if (window.scrollY > lastScrollY && window.scrollY > 100) {
            navbar.style.transform = 'translateY(-100%)';
        } else {
            navbar.style.transform = 'translateY(0)';
        }
        lastScrollY = window.scrollY;
    });
    
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
            }
        });
    });
    
    // Animated counters for statistics
    function animateCounter(element, target, duration = 2000) {
        let start = 0;
        const increment = target / (duration / 16);
        
        function updateCounter() {
            start += increment;
            if (start < target) {
                element.textContent = Math.floor(start).toLocaleString();
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = target.toLocaleString();
            }
        }
        updateCounter();
    }
    
    // Enhanced particle system
    function createParticles() {
        const particleContainer = document.querySelector('.hero-particles');
        if (!particleContainer) return;
        
        function createParticle() {
            const particle = document.createElement('div');
            particle.className = 'particle';
            particle.style.cssText = `
                position: absolute;
                width: ${Math.random() * 4 + 2}px;
                height: ${Math.random() * 4 + 2}px;
                background: radial-gradient(circle, rgba(88, 101, 242, 0.8) 0%, rgba(88, 101, 242, 0.2) 100%);
                border-radius: 50%;
                left: ${Math.random() * 100}%;
                top: ${Math.random() * 100}%;
                animation: float ${Math.random() * 3 + 2}s ease-in-out infinite;
                opacity: ${Math.random() * 0.5 + 0.2};
                pointer-events: none;
            `;
            
            particleContainer.appendChild(particle);
            
            // Remove particle after animation
            setTimeout(() => {
                if (particle.parentNode) {
                    particle.parentNode.removeChild(particle);
                }
            }, 5000);
        }
        
        // Create initial particles
        for (let i = 0; i < 20; i++) {
            setTimeout(createParticle, i * 100);
        }
        
        // Continue creating particles
        setInterval(createParticle, 300);
    }
    
    // Initialize particle system
    createParticles();
    
    // Enhanced scroll effects
    function updateScrollEffects() {
        const scrolled = window.scrollY;
        const rate = scrolled * -0.5;
        
        // Parallax effect for hero particles
        const heroParticles = document.querySelector('.hero-particles');
        if (heroParticles) {
            heroParticles.style.transform = `translateY(${rate}px)`;
        }
        
        // Navbar effects
        const navbar = document.querySelector('.navbar');
        if (navbar) {
            if (scrolled > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        }
        
        // Back to top button
        const backToTop = document.querySelector('.back-to-top');
        if (backToTop) {
            if (scrolled > 500) {
                backToTop.classList.add('visible');
            } else {
                backToTop.classList.remove('visible');
            }
        }
    }
    
    // Throttled scroll listener
    let ticking = false;
    function handleScroll() {
        if (!ticking) {
            requestAnimationFrame(updateScrollEffects);
            ticking = true;
            setTimeout(() => ticking = false, 10);
        }
    }
    
    window.addEventListener('scroll', handleScroll);
    
    // Intersection Observer for animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
                
                // Stagger animation for feature cards
                if (entry.target.classList.contains('feature-card')) {
                    const cards = document.querySelectorAll('.feature-card');
                    cards.forEach((card, index) => {
                        setTimeout(() => {
                            card.style.animation = `fadeInUp 0.6s ease-out ${index * 0.1}s both`;
                        }, index * 100);
                    });
                }
            }
        });
    }, observerOptions);
    
    // Observe elements for animation
    document.querySelectorAll('.feature-card, .stat-card, .testimonial-card').forEach(el => {
        observer.observe(el);
    });
    
    // Demo Chat Functionality
    const chatInput = document.querySelector('.chat-input input');
    const chatSend = document.querySelector('.chat-send');
    const chatMessages = document.querySelector('.chat-messages');
    const demoCmdButtons = document.querySelectorAll('.demo-cmd-btn');
    
    // Demo responses
    const demoResponses = {
        '/help': {
            type: 'embed',
            title: 'Axion Bot - Kommando Hjælp',
            description: 'Her er en oversigt over tilgængelige kommandoer:',
            fields: [
                { name: 'Grundlæggende', value: '`/ping` - Test bot respons\n`/info` - Bot information\n`/help` - Vis denne hjælp' },
                { name: 'Moderation', value: '`/ban` - Ban en bruger\n`/kick` - Kick en bruger\n`/warn` - Advar en bruger' },
                { name: 'Statistikker', value: '`/modstats` - Moderation statistikker\n`/serverstats` - Server statistikker' }
            ]
        },
        '/ping': {
            type: 'simple',
            text: '🏓 Pong! Latency: **23ms** | API Latency: **156ms**'
        },
        '/info': {
            type: 'embed',
            title: 'Axion Bot Information',
            description: 'Den ultimative Discord bot til moderation og server management',
            fields: [
                { name: 'Version', value: '2.1.0' },
                { name: 'Uptime', value: '15 dage, 7 timer' },
                { name: 'Servere', value: '50,247' },
                { name: 'Brugere', value: '2,145,891' }
            ]
        },
        '/modstats': {
            type: 'embed',
            title: 'Moderation Statistikker',
            description: 'Statistikker for denne server:',
            fields: [
                { name: 'Advarsler i dag', value: '12' },
                { name: 'Automatiske handlinger', value: '45' },
                { name: 'Spam beskeder blokeret', value: '127' },
                { name: 'Moderation niveau', value: 'Standard' }
            ]
        },
        '/warn': {
            type: 'embed',
            title: 'Bruger Advaret',
            description: 'Advarsel givet til bruger',
            fields: [
                { name: 'Bruger', value: '@user' },
                { name: 'Årsag', value: 'Spam beskeder' },
                { name: 'Moderator', value: 'Demo User' },
                { name: 'Totale advarsler', value: '1' }
            ]
        }
    };
    
    function addMessage(content, isBot = false, isEmbed = false) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `demo-message ${isBot ? 'bot' : 'user'}`;
        
        if (isEmbed && isBot) {
            messageDiv.innerHTML = `
                <div class="message-avatar bot-avatar">
                    <i class="fas fa-robot"></i>
                </div>
                <div class="message-content">
                    <span class="message-author">Axion Bot</span>
                    <span class="bot-tag">BOT</span>
                    <div class="message-embed">
                        <div class="embed-header">
                            <i class="fas fa-info-circle"></i>
                            <span>${content.title}</span>
                        </div>
                        <div class="embed-description">${content.description}</div>
                        ${content.fields ? content.fields.map(field => `
                            <div class="embed-field">
                                <div class="field-name">${field.name}</div>
                                <div class="field-value">${field.value}</div>
                            </div>
                        `).join('') : ''}
                    </div>
                </div>
            `;
        } else {
            messageDiv.innerHTML = `
                <div class="message-avatar ${isBot ? 'bot-avatar' : ''}">
                    <i class="fas fa-${isBot ? 'robot' : 'user'}"></i>
                </div>
                <div class="message-content">
                    <span class="message-author">${isBot ? 'Axion Bot' : 'Demo User'}</span>
                    ${isBot ? '<span class="bot-tag">BOT</span>' : ''}
                    <div class="message-text">${content}</div>
                </div>
            `;
        }
        
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    function handleDemoCommand(command) {
        // Add user message
        addMessage(command);
        
        // Simulate typing delay
        setTimeout(() => {
            const response = demoResponses[command];
            if (response) {
                if (response.type === 'embed') {
                    addMessage(response, true, true);
                } else {
                    addMessage(response.text, true);
                }
            } else {
                addMessage('❌ Ukendt kommando. Prøv `/help` for at se tilgængelige kommandoer.', true);
            }
        }, 500);
    }
    
    // Demo command buttons
    demoCmdButtons.forEach(button => {
        button.addEventListener('click', () => {
            const command = button.dataset.command;
            handleDemoCommand(command);
        });
    });
    
    // Chat input handling
    if (chatInput && chatSend) {
        function sendMessage() {
            const message = chatInput.value.trim();
            if (message) {
                handleDemoCommand(message);
                chatInput.value = '';
            }
        }
        
        chatSend.addEventListener('click', sendMessage);
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }
    
    // Back to top button
    const backToTopBtn = document.getElementById('backToTop');
    if (backToTopBtn) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 500) {
                backToTopBtn.classList.add('visible');
            } else {
                backToTopBtn.classList.remove('visible');
            }
        });
        
        backToTopBtn.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }
    
    // Hamburger menu functionality (moved to avoid redeclaration)
    const hamburgerMenu = document.querySelector('.hamburger');
    const navigationMenu = document.querySelector('.nav-menu');
    
    if (hamburgerMenu && navigationMenu) {
        hamburgerMenu.addEventListener('click', () => {
            hamburgerMenu.classList.toggle('active');
            navigationMenu.classList.toggle('active');
        });
        
        // Close menu when clicking on nav links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                hamburgerMenu.classList.remove('active');
                navigationMenu.classList.remove('active');
            });
        });
    }
    
    // Add loading state to buttons
    document.querySelectorAll('.btn').forEach(button => {
        button.addEventListener('click', function(e) {
            if (this.href && this.href.includes('discord.com')) {
                this.classList.add('loading');
                this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Åbner Discord...';
                
                setTimeout(() => {
                    this.classList.remove('loading');
                    this.innerHTML = '<i class="fab fa-discord"></i> Tilføj til Discord';
                }, 3000);
            }
        });
    });
    
    // Add loading animation
    window.addEventListener('load', () => {
        document.body.classList.add('loaded');
    });
    
    // Enhanced button hover effects
    document.querySelectorAll('.btn').forEach(btn => {
        btn.addEventListener('mouseenter', (e) => {
            const rect = btn.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const ripple = document.createElement('span');
            ripple.className = 'btn-ripple';
            ripple.style.cssText = `
                position: absolute;
                border-radius: 50%;
                background: rgba(255, 255, 255, 0.3);
                transform: scale(0);
                animation: ripple 0.6s linear;
                left: ${x}px;
                top: ${y}px;
                width: 0;
                height: 0;
            `;
            
            btn.appendChild(ripple);
            
            setTimeout(() => {
                if (ripple.parentNode) {
                    ripple.parentNode.removeChild(ripple);
                }
            }, 600);
        });
    });
    
    // Add ripple animation CSS
    const style = document.createElement('style');
    style.textContent = `
        @keyframes ripple {
            to {
                transform: scale(4);
                opacity: 0;
                width: 20px;
                height: 20px;
                margin-left: -10px;
                margin-top: -10px;
            }
        }
        
        .btn {
            position: relative;
            overflow: hidden;
        }
        
        .animate-in {
            animation: fadeInUp 0.6s ease-out both;
        }
        
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    `;
    document.head.appendChild(style);
    
    // Enhanced loading sequence
    function enhancedLoadingSequence() {
        const loadingOverlay = document.querySelector('.loading-overlay');
        const loadingText = document.querySelector('.loading-text');
        
        if (loadingText) {
            const messages = [
                'Indlæser Axion Bot...',
                'Forbereder funktioner...',
                'Næsten klar...'
            ];
            
            let messageIndex = 0;
            const messageInterval = setInterval(() => {
                messageIndex = (messageIndex + 1) % messages.length;
                loadingText.textContent = messages[messageIndex];
            }, 1000);
            
            setTimeout(() => {
                clearInterval(messageInterval);
                if (loadingOverlay) {
                    loadingOverlay.style.opacity = '0';
                    setTimeout(() => {
                        loadingOverlay.style.display = 'none';
                        document.body.classList.add('loaded');
                    }, 500);
                }
            }, 2000);
        }
    }
    
    // Start enhanced loading sequence
    if (document.readyState === 'complete') {
        enhancedLoadingSequence();
    } else {
        window.addEventListener('load', enhancedLoadingSequence);
    }
    
    // Enhanced button interactions
    document.querySelectorAll('.btn').forEach(button => {
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
        
        button.addEventListener('click', function(e) {
            // Ripple effect
            const ripple = document.createElement('div');
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
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
});
