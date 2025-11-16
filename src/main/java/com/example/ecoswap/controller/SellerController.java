package com.example.ecoswap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.OrderService;
import com.example.ecoswap.services.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Controller
public class SellerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/seller/dashboard")
    public String sellerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("title", "Seller Dashboard");

        User user = userDetails.getUser();
        Long sellerId = user.getId();

        // Get order statistics
        Map<String, Object> orderStats = orderService.getSellerOrderStatistics(sellerId);

        // Get product statistics
        Long totalProducts = productService.getSellerProductCount(sellerId);
        Long activeProducts = productService.getActiveProductCount(sellerId);
        List<Product> lowStockProducts = productService.getLowStockProducts(sellerId);

        // Get recent orders
        List<Order> recentOrders = orderService.getRecentOrdersBySeller(sellerId, 3);

        // Calculate CO2 saved from all products
        List<Product> allProducts = productService.getProductsBySeller(sellerId);
        double totalCo2Saved = allProducts.stream()
            .mapToDouble(p -> p.getCo2Saved() != null ? p.getCo2Saved() : 0.0)
            .sum();

        double totalPlasticSaved = allProducts.stream()
            .mapToDouble(p -> p.getPlasticSaved() != null ? p.getPlasticSaved() : 0.0)
            .sum();

        // Add all attributes to model
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        // Order stats
        model.addAttribute("totalOrders", orderStats.get("totalOrders"));
        model.addAttribute("pendingOrders", orderStats.get("pendingOrders"));
        model.addAttribute("totalRevenue", orderStats.get("totalRevenue"));
        model.addAttribute("monthlyRevenue", orderStats.get("monthlyRevenue"));
        model.addAttribute("recentOrders", recentOrders);

        // Product stats
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("lowStockCount", lowStockProducts.size());
        model.addAttribute("lowStockProducts", lowStockProducts);

        // Environmental impact
        model.addAttribute("totalCo2Saved", totalCo2Saved);
        model.addAttribute("totalPlasticSaved", totalPlasticSaved);
        model.addAttribute("itemsRecycled", orderStats.get("totalItemsSold"));

        return "dashboard/seller";
    }
}
