package com.example.ecoswap.services;

import com.example.ecoswap.model.User;
import com.example.ecoswap.model.PasswordResetToken;
import com.example.ecoswap.model.SellerProfile;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.repository.UserRepository;
import com.example.ecoswap.repository.PasswordResetTokenRepository;
import com.example.ecoswap.repository.SellerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get all users with pagination
    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable);
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get users by role
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Get users by role with pagination
    public Page<User> getUsersByRole(Role role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByRole(role, pageable);
    }

    // Search users
    public List<User> searchUsers(String search) {
        return userRepository.findByFullNameContainingOrEmailContaining(search, search);
    }

    // Count users by role
    public Long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }

    // Count total users
    public Long getTotalUserCount() {
        return userRepository.count();
    }

    // Enable/Disable user
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    // Delete user
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // Get pending seller approvals
    public List<SellerProfile> getPendingSellerApprovals() {
        return sellerProfileRepository.findByStatus("PENDING");
    }

    // Approve seller
    @Transactional
    public void approveSeller(Long sellerProfileId) {
        SellerProfile profile = sellerProfileRepository.findById(sellerProfileId)
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        profile.setStatus("APPROVED");
        sellerProfileRepository.save(profile);
    }

    // Reject seller
    @Transactional
    public void rejectSeller(Long sellerProfileId) {
        SellerProfile profile = sellerProfileRepository.findById(sellerProfileId)
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        profile.setStatus("REJECTED");
        sellerProfileRepository.save(profile);
    }

    // Get seller profile by user ID
    public Optional<SellerProfile> getSellerProfileByUserId(Long userId) {
        return sellerProfileRepository.findByUserId(userId);
    }

    // Get all seller profiles
    public List<SellerProfile> getAllSellerProfiles() {
        return sellerProfileRepository.findAll();
    }

    // Update user
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // ==================== Password Reset Methods ====================

    /**
     * Create a password reset token for the user
     * Token expires in 1 hour
     */
    @Transactional
    public String createPasswordResetToken(User user) {
        // Delete any existing tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Create token with 1 hour expiry
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);

        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    /**
     * Validate a password reset token
     * Returns the user if token is valid, empty otherwise
     */
    public Optional<User> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.findByToken(token);

        if (resetToken.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken passwordResetToken = resetToken.get();

        // Check if token is valid (not expired and not used)
        if (!passwordResetToken.isValid()) {
            return Optional.empty();
        }

        return Optional.of(passwordResetToken.getUser());
    }

    /**
     * Reset user password using a valid token
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (resetTokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOpt.get();

        // Validate token
        if (!resetToken.isValid()) {
            return false;
        }

        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Delete all other tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        return true;
    }

    /**
     * Clean up expired password reset tokens
     * This can be called periodically or as part of maintenance
     */
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
