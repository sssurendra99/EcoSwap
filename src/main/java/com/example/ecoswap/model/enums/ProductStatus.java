package com.example.ecoswap.model.enums;

public enum ProductStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    OUT_OF_STOCK("Out of Stock"),
    PENDING_APPROVAL("Pending Approval");
    
    private String displayName;
    
    ProductStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
