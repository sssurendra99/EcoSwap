package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String fullName;
    private String address;
    private String phone;

    public CustomerProfile() {}
    public CustomerProfile(User user, String fullName, String address, String phone) {
        this.user = user; this.fullName = fullName; this.address = address; this.phone = phone;
    }

}
