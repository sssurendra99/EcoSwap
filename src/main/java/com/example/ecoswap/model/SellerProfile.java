package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
        this.user = user; 
        this.storeName = storeName; 
        this.businessAddress = businessAddress;
    }

}
