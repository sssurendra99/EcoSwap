package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private String type; // SUCCESS, INFO, WARNING, ERROR

    @Column
    private String icon; // Font Awesome icon class

    @Column
    private String link; // URL to navigate when clicked

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }

    // Helper constructor
    public Notification(User user, String title, String message, String type, String icon, String link) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.icon = icon;
        this.link = link;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
