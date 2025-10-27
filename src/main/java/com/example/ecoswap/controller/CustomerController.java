package com.example.ecoswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {
    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model) {
        model.addAttribute("title", "Customer Dashboard");
        return "dashboard/customer"; // create simple template
    }
}
