// Search categories
function searchCategories() {
    const searchTerm = document.getElementById('categorySearch').value.toLowerCase();
    const cards = document.querySelectorAll('.category-card');
    
    cards.forEach(card => {
        const name = card.querySelector('.category-name').textContent.toLowerCase();
        const description = card.querySelector('.category-description').textContent.toLowerCase();
        
        if (name.includes(searchTerm) || description.includes(searchTerm)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// Edit category
function editCategory(id) {
    window.location.href = '/dashboard/categories/' + id + '/edit';
}

// Delete category
function deleteCategory(id, name, productCount) {
    if (productCount > 0) {
        alert('Cannot delete category "' + name + '" because it has ' + productCount + ' products. Please reassign or delete the products first.');
        return;
    }
    
    if (confirm('Are you sure you want to delete the category "' + name + '"? This action cannot be undone.')) {
        fetch('/dashboard/categories/' + id, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Category deleted successfully!', 'success');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                alert('Error: ' + (data.message || 'Failed to delete category'));
            }
        })
        .catch(error => {
            alert('Error deleting category');
            console.error('Error:', error);
        });
    }
}

// Toggle category status
function toggleCategoryStatus(id, isActive) {
    const action = isActive ? 'activate' : 'deactivate';
    
    fetch('/dashboard/categories/' + id + '/' + action, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (response.ok) {
            // Update the status badge
            const card = document.querySelector(`[data-category-id="${id}"]`);
            const badge = card.querySelector('.status-badge');
            badge.textContent = isActive ? 'Active' : 'Inactive';
            badge.className = 'status-badge ' + (isActive ? 'active' : 'inactive');
            
            showToast(isActive ? 'Category activated!' : 'Category deactivated!', 'success');
        } else {
            showToast('Error updating category status', 'error');
            // Revert checkbox
            document.getElementById('toggle-' + id).checked = !isActive;
        }
    })
    .catch(error => {
        showToast('Error updating category status', 'error');
        console.error('Error:', error);
        // Revert checkbox
        document.getElementById('toggle-' + id).checked = !isActive;
    });
}

// Toast notification
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(toast);
    setTimeout(() => toast.classList.add('show'), 10);
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Auto-hide alerts on page load
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert[data-auto-hide]');
    alerts.forEach(alert => {
        const delay = parseInt(alert.getAttribute('data-auto-hide')) || 5000;
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, delay);
    });
});
