// Dashboard Layout JavaScript

document.addEventListener('DOMContentLoaded', function() {
    
    // ===== SIDEBAR TOGGLE =====
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileToggle = document.getElementById('mobileToggle');

    // Desktop sidebar toggle
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            sidebar.classList.toggle('collapsed');
            mainContent.classList.toggle('expanded');
            
            // Save state to localStorage
            const isCollapsed = sidebar.classList.contains('collapsed');
            localStorage.setItem('sidebarCollapsed', isCollapsed);
        });
    }

    // Mobile sidebar toggle
    if (mobileToggle) {
        mobileToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
            
            // Add overlay
            if (sidebar.classList.contains('active')) {
                const overlay = document.createElement('div');
                overlay.className = 'sidebar-overlay';
                document.body.appendChild(overlay);
                
                // Close sidebar when clicking overlay
                overlay.addEventListener('click', function() {
                    sidebar.classList.remove('active');
                    overlay.remove();
                });
            }
        });
    }

    // Restore sidebar state from localStorage
    const sidebarCollapsed = localStorage.getItem('sidebarCollapsed');
    if (sidebarCollapsed === 'true' && window.innerWidth > 1024) {
        sidebar.classList.add('collapsed');
        mainContent.classList.add('expanded');
    }

    // ===== ACTIVE NAV LINK =====
    const navLinks = document.querySelectorAll('.nav-link');
    const currentPath = window.location.pathname;

    navLinks.forEach(link => {
        const linkPath = new URL(link.href).pathname;
        
        if (currentPath === linkPath || currentPath.startsWith(linkPath + '/')) {
            link.closest('.nav-item').classList.add('active');
        }

        link.addEventListener('click', function() {
            navLinks.forEach(l => l.closest('.nav-item').classList.remove('active'));
            this.closest('.nav-item').classList.add('active');
        });
    });

    // ===== DROPDOWN MENUS =====
    
    // Close dropdowns when clicking outside
    document.addEventListener('click', function(event) {
        const notificationDropdown = document.querySelector('.notification-dropdown');
        const userDropdown = document.querySelector('.user-dropdown');
        
        if (notificationDropdown && !notificationDropdown.contains(event.target)) {
            notificationDropdown.querySelector('.notification-menu')?.classList.remove('show');
        }
        
        if (userDropdown && !userDropdown.contains(event.target)) {
            userDropdown.querySelector('.user-menu')?.classList.remove('show');
        }
    });

    // ===== SEARCH BOX =====
    const searchInput = document.querySelector('.search-box input');
    
    if (searchInput) {
        searchInput.addEventListener('focus', function() {
            this.closest('.search-box').classList.add('focused');
        });

        searchInput.addEventListener('blur', function() {
            this.closest('.search-box').classList.remove('focused');
        });

        // Search functionality (you can customize this)
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            
            // Add your search logic here
            console.log('Searching for:', searchTerm);
        });
    }

    // ===== NOTIFICATION MARK AS READ =====
    const markReadBtn = document.querySelector('.mark-read');
    
    if (markReadBtn) {
        markReadBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            const unreadNotifications = document.querySelectorAll('.notification-item.unread');
            unreadNotifications.forEach(notif => {
                notif.classList.remove('unread');
            });

            // Update notification count
            const notificationBadge = document.querySelector('.notification-badge');
            if (notificationBadge) {
                notificationBadge.textContent = '0';
                notificationBadge.style.display = 'none';
            }

            // You can add AJAX call here to update on server
            console.log('All notifications marked as read');
        });
    }

    // ===== SMOOTH SCROLL =====
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href !== '#') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });

    // ===== ANIMATIONS ON SCROLL =====
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Observe stat cards, impact cards, etc.
    const animatedElements = document.querySelectorAll('.stat-card, .impact-card, .action-card');
    animatedElements.forEach(el => observer.observe(el));

    // ===== COUNTER ANIMATION =====
    function animateCounter(element, target, duration = 2000) {
        const start = 0;
        const increment = target / (duration / 16);
        let current = start;

        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                element.textContent = formatNumber(target);
                clearInterval(timer);
            } else {
                element.textContent = formatNumber(Math.floor(current));
            }
        }, 16);
    }

    function formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    // Animate counters when they come into view
    const counterObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const statValue = entry.target.querySelector('.stat-value');
                if (statValue) {
                    const text = statValue.textContent.replace(/[^0-9]/g, '');
                    const number = parseInt(text);
                    if (!isNaN(number)) {
                        animateCounter(statValue, number);
                    }
                }
                counterObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });

    document.querySelectorAll('.stat-card').forEach(card => {
        counterObserver.observe(card);
    });

    // ===== RESPONSIVE HANDLING =====
    function handleResize() {
        const width = window.innerWidth;
        
        if (width <= 768) {
            sidebar.classList.remove('collapsed');
            mainContent.classList.remove('expanded');
        } else if (width > 1024) {
            sidebar.classList.remove('active');
            const overlay = document.querySelector('.sidebar-overlay');
            if (overlay) overlay.remove();
        }
    }

    window.addEventListener('resize', handleResize);

    // ===== TOOLTIP =====
    const tooltipTriggers = document.querySelectorAll('[data-tooltip]');
    
    tooltipTriggers.forEach(trigger => {
        trigger.addEventListener('mouseenter', function() {
            const tooltip = document.createElement('div');
            tooltip.className = 'tooltip';
            tooltip.textContent = this.getAttribute('data-tooltip');
            document.body.appendChild(tooltip);

            const rect = this.getBoundingClientRect();
            tooltip.style.top = rect.top - tooltip.offsetHeight - 10 + 'px';
            tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';

            setTimeout(() => tooltip.classList.add('show'), 10);

            this._tooltip = tooltip;
        });

        trigger.addEventListener('mouseleave', function() {
            if (this._tooltip) {
                this._tooltip.classList.remove('show');
                setTimeout(() => this._tooltip.remove(), 300);
            }
        });
    });

    // ===== FORM VALIDATION HELPER =====
    const forms = document.querySelectorAll('form[data-validate]');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const requiredFields = form.querySelectorAll('[required]');
            let isValid = true;

            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.classList.add('error');
                    
                    // Remove error class on input
                    field.addEventListener('input', function() {
                        this.classList.remove('error');
                    }, { once: true });
                }
            });

            if (!isValid) {
                e.preventDefault();
                // Show error message
                showToast('Please fill in all required fields', 'error');
            }
        });
    });

    // ===== TOAST NOTIFICATION =====
    window.showToast = function(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => toast.classList.add('show'), 10);
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, duration);
    };

    // ===== CONFIRM DIALOG =====
    window.confirmAction = function(message, callback) {
        const confirmed = confirm(message);
        if (confirmed && typeof callback === 'function') {
            callback();
        }
        return confirmed;
    };

    // ===== AUTO-HIDE ALERTS =====
    const alerts = document.querySelectorAll('.alert[data-auto-hide]');
    alerts.forEach(alert => {
        const delay = parseInt(alert.getAttribute('data-auto-hide')) || 5000;
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, delay);
    });

    // ===== LOADING STATE =====
    window.setLoading = function(element, isLoading) {
        if (isLoading) {
            element.classList.add('loading');
            element.disabled = true;
            element._originalText = element.textContent;
            element.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';
        } else {
            element.classList.remove('loading');
            element.disabled = false;
            element.textContent = element._originalText || 'Submit';
        }
    };

    // ===== COPY TO CLIPBOARD =====
    window.copyToClipboard = function(text) {
        navigator.clipboard.writeText(text).then(() => {
            showToast('Copied to clipboard!', 'success');
        }).catch(() => {
            showToast('Failed to copy', 'error');
        });
    };

    // ===== PRINT PAGE =====
    window.printPage = function() {
        window.print();
    };

    // ===== EXPORT TABLE =====
    window.exportTable = function(tableId, filename = 'data.csv') {
        const table = document.getElementById(tableId);
        if (!table) return;

        let csv = [];
        const rows = table.querySelectorAll('tr');

        rows.forEach(row => {
            const cols = row.querySelectorAll('td, th');
            const rowData = Array.from(cols).map(col => `"${col.textContent.trim()}"`);
            csv.push(rowData.join(','));
        });

        const csvContent = csv.join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);

        showToast('Table exported successfully!', 'success');
    };

    console.log('Dashboard Layout Initialized ✅');
});

// ===== UTILITY FUNCTIONS =====

// Format currency
function formatCurrency(amount, currency = 'USD') {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

// Format date
function formatDate(date, format = 'short') {
    const options = format === 'long' 
        ? { year: 'numeric', month: 'long', day: 'numeric' }
        : { year: 'numeric', month: 'short', day: 'numeric' };
    
    return new Intl.DateTimeFormat('en-US', options).format(new Date(date));
}

// Debounce function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}