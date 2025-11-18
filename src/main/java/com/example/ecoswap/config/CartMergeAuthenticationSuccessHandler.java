package com.example.ecoswap.config;

import com.example.ecoswap.model.CartItem;
import com.example.ecoswap.model.User;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.security.RoleBasedAuthSuccessHandler;
import com.example.ecoswap.services.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Handles cart merging when a guest user logs in
 */
@Component
public class CartMergeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String SESSION_CART_KEY = "guestCart";
    private final RoleBasedAuthSuccessHandler roleBasedHandler;
    private final CartService cartService;

    public CartMergeAuthenticationSuccessHandler(
            RoleBasedAuthSuccessHandler roleBasedHandler,
            CartService cartService
    ) {
        this.roleBasedHandler = roleBasedHandler;
        this.cartService = cartService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        // Get session cart if exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<CartItem> sessionCart = (List<CartItem>) session.getAttribute(SESSION_CART_KEY);

            if (sessionCart != null && !sessionCart.isEmpty()) {
                // Get logged in user
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                User user = userDetails.getUser();

                // Merge session cart with database cart
                for (CartItem item : sessionCart) {
                    try {
                        cartService.addToCart(user, item.getProduct().getId(), item.getQuantity());
                    } catch (Exception e) {
                        // Log error but continue with other items
                        System.err.println("Error merging cart item: " + e.getMessage());
                    }
                }

                // Clear session cart after merging
                session.removeAttribute(SESSION_CART_KEY);
            }
        }

        // Delegate to role-based handler for redirect
        roleBasedHandler.onAuthenticationSuccess(request, response, authentication);
    }
}
