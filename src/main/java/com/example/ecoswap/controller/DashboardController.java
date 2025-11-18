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

    @GetMapping("/admin/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        model.addAttribute("title", "Admin Dashboard");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        // User statistics
        Long totalUsers = userService.getTotalUserCount();
        Long totalSellers = userService.countUsersByRole(Role.SELLER);
        Long totalCustomers = userService.countUsersByRole(Role.CUSTOMER);
        Long pendingSellerApprovals = (long) userService.getPendingSellerApprovals().size();

        // Product statistics
        Long totalProducts = productService.getTotalProductCount();

        // Order statistics
        // For now, we can get total orders count
        // You might want to add more specific methods to OrderService

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSellers", totalSellers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("pendingSellerApprovals", pendingSellerApprovals);
        model.addAttribute("totalProducts", totalProducts);

        return "dashboard/index";
    }
}