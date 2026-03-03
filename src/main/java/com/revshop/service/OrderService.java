package com.revshop.service;

import com.revshop.dto.CheckoutDTO;
import com.revshop.dto.OrderDTO;
import com.revshop.entity.Order;
import com.revshop.repository.OrderRepository;
import com.revshop.repository.PaymentRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LogManager.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public Order placeOrder(String email, CheckoutDTO checkoutDTO) {
        // TODO Day 3
        logger.info("PlaceOrder called for: {}", email);
        return null;
    }

    public List<OrderDTO> getOrderHistory(String email) {
        // TODO Day 3
        logger.info("GetOrderHistory called for: {}", email);
        return null;
    }

    public OrderDTO getOrderById(Long orderId, String email) {
        // TODO Day 3
        logger.info("GetOrderById called for orderId: {}", orderId);
        return null;
    }

    public void cancelOrder(Long orderId, String email) {
        // TODO Day 3
        logger.info("CancelOrder called for orderId: {}", orderId);
    }

    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        // TODO Day 3
        logger.info("UpdateOrderStatus called for orderId: {} status: {}", orderId, status);
    }

    public List<OrderDTO> getAllOrders() {
        // TODO Day 3
        logger.info("GetAllOrders called");
        return null;
    }
}

