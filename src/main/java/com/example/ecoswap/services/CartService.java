package com.example.ecoswap.services;

import com.example.ecoswap.model.Cart;
import com.example.ecoswap.model.CartItem;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.repository.CartRepository;
import com.example.ecoswap.repository.CartItemRepository;
import com.example.ecoswap.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get or create cart for user
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUser(user);
                return cartRepository.save(cart);
            });
    }

    // Get cart by user ID
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    // Add item to cart
    @Transactional
    public Cart addToCart(User user, Long productId, Integer quantity) {
        // Validate product
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new RuntimeException("Product is not available");
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Only " + product.getStock() + " items available");
        }

        // Get or create cart
        Cart cart = getOrCreateCart(user);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Cannot add more items. Only " + product.getStock() + " items available");
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addCartItem(newItem);
            cartItemRepository.save(newItem);
        }

        return cartRepository.save(cart);
    }

    // Update cart item quantity
    @Transactional
    public Cart updateCartItemQuantity(User user, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify cart belongs to user
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to cart");
        }

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Check stock availability
        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Only " + cartItem.getProduct().getStock() + " items available");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return cartRepository.findById(cartItem.getCart().getId()).orElseThrow();
    }

    // Remove item from cart
    @Transactional
    public Cart removeFromCart(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify cart belongs to user
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to cart");
        }

        Cart cart = cartItem.getCart();
        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        return cartRepository.save(cart);
    }

    // Clear cart
    @Transactional
    public void clearCart(User user) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.clear();
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.save(cart);
        }
    }

    // Get cart total
    public BigDecimal getCartTotal(Long userId) {
        return cartRepository.findByUserId(userId)
            .map(Cart::getTotal)
            .orElse(BigDecimal.ZERO);
    }

    // Get cart item count
    public int getCartItemCount(Long userId) {
        return cartRepository.findByUserId(userId)
            .map(Cart::getTotalItems)
            .orElse(0);
    }

    // Validate cart before checkout
    public boolean validateCart(Cart cart) {
        if (cart.getCartItems().isEmpty()) {
            return false;
        }

        // Check all items are available
        for (CartItem item : cart.getCartItems()) {
            if (!item.isAvailable()) {
                return false;
            }
        }

        return true;
    }

    // Get cart item by ID
    public Optional<CartItem> getCartItemById(Long id) {
        return cartItemRepository.findById(id);
    }
}
