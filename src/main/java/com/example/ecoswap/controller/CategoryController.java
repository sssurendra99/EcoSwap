package com.example.ecoswap.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ecoswap.dtos.CategoryDTO;
import com.example.ecoswap.model.Category;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.CategoryService;

import java.util.List;

@Controller
@RequestMapping("/dashboard/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * List all categories (Admin only)
     */
    @GetMapping
    public String listCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String search,
            Model model
    ) {
        User user = userDetails.getUser();
        
        // Only admins can manage categories
        if (user.getRole() == Role.CUSTOMER) {
            return "redirect:/dashboard";
        }
        
        List<CategoryDTO> categories;
        if (search != null && !search.isEmpty()) {
            List<Category> searchResults = categoryService.searchCategories(search);
            categories = searchResults.stream()
                .map(c -> new CategoryDTO(
                    c.getId(), c.getName(), c.getDescription(), c.getIcon(),
                    c.getColor(), c.getSlug(), c.getIsActive(), c.getDisplayOrder(),
                    c.getProductCount()
                ))
                .toList();
        } else {
            categories = categoryService.getAllCategoriesWithProductCount();
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("totalCategories", categoryService.getTotalCategoryCount());
        model.addAttribute("activeCategories", categoryService.getActiveCategoryCount());
        model.addAttribute("pageTitle", "Categories");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        
        return "dashboard/categories";
    }
    
    /**
     * Show add category form
     */
    @GetMapping("/add")
    public String showAddForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        
        if (user.getRole() == Role.CUSTOMER) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add New Category");
        
        return "dashboard/forms/category-form";
    }
    
    /**
     * Save new category
     */
    @PostMapping("/add")
    public String saveCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute Category category,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();
        
        if (user.getRole() == Role.CUSTOMER) {
            return "redirect:/dashboard";
        }
        
        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        
        return "redirect:/dashboard/categories";
    }
    
    /**
     * Show edit category form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        model.addAttribute("category", category);
        model.addAttribute("productCount", categoryService.getProductCount(id));
        model.addAttribute("pageTitle", "Edit Category");
        
        return "dashboard/forms/category-form";
    }
    
    /**
     * Update category
     */
    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @ModelAttribute Category category,
            RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.updateCategory(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        
        return "redirect:/dashboard/categories";
    }
    
    /**
     * Delete category
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().body("{\"success\": true}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Activate category
     */
    @PostMapping("/{id}/activate")
    public String activateCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.activateCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category activated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/dashboard/categories";
    }
    
    /**
     * Deactivate category
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deactivateCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deactivated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/dashboard/categories";
    }
    
    /**
     * Reorder categories
     */
    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderCategories(@RequestBody List<Long> categoryIds) {
        try {
            categoryService.reorderCategories(categoryIds);
            return ResponseEntity.ok().body("{\"success\": true}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Get category details (AJAX)
     */
    @GetMapping("/{id}/details")
    @ResponseBody
    public ResponseEntity<Category> getCategoryDetails(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        return ResponseEntity.ok(category);
    }
}
