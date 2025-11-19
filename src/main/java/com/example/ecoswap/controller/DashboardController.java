package com.example.ecoswap.controller;

import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.repository.UserRepository;
import com.example.ecoswap.services.OrderService;
import com.example.ecoswap.services.ProductService;
import com.example.ecoswap.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * General dashboard endpoint that redirects to role-based dashboard
     */
    @GetMapping("/dashboard")
    public String redirectToDashboard(Authentication authentication) {
        // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Redirect based on user role
        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case SELLER -> "redirect:/seller/dashboard";
            case CUSTOMER -> "redirect:/customer/dashboard";
        };
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        model.addAttribute("title", "Admin Dashboard - EcoSwap");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        // User statistics
        Long totalUsers = userService.getTotalUserCount();
        Long totalSellers = userService.countUsersByRole(Role.SELLER);
        Long totalCustomers = userService.countUsersByRole(Role.CUSTOMER);
        Long pendingSellerApprovals = (long) userService.getPendingSellerApprovals().size();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSellers", totalSellers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("pendingSellerApprovals", pendingSellerApprovals);

        // Product statistics
        Long totalProducts = productService.getTotalProductCount();
        Long activeProducts = productService.countByStatus("ACTIVE");
        Long outOfStockProducts = productService.getOutOfStockProductCount();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);

        // Order and revenue statistics (platform-wide)
        java.util.Map<String, Object> platformStats = orderService.getPlatformStatistics();
        model.addAttribute("totalOrders", platformStats.get("totalOrders"));
        model.addAttribute("pendingOrders", platformStats.get("pendingOrders"));
        model.addAttribute("processingOrders", platformStats.get("processingOrders"));
        model.addAttribute("shippedOrders", platformStats.get("shippedOrders"));
        model.addAttribute("deliveredOrders", platformStats.get("deliveredOrders"));
        model.addAttribute("totalRevenue", platformStats.get("totalRevenue"));
        model.addAttribute("monthlyRevenue", platformStats.get("monthlyRevenue"));
        model.addAttribute("recentOrders", platformStats.get("recentOrders"));

        // Environmental impact
        Double totalCo2Saved = productService.calculateTotalCo2Saved();
        Double totalPlasticSaved = productService.calculateTotalPlasticSaved();
        Double averageEcoScore = productService.getAverageEcoScore();

        model.addAttribute("totalCo2Saved", totalCo2Saved);
        model.addAttribute("totalPlasticSaved", totalPlasticSaved);
        model.addAttribute("averageEcoScore", averageEcoScore);

        return "dashboard/index";
    }
}