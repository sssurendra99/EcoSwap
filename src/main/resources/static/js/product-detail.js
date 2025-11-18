// Product Detail Page JavaScript

// Increment quantity
function incrementQuantity() {
    const input = document.getElementById('quantity');
    const max = parseInt(input.max);
    const current = parseInt(input.value);

    if (current < max) {
        input.value = current + 1;
    }
}

// Decrement quantity
function decrementQuantity() {
    const input = document.getElementById('quantity');
    const min = parseInt(input.min);
    const current = parseInt(input.value);

    if (current > min) {
        input.value = current - 1;
    }
}

// Validate quantity on input change
document.addEventListener('DOMContentLoaded', function() {
    const quantityInput = document.getElementById('quantity');

    if (quantityInput) {
        quantityInput.addEventListener('change', function() {
            const min = parseInt(this.min);
            const max = parseInt(this.max);
            let value = parseInt(this.value);

            // Validate bounds
            if (value < min) {
                this.value = min;
            } else if (value > max) {
                this.value = max;
            } else if (isNaN(value)) {
                this.value = min;
            }
        });
    }

    // Review Form Handling
    const writeReviewBtn = document.getElementById('writeReviewBtn');
    const reviewFormContainer = document.getElementById('reviewFormContainer');
    const cancelReviewBtn = document.getElementById('cancelReviewBtn');

    if (writeReviewBtn && reviewFormContainer) {
        writeReviewBtn.addEventListener('click', function(e) {
            e.preventDefault();
            reviewFormContainer.style.display = 'block';
            writeReviewBtn.style.display = 'none';
            setTimeout(() => {
                reviewFormContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 100);
        });
    }

    // Star Rating Interactivity
    const starRatingContainer = document.querySelector('.star-rating-input');
    let resetStarColors = null;

    if (starRatingContainer) {
        const starInputs = starRatingContainer.querySelectorAll('input[type="radio"]');
        const starLabels = starRatingContainer.querySelectorAll('label');

        // Function to reset all stars to default color
        resetStarColors = function() {
            starLabels.forEach(label => {
                label.classList.remove('active');
            });
        };

        // Function to highlight stars based on rating value
        function highlightStars(rating) {
            resetStarColors();
            // Highlight all stars with value <= rating
            starInputs.forEach((input) => {
                const label = input.nextElementSibling;
                if (label && parseInt(input.value) <= rating) {
                    label.classList.add('active');
                }
            });
        }

        // Add click handlers to labels
        starLabels.forEach((label, index) => {
            label.addEventListener('click', function(e) {
                e.preventDefault();
                // Find the associated radio button
                const inputId = this.getAttribute('for');
                const input = document.getElementById(inputId);
                if (input) {
                    input.checked = true;
                    const rating = parseInt(input.value);
                    highlightStars(rating);
                }
            });

            // Add hover effect
            label.addEventListener('mouseenter', function() {
                const inputId = this.getAttribute('for');
                const input = document.getElementById(inputId);
                if (input) {
                    const rating = parseInt(input.value);
                    highlightStars(rating);
                }
            });
        });

        // Reset to selected rating on mouse leave
        starRatingContainer.addEventListener('mouseleave', function() {
            const checkedInput = starRatingContainer.querySelector('input[type="radio"]:checked');
            if (checkedInput) {
                const rating = parseInt(checkedInput.value);
                highlightStars(rating);
            } else {
                resetStarColors();
            }
        });
    }

    // Cancel review button
    if (cancelReviewBtn && reviewFormContainer && writeReviewBtn) {
        cancelReviewBtn.addEventListener('click', function() {
            reviewFormContainer.style.display = 'none';
            writeReviewBtn.style.display = 'inline-flex';
            // Reset form
            const form = document.querySelector('.review-form');
            if (form) {
                form.reset();
                // Also reset star colors if function exists
                if (resetStarColors) {
                    resetStarColors();
                }
            }
        });
    }

    // Wishlist Form Handling
    const wishlistForm = document.getElementById('wishlistForm');
    if (wishlistForm) {
        wishlistForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = new FormData(this);
            const productId = formData.get('productId');

            fetch('/customer/wishlist/add-ajax', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `productId=${productId}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Replace form with success message
                    const wishlistAction = document.querySelector('.wishlist-action');
                    wishlistAction.innerHTML = `
                        <div class="wishlist-status">
                            <i class="fas fa-heart"></i> In your wishlist
                            <a href="/customer/wishlist" class="view-wishlist-link">View Wishlist</a>
                        </div>
                    `;
                } else {
                    alert(data.message || 'Failed to add to wishlist');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred. Please try again.');
            });
        });
    }

    // Helpful buttons
    const helpfulButtons = document.querySelectorAll('.btn-helpful');
    helpfulButtons.forEach(button => {
        button.addEventListener('click', function() {
            const reviewId = this.dataset.reviewId;

            fetch(`/reviews/helpful/${reviewId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.text())
            .then(data => {
                if (data !== 'error') {
                    this.innerHTML = `<i class="fas fa-thumbs-up"></i> Helpful (${data})`;
                    this.disabled = true;
                    this.style.opacity = '0.6';
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
        });
    });
});
