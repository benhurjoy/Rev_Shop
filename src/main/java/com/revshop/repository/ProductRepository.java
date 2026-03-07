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

    // ── raw finders kept for internal use (no lazy risk if used inside @Transactional) ──

    List<Product> findByStockQuantityLessThan(int threshold);
    List<Product> findByStockQuantityEquals(int quantity);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller = :seller AND p.active = true")
    Long countActiveProductsBySeller(@Param("seller") User seller);

    // ── JOIN FETCH variants — use these everywhere entities are passed to templates ──

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Product> findAllActiveWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.seller = :seller")
    List<Product> findBySellerWithDetails(@Param("seller") User seller);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.seller = :seller AND p.active = true")
    List<Product> findBySellerActiveTrueWithDetails(@Param("seller") User seller);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')) AND p.active = true")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrueWithDetails(@Param("name") String name);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller WHERE p.category.id = :categoryId AND p.active = true")
    List<Product> findByCategoryIdAndActiveTrueWithDetails(@Param("categoryId") Long categoryId);
}
