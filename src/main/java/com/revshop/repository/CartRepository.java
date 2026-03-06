package com.revshop.repository;

import com.revshop.entity.Cart;
import com.revshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    boolean existsByUser(User user);

    // ── Use this when you only need the Cart shell (e.g. to check existence) ──
    Optional<Cart> findByUser(User user);

    // ── Use this whenever cart items + products are rendered in a template ──
    @Query("""
        SELECT c FROM Cart c
        LEFT JOIN FETCH c.cartItems ci
        LEFT JOIN FETCH ci.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        WHERE c.user = :user
        """)
    Optional<Cart> findByUserWithItems(@Param("user") User user);
}