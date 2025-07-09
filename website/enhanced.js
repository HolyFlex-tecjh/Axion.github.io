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
    
    // Interactive demo chat
    const chatMessages = document.querySelector('.chat-messages');
    const chatInput = document.querySelector('.chat-input input');
    const chatSend = document.querySelector('.chat-send');
    
    const demoMessages = [
        { type: 'user', author: 'Admin', text: '!moderation setup' },
        { type: 'bot', author: 'Axion Bot', text: '🛡️ Moderation system er nu konfigureret! Auto-moderation er aktiveret med spam beskyttelse og toxic chat filter.' },
        { type: 'user', author: 'Member', text: '!play Imagine Dragons' },
        { type: 'bot', author: 'Axion Bot', text: '🎵 Nu afspiller: **Believer - Imagine Dragons** | Tilføjet til køen af Member' },
        { type: 'user', author: 'Admin', text: '!stats server' },
        { type: 'bot', author: 'Axion Bot', text: '📊 **Server Statistikker**\n👥 Medlemmer: 1,247\n💬 Beskeder i dag: 3,891\n🎵 Sange afspillet: 156' }
    ];
    
    let messageIndex = 0;
    let isTyping = false;
    
    function addMessage(message, delay = 0) {
        setTimeout(() => {
            const messageEl = document.createElement('div');
            messageEl.className = `chat-message ${message.type}-message`;
            messageEl.innerHTML = `
                <div class="message-avatar">
                    <i class="fas ${message.type === 'bot' ? 'fa-robot' : 'fa-user'}"></i>
                </div>
                <div class="message-content">
                    <div class="message-author">${message.author}</div>
                    <div class="message-text">${message.text}</div>
                </div>
            `;
            
            chatMessages.appendChild(messageEl);
            chatMessages.scrollTop = chatMessages.scrollHeight;
            
            // Animate message appearance
            setTimeout(() => {
                messageEl.style.opacity = '1';
                messageEl.style.transform = 'translateY(0)';
            }, 100);
        }, delay);
    }
    
    function startDemoChat() {
        if (messageIndex < demoMessages.length) {
            addMessage(demoMessages[messageIndex], messageIndex * 2000);
            messageIndex++;
            setTimeout(startDemoChat, 2000);
        } else {
            // Reset demo after all messages
            setTimeout(() => {
                chatMessages.innerHTML = '';
                messageIndex = 0;
                startDemoChat();
            }, 5000);
        }
    }
    
    // Start demo chat when section is visible
    const demoSection = document.querySelector('.interactive-demo');
    if (demoSection) {
        const demoObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && messageIndex === 0) {
                    setTimeout(startDemoChat, 1000);
                }
            });
        }, { threshold: 0.5 });
        
        demoObserver.observe(demoSection);
    }
    
    // Interactive chat input
    if (chatInput && chatSend) {
        function sendMessage() {
            const text = chatInput.value.trim();
            if (text && !isTyping) {
                isTyping = true;
                
                // Add user message
                addMessage({
                    type: 'user',
                    author: 'Du',
                    text: text
                });
                
                // Add bot response after delay
                setTimeout(() => {
                    const responses = [
                        '🤖 Jeg er en demo! Prøv kommandoer som !help, !play eller !stats',
                        '✨ Det var en fed kommando! Axion Bot kan meget mere end dette.',
                        '🎵 Musik, moderation, og meget mere - alt sammen i én bot!',
                        '📊 Axion Bot gør det nemt at administrere din Discord server.',
                        '🛡️ Sikkerhed og moderering på højeste niveau med Axion Bot.'
                    ];
                    const randomResponse = responses[Math.floor(Math.random() * responses.length)];
                    
                    addMessage({
                        type: 'bot',
                        author: 'Axion Bot',
                        text: randomResponse
                    });
                    
                    isTyping = false;
                }, 1500);
                
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
    
    // Parallax effect for hero section
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        const hero = document.querySelector('.hero');
        const heroContent = document.querySelector('.hero-content');
        const heroImage = document.querySelector('.hero-image');
        
        if (hero && scrolled < hero.offsetHeight) {
            if (heroContent) {
                heroContent.style.transform = `translateY(${scrolled * 0.2}px)`;
            }
            if (heroImage) {
                heroImage.style.transform = `translateY(${scrolled * 0.1}px)`;
            }
        }
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
    
    // Mobile menu toggle
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    
    if (hamburger && navMenu) {
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
        });
        
        // Close menu when clicking on links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
            });
        });
    }
    
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
