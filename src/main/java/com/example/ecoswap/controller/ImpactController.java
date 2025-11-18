package com.example.ecoswap.controller;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.OrderStatus;
import com.example.ecoswap.repository.OrderRepository;
import com.example.ecoswap.repository.ProductRepository;
import com.example.ecoswap.repository.UserRepository;
import com.example.ecoswap.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ImpactController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Customer Impact Tracker
     */
    @GetMapping("/customer/impact")
    public String customerImpact(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Get all customer orders
        List<Order> allOrders = orderRepository.findByCustomer(user);
        List<Order> completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .collect(Collectors.toList());

        // Calculate total items purchased
        long totalItems = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToLong(item -> item.getQuantity())
            .sum();

        // Calculate environmental impact
        double totalCo2Saved = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double productCo2 = item.getProduct().getCo2Saved();
                return (productCo2 != null ? productCo2 : 0.0) * item.getQuantity();
            })
            .sum();

        double totalPlasticSaved = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double productPlastic = item.getProduct().getPlasticSaved();
                return (productPlastic != null ? productPlastic : 0.0) * item.getQuantity();
            })
            .sum();

        // Calculate water saved (estimate: 1000L per kg of plastic)
        double waterSaved = (totalPlasticSaved / 1000) * 1000;

        // Calculate trees equivalent (1 tree absorbs ~21kg CO2 per year)
        double treesEquivalent = totalCo2Saved / 21;

        // Calculate car miles equivalent (1 mile = ~0.4kg CO2)
        double carMilesEquivalent = totalCo2Saved / 0.4;

        // Monthly impact (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<Order> recentOrders = completedOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.toList());

        double monthlyCo2 = recentOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double productCo2 = item.getProduct().getCo2Saved();
                return (productCo2 != null ? productCo2 : 0.0) * item.getQuantity();
            })
            .sum();

        double monthlyPlastic = recentOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double productPlastic = item.getProduct().getPlasticSaved();
                return (productPlastic != null ? productPlastic : 0.0) * item.getQuantity();
            })
            .sum();

        // Get impact by category
        Map<String, Double> co2ByCategory = new HashMap<>();
        Map<String, Double> plasticByCategory = new HashMap<>();

        completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .forEach(item -> {
                String categoryName = item.getProduct().getCategory().getName();
                Double co2 = item.getProduct().getCo2Saved();
                Double plastic = item.getProduct().getPlasticSaved();

                co2ByCategory.merge(categoryName,
                    (co2 != null ? co2 : 0.0) * item.getQuantity(),
                    Double::sum);
                plasticByCategory.merge(categoryName,
                    (plastic != null ? plastic : 0.0) * item.getQuantity(),
                    Double::sum);
            });

        // Calculate member days
        long memberDays = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());

        // Calculate daily average
        double dailyAvgCo2 = memberDays > 0 ? totalCo2Saved / memberDays : 0;
        double dailyAvgPlastic = memberDays > 0 ? totalPlasticSaved / memberDays : 0;

        // Achievements
        List<Map<String, Object>> achievements = new ArrayList<>();

        if (totalItems >= 1) {
            achievements.add(Map.of(
                "icon", "🌱",
                "title", "First Step",
                "description", "Made your first eco-friendly purchase",
                "unlocked", true
            ));
        }

        if (totalItems >= 10) {
            achievements.add(Map.of(
                "icon", "🌿",
                "title", "Green Warrior",
                "description", "Purchased 10+ sustainable products",
                "unlocked", true
            ));
        }

        if (totalCo2Saved >= 100) {
            achievements.add(Map.of(
                "icon", "🌍",
                "title", "Planet Protector",
                "description", "Saved 100kg+ of CO₂ emissions",
                "unlocked", true
            ));
        }

        if (totalPlasticSaved >= 1000) {
            achievements.add(Map.of(
                "icon", "♻️",
                "title", "Plastic Warrior",
                "description", "Prevented 1kg+ of plastic waste",
                "unlocked", true
            ));
        }

        // Add attributes to model
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("title", "Impact Tracker");
        model.addAttribute("pageTitle", "Environmental Impact");
        model.addAttribute("totalCo2Saved", totalCo2Saved);
        model.addAttribute("totalPlasticSaved", totalPlasticSaved);
        model.addAttribute("waterSaved", waterSaved);
        model.addAttribute("treesEquivalent", treesEquivalent);
        model.addAttribute("carMilesEquivalent", carMilesEquivalent);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalOrders", completedOrders.size());
        model.addAttribute("monthlyCo2", monthlyCo2);
        model.addAttribute("monthlyPlastic", monthlyPlastic);
        model.addAttribute("co2ByCategory", co2ByCategory);
        model.addAttribute("plasticByCategory", plasticByCategory);
        model.addAttribute("memberDays", memberDays);
        model.addAttribute("dailyAvgCo2", dailyAvgCo2);
        model.addAttribute("dailyAvgPlastic", dailyAvgPlastic);
        model.addAttribute("achievements", achievements);
        model.addAttribute("memberSince", user.getCreatedAt());

        return "customer/impact";
    }

    /**
     * Seller Impact Tracker
     */
    @GetMapping("/seller/impact")
    public String sellerImpact(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Long sellerId = user.getId();

        // Get all seller products
        List<Product> products = productRepository.findBySellerId(sellerId);

        // Calculate potential impact (if all stock was sold)
        double potentialCo2 = products.stream()
            .mapToDouble(p -> {
                Double co2 = p.getCo2Saved();
                return (co2 != null ? co2 : 0.0) * p.getStock();
            })
            .sum();

        double potentialPlastic = products.stream()
            .mapToDouble(p -> {
                Double plastic = p.getPlasticSaved();
                return (plastic != null ? plastic : 0.0) * p.getStock();
            })
            .sum();

        // Get all orders containing seller's products
        List<Order> allOrders = orderRepository.findAll().stream()
            .filter(order -> order.getOrderItems().stream()
                .anyMatch(item -> item.getSeller().getId().equals(sellerId)))
            .collect(Collectors.toList());

        List<Order> completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .collect(Collectors.toList());

        // Calculate actual impact (from sold products)
        double actualCo2 = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .mapToDouble(item -> {
                Double co2 = item.getProduct().getCo2Saved();
                return (co2 != null ? co2 : 0.0) * item.getQuantity();
            })
            .sum();

        double actualPlastic = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .mapToDouble(item -> {
                Double plastic = item.getProduct().getPlasticSaved();
                return (plastic != null ? plastic : 0.0) * item.getQuantity();
            })
            .sum();

        // Calculate total items sold
        long totalItemsSold = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .mapToLong(item -> item.getQuantity())
            .sum();

        // Calculate customers helped
        long customersHelped = completedOrders.stream()
            .map(Order::getCustomer)
            .distinct()
            .count();

        // Monthly impact
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<Order> recentOrders = completedOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.toList());

        double monthlyCo2 = recentOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .filter(item -> item.getSeller().getId().equals(sellerId))
            .mapToDouble(item -> {
                Double co2 = item.getProduct().getCo2Saved();
                return (co2 != null ? co2 : 0.0) * item.getQuantity();
            })
            .sum();

        // Top impact products
        List<Map<String, Object>> topProducts = products.stream()
            .sorted((p1, p2) -> {
                Double co2_1 = p1.getCo2Saved() != null ? p1.getCo2Saved() : 0.0;
                Double co2_2 = p2.getCo2Saved() != null ? p2.getCo2Saved() : 0.0;
                return Double.compare(co2_2, co2_1);
            })
            .limit(5)
            .map(p -> Map.<String, Object>of(
                "name", p.getName(),
                "co2Saved", p.getCo2Saved() != null ? p.getCo2Saved() : 0.0,
                "plasticSaved", p.getPlasticSaved() != null ? p.getPlasticSaved() : 0.0,
                "stock", p.getStock(),
                "image", p.getImage()
            ))
            .collect(Collectors.toList());

        // Add attributes to model
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("title", "Impact Tracker");
        model.addAttribute("pageTitle", "Environmental Impact");
        model.addAttribute("actualCo2", actualCo2);
        model.addAttribute("actualPlastic", actualPlastic);
        model.addAttribute("potentialCo2", potentialCo2);
        model.addAttribute("potentialPlastic", potentialPlastic);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("totalItemsSold", totalItemsSold);
        model.addAttribute("customersHelped", customersHelped);
        model.addAttribute("monthlyCo2", monthlyCo2);
        model.addAttribute("topProducts", topProducts);

        return "seller/impact";
    }

    /**
     * Admin Impact Tracker (Platform-wide)
     */
    @GetMapping("/admin/impact")
    public String adminImpact(Authentication authentication, Model model) {
        // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Get all completed orders
        List<Order> allOrders = orderRepository.findAll();
        List<Order> completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .collect(Collectors.toList());

        // Calculate platform-wide impact
        double totalCo2 = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double co2 = item.getProduct().getCo2Saved();
                return (co2 != null ? co2 : 0.0) * item.getQuantity();
            })
            .sum();

        double totalPlastic = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double plastic = item.getProduct().getPlasticSaved();
                return (plastic != null ? plastic : 0.0) * item.getQuantity();
            })
            .sum();

        // Get all products
        List<Product> allProducts = productRepository.findAll();

        // Calculate potential impact
        double potentialCo2 = allProducts.stream()
            .mapToDouble(p -> {
                Double co2 = p.getCo2Saved();
                return (co2 != null ? co2 : 0.0) * p.getStock();
            })
            .sum();

        // Get all users
        long totalCustomers = userRepository.count();

        // Calculate total items sold
        long totalItemsSold = completedOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToLong(item -> item.getQuantity())
            .sum();

        // Monthly growth
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<Order> recentOrders = completedOrders.stream()
            .filter(o -> o.getCreatedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.toList());

        double monthlyCo2 = recentOrders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .mapToDouble(item -> {
                Double co2 = item.getProduct().getCo2Saved();
                return (co2 != null ? co2 : 0.0) * item.getQuantity();
            })
            .sum();

        // Add attributes to model
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("title", "Platform Impact");
        model.addAttribute("pageTitle", "Platform Impact Tracker");
        model.addAttribute("totalCo2", totalCo2);
        model.addAttribute("totalPlastic", totalPlastic);
        model.addAttribute("potentialCo2", potentialCo2);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalProducts", allProducts.size());
        model.addAttribute("totalItemsSold", totalItemsSold);
        model.addAttribute("monthlyCo2", monthlyCo2);
        model.addAttribute("totalOrders", completedOrders.size());

        return "admin/impact";
    }
}
