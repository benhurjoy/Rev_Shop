package com.revshop.controller;

import com.revshop.dto.CheckoutDTO;
import com.revshop.entity.Order;
import com.revshop.exception.PaymentException;
import com.revshop.service.CartService;
import com.revshop.service.NotificationService;
import com.revshop.service.OrderService;
import com.revshop.service.PaymentService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequestMapping("/buyer")
@PreAuthorize("hasRole('BUYER')")
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CartService cartService;

    @Autowired
    private NotificationService notificationService;

    @Value("${razorpay.key.id:test_key}")
    private String razorpayKeyId;

    // ── Checkout Page ─────────────────────────────────────────
    @GetMapping("/checkout")
    public String showCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Checkout page accessed by: {}", email);

        if (cartService.getCartItems(email).isEmpty()) {
            return "redirect:/buyer/cart";
        }

        BigDecimal total = cartService.calculateTotal(email);

        // FIX: pre-compute gst and grandTotal here so the template can use
        // ${gst} and ${grandTotal} instead of blocked new java.math.BigDecimal() expressions
        BigDecimal gst = total.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = total.add(gst).setScale(2, RoundingMode.HALF_UP);

        model.addAttribute("checkoutDTO", new CheckoutDTO());
        model.addAttribute("cartItems", cartService.getCartItems(email));
        model.addAttribute("total", total);
        model.addAttribute("gst", gst);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        return "buyer/checkout";
    }

    // ── Place Order ───────────────────────────────────────────
    @PostMapping("/checkout")
    public String placeOrder(
            @Valid @ModelAttribute CheckoutDTO checkoutDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        String email = userDetails.getUsername();

        if (result.hasErrors()) {
            // FIX: also add gst/grandTotal when re-rendering on validation error
            BigDecimal total = cartService.calculateTotal(email);
            BigDecimal gst = total.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grandTotal = total.add(gst).setScale(2, RoundingMode.HALF_UP);
            model.addAttribute("cartItems", cartService.getCartItems(email));
            model.addAttribute("total", total);
            model.addAttribute("gst", gst);
            model.addAttribute("grandTotal", grandTotal);
            return "buyer/checkout";
        }

        try {
            Order order = orderService.placeOrder(email, checkoutDTO);
            logger.info("Order placed: {} for: {}", order.getId(), email);

            // Razorpay flow
            if (checkoutDTO.getPaymentMethod() == com.revshop.entity.Payment.PaymentMethod.RAZORPAY) {
                String razorpayOrderId = paymentService.createRazorpayOrder(order.getId());
                model.addAttribute("razorpayOrderId", razorpayOrderId);
                model.addAttribute("razorpayKeyId", razorpayKeyId);
                model.addAttribute("order", order);
                model.addAttribute("total", order.getTotalAmount());
                model.addAttribute("email", email);
                logger.info("Razorpay payment initiated for orderId: {}", order.getId());
                return "buyer/razorpay-payment";
            }

            // COD flow
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order ID: #" + order.getId());
            logger.info("COD order confirmed: {} for: {}", order.getId(), email);
            return "redirect:/buyer/orders";

        } catch (Exception e) {
            logger.error("Order placement failed for: {} - {}", email, e.getMessage());

            // FIX: also add gst/grandTotal when re-rendering on order failure
            BigDecimal total = cartService.calculateTotal(email);
            BigDecimal gst = total.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grandTotal = total.add(gst).setScale(2, RoundingMode.HALF_UP);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cartItems", cartService.getCartItems(email));
            model.addAttribute("total", total);
            model.addAttribute("gst", gst);
            model.addAttribute("grandTotal", grandTotal);
            return "buyer/checkout";
        }
    }

    // ── Razorpay Callback ─────────────────────────────────────
    @PostMapping("/payment/callback")
    public String razorpayCallback(
            @RequestParam String razorpay_order_id,
            @RequestParam String razorpay_payment_id,
            @RequestParam String razorpay_signature,
            RedirectAttributes redirectAttributes) {

        logger.info("Razorpay callback received for orderId: {}", razorpay_order_id);
        try {
            paymentService.confirmPayment(
                    razorpay_order_id,
                    razorpay_payment_id,
                    razorpay_signature);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment successful! Your order is confirmed.");
            logger.info("Payment confirmed for orderId: {}", razorpay_order_id);
        } catch (PaymentException e) {
            logger.error("Payment failed for orderId: {} - {}",
                    razorpay_order_id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Payment verification failed. Please contact support.");
        }
        return "redirect:/buyer/orders";
    }

    // ── View All Orders ───────────────────────────────────────
    @GetMapping("/orders")
    public String viewOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing orders: {}", email);

        model.addAttribute("orders", orderService.getOrderHistory(email));
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        return "buyer/orders";
    }

    // ── View Order Detail ─────────────────────────────────────
    @GetMapping("/orders/{orderId}")
    public String viewOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        logger.info("Buyer viewing order detail - orderId: {} by: {}", orderId, email);

        model.addAttribute("order", orderService.getOrderById(orderId, email));
        model.addAttribute("cartCount", cartService.getCartItemCount(email));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        return "buyer/order-detail";
    }

    // ── Cancel Order ──────────────────────────────────────────
    @PostMapping("/orders/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("Buyer cancelling orderId: {} by: {}", orderId, email);
        try {
            orderService.cancelOrder(orderId, email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + orderId + " cancelled successfully.");
        } catch (Exception e) {
            logger.error("Cancel order failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders";
    }
}
