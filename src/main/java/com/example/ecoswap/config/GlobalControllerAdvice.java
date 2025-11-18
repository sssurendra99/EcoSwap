package com.example.ecoswap.config;

import com.example.ecoswap.model.Settings;
import com.example.ecoswap.model.User;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.CartService;
import com.example.ecoswap.services.SettingsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to make settings and cart count available to all templates
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private static final String SESSION_CART_KEY = "guestCart";

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private CartService cartService;

    /**
     * Add site settings to all models
     */
    @ModelAttribute
    public void addSettingsToModel(Model model) {
        try {
            Settings settings = settingsService.getSettings();
            model.addAttribute("siteSettings", settings);
        } catch (Exception e) {
            // In case of error, create default settings
            model.addAttribute("siteSettings", new Settings());
        }
    }

    /**
     * Add cart item count to all pages
     * Supports both authenticated users (database cart) and guests (session cart)
     */
    @ModelAttribute("cartItemCount")
    public Integer addCartItemCount(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            // Authenticated user - get count from database
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();
            return cartService.getCartItemCount(user.getId());
        } else {
            // Guest user - get count from session
            return getSessionCartItemCount(session);
        }
    }

    /**
     * Get cart item count from session for guest users
     */
    private int getSessionCartItemCount(HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<com.example.ecoswap.model.CartItem> cart =
            (java.util.List<com.example.ecoswap.model.CartItem>) session.getAttribute(SESSION_CART_KEY);

        if (cart == null || cart.isEmpty()) {
            return 0;
        }

        return cart.stream()
            .mapToInt(com.example.ecoswap.model.CartItem::getQuantity)
            .sum();
    }
}
