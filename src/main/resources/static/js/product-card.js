/**
 * Product Card - Add to Cart Functionality
 * Handles adding products to cart via AJAX
 */

/**
 * Add product to cart
 * @param {number} productId - The ID of the product to add
 * @param {HTMLElement} button - The button element that was clicked
 */
function addToCart(productId, button) {
    // Prevent multiple clicks
    if (button.classList.contains('adding') || button.classList.contains('added')) {
        return;
    }

    // Update button state to "adding"
    button.classList.add('adding');
    const originalContent = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> <span class="btn-text">Adding...</span>';

    // Make AJAX request to add to cart
    fetch('/cart/add-ajax', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `productId=${productId}&quantity=1`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update button to show success
            button.classList.remove('adding');
            button.classList.add('added');
            button.innerHTML = '<i class="fas fa-check"></i> <span class="btn-text">Added to Cart!</span>';

            // Update cart count in header
            updateCartCount(data.itemCount);

            // Show success notification
            showNotification('Product added to cart successfully!', 'success');

            // Reset button after 2 seconds
            setTimeout(() => {
                button.classList.remove('added');
                button.innerHTML = originalContent;
            }, 2000);
        } else {
            // Show error
            button.classList.remove('adding');
            button.innerHTML = originalContent;
            showNotification(data.message || 'Failed to add product to cart', 'error');
        }
    })
    .catch(error => {
        console.error('Error adding to cart:', error);
        button.classList.remove('adding');
        button.innerHTML = originalContent;
        showNotification('An error occurred. Please try again.', 'error');
    });
}

/**
 * Update cart count in header
 * @param {number} count - New cart item count
 */
function updateCartCount(count) {
    // Update all cart badges on the page
    const cartBadges = document.querySelectorAll('.cart-badge');
    cartBadges.forEach(badge => {
        if (count > 0) {
            badge.textContent = count;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    });
}

/**
 * Show notification message
 * @param {string} message - Message to display
 * @param {string} type - Notification type (success, error, info)
 */
function showNotification(message, type = 'info') {
    // Check if notification container exists, create if not
    let container = document.getElementById('notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 10px;
        `;
        document.body.appendChild(container);
    }

    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;

    // Set notification styles based on type
    const colors = {
        success: { bg: '#d1fae5', text: '#065f46', border: '#10b981' },
        error: { bg: '#fee2e2', text: '#991b1b', border: '#ef4444' },
        info: { bg: '#dbeafe', text: '#1e40af', border: '#3b82f6' }
    };

    const color = colors[type] || colors.info;

    notification.style.cssText = `
        background: ${color.bg};
        color: ${color.text};
        border-left: 4px solid ${color.border};
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        min-width: 300px;
        max-width: 400px;
        animation: slideIn 0.3s ease-out;
        display: flex;
        align-items: center;
        gap: 10px;
        font-weight: 500;
    `;

    // Add icon based on type
    const icons = {
        success: '<i class="fas fa-check-circle"></i>',
        error: '<i class="fas fa-exclamation-circle"></i>',
        info: '<i class="fas fa-info-circle"></i>'
    };

    notification.innerHTML = `
        ${icons[type] || icons.info}
        <span>${message}</span>
    `;

    // Add to container
    container.appendChild(notification);

    // Auto-remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-in';
        setTimeout(() => {
            notification.remove();
            // Remove container if empty
            if (container.children.length === 0) {
                container.remove();
            }
        }, 300);
    }, 3000);
}

// Add animation styles
if (!document.getElementById('notification-animations')) {
    const style = document.createElement('style');
    style.id = 'notification-animations';
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(400px);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(400px);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}
