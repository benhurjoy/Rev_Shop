package com.revshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer discountPercent;
    private BigDecimal subtotal;
}
