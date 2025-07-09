// Login functionality
document.addEventListener('DOMContentLoaded', function() {
    initLoginPage();
});

function initLoginPage() {
    const discordLoginBtn = document.getElementById('discordLogin');
    const demoLoginBtn = document.getElementById('demoLogin');
    const loginLoading = document.getElementById('loginLoading');
    
    // Discord login
    if (discordLoginBtn) {
        discordLoginBtn.addEventListener('click', function() {
            handleDiscordLogin();
        });
    }
    
    // Demo login
    if (demoLoginBtn) {
        demoLoginBtn.addEventListener('click', function() {
            handleDemoLogin();
        });
    }
    
    // Check if user is already logged in
    checkAuthStatus();
}

function handleDiscordLogin() {
    const loginLoading = document.getElementById('loginLoading');
    
    // Show loading
    showLoading('Omdirigerer til Discord...');
    
    // Simulate Discord OAuth flow
    setTimeout(() => {
        // In a real implementation, this would redirect to Discord OAuth
        // For demo purposes, we'll simulate a successful login
        const discordAuthUrl = `https://discord.com/api/oauth2/authorize?client_id=YOUR_BOT_ID&redirect_uri=${encodeURIComponent(window.location.origin + '/auth/callback')}&response_type=code&scope=identify%20guilds`;
        
        // For demo, we'll just simulate the auth process
        simulateDiscordAuth();
    }, 1000);
}

function simulateDiscordAuth() {
    showLoading('Henter bruger information...');
    
    setTimeout(() => {
        showLoading('Indlæser servere...');
        
        setTimeout(() => {
            // Create fake user data
            const userData = {
                id: '123456789012345678',
                username: 'DiscordUser',
                discriminator: '1234',
                avatar: 'https://cdn.discordapp.com/embed/avatars/0.png',
                guilds: generateFakeGuilds()
            };
            
            // Store user data
            localStorage.setItem('axion-user', JSON.stringify(userData));
            localStorage.setItem('axion-auth-token', 'fake-jwt-token-' + Date.now());
            localStorage.setItem('axion-login-time', Date.now().toString());
            
            showLoading('Login succesfuldt! Omdirigerer...');
            
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1500);
            
        }, 1500);
    }, 1000);
}

function handleDemoLogin() {
    showLoading('Indlæser demo dashboard...');
    
    setTimeout(() => {
        // Create demo user data
        const demoData = {
            id: 'demo-user',
            username: 'Demo User',
            discriminator: '0000',
            avatar: 'https://cdn.discordapp.com/embed/avatars/0.png',
            guilds: generateDemoGuilds(),
            isDemo: true
        };
        
        // Store demo data
        localStorage.setItem('axion-user', JSON.stringify(demoData));
        localStorage.setItem('axion-auth-token', 'demo-token');
        localStorage.setItem('axion-login-time', Date.now().toString());
        
        showLoading('Demo klar! Omdirigerer...');
        
        setTimeout(() => {
            window.location.href = 'dashboard.html';
        }, 1000);
        
    }, 1500);
}

function showLoading(message) {
    const loginLoading = document.getElementById('loginLoading');
    const loadingContent = loginLoading.querySelector('.loading-content');
    
    if (loadingContent) {
        const messageEl = loadingContent.querySelector('p');
        if (messageEl) {
            messageEl.textContent = message;
        }
    }
    
    loginLoading.classList.add('show');
}

function hideLoading() {
    const loginLoading = document.getElementById('loginLoading');
    loginLoading.classList.remove('show');
}

function checkAuthStatus() {
    const token = localStorage.getItem('axion-auth-token');
    const user = localStorage.getItem('axion-user');
    
    if (token && user) {
        // User is already logged in, redirect to dashboard
        showLoading('Du er allerede logget ind. Omdirigerer...');
        setTimeout(() => {
            window.location.href = 'dashboard.html';
        }, 1000);
    }
}

function generateFakeGuilds() {
    return [
        {
            id: '123456789012345678',
            name: 'Awesome Gaming Server',
            icon: 'https://cdn.discordapp.com/embed/avatars/1.png',
            owner: true,
            permissions: '8',
            memberCount: 1247,
            botPresent: true
        },
        {
            id: '123456789012345679',
            name: 'Chill Lounge',
            icon: 'https://cdn.discordapp.com/embed/avatars/2.png',
            owner: false,
            permissions: '268435456',
            memberCount: 89,
            botPresent: true
        },
        {
            id: '123456789012345680',
            name: 'Study Group',
            icon: 'https://cdn.discordapp.com/embed/avatars/3.png',
            owner: false,
            permissions: '8',
            memberCount: 156,
            botPresent: false
        },
        {
            id: '123456789012345681',
            name: 'Meme Central',
            icon: 'https://cdn.discordapp.com/embed/avatars/4.png',
            owner: true,
            permissions: '8',
            memberCount: 892,
            botPresent: true
        }
    ];
}

function generateDemoGuilds() {
    return [
        {
            id: 'demo-guild-1',
            name: 'Demo Server',
            icon: 'https://cdn.discordapp.com/embed/avatars/0.png',
            owner: true,
            permissions: '8',
            memberCount: 500,
            botPresent: true
        }
    ];
}

// Auth utilities for other pages
function isLoggedIn() {
    const token = localStorage.getItem('axion-auth-token');
    const user = localStorage.getItem('axion-user');
    return !!(token && user);
}

function getCurrentUser() {
    const userStr = localStorage.getItem('axion-user');
    return userStr ? JSON.parse(userStr) : null;
}

function logout() {
    localStorage.removeItem('axion-auth-token');
    localStorage.removeItem('axion-user');
    localStorage.removeItem('axion-login-time');
    window.location.href = 'index.html';
}

function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Add enhanced button effects
document.addEventListener('DOMContentLoaded', function() {
    // Add ripple effect to buttons
    document.querySelectorAll('.discord-login-btn, .demo-login-btn').forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple-effect');
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
    
    // Add particle effects to login form
    createLoginParticles();
});

function createLoginParticles() {
    const particleContainer = document.querySelector('.login-particles');
    if (!particleContainer) return;
    
    for (let i = 0; i < 20; i++) {
        const particle = document.createElement('div');
        particle.classList.add('floating-particle');
        particle.style.cssText = `
            position: absolute;
            width: ${Math.random() * 4 + 2}px;
            height: ${Math.random() * 4 + 2}px;
            background: rgba(88, 101, 242, ${Math.random() * 0.6 + 0.2});
            border-radius: 50%;
            left: ${Math.random() * 100}%;
            top: ${Math.random() * 100}%;
            animation: floatParticle ${Math.random() * 20 + 10}s linear infinite;
            animation-delay: ${Math.random() * 5}s;
        `;
        particleContainer.appendChild(particle);
    }
}

// Add floating particle animation CSS
const particleStyles = document.createElement('style');
particleStyles.textContent = `
    @keyframes floatParticle {
        0% {
            transform: translateY(100vh) rotate(0deg);
            opacity: 0;
        }
        10% {
            opacity: 1;
        }
        90% {
            opacity: 1;
        }
        100% {
            transform: translateY(-100px) rotate(360deg);
            opacity: 0;
        }
    }
    
    .ripple-effect {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        pointer-events: none;
        transform: scale(0);
        animation: rippleAnimation 0.6s linear;
    }
    
    @keyframes rippleAnimation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
`;
document.head.appendChild(particleStyles);
