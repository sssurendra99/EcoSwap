package com.example.ecoswap.dtos;


public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private String slug;
    private Boolean isActive;
    private Integer displayOrder;
    private Integer productCount;
    
    // Constructors
    public CategoryDTO() {}
    
    public CategoryDTO(Long id, String name, String description, String icon, 
                      String color, String slug, Boolean isActive, 
                      Integer displayOrder, Integer productCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
        this.slug = slug;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
        this.productCount = productCount;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }
}
