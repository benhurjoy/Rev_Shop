package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    @SequenceGenerator(name = "products_seq", sequenceName = "products_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Oracle VARCHAR2 max is 4000 chars — length=2000 is safe
    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal mrp;

    @Column(nullable = false)
    @Builder.Default
    private Integer discountPercent = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    private String imageUrl;

    // Oracle: boolean -> NUMBER(1,0). Removed MySQL-specific columnDefinition.
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // !! CHANGED: Removed columnDefinition = "boolean default false"
    // Oracle does not support "boolean" as a column type.
    // Hibernate will map this to NUMBER(1,0) DEFAULT 0 via OracleDialect.
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "";
    }
}