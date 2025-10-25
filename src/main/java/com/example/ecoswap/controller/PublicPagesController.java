package com.example.ecoswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PublicPagesController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "EcoSwap | Home");
        return "public/default";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "EcoSwap | About");
        return "public/about";
    }
}
