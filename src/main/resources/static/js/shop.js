// Shop Page JavaScript

// Quick add to cart function
function quickAddToCart(button, event) {
    event.preventDefault();
    event.stopPropagation();

    const productId = button.getAttribute('data-product-id');
    const originalText = button.innerHTML;

    // Disable button and show loading state
    button.disabled = true;
    button.innerHTML = '⏳ Adding...';

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
            // Show success state
            button.innerHTML = '✓ Added!';
            button.classList.add('success');

            // Update cart count in navbar if it exists
            updateCartCount(data.itemCount);

            // Reset button after 2 seconds
            setTimeout(() => {
                button.innerHTML = originalText;
                button.disabled = false;
                button.classList.remove('success');
            }, 2000);
        } else {
            // Show error
            alert(data.message || 'Failed to add item to cart');
            button.innerHTML = originalText;
            button.disabled = false;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred. Please try again.');
        button.innerHTML = originalText;
        button.disabled = false;
    });
}

// Update cart count in navbar
function updateCartCount(count) {
    const cartBadge = document.querySelector('.cart-count, .cart-badge');
    if (cartBadge) {
        cartBadge.textContent = count;
        cartBadge.style.display = count > 0 ? 'inline-block' : 'none';
    }
}
