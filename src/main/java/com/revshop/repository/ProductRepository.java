package com.revshop.repository;

import com.revshop.entity.Product;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStockQuantityLessThan(int threshold);
    List<Product> findByStockQuantityEquals(int quantity);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller = :seller AND p.active = true AND p.deleted = false")
    Long countActiveProductsBySeller(@Param("seller") User seller);

    // FIX: added LEFT JOIN FETCH p.variants so variants are always loaded
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    // FIX: added LEFT JOIN FETCH p.variants
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller " +
            "WHERE p.active = true AND p.deleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Product> findAllActiveWithDetails();

    // FIX: added LEFT JOIN FETCH p.variants
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller " +
            "WHERE p.seller = :seller AND p.deleted = false " +
            "ORDER BY p.createdAt DESC")
    List<Product> findBySellerNotDeletedWithDetails(@Param("seller") User seller);

    // FIX: added LEFT JOIN FETCH p.variants
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller " +
            "WHERE p.seller = :seller AND p.active = true AND p.deleted = false")
    List<Product> findBySellerActiveTrueWithDetails(@Param("seller") User seller);

    // FIX: added LEFT JOIN FETCH p.variants
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')) " +
            "AND p.active = true AND p.deleted = false")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrueWithDetails(@Param("name") String name);

    // FIX: added LEFT JOIN FETCH p.variants
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.seller " +
            "WHERE p.category.id = :categoryId " +
            "AND p.active = true AND p.deleted = false")
    List<Product> findByCategoryIdAndActiveTrueWithDetails(@Param("categoryId") Long categoryId);
}