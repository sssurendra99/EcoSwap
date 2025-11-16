package com.example.ecoswap.repository;

import com.example.ecoswap.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find all cart items by cart ID
    List<CartItem> findByCartId(Long cartId);

    // Find cart item by cart and product
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    // Delete all cart items by cart ID
    void deleteByCartId(Long cartId);

    // Count cart items by cart ID
    Long countByCartId(Long cartId);
}
