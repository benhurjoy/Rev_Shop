package com.revshop.repository;

import com.revshop.entity.User;
import com.revshop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}