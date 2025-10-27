package com.example.ecoswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(Model model, String error) {
        if (error != null) model.addAttribute("error", "Invalid username or password");
        return "auth/login";
    }
}
