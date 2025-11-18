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
     * View profile - Redirects to role-specific settings page
     */
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        // Redirect to role-specific settings page which includes profile section
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/settings";
            case SELLER:
                return "redirect:/seller/settings";
            case CUSTOMER:
                return "redirect:/customer/settings";
            default:
                return "redirect:/";
        }
    }

    /**
     * Show edit profile form - Redirects to role-specific settings page
     */
    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        // Redirect to role-specific settings page
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/settings";
            case SELLER:
                return "redirect:/seller/settings";
            case CUSTOMER:
                return "redirect:/customer/settings";
            default:
                return "redirect:/";
        }
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

        // Redirect to role-specific settings page
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/settings";
            case SELLER:
                return "redirect:/seller/settings";
            case CUSTOMER:
                return "redirect:/customer/settings";
            default:
                return "redirect:/";
        }
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
            return "redirect:/seller/settings";
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

        return "redirect:/seller/settings";
    }

    /**
     * Show change password form - Redirects to role-specific settings page
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        // Redirect to role-specific settings page where password change should be available
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/settings";
            case SELLER:
                return "redirect:/seller/settings";
            case CUSTOMER:
                return "redirect:/customer/settings";
            default:
                return "redirect:/";
        }
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

        String settingsUrl = getSettingsUrlForRole(user.getRole());

        try {
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
                return "redirect:" + settingsUrl;
            }

            // Verify new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
                return "redirect:" + settingsUrl;
            }

            // Verify password strength
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long");
                return "redirect:" + settingsUrl;
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:" + settingsUrl;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing password: " + e.getMessage());
            return "redirect:" + settingsUrl;
        }
    }

    /**
     * Helper method to get settings URL for role
     */
    private String getSettingsUrlForRole(Role role) {
        switch (role) {
            case ADMIN:
                return "/admin/settings";
            case SELLER:
                return "/seller/settings";
            case CUSTOMER:
                return "/customer/settings";
            default:
                return "/";
        }
    }

    /**
     * View order history (for customers) - Redirects to dashboard orders
     */
    @GetMapping("/orders")
    public String viewOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // Redirect to dashboard orders page
        return "redirect:/dashboard/orders";
    }
}
