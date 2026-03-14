package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "variant_seq")
    @SequenceGenerator(name = "variant_seq", sequenceName = "variant_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // nullable — some products only have colors, some only sizes, some both
    @Column(name = "color", length = 100)
    private String color;

    @Column(name = "size_value", length = 100)
    private String size;

    // Each variant has its own stock
    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    // Optional price override — null means use product base price
    @Column(name = "price_override")
    private BigDecimal priceOverride;

    // Optional variant-specific image — null means use product main image
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Optional SKU code for inventory tracking
    @Column(name = "sku_code", length = 100)
    private String skuCode;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}