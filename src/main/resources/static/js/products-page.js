// View switching
function switchView(view) {
    const gridView = document.getElementById('gridView');
    const tableView = document.getElementById('tableView');
    const gridBtn = document.getElementById('gridViewBtn');
    const tableBtn = document.getElementById('tableViewBtn');
    
    if (view === 'grid') {
        gridView.style.display = 'grid';
        tableView.style.display = 'none';
        gridBtn.classList.add('active');
        tableBtn.classList.remove('active');
        localStorage.setItem('productView', 'grid');
    } else {
        gridView.style.display = 'none';
        tableView.style.display = 'block';
        tableBtn.classList.add('active');
        gridBtn.classList.remove('active');
        localStorage.setItem('productView', 'table');
    }
}

// Restore view preference
document.addEventListener('DOMContentLoaded', function() {
    const savedView = localStorage.getItem('productView');
    if (savedView) {
        switchView(savedView);
    }
});

// Search functionality
document.getElementById('productSearch').addEventListener('input', function(e) {
    const searchTerm = e.target.value.toLowerCase();
    filterProducts();
});

// Filter functionality
document.querySelectorAll('.filter-select').forEach(select => {
    select.addEventListener('change', filterProducts);
});

function filterProducts() {
    const searchTerm = document.getElementById('productSearch').value.toLowerCase();
    const category = document.getElementById('categoryFilter').value;
    const status = document.getElementById('statusFilter').value;
    const ecoRating = document.getElementById('ecoRatingFilter').value;
    
    // Add AJAX call here to filter products on server side
    console.log('Filtering:', { searchTerm, category, status, ecoRating });
}

function resetFilters() {
    document.getElementById('productSearch').value = '';
    document.getElementById('categoryFilter').value = '';
    document.getElementById('statusFilter').value = '';
    document.getElementById('ecoRatingFilter').value = '';
    filterProducts();
}

// Product actions
function viewProduct(id) {
    window.location.href = '/dashboard/products/' + id;
}

function editProduct(id) {
    window.location.href = '/dashboard/products/' + id + '/edit';
}

function deleteProduct(id, name) {
    if (confirm('Are you sure you want to delete "' + name + '"? This action cannot be undone.')) {
        fetch('/dashboard/products/' + id, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert('Error deleting product');
            }
        });
    }
}

// Select all checkbox
function toggleSelectAll() {
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.product-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAll.checked;
    });
    updateBulkActions();
}

// Update bulk actions visibility
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.product-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', updateBulkActions);
    });
});

function updateBulkActions() {
    const checkedBoxes = document.querySelectorAll('.product-checkbox:checked');
    const bulkActions = document.getElementById('bulkActions');
    const selectedCount = document.getElementById('selectedCount');
    
    if (checkedBoxes.length > 0) {
        bulkActions.style.display = 'flex';
        selectedCount.textContent = checkedBoxes.length;
    } else {
        bulkActions.style.display = 'none';
    }
}

function clearSelection() {
    document.querySelectorAll('.product-checkbox').forEach(checkbox => {
        checkbox.checked = false;
    });
    document.getElementById('selectAll').checked = false;
    updateBulkActions();
}

// Bulk actions
function bulkActivate() {
    const selected = getSelectedIds();
    if (selected.length === 0) return;
    
    // Add AJAX call here
    console.log('Activating products:', selected);
}

function bulkDeactivate() {
    const selected = getSelectedIds();
    if (selected.length === 0) return;
    
    // Add AJAX call here
    console.log('Deactivating products:', selected);
}

function bulkDelete() {
    const selected = getSelectedIds();
    if (selected.length === 0) return;
    
    if (confirm('Are you sure you want to delete ' + selected.length + ' products? This action cannot be undone.')) {
        // Add AJAX call here
        console.log('Deleting products:', selected);
    }
}

function getSelectedIds() {
    const checkboxes = document.querySelectorAll('.product-checkbox:checked');
    return Array.from(checkboxes).map(cb => cb.value);
}

// Pagination
function changePage(page) {
    window.location.href = '/dashboard/products?page=' + page;
}
