package com.example.ecoswap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String storeName;
    private String businessAddress;
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED

    public SellerProfile() {}
    public SellerProfile(User user, String storeName, String businessAddress) {
        this.user = user; this.storeName = storeName; this.businessAddress = businessAddress;
    }

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
