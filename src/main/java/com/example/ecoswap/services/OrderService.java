package com.example.ecoswap.services;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.OrderItem;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.OrderStatus;
import com.example.ecoswap.repository.OrderRepository;
import com.example.ecoswap.repository.OrderItemRepository;
import com.example.ecoswap.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Generate unique order number
    public String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(new Random().nextInt(1000));
        return "ORD-" + timestamp + "-" + random;
    }

    // Get all orders with pagination
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable);
    }

    // Get order by ID
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Get order by order number
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    // Get orders by customer
    public Page<Order> getOrdersByCustomer(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    // Get orders by seller
    public Page<Order> getOrdersBySeller(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findOrdersBySeller(sellerId, pageable);
    }

    // Get orders by seller and status
    public Page<Order> getOrdersBySellerAndStatus(Long sellerId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findOrdersBySellerAndStatus(sellerId, status, pageable);
    }

    // Search orders by seller
    public Page<Order> searchOrdersBySeller(Long sellerId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.searchOrdersBySeller(sellerId, search, pageable);
    }

    // Get recent orders by seller
    public List<Order> getRecentOrdersBySeller(Long sellerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findRecentOrdersBySeller(sellerId, pageable);
    }

    // Create new order
    @Transactional
    public Order createOrder(Order order) {
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(LocalDateTime.now());
        order.calculateTotal();
        return orderRepository.save(order);
    }

    // Update order status
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);

        // Update timestamp based on status
        switch (newStatus) {
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                // Restore product stock
                restoreStock(order);
                break;
            default:
                break;
        }

        return orderRepository.save(order);
    }

    // Update tracking number
    @Transactional
    public Order updateTrackingNumber(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setTrackingNumber(trackingNumber);
        return orderRepository.save(order);
    }

    // Cancel order
    @Transactional
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    // Restore stock when order is cancelled
    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    // Statistics methods

    // Count orders by seller
    public Long countOrdersBySeller(Long sellerId) {
        return orderRepository.countOrdersBySeller(sellerId);
    }

    // Count orders by seller and status
    public Long countOrdersBySellerAndStatus(Long sellerId, OrderStatus status) {
        return orderRepository.countOrdersBySellerAndStatus(sellerId, status);
    }

    // Calculate seller revenue
    public BigDecimal calculateSellerRevenue(Long sellerId) {
        List<OrderStatus> completedStatuses = Arrays.asList(
            OrderStatus.DELIVERED,
            OrderStatus.SHIPPED,
            OrderStatus.PROCESSING,
            OrderStatus.CONFIRMED
        );
        return orderRepository.calculateSellerRevenue(sellerId, completedStatuses);
    }

    // Calculate seller revenue for current month
    public BigDecimal calculateSellerMonthlyRevenue(Long sellerId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now();
        List<OrderStatus> completedStatuses = Arrays.asList(
            OrderStatus.DELIVERED,
            OrderStatus.SHIPPED,
            OrderStatus.PROCESSING,
            OrderStatus.CONFIRMED
        );
        return orderRepository.calculateSellerRevenueByDateRange(sellerId, completedStatuses, startOfMonth, endOfMonth);
    }

    // Get order statistics for seller dashboard
    public Map<String, Object> getSellerOrderStatistics(Long sellerId) {
        Map<String, Object> stats = new HashMap<>();

        // Total orders
        Long totalOrders = countOrdersBySeller(sellerId);
        stats.put("totalOrders", totalOrders);

        // Orders by status
        stats.put("pendingOrders", countOrdersBySellerAndStatus(sellerId, OrderStatus.PENDING));
        stats.put("processingOrders", countOrdersBySellerAndStatus(sellerId, OrderStatus.PROCESSING));
        stats.put("shippedOrders", countOrdersBySellerAndStatus(sellerId, OrderStatus.SHIPPED));
        stats.put("deliveredOrders", countOrdersBySellerAndStatus(sellerId, OrderStatus.DELIVERED));
        stats.put("cancelledOrders", countOrdersBySellerAndStatus(sellerId, OrderStatus.CANCELLED));

        // Revenue
        stats.put("totalRevenue", calculateSellerRevenue(sellerId));
        stats.put("monthlyRevenue", calculateSellerMonthlyRevenue(sellerId));

        // Items sold
        Long itemsSold = orderItemRepository.countItemsSoldBySeller(sellerId);
        stats.put("totalItemsSold", itemsSold);

        // Recent orders
        List<Order> recentOrders = getRecentOrdersBySeller(sellerId, 5);
        stats.put("recentOrders", recentOrders);

        return stats;
    }

    // Get orders within date range
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByDateRange(startDate, endDate);
    }

    // Get seller orders within date range
    public List<Order> getOrdersBySellerAndDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBySellerAndDateRange(sellerId, startDate, endDate);
    }

    // Delete order (admin only)
    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    // ============ ADMIN/PLATFORM-WIDE STATISTICS ============

    /**
     * Get total order count across the entire platform
     */
    public Long getTotalOrderCount() {
        return orderRepository.count();
    }

    /**
     * Get total platform revenue (all delivered and confirmed orders)
     */
    public BigDecimal calculateTotalPlatformRevenue() {
        List<OrderStatus> completedStatuses = Arrays.asList(
            OrderStatus.DELIVERED,
            OrderStatus.CONFIRMED
        );
        BigDecimal revenue = orderRepository.calculateTotalRevenueByStatuses(completedStatuses);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get platform revenue for current month
     */
    public BigDecimal calculateMonthlyPlatformRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<OrderStatus> completedStatuses = Arrays.asList(
            OrderStatus.DELIVERED,
            OrderStatus.CONFIRMED
        );
        BigDecimal revenue = orderRepository.calculateRevenueByDateRangeAndStatuses(completedStatuses, startOfMonth, endOfMonth);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Count orders by status (platform-wide)
     */
    public Long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Get recent orders across the platform
     */
    public List<Order> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable).getContent();
    }

    /**
     * Get comprehensive platform statistics for admin dashboard
     */
    public Map<String, Object> getPlatformStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Total orders
        Long totalOrders = getTotalOrderCount();
        stats.put("totalOrders", totalOrders);

        // Orders by status
        stats.put("pendingOrders", countOrdersByStatus(OrderStatus.PENDING));
        stats.put("processingOrders", countOrdersByStatus(OrderStatus.PROCESSING));
        stats.put("shippedOrders", countOrdersByStatus(OrderStatus.SHIPPED));
        stats.put("deliveredOrders", countOrdersByStatus(OrderStatus.DELIVERED));
        stats.put("cancelledOrders", countOrdersByStatus(OrderStatus.CANCELLED));

        // Revenue
        stats.put("totalRevenue", calculateTotalPlatformRevenue());
        stats.put("monthlyRevenue", calculateMonthlyPlatformRevenue());

        // Recent orders
        List<Order> recentOrders = getRecentOrders(10);
        stats.put("recentOrders", recentOrders);

        return stats;
    }
}
