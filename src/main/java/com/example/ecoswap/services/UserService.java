package com.example.ecoswap.services;

import com.example.ecoswap.model.User;
import com.example.ecoswap.model.SellerProfile;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.repository.UserRepository;
import com.example.ecoswap.repository.SellerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

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
}
