package com.example.ecoswap.controller;

import com.example.ecoswap.model.SellerProfile;
import com.example.ecoswap.model.User;
import com.example.ecoswap.repository.SellerProfileRepository;
import com.example.ecoswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Seller Settings
 */
@Controller
@RequestMapping("/seller/settings")
public class SellerSettingsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Show seller settings page
     */
    @GetMapping
    public String showSettings(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        SellerProfile sellerProfile = user.getSellerProfile();

        // Create seller profile if it doesn't exist
        if (sellerProfile == null) {
            sellerProfile = new SellerProfile();
            sellerProfile.setUser(user);
            sellerProfile.setStatus("APPROVED");
            sellerProfile = sellerProfileRepository.save(sellerProfile);
            user.setSellerProfile(sellerProfile);
        }

        model.addAttribute("user", user);
        model.addAttribute("sellerProfile", sellerProfile);
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("pageTitle", "Settings");
        return "seller/settings";
    }

    /**
     * Update seller profile information
     */
    @PostMapping("/update-profile")
    public String updateProfile(Authentication authentication,
                               @RequestParam String businessName,
                               @RequestParam String businessAddress,
                               @RequestParam String businessPhone,
                               @RequestParam(required = false) String businessDescription,
                               @RequestParam(required = false) String taxId,
                               @RequestParam(required = false) String bankAccountName,
                               @RequestParam(required = false) String bankAccountNumber,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();
            SellerProfile sellerProfile = user.getSellerProfile();

            // Create seller profile if it doesn't exist
            if (sellerProfile == null) {
                sellerProfile = new SellerProfile();
                sellerProfile.setUser(user);
                sellerProfile.setStatus("APPROVED");
            }

            sellerProfile.setBusinessName(businessName);
            sellerProfile.setBusinessAddress(businessAddress);
            sellerProfile.setBusinessPhone(businessPhone);
            sellerProfile.setBusinessDescription(businessDescription);
            sellerProfile.setTaxId(taxId);
            sellerProfile.setBankAccountName(bankAccountName);
            sellerProfile.setBankAccountNumber(bankAccountNumber);

            sellerProfileRepository.save(sellerProfile);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/seller/settings";
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
                    return "redirect:/seller/settings";
                }
                user.setEmail(email);
            }

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update account: " + e.getMessage());
        }
        return "redirect:/seller/settings";
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
                return "redirect:/seller/settings";
            }

            // Verify new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match!");
                return "redirect:/seller/settings";
            }

            // Validate new password length
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters!");
                return "redirect:/seller/settings";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change password: " + e.getMessage());
        }
        return "redirect:/seller/settings";
    }

    /**
     * Update notification preferences
     */
    @PostMapping("/update-notifications")
    public String updateNotifications(Authentication authentication,
                                     @RequestParam(required = false) Boolean emailNewOrder,
                                     @RequestParam(required = false) Boolean emailProductApproved,
                                     @RequestParam(required = false) Boolean emailLowStock,
                                     @RequestParam(required = false) Boolean emailNewReview,
                                     RedirectAttributes redirectAttributes) {
        try {
            // For now, just show success message
            // Later, implement actual notification preferences storage
            redirectAttributes.addFlashAttribute("successMessage", "Notification preferences updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update preferences: " + e.getMessage());
        }
        return "redirect:/seller/settings";
    }
}
