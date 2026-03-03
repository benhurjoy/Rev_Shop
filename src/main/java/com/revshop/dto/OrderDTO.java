package com.revshop.dto;

import com.revshop.entity.Order;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {

    private Long id;
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;
    private List<OrderItemDTO> orderItems;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private String shippingAddress;
    private String city;
    private String state;
    private String pincode;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime orderedAt;
}
