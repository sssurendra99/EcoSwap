package com.example.ecoswap.controller;

import com.example.ecoswap.model.SellerProfile;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * List all users with filters
     */
    @GetMapping
    public String listUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model
    ) {
        User currentUser = userDetails.getUser();

        Page<User> userPage;

        // Filter by role and search
        if (search != null && !search.isEmpty()) {
            List<User> searchResults = userService.searchUsers(search);
            // Convert list to page manually or use PageImpl if needed
            // For simplicity, showing all search results
            model.addAttribute("users", searchResults);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", searchResults.size());
        } else if (role != null && !role.isEmpty() && !role.equals("ALL")) {
            Role userRole = Role.valueOf(role);
            userPage = userService.getUsersByRole(userRole, page, size);
            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userPage.getTotalPages());
            model.addAttribute("totalItems", userPage.getTotalElements());
        } else {
            userPage = userService.getAllUsers(page, size);
            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userPage.getTotalPages());
            model.addAttribute("totalItems", userPage.getTotalElements());
        }

        // Statistics
        model.addAttribute("totalUsers", userService.getTotalUserCount());
        model.addAttribute("totalAdmins", userService.countUsersByRole(Role.ADMIN));
        model.addAttribute("totalSellers", userService.countUsersByRole(Role.SELLER));
        model.addAttribute("totalCustomers", userService.countUsersByRole(Role.CUSTOMER));

        // Filters
        model.addAttribute("selectedRole", role);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("userName", currentUser.getFullName());
        model.addAttribute("userRole", currentUser.getRole().getDisplayName());

        return "admin/users/list";
    }

    /**
     * View user details
     */
    @GetMapping("/{id}")
    public String viewUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model
    ) {
        User currentUser = userDetails.getUser();
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);

        // Get seller profile if user is a seller
        if (user.getRole() == Role.SELLER) {
            userService.getSellerProfileByUserId(user.getId())
                    .ifPresent(profile -> model.addAttribute("sellerProfile", profile));
        }

        model.addAttribute("pageTitle", "User Details");
        model.addAttribute("userName", currentUser.getFullName());
        model.addAttribute("userRole", currentUser.getRole().getDisplayName());

        return "admin/users/detail";
    }

    /**
     * Toggle user status (enable/disable)
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user status: " + e.getMessage());
        }

        return "redirect:/admin/users/" + id;
    }

    /**
     * Delete user
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }

    /**
     * Seller approvals page
     */
    @GetMapping("/seller-approvals")
    public String sellerApprovals(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User currentUser = userDetails.getUser();

        List<SellerProfile> pendingApprovals = userService.getPendingSellerApprovals();
        List<SellerProfile> allSellerProfiles = userService.getAllSellerProfiles();

        model.addAttribute("pendingApprovals", pendingApprovals);
        model.addAttribute("allSellerProfiles", allSellerProfiles);
        model.addAttribute("totalPending", pendingApprovals.size());

        model.addAttribute("pageTitle", "Seller Approvals");
        model.addAttribute("userName", currentUser.getFullName());
        model.addAttribute("userRole", currentUser.getRole().getDisplayName());

        return "admin/users/seller-approvals";
    }

    /**
     * Approve seller
     */
    @PostMapping("/seller-approvals/{id}/approve")
    public String approveSeller(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.approveSeller(id);
            redirectAttributes.addFlashAttribute("successMessage", "Seller approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving seller: " + e.getMessage());
        }

        return "redirect:/admin/users/seller-approvals";
    }

    /**
     * Reject seller
     */
    @PostMapping("/seller-approvals/{id}/reject")
    public String rejectSeller(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.rejectSeller(id);
            redirectAttributes.addFlashAttribute("successMessage", "Seller application rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting seller: " + e.getMessage());
        }

        return "redirect:/admin/users/seller-approvals";
    }
}
