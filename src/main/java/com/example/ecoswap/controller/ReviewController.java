package com.example.ecoswap.controller;

import com.example.ecoswap.model.Review;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.services.ReviewService;
import com.example.ecoswap.services.ProductService;
import com.example.ecoswap.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    /**
     * Submit a new review for a product
     */
    @PostMapping("/submit")
    public String submitReview(
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = authentication.getName();
            User customer = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // Create review
            Review review = new Review();
            review.setProduct(product);
            review.setCustomer(customer);
            review.setRating(rating);
            review.setTitle(title);
            review.setComment(comment);

            reviewService.createReview(review);

            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit review. Please try again.");
        }

        return "redirect:/product/" + productId;
    }

    /**
     * Update an existing review
     */
    @PostMapping("/update/{reviewId}")
    public String updateReview(
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = authentication.getName();
            User customer = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            // Verify ownership
            if (!review.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalStateException("You can only edit your own reviews");
            }

            reviewService.updateReview(reviewId, title, comment, rating);

            redirectAttributes.addFlashAttribute("success", "Review updated successfully!");
            return "redirect:/customer/reviews";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update review. Please try again.");
            return "redirect:/customer/reviews";
        }
    }

    /**
     * Delete a review
     */
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = authentication.getName();
            User customer = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            // Verify ownership
            if (!review.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalStateException("You can only delete your own reviews");
            }

            reviewService.deleteReview(reviewId);

            redirectAttributes.addFlashAttribute("success", "Review deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete review. Please try again.");
        }

        return "redirect:/customer/reviews";
    }

    /**
     * Mark a review as helpful
     */
    @PostMapping("/helpful/{reviewId}")
    @ResponseBody
    public String markHelpful(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.markReviewHelpful(reviewId);
            return String.valueOf(review.getHelpfulCount());
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * Seller: Approve a review
     */
    @PostMapping("/seller/approve/{reviewId}")
    public String approveReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = authentication.getName();
            User seller = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            // Verify that the review is for seller's product
            if (!review.getProduct().getSeller().getId().equals(seller.getId())) {
                throw new IllegalStateException("You can only moderate reviews for your own products");
            }

            reviewService.approveReview(reviewId);

            redirectAttributes.addFlashAttribute("success", "Review approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve review: " + e.getMessage());
        }

        return "redirect:/seller/reviews";
    }

    /**
     * Seller: Reject a review
     */
    @PostMapping("/seller/reject/{reviewId}")
    public String rejectReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = authentication.getName();
            User seller = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            // Verify that the review is for seller's product
            if (!review.getProduct().getSeller().getId().equals(seller.getId())) {
                throw new IllegalStateException("You can only moderate reviews for your own products");
            }

            reviewService.rejectReview(reviewId);

            redirectAttributes.addFlashAttribute("success", "Review rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject review: " + e.getMessage());
        }

        return "redirect:/seller/reviews";
    }
}
