// Debug script for appeal system
console.log('Debug: Appeal system loading...');

// Check if all required elements exist
document.addEventListener('DOMContentLoaded', function() {
    console.log('Debug: DOM loaded, checking elements...');
    
    // Check navigation elements
    const appealLink = document.querySelector('a[href="#appeals"]');
    const myAppealsLink = document.querySelector('a[href="#my-appeals"]');
    const bannedServersLink = document.querySelector('a[href="#banned-servers"]');
    
    console.log('Appeal link:', appealLink);
    console.log('My appeals link:', myAppealsLink);
    console.log('Banned servers link:', bannedServersLink);
    
    // Check content sections
    const appealsSection = document.getElementById('appeals-section');
    const myAppealsSection = document.getElementById('my-appeals-section');
    const bannedServersSection = document.getElementById('banned-servers-section');
    
    console.log('Appeals section:', appealsSection);
    console.log('My appeals section:', myAppealsSection);
    console.log('Banned servers section:', bannedServersSection);
    
    // Check modals
    const appealModal = document.getElementById('appealModal');
    const responseModal = document.getElementById('responseModal');
    
    console.log('Appeal modal:', appealModal);
    console.log('Response modal:', responseModal);
    
    // Add click listeners for debugging
    if (appealLink) {
        appealLink.addEventListener('click', function(e) {
            console.log('Debug: Appeal link clicked');
            e.preventDefault();
            showAppealsSection();
        });
    }
    
    if (myAppealsLink) {
        myAppealsLink.addEventListener('click', function(e) {
            console.log('Debug: My appeals link clicked');
            e.preventDefault();
            showMyAppealsSection();
        });
    }
    
    if (bannedServersLink) {
        bannedServersLink.addEventListener('click', function(e) {
            console.log('Debug: Banned servers link clicked');
            e.preventDefault();
            showBannedServersSection();
        });
    }
    
    // Test functions
    window.debugShowAppeals = function() {
        console.log('Debug: Showing appeals section');
        showAppealsSection();
    };
    
    window.debugShowMyAppeals = function() {
        console.log('Debug: Showing my appeals section');
        showMyAppealsSection();
    };
    
    window.debugShowBannedServers = function() {
        console.log('Debug: Showing banned servers section');
        showBannedServersSection();
    };
    
    window.debugShowAppealModal = function() {
        console.log('Debug: Showing appeal modal');
        const modal = document.getElementById('appealModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    };
});

function showAppealsSection() {
    console.log('Debug: showAppealsSection called');
    hideAllSections();
    const section = document.getElementById('appeals-section');
    if (section) {
        section.style.display = 'block';
        loadAdminAppeals();
    }
}

function showMyAppealsSection() {
    console.log('Debug: showMyAppealsSection called');
    hideAllSections();
    const section = document.getElementById('my-appeals-section');
    if (section) {
        section.style.display = 'block';
        loadUserAppeals();
    }
}

function showBannedServersSection() {
    console.log('Debug: showBannedServersSection called');
    hideAllSections();
    const section = document.getElementById('banned-servers-section');
    if (section) {
        section.style.display = 'block';
        loadBannedServers();
    }
}

function hideAllSections() {
    console.log('Debug: Hiding all sections');
    const sections = [
        'appeals-section',
        'my-appeals-section', 
        'banned-servers-section'
    ];
    
    sections.forEach(id => {
        const section = document.getElementById(id);
        if (section) {
            section.style.display = 'none';
        }
    });
    
    // Also hide main dashboard content
    const dashboardContent = document.querySelector('.dashboard-content');
    if (dashboardContent) {
        dashboardContent.style.display = 'none';
    }
}

function loadAdminAppeals() {
    console.log('Debug: Loading admin appeals...');
    const appealsGrid = document.getElementById('appealsGrid');
    if (!appealsGrid) {
        console.error('Debug: Appeals grid not found');
        return;
    }
    
    // Show loading state
    appealsGrid.innerHTML = '<div class="loading-state">Loading appeals...</div>';
    
    // Mock data
    setTimeout(() => {
        const mockAppeals = [
            {
                id: 1,
                userId: '123456789',
                username: 'BannedUser#1234',
                avatar: 'https://cdn.discordapp.com/avatars/123456789/avatar.png',
                serverId: '987654321',
                serverName: 'Gaming Community',
                reason: 'I was banned for spam, but I believe it was a misunderstanding.',
                apology: 'I apologize for any confusion and will be more careful in the future.',
                status: 'pending',
                createdAt: '2023-12-01T10:30:00Z'
            }
        ];
        
        appealsGrid.innerHTML = mockAppeals.map(appeal => `
            <div class="appeal-card ${appeal.status}">
                <div class="appeal-header">
                    <div class="appeal-user">
                        <img src="${appeal.avatar}" alt="${appeal.username}" class="user-avatar">
                        <div class="user-info">
                            <span class="username">${appeal.username}</span>
                            <span class="user-id">${appeal.userId}</span>
                        </div>
                    </div>
                    <span class="appeal-status status-${appeal.status}">Pending</span>
                </div>
                
                <div class="appeal-server">
                    <i class="fas fa-server"></i>
                    <span>${appeal.serverName}</span>
                </div>
                
                <div class="appeal-content">
                    <h4>Reason:</h4>
                    <p>${appeal.reason}</p>
                    
                    <h4>Apology:</h4>
                    <p>${appeal.apology}</p>
                </div>
                
                <div class="appeal-meta">
                    <span class="appeal-date">
                        <i class="fas fa-calendar"></i>
                        1. december 2023 10:30
                    </span>
                </div>
                
                <div class="appeal-actions">
                    <button class="btn btn-success btn-sm">
                        <i class="fas fa-check"></i>
                        Approve
                    </button>
                    <button class="btn btn-danger btn-sm">
                        <i class="fas fa-times"></i>
                        Deny
                    </button>
                </div>
            </div>
        `).join('');
        
        console.log('Debug: Admin appeals loaded');
    }, 1000);
}

function loadUserAppeals() {
    console.log('Debug: Loading user appeals...');
    const userAppealsList = document.getElementById('userAppealsList');
    if (!userAppealsList) {
        console.error('Debug: User appeals list not found');
        return;
    }
    
    userAppealsList.innerHTML = '<div class="loading-state">Loading your appeals...</div>';
    
    setTimeout(() => {
        userAppealsList.innerHTML = `
            <div class="user-appeal-item status-pending">
                <div class="appeal-header">
                    <div class="appeal-server">
                        <i class="fas fa-server"></i>
                        <span>Gaming Community</span>
                    </div>
                    <span class="appeal-status status-pending">Pending</span>
                </div>
                
                <div class="appeal-content">
                    <p>I was banned for using inappropriate language, but I believe the punishment was too harsh.</p>
                </div>
                
                <div class="appeal-meta">
                    <span class="appeal-date">
                        <i class="fas fa-calendar"></i>
                        Submitted: 1. december 2023 10:30
                    </span>
                </div>
            </div>
        `;
        
        console.log('Debug: User appeals loaded');
    }, 1000);
}

function loadBannedServers() {
    console.log('Debug: Loading banned servers...');
    const bannedServersGrid = document.getElementById('bannedServersGrid');
    if (!bannedServersGrid) {
        console.error('Debug: Banned servers grid not found');
        return;
    }
    
    bannedServersGrid.innerHTML = '<div class="loading-state">Loading banned servers...</div>';
    
    setTimeout(() => {
        bannedServersGrid.innerHTML = `
            <div class="banned-server-card">
                <div class="server-header">
                    <img src="https://cdn.discordapp.com/icons/123456789/icon.png" alt="Server" class="server-icon">
                    <div class="server-info">
                        <h3>Gaming Community</h3>
                        <span class="server-id">123456789</span>
                    </div>
                </div>
                
                <div class="ban-details">
                    <div class="ban-reason">
                        <i class="fas fa-exclamation-triangle"></i>
                        <strong>Reason:</strong> Inappropriate behavior
                    </div>
                    
                    <div class="ban-meta">
                        <div class="ban-date">
                            <i class="fas fa-calendar"></i>
                            Banned: 25. november 2023 16:30
                        </div>
                        <div class="banned-by">
                            <i class="fas fa-user"></i>
                            By: ModeratorBot#1234
                        </div>
                    </div>
                </div>
                
                <div class="server-actions">
                    <button class="btn btn-primary btn-sm" onclick="debugShowAppealModal()">
                        <i class="fas fa-gavel"></i>
                        Submit Appeal
                    </button>
                </div>
            </div>
        `;
        
        console.log('Debug: Banned servers loaded');
    }, 1000);
}

console.log('Debug: Appeal system debug script loaded');
