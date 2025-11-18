package com.example.ecoswap.controller;

import com.example.ecoswap.model.CustomerProfile;
import com.example.ecoswap.model.User;
import com.example.ecoswap.repository.CustomerProfileRepository;
import com.example.ecoswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Customer Settings
 */
@Controller
@RequestMapping("/customer/settings")
public class CustomerSettingsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Show customer settings page
     */
    @GetMapping
    public String showSettings(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        CustomerProfile customerProfile = user.getCustomerProfile();

        model.addAttribute("user", user);
        model.addAttribute("customerProfile", customerProfile);
        model.addAttribute("pageTitle", "Settings");
        return "customer/settings";
    }

    /**
     * Update customer profile information
     */
    @PostMapping("/update-profile")
    public String updateProfile(Authentication authentication,
                               @RequestParam String phoneNumber,
                               @RequestParam(required = false) String shippingAddress,
                               @RequestParam(required = false) String city,
                               @RequestParam(required = false) String state,
                               @RequestParam(required = false) String zipCode,
                               @RequestParam(required = false) String country,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();
            CustomerProfile customerProfile = user.getCustomerProfile();

            customerProfile.setPhoneNumber(phoneNumber);
            customerProfile.setShippingAddress(shippingAddress);
            customerProfile.setCity(city);
            customerProfile.setState(state);
            customerProfile.setZipCode(zipCode);
            customerProfile.setCountry(country);

            customerProfileRepository.save(customerProfile);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/customer/settings";
    }

    /**
     * Update account information (name, email)
     */
    @PostMapping("/update-account")
    public String updateAccount(Authentication authentication,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               RedirectAttributes redirectAttributes) {
        try {
            String currentEmail = authentication.getName();
            User user = userRepository.findByEmail(currentEmail).orElseThrow();

            user.setFirstName(firstName);
            user.setLastName(lastName);

            // Check if email is being changed and if new email is already taken
            if (!currentEmail.equals(email)) {
                if (userRepository.findByEmail(email).isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Email already in use!");
                    return "redirect:/customer/settings";
                }
                user.setEmail(email);
            }

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update account: " + e.getMessage());
        }
        return "redirect:/customer/settings";
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
                                @RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect!");
                return "redirect:/customer/settings";
            }

            // Verify new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match!");
                return "redirect:/customer/settings";
            }

            // Validate new password length
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters!");
                return "redirect:/customer/settings";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change password: " + e.getMessage());
        }
        return "redirect:/customer/settings";
    }

    /**
     * Update notification preferences
     */
    @PostMapping("/update-notifications")
    public String updateNotifications(Authentication authentication,
                                     @RequestParam(required = false) Boolean emailOrderStatus,
                                     @RequestParam(required = false) Boolean emailPromotions,
                                     @RequestParam(required = false) Boolean emailNewProducts,
                                     @RequestParam(required = false) Boolean emailNewsletter,
                                     RedirectAttributes redirectAttributes) {
        try {
            // For now, just show success message
            // Later, implement actual notification preferences storage
            redirectAttributes.addFlashAttribute("successMessage", "Notification preferences updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update preferences: " + e.getMessage());
        }
        return "redirect:/customer/settings";
    }
}
