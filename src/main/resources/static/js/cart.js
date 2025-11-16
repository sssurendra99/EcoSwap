// Cart page JavaScript

function incrementQuantity(btn) {
    const input = btn.previousElementSibling;
    const max = parseInt(input.max);
    const current = parseInt(input.value);
    if (current < max) {
        input.value = current + 1;
        input.form.submit();
    } else {
        alert('Maximum stock reached');
    }
}

function decrementQuantity(btn) {
    const input = btn.nextElementSibling;
    const current = parseInt(input.value);
    if (current > 1) {
        input.value = current - 1;
        input.form.submit();
    }
}

// Optional: Add AJAX functionality for smoother updates
function updateCartAjax(cartItemId, quantity) {
    fetch(`/cart/update-ajax/${cartItemId}?quantity=${quantity}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update cart total and item count in UI
            location.reload(); // Simple reload for now
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to update cart');
    });
}

// Confirm before clearing cart
function confirmClearCart() {
    return confirm('Are you sure you want to clear your entire cart?');
}
