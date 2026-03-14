package com.revshop.repository;

import com.revshop.entity.Product;
import com.revshop.entity.Review;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByProductAndBuyer(Product product, User buyer);
    boolean existsByProductAndBuyer(Product product, User buyer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(@Param("product") Product product);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    Long countByProduct(@Param("product") Product product);

    // ── Used by product detail page (buyer name, product name accessed in views) ──

    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.buyer
        WHERE r.product = :product
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByProductWithBuyer(@Param("product") Product product);

    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.product p
        LEFT JOIN FETCH p.category
        WHERE r.buyer = :buyer
        """)
    List<Review> findByBuyerWithProduct(@Param("buyer") User buyer);

    // ── NEW: all reviews across a seller's products (for seller reviews page) ──
    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.product p
        JOIN FETCH r.buyer b
        WHERE p.seller = :seller
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByProduct_Seller(@Param("seller") User seller);
}