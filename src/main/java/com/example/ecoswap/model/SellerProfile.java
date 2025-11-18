package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seller_profiles")
public class SellerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String storeName;

    @Column
    private String businessName;

    @Column(length = 1000)
    private String businessDescription;

    @Column
    private String businessAddress;

    @Column
    private String businessPhone;

    @Column
    private String businessEmail;

    @Column
    private String taxId;

    @Column
    private String bankAccountName;

    @Column
    private String bankAccountNumber;

    @Column
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime approvedAt;

    public SellerProfile(User user, String storeName, String businessAddress) {
        this.user = user;
        this.storeName = storeName;
        this.businessAddress = businessAddress;
        this.createdAt = LocalDateTime.now();
    }
}
