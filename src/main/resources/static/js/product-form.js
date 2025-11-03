
// Image preview
function previewImage(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('imagePreview');
            preview.innerHTML = `<img src="${e.target.result}" alt="Preview" id="previewImg">`;
        }
        reader.readAsDataURL(file);
    }
}

// Eco score display
function updateEcoDisplay(value) {
    const display = document.getElementById('ecoDisplay');
    const labels = ['Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];
    const label = labels[value - 1];
    
    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= value) {
            starsHtml += '<i class="fas fa-leaf"></i>';
        } else {
            starsHtml += '<i class="far fa-leaf"></i>';
        }
    }
    
    display.innerHTML = `
        <div class="eco-stars">${starsHtml}</div>
        <span class="eco-label">${label}</span>
    `;
}

// Initialize eco display on page load
document.addEventListener('DOMContentLoaded', function() {
    const ecoScore = document.getElementById('ecoScore');
    if (ecoScore) {
        updateEcoDisplay(ecoScore.value);
    }
});

// Form validation
document.querySelector('.product-form').addEventListener('submit', function(e) {
    const price = parseFloat(document.getElementById('price').value);
    const originalPrice = parseFloat(document.getElementById('originalPrice').value);
    
    if (originalPrice && originalPrice < price) {
        e.preventDefault();
        alert('Original price cannot be less than selling price');
        return false;
    }
});
