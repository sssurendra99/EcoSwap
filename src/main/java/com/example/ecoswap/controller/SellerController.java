package com.example.ecoswap.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.ecoswap.model.User;
import com.example.ecoswap.security.CustomUserDetails;


@Controller
public class SellerController {

    @GetMapping("/seller/dashboard")
    public String sellerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("title", "Seller Dashboard");

        User user = userDetails.getUser();
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        return "dashboard/seller";
    }
}
