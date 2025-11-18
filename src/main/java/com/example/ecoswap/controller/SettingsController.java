package com.example.ecoswap.controller;

import com.example.ecoswap.model.Settings;
import com.example.ecoswap.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for managing site settings (Admin only)
 */
@Controller
@RequestMapping("/admin/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * Show settings page
     */
    @GetMapping
    public String showSettings(Model model) {
        Settings settings = settingsService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("pageTitle", "Settings");
        return "admin/settings";
    }

    /**
     * Update settings
     */
    @PostMapping("/update")
    public String updateSettings(@ModelAttribute Settings settings,
                                 RedirectAttributes redirectAttributes) {
        try {
            settingsService.updateSettings(settings);
            redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update settings: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    /**
     * Reset settings to default
     */
    @PostMapping("/reset")
    public String resetSettings(RedirectAttributes redirectAttributes) {
        try {
            Settings defaultSettings = new Settings();
            settingsService.updateSettings(defaultSettings);
            redirectAttributes.addFlashAttribute("successMessage", "Settings reset to default values!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reset settings: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
