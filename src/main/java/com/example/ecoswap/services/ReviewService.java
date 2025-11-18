package com.example.ecoswap.services;

import com.example.ecoswap.model.Review;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.Order;
import com.example.ecoswap.repository.ReviewRepository;
import com.example.ecoswap.repository.ProductRepository;
import com.example.ecoswap.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Create a new review
     */
    @Transactional
    public Review createReview(Review review) {
        // Check if customer has already reviewed this product
        Optional<Review> existingReview = reviewRepository.findByProductIdAndCustomerId(
            review.getProduct().getId(),
            review.getCustomer().getId()
        );

        if (existingReview.isPresent()) {
            throw new IllegalStateException("You have already reviewed this product");
        }

        // Verify if purchase is verified (customer bought the product)
        boolean hasOrderedProduct = hasCustomerOrderedProduct(
            review.getCustomer().getId(),
            review.getProduct().getId()
        );
        review.setVerified(hasOrderedProduct);

        // Save the review
        Review savedReview = reviewRepository.save(review);

        // Update product rating and review count
        updateProductRating(review.getProduct().getId());

        return savedReview;
    }

    /**
     * Update an existing review
     */
    @Transactional
    public Review updateReview(Long reviewId, String title, String comment, Integer rating) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.isEditable()) {
            throw new IllegalStateException("Review can only be edited within 30 days of creation");
        }

        review.setTitle(title);
        review.setComment(comment);
        review.setRating(rating);
        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        return updatedReview;
    }

    /**
     * Delete a review
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        Long productId = review.getProduct().getId();

        reviewRepository.delete(review);

        // Update product rating
        updateProductRating(productId);
    }

    /**
     * Get all reviews for a product
     */
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrueOrderByCreatedAtDesc(productId);
    }

    /**
     * Get all reviews by a customer
     */
    public List<Review> getCustomerReviews(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get all reviews for products owned by a seller
     */
    public List<Review> getSellerProductReviews(Long sellerId) {
        return reviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * Get a review by ID
     */
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    /**
     * Check if customer has already reviewed a product
     */
    public boolean hasCustomerReviewedProduct(Long customerId, Long productId) {
        return reviewRepository.findByProductIdAndCustomerId(productId, customerId).isPresent();
    }

    /**
     * Approve a review (admin/seller moderation)
     */
    @Transactional
    public Review approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setApproved(true);
        Review approvedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        return approvedReview;
    }

    /**
     * Reject a review (admin/seller moderation)
     */
    @Transactional
    public Review rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setApproved(false);
        return reviewRepository.save(review);
    }

    /**
     * Mark a review as helpful
     */
    @Transactional
    public Review markReviewHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        return reviewRepository.save(review);
    }

    /**
     * Get pending reviews (for moderation)
     */
    public List<Review> getPendingReviews() {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }

    /**
     * Update product rating and review count
     */
    @Transactional
    public void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        Long reviewCount = reviewRepository.countByProductIdAndApprovedTrue(productId);

        product.setRating(averageRating != null ? averageRating : 0.0);
        product.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);

        productRepository.save(product);
    }

    /**
     * Check if customer has ordered a product (for verified purchase badge)
     */
    private boolean hasCustomerOrderedProduct(Long customerId, Long productId) {
        // This is a simplified check - you may want to add more conditions
        // like checking if the order was delivered successfully
        return orderRepository.existsByCustomerIdAndOrderItems_ProductId(customerId, productId);
    }
}
