package com.revshop.controller;

import com.revshop.dto.CheckoutDTO;
import com.revshop.entity.Order;
import com.revshop.exception.PaymentException;
import com.revshop.service.CartService;
import com.revshop.service.CouponService;
import com.revshop.service.NotificationService;
import com.revshop.service.OrderService;
import com.revshop.service.PaymentService;
import jakarta.servlet.http.HttpSession;
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

    @Autowired private OrderService orderService;
    @Autowired private PaymentService paymentService;
    @Autowired private CartService cartService;
    @Autowired private NotificationService notificationService;
    @Autowired private CouponService couponService;

    @Value("${razorpay.key.id:test_key}")
    private String razorpayKeyId;

    // ── Helper: build checkout model ──────────────────────────
    private void populateCheckoutModel(Model model, String email,
                                       BigDecimal discount, String appliedCoupon) {
        BigDecimal total          = cartService.calculateTotal(email);
        BigDecimal safeDiscount   = (discount != null ? discount : BigDecimal.ZERO);
        BigDecimal discountedTotal = total.subtract(safeDiscount).max(BigDecimal.ZERO);
        BigDecimal gst            = discountedTotal.multiply(new BigDecimal("0.18"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal     = discountedTotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        model.addAttribute("cartItems",        cartService.getCartItems(email));
        model.addAttribute("total",            total);
        model.addAttribute("discount",         safeDiscount);
        model.addAttribute("gst",              gst);
        model.addAttribute("grandTotal",       grandTotal);
        model.addAttribute("appliedCoupon",    appliedCoupon != null ? appliedCoupon : "");
        model.addAttribute("cartCount",        cartService.getCartItemCount(email));
        model.addAttribute("unreadCount",      notificationService.getUnreadCount(email));
        // Show only active, non-expired, within-usage-limit coupons to the buyer
        model.addAttribute("availableCoupons", couponService.getAvailableCoupons(total));
    }

    // ── Checkout GET ──────────────────────────────────────────
    @GetMapping("/checkout")
    public String showCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {

        String email = userDetails.getUsername();
        if (cartService.getCartItems(email).isEmpty()) {
            return "redirect:/buyer/cart";
        }

        String appliedCoupon = (String)     session.getAttribute("appliedCoupon");
        BigDecimal discount  = (BigDecimal) session.getAttribute("couponDiscount");

        model.addAttribute("checkoutDTO", new CheckoutDTO());
        populateCheckoutModel(model, email, discount, appliedCoupon);
        return "buyer/checkout";
    }

    // ── Apply Coupon ──────────────────────────────────────────
    @PostMapping("/checkout/apply-coupon")
    public String applyCoupon(
            @RequestParam String couponCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        try {
            BigDecimal total    = cartService.calculateTotal(email);
            BigDecimal discount = couponService.applyAndCalculateDiscount(couponCode, total);
            session.setAttribute("appliedCoupon",  couponCode.toUpperCase().trim());
            session.setAttribute("couponDiscount", discount);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Coupon applied! You save ₹" + discount);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/checkout";
    }

    // ── Remove Coupon ─────────────────────────────────────────
    @PostMapping("/checkout/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("appliedCoupon");
        session.removeAttribute("couponDiscount");
        redirectAttributes.addFlashAttribute("successMessage", "Coupon removed.");
        return "redirect:/buyer/checkout";
    }

    // ── Place Order ───────────────────────────────────────────
    // FIX (back-button bug): For RAZORPAY, do NOT save the order here.
    // Save a "pending checkout" in session and only create the order
    // after Razorpay confirms payment in the callback.
    @PostMapping("/checkout")
    public String placeOrder(
            @Valid @ModelAttribute CheckoutDTO checkoutDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String email = userDetails.getUsername();

        // Always read discount from session (tamper-proof)
        String appliedCoupon = (String)     session.getAttribute("appliedCoupon");
        BigDecimal discount  = (BigDecimal) session.getAttribute("couponDiscount");
        if (discount == null) discount = BigDecimal.ZERO;

        if (result.hasErrors()) {
            populateCheckoutModel(model, email, discount, appliedCoupon);
            return "buyer/checkout";
        }

        checkoutDTO.setAppliedCoupon(appliedCoupon);

        try {
            if (checkoutDTO.getPaymentMethod() ==
                    com.revshop.entity.Payment.PaymentMethod.RAZORPAY) {

                // ── RAZORPAY: store intent in session, don't create order yet ──
                // Calculate the grand total to pass to Razorpay
                BigDecimal total           = cartService.calculateTotal(email);
                BigDecimal discountedTotal = total.subtract(discount).max(BigDecimal.ZERO);
                BigDecimal gst             = discountedTotal.multiply(new BigDecimal("0.18"))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal grandTotal      = discountedTotal.add(gst)
                        .setScale(2, RoundingMode.HALF_UP);

                // Save the full checkout intent in session
                session.setAttribute("pendingCheckoutDTO", checkoutDTO);
                session.setAttribute("pendingDiscount",    discount);
                session.setAttribute("pendingGrandTotal",  grandTotal);

                // Create a Razorpay order (just a payment order, no DB Order yet)
                String razorpayOrderId = paymentService.createRazorpayOrderForAmount(grandTotal);

                model.addAttribute("razorpayOrderId", razorpayOrderId);
                model.addAttribute("razorpayKeyId",   razorpayKeyId);
                model.addAttribute("grandTotal",      grandTotal);
                model.addAttribute("email",           email);
                logger.info("Razorpay payment initiated for: {}, amount: {}", email, grandTotal);
                return "buyer/razorpay-payment";
            }

            // ── COD: place order immediately ──────────────────
            Order order = orderService.placeOrder(email, checkoutDTO, discount);
            logger.info("COD order placed: {} for: {}", order.getId(), email);

            if (appliedCoupon != null && !appliedCoupon.isBlank()) {
                couponService.incrementUsage(appliedCoupon);
            }

            // Clear coupon session
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("couponDiscount");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order ID: #" + order.getId());
            return "redirect:/buyer/orders";

        } catch (Exception e) {
            logger.error("Order placement failed for: {} - {}", email, e.getMessage());
            populateCheckoutModel(model, email, discount, appliedCoupon);
            model.addAttribute("errorMessage", e.getMessage());
            return "buyer/checkout";
        }
    }

    // ── Razorpay Callback ─────────────────────────────────────
    // FIX: Order is created HERE, only after Razorpay confirms payment.
    // If the user presses back and never pays, this callback never fires
    // and no order is ever saved to the database.
    @PostMapping("/payment/callback")
    public String razorpayCallback(
            @RequestParam String razorpay_order_id,
            @RequestParam String razorpay_payment_id,
            @RequestParam String razorpay_signature,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("Razorpay callback for: {}, paymentId: {}", email, razorpay_payment_id);

        try {
            // 1. Verify the signature with Razorpay
            paymentService.verifyRazorpaySignature(
                    razorpay_order_id, razorpay_payment_id, razorpay_signature);

            // 2. Retrieve the checkout intent saved before the payment
            CheckoutDTO checkoutDTO = (CheckoutDTO) session.getAttribute("pendingCheckoutDTO");
            BigDecimal discount     = (BigDecimal) session.getAttribute("pendingDiscount");
            if (checkoutDTO == null) {
                throw new PaymentException("Session expired. Please try checkout again.");
            }
            if (discount == null) discount = BigDecimal.ZERO;

            // 3. Now create the order in the database
            Order order = orderService.placeOrder(email, checkoutDTO, discount);
            logger.info("Razorpay order confirmed and saved: {} for: {}", order.getId(), email);

            // 4. Mark the payment as PAID and link Razorpay IDs
            paymentService.confirmPayment(razorpay_order_id, razorpay_payment_id, order.getId());

            // 5. Increment coupon usage
            String appliedCoupon = checkoutDTO.getAppliedCoupon();
            if (appliedCoupon != null && !appliedCoupon.isBlank()) {
                couponService.incrementUsage(appliedCoupon);
            }

            // 6. Clean up session
            session.removeAttribute("pendingCheckoutDTO");
            session.removeAttribute("pendingDiscount");
            session.removeAttribute("pendingGrandTotal");
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("couponDiscount");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment successful! Order #" + order.getId() + " confirmed.");
            return "redirect:/buyer/orders";

        } catch (Exception e) {
            logger.error("Razorpay callback failed for: {} - {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Payment verification failed. Please contact support. Ref: " + razorpay_payment_id);
            return "redirect:/buyer/orders";
        }
    }

    // ── View All Orders ───────────────────────────────────────
    @GetMapping("/orders")
    public String viewOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();
        model.addAttribute("orders",      orderService.getOrderHistory(email));
        model.addAttribute("cartCount",   cartService.getCartItemCount(email));
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
        model.addAttribute("order",       orderService.getOrderById(orderId, email));
        model.addAttribute("cartCount",   cartService.getCartItemCount(email));
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
        try {
            orderService.cancelOrder(orderId, email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + orderId + " cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders";
    }
}