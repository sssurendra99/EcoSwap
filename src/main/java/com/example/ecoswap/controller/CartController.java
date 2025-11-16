package com.example.ecoswap.controller;

import com.example.ecoswap.model.Cart;
import com.example.ecoswap.model.CartItem;
import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.OrderItem;
import com.example.ecoswap.model.User;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.CartService;
import com.example.ecoswap.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    /**
     * View cart
     */
    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());
        Cart cart = cartOpt.orElse(new Cart());

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getCartItems());
        model.addAttribute("cartTotal", cart.getTotal());
        model.addAttribute("cartItemCount", cart.getTotalItems());
        model.addAttribute("pageTitle", "Shopping Cart");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        return "cart/cart";
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect to the referring page or cart
        return "redirect:/cart";
    }

    /**
     * Add item to cart via AJAX
     */
    @PostMapping("/add-ajax")
    @ResponseBody
    public String addToCartAjax(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        User user = userDetails.getUser();

        try {
            Cart cart = cartService.addToCart(user, productId, quantity);
            return "{\"success\": true, \"itemCount\": " + cart.getTotalItems() + ", \"message\": \"Product added to cart\"}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
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
     * Get cart count (for navbar badge)
     */
    @GetMapping("/count")
    @ResponseBody
    public String getCartCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        int count = cartService.getCartItemCount(user.getId());
        return "{\"count\": " + count + "}";
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

            // Calculate totals
            BigDecimal subtotal = cart.getTotal();
            BigDecimal shippingCost = new BigDecimal("10.00"); // Fixed shipping for now
            BigDecimal tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax

            order.setSubtotal(subtotal);
            order.setShippingCost(shippingCost);
            order.setTax(tax);
            order.setTotalAmount(subtotal.add(shippingCost).add(tax));

            // Create order items from cart items
            for (CartItem cartItem : cart.getCartItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setSeller(cartItem.getProduct().getSeller());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                orderItem.setProductName(cartItem.getProduct().getName());
                orderItem.setProductSku(cartItem.getProduct().getSku());
                orderItem.setProductImage(cartItem.getProduct().getImage());

                order.addOrderItem(orderItem);

                // Update product stock
                cartItem.getProduct().setStock(
                    cartItem.getProduct().getStock() - cartItem.getQuantity()
                );
            }

            // Save order
            Order savedOrder = orderService.createOrder(order);

            // Clear cart
            cartService.clearCart(user);

            redirectAttributes.addFlashAttribute("successMessage",
                "Order placed successfully! Order number: " + savedOrder.getOrderNumber());

            return "redirect:/dashboard/orders/" + savedOrder.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error placing order: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}
