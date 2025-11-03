const productId = /*[[${product.id}]]*/ 0;

// Tab switching
function switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tabName + 'Tab').classList.add('active');
    event.target.classList.add('active');
}

// Delete functionality
function deleteProduct(id) {
    document.getElementById('deleteModal').style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
}

function confirmDelete() {
    fetch('/dashboard/products/' + productId, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/dashboard/products';
        } else {
            alert('Error deleting product');
            closeDeleteModal();
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error deleting product');
        closeDeleteModal();
    });
}

// Close modal on outside click
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target === modal) {
        closeDeleteModal();
    }
}
