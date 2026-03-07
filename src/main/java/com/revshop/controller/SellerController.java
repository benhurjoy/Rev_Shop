package com.revshop.controller;


import com.revshop.dto.ProductDTO;
import com.revshop.service.CategoryService;
import com.revshop.service.NotificationService;
import com.revshop.service.ProductService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private static final Logger logger = LogManager.getLogger(SellerController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private NotificationService notificationService;

    // uploadDir = "uploads/products" (from application.properties)
    @Value("${file.upload-dir}")
    private String uploadDir;

    // ── Dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Seller dashboard accessed by: {}", email);

        model.addAttribute("products",
                productService.getProductsBySeller(email));
        model.addAttribute("unreadCount",
                notificationService.getUnreadCount(email));
        return "seller/dashboard";
    }

    // ── View All My Products ──────────────────────────────────
    @GetMapping("/products")
    public String myProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Seller viewing products: {}", email);

        model.addAttribute("products",
                productService.getProductsBySeller(email));
        model.addAttribute("unreadCount",
                notificationService.getUnreadCount(email));
        return "seller/products";
    }

    // ── Add Product ───────────────────────────────────────────
    @GetMapping("/products/add")
    public String showAddProductPage(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "seller/add-product";
    }

    @PostMapping("/products/add")
    public String addProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "seller/add-product";
        }

        String email = userDetails.getUsername();
        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImage(image);
                productDTO.setImageUrl(imageUrl);
            }

            productService.addProduct(productDTO, email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Product added successfully!");
            logger.info("Product added by seller: {}", email);
            return "redirect:/seller/products";

        } catch (Exception e) {
            logger.error("Add product failed for seller: {} - {}", email, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "seller/add-product";
        }
    }

    // ── Edit Product ──────────────────────────────────────────
    @GetMapping("/products/edit/{productId}")
    public String showEditProductPage(
            @PathVariable Long productId,
            Model model) {

        logger.info("Edit product page for productId: {}", productId);
        model.addAttribute("productDTO",
                productService.getProductById(productId));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "seller/edit-product";
    }

    @PostMapping("/products/edit/{productId}")
    public String editProduct(
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "seller/edit-product";
        }

        String email = userDetails.getUsername();
        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImage(image);
                productDTO.setImageUrl(imageUrl);
            }

            productService.updateProduct(productId, productDTO, email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Product updated successfully!");
            logger.info("Product updated: {} by seller: {}", productId, email);
            return "redirect:/seller/products";

        } catch (Exception e) {
            logger.error("Update product failed - productId: {} - {}",
                    productId, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "seller/edit-product";
        }
    }

    // ── Delete Product ────────────────────────────────────────
    @PostMapping("/products/delete/{productId}")
    public String deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("Seller deleting productId: {} by: {}", productId, email);
        productService.deleteProduct(productId, email);
        redirectAttributes.addFlashAttribute("successMessage",
                "Product deleted successfully!");
        return "redirect:/seller/products";
    }

    // ── Toggle Product Visibility ─────────────────────────────
    @PostMapping("/products/toggle/{productId}")
    public String toggleVisibility(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("Toggle visibility productId: {} by: {}", productId, email);
        productService.toggleProductVisibility(productId, email);
        redirectAttributes.addFlashAttribute("successMessage",
                "Product visibility updated!");
        return "redirect:/seller/products";
    }

    // ── Notifications ─────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Seller viewing notifications: {}", email);
        model.addAttribute("notifications",
                notificationService.getNotifications(email));
        model.addAttribute("unreadCount",
                notificationService.getUnreadCount(email));
        return "seller/notifications";
    }

    @PostMapping("/notifications/read/{id}")
    public String markRead(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        logger.info("Seller marking notification read: {}", id);
        notificationService.markAsRead(id);
        return "redirect:/seller/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        notificationService.markAllAsRead(email);
        return "redirect:/seller/notifications";
    }

    // ── Low Stock ─────────────────────────────────────────────
    @GetMapping("/low-stock")
    public String lowStock(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Seller viewing low stock: {}", email);

        model.addAttribute("lowStockProducts",
                productService.getProductsBySeller(email)
                        .stream()
                        .filter(p -> p.getStockQuantity() < 5)
                        .toList());
        model.addAttribute("unreadCount",
                notificationService.getUnreadCount(email));
        return "seller/low-stock";
    }

    // ── Helper: Save Image ────────────────────────────────────
    private String saveImage(MultipartFile image) throws IOException {
        // uploadDir = "uploads/products" — this is where files are physically saved
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Sanitize filename and make it unique
        String originalName = image.getOriginalFilename() != null
                ? image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "image";
        String fileName = UUID.randomUUID() + "_" + originalName;

        // FIX: use REPLACE_EXISTING to avoid FileAlreadyExistsException on retries
        Files.copy(image.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        // FIX: return the web-accessible URL path.
        // WebConfig maps /uploads/** → the "uploads/" folder root.
        // So /uploads/products/filename.jpg → uploads/products/filename.jpg on disk. ✓
        String webPath = "/uploads/products/" + fileName;
        logger.info("Image saved to disk: {} | Web path: {}", uploadPath.resolve(fileName), webPath);
        return webPath;
    }
}

