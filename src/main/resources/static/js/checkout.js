// Checkout page JavaScript

// Payment method selection
document.addEventListener('DOMContentLoaded', function() {
    const paymentOptions = document.querySelectorAll('.payment-option');

    paymentOptions.forEach(option => {
        option.addEventListener('click', function() {
            // Remove selected class from all options
            paymentOptions.forEach(opt => opt.classList.remove('selected'));

            // Add selected class to clicked option
            this.classList.add('selected');

            // Check the radio button
            const radio = this.querySelector('input[type="radio"]');
            if (radio) {
                radio.checked = true;
            }
        });
    });
});

// Form validation
function validateCheckoutForm(form) {
    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            isValid = false;
            field.style.borderColor = '#ef4444';
        } else {
            field.style.borderColor = '#d1d5db';
        }
    });

    if (!isValid) {
        alert('Please fill in all required fields');
        return false;
    }

    // Validate email format
    const emailField = form.querySelector('input[type="email"]');
    if (emailField) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailField.value)) {
            alert('Please enter a valid email address');
            emailField.style.borderColor = '#ef4444';
            return false;
        }
    }

    // Validate phone format
    const phoneField = form.querySelector('input[type="tel"]');
    if (phoneField && phoneField.value) {
        const phoneRegex = /^[\d\s\-\+\(\)]+$/;
        if (!phoneRegex.test(phoneField.value)) {
            alert('Please enter a valid phone number');
            phoneField.style.borderColor = '#ef4444';
            return false;
        }
    }

    return true;
}

// Handle form submission
function handleCheckoutSubmit(event) {
    const form = event.target;

    if (!validateCheckoutForm(form)) {
        event.preventDefault();
        return false;
    }

    // Disable submit button to prevent double submission
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    }

    return true;
}

// Auto-fill from saved data (if implemented)
function loadSavedAddress() {
    // Implementation for loading saved addresses
    console.log('Load saved address');
}

// Calculate shipping based on address (if implemented)
function calculateShipping() {
    // Implementation for shipping calculation
    console.log('Calculate shipping');
}
