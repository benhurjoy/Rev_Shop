package com.revshop.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.revshop.entity.Order;
import com.revshop.entity.Payment;
import com.revshop.exception.PaymentException;
import com.revshop.repository.OrderRepository;
import com.revshop.repository.PaymentRepository;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${razorpay.key.id:test_key}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:test_secret}")
    private String razorpayKeySecret;

    public String createRazorpayOrder(Long orderId) {
        logger.info("CreateRazorpayOrder called for orderId: {}", orderId);
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new PaymentException("Order not found: " + orderId));

            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", order.getTotalAmount()
                    .multiply(java.math.BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", "order_" + orderId);

            com.razorpay.Order razorpayOrder = client.orders.create(options);
            String razorpayOrderId = razorpayOrder.get("id");

            paymentRepository.findByOrder(order).ifPresent(payment -> {
                payment.setRazorpayOrderId(razorpayOrderId);
                paymentRepository.save(payment);
            });

            logger.info("Razorpay order created: {} for orderId: {}", razorpayOrderId, orderId);
            return razorpayOrderId;

        } catch (RazorpayException e) {
            logger.error("Razorpay order creation failed for orderId: {} - {}", orderId, e.getMessage());
            throw new PaymentException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public boolean verifyRazorpayPayment(String razorpayOrderId,
                                         String razorpayPaymentId,
                                         String razorpaySignature) {
        logger.info("VerifyRazorpayPayment called for orderId: {}", razorpayOrderId);
        try {
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256"));
            String generated = new String(Hex.encodeHex(mac.doFinal(data.getBytes())));
            boolean valid = generated.equals(razorpaySignature);
            logger.info("Razorpay signature verification: {}", valid ? "SUCCESS" : "FAILED");
            return valid;
        } catch (Exception e) {
            logger.error("Razorpay signature verification error: {}", e.getMessage());
            throw new PaymentException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public Payment processPayment(Order order, Payment.PaymentMethod method) {
        logger.info("ProcessPayment called for orderId: {} method: {}", order.getId(), method);
        try {
            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new PaymentException("Payment record not found for order: " + order.getId()));

            if (method == Payment.PaymentMethod.COD) {
                payment.setStatus(Payment.PaymentStatus.PENDING);
                payment.setPaymentMethod(Payment.PaymentMethod.COD);
            } else {
                payment.setPaymentMethod(Payment.PaymentMethod.RAZORPAY);
            }

            Payment saved = paymentRepository.save(payment);
            logger.info("Payment processed for orderId: {}", order.getId());
            return saved;
        } catch (PaymentException e) {
            logger.error("Payment processing failed for orderId: {} - {}", order.getId(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void confirmPayment(String razorpayOrderId,
                               String razorpayPaymentId,
                               String razorpaySignature) {
        logger.info("ConfirmPayment called for razorpayOrderId: {}", razorpayOrderId);

        if (!verifyRazorpayPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            logger.error("Payment verification failed for orderId: {}", razorpayOrderId);
            throw new PaymentException("Invalid payment signature");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + razorpayOrderId));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(java.time.LocalDateTime.now());
        paymentRepository.save(payment);

        payment.getOrder().setStatus(Order.OrderStatus.PROCESSING);
        orderRepository.save(payment.getOrder());

        logger.info("Payment confirmed successfully for razorpayOrderId: {}", razorpayOrderId);
    }

    public Payment getPaymentByOrder(Long orderId) {
        logger.info("GetPaymentByOrder called for orderId: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException("Order not found: " + orderId));
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));
    }
}