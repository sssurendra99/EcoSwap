package com.example.ecoswap.repository;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find all orders by customer
    @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.createdAt DESC")
    List<Order> findByCustomer(@Param("customer") User customer);

    // Find by customer
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // Find orders containing items from a specific seller
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    // Find orders by seller and status
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findOrdersBySellerAndStatus(@Param("sellerId") Long sellerId, @Param("status") OrderStatus status, Pageable pageable);

    // Find orders by status
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Count orders by seller
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId")
    Long countOrdersBySeller(@Param("sellerId") Long sellerId);

    // Count orders by seller and status
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId AND o.status = :status")
    Long countOrdersBySellerAndStatus(@Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    // Calculate total revenue for seller
    @Query("SELECT COALESCE(SUM(oi.lineTotal), 0) FROM OrderItem oi WHERE oi.seller.id = :sellerId AND oi.order.status IN :statuses")
    BigDecimal calculateSellerRevenue(@Param("sellerId") Long sellerId, @Param("statuses") List<OrderStatus> statuses);

    // Calculate total revenue for seller within date range
    @Query("SELECT COALESCE(SUM(oi.lineTotal), 0) FROM OrderItem oi WHERE oi.seller.id = :sellerId AND oi.order.status IN :statuses AND oi.order.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateSellerRevenueByDateRange(@Param("sellerId") Long sellerId, @Param("statuses") List<OrderStatus> statuses, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Search orders by seller
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId AND (LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.customer.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY o.createdAt DESC")
    Page<Order> searchOrdersBySeller(@Param("sellerId") Long sellerId, @Param("search") String search, Pageable pageable);

    // Get recent orders
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);

    // Get recent orders by seller
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    // Count orders by customer
    Long countByCustomerId(Long customerId);

    // Find orders within date range
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find orders by seller within date range
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller.id = :sellerId AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersBySellerAndDateRange(@Param("sellerId") Long sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Check if customer has ordered a specific product (for review verification)
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o JOIN o.orderItems oi WHERE o.customer.id = :customerId AND oi.product.id = :productId")
    boolean existsByCustomerIdAndOrderItems_ProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    // ============ ADMIN/PLATFORM-WIDE QUERIES ============

    // Count orders by status (platform-wide)
    Long countByStatus(OrderStatus status);

    // Calculate total platform revenue by statuses
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses")
    BigDecimal calculateTotalRevenueByStatuses(@Param("statuses") List<OrderStatus> statuses);

    // Calculate platform revenue by date range and statuses
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRangeAndStatuses(@Param("statuses") List<OrderStatus> statuses, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
