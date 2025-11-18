// Seller Reviews JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Filter tabs
    const filterTabs = document.querySelectorAll('.tab-btn');
    const reviewCards = document.querySelectorAll('.review-card');

    filterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // Remove active class from all tabs
            filterTabs.forEach(t => t.classList.remove('active'));
            // Add active class to clicked tab
            this.classList.add('active');

            const filter = this.dataset.filter;

            // Filter reviews
            reviewCards.forEach(card => {
                if (filter === 'all') {
                    card.style.display = 'block';
                } else if (filter === 'approved') {
                    card.style.display = card.dataset.status === 'approved' ? 'block' : 'none';
                } else if (filter === 'pending') {
                    card.style.display = card.dataset.status === 'pending' ? 'block' : 'none';
                } else if (filter === 'verified') {
                    card.style.display = card.dataset.verified === 'true' ? 'block' : 'none';
                }
            });
        });
    });

    // Approve review buttons
    const approveButtons = document.querySelectorAll('.approve-btn');
    const approveForm = document.getElementById('approveReviewForm');

    approveButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (confirm('Are you sure you want to approve this review?')) {
                const reviewId = this.dataset.reviewId;
                approveForm.action = `/reviews/seller/approve/${reviewId}`;
                approveForm.submit();
            }
        });
    });

    // Reject review buttons
    const rejectButtons = document.querySelectorAll('.reject-btn');
    const rejectForm = document.getElementById('rejectReviewForm');

    rejectButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (confirm('Are you sure you want to reject this review? It will be hidden from public view.')) {
                const reviewId = this.dataset.reviewId;
                rejectForm.action = `/reviews/seller/reject/${reviewId}`;
                rejectForm.submit();
            }
        });
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
