package com.example.ecoswap.repository;

import com.example.ecoswap.model.Review;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a product
    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    // Find all reviews for a product (by ID)
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Find approved reviews for a product
    List<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(Long productId);

    // Find all reviews by a customer
    List<Review> findByCustomerOrderByCreatedAtDesc(User customer);

    // Find all reviews by a customer (by ID)
    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Find reviews for products owned by a specific seller
    @Query("SELECT r FROM Review r WHERE r.product.seller.id = :sellerId ORDER BY r.createdAt DESC")
    List<Review> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

    // Check if customer has already reviewed a product
    Optional<Review> findByProductIdAndCustomerId(Long productId, Long customerId);

    // Find verified reviews for a product
    List<Review> findByProductIdAndVerifiedTrueOrderByCreatedAtDesc(Long productId);

    // Find reviews pending approval
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();

    // Get average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Count approved reviews for a product
    Long countByProductIdAndApprovedTrue(Long productId);

    // Find recent reviews across all products
    List<Review> findTop10ByApprovedTrueOrderByCreatedAtDesc();
}
