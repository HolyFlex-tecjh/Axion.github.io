// Dashboard functionality with authentication
document.addEventListener('DOMContentLoaded', function() {
    initDashboard();
});

let currentUser = null;
let currentServer = null;

function initDashboard() {
    // Check authentication first
    if (!checkAuthentication()) {
        showAuthRequired();
        return;
    }
    
    // Show loading screen
    showLoading();
    
    // Load user data
    currentUser = getCurrentUser();
    
    // Initialize dashboard components
    setTimeout(() => {
        setupUserInterface();
        setupServerSelector();
        setupEventListeners();
        initMobileMenu();
        initParticleBackground();
        loadInitialData();
        
        // Hide loading screen
        hideLoading();
        
        // Initialize enhanced features
        initializeCharts();
        setupRealTimeUpdates();
        setupFormHandlers();
        setupAnimations();
    }, 1500);
}

function checkAuthentication() {
    const token = localStorage.getItem('axion-auth-token');
    const user = localStorage.getItem('axion-user');
    return !!(token && user);
}

function getCurrentUser() {
    const userStr = localStorage.getItem('axion-user');
    return userStr ? JSON.parse(userStr) : null;
}

function showAuthRequired() {
    const authRequired = document.getElementById('authRequired');
    const dashboardContainer = document.querySelector('.dashboard-container');
    
    if (authRequired) {
        authRequired.classList.add('show');
    }
    
    if (dashboardContainer) {
        dashboardContainer.style.display = 'none';
    }
}

function showLoading() {
    const loading = document.getElementById('dashboardLoading');
    if (loading) {
        loading.classList.add('show');
    }
}

function hideLoading() {
    const loading = document.getElementById('dashboardLoading');
    if (loading) {
        loading.classList.remove('show');
    }
}

function setupUserInterface() {
    if (!currentUser) return;
    
    // Update user info in sidebar
    const userAvatar = document.getElementById('dashboardUserAvatar');
    const userName = document.getElementById('dashboardUserName');
    const userId = document.getElementById('dashboardUserId');
    
    if (userAvatar && currentUser.avatar) {
        userAvatar.src = currentUser.avatar;
    }
    
    if (userName) {
        userName.textContent = currentUser.username;
    }
    
    if (userId) {
        userId.textContent = `#${currentUser.discriminator}`;
    }
    
    // Show demo badge if demo user
    if (currentUser.isDemo) {
        addDemoBadge();
    }
}

function addDemoBadge() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        const demoBadge = document.createElement('div');
        demoBadge.className = 'server-info-badge';
        demoBadge.innerHTML = `
            <i class="fas fa-eye"></i>
            <span>Demo Mode</span>
        `;
        sidebar.appendChild(demoBadge);
    }
}

function setupServerSelector() {
    if (!currentUser || !currentUser.guilds) return;
    
    const serverSelect = document.getElementById('serverSelect');
    if (!serverSelect) return;
    
    // Clear existing options
    serverSelect.innerHTML = '';
    
    // Filter servers where user has admin permissions or bot is present
    const availableServers = currentUser.guilds.filter(guild => {
        return guild.botPresent && (guild.owner || guild.permissions === '8');
    });
    
    if (availableServers.length === 0) {
        serverSelect.innerHTML = '<option value="">Ingen tilgængelige servere</option>';
        showNoBotServers();
        return;
    }
    
    // Add servers to dropdown
    availableServers.forEach(guild => {
        const option = document.createElement('option');
        option.value = guild.id;
        option.textContent = `${guild.name} (${guild.memberCount} medlemmer)`;
        serverSelect.appendChild(option);
    });
    
    // Select first server by default
    if (availableServers.length > 0) {
        currentServer = availableServers[0];
        serverSelect.value = currentServer.id;
        loadServerData(currentServer);
    }
    
    // Add change event listener
    serverSelect.addEventListener('change', function() {
        const selectedGuild = availableServers.find(g => g.id === this.value);
        if (selectedGuild) {
            currentServer = selectedGuild;
            loadServerData(selectedGuild);
        }
    });
}

function showNoBotServers() {
    const mainContent = document.querySelector('.dashboard-main');
    if (mainContent) {
        mainContent.innerHTML = `
            <div class="no-servers-message">
                <div class="no-servers-content">
                    <i class="fas fa-robot"></i>
                    <h2>Ingen Servere Fundet</h2>
                    <p>Axion Bot er ikke tilføjet til nogen af dine servere, eller du har ikke administrator rettigheder.</p>
                    <a href="index.html#invite" class="btn btn-primary">
                        <i class="fas fa-plus"></i>
                        Tilføj Bot til Server
                    </a>
                </div>
            </div>
        `;
    }
}

function loadServerData(server) {
    if (!server) return;
    
    // Update dashboard title
    const dashboardTitle = document.querySelector('.dashboard-header h1');
    if (dashboardTitle) {
        dashboardTitle.textContent = `${server.name} Dashboard`;
    }
    
    // Generate fake data for demo
    const serverData = generateServerData(server);
    
    // Update statistics
    updateStatistics(serverData.stats);
    
    // Update charts if they exist
    if (typeof updateCharts === 'function') {
        updateCharts(serverData.charts);
    }
    
    // Update recent activity
    updateRecentActivity(serverData.activity);
    
    // Update quick actions
    updateQuickActions(server);
}

function generateServerData(server) {
    const isDemo = currentUser.isDemo;
    
    return {
        stats: {
            totalMembers: server.memberCount,
            onlineMembers: Math.floor(server.memberCount * 0.3),
            botCommands: isDemo ? 1247 : Math.floor(Math.random() * 2000) + 500,
            messagesPerDay: isDemo ? 523 : Math.floor(Math.random() * 1000) + 200
        },
        charts: {
            memberActivity: generateMemberActivityData(),
            commandUsage: generateCommandUsageData(),
            messageFrequency: generateMessageFrequencyData()
        },
        activity: generateRecentActivity(server)
    };
}

function generateMemberActivityData() {
    const data = [];
    for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        data.push({
            date: date.toLocaleDateString('da-DK', { weekday: 'short' }),
            members: Math.floor(Math.random() * 100) + 50
        });
    }
    return data;
}

function generateCommandUsageData() {
    return [
        { command: 'play', usage: Math.floor(Math.random() * 100) + 50 },
        { command: 'ban', usage: Math.floor(Math.random() * 20) + 5 },
        { command: 'kick', usage: Math.floor(Math.random() * 15) + 3 },
        { command: 'meme', usage: Math.floor(Math.random() * 80) + 20 },
        { command: 'serverinfo', usage: Math.floor(Math.random() * 30) + 10 }
    ];
}

function generateMessageFrequencyData() {
    const data = [];
    for (let i = 23; i >= 0; i--) {
        data.push({
            hour: i,
            messages: Math.floor(Math.random() * 50) + 10
        });
    }
    return data;
}

function generateRecentActivity(server) {
    const activities = [
        `Bruger blev kicked af <@Moderator>`,
        `Ny bruger joinet serveren`,
        `Musik kommando brugt i #music`,
        `Auto-moderation fjernede spam besked`,
        `Rolle tildelt til ny bruger`,
        `Backup blev oprettet automatisk`
    ];
    
    return activities.slice(0, 5).map((activity, index) => ({
        id: index,
        text: activity,
        time: `${Math.floor(Math.random() * 60) + 1} min siden`,
        type: ['kick', 'join', 'music', 'automod', 'role', 'backup'][index]
    }));
}

function setupEventListeners() {
    // Logout button
    const logoutBtn = document.getElementById('dashboardLogout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
    
    // Menu items
    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all items
            document.querySelectorAll('.menu-item').forEach(i => i.classList.remove('active'));
            
            // Add active class to clicked item
            this.classList.add('active');
            
            // Load corresponding content
            const section = this.getAttribute('href').substring(1);
            loadSection(section);
        });
    });
}

function handleLogout() {
    if (confirm('Er du sikker på at du vil logge ud?')) {
        localStorage.removeItem('axion-auth-token');
        localStorage.removeItem('axion-user');
        localStorage.removeItem('axion-login-time');
        
        window.location.href = 'index.html';
    }
}

function loadSection(section) {
    const mainContent = document.querySelector('.dashboard-main');
    if (!mainContent) return;
    
    // This would load different dashboard sections
    // For now, we'll just show a placeholder
    mainContent.innerHTML = `
        <div class="section-placeholder">
            <h2>${section.charAt(0).toUpperCase() + section.slice(1)} Sektion</h2>
            <p>Denne sektion er under udvikling.</p>
        </div>
    `;
}

function updateStatistics(stats) {
    // Update stat cards with animation
    const statElements = {
        'total-members': stats.totalMembers,
        'online-members': stats.onlineMembers,
        'bot-commands': stats.botCommands,
        'messages-day': stats.messagesPerDay
    };
    
    Object.entries(statElements).forEach(([id, value]) => {
        const element = document.getElementById(id);
        if (element) {
            animateNumber(element, 0, value, 1500);
        }
    });
}

function animateNumber(element, start, end, duration) {
    const startTime = Date.now();
    const difference = end - start;
    
    function update() {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const current = Math.floor(start + (difference * progress));
        
        element.textContent = current.toLocaleString('da-DK');
        
        if (progress < 1) {
            requestAnimationFrame(update);
        }
    }
    
    update();
}

function updateRecentActivity(activities) {
    const activityContainer = document.querySelector('.recent-activity-list');
    if (!activityContainer) return;
    
    activityContainer.innerHTML = activities.map(activity => `
        <div class="activity-item">
            <div class="activity-icon ${activity.type}">
                <i class="fas ${getActivityIcon(activity.type)}"></i>
            </div>
            <div class="activity-content">
                <p>${activity.text}</p>
                <span class="activity-time">${activity.time}</span>
            </div>
        </div>
    `).join('');
}

function getActivityIcon(type) {
    const icons = {
        kick: 'fa-door-open',
        join: 'fa-user-plus',
        music: 'fa-music',
        automod: 'fa-shield-alt',
        role: 'fa-user-tag',
        backup: 'fa-save'
    };
    return icons[type] || 'fa-info';
}

function updateQuickActions(server) {
    // Update quick action buttons based on server permissions
    const quickActions = document.querySelector('.quick-actions');
    if (!quickActions) return;
    
    // Add server-specific quick actions
    console.log('Quick actions updated for server:', server.name);
}

function loadInitialData() {
    // Load initial dashboard data
    console.log('Loading initial dashboard data for user:', currentUser?.username);
}

// Enhanced Dashboard Features
function initializeCharts() {
    // Chart initialization code would go here
    console.log('Charts initialized');
}

let realTimeUpdateInterval;
let isPageVisible = true;

// Track page visibility to pause updates when not visible
document.addEventListener('visibilitychange', function() {
    isPageVisible = !document.hidden;
    if (isPageVisible && currentServer) {
        setupRealTimeUpdates();
    } else {
        clearRealTimeUpdates();
    }
});

function setupRealTimeUpdates() {
    // Clear existing interval to prevent duplicates
    clearRealTimeUpdates();
    
    // Only update if page is visible and server is selected
    if (!isPageVisible || !currentServer) return;
    
    // Reduced frequency: Update every 2 minutes instead of 30 seconds
    realTimeUpdateInterval = setInterval(() => {
        if (currentServer && isPageVisible) {
            // Update live statistics
            const newData = generateServerData(currentServer);
            updateStatistics(newData.stats);
        }
    }, 120000); // Update every 2 minutes
}

function clearRealTimeUpdates() {
    if (realTimeUpdateInterval) {
        clearInterval(realTimeUpdateInterval);
        realTimeUpdateInterval = null;
    }
}

function setupFormHandlers() {
    // Form submission handlers
    console.log('Form handlers setup');
}

function initMobileMenu() {
    const hamburger = document.querySelector('.mobile-hamburger');
    const sidebar = document.querySelector('.sidebar');
    
    if (hamburger && sidebar) {
        hamburger.addEventListener('click', function() {
            sidebar.classList.toggle('mobile-open');
            this.classList.toggle('active');
        });
        
        // Close sidebar when clicking outside
        document.addEventListener('click', function(e) {
            if (!sidebar.contains(e.target) && !hamburger.contains(e.target)) {
                sidebar.classList.remove('mobile-open');
                hamburger.classList.remove('active');
            }
        });
    }
}

function initParticleBackground() {
    // Particle background animation
    const particleContainer = document.querySelector('.dashboard-particles');
    if (!particleContainer) return;
    
    for (let i = 0; i < 30; i++) {
        const particle = document.createElement('div');
        particle.classList.add('dashboard-particle');
        particle.style.cssText = `
            position: absolute;
            width: ${Math.random() * 3 + 1}px;
            height: ${Math.random() * 3 + 1}px;
            background: rgba(88, 101, 242, ${Math.random() * 0.5 + 0.2});
            border-radius: 50%;
            left: ${Math.random() * 100}%;
            top: ${Math.random() * 100}%;
            animation: dashboardFloat ${Math.random() * 15 + 10}s linear infinite;
            animation-delay: ${Math.random() * 5}s;
        `;
        particleContainer.appendChild(particle);
    }
}

function setupAnimations() {
    // Enhanced animations and interactions
    const cards = document.querySelectorAll('.stat-card, .chart-card');
    
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
        card.classList.add('animate-in');
    });
    
    // Add hover effects
    document.querySelectorAll('.stat-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
}

// Add animation styles
const dashboardStyles = document.createElement('style');
dashboardStyles.textContent = `
    @keyframes dashboardFloat {
        0%, 100% {
            transform: translateY(0) rotate(0deg);
            opacity: 0.5;
        }
        50% {
            transform: translateY(-20px) rotate(180deg);
            opacity: 1;
        }
    }
    
    .animate-in {
        opacity: 0;
        transform: translateY(30px);
        animation: slideInUp 0.6s ease-out forwards;
    }
    
    @keyframes slideInUp {
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    .no-servers-message {
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 60vh;
        text-align: center;
    }
    
    .no-servers-content {
        background: rgba(15, 15, 35, 0.9);
        backdrop-filter: blur(20px);
        border: 1px solid rgba(88, 101, 242, 0.3);
        border-radius: var(--border-radius-xl);
        padding: 3rem;
        max-width: 500px;
    }
    
    .no-servers-content i {
        font-size: 4rem;
        color: var(--primary-color);
        margin-bottom: 1.5rem;
    }
    
    .no-servers-content h2 {
        color: var(--text-primary);
        font-size: 2rem;
        font-weight: 700;
        margin-bottom: 1rem;
    }
    
    .no-servers-content p {
        color: var(--text-secondary);
        font-size: 1.1rem;
        line-height: 1.6;
        margin-bottom: 2rem;
    }
    
    .section-placeholder {
        text-align: center;
        padding: 4rem 2rem;
        background: rgba(15, 15, 35, 0.9);
        border-radius: var(--border-radius-lg);
        border: 1px solid rgba(88, 101, 242, 0.2);
    }
    
    .section-placeholder h2 {
        color: var(--text-primary);
        font-size: 2rem;
        margin-bottom: 1rem;
    }
    
    .section-placeholder p {
        color: var(--text-secondary);
        font-size: 1.1rem;
    }
`;
document.head.appendChild(dashboardStyles);
