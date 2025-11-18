package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.example.ecoswap.model.enums.Role;

@Getter
@Setter
@NoArgsConstructor  // ← ADD THIS - Required by JPA/Hibernate
@AllArgsConstructor // ← ADD THIS - For your custom constructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column
    private String phoneNumber;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // CUSTOMER, SELLER, ADMIN

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private boolean enabled = true;

    // Bidirectional relationships with profiles
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerProfile customerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerProfile sellerProfile;

    // Custom constructor for registration
    public User(String email, String password, String fullname, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.fullName = fullname;
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
    }

    // Helper methods for name handling
    public String getFirstName() {
        if (fullName != null && fullName.contains(" ")) {
            return fullName.substring(0, fullName.indexOf(" "));
        }
        return fullName;
    }

    public String getLastName() {
        if (fullName != null && fullName.contains(" ")) {
            return fullName.substring(fullName.indexOf(" ") + 1);
        }
        return "";
    }

    public void setFirstName(String firstName) {
        if (fullName != null && fullName.contains(" ")) {
            String lastName = getLastName();
            this.fullName = firstName + " " + lastName;
        } else {
            this.fullName = firstName;
        }
    }

    public void setLastName(String lastName) {
        if (fullName != null) {
            String firstName = getFirstName();
            this.fullName = firstName + " " + lastName;
        } else {
            this.fullName = lastName;
        }
    }
}
