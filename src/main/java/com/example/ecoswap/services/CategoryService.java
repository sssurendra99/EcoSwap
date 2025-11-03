package com.example.ecoswap.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecoswap.dtos.CategoryDTO;
import com.example.ecoswap.model.Category;
import com.example.ecoswap.repository.CategoryRepository;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    /**
     * Get all active categories
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
    
    /**
     * Get all categories with product count
     */
    public List<CategoryDTO> getAllCategoriesWithProductCount() {
        return categoryRepository.findAllWithProductCount();
    }
    
    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * Get category by name
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * Get category by slug
     */
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }
    
    /**
     * Search categories
     */
    public List<Category> searchCategories(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAllCategories();
        }
        return categoryRepository.searchCategories(search);
    }
    
    /**
     * Save new category
     */
    @Transactional
    public Category saveCategory(Category category) {
        // Generate slug from name if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }
        
        // Validate unique name
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * Update category
     */
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(categoryDetails.getName())) {
            if (categoryRepository.existsByNameAndIdNot(categoryDetails.getName(), id)) {
                throw new RuntimeException("Category with name '" + categoryDetails.getName() + "' already exists");
            }
        }
        
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setIcon(categoryDetails.getIcon());
        category.setColor(categoryDetails.getColor());
        category.setSlug(categoryDetails.getSlug() != null ? categoryDetails.getSlug() : generateSlug(categoryDetails.getName()));
        category.setIsActive(categoryDetails.getIsActive());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        
        return categoryRepository.save(category);
    }
    
    /**
     * Delete category
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if category has products
        Long productCount = categoryRepository.countProductsByCategory(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with " + productCount + " products. " +
                                     "Please reassign or delete products first.");
        }
        
        categoryRepository.deleteById(id);
    }
    
    /**
     * Activate category
     */
    @Transactional
    public void activateCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setIsActive(true);
        categoryRepository.save(category);
    }
    
    /**
     * Deactivate category
     */
    @Transactional
    public void deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    
    /**
     * Get product count for a category
     */
    public Long getProductCount(Long categoryId) {
        return categoryRepository.countProductsByCategory(categoryId);
    }
    
    /**
     * Reorder categories
     */
    @Transactional
    public void reorderCategories(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            category.setDisplayOrder(i);
            categoryRepository.save(category);
        }
    }
    
    /**
     * Generate slug from name
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
            .replaceAll("\\s+", "-") // Replace spaces with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single
            .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }
    
    /**
     * Get total category count
     */
    public Long getTotalCategoryCount() {
        return categoryRepository.count();
    }
    
    /**
     * Get active category count
     */
    public Long getActiveCategoryCount() {
        return (long) categoryRepository.findByIsActiveTrue().size();
    }
    
    /**
     * Initialize default categories (call this on app startup if needed)
     */
    @Transactional
    public void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            createDefaultCategory("Reusable Products", "Sustainable alternatives to single-use items", "fa-recycle", "#1dbf73", 1);
            createDefaultCategory("Organic Food", "Organic and locally sourced food products", "fa-leaf", "#52c41a", 2);
            createDefaultCategory("Eco Fashion", "Sustainable clothing and accessories", "fa-tshirt", "#73d13d", 3);
            createDefaultCategory("Green Tech", "Eco-friendly technology and gadgets", "fa-laptop", "#95de64", 4);
            createDefaultCategory("Zero Waste", "Products for a zero-waste lifestyle", "fa-trash-alt", "#1dbf73", 5);
            createDefaultCategory("Home & Garden", "Sustainable home and garden products", "fa-home", "#52c41a", 6);
            createDefaultCategory("Personal Care", "Natural and organic personal care items", "fa-spa", "#73d13d", 7);
            createDefaultCategory("Second-Hand", "Pre-loved items giving products a second life", "fa-redo", "#95de64", 8);
        }
    }
    
    private void createDefaultCategory(String name, String description, String icon, String color, int order) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setColor(color);
        category.setDisplayOrder(order);
        category.setIsActive(true);
        categoryRepository.save(category);
    }
}
