package com.revshop.repository;

import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.entity.Product;
import com.revshop.entity.ProductVariant;
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

    // FIX: variant-aware duplicate check.
    // The old findByCartAndProduct matched ANY item for that product regardless
    // of variant — so Size S and Size M would collide into the same row.
    // Now we match on (cart + product + variant) together:
    //   - variant=null  → no-variant product (plain stock item)
    //   - variant=<obj> → specific size/color combination
    Optional<CartItem> findByCartAndProductAndVariant(Cart cart, Product product, ProductVariant variant);

    // Keep the old method for any callers that don't use variants
    // (e.g. wishlist move-to-cart for non-variant products)
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // Always use this when rendering cart items in templates —
    // also fetches the variant so templates can display color/size labels
    @Query("""
        SELECT ci FROM CartItem ci
        JOIN FETCH ci.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.seller
        LEFT JOIN FETCH ci.variant
        WHERE ci.cart = :cart
        """)
    List<CartItem> findByCartWithProduct(@Param("cart") Cart cart);
}