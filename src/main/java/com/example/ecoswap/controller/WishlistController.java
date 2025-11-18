package com.example.ecoswap.controller;

import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    /**
     * View wishlist
     */
    @GetMapping
    public String viewWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = userDetails.getUser();
        List<Product> wishlistProducts = wishlistService.getWishlistProducts(user.getId());

        model.addAttribute("wishlistProducts", wishlistProducts);
        model.addAttribute("wishlistCount", wishlistProducts.size());
        model.addAttribute("pageTitle", "My Wishlist");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        return "customer/wishlist";
    }

    /**
     * Add product to wishlist
     */
    @PostMapping("/add")
    public String addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            wishlistService.addToWishlist(user, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to wishlist!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/wishlist";
    }

    /**
     * Add product to wishlist via AJAX
     */
    @PostMapping("/add-ajax")
    @ResponseBody
    public java.util.Map<String, Object> addToWishlistAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId
    ) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        User user = userDetails.getUser();

        try {
            wishlistService.addToWishlist(user, productId);
            int itemCount = wishlistService.getWishlistItemCount(user.getId());
            response.put("success", true);
            response.put("itemCount", itemCount);
            response.put("message", "Added to wishlist");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Remove product from wishlist
     */
    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            wishlistService.removeFromWishlist(user, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product removed from wishlist!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/wishlist";
    }

    /**
     * Remove product from wishlist via AJAX
     */
    @DeleteMapping("/remove-ajax/{productId}")
    @ResponseBody
    public java.util.Map<String, Object> removeFromWishlistAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        User user = userDetails.getUser();

        try {
            wishlistService.removeFromWishlist(user, productId);
            int itemCount = wishlistService.getWishlistItemCount(user.getId());
            response.put("success", true);
            response.put("itemCount", itemCount);
            response.put("message", "Removed from wishlist");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Clear wishlist
     */
    @PostMapping("/clear")
    public String clearWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            wishlistService.clearWishlist(user);
            redirectAttributes.addFlashAttribute("successMessage", "Wishlist cleared!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/wishlist";
    }

    /**
     * Get wishlist count (for navbar badge)
     */
    @GetMapping("/count")
    @ResponseBody
    public String getWishlistCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        int count = wishlistService.getWishlistItemCount(user.getId());
        return "{\"count\": " + count + "}";
    }

    /**
     * Check if product is in wishlist
     */
    @GetMapping("/check/{productId}")
    @ResponseBody
    public String checkWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        User user = userDetails.getUser();
        boolean isInWishlist = wishlistService.isInWishlist(user.getId(), productId);
        return "{\"isInWishlist\": " + isInWishlist + "}";
    }
}
