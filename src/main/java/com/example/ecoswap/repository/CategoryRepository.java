package com.example.ecoswap.repository;

import com.example.ecoswap.dtos.CategoryDTO;
import com.example.ecoswap.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find by name
    Optional<Category> findByName(String name);
    
    // Find by slug
    Optional<Category> findBySlug(String slug);
    
    // Find active categories
    List<Category> findByIsActiveTrue();
    
    // Count active categories
    Long countByIsActiveTrue();
    
    // Find all categories ordered by display order
    List<Category> findAllByOrderByDisplayOrderAsc();
    
    // Find active categories ordered by display order
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    // Search categories
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Category> searchCategories(@Param("search") String search);
    
    // Count products in category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategory(@Param("categoryId") Long categoryId);
    
    // Get categories with product count - FIXED PACKAGE NAME
    @Query("SELECT new com.example.ecoswap.dtos.CategoryDTO(" +
           "c.id, c.name, c.description, c.icon, c.color, c.slug, " +
           "c.isActive, c.displayOrder, " +
           "CAST(COUNT(p) AS int)) " +
           "FROM Category c LEFT JOIN c.products p " +
           "GROUP BY c.id, c.name, c.description, c.icon, c.color, c.slug, c.isActive, c.displayOrder " +
           "ORDER BY c.displayOrder ASC")
    List<CategoryDTO> findAllWithProductCount();
    
    // Check if category name exists (excluding specific id)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}