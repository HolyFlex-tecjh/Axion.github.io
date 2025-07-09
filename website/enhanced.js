// Enhanced Axion Bot Website JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
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
        setTimeout(hideLoader, 1000); // Small delay for visual effect
    } else {
        window.addEventListener('load', () => {
            setTimeout(hideLoader, 1000);
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
    
    // Intersection Observer for animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate');
                
                // Animate statistics when they come into view
                if (entry.target.classList.contains('stat-number')) {
                    const target = parseInt(entry.target.getAttribute('data-target'));
                    animateCounter(entry.target, target);
                }
                
                // Animate live numbers
                if (entry.target.classList.contains('live-number')) {
                    const target = parseInt(entry.target.getAttribute('data-target'));
                    animateCounter(entry.target, target);
                }
            }
        });
    }, observerOptions);
    
    // Observe all animated elements
    document.querySelectorAll('.stat-number, .live-number, .feature-card, .stat-card, .testimonial-card').forEach(el => {
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
    
    // Add dynamic particles
    function createParticle() {
        const particle = document.createElement('div');
        particle.className = 'floating-particle';
        particle.style.cssText = `
            position: fixed;
            width: 4px;
            height: 4px;
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            border-radius: 50%;
            pointer-events: none;
            z-index: 1;
            left: ${Math.random() * 100}vw;
            top: 100vh;
            opacity: 0.7;
            animation: particle-float ${15 + Math.random() * 10}s linear infinite;
        `;
        
        document.body.appendChild(particle);
        
        // Remove particle after animation
        setTimeout(() => {
            if (particle.parentNode) {
                particle.parentNode.removeChild(particle);
            }
        }, 25000);
    }
    
    // Create particles periodically
    setInterval(createParticle, 3000);
    
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
    `;
    document.head.appendChild(style);
});
