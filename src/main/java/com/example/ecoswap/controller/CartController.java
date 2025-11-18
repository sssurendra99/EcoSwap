package com.example.ecoswap.controller;

import com.example.ecoswap.model.Cart;
import com.example.ecoswap.model.CartItem;
import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.OrderItem;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.repository.ProductRepository;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.CartService;
import com.example.ecoswap.services.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String SESSION_CART_KEY = "guestCart";

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.example.ecoswap.services.EmailService emailService;

    /**
     * View cart - supports both authenticated and anonymous users
     */
    @GetMapping
    public String viewCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            Model model
    ) {
        Cart cart;
        List<CartItem> cartItems;
        BigDecimal cartTotal = BigDecimal.ZERO;
        int cartItemCount = 0;

        if (userDetails != null) {
            // Authenticated user - use database cart
            User user = userDetails.getUser();
            Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());
            cart = cartOpt.orElse(new Cart());
            cartItems = cart.getCartItems();
            cartTotal = cart.getTotal();
            cartItemCount = cart.getTotalItems();

            model.addAttribute("userName", user.getFullName());
            model.addAttribute("userRole", user.getRole().getDisplayName());
        } else {
            // Anonymous user - use session cart
            cartItems = getSessionCart(session);
            for (CartItem item : cartItems) {
                cartTotal = cartTotal.add(item.getSubtotal());
                cartItemCount += item.getQuantity();
            }

            model.addAttribute("userName", "Guest");
            model.addAttribute("userRole", "Guest");
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("pageTitle", "Shopping Cart");

        return "cart/cart";
    }

    /**
     * Add item to cart - supports both authenticated and anonymous users
     */
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (userDetails != null) {
                // Authenticated user - use database cart
                User user = userDetails.getUser();
                cartService.addToCart(user, productId, quantity);
            } else {
                // Anonymous user - use session cart
                addToSessionCart(session, productId, quantity);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect to the referring page or cart
        return "redirect:/cart";
    }

    /**
     * Add item to cart via AJAX - supports both authenticated and anonymous users
     */
    @PostMapping("/add-ajax")
    @ResponseBody
    public java.util.Map<String, Object> addToCartAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            int itemCount;
            if (userDetails != null) {
                // Authenticated user
                User user = userDetails.getUser();
                Cart cart = cartService.addToCart(user, productId, quantity);
                itemCount = cart.getTotalItems();
            } else {
                // Anonymous user
                addToSessionCart(session, productId, quantity);
                itemCount = getSessionCartItemCount(session);
            }
            response.put("success", true);
            response.put("itemCount", itemCount);
            response.put("message", "Product added to cart");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/update/{cartItemId}")
    public String updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            cartService.updateCartItemQuantity(user, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Update cart item via AJAX
     */
    @PostMapping("/update-ajax/{cartItemId}")
    @ResponseBody
    public String updateCartItemAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity
    ) {
        User user = userDetails.getUser();

        try {
            Cart cart = cartService.updateCartItemQuantity(user, cartItemId, quantity);
            return "{\"success\": true, \"cartTotal\": " + cart.getTotal() + ", \"itemCount\": " + cart.getTotalItems() + "}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            cartService.removeFromCart(user, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Remove item via AJAX
     */
    @DeleteMapping("/remove-ajax/{cartItemId}")
    @ResponseBody
    public String removeFromCartAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId
    ) {
        User user = userDetails.getUser();

        try {
            Cart cart = cartService.removeFromCart(user, cartItemId);
            return "{\"success\": true, \"cartTotal\": " + cart.getTotal() + ", \"itemCount\": " + cart.getTotalItems() + "}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Clear cart
     */
    @PostMapping("/clear")
    public String clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            cartService.clearCart(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cart cleared successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Get cart count (for navbar badge) - supports both authenticated and anonymous users
     */
    @GetMapping("/count")
    @ResponseBody
    public String getCartCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session
    ) {
        int count;
        if (userDetails != null) {
            // Authenticated user
            User user = userDetails.getUser();
            count = cartService.getCartItemCount(user.getId());
        } else {
            // Anonymous user
            count = getSessionCartItemCount(session);
        }
        return "{\"count\": " + count + "}";
    }

    // ===== Session Cart Helper Methods =====

    /**
     * Get cart items from session
     */
    @SuppressWarnings("unchecked")
    private List<CartItem> getSessionCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        return cart;
    }

    /**
     * Add product to session cart
     */
    private void addToSessionCart(HttpSession session, Long productId, Integer quantity) {
        List<CartItem> cart = getSessionCart(session);

        // Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            // Note: subtotal is calculated dynamically via getSubtotal()
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            // Note: subtotal is calculated dynamically via getSubtotal()
            cart.add(newItem);
        }

        session.setAttribute(SESSION_CART_KEY, cart);
    }

    /**
     * Get total number of items in session cart
     */
    private int getSessionCartItemCount(HttpSession session) {
        List<CartItem> cart = getSessionCart(session);
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Proceed to checkout
     */
    @GetMapping("/checkout")
    public String proceedToCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());

        if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty!");
            return "redirect:/cart";
        }

        Cart cart = cartOpt.get();

        // Validate cart
        if (!cartService.validateCart(cart)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Some items in your cart are no longer available. Please review your cart.");
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getCartItems());
        model.addAttribute("subtotal", cart.getTotal());
        model.addAttribute("pageTitle", "Checkout");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        return "cart/checkout";
    }

    /**
     * Place order from cart
     */
    @PostMapping("/place-order")
    public String placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String customerName,
            @RequestParam String customerEmail,
            @RequestParam String customerPhone,
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingState,
            @RequestParam String shippingZipCode,
            @RequestParam String shippingCountry,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String orderNotes,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            // Get cart
            Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());

            if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty!");
                return "redirect:/cart";
            }

            Cart cart = cartOpt.get();

            // Validate cart
            if (!cartService.validateCart(cart)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Some items in your cart are no longer available.");
                return "redirect:/cart";
            }

            // Create order
            Order order = new Order();
            order.setCustomer(user);
            order.setCustomerEmail(customerEmail);
            order.setCustomerPhone(customerPhone);
            order.setShippingAddress(shippingAddress);
            order.setShippingCity(shippingCity);
            order.setShippingState(shippingState);
            order.setShippingZipCode(shippingZipCode);
            order.setShippingCountry(shippingCountry);
            order.setPaymentMethod(paymentMethod);
            order.setOrderNotes(orderNotes);

            // Calculate totals with null safety
            BigDecimal subtotal = cart.getTotal();
            if (subtotal == null) {
                subtotal = BigDecimal.ZERO;
            }

            BigDecimal shippingCost = new BigDecimal("10.00"); // Fixed shipping for now
            BigDecimal tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax

            order.setSubtotal(subtotal);
            order.setShippingCost(shippingCost);
            order.setTax(tax);
            order.setTotalAmount(subtotal.add(shippingCost).add(tax));

            // Create order items from cart items
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = cartItem.getProduct();

                // Skip if product is null or invalid
                if (product == null) {
                    continue;
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setSeller(product.getSeller());
                orderItem.setQuantity(cartItem.getQuantity());

                // Set price with null safety
                BigDecimal itemPrice = product.getPrice();
                if (itemPrice == null) {
                    itemPrice = BigDecimal.ZERO;
                }
                orderItem.setPrice(itemPrice);

                orderItem.setProductName(product.getName() != null ? product.getName() : "Unknown Product");
                orderItem.setProductSku(product.getSku() != null ? product.getSku() : "N/A");
                orderItem.setProductImage(product.getImage());

                order.addOrderItem(orderItem);

                // Update product stock
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                int newStock = Math.max(0, currentStock - cartItem.getQuantity());
                product.setStock(newStock);
            }

            // Save order
            Order savedOrder = orderService.createOrder(order);

            // Clear cart
            cartService.clearCart(user);

            // Send order confirmation email
            try {
                emailService.sendOrderConfirmation(savedOrder);
            } catch (Exception emailException) {
                // Log error but don't fail the order
                System.err.println("Failed to send order confirmation email: " + emailException.getMessage());
            }

            redirectAttributes.addFlashAttribute("successMessage",
                "Order placed successfully! Order number: " + savedOrder.getOrderNumber() + ". A confirmation email has been sent to " + savedOrder.getCustomerEmail());

            return "redirect:/dashboard/orders/" + savedOrder.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error placing order: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}
