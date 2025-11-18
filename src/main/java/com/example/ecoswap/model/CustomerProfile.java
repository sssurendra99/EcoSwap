package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String fullName;

    @Column
    private String phoneNumber;

    @Column(length = 500)
    private String shippingAddress;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String zipCode;

    @Column
    private String country;

    // Legacy fields for compatibility
    @Deprecated
    @Column
    private String address;

    @Deprecated
    @Column
    private String phone;

    public CustomerProfile(User user, String fullName, String address, String phone) {
        this.user = user;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
    }
}
