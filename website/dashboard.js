// Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize charts
    initializeCharts();
    
    // Setup menu interactions
    setupMenuInteractions();
    
    // Setup real-time updates
    setupRealTimeUpdates();
    
    // Setup form handlers
    setupFormHandlers();
    
    // Setup mobile menu
    setupMobileMenu();
    
    // Setup animations
    setupAnimations();
    
    // Setup counter animations
    animateCounters();
    
    // Add new enhancements
    setTimeout(() => {
        createParticleBackground();
        setupCardEffects();
        simulateLiveData();
    }, 1000);
});

// Mobile menu functionality
function setupMobileMenu() {
    const mobileToggle = document.querySelector('.mobile-menu-toggle');
    const sidebar = document.querySelector('.sidebar');
    
    if (mobileToggle && sidebar) {
        mobileToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
        
        // Close sidebar when clicking outside
        document.addEventListener('click', function(e) {
            if (!sidebar.contains(e.target) && !mobileToggle.contains(e.target)) {
                sidebar.classList.remove('active');
            }
        });
    }
}

// Setup scroll animations
function setupAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observe all stat cards and action cards
    document.querySelectorAll('.stat-card, .action-card, .activity-feed, .chart-container').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
        observer.observe(el);
    });
}

// Animate counters
function animateCounters() {
    const counters = document.querySelectorAll('.stat-value');
    
    counters.forEach(counter => {
        const target = parseInt(counter.textContent.replace(/[^\d]/g, ''));
        if (isNaN(target)) return;
        
        let current = 0;
        const increment = target / 100;
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            
            // Format number with commas or percentage
            if (counter.textContent.includes('%')) {
                counter.textContent = current.toFixed(1) + '%';
            } else {
                counter.textContent = Math.floor(current).toLocaleString();
            }
        }, 20);
    });
}

function initializeCharts() {
    // Activity Chart
    const activityCtx = document.getElementById('activityChart');
    if (activityCtx) {
        new Chart(activityCtx, {
            type: 'line',
            data: {
                labels: ['Man', 'Tir', 'Ons', 'Tor', 'Fre', 'Lør', 'Søn'],
                datasets: [{
                    label: 'Beskeder',
                    data: [1200, 1900, 3000, 5000, 2000, 3000, 4500],
                    borderColor: '#5865f2',
                    backgroundColor: 'rgba(88, 101, 242, 0.2)',
                    tension: 0.4,
                    fill: true,
                    pointBackgroundColor: '#5865f2',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8
                }, {
                    label: 'Aktive Brugere',
                    data: [300, 450, 600, 800, 500, 650, 750],
                    borderColor: '#00d4aa',
                    backgroundColor: 'rgba(0, 212, 170, 0.2)',
                    tension: 0.4,
                    fill: true,
                    pointBackgroundColor: '#00d4aa',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: '#ffffff',
                            font: {
                                family: 'Inter',
                                weight: '600'
                            },
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)',
                            drawBorder: false
                        },
                        ticks: {
                            color: '#a0a9c0',
                            font: {
                                family: 'Inter'
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            color: '#a0a9c0',
                            font: {
                                family: 'Inter'
                            }
                        }
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                },
                animation: {
                    duration: 2000,
                    easing: 'easeInOutCubic'
                }
            }
        });
    }

    // Command Usage Chart
    const commandCtx = document.getElementById('commandChart');
    if (commandCtx) {
        new Chart(commandCtx, {
            type: 'doughnut',
            data: {
                labels: ['Musik', 'Moderation', 'Fun', 'Utility', 'Andre'],
                datasets: [{
                    data: [35, 25, 20, 15, 5],
                    backgroundColor: [
                        '#8b5cf6',
                        '#ef4444',
                        '#f59e0b',
                        '#10b981',
                        '#6b7280'
                    ],
                    borderColor: 'rgba(15, 15, 35, 0.8)',
                    borderWidth: 3,
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: '#ffffff',
                            font: {
                                family: 'Inter',
                                weight: '600'
                            },
                            padding: 15,
                            usePointStyle: true
                        }
                    }
                },
                animation: {
                    animateRotate: true,
                    duration: 2000,
                    easing: 'easeInOutCubic'
                }
            }
        });
    }
}

function setupMenuInteractions() {
    // Menu item clicks
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all items
            menuItems.forEach(i => i.classList.remove('active'));
            
            // Add active class to clicked item
            this.classList.add('active');
            
            // Load content based on selection
            const page = this.getAttribute('href').substring(1);
            loadPageContent(page);
        });
    });

    // Server selector
    const serverSelector = document.querySelector('.server-selector');
    if (serverSelector) {
        serverSelector.addEventListener('click', function() {
            showServerSwitcher();
        });
    }

    // User menu
    const userMenu = document.querySelector('.user-menu');
    if (userMenu) {
        userMenu.addEventListener('click', function() {
            showUserMenu();
        });
    }
}

function loadPageContent(page) {
    const content = document.querySelector('.dashboard-content');
    
    // Show loading state
    content.innerHTML = `
        <div style="text-align: center; padding: 50px;">
            <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary-color);"></i>
            <p style="margin-top: 16px; color: var(--text-secondary);">Indlæser ${page}...</p>
        </div>
    `;
    
    // Simulate loading delay
    setTimeout(() => {
        switch(page) {
            case 'analytics':
                loadAnalyticsPage();
                break;
            case 'general':
                loadGeneralSettings();
                break;
            case 'moderation':
                loadModerationSettings();
                break;
            case 'automod':
                loadAutoModerationSettings();
                break;
            case 'music':
                loadMusicSettings();
                break;
            case 'commands':
                loadCustomCommands();
                break;
            case 'roles':
                loadRoleSettings();
                break;
            case 'welcome':
                loadWelcomeSettings();
                break;
            case 'levels':
                loadLevelingSettings();
                break;
            default:
                loadDashboard();
        }
    }, 500);
}

function loadDashboard() {
    // This would reload the main dashboard content
    location.reload();
}

function loadAnalyticsPage() {
    const content = document.querySelector('.dashboard-content');
    content.innerHTML = `
        <div class="section">
            <h2 class="section-title">Detaljeret Analytics</h2>
            <div class="analytics-grid">
                <div class="analytics-card">
                    <h3>Besked Aktivitet</h3>
                    <canvas id="messageAnalytics"></canvas>
                </div>
                <div class="analytics-card">
                    <h3>Kanal Popularitet</h3>
                    <canvas id="channelAnalytics"></canvas>
                </div>
                <div class="analytics-card">
                    <h3>Bruger Engagement</h3>
                    <canvas id="userAnalytics"></canvas>
                </div>
            </div>
        </div>
    `;
    
    // Initialize analytics charts
    // This would include more detailed charts
}

function loadGeneralSettings() {
    const content = document.querySelector('.dashboard-content');
    content.innerHTML = `
        <div class="section">
            <h2 class="section-title">Generelle Indstillinger</h2>
            <div class="settings-form">
                <div class="form-group">
                    <label>Bot Prefix</label>
                    <input type="text" value="!" class="form-control">
                    <small>Kommando prefix for botten</small>
                </div>
                
                <div class="form-group">
                    <label>Sprog</label>
                    <select class="form-control">
                        <option>Dansk</option>
                        <option>English</option>
                        <option>Deutsch</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Tidszone</label>
                    <select class="form-control">
                        <option>Europe/Copenhagen</option>
                        <option>Europe/London</option>
                        <option>America/New_York</option>
                    </select>
                </div>
                
                <button class="btn btn-primary">Gem Indstillinger</button>
            </div>
        </div>
    `;
}

function loadModerationSettings() {
    const content = document.querySelector('.dashboard-content');
    content.innerHTML = `
        <div class="section">
            <h2 class="section-title">Moderation Indstillinger</h2>
            <div class="settings-form">
                <div class="form-group">
                    <label class="checkbox-label">
                        <input type="checkbox" checked>
                        <span class="checkmark"></span>
                        Automatisk slet spam beskeder
                    </label>
                </div>
                
                <div class="form-group">
                    <label class="checkbox-label">
                        <input type="checkbox" checked>
                        <span class="checkmark"></span>
                        Log moderationshandlinger
                    </label>
                </div>
                
                <div class="form-group">
                    <label>Moderation Log Kanal</label>
                    <select class="form-control">
                        <option>#mod-log</option>
                        <option>#admin-log</option>
                        <option>Ingen</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Max advarsler før ban</label>
                    <input type="number" value="3" class="form-control">
                </div>
                
                <button class="btn btn-primary">Gem Indstillinger</button>
            </div>
        </div>
    `;
}

function setupRealTimeUpdates() {
    // Simulate real-time updates
    setInterval(updateStats, 30000); // Update every 30 seconds
    setInterval(updateActivity, 10000); // Update activity every 10 seconds
}

function updateStats() {
    // Update dashboard stats
    const statValues = document.querySelectorAll('.stat-value');
    statValues.forEach(stat => {
        const currentValue = parseInt(stat.textContent.replace(/[^\d]/g, ''));
        const change = Math.floor(Math.random() * 10) - 5; // Random change
        const newValue = Math.max(0, currentValue + change);
        
        // Animate the change
        animateValue(stat, currentValue, newValue, 1000);
    });
}

function animateValue(element, start, end, duration) {
    const range = end - start;
    const increment = range / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
            current = end;
            clearInterval(timer);
        }
        element.textContent = Math.floor(current).toLocaleString();
    }, 16);
}

function updateActivity() {
    // Add new activity items
    const activities = [
        {
            icon: 'fas fa-music',
            class: 'music',
            action: 'Musik afspillet',
            details: 'Random sang tilføjet til kø',
            time: 'Lige nu'
        },
        {
            icon: 'fas fa-user-plus',
            class: 'welcome',
            action: 'Nyt medlem',
            details: `RandomUser#${Math.floor(Math.random() * 9999)} joinde serveren`,
            time: 'Lige nu'
        }
    ];
    
    const activityFeed = document.querySelector('.activity-feed');
    if (activityFeed && Math.random() > 0.7) { // 30% chance to add new activity
        const randomActivity = activities[Math.floor(Math.random() * activities.length)];
        const newItem = createActivityItem(randomActivity);
        
        activityFeed.insertBefore(newItem, activityFeed.firstChild);
        
        // Remove old items if more than 10
        const items = activityFeed.querySelectorAll('.activity-item');
        if (items.length > 10) {
            items[items.length - 1].remove();
        }
    }
}

function createActivityItem(activity) {
    const item = document.createElement('div');
    item.className = 'activity-item';
    item.style.opacity = '0';
    item.style.transform = 'translateY(-20px)';
    
    item.innerHTML = `
        <div class="activity-icon ${activity.class}">
            <i class="${activity.icon}"></i>
        </div>
        <div class="activity-content">
            <span class="activity-action">${activity.action}</span>
            <span class="activity-details">${activity.details}</span>
            <span class="activity-time">${activity.time}</span>
        </div>
    `;
    
    // Animate in
    setTimeout(() => {
        item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
        item.style.opacity = '1';
        item.style.transform = 'translateY(0)';
    }, 100);
    
    return item;
}

function setupFormHandlers() {
    // Handle form submissions
    document.addEventListener('submit', function(e) {
        if (e.target.classList.contains('settings-form') || e.target.closest('.settings-form')) {
            e.preventDefault();
            
            const button = e.target.querySelector('button[type="submit"], .btn-primary');
            if (button) {
                const originalText = button.textContent;
                button.textContent = 'Gemmer...';
                button.disabled = true;
                
                // Simulate save
                setTimeout(() => {
                    button.textContent = 'Gemt!';
                    button.style.background = '#10b981';
                    
                    setTimeout(() => {
                        button.textContent = originalText;
                        button.disabled = false;
                        button.style.background = '';
                    }, 2000);
                }, 1000);
            }
        }
    });
}

function showServerSwitcher() {
    // Create modal for server switching
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>Vælg Server</h3>
                <button class="modal-close">&times;</button>
            </div>
            <div class="modal-body">
                <div class="server-list">
                    <div class="server-item active">
                        <img src="https://cdn.discordapp.com/icons/123456789/server-icon.png" alt="Server">
                        <div class="server-info">
                            <span class="server-name">Min Discord Server</span>
                            <span class="server-members">1,234 medlemmer</span>
                        </div>
                    </div>
                    <div class="server-item">
                        <img src="https://cdn.discordapp.com/icons/987654321/server-icon2.png" alt="Server">
                        <div class="server-info">
                            <span class="server-name">Gaming Community</span>
                            <span class="server-members">5,678 medlemmer</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Close modal handlers
    modal.querySelector('.modal-close').addEventListener('click', () => {
        modal.remove();
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.remove();
        }
    });
}

function showUserMenu() {
    // Create dropdown for user menu
    const dropdown = document.createElement('div');
    dropdown.className = 'user-dropdown';
    dropdown.innerHTML = `
        <div class="dropdown-item">
            <i class="fas fa-user"></i>
            Profil
        </div>
        <div class="dropdown-item">
            <i class="fas fa-cog"></i>
            Indstillinger
        </div>
        <div class="dropdown-item">
            <i class="fas fa-question-circle"></i>
            Hjælp
        </div>
        <hr>
        <div class="dropdown-item">
            <i class="fas fa-sign-out-alt"></i>
            Log ud
        </div>
    `;
    
    // Position and show dropdown
    const userMenu = document.querySelector('.user-menu');
    userMenu.appendChild(dropdown);
    
    // Close dropdown when clicking outside
    setTimeout(() => {
        document.addEventListener('click', function closeDropdown(e) {
            if (!userMenu.contains(e.target)) {
                dropdown.remove();
                document.removeEventListener('click', closeDropdown);
            }
        });
    }, 100);
}

// Add CSS for dynamic elements
const dynamicStyles = document.createElement('style');
dynamicStyles.textContent = `
    .settings-form {
        max-width: 600px;
    }
    
    .form-group {
        margin-bottom: 24px;
    }
    
    .form-group label {
        display: block;
        font-weight: 600;
        margin-bottom: 8px;
        color: var(--text-primary);
    }
    
    .form-control {
        width: 100%;
        padding: 12px;
        border: 1px solid var(--border-color);
        border-radius: var(--border-radius);
        font-size: 0.9rem;
        transition: var(--transition);
    }
    
    .form-control:focus {
        outline: none;
        border-color: var(--primary-color);
        box-shadow: 0 0 0 3px rgba(88, 101, 242, 0.1);
    }
    
    .checkbox-label {
        display: flex !important;
        align-items: center;
        gap: 12px;
        cursor: pointer;
    }
    
    .modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    
    .modal-content {
        background: white;
        border-radius: var(--border-radius);
        width: 90%;
        max-width: 500px;
        max-height: 80vh;
        overflow: hidden;
    }
    
    .modal-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px;
        border-bottom: 1px solid var(--border-color);
    }
    
    .modal-close {
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        color: var(--text-secondary);
    }
    
    .server-list {
        padding: 20px;
    }
    
    .server-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-radius: var(--border-radius);
        cursor: pointer;
        transition: var(--transition);
    }
    
    .server-item:hover {
        background: var(--bg-secondary);
    }
    
    .server-item.active {
        background: rgba(88, 101, 242, 0.1);
        border: 1px solid var(--primary-color);
    }
    
    .user-dropdown {
        position: absolute;
        top: 100%;
        right: 0;
        background: white;
        border: 1px solid var(--border-color);
        border-radius: var(--border-radius);
        box-shadow: var(--shadow-lg);
        min-width: 200px;
        z-index: 100;
    }
    
    .dropdown-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        cursor: pointer;
        transition: var(--transition);
    }
    
    .dropdown-item:hover {
        background: var(--bg-secondary);
    }
    
    .analytics-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
        gap: 20px;
    }
    
    .analytics-card {
        background: var(--bg-primary);
        border-radius: var(--border-radius);
        padding: 24px;
        box-shadow: var(--shadow-sm);
        border: 1px solid var(--border-color);
    }
    
    /* Particle background */
    canvas {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
        z-index: 1;
    }
`;

document.head.appendChild(dynamicStyles);

// Add particle background effect
function createParticleBackground() {
    const canvas = document.createElement('canvas');
    canvas.style.position = 'fixed';
    canvas.style.top = '0';
    canvas.style.left = '0';
    canvas.style.width = '100%';
    canvas.style.height = '100%';
    canvas.style.pointerEvents = 'none';
    canvas.style.zIndex = '1';
    canvas.style.opacity = '0.3';
    
    document.body.appendChild(canvas);
    
    const ctx = canvas.getContext('2d');
    let particles = [];
    
    function resizeCanvas() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }
    
    function createParticle() {
        return {
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            vx: (Math.random() - 0.5) * 0.5,
            vy: (Math.random() - 0.5) * 0.5,
            radius: Math.random() * 2 + 1,
            opacity: Math.random() * 0.5 + 0.1
        };
    }
    
    function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        particles.forEach((particle, index) => {
            particle.x += particle.vx;
            particle.y += particle.vy;
            
            if (particle.x < 0 || particle.x > canvas.width) particle.vx *= -1;
            if (particle.y < 0 || particle.y > canvas.height) particle.vy *= -1;
            
            ctx.beginPath();
            ctx.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(88, 101, 242, ${particle.opacity})`;
            ctx.fill();
            
            // Draw connections
            particles.slice(index + 1).forEach(otherParticle => {
                const dx = particle.x - otherParticle.x;
                const dy = particle.y - otherParticle.y;
                const distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < 100) {
                    ctx.beginPath();
                    ctx.moveTo(particle.x, particle.y);
                    ctx.lineTo(otherParticle.x, otherParticle.y);
                    ctx.strokeStyle = `rgba(88, 101, 242, ${0.1 * (100 - distance) / 100})`;
                    ctx.lineWidth = 0.5;
                    ctx.stroke();
                }
            });
        });
        
        requestAnimationFrame(animate);
    }
    
    resizeCanvas();
    
    for (let i = 0; i < 50; i++) {
        particles.push(createParticle());
    }
    
    animate();
    
    window.addEventListener('resize', resizeCanvas);
}

// Enhanced hover effects for cards
function setupCardEffects() {
    const cards = document.querySelectorAll('.stat-card, .action-card');
    
    cards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;
            
            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-8px)`;
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateY(0)';
        });
    });
}

// Live data simulation
function simulateLiveData() {
    setInterval(() => {
        // Update random stat values
        const statValues = document.querySelectorAll('.stat-value');
        statValues.forEach(stat => {
            if (!stat.textContent.includes('%')) {
                const currentValue = parseInt(stat.textContent.replace(/[^\d]/g, ''));
                const change = Math.floor(Math.random() * 20) - 10;
                const newValue = Math.max(0, currentValue + change);
                stat.textContent = newValue.toLocaleString();
            }
        });
        
        // Add new activity items occasionally
        if (Math.random() < 0.3) {
            addNewActivityItem();
        }
    }, 5000);
}

function addNewActivityItem() {
    const activities = [
        { icon: 'fas fa-user-plus', class: 'welcome', action: 'Nyt medlem', details: `User#${Math.floor(Math.random() * 9999)} joinde serveren`, time: 'Lige nu' },
        { icon: 'fas fa-music', class: 'music', action: 'Musik afspillet', details: 'Nu spiller: Random Song - Artist', time: 'Lige nu' },
        { icon: 'fas fa-terminal', class: 'command', action: 'Command udført', details: `!help kommando brugt af User#${Math.floor(Math.random() * 9999)}`, time: 'Lige nu' }
    ];
    
    const activity = activities[Math.floor(Math.random() * activities.length)];
    const feed = document.querySelector('.activity-feed');
    
    if (feed && feed.children.length >= 4) {
        feed.removeChild(feed.lastElementChild);
    }
    
    const activityItem = document.createElement('div');
    activityItem.className = 'activity-item';
    activityItem.style.opacity = '0';
    activityItem.style.transform = 'translateX(-20px)';
    
    activityItem.innerHTML = `
        <div class="activity-icon ${activity.class}">
            <i class="${activity.icon}"></i>
        </div>
        <div class="activity-content">
            <span class="activity-action">${activity.action}</span>
            <span class="activity-details">${activity.details}</span>
            <span class="activity-time">${activity.time}</span>
        </div>
    `;
    
    feed.insertBefore(activityItem, feed.firstChild);
    
    // Animate in
    setTimeout(() => {
        activityItem.style.transition = 'all 0.3s ease';
        activityItem.style.opacity = '1';
        activityItem.style.transform = 'translateX(0)';
    }, 100);
}

// Initialize all enhancements
document.addEventListener('DOMContentLoaded', function() {
    // ... existing code ...
    
    // Add new enhancements
    setTimeout(() => {
        createParticleBackground();
        setupCardEffects();
        simulateLiveData();
    }, 1000);
});

// Add loading state management
function showLoading(element) {
    element.classList.add('loading');
}

function hideLoading(element) {
    element.classList.remove('loading');
}

// Enhanced menu interactions with smooth transitions
function enhancedMenuSetup() {
    const menuItems = document.querySelectorAll('.menu-item');
    
    menuItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Remove active from all items
            menuItems.forEach(mi => mi.classList.remove('active'));
            
            // Add active to clicked item
            item.classList.add('active');
            
            // Simulate page transition
            const mainContent = document.querySelector('.dashboard-content');
            showLoading(mainContent);
            
            setTimeout(() => {
                hideLoading(mainContent);
            }, 800);
        });
    });
}
