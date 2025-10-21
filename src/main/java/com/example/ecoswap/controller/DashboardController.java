package com.example.ecoswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Admin Dashboard");
        return "dashboard/index";  // Just return the view name
    }
}