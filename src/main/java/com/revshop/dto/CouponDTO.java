package com.revshop.dto;

import com.revshop.entity.Coupon;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {

    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Code must be 3–20 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Code must be alphanumeric only")
    private String code;

    @NotNull(message = "Discount type is required")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount must be greater than 0")
    private BigDecimal discountValue;

    @NotNull(message = "Minimum order amount is required")
    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount;

    @Min(value = 0, message = "Usage limit cannot be negative")
    private int usageLimit;

    private int usedCount;

    @NotNull(message = "Expiry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expiryDate;

    private boolean active;

    // Read-only display fields
    private String status;
}