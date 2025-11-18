package com.example.ecoswap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.ecoswap.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private String shippingAddress;

    @Column
    private String shippingCity;

    @Column
    private String shippingState;

    @Column
    private String shippingZipCode;

    @Column
    private String shippingCountry = "USA";

    @Column
    private String customerPhone;

    @Column
    private String customerEmail;

    @Column(length = 1000)
    private String orderNotes;

    @Column
    private String paymentMethod = "COD"; // COD, CARD, etc.

    @Column
    private String paymentStatus = "PENDING"; // PENDING, PAID, FAILED

    @Column
    private String trackingNumber;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to add order items
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // Helper method to remove order items
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    // Helper method to calculate total
    public void calculateTotal() {
        this.subtotal = orderItems.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Null safety for shipping cost and tax
        BigDecimal shipping = (this.shippingCost != null) ? this.shippingCost : BigDecimal.ZERO;
        BigDecimal taxAmount = (this.tax != null) ? this.tax : BigDecimal.ZERO;

        this.totalAmount = this.subtotal.add(shipping).add(taxAmount);
    }
}
