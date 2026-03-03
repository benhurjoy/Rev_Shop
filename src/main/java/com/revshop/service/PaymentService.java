package com.revshop.service;

import com.revshop.entity.Order;
import com.revshop.entity.Payment;
import com.revshop.repository.OrderRepository;
import com.revshop.repository.PaymentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public String createRazorpayOrder(Long orderId) {
        // TODO Day 3
        logger.info("CreateRazorpayOrder called for orderId: {}", orderId);
        return null;
    }

    public boolean verifyRazorpayPayment(String razorpayOrderId,
                                         String razorpayPaymentId,
                                         String razorpaySignature) {
        // TODO Day 3
        logger.info("VerifyRazorpayPayment called for orderId: {}", razorpayOrderId);
        return false;
    }

    public Payment processPayment(Order order, Payment.PaymentMethod method) {
        // TODO Day 3
        logger.info("ProcessPayment called for orderId: {}", order.getId());
        return null;
    }

    public Payment getPaymentByOrder(Long orderId) {
        // TODO Day 3
        logger.info("GetPaymentByOrder called for orderId: {}", orderId);
        return null;
    }
}

