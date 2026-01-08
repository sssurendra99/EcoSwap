package com.example.ecoswap.controller;

import com.example.ecoswap.model.User;
import com.example.ecoswap.services.EmailService;
import com.example.ecoswap.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    /**
     * Display forgot password form
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    /**
     * Process forgot password request
     * Send password reset email if user exists
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            // For security reasons, don't reveal if email exists or not
            // Show success message regardless
            redirectAttributes.addFlashAttribute("message",
                    "If an account exists with this email, you will receive a password reset link shortly.");
            return "redirect:/forgot-password";
        }

        User user = userOpt.get();

        try {
            // Generate reset token
            String resetToken = userService.createPasswordResetToken(user);

            // Build base URL from request
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if ((request.getScheme().equals("http") && request.getServerPort() != 80) ||
                    (request.getScheme().equals("https") && request.getServerPort() != 443)) {
                baseUrl += ":" + request.getServerPort();
            }

            // Send password reset email
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    resetToken,
                    baseUrl
            );

            redirectAttributes.addFlashAttribute("message",
                    "If an account exists with this email, you will receive a password reset link shortly.");

        } catch (Exception e) {
            System.err.println("Error processing password reset: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while processing your request. Please try again.");
        }

        return "redirect:/forgot-password";
    }

    /**
     * Display reset password form
     * Validates token and shows form if valid
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam("token") String token,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.validatePasswordResetToken(token);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid or expired password reset link. Please request a new one.");
            return "redirect:/forgot-password";
        }

        model.addAttribute("token", token);
        model.addAttribute("email", userOpt.get().getEmail());

        return "auth/reset-password";
    }

    /**
     * Process password reset
     * Update user password if token is valid
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }

        // Validate password strength (minimum 6 characters)
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error",
                    "Password must be at least 6 characters long.");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }

        // Reset password
        boolean success = userService.resetPassword(token, password);

        if (!success) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid or expired password reset link. Please request a new one.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("message",
                "Your password has been successfully reset. You can now login with your new password.");

        return "redirect:/login";
    }
}
