/**
 * Settings Page JavaScript
 * Handles tab switching and real-time previews
 */

document.addEventListener('DOMContentLoaded', function() {
    // Tab switching functionality
    initTabSwitching();

    // Currency preview
    initCurrencyPreview();

    // Color picker sync
    initColorPickers();

    // Form validation
    initFormValidation();
});

/**
 * Initialize tab switching
 */
function initTabSwitching() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabId = button.getAttribute('data-tab');

            // Remove active class from all buttons and panes
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabPanes.forEach(pane => pane.classList.remove('active'));

            // Add active class to clicked button and corresponding pane
            button.classList.add('active');
            const targetPane = document.getElementById(tabId);
            if (targetPane) {
                targetPane.classList.add('active');
            }
        });
    });
}

/**
 * Initialize currency preview
 */
function initCurrencyPreview() {
    const currencySymbol = document.getElementById('currencySymbol');
    const currencyPosition = document.getElementById('currencyPosition');
    const decimalPlaces = document.getElementById('decimalPlaces');
    const pricePreview = document.getElementById('pricePreview');

    if (!currencySymbol || !currencyPosition || !decimalPlaces || !pricePreview) {
        return;
    }

    const updatePreview = () => {
        const symbol = currencySymbol.value || '$';
        const position = currencyPosition.value;
        const decimals = parseInt(decimalPlaces.value) || 2;
        const amount = (99.99).toFixed(decimals);

        if (position === 'after') {
            pricePreview.textContent = amount + ' ' + symbol;
        } else {
            pricePreview.textContent = symbol + amount;
        }
    };

    // Update preview on input changes
    currencySymbol.addEventListener('input', updatePreview);
    currencyPosition.addEventListener('change', updatePreview);
    decimalPlaces.addEventListener('input', updatePreview);

    // Initial update
    updatePreview();
}

/**
 * Initialize color picker synchronization
 */
function initColorPickers() {
    // Primary color
    const primaryColorPicker = document.querySelector('#primaryColor[type="color"]');
    const primaryColorText = document.querySelector('#primaryColor[type="text"]');

    if (primaryColorPicker && primaryColorText) {
        syncColorInputs(primaryColorPicker, primaryColorText);
    }

    // Secondary color
    const secondaryColorPicker = document.querySelector('#secondaryColor[type="color"]');
    const secondaryColorText = document.querySelector('#secondaryColor[type="text"]');

    if (secondaryColorPicker && secondaryColorText) {
        syncColorInputs(secondaryColorPicker, secondaryColorText);
    }
}

/**
 * Sync color picker and text input
 */
function syncColorInputs(colorPicker, textInput) {
    colorPicker.addEventListener('input', (e) => {
        textInput.value = e.target.value;
    });

    textInput.addEventListener('input', (e) => {
        const value = e.target.value;
        if (/^#[0-9A-F]{6}$/i.test(value)) {
            colorPicker.value = value;
        }
    });
}

/**
 * Initialize form validation
 */
function initFormValidation() {
    const form = document.querySelector('.settings-form');

    if (form) {
        form.addEventListener('submit', function(e) {
            // Validate shop name
            const shopName = document.getElementById('shopName');
            if (shopName && shopName.value.trim() === '') {
                e.preventDefault();
                alert('Shop name is required!');
                shopName.focus();
                // Switch to general tab
                document.querySelector('[data-tab="general"]').click();
                return false;
            }

            // Validate currency code
            const currencyCode = document.getElementById('currencyCode');
            if (currencyCode && currencyCode.value.trim() === '') {
                e.preventDefault();
                alert('Currency code is required!');
                currencyCode.focus();
                // Switch to currency tab
                document.querySelector('[data-tab="currency"]').click();
                return false;
            }

            // Validate currency symbol
            const currencySymbol = document.getElementById('currencySymbol');
            if (currencySymbol && currencySymbol.value.trim() === '') {
                e.preventDefault();
                alert('Currency symbol is required!');
                currencySymbol.focus();
                // Switch to currency tab
                document.querySelector('[data-tab="currency"]').click();
                return false;
            }

            // Show loading indicator
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span>⏳</span> Saving...';
            }

            return true;
        });
    }
}

/**
 * Confirm before resetting settings
 */
function confirmReset() {
    return confirm('Are you sure you want to reset all settings to default values? This action cannot be undone.');
}
