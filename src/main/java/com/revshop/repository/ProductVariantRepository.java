package com.revshop.repository;

import com.revshop.entity.Product;
import com.revshop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductAndActiveTrue(Product product);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.active = true")
    List<ProductVariant> findActiveByProductId(@Param("productId") Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId " +
            "AND v.color = :color AND v.size = :size AND v.active = true")
    Optional<ProductVariant> findByProductIdAndColorAndSize(
            @Param("productId") Long productId,
            @Param("color") String color,
            @Param("size") String size);
}