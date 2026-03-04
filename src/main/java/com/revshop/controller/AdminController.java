package com.revshop.controller;

import com.revshop.entity.Order;
import com.revshop.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    // ── Dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        logger.info("Admin dashboard accessed");
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/dashboard";
    }

    // ── Users ─────────────────────────────────────────────────
    @GetMapping("/users")
    public String users(Model model) {
        logger.info("Admin viewing all users");
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/block/{userId}")
    public String blockUser(
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin blocking userId: {}", userId);
        adminService.blockUser(userId);
        redirectAttributes.addFlashAttribute("successMessage",
                "User blocked successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/unblock/{userId}")
    public String unblockUser(
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin unblocking userId: {}", userId);
        adminService.unblockUser(userId);
        redirectAttributes.addFlashAttribute("successMessage",
                "User unblocked successfully.");
        return "redirect:/admin/users";
    }

    // ── Products ──────────────────────────────────────────────
    @GetMapping("/products")
    public String products(Model model) {
        logger.info("Admin viewing all products");
        model.addAttribute("products", productService.getAllActiveProducts());
        return "admin/products";
    }

    @PostMapping("/products/delete/{productId}")
    public String deleteProduct(
            @PathVariable Long productId,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin deleting productId: {}", productId);
        adminService.removeProduct(productId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Product removed successfully.");
        return "redirect:/admin/products";
    }

    // ── Categories ────────────────────────────────────────────
    @GetMapping("/categories")
    public String categories(Model model) {
        logger.info("Admin viewing categories");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin adding category: {}", name);
        try {
            categoryService.addCategory(name, description);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Category added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin deleting categoryId: {}", id);
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Category deleted successfully.");
        return "redirect:/admin/categories";
    }

    // ── Orders ────────────────────────────────────────────────
    @GetMapping("/orders")
    public String orders(Model model) {
        logger.info("Admin viewing all orders");
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        return "admin/orders";
    }

    @PostMapping("/orders/status/{orderId}")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin updating orderId: {} to status: {}", orderId, status);
        orderService.updateOrderStatus(orderId, status);
        redirectAttributes.addFlashAttribute("successMessage",
                "Order status updated successfully.");
        return "redirect:/admin/orders";
    }

    // ── Reviews ───────────────────────────────────────────────
    @PostMapping("/reviews/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            RedirectAttributes redirectAttributes) {
        logger.info("Admin deleting reviewId: {}", reviewId);
        adminService.removeReview(reviewId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Review removed successfully.");
        return "redirect:/admin/products";
    }
}