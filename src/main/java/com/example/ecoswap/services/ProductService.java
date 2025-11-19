package com.example.ecoswap.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecoswap.model.Product;
import com.example.ecoswap.repository.ProductRepository;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    // Get all products with pagination
    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable);
    }
    
    // Get product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    // Get products by seller
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
    
    // Get products by seller with pagination - FIXED
    public Page<Product> getProductsBySeller(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findBySellerIdPage(sellerId, pageable); // Changed method name
    }
    
    // Get products by category
    public Page<Product> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    // Get products by status - FIXED: Changed to String
    public Page<Product> getProductsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByStatus(status, pageable);
    }
    
    // Search products
    public Page<Product> searchProducts(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchProducts(search, pageable);
    }
    
    // Search products by seller
    public Page<Product> searchProductsBySeller(Long sellerId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchProductsBySeller(sellerId, search, pageable);
    }
    
    // Save product
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    // Update product
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setOriginalPrice(productDetails.getOriginalPrice());
        product.setStock(productDetails.getStock());
        product.setImage(productDetails.getImage());
        product.setCategory(productDetails.getCategory());
        product.setEcoScore(productDetails.getEcoScore());
        product.setStatus(productDetails.getStatus());
        product.setCo2Saved(productDetails.getCo2Saved());
        product.setPlasticSaved(productDetails.getPlasticSaved());
        
        return productRepository.save(product);
    }
    
    // Delete product
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    // Activate product - FIXED: Changed to String
    @Transactional
    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus("ACTIVE"); // Changed from enum to String
        productRepository.save(product);
    }
    
    // Deactivate product - FIXED: Changed to String
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus("INACTIVE"); // Changed from enum to String
        productRepository.save(product);
    }
    
    // Bulk activate
    @Transactional
    public void bulkActivate(List<Long> ids) {
        ids.forEach(this::activateProduct);
    }
    
    // Bulk deactivate
    @Transactional
    public void bulkDeactivate(List<Long> ids) {
        ids.forEach(this::deactivateProduct);
    }
    
    // Bulk delete
    @Transactional
    public void bulkDelete(List<Long> ids) {
        ids.forEach(this::deleteProduct);
    }
    
    // Statistics
    public Long getTotalProductCount() {
        return productRepository.count();
    }
    
    public Long getSellerProductCount(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }
    
    // FIXED: Changed to String
    public Long getActiveProductCount(Long sellerId) {
        return productRepository.countBySellerIdAndStatus(sellerId, "ACTIVE");
    }

    /**
     * Count products by status (platform-wide)
     */
    public Long countByStatus(String status) {
        return productRepository.countByStatus(status);
    }

    public List<Product> getLowStockProducts(Long sellerId) {
        return productRepository.findLowStockProducts(sellerId);
    }

    // FIXED: Changed method name
    public Long getOutOfStockProductCount() {
        return productRepository.countByStock(0);
    }
    
    public List<Product> getTopRatedProducts(int limit) {
        return productRepository.findTopRatedProducts(PageRequest.of(0, limit));
    }

    // ============ ENVIRONMENTAL IMPACT CALCULATIONS ============

    /**
     * Calculate total CO2 saved across all products
     */
    public Double calculateTotalCo2Saved() {
        Double total = productRepository.sumCo2Saved();
        return total != null ? total : 0.0;
    }

    /**
     * Calculate total plastic saved across all products
     */
    public Double calculateTotalPlasticSaved() {
        Double total = productRepository.sumPlasticSaved();
        return total != null ? total : 0.0;
    }

    /**
     * Get average eco score across all products
     */
    public Double getAverageEcoScore() {
        Double avg = productRepository.averageEcoScore();
        return avg != null ? avg : 0.0;
    }
}