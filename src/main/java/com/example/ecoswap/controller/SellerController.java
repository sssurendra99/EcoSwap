package com.example.ecoswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SellerController {
    @GetMapping("/seller/dashboard")
    public String sellerDashboard(Model model) {
        model.addAttribute("title", "Seller Dashboard");
        return "dashboard/seller";
    }
}
