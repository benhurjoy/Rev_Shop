package com.revshop.repository;

import com.revshop.entity.Product;
import com.revshop.entity.User;
import com.revshop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    boolean existsByUser(User user);

    // ── Fetches wishlist products with category + seller in one query ──
    @Query("""
        SELECT w FROM Wishlist w
        LEFT JOIN FETCH w.products p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        WHERE w.user = :user
        """)
    Optional<Wishlist> findByUser(@Param("user") User user);

    // ── Find all wishlists containing a specific product ──
    // Used by ProductService to notify buyers when a wishlisted product goes low on stock
    @Query("""
        SELECT w FROM Wishlist w
        JOIN FETCH w.user
        WHERE :product MEMBER OF w.products
        """)
    List<Wishlist> findAllByProductsContaining(@Param("product") Product product);
}