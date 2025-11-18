// Customer Reviews JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Modal elements
    const editModal = document.getElementById('editReviewModal');
    const deleteModal = document.getElementById('deleteReviewModal');
    const editForm = document.getElementById('editReviewForm');
    const deleteForm = document.getElementById('deleteReviewForm');

    // Edit review buttons
    const editButtons = document.querySelectorAll('.edit-review-btn');
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            const reviewId = this.dataset.reviewId;
            const rating = this.dataset.rating;
            const title = this.dataset.title || '';
            const comment = this.dataset.comment || '';

            // Set form action
            editForm.action = `/reviews/update/${reviewId}`;

            // Set form values
            document.getElementById('editTitle').value = title;
            document.getElementById('editComment').value = comment;

            // Set rating
            const ratingInput = document.querySelector(`input[name="rating"][value="${rating}"]`);
            if (ratingInput) {
                ratingInput.checked = true;
            }

            // Show modal
            editModal.classList.add('active');
        });
    });

    // Delete review buttons
    const deleteButtons = document.querySelectorAll('.delete-review-btn');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const reviewId = this.dataset.reviewId;

            // Set form action
            deleteForm.action = `/reviews/delete/${reviewId}`;

            // Show modal
            deleteModal.classList.add('active');
        });
    });

    // Close modal buttons
    const closeButtons = document.querySelectorAll('.modal-close');
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            editModal.classList.remove('active');
            deleteModal.classList.remove('active');
        });
    });

    // Close modal on outside click
    window.addEventListener('click', function(event) {
        if (event.target === editModal) {
            editModal.classList.remove('active');
        }
        if (event.target === deleteModal) {
            deleteModal.classList.remove('active');
        }
    });

    // Auto-hide flash messages after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => {
                alert.remove();
            }, 300);
        }, 5000);
    });
});
