// Debug script for commands page
console.log('Commands page debug script loaded');

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded');
    
    // Check if elements exist
    const hero = document.querySelector('.commands-hero');
    const stats = document.querySelectorAll('.stat-number');
    const navbar = document.querySelector('.navbar');
    
    console.log('Hero element:', hero);
    console.log('Stats elements:', stats);
    console.log('Navbar element:', navbar);
    
    // Simple counter animation
    if (stats.length > 0) {
        stats.forEach(stat => {
            const target = parseInt(stat.getAttribute('data-target'));
            if (target) {
                let current = 0;
                const increment = target / 100;
                const timer = setInterval(() => {
                    current += increment;
                    if (current >= target) {
                        stat.textContent = target;
                        clearInterval(timer);
                    } else {
                        stat.textContent = Math.floor(current);
                    }
                }, 20);
            }
        });
    }
    
    // Basic navigation functionality
    const hamburger = document.getElementById('hamburger');
    const navMenu = document.querySelector('.nav-menu');
    
    if (hamburger && navMenu) {
        hamburger.addEventListener('click', function() {
            navMenu.classList.toggle('active');
            hamburger.classList.toggle('active');
        });
    }
    
    console.log('Debug script completed');
});
