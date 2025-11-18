package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Settings Entity
 * Stores site-wide configuration settings
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // General Settings
    @Column(name = "shop_name", nullable = false)
    private String shopName = "EcoSwap";

    @Column(name = "shop_tagline")
    private String shopTagline = "Your sustainable marketplace for eco-friendly products";

    @Column(name = "shop_description", length = 1000)
    private String shopDescription = "Join our community and make a positive impact on the planet";

    @Column(name = "contact_email")
    private String contactEmail = "support@ecoswap.com";

    @Column(name = "support_phone")
    private String supportPhone = "+1 (555) 123-4567";

    @Column(name = "address", length = 500)
    private String address;

    // Currency Settings
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "USD";

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol = "$";

    @Column(name = "currency_position", nullable = false)
    private String currencyPosition = "before"; // before or after

    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;

    // Product Settings
    @Column(name = "products_per_page")
    private Integer productsPerPage = 12;

    @Column(name = "allow_reviews")
    private Boolean allowReviews = true;

    @Column(name = "auto_approve_products")
    private Boolean autoApproveProducts = false;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 5;

    // Order Settings
    @Column(name = "order_prefix")
    private String orderPrefix = "ECO";

    @Column(name = "minimum_order_amount")
    private Double minimumOrderAmount = 0.0;

    @Column(name = "tax_rate")
    private Double taxRate = 0.0;

    @Column(name = "shipping_fee")
    private Double shippingFee = 0.0;

    @Column(name = "free_shipping_threshold")
    private Double freeShippingThreshold = 100.0;

    // Email Settings
    @Column(name = "email_notifications_enabled")
    private Boolean emailNotificationsEnabled = true;

    @Column(name = "admin_notification_email")
    private String adminNotificationEmail;

    @Column(name = "send_order_confirmation")
    private Boolean sendOrderConfirmation = true;

    @Column(name = "send_shipping_notification")
    private Boolean sendShippingNotification = true;

    // Appearance Settings
    @Column(name = "primary_color")
    private String primaryColor = "#11998e";

    @Column(name = "secondary_color")
    private String secondaryColor = "#38ef7d";

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "favicon_url")
    private String faviconUrl;

    // Social Media
    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    // SEO Settings
    @Column(name = "meta_title")
    private String metaTitle = "EcoSwap - Sustainable Marketplace";

    @Column(name = "meta_description", length = 500)
    private String metaDescription = "Buy and sell eco-friendly products on EcoSwap";

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords = "eco-friendly, sustainable, marketplace, green products";

    // Maintenance
    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode = false;

    @Column(name = "maintenance_message", length = 1000)
    private String maintenanceMessage = "We are currently performing maintenance. Please check back soon.";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Format price with currency symbol and position
     */
    public String formatPrice(Double amount) {
        if (amount == null) {
            amount = 0.0;
        }
        String formattedAmount = String.format("%." + decimalPlaces + "f", amount);

        if ("after".equalsIgnoreCase(currencyPosition)) {
            return formattedAmount + " " + currencySymbol;
        } else {
            return currencySymbol + formattedAmount;
        }
    }

    /**
     * Get full shop title for meta tags
     */
    public String getFullTitle() {
        return shopName + " - " + shopTagline;
    }
}
