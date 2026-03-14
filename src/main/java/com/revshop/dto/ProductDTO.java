package com.revshop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than 0")
    private BigDecimal mrp;

    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    private Integer discountPercent;

    // Derived — sum of variant stocks
    private Integer stockQuantity;

    private String imageUrl;
    private boolean active;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;

    private Double averageRating;
    private Long totalReviews;

    // Category flags — drive UI show/hide
    private boolean categoryHasColors;
    private boolean categoryHasSizes;

    // ── Variants ───────────────────────────────────────────────
    private List<ProductVariantDTO> variants = new ArrayList<>();
    // ──────────────────────────────────────────────────────────
}