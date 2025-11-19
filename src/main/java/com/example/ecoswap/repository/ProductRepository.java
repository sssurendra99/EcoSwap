package com.example.ecoswap.repository;

import com.example.ecoswap.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find by seller - FIXED: Use seller.id instead of sellerId
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);
    
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId")
    Page<Product> findBySellerIdPage(@Param("sellerId") Long sellerId, Pageable pageable);
    
    // Find by category - FIXED: Use category.id
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    // Find by status
    Page<Product> findByStatus(String status, Pageable pageable);
    Long countByStatus(String status);
    
    // Find by seller and status - FIXED
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status")
    Page<Product> findBySellerIdAndStatus(@Param("sellerId") Long sellerId, 
                                           @Param("status") String status, 
                                           Pageable pageable);
    
    // Count by seller - FIXED
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId")
    Long countBySellerId(@Param("sellerId") Long sellerId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status")
    Long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") String status);
    
    // Count by seller and stock
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.stock = :stock")
    Long countBySellerIdAndStock(@Param("sellerId") Long sellerId, @Param("stock") Integer stock);
    
    // Count by stock
    Long countByStock(Integer stock);
    
    // Count by category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    // Low stock products - FIXED
    @Query("SELECT p FROM Product p WHERE p.stock <= 10 AND p.stock > 0 AND (:sellerId IS NULL OR p.seller.id = :sellerId)")
    List<Product> findLowStockProducts(@Param("sellerId") Long sellerId);
    
    // Search products - FIXED
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);
    
    // Search products by seller - FIXED: Use seller.id
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProductsBySeller(@Param("sellerId") Long sellerId, 
                                         @Param("search") String search, 
                                         Pageable pageable);
    
    // Top rated products
    @Query("SELECT p FROM Product p ORDER BY p.rating DESC")
    List<Product> findTopRatedProducts(Pageable pageable);
    
    // Featured products
    List<Product> findByIsFeaturedTrue();
    
    // New products
    List<Product> findByIsNewTrue();
    
    // Products on sale
    List<Product> findByOnSaleTrue();

    // ============ ENVIRONMENTAL IMPACT QUERIES ============

    // Sum of CO2 saved across all products
    @Query("SELECT SUM(p.co2Saved) FROM Product p WHERE p.co2Saved IS NOT NULL")
    Double sumCo2Saved();

    // Sum of plastic saved across all products
    @Query("SELECT SUM(p.plasticSaved) FROM Product p WHERE p.plasticSaved IS NOT NULL")
    Double sumPlasticSaved();

    // Average eco score across all products
    @Query("SELECT AVG(p.ecoScore) FROM Product p WHERE p.ecoScore IS NOT NULL")
    Double averageEcoScore();
}