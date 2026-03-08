package com.revshop.service;

import com.revshop.dto.CheckoutDTO;
import com.revshop.dto.OrderDTO;
import com.revshop.dto.OrderItemDTO;
import com.revshop.entity.*;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Order placeOrder(String email, CheckoutDTO dto) {
        logger.info("PlaceOrder called for: {}", email);

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        List<CartItem> cartItems = cartService.getCartItems(email);

        if (cartItems.isEmpty()) {
            logger.warn("PlaceOrder failed - empty cart for: {}", email);
            throw new BadRequestException("Cart is empty. Add products before placing order");
        }

        BigDecimal total = cartService.calculateTotal(email);

        Order order = Order.builder()
                .buyer(buyer)
                .totalAmount(total)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(dto.getShippingAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .build();

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    if (product.getStockQuantity() < cartItem.getQuantity()) {
                        throw new BadRequestException("Insufficient stock for: " + product.getName());
                    }
                    return OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(cartItem.getQuantity())
                            .price(product.getPrice())
                            .mrp(product.getMrp())
                            .discountPercent(product.getDiscountPercent())
                            .build();
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Reduce stock for each product
        cartItems.forEach(item ->
                productRepository.findById(item.getProduct().getId()).ifPresent(p -> {
                    p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
                    productRepository.save(p);
                })
        );

        // Create payment record
        Payment payment = Payment.builder()
                .order(savedOrder)
                .amount(total)
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(dto.getPaymentMethod())
                .build();
        paymentRepository.save(payment);

        cartService.clearCart(email);

        // Notify buyer
        notificationService.sendOrderNotification(email, "PLACED", savedOrder.getId());

        // Notify each seller whose product was ordered
        savedOrder.getOrderItems().forEach(item -> {
            String sellerEmail = item.getProduct().getSeller().getEmail();
            notificationService.sendNotification(
                    sellerEmail,
                    "New Order Received",
                    "You received a new order #" + savedOrder.getId() +
                            " for \"" + item.getProduct().getName() + "\" (Qty: " + item.getQuantity() + ")",
                    Notification.NotificationType.ORDER_PLACED
            );
        });

        logger.info("Order placed successfully: {} for: {}", savedOrder.getId(), email);
        return savedOrder;
    }

    public List<OrderDTO> getOrderHistory(String email) {
        logger.info("GetOrderHistory called for: {}", email);
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return orderRepository.findByBuyerWithItems(buyer)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long orderId, String email) {
        logger.info("GetOrderById called for orderId: {} by: {}", orderId, email);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new ResourceNotFoundException("Order not found: " + orderId);
                });

        return mapToDTO(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, String email) {
        logger.info("CancelOrder called for orderId: {} by: {}", orderId, email);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            logger.warn("CancelOrder failed - order not in PENDING state: {}", orderId);
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restore stock
        order.getOrderItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        // Notify buyer
        notificationService.sendOrderNotification(email, "CANCELLED", orderId);

        // Notify each seller
        order.getOrderItems().forEach(item -> {
            String sellerEmail = item.getProduct().getSeller().getEmail();
            notificationService.sendNotification(
                    sellerEmail,
                    "Order Cancelled",
                    "Order #" + orderId + " for \"" + item.getProduct().getName() + "\" was cancelled by the buyer.",
                    Notification.NotificationType.ORDER_STATUS_UPDATED
            );
        });

        logger.info("Order cancelled successfully: {}", orderId);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        logger.info("UpdateOrderStatus called for orderId: {} status: {}", orderId, status);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);

        // Notify buyer
        notificationService.sendOrderNotification(order.getBuyer().getEmail(), status.name(), orderId);
        logger.info("Order status updated to: {} for orderId: {}", status, orderId);
    }

    public List<OrderDTO> getAllOrders() {
        logger.info("GetAllOrders called");

        return orderRepository.findAllWithDetails()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();

        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyer().getId());
        dto.setBuyerName(order.getBuyer().getFirstName() + " " + order.getBuyer().getLastName());
        dto.setBuyerEmail(order.getBuyer().getEmail());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCity(order.getCity());
        dto.setState(order.getState());
        dto.setPincode(order.getPincode());
        dto.setOrderedAt(order.getOrderedAt());

        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setProductImage(item.getProduct().getImageUrl());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setMrp(item.getMrp());
                    itemDTO.setDiscountPercent(item.getDiscountPercent());
                    itemDTO.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setOrderItems(itemDTOs);

        paymentRepository.findByOrder(order).ifPresent(payment -> {
            dto.setPaymentMethod(payment.getPaymentMethod().name());
            dto.setPaymentStatus(payment.getStatus().name());
        });

        return dto;
    }
}