package com.example.ecoswap.controller;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.OrderStatus;
import com.example.ecoswap.repository.OrderRepository;
import com.example.ecoswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CustomerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Authentication authentication, Model model) {
        // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Get customer's orders
        List<Order> allOrders = orderRepository.findByCustomer(user);

        // Calculate statistics
        int totalOrders = allOrders.size();

        // Count active orders (PENDING, CONFIRMED, SHIPPED)
        long activeOrders = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING ||
                        order.getStatus() == OrderStatus.CONFIRMED ||
                        order.getStatus() == OrderStatus.SHIPPED)
                .count();

        // Calculate total spent
        BigDecimal totalSpent = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average order value
        BigDecimal avgOrderValue = totalOrders > 0 ?
                totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        // Calculate total items purchased
        long itemsPurchased = allOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToLong(item -> item.getQuantity())
                .sum();

        // Calculate environmental impact
        // CO2 Saved: sum of (product.co2Saved * quantity) for all order items
        double co2Saved = allOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(item -> {
                    Double productCo2 = item.getProduct().getCo2Saved();
                    return (productCo2 != null ? productCo2 : 0.0) * item.getQuantity();
                })
                .sum();

        // Plastic Saved: sum of (product.plasticSaved * quantity) for all order items
        double plasticSaved = allOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(item -> {
                    Double productPlastic = item.getProduct().getPlasticSaved();
                    return (productPlastic != null ? productPlastic : 0.0) * item.getQuantity();
                })
                .sum();

        // Get recent orders (limit to 3 for display)
        List<Order> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(3)
                .toList();

        // TODO: Implement wishlist functionality
        int wishlistItems = 0;
        List<?> wishlistProducts = List.of(); // Empty for now

        // Loyalty points (placeholder for future implementation)
        int loyaltyPoints = totalOrders * 10; // 10 points per order

        // Add attributes to model
        model.addAttribute("title", "Customer Dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("co2Saved", co2Saved);
        model.addAttribute("itemsPurchased", itemsPurchased);
        model.addAttribute("plasticSaved", plasticSaved);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("avgOrderValue", avgOrderValue);
        model.addAttribute("memberSince", user.getCreatedAt());
        model.addAttribute("loyaltyPoints", loyaltyPoints);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("wishlistProducts", wishlistProducts);

        return "dashboard/customer";
    }
}
