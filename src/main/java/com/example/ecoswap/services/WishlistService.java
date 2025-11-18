package com.example.ecoswap.services;

import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.Wishlist;
import com.example.ecoswap.repository.ProductRepository;
import com.example.ecoswap.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get or create wishlist for user
    public Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Wishlist wishlist = new Wishlist();
                wishlist.setUser(user);
                return wishlistRepository.save(wishlist);
            });
    }

    // Get wishlist by user ID
    public Optional<Wishlist> getWishlistByUserId(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    // Add product to wishlist
    @Transactional
    public Wishlist addToWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (wishlist.containsProduct(product)) {
            throw new RuntimeException("Product already in wishlist");
        }

        wishlist.addProduct(product);
        return wishlistRepository.save(wishlist);
    }

    // Remove product from wishlist
    @Transactional
    public Wishlist removeFromWishlist(User user, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        wishlist.removeProduct(product);
        return wishlistRepository.save(wishlist);
    }

    // Check if product is in wishlist
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    // Get all products in wishlist
    public List<Product> getWishlistProducts(Long userId) {
        return wishlistRepository.findByUserId(userId)
            .map(Wishlist::getProducts)
            .orElse(List.of());
    }

    // Get wishlist item count
    public int getWishlistItemCount(Long userId) {
        return wishlistRepository.findByUserId(userId)
            .map(Wishlist::getTotalItems)
            .orElse(0);
    }

    // Clear wishlist
    @Transactional
    public void clearWishlist(User user) {
        wishlistRepository.findByUserId(user.getId())
            .ifPresent(wishlist -> {
                wishlist.getProducts().clear();
                wishlistRepository.save(wishlist);
            });
    }
}
