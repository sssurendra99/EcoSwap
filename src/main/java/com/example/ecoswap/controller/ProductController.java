package com.example.ecoswap.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ecoswap.model.Product;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.CategoryService;
import com.example.ecoswap.services.ProductService;

import java.util.List;

@Controller
@RequestMapping("/dashboard/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * List all products (with pagination and filtering)
     */
    @GetMapping
    public String listProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            Model model
    ) {
        User user = userDetails.getUser();
        
        Page<Product> productPage;
        
        // Role-based product listing
        if (user.getRole() == Role.ADMIN) {
            // Admin sees all products
            if (search != null && !search.isEmpty()) {
                productPage = productService.searchProducts(search, page, size);
            } else {
                productPage = productService.getAllProducts(page, size);
            }
        } else if (user.getRole() == Role.SELLER) {
            // Seller sees only their products
            if (search != null && !search.isEmpty()) {
                productPage = productService.searchProductsBySeller(user.getId(), search, page, size);
            } else {
                productPage = productService.getProductsBySeller(user.getId(), page, size);
            }
        } else {
            // Customers shouldn't access this page
            return "redirect:/dashboard";
        }
        
        // Add products and pagination to model
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        
        // Add statistics
        Long sellerId = user.getRole() == Role.SELLER ? user.getId() : null;
        if (sellerId != null) {
            model.addAttribute("totalProducts", productService.getSellerProductCount(sellerId));
            model.addAttribute("activeProducts", productService.getActiveProductCount(sellerId));
            model.addAttribute("lowStockProducts", productService.getLowStockProducts(sellerId).size());
            model.addAttribute("outOfStockProducts", productService.getOutOfStockProductCount());
        } else {
            model.addAttribute("totalProducts", productService.getTotalProductCount());
            model.addAttribute("activeProducts", productPage.getTotalElements());
            model.addAttribute("lowStockProducts", 0);
            model.addAttribute("outOfStockProducts", 0);
        }
        
        // Add categories for filter
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // Add user info
        model.addAttribute("pageTitle", "Products");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        
        return "dashboard/products";
    }
    
    /**
     * Show add product form
     */
    @GetMapping("/add")
    public String showAddForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        
        if (user.getRole() == Role.CUSTOMER) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Add New Product");
        
        return "dashboard/forms/product-form";
    }
    
    /**
     * Save new product
     */
    @PostMapping("/add")
    public String saveProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute Product product,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();
        product.setSeller(user);
        
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding product: " + e.getMessage());
        }
        
        return "redirect:/dashboard/products";
    }
    
    /**
     * Show edit product form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Edit Product");
        
        return "dashboard/forms/product-form";
    }
    
    /**
     * Update product
     */
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating product: " + e.getMessage());
        }
        
        return "redirect:/dashboard/products";
    }
    
    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * View product details
     */
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        model.addAttribute("product", product);
        model.addAttribute("pageTitle", product.getName());
        
        return "dashboard/product-details";
    }
    
    /**
     * Bulk actions
     */
    @PostMapping("/bulk-activate")
    public String bulkActivate(@RequestParam List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            productService.bulkActivate(ids);
            redirectAttributes.addFlashAttribute("successMessage", ids.size() + " products activated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error activating products");
        }
        return "redirect:/dashboard/products";
    }
    
    @PostMapping("/bulk-deactivate")
    public String bulkDeactivate(@RequestParam List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            productService.bulkDeactivate(ids);
            redirectAttributes.addFlashAttribute("successMessage", ids.size() + " products deactivated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating products");
        }
        return "redirect:/dashboard/products";
    }
    
    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam List<Long> ids, RedirectAttributes redirectAttributes) {
        try {
            productService.bulkDelete(ids);
            redirectAttributes.addFlashAttribute("successMessage", ids.size() + " products deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting products");
        }
        return "redirect:/dashboard/products";
    }
}