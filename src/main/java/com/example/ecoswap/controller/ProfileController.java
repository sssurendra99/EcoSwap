package com.example.ecoswap.controller;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.SellerProfile;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.repository.SellerProfileRepository;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.OrderService;
import com.example.ecoswap.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    /**
     * View profile
     */
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        // Add seller profile if user is a seller
        if (user.getRole() == Role.SELLER) {
            Optional<SellerProfile> sellerProfile = sellerProfileRepository.findByUserId(user.getId());
            model.addAttribute("sellerProfile", sellerProfile.orElse(null));
        }

        // Add order history for customers
        if (user.getRole() == Role.CUSTOMER) {
            Page<Order> orders = orderService.getOrdersByCustomer(user.getId(), 0, 5);
            model.addAttribute("recentOrders", orders.getContent());
            model.addAttribute("totalOrders", orders.getTotalElements());
        }

        return "profile/view";
    }

    /**
     * Show edit profile form
     */
    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        // Add seller profile if user is a seller
        if (user.getRole() == Role.SELLER) {
            Optional<SellerProfile> sellerProfile = sellerProfileRepository.findByUserId(user.getId());
            model.addAttribute("sellerProfile", sellerProfile.orElse(new SellerProfile()));
        }

        return "profile/edit";
    }

    /**
     * Update profile
     */
    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setAddress(address);

            userService.updateUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Update seller business profile
     */
    @PostMapping("/update-business")
    public String updateBusinessProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String businessName,
            @RequestParam String businessDescription,
            @RequestParam String businessAddress,
            @RequestParam String businessPhone,
            @RequestParam(required = false) String businessEmail,
            @RequestParam(required = false) String taxId,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        if (user.getRole() != Role.SELLER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only sellers can update business profile");
            return "redirect:/profile";
        }

        try {
            Optional<SellerProfile> profileOpt = sellerProfileRepository.findByUserId(user.getId());
            SellerProfile sellerProfile;

            if (profileOpt.isPresent()) {
                sellerProfile = profileOpt.get();
            } else {
                sellerProfile = new SellerProfile();
                sellerProfile.setUser(user);
                sellerProfile.setStatus("APPROVED"); // If they're already a seller
            }

            sellerProfile.setBusinessName(businessName);
            sellerProfile.setBusinessDescription(businessDescription);
            sellerProfile.setBusinessAddress(businessAddress);
            sellerProfile.setBusinessPhone(businessPhone);
            sellerProfile.setBusinessEmail(businessEmail);
            sellerProfile.setTaxId(taxId);

            sellerProfileRepository.save(sellerProfile);

            redirectAttributes.addFlashAttribute("successMessage", "Business profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating business profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Show change password form
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();

        model.addAttribute("pageTitle", "Change Password");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        return "profile/change-password";
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
                return "redirect:/profile/change-password";
            }

            // Verify new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
                return "redirect:/profile/change-password";
            }

            // Verify password strength
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long");
                return "redirect:/profile/change-password";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing password: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    /**
     * View order history (for customers)
     */
    @GetMapping("/orders")
    public String viewOrderHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        User user = userDetails.getUser();

        Page<Order> orders = orderService.getOrdersByCustomer(user.getId(), page, size);

        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        model.addAttribute("pageTitle", "My Orders");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        return "profile/orders";
    }
}
