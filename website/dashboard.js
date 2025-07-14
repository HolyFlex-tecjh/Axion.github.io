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
    
    // Initialize appeal system
    initializeAppealSystem();
    
    // Add new enhancements
    setTimeout(() => {
        createParticleBackground();
        setupCardEffects();
        simulateLiveData();
        initializeFormHandlers();
        initializeInteractiveElements();
    }, 1000);
});

// Appeal System Implementation
function initializeAppealSystem() {
    setupAppealNavigation();
    setupAppealModals();
    setupAppealHandlers();
    loadAppealData();
}

// Navigation for all sections
function setupAppealNavigation() {
    const menuItems = document.querySelectorAll('.menu-item');
    const sections = {
        'dashboard': document.querySelector('.dashboard-content'),
        'analytics': document.getElementById('analytics-section'),
        'general': document.getElementById('general-section'),
        'moderation': document.getElementById('moderation-section'),
        'automod': document.getElementById('automod-section'),
        'music': document.getElementById('music-section'),
        'commands': document.getElementById('commands-section'),
        'roles': document.getElementById('roles-section'),
        'welcome': document.getElementById('welcome-section'),
        'levels': document.getElementById('levels-section'),
        'appeals': document.getElementById('appeals-section'),
        'my-appeals': document.getElementById('my-appeals-section'),
        'banned-servers': document.getElementById('banned-servers-section')
    };
    
    menuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            const href = this.getAttribute('href');
            if (href && href.startsWith('#')) {
                const sectionId = href.substring(1);
                showSection(sectionId, sections);
                
                // Update active state
                menuItems.forEach(mi => mi.classList.remove('active'));
                this.classList.add('active');
                
                // Update page title
                updatePageTitle(sectionId);
            }
        });
    });
    
    // Set dashboard as active by default
    const dashboardItem = document.querySelector('.menu-item[href="#dashboard"]');
    if (dashboardItem) {
        dashboardItem.classList.add('active');
    }
}

function showSection(sectionId, sections) {
    // Hide all sections
    Object.values(sections).forEach(section => {
        if (section) section.style.display = 'none';
    });
    
    // Show selected section
    const targetSection = sections[sectionId];
    if (targetSection) {
        targetSection.style.display = 'block';
        
        // Load section-specific data
        switch(sectionId) {
            case 'analytics':
                loadAnalyticsData();
                break;
            case 'appeals':
                loadAdminAppeals();
                break;
            case 'my-appeals':
                loadUserAppeals();
                break;
            case 'banned-servers':
                loadBannedServers();
                break;
            case 'commands':
                loadCustomCommands();
                break;
            case 'roles':
                loadRolesData();
                break;
        }
    }
}

// Update page title based on section
function updatePageTitle(sectionId) {
    const pageTitle = document.querySelector('.page-title h1');
    const pageSubtitle = document.querySelector('.page-title p');
    
    const titles = {
        'dashboard': { title: 'Dashboard Oversigt', subtitle: 'Administrer din Axion Bot konfiguration' },
        'analytics': { title: 'Analytics', subtitle: 'Se detaljerede statistikker og trends' },
        'general': { title: 'Generelle Indstillinger', subtitle: 'Konfigurer grundlæggende bot indstillinger' },
        'moderation': { title: 'Moderation', subtitle: 'Administrer moderation indstillinger' },
        'automod': { title: 'Auto-Moderation', subtitle: 'Automatisk spam og toxic detection' },
        'music': { title: 'Musik', subtitle: 'Konfigurer musik bot indstillinger' },
        'commands': { title: 'Custom Commands', subtitle: 'Opret og administrer custom commands' },
        'roles': { title: 'Roller & Permissions', subtitle: 'Administrer server roller og tilladelser' },
        'welcome': { title: 'Velkomst Beskeder', subtitle: 'Opsæt personaliserede velkomst beskeder' },
        'levels': { title: 'Leveling System', subtitle: 'Konfigurer XP og level system' },
        'appeals': { title: 'Ban Appeals Management', subtitle: 'Administrer ban appeals fra brugere' },
        'my-appeals': { title: 'Mine Appeals', subtitle: 'Se og administrer dine ban appeals' },
        'banned-servers': { title: 'Banned Fra Servere', subtitle: 'Servere du er banned fra' }
    };
    
    const titleData = titles[sectionId] || titles['dashboard'];
    if (pageTitle) pageTitle.textContent = titleData.title;
    if (pageSubtitle) pageSubtitle.textContent = titleData.subtitle;
}

// Modal setup
function setupAppealModals() {
    const appealModal = document.getElementById('appealModal');
    const responseModal = document.getElementById('responseModal');
    const createAppealBtn = document.getElementById('createAppealBtn');
    const closeAppealModal = document.getElementById('closeAppealModal');
    const closeResponseModal = document.getElementById('closeResponseModal');
    
    // Create appeal modal
    if (createAppealBtn) {
        createAppealBtn.addEventListener('click', () => {
            showAppealModal();
        });
    }
    
    // Close modals
    if (closeAppealModal) {
        closeAppealModal.addEventListener('click', () => {
            hideModal(appealModal);
        });
    }
    
    if (closeResponseModal) {
        closeResponseModal.addEventListener('click', () => {
            hideModal(responseModal);
        });
    }
    
    // Close modals when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target === appealModal) {
            hideModal(appealModal);
        }
        if (e.target === responseModal) {
            hideModal(responseModal);
        }
    });
}

// Appeal form handlers
function setupAppealHandlers() {
    const appealForm = document.getElementById('appealForm');
    const cancelAppeal = document.getElementById('cancelAppeal');
    const refreshAppeals = document.getElementById('refreshAppeals');
    const refreshBannedServers = document.getElementById('refreshBannedServers');
    const appealStatusFilter = document.getElementById('appealStatusFilter');
    
    // Appeal form submission
    if (appealForm) {
        appealForm.addEventListener('submit', handleAppealSubmission);
    }
    
    // Cancel appeal
    if (cancelAppeal) {
        cancelAppeal.addEventListener('click', () => {
            hideModal(document.getElementById('appealModal'));
        });
    }
    
    // Refresh buttons
    if (refreshAppeals) {
        refreshAppeals.addEventListener('click', loadAdminAppeals);
    }
    
    if (refreshBannedServers) {
        refreshBannedServers.addEventListener('click', loadBannedServers);
    }
    
    // Status filter
    if (appealStatusFilter) {
        appealStatusFilter.addEventListener('change', filterAppeals);
    }
    
    // Response form handlers
    const approveAppeal = document.getElementById('approveAppeal');
    const denyAppeal = document.getElementById('denyAppeal');
    
    if (approveAppeal) {
        approveAppeal.addEventListener('click', () => handleAppealResponse('approved'));
    }
    
    if (denyAppeal) {
        denyAppeal.addEventListener('click', () => handleAppealResponse('denied'));
    }
}

// Load appeal data
function loadAppealData() {
    loadAppealCounts();
    loadBannedServersForDropdown();
}

// Load analytics data
function loadAnalyticsData() {
    console.log('Loading analytics data...');
    // Initialize analytics charts if needed
    initializeAnalyticsCharts();
    
    // Simulate loading analytics data
    setTimeout(() => {
        // Update analytics cards with sample data
        const analyticsCards = document.querySelectorAll('.analytics-card');
        analyticsCards.forEach(card => {
            card.classList.add('loaded');
        });
    }, 500);
}

// Load custom commands
function loadCustomCommands() {
    console.log('Loading custom commands...');
    
    const commandsList = document.querySelector('.commands-list');
    if (commandsList) {
        // Sample commands data
        const sampleCommands = [
            { name: '!hello', response: 'Hello there!', uses: 45 },
            { name: '!rules', response: 'Please follow server rules', uses: 23 },
            { name: '!info', response: 'Server information here', uses: 67 },
            { name: '!help', response: 'Available commands: !hello, !rules, !info', uses: 89 }
        ];
        
        commandsList.innerHTML = sampleCommands.map(cmd => `
            <div class="command-item">
                <span class="command-name">${cmd.name}</span>
                <span class="command-response">${cmd.response}</span>
                <span class="command-uses">${cmd.uses} uses</span>
                <div class="command-actions">
                    <button class="btn btn-sm btn-secondary">Edit</button>
                    <button class="btn btn-sm btn-danger">Delete</button>
                </div>
            </div>
        `).join('');
    }
}

// Load roles data
function loadRolesData() {
    console.log('Loading roles data...');
    
    const rolesList = document.querySelector('.roles-list');
    if (rolesList) {
        // Sample roles data
        const sampleRoles = [
            { name: '@Admin', permissions: 'All Permissions', members: 3, color: '#ff6b6b' },
            { name: '@Moderator', permissions: 'Moderation, Kick, Ban', members: 8, color: '#4ecdc4' },
            { name: '@VIP', permissions: 'Special Channels, Custom Emoji', members: 15, color: '#ffd93d' },
            { name: '@Member', permissions: 'Basic Permissions', members: 234, color: '#45b7d1' }
        ];
        
        rolesList.innerHTML = sampleRoles.map(role => `
            <div class="role-item">
                <div class="role-color" style="background-color: ${role.color}; width: 12px; height: 12px; border-radius: 50%; margin-right: 8px;"></div>
                <span class="role-name">${role.name}</span>
                <span class="role-permissions">${role.permissions}</span>
                <span class="role-members">${role.members} members</span>
                <button class="btn btn-sm btn-secondary">Configure</button>
            </div>
        `).join('');
    }
}

// Initialize analytics charts
function initializeAnalyticsCharts() {
    // Growth Chart
    const growthCtx = document.getElementById('growthChart');
    if (growthCtx) {
        new Chart(growthCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'Members',
                    data: [100, 150, 200, 280, 350, 400],
                    borderColor: 'rgb(147, 51, 234)',
                    backgroundColor: 'rgba(147, 51, 234, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        labels: {
                            color: '#ffffff'
                        }
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            color: '#ffffff'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#ffffff'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    }
                }
            }
        });
    }
    
    // Message Chart
    const messageCtx = document.getElementById('messageChart');
    if (messageCtx) {
        new Chart(messageCtx, {
            type: 'bar',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Messages',
                    data: [120, 190, 300, 500, 200, 300, 450],
                    backgroundColor: 'rgba(59, 130, 246, 0.8)',
                    borderColor: 'rgb(59, 130, 246)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        labels: {
                            color: '#ffffff'
                        }
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            color: '#ffffff'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#ffffff'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    }
                }
            }
        });
    }
}

function loadAppealCounts() {
    // Simulate API call to get appeal counts
    setTimeout(() => {
        const appealNotification = document.getElementById('appealNotification');
        const bannedServersCount = document.getElementById('bannedServersCount');
        
        // Mock data - replace with actual API calls
        const pendingAppeals = 3;
        const bannedServers = 2;
        
        if (appealNotification) {
            appealNotification.textContent = pendingAppeals;
            appealNotification.style.display = pendingAppeals > 0 ? 'block' : 'none';
        }
        
        if (bannedServersCount) {
            bannedServersCount.textContent = bannedServers;
            bannedServersCount.style.display = bannedServers > 0 ? 'block' : 'none';
        }
    }, 500);
}

// Initialize form handlers for settings
function initializeFormHandlers() {
    // Handle toggle switches
    const toggleSwitches = document.querySelectorAll('.toggle-switch input[type="checkbox"]');
    toggleSwitches.forEach(toggle => {
        toggle.addEventListener('change', function() {
            const setting = this.closest('.setting-group').querySelector('h3').textContent;
            console.log(`${setting} ${this.checked ? 'enabled' : 'disabled'}`);
            
            // Show feedback
            showSettingFeedback(this, `${setting} ${this.checked ? 'aktiveret' : 'deaktiveret'}`);
        });
    });
    
    // Handle form inputs
    const formInputs = document.querySelectorAll('.form-control');
    formInputs.forEach(input => {
        input.addEventListener('change', function() {
            const setting = this.closest('.setting-group').querySelector('h3, label').textContent;
            console.log(`${setting} changed to: ${this.value}`);
            
            // Show feedback
            showSettingFeedback(this, `${setting} opdateret`);
        });
    });
}

// Initialize interactive elements
function initializeInteractiveElements() {
    // Add hover effects to cards
    const cards = document.querySelectorAll('.stat-card, .analytics-card, .command-item, .role-item');
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.transition = 'transform 0.2s ease';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // Add click handlers for action buttons
    const actionButtons = document.querySelectorAll('.btn');
    actionButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            // Add ripple effect
            const ripple = document.createElement('span');
            ripple.classList.add('ripple');
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

// Show setting feedback
function showSettingFeedback(element, message) {
    // Create feedback element
    const feedback = document.createElement('div');
    feedback.className = 'setting-feedback';
    feedback.textContent = message;
    feedback.style.cssText = `
        position: absolute;
        background: rgba(88, 101, 242, 0.9);
        color: white;
        padding: 8px 12px;
        border-radius: 6px;
        font-size: 12px;
        z-index: 1000;
        pointer-events: none;
        opacity: 0;
        transform: translateY(10px);
        transition: all 0.3s ease;
    `;
    
    // Position feedback near the element
    const rect = element.getBoundingClientRect();
    feedback.style.left = rect.left + 'px';
    feedback.style.top = (rect.bottom + 5) + 'px';
    
    document.body.appendChild(feedback);
    
    // Animate in
    setTimeout(() => {
        feedback.style.opacity = '1';
        feedback.style.transform = 'translateY(0)';
    }, 10);
    
    // Remove after delay
    setTimeout(() => {
        feedback.style.opacity = '0';
        feedback.style.transform = 'translateY(-10px)';
        setTimeout(() => {
            feedback.remove();
        }, 300);
    }, 2000);
}

// Admin appeals management
function loadAdminAppeals() {
    const appealsGrid = document.getElementById('appealsGrid');
    if (!appealsGrid) return;
    
    // Show loading state
    appealsGrid.innerHTML = '<div class="loading-state">Loading appeals...</div>';
    
    // Simulate API call
    setTimeout(() => {
        const mockAppeals = [
            {
                id: 1,
                userId: '123456789',
                username: 'BannedUser#1234',
                avatar: 'https://cdn.discordapp.com/avatars/123456789/avatar.png',
                serverId: '987654321',
                serverName: 'My Discord Server',
                reason: 'I was banned for spam, but I believe it was a misunderstanding. I was sharing a link to a helpful resource and didn\'t realize it would be considered spam.',
                apology: 'I apologize for any confusion and will be more careful about sharing links in the future.',
                status: 'pending',
                createdAt: '2023-12-01T10:30:00Z',
                updatedAt: '2023-12-01T10:30:00Z'
            },
            {
                id: 2,
                userId: '987654321',
                username: 'RegretfulUser#5678',
                avatar: 'https://cdn.discordapp.com/avatars/987654321/avatar.png',
                serverId: '123456789',
                serverName: 'Another Server',
                reason: 'I was banned for inappropriate behavior. I realize my actions were wrong and have learned from this experience.',
                apology: 'I sincerely apologize for my behavior and understand why I was banned. I would like a second chance to be a positive member of the community.',
                status: 'pending',
                createdAt: '2023-11-30T15:45:00Z',
                updatedAt: '2023-11-30T15:45:00Z'
            }
        ];
        
        renderAppeals(mockAppeals);
    }, 1000);
}

function renderAppeals(appeals) {
    const appealsGrid = document.getElementById('appealsGrid');
    if (!appealsGrid) return;
    
    if (appeals.length === 0) {
        appealsGrid.innerHTML = '<div class="no-data">No appeals found.</div>';
        return;
    }
    
    appealsGrid.innerHTML = appeals.map(appeal => `
        <div class="appeal-card ${appeal.status}" data-appeal-id="${appeal.id}">
            <div class="appeal-header">
                <div class="appeal-user">
                    <img src="${appeal.avatar}" alt="${appeal.username}" class="user-avatar">
                    <div class="user-info">
                        <span class="username">${appeal.username}</span>
                        <span class="user-id">${appeal.userId}</span>
                    </div>
                </div>
                <span class="appeal-status status-${appeal.status}">${getStatusText(appeal.status)}</span>
            </div>
            
            <div class="appeal-server">
                <i class="fas fa-server"></i>
                <span>${appeal.serverName}</span>
            </div>
            
            <div class="appeal-content">
                <h4>Reason:</h4>
                <p>${appeal.reason}</p>
                
                ${appeal.apology ? `
                    <h4>Apology:</h4>
                    <p>${appeal.apology}</p>
                ` : ''}
            </div>
            
            <div class="appeal-meta">
                <span class="appeal-date">
                    <i class="fas fa-calendar"></i>
                    ${formatDate(appeal.createdAt)}
                </span>
            </div>
            
            ${appeal.status === 'pending' ? `
                <div class="appeal-actions">
                    <button class="btn btn-success btn-sm" onclick="respondToAppeal(${appeal.id}, 'approve')">
                        <i class="fas fa-check"></i>
                        Approve
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="respondToAppeal(${appeal.id}, 'deny')">
                        <i class="fas fa-times"></i>
                        Deny
                    </button>
                </div>
            ` : ''}
        </div>
    `).join('');
}

// User appeals management
function loadUserAppeals() {
    const userAppealsList = document.getElementById('userAppealsList');
    if (!userAppealsList) return;
    
    // Show loading state
    userAppealsList.innerHTML = '<div class="loading-state">Loading your appeals...</div>';
    
    // Simulate API call
    setTimeout(() => {
        const mockUserAppeals = [
            {
                id: 1,
                serverId: '987654321',
                serverName: 'Gaming Community',
                reason: 'I was banned for using inappropriate language, but I believe the punishment was too harsh.',
                status: 'pending',
                createdAt: '2023-12-01T10:30:00Z',
                response: null
            },
            {
                id: 2,
                serverId: '123456789',
                serverName: 'Study Group',
                reason: 'I was banned for spam, but I was just trying to help with homework.',
                status: 'denied',
                createdAt: '2023-11-28T14:20:00Z',
                response: {
                    reason: 'After review, the ban was justified due to repeated violations.',
                    respondedAt: '2023-11-29T09:15:00Z',
                    respondedBy: 'Admin#1234'
                }
            }
        ];
        
        renderUserAppeals(mockUserAppeals);
    }, 1000);
}

function renderUserAppeals(appeals) {
    const userAppealsList = document.getElementById('userAppealsList');
    if (!userAppealsList) return;
    
    if (appeals.length === 0) {
        userAppealsList.innerHTML = `
            <div class="no-data">
                <i class="fas fa-clipboard-list"></i>
                <p>You haven't submitted any appeals yet.</p>
                <button class="btn btn-primary" id="createFirstAppeal">
                    <i class="fas fa-plus"></i>
                    Create Your First Appeal
                </button>
            </div>
        `;
        
        // Add event listener to the create button
        const createFirstAppeal = document.getElementById('createFirstAppeal');
        if (createFirstAppeal) {
            createFirstAppeal.addEventListener('click', showAppealModal);
        }
        return;
    }
    
    userAppealsList.innerHTML = appeals.map(appeal => `
        <div class="user-appeal-item status-${appeal.status}">
            <div class="appeal-header">
                <div class="appeal-server">
                    <i class="fas fa-server"></i>
                    <span>${appeal.serverName}</span>
                </div>
                <span class="appeal-status status-${appeal.status}">${getStatusText(appeal.status)}</span>
            </div>
            
            <div class="appeal-content">
                <p>${appeal.reason}</p>
            </div>
            
            ${appeal.response ? `
                <div class="appeal-response">
                    <h4>Admin Response:</h4>
                    <p>${appeal.response.reason}</p>
                    <div class="response-meta">
                        <span>Responded by ${appeal.response.respondedBy} on ${formatDate(appeal.response.respondedAt)}</span>
                    </div>
                </div>
            ` : ''}
            
            <div class="appeal-meta">
                <span class="appeal-date">
                    <i class="fas fa-calendar"></i>
                    Submitted: ${formatDate(appeal.createdAt)}
                </span>
            </div>
        </div>
    `).join('');
}

// Banned servers management
function loadBannedServers() {
    const bannedServersGrid = document.getElementById('bannedServersGrid');
    if (!bannedServersGrid) return;
    
    // Show loading state
    bannedServersGrid.innerHTML = '<div class="loading-state">Loading banned servers...</div>';
    
    // Simulate API call
    setTimeout(() => {
        const mockBannedServers = [
            {
                serverId: '123456789',
                serverName: 'Gaming Community',
                serverIcon: 'https://cdn.discordapp.com/icons/123456789/icon.png',
                banReason: 'Inappropriate behavior',
                bannedAt: '2023-11-25T16:30:00Z',
                bannedBy: 'ModeratorBot#1234',
                canAppeal: true,
                hasActiveAppeal: false
            },
            {
                serverId: '987654321',
                serverName: 'Study Group',
                serverIcon: 'https://cdn.discordapp.com/icons/987654321/icon.png',
                banReason: 'Spam',
                bannedAt: '2023-11-20T12:15:00Z',
                bannedBy: 'Admin#5678',
                canAppeal: true,
                hasActiveAppeal: true
            }
        ];
        
        renderBannedServers(mockBannedServers);
    }, 1000);
}

function renderBannedServers(servers) {
    const bannedServersGrid = document.getElementById('bannedServersGrid');
    if (!bannedServersGrid) return;
    
    if (servers.length === 0) {
        bannedServersGrid.innerHTML = `
            <div class="no-data">
                <i class="fas fa-check-circle"></i>
                <p>You are not banned from any servers!</p>
            </div>
        `;
        return;
    }
    
    bannedServersGrid.innerHTML = servers.map(server => `
        <div class="banned-server-card">
            <div class="server-header">
                <img src="${server.serverIcon}" alt="${server.serverName}" class="server-icon">
                <div class="server-info">
                    <h3>${server.serverName}</h3>
                    <span class="server-id">${server.serverId}</span>
                </div>
            </div>
            
            <div class="ban-details">
                <div class="ban-reason">
                    <i class="fas fa-exclamation-triangle"></i>
                    <strong>Reason:</strong> ${server.banReason}
                </div>
                
                <div class="ban-meta">
                    <div class="ban-date">
                        <i class="fas fa-calendar"></i>
                        Banned: ${formatDate(server.bannedAt)}
                    </div>
                    <div class="banned-by">
                        <i class="fas fa-user"></i>
                        By: ${server.bannedBy}
                    </div>
                </div>
            </div>
            
            <div class="server-actions">
                ${server.canAppeal ? 
                    server.hasActiveAppeal ? 
                        '<button class="btn btn-secondary btn-sm" disabled><i class="fas fa-clock"></i> Appeal Pending</button>' :
                        `<button class="btn btn-primary btn-sm" onclick="createAppealForServer('${server.serverId}', '${server.serverName}')"><i class="fas fa-gavel"></i> Submit Appeal</button>`
                    : 
                    '<button class="btn btn-secondary btn-sm" disabled><i class="fas fa-ban"></i> Cannot Appeal</button>'
                }
            </div>
        </div>
    `).join('');
}

// Modal functions
function showAppealModal() {
    const modal = document.getElementById('appealModal');
    if (modal) {
        modal.style.display = 'flex';
        loadBannedServersForDropdown();
    }
}

function hideModal(modal) {
    if (modal) {
        modal.style.display = 'none';
    }
}

function showResponseModal(appealId) {
    const modal = document.getElementById('responseModal');
    if (modal) {
        modal.style.display = 'flex';
        loadAppealDetails(appealId);
    }
}

// Appeal handling functions
function handleAppealSubmission(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const appealData = {
        serverId: document.getElementById('appealServer').value,
        reason: document.getElementById('appealReason').value,
        apology: document.getElementById('appealApology').value
    };
    
    // Show loading state
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
    submitBtn.disabled = true;
    
    // Simulate API call
    setTimeout(() => {
        // Reset form
        e.target.reset();
        
        // Hide modal
        hideModal(document.getElementById('appealModal'));
        
        // Show success message
        showNotification('Appeal submitted successfully!', 'success');
        
        // Refresh appeals if on that page
        loadUserAppeals();
        
        // Reset button
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }, 1500);
}

function handleAppealResponse(action) {
    const responseReason = document.getElementById('responseReason').value;
    const modal = document.getElementById('responseModal');
    const appealId = modal.dataset.appealId;
    
    if (!appealId) return;
    
    // Show loading state
    const buttons = modal.querySelectorAll('button');
    buttons.forEach(btn => btn.disabled = true);
    
    // Simulate API call
    setTimeout(() => {
        // Hide modal
        hideModal(modal);
        
        // Show success message
        showNotification(`Appeal ${action} successfully!`, 'success');
        
        // Refresh appeals
        loadAdminAppeals();
        
        // Reset buttons
        buttons.forEach(btn => btn.disabled = false);
    }, 1500);
}

// Helper functions
function loadBannedServersForDropdown() {
    const serverSelect = document.getElementById('appealServer');
    if (!serverSelect) return;
    
    // Simulate API call
    setTimeout(() => {
        const mockServers = [
            { id: '123456789', name: 'Gaming Community' },
            { id: '987654321', name: 'Study Group' }
        ];
        
        serverSelect.innerHTML = '<option value="">Vælg server...</option>' + 
            mockServers.map(server => `<option value="${server.id}">${server.name}</option>`).join('');
    }, 500);
}

function loadAppealDetails(appealId) {
    const modal = document.getElementById('responseModal');
    const appealDetails = document.getElementById('appealDetails');
    
    if (!appealDetails) return;
    
    modal.dataset.appealId = appealId;
    
    // Show loading state
    appealDetails.innerHTML = '<div class="loading-state">Loading appeal details...</div>';
    
    // Simulate API call
    setTimeout(() => {
        const mockAppeal = {
            id: appealId,
            username: 'BannedUser#1234',
            userId: '123456789',
            serverName: 'Gaming Community',
            reason: 'I was banned for spam, but I believe it was a misunderstanding.',
            apology: 'I apologize for any confusion and will be more careful in the future.',
            createdAt: '2023-12-01T10:30:00Z'
        };
        
        appealDetails.innerHTML = `
            <div class="appeal-detail-card">
                <div class="appeal-user">
                    <strong>User:</strong> ${mockAppeal.username} (${mockAppeal.userId})
                </div>
                <div class="appeal-server">
                    <strong>Server:</strong> ${mockAppeal.serverName}
                </div>
                <div class="appeal-reason">
                    <strong>Reason:</strong>
                    <p>${mockAppeal.reason}</p>
                </div>
                ${mockAppeal.apology ? `
                    <div class="appeal-apology">
                        <strong>Apology:</strong>
                        <p>${mockAppeal.apology}</p>
                    </div>
                ` : ''}
                <div class="appeal-date">
                    <strong>Submitted:</strong> ${formatDate(mockAppeal.createdAt)}
                </div>
            </div>
        `;
    }, 500);
}

function filterAppeals() {
    const filter = document.getElementById('appealStatusFilter').value;
    const appeals = document.querySelectorAll('.appeal-card');
    
    appeals.forEach(appeal => {
        if (filter === 'all' || appeal.classList.contains(filter)) {
            appeal.style.display = 'block';
        } else {
            appeal.style.display = 'none';
        }
    });
}

function respondToAppeal(appealId, action) {
    const modal = document.getElementById('responseModal');
    modal.dataset.appealId = appealId;
    showResponseModal(appealId);
}

function createAppealForServer(serverId, serverName) {
    const modal = document.getElementById('appealModal');
    const serverSelect = document.getElementById('appealServer');
    
    if (serverSelect) {
        // Pre-select the server
        serverSelect.innerHTML = `<option value="${serverId}" selected>${serverName}</option>`;
    }
    
    showAppealModal();
}

function getStatusText(status) {
    const statusMap = {
        'pending': 'Pending',
        'approved': 'Approved',
        'denied': 'Denied'
    };
    return statusMap[status] || status;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('da-DK', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check' : 'info'}-circle"></i>
        <span>${message}</span>
        <button class="notification-close">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Show notification
    setTimeout(() => notification.classList.add('show'), 100);
    
    // Auto hide after 5 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 5000);
    
    // Close button
    notification.querySelector('.notification-close').addEventListener('click', () => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    });
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

let updateStatsInterval;
let updateActivityInterval;
let isPageVisible = true;

// Track page visibility to pause updates when not visible
document.addEventListener('visibilitychange', function() {
    isPageVisible = !document.hidden;
    if (isPageVisible) {
        setupRealTimeUpdates();
    } else {
        clearRealTimeUpdates();
    }
});

function setupRealTimeUpdates() {
    // Clear existing intervals to prevent duplicates
    clearRealTimeUpdates();
    
    // Only update if page is visible
    if (!isPageVisible) return;
    
    // Reduced frequency: Update stats every 2 minutes, activity every 30 seconds
    updateStatsInterval = setInterval(updateStats, 120000); // Update every 2 minutes
    updateActivityInterval = setInterval(updateActivity, 30000); // Update activity every 30 seconds
}

function clearRealTimeUpdates() {
    if (updateStatsInterval) {
        clearInterval(updateStatsInterval);
        updateStatsInterval = null;
    }
    if (updateActivityInterval) {
        clearInterval(updateActivityInterval);
        updateActivityInterval = null;
    }
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
    // Use requestAnimationFrame for better performance
    const range = end - start;
    const startTime = performance.now();
    
    function animate(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // Use easing function for smoother animation
        const easeProgress = 1 - Math.pow(1 - progress, 3);
        const current = start + (range * easeProgress);
        
        element.textContent = Math.floor(current).toLocaleString();
        
        if (progress < 1) {
            requestAnimationFrame(animate);
        }
    }
    
    requestAnimationFrame(animate);
}

function updateActivity() {
    // Only update if page is visible and activity feed exists
    if (!isPageVisible) return;
    
    const activityFeed = document.querySelector('.activity-feed');
    if (!activityFeed) return;
    
    // Reduced frequency: 20% chance to add new activity (was 30%)
    if (Math.random() > 0.8) {
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
        
        const randomActivity = activities[Math.floor(Math.random() * activities.length)];
        const newItem = createActivityItem(randomActivity);
        
        // Use DocumentFragment for better performance
        const fragment = document.createDocumentFragment();
        fragment.appendChild(newItem);
        activityFeed.insertBefore(fragment, activityFeed.firstChild);
        
        // Remove old items if more than 8 (reduced from 10)
        const items = activityFeed.querySelectorAll('.activity-item');
        if (items.length > 8) {
            // Remove multiple items at once for better performance
            const itemsToRemove = Array.from(items).slice(8);
            itemsToRemove.forEach(item => item.remove());
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
