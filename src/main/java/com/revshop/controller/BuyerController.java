package com.revshop.controller;

import com.revshop.dto.ReviewDTO;
import com.revshop.dto.UserDTO;
import com.revshop.entity.User;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.UserRepository;
import com.revshop.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequestMapping("/buyer")
@PreAuthorize("hasRole('BUYER')")
public class BuyerController {

    private static final Logger logger = LogManager.getLogger(BuyerController.class);

    @Autowired private ProductService productService;
    @Autowired private CartService cartService;
    @Autowired private WishlistService wishlistService;
    @Autowired private ReviewService reviewService;
    @Autowired private NotificationService notificationService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Helper: build UserDTO from email ─────────────────────
    private UserDTO getCurrentUserDTO(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setBlocked(user.isBlocked());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // ── Home ─────────────────────────────────────────────────
    @GetMapping("/home")
    public String home(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer home accessed by: {}", email);

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("products", productService.searchProducts(keyword));
            model.addAttribute("keyword", keyword);
        } else if (categoryId != null) {
            model.addAttribute("products", productService.filterByCategory(categoryId));
            model.addAttribute("selectedCategory", categoryId);
        } else {
            model.addAttribute("products", productService.getAllActiveProducts());
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("currentUser", getCurrentUserDTO(email));
        return "buyer/home";
    }

    // ── Profile ───────────────────────────────────────────────
    @GetMapping("/profile")
    public String viewProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing profile: {}", email);

        model.addAttribute("currentUser", getCurrentUserDTO(email));
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        return "buyer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("Buyer updating profile: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            logger.error("Profile update failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
        }
        return "redirect:/buyer/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("Buyer changing password: {}", email);

        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("pwError", "New passwords do not match.");
                return "redirect:/buyer/profile";
            }
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("pwError", "Password must be at least 6 characters.");
                return "redirect:/buyer/profile";
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("pwError", "Current password is incorrect.");
                return "redirect:/buyer/profile";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("pwSuccess", "Password changed successfully!");

        } catch (Exception e) {
            logger.error("Password change failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("pwError", "Failed: " + e.getMessage());
        }
        return "redirect:/buyer/profile";
    }

    // ── Product Detail ────────────────────────────────────────
    @GetMapping("/product/{productId}")
    public String productDetail(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing product: {} by: {}", productId, email);

        model.addAttribute("product", productService.getProductById(productId));
        model.addAttribute("reviews", reviewService.getReviewsByProduct(productId));
        model.addAttribute("averageRating", reviewService.getAverageRating(productId));
        model.addAttribute("hasReviewed", reviewService.hasAlreadyReviewed(email, productId));
        model.addAttribute("hasPurchased", reviewService.hasPurchasedProduct(email, productId));
        model.addAttribute("myReview", reviewService.getReviewByBuyer(email, productId)); // ← NEW
        model.addAttribute("isInWishlist", wishlistService.isInWishlist(email, productId));
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("currentUser", getCurrentUserDTO(email));
        return "buyer/product-detail";
    }

    // ── Cart ─────────────────────────────────────────────────
    @GetMapping("/cart")
    public String viewCart(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing cart: {}", email);

        BigDecimal total = cartService.calculateTotal(email);
        BigDecimal gst = total.multiply(new BigDecimal("0.18"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = total.add(gst)
                .setScale(2, RoundingMode.HALF_UP);

        model.addAttribute("cartItems", cartService.getCartItems(email));
        model.addAttribute("total", total);
        model.addAttribute("gst", gst);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("currentUser", getCurrentUserDTO(email));
        return "buyer/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("AddToCart by: {} productId: {} qty: {}", email, productId, quantity);
        try {
            cartService.addToCart(email, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart!");
        } catch (Exception e) {
            logger.error("AddToCart failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/cart";
    }

    @PostMapping("/cart/remove/{cartItemId}")
    public String removeFromCart(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("RemoveFromCart by: {} cartItemId: {}", email, cartItemId);
        try {
            cartService.removeFromCart(email, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/cart";
    }

    @PostMapping("/cart/update/{cartItemId}")
    public String updateCartQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("UpdateCart by: {} cartItemId: {} qty: {}", email, cartItemId, quantity);
        try {
            cartService.updateQuantity(email, cartItemId, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/cart";
    }

    // ── Wishlist ──────────────────────────────────────────────
    @GetMapping("/wishlist")
    public String viewWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing wishlist: {}", email);

        model.addAttribute("wishlist", wishlistService.getOrCreateWishlist(email));
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("currentUser", getCurrentUserDTO(email));
        return "buyer/wishlist";
    }

    @PostMapping("/wishlist/add/{productId}")
    public String addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("AddToWishlist by: {} productId: {}", email, productId);
        try {
            wishlistService.addToWishlist(email, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Added to wishlist!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/wishlist";
    }

    @PostMapping("/wishlist/remove/{productId}")
    public String removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("RemoveFromWishlist by: {} productId: {}", email, productId);
        wishlistService.removeFromWishlist(email, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Removed from wishlist.");
        return "redirect:/buyer/wishlist";
    }

    @PostMapping("/wishlist/move-to-cart/{productId}")
    public String moveToCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("MoveToCart by: {} productId: {}", email, productId);
        try {
            wishlistService.moveToCart(email, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Moved to cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/cart";
    }

    // ── Reviews ───────────────────────────────────────────────
    @PostMapping("/review/add")
    public String addReview(
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("AddReview by: {} for productId: {}", email, productId);
        try {
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setProductId(productId);
            reviewDTO.setRating(rating);
            reviewDTO.setComment(comment);
            reviewService.addReview(email, reviewDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/product/" + productId;
    }

    // ── Edit Review ── NEW ────────────────────────────────────
    @PostMapping("/review/edit")
    public String editReview(
            @RequestParam Long reviewId,
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("EditReview by: {} reviewId: {}", email, reviewId);
        try {
            reviewService.editReview(reviewId, email, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Review updated successfully!");
        } catch (Exception e) {
            logger.error("EditReview failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/product/" + productId;
    }

    // ── Delete Review ── NEW ──────────────────────────────────
    @PostMapping("/review/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("DeleteReview by: {} reviewId: {}", email, reviewId);
        try {
            reviewService.deleteReviewByBuyer(reviewId, email);
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted.");
        } catch (Exception e) {
            logger.error("DeleteReview failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/product/" + productId;
    }

    // ── Notifications ─────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing notifications: {}", email);

        model.addAttribute("notifications", notificationService.getNotifications(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("currentUser", getCurrentUserDTO(email));
        return "buyer/notifications";
    }

    @PostMapping("/notifications/read/{id}")
    public String markNotificationRead(@PathVariable Long id) {
        logger.info("MarkAsRead notificationId: {}", id);
        notificationService.markAsRead(id);
        return "redirect:/buyer/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        notificationService.markAllAsRead(email);
        return "redirect:/buyer/notifications";
    }

    // ── PASTE YOUR REMAINING METHODS HERE (checkout etc.) ────

}