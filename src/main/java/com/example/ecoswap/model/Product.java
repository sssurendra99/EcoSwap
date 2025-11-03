package com.example.ecoswap.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column
    private BigDecimal originalPrice;
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Column
    private String image;
    
    // Use @ManyToOne relationships instead of just IDs
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @Column(nullable = false)
    private Integer ecoScore = 3;
    
    @Column
    private Double rating = 0.0;
    
    @Column
    private Integer reviewCount = 0;
    
    @Column(nullable = false)
    private String status = "ACTIVE";
    
    @Column
    private Boolean isNew = false;
    
    @Column
    private Boolean onSale = false;
    
    @Column
    private Boolean isFeatured = false;
    
    @Column
    private Double co2Saved = 0.0;
    
    @Column
    private Double plasticSaved = 0.0;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime updatedAt;
    
    // Constructors
    public Product() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    
    public Integer getEcoScore() { return ecoScore; }
    public void setEcoScore(Integer ecoScore) { this.ecoScore = ecoScore; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Boolean getIsNew() { return isNew; }
    public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    
    public Boolean getOnSale() { return onSale; }
    public void setOnSale(Boolean onSale) { this.onSale = onSale; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Double getCo2Saved() { return co2Saved; }
    public void setCo2Saved(Double co2Saved) { this.co2Saved = co2Saved; }
    
    public Double getPlasticSaved() { return plasticSaved; }
    public void setPlasticSaved(Double plasticSaved) { this.plasticSaved = plasticSaved; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}