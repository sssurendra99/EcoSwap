package com.example.ecoswap.controller;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.OrderItem;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.OrderStatus;
import com.example.ecoswap.repository.OrderRepository;
import com.example.ecoswap.repository.ProductRepository;
import com.example.ecoswap.repository.UserRepository;
import com.example.ecoswap.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AnalyticsController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Seller Analytics Dashboard
     */
    @GetMapping("/seller/analytics")
    public String sellerAnalytics(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Long sellerId = user.getId();

        // Get all orders containing seller's products
        List<Order> allOrders = orderRepository.findAll().stream()
            .filter(order -> order.getOrderItems().stream()
                .anyMatch(item -> item.getSeller().getId().equals(sellerId)))
            .collect(Collectors.toList());

        // Filter by status
        List<Order> deliveredOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .collect(Collectors.toList());

        List<Order> pendingOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING)
            .collect(Collectors.toList());

        List<Order> confirmedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
            .collect(Collectors.toList());

        List<Order> shippedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.SHIPPED)
            .collect(Collectors.toList());

        List<Order> cancelledOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
            .collect(Collectors.toList());

        // Calculate revenue statistics
        BigDecimal totalRevenue = deliveredOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly revenue (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        BigDecimal monthlyRevenue = deliveredOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(thirtyDaysAgo))
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Weekly revenue (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        BigDecimal weeklyRevenue = deliveredOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(sevenDaysAgo))
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Daily average revenue
        long daysSinceFirstOrder = 1;
        if (!deliveredOrders.isEmpty()) {
            LocalDateTime firstOrderDate = deliveredOrders.stream()
                .map(Order::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
            daysSinceFirstOrder = Math.max(1, ChronoUnit.DAYS.between(firstOrderDate, LocalDateTime.now()));
        }
        BigDecimal dailyAvgRevenue = totalRevenue.divide(
            BigDecimal.valueOf(daysSinceFirstOrder), 2, BigDecimal.ROUND_HALF_UP
        );

        // Items sold statistics
        long totalItemsSold = deliveredOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .mapToLong(OrderItem::getQuantity)
            .sum();

        long monthlyItemsSold = deliveredOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(thirtyDaysAgo))
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .mapToLong(OrderItem::getQuantity)
            .sum();

        // Customer statistics
        long uniqueCustomers = deliveredOrders.stream()
            .map(Order::getCustomer)
            .map(User::getId)
            .distinct()
            .count();

        long newCustomersThisMonth = deliveredOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(thirtyDaysAgo))
            .map(Order::getCustomer)
            .map(User::getId)
            .distinct()
            .count();

        // Product statistics
        List<Product> allProducts = productRepository.findBySellerId(sellerId);
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(p -> p.getStock() > 0).count();
        long outOfStock = allProducts.stream().filter(p -> p.getStock() == 0).count();

        // Best selling products (top 5 by quantity sold)
        Map<Long, Long> productSalesCount = new HashMap<>();
        Map<Long, BigDecimal> productSalesRevenue = new HashMap<>();

        deliveredOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .forEach(item -> {
                Long productId = item.getProduct().getId();
                productSalesCount.merge(productId, (long) item.getQuantity(), Long::sum);
                productSalesRevenue.merge(productId, item.getLineTotal(), BigDecimal::add);
            });

        List<Map<String, Object>> topProducts = productSalesCount.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Product product = productRepository.findById(entry.getKey()).orElse(null);
                if (product != null) {
                    return Map.<String, Object>of(
                        "id", product.getId(),
                        "name", product.getName(),
                        "image", product.getImage() != null ? product.getImage() : "",
                        "unitsSold", entry.getValue(),
                        "revenue", productSalesRevenue.get(entry.getKey())
                    );
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // Revenue by category
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        deliveredOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .forEach(item -> {
                String categoryName = item.getProduct().getCategory().getName();
                revenueByCategory.merge(categoryName, item.getLineTotal(), BigDecimal::add);
            });

        // Sales trend data (last 12 months)
        Map<String, BigDecimal> monthlySalesTrend = new LinkedHashMap<>();
        Map<String, Long> monthlyOrdersTrend = new LinkedHashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minus(i, ChronoUnit.MONTHS).withDayOfMonth(1);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            String monthLabel = monthStart.format(monthFormatter);

            BigDecimal monthRevenue = deliveredOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(monthStart) && o.getCreatedAt().isBefore(monthEnd))
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getSeller().getId().equals(sellerId))
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long monthOrders = deliveredOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(monthStart) && o.getCreatedAt().isBefore(monthEnd))
                .filter(order -> order.getOrderItems().stream()
                    .anyMatch(item -> item.getSeller().getId().equals(sellerId)))
                .count();

            monthlySalesTrend.put(monthLabel, monthRevenue);
            monthlyOrdersTrend.put(monthLabel, monthOrders);
        }

        // Order status breakdown
        Map<String, Long> orderStatusBreakdown = new LinkedHashMap<>();
        orderStatusBreakdown.put("Delivered", (long) deliveredOrders.size());
        orderStatusBreakdown.put("Shipped", (long) shippedOrders.size());
        orderStatusBreakdown.put("Confirmed", (long) confirmedOrders.size());
        orderStatusBreakdown.put("Pending", (long) pendingOrders.size());
        orderStatusBreakdown.put("Cancelled", (long) cancelledOrders.size());

        // Average order value
        BigDecimal avgOrderValue = deliveredOrders.isEmpty() ? BigDecimal.ZERO :
            totalRevenue.divide(BigDecimal.valueOf(deliveredOrders.size()), 2, BigDecimal.ROUND_HALF_UP);

        // Conversion rate (delivered / total orders)
        double conversionRate = allOrders.isEmpty() ? 0 :
            (double) deliveredOrders.size() / allOrders.size() * 100;

        // Add all attributes to model
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("title", "Analytics");
        model.addAttribute("pageTitle", "Sales Analytics");

        // Revenue stats
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("weeklyRevenue", weeklyRevenue);
        model.addAttribute("dailyAvgRevenue", dailyAvgRevenue);
        model.addAttribute("avgOrderValue", avgOrderValue);

        // Order stats
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("deliveredOrders", deliveredOrders.size());
        model.addAttribute("pendingOrders", pendingOrders.size());
        model.addAttribute("conversionRate", String.format("%.1f", conversionRate));
        model.addAttribute("orderStatusBreakdown", orderStatusBreakdown);

        // Items stats
        model.addAttribute("totalItemsSold", totalItemsSold);
        model.addAttribute("monthlyItemsSold", monthlyItemsSold);

        // Customer stats
        model.addAttribute("uniqueCustomers", uniqueCustomers);
        model.addAttribute("newCustomersThisMonth", newCustomersThisMonth);

        // Product stats
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("outOfStock", outOfStock);
        model.addAttribute("topProducts", topProducts);

        // Charts data
        model.addAttribute("revenueByCategory", revenueByCategory);
        model.addAttribute("monthlySalesTrend", monthlySalesTrend);
        model.addAttribute("monthlyOrdersTrend", monthlyOrdersTrend);

        return "seller/analytics";
    }

    /**
     * Admin Analytics Dashboard (can be added later if needed)
     */
    @GetMapping("/dashboard/analytics")
    public String dashboardAnalytics(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("title", "Analytics");
        model.addAttribute("pageTitle", "Platform Analytics");

        // Redirect based on role
        if (user.getRole().name().equals("SELLER")) {
            return "redirect:/seller/analytics";
        } else if (user.getRole().name().equals("ADMIN")) {
            // For now, redirect to seller analytics
            // Later can create separate admin analytics
            return "redirect:/seller/analytics";
        }

        return "redirect:/";
    }
}
