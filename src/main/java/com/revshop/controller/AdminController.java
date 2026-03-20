package com.revshop.controller;

import com.revshop.dto.CategoryDTO;
import com.revshop.dto.CouponDTO;
import com.revshop.dto.ProductDTO;
import com.revshop.entity.Order;
import com.revshop.service.*;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);

    @Autowired private AdminService adminService;
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private CouponService couponService;

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
    public String blockUser(@PathVariable Long userId, RedirectAttributes ra) {
        logger.info("Admin blocking userId: {}", userId);
        adminService.blockUser(userId);
        ra.addFlashAttribute("successMessage", "User blocked successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/unblock/{userId}")
    public String unblockUser(@PathVariable Long userId, RedirectAttributes ra) {
        logger.info("Admin unblocking userId: {}", userId);
        adminService.unblockUser(userId);
        ra.addFlashAttribute("successMessage", "User unblocked successfully.");
        return "redirect:/admin/users";
    }

    // ── Products ──────────────────────────────────────────────
    @GetMapping("/products")
    public String products(Model model) {
        logger.info("Admin viewing all products");
        List<ProductDTO> products = productService.getAllActiveProducts();
        long activeCount     = products.stream().filter(ProductDTO::isActive).count();
        long lowStockCount   = products.stream().filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() <= 5).count();
        long outOfStockCount = products.stream().filter(p -> p.getStockQuantity() == 0).count();
        model.addAttribute("products", products);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        return "admin/products";
    }

    @PostMapping("/products/delete/{productId}")
    public String deleteProduct(@PathVariable Long productId, RedirectAttributes ra) {
        logger.info("Admin deleting productId: {}", productId);
        adminService.removeProduct(productId);
        ra.addFlashAttribute("successMessage", "Product removed successfully.");
        return "redirect:/admin/products";
    }

    // ── Categories ────────────────────────────────────────────
    @GetMapping("/categories")
    public String categories(Model model) {
        logger.info("Admin viewing categories");
        var categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("totalProductsAcrossCategories",
                categories.stream().mapToLong(c -> c.getProducts() != null ? c.getProducts().size() : 0L).sum());
        model.addAttribute("categoryDTO", new CategoryDTO());
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(
            @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        logger.info("Admin adding category: {}", categoryDTO.getName());
        if (result.hasErrors()) {
            var categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("totalProductsAcrossCategories",
                    categories.stream().mapToLong(c -> c.getProducts() != null ? c.getProducts().size() : 0L).sum());
            return "admin/categories";
        }
        try {
            categoryService.addCategory(categoryDTO.getName(), categoryDTO.getDescription(),
                    categoryDTO.isHasColors(), categoryDTO.isHasSizes());
            ra.addFlashAttribute("successMessage", "Category '" + categoryDTO.getName() + "' added successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ── NEW: update variant flags on an existing category ─────
    @PostMapping("/categories/update-variants/{id}")
    public String updateCategoryVariants(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean hasColors,
            @RequestParam(defaultValue = "false") boolean hasSizes,
            RedirectAttributes ra) {
        logger.info("Admin updating variants for categoryId: {} hasColors={} hasSizes={}", id, hasColors, hasSizes);
        try {
            categoryService.updateCategoryVariants(id, hasColors, hasSizes);
            ra.addFlashAttribute("successMessage", "Variant settings updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        logger.info("Admin deleting categoryId: {}", id);
        categoryService.deleteCategory(id);
        ra.addFlashAttribute("successMessage", "Category deleted successfully.");
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
            RedirectAttributes ra) {
        logger.info("Admin updating orderId: {} to status: {}", orderId, status);
        orderService.updateOrderStatus(orderId, status);
        ra.addFlashAttribute("successMessage", "Order status updated successfully.");
        return "redirect:/admin/orders";
    }

    // ── Reviews ───────────────────────────────────────────────
    @PostMapping("/reviews/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, RedirectAttributes ra) {
        logger.info("Admin deleting reviewId: {}", reviewId);
        adminService.removeReview(reviewId);
        ra.addFlashAttribute("successMessage", "Review removed successfully.");
        return "redirect:/admin/products";
    }

    // ── Coupons ───────────────────────────────────────────────
    @GetMapping("/coupons")
    public String coupons(Model model) {
        logger.info("Admin viewing coupons");
        model.addAttribute("coupons", couponService.getAllCoupons());
        model.addAttribute("couponDTO", new CouponDTO());
        return "admin/coupons";
    }

    @PostMapping("/coupons/add")
    public String addCoupon(
            @Valid @ModelAttribute("couponDTO") CouponDTO couponDTO,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        logger.info("Admin creating coupon: {}", couponDTO.getCode());
        if (result.hasErrors()) {
            model.addAttribute("coupons", couponService.getAllCoupons());
            return "admin/coupons";
        }
        try {
            couponService.createCoupon(couponDTO);
            ra.addFlashAttribute("successMessage",
                    "Coupon '" + couponDTO.getCode().toUpperCase() + "' created successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/toggle/{id}")
    public String toggleCoupon(@PathVariable Long id, RedirectAttributes ra) {
        logger.info("Admin toggling couponId: {}", id);
        try {
            couponService.toggleCoupon(id);
            ra.addFlashAttribute("successMessage", "Coupon status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable Long id, RedirectAttributes ra) {
        logger.info("Admin deleting couponId: {}", id);
        try {
            couponService.deleteCoupon(id);
            ra.addFlashAttribute("successMessage", "Coupon deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }
}