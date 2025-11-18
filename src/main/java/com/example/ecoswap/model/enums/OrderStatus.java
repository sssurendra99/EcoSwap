package com.example.ecoswap.model.enums;

public enum OrderStatus {
    PENDING("Pending", "Order placed, awaiting confirmation"),
    CONFIRMED("Confirmed", "Order confirmed, preparing for shipment"),
    PROCESSING("Processing", "Order is being processed"),
    SHIPPED("Shipped", "Order has been shipped"),
    DELIVERED("Delivered", "Order delivered successfully"),
    CANCELLED("Cancelled", "Order cancelled"),
    REFUNDED("Refunded", "Order refunded");

    private String displayName;
    private String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this status comes at or after another status in the order flow
     */
    public boolean isAtOrAfter(OrderStatus other) {
        return this.ordinal() >= other.ordinal();
    }

    /**
     * Check if order is in a completed state (delivered or cancelled)
     */
    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }
}
