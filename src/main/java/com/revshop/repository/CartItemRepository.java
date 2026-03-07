package com.revshop.repository;

import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    boolean existsByCartAndProduct(Cart cart, Product product);
    void deleteByCart(Cart cart);

    // ── Use findByCartAndProduct only inside @Transactional service methods ──
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // ── Always use this when rendering cart items in templates ──
    @Query("""
        SELECT ci FROM CartItem ci
        JOIN FETCH ci.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        WHERE ci.cart = :cart
        """)
    List<CartItem> findByCartWithProduct(@Param("cart") Cart cart);
}