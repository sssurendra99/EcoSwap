
// Image preview from URL
function previewImageFromUrl(url) {
    const preview = document.getElementById('imagePreview');
    const placeholder = document.getElementById('placeholder');

    if (url && url.trim() !== '') {
        // Hide placeholder if it exists
        if (placeholder) {
            placeholder.style.display = 'none';
        }

        // Show image preview
        const existingImg = document.getElementById('previewImg');
        if (existingImg) {
            existingImg.src = url;
            existingImg.style.display = 'block';
        } else {
            const newImg = document.createElement('img');
            newImg.id = 'previewImg';
            newImg.src = url;
            newImg.alt = 'Product Image';
            newImg.style.maxWidth = '100%';
            newImg.style.height = 'auto';
            newImg.style.borderRadius = '8px';
            newImg.style.marginTop = '1rem';

            // Handle image load error
            newImg.onerror = function() {
                this.style.display = 'none';
                if (placeholder) {
                    placeholder.style.display = 'block';
                }
                alert('Failed to load image. Please check the URL.');
            };

            preview.appendChild(newImg);
        }
    } else {
        // Show placeholder if URL is empty
        const img = document.getElementById('previewImg');
        if (img) {
            img.style.display = 'none';
        }
        if (placeholder) {
            placeholder.style.display = 'block';
        }
    }
}

// Image preview from file upload
function previewImage(event) {
    const file = event.target.files[0];
    const fileNameSpan = document.getElementById('fileName');
    const preview = document.getElementById('imagePreview');
    const placeholder = document.getElementById('placeholder');
    const previewImg = document.getElementById('previewImg');

    if (file) {
        // Validate file type
        if (!file.type.startsWith('image/')) {
            alert('Please select an image file');
            event.target.value = '';
            return;
        }

        // Validate file size (5MB max)
        const maxSize = 5 * 1024 * 1024;
        if (file.size > maxSize) {
            alert('File size must not exceed 5MB');
            event.target.value = '';
            return;
        }

        // Update file name display
        if (fileNameSpan) {
            fileNameSpan.textContent = file.name;
        }

        // Create preview
        const reader = new FileReader();
        reader.onload = function(e) {
            // Hide placeholder if it exists
            if (placeholder) {
                placeholder.style.display = 'none';
            }

            // Update or create image preview
            if (previewImg) {
                previewImg.src = e.target.result;
                previewImg.style.display = 'block';
            } else {
                const newImg = document.createElement('img');
                newImg.id = 'previewImg';
                newImg.src = e.target.result;
                newImg.alt = 'Product Preview';
                newImg.style.maxWidth = '100%';
                newImg.style.height = 'auto';
                newImg.style.borderRadius = '4px';
                newImg.style.marginTop = '1rem';
                preview.appendChild(newImg);
            }
        };
        reader.readAsDataURL(file);
    } else {
        // Reset to default state
        if (fileNameSpan) {
            fileNameSpan.textContent = 'Choose Image File';
        }
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

    // Form validation
    const productForm = document.querySelector('.product-form');
    if (productForm) {
        productForm.addEventListener('submit', function(e) {
            const price = parseFloat(document.getElementById('price').value);
            const originalPrice = parseFloat(document.getElementById('originalPrice').value);

            if (originalPrice && originalPrice < price) {
                e.preventDefault();
                alert('Original price cannot be less than selling price');
                return false;
            }
        });
    }

    // Debug: Log checkbox states on change
    const checkboxes = document.querySelectorAll('.checkbox-label input[type="checkbox"]');
    checkboxes.forEach(checkbox => {
        console.log('Checkbox found:', checkbox.name, 'Checked:', checkbox.checked);

        checkbox.addEventListener('change', function() {
            console.log('Checkbox changed:', this.name, 'Checked:', this.checked);
        });
    });
});
