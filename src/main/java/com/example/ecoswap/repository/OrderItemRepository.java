package com.example.ecoswap.repository;

import com.example.ecoswap.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find all order items by order ID
    List<OrderItem> findByOrderId(Long orderId);

    // Find all order items by seller
    @Query("SELECT oi FROM OrderItem oi WHERE oi.seller.id = :sellerId ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findBySellerId(@Param("sellerId") Long sellerId);

    // Find order items by product
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    // Count items sold by seller
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.seller.id = :sellerId")
    Long countItemsSoldBySeller(@Param("sellerId") Long sellerId);

    // Get best selling products for seller
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalSold FROM OrderItem oi WHERE oi.seller.id = :sellerId GROUP BY oi.product.id, oi.product.name ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProductsBySeller(@Param("sellerId") Long sellerId);
}
