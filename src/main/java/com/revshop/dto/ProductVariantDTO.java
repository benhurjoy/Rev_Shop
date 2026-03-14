package com.revshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVariantDTO {

    private Long id;

    private String color;   // nullable
    private String size;    // nullable

    private Integer stockQuantity;

    // null → use product base price
    private BigDecimal priceOverride;

    // null → use product main image
    private String imageUrl;

    private String skuCode;

    private boolean active;

    // Convenience: resolved price (priceOverride if set, else product.price)
    private BigDecimal resolvedPrice;
}