// Generate slug from name
function generateSlug() {
    const nameInput = document.getElementById('name');
    const slugInput = document.getElementById('slug');
    
    if (!nameInput || !slugInput) return;
    
    const name = nameInput.value;
    const slug = name.toLowerCase()
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-|-$/g, '');
    
    slugInput.value = slug;
    updatePreview();
}

// Update preview
function updatePreview() {
    const nameInput = document.getElementById('name');
    const slugInput = document.getElementById('slug');
    const colorInput = document.getElementById('color');
    const iconInput = document.getElementById('icon');
    const previewName = document.getElementById('previewName');
    const previewSlug = document.getElementById('previewSlug');
    const categoryPreview = document.getElementById('categoryPreview');
    const previewIcon = document.getElementById('previewIcon');
    
    if (!nameInput || !slugInput || !colorInput || !iconInput) return;
    
    const name = nameInput.value || 'Category Name';
    const slug = slugInput.value || 'category-slug';
    const color = colorInput.value;
    const icon = iconInput.value || 'fa-leaf';
    
    if (previewName) previewName.textContent = name;
    if (previewSlug) previewSlug.textContent = '/' + slug;
    if (categoryPreview) {
        categoryPreview.style.background = `linear-gradient(135deg, ${color} 0%, ${color}dd 100%)`;
    }
    if (previewIcon) previewIcon.className = 'fas ' + icon;
}

// Set color from preset
function setColor(color) {
    const colorInput = document.getElementById('color');
    const colorHexInput = document.getElementById('colorHex');
    
    if (colorInput) colorInput.value = color;
    if (colorHexInput) colorHexInput.value = color;
    updatePreview();
}

// Update color from hex input
function updateColorFromHex() {
    const hexInput = document.getElementById('colorHex');
    const colorInput = document.getElementById('color');
    
    if (!hexInput || !colorInput) return;
    
    let hex = hexInput.value;
    
    // Add # if missing
    if (!hex.startsWith('#')) {
        hex = '#' + hex;
    }
    
    // Validate hex color
    if (/^#[0-9A-F]{6}$/i.test(hex)) {
        colorInput.value = hex;
        hexInput.value = hex;
        updatePreview();
    }
}

// Select icon
function selectIcon(iconClass) {
    const iconInput = document.getElementById('icon');
    if (iconInput) iconInput.value = iconClass;
    
    // Remove active class from all icons
    document.querySelectorAll('.icon-option').forEach(opt => {
        opt.classList.remove('active');
    });
    
    // Add active class to selected icon
    const selectedIcon = document.querySelector(`[data-icon="${iconClass}"]`);
    if (selectedIcon) selectedIcon.classList.add('active');
    
    updatePreview();
}

// Filter icons
function filterIcons() {
    const searchInput = document.getElementById('iconSearch');
    if (!searchInput) return;
    
    const searchTerm = searchInput.value.toLowerCase();
    const icons = document.querySelectorAll('.icon-option');
    
    icons.forEach(icon => {
        const iconName = icon.getAttribute('data-icon').toLowerCase();
        if (iconName.includes(searchTerm)) {
            icon.style.display = 'flex';
        } else {
            icon.style.display = 'none';
        }
    });
}

// Initialize form on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the form page
    const categoryForm = document.querySelector('.category-form');
    if (!categoryForm) return;
    
    // Set initial active icon
    const iconInput = document.getElementById('icon');
    if (iconInput) {
        const currentIcon = iconInput.value || 'fa-leaf';
        const iconElement = document.querySelector(`[data-icon="${currentIcon}"]`);
        if (iconElement) {
            iconElement.classList.add('active');
        }
    }
    
    // Update preview with existing data
    updatePreview();
    
    // Update hex input with current color
    const colorInput = document.getElementById('color');
    const colorHexInput = document.getElementById('colorHex');
    if (colorInput && colorHexInput) {
        colorHexInput.value = colorInput.value;
    }
    
    // Add color input listener
    if (colorInput) {
        colorInput.addEventListener('input', function() {
            const colorHex = document.getElementById('colorHex');
            if (colorHex) colorHex.value = this.value;
            updatePreview();
        });
    }
    
    // Form validation
    categoryForm.addEventListener('submit', function(e) {
        const name = document.getElementById('name').value.trim();
        const slug = document.getElementById('slug').value.trim();
        const color = document.getElementById('color').value;
        const icon = document.getElementById('icon').value;
        
        if (!name || !slug || !color || !icon) {
            e.preventDefault();
            alert('Please fill in all required fields');
            return false;
        }
        
        // Validate slug format
        const slugRegex = /^[a-z0-9-]+$/;
        if (!slugRegex.test(slug)) {
            e.preventDefault();
            alert('Slug can only contain lowercase letters, numbers, and hyphens');
            return false;
        }
    });
});