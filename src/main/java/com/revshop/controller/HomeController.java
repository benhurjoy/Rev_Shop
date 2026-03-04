package com.revshop.controller;

import com.revshop.service.CategoryService;
import com.revshop.service.ProductService;
import com.revshop.service.ReviewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final Logger logger = LogManager.getLogger(HomeController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    // ── Public Home ───────────────────────────────────────────
    @GetMapping({"/", "/home"})
    public String home(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {

        logger.info("Home page accessed - keyword: {} categoryId: {}", keyword, categoryId);

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("products",
                    productService.searchProducts(keyword));
            model.addAttribute("keyword", keyword);
        } else if (categoryId != null) {
            model.addAttribute("products",
                    productService.filterByCategory(categoryId));
            model.addAttribute("selectedCategory", categoryId);
        } else {
            model.addAttribute("products",
                    productService.getAllActiveProducts());
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        return "home";
    }

    // ── Public Product Detail ─────────────────────────────────
    @GetMapping("/product/{productId}")
    public String productDetail(
            @PathVariable Long productId,
            Model model) {

        logger.info("Public product detail accessed for productId: {}", productId);
        model.addAttribute("product", productService.getProductById(productId));
        model.addAttribute("reviews", reviewService.getReviewsByProduct(productId));
        model.addAttribute("averageRating", reviewService.getAverageRating(productId));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product-detail";
    }
}
