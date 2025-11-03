package com.example.ecoswap.model.enums;

public enum Role {
    CUSTOMER("Customer"),
    SELLER("Seller"),
    ADMIN("Admin");
    
    private String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public String replace(String string, String string2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replace'");
    }
}
