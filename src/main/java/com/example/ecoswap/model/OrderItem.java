package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price; // Price at time of order

    @Column(nullable = false)
    private BigDecimal lineTotal; // quantity * price

    @Column
    private String productName; // Store product name in case product is deleted

    @Column
    private String productSku;

    @Column
    private String productImage;

    @PrePersist
    @PreUpdate
    public void calculateLineTotal() {
        if (price != null && quantity != null) {
            this.lineTotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
