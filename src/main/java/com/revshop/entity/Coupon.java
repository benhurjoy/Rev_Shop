package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coupons_seq")
    @SequenceGenerator(name = "coupons_seq", sequenceName = "coupons_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue; // % or flat ₹ amount

    @Column(nullable = false)
    private BigDecimal minimumOrderAmount; // 0 means no minimum

    @Column(nullable = false)
    private int usageLimit; // 0 means unlimited

    @Column(nullable = false)
    @Builder.Default
    private int usedCount = 0;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (code != null) code = code.toUpperCase().trim();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isUsageLimitReached() {
        return usageLimit > 0 && usedCount >= usageLimit;
    }

    public boolean isValid() {
        return active && !isExpired() && !isUsageLimitReached();
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED
    }
}