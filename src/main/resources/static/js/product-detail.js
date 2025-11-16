// Product Detail Page JavaScript

// Increment quantity
function incrementQuantity() {
    const input = document.getElementById('quantity');
    const max = parseInt(input.max);
    const current = parseInt(input.value);

    if (current < max) {
        input.value = current + 1;
    }
}

// Decrement quantity
function decrementQuantity() {
    const input = document.getElementById('quantity');
    const min = parseInt(input.min);
    const current = parseInt(input.value);

    if (current > min) {
        input.value = current - 1;
    }
}

// Validate quantity on input change
document.addEventListener('DOMContentLoaded', function() {
    const quantityInput = document.getElementById('quantity');

    if (quantityInput) {
        quantityInput.addEventListener('change', function() {
            const min = parseInt(this.min);
            const max = parseInt(this.max);
            let value = parseInt(this.value);

            // Validate bounds
            if (value < min) {
                this.value = min;
            } else if (value > max) {
                this.value = max;
            } else if (isNaN(value)) {
                this.value = min;
            }
        });
    }
});
