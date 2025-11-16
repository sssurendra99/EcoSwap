package com.example.ecoswap.controller;

import com.example.ecoswap.model.Product;
import com.example.ecoswap.services.CategoryService;
import com.example.ecoswap.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class PublicPagesController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "EcoSwap | Sustainable Marketplace");

        // Get featured products
        List<Product> featuredProducts = productService.getAllProducts(0, 8).getContent();

        // Get categories
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "public/default";
    }

    @GetMapping("/shop")
    public String shop(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            Model model
    ) {
        model.addAttribute("title", "Shop | EcoSwap");

        Page<Product> productPage;

        if (search != null && !search.isEmpty()) {
            productPage = productService.searchProducts(search, page, size);
        } else if (categoryId != null) {
            productPage = productService.getProductsByCategory(categoryId, page, size);
        } else {
            productPage = productService.getAllProducts(page, size);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategory", categoryId);

        return "public/shop";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("title", product.getName() + " | EcoSwap");
        model.addAttribute("product", product);

        // Related products from same category
        List<Product> relatedProducts = productService.getProductsByCategory(
            product.getCategory().getId(), 0, 4
        ).getContent();

        model.addAttribute("relatedProducts", relatedProducts);

        return "public/product-detail";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Us | EcoSwap");
        return "public/about";
    }

    @GetMapping("/contactus")
    public String contactus(Model model) {
        model.addAttribute("title", "Contact Us | EcoSwap");
        return "public/contact_us";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("title", "FAQ | EcoSwap");
        return "public/faq";
    }
}
