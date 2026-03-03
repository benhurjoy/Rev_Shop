package com.revshop.service;

import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.repository.CartItemRepository;
import com.revshop.repository.CartRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private static final Logger logger = LogManager.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public Cart getOrCreateCart(String email) {
        // TODO Day 3
        logger.info("GetOrCreateCart called for: {}", email);
        return null;
    }

    public void addToCart(String email, Long productId, Integer quantity) {
        // TODO Day 3
        logger.info("AddToCart called for email: {} productId: {}", email, productId);
    }

    public void removeFromCart(String email, Long cartItemId) {
        // TODO Day 3
        logger.info("RemoveFromCart called for email: {} cartItemId: {}", email, cartItemId);
    }

    public void updateQuantity(String email, Long cartItemId, Integer quantity) {
        // TODO Day 3
        logger.info("UpdateQuantity called for cartItemId: {} quantity: {}", cartItemId, quantity);
    }

    public List<CartItem> getCartItems(String email) {
        // TODO Day 3
        logger.info("GetCartItems called for: {}", email);
        return null;
    }

    public BigDecimal calculateTotal(String email) {
        // TODO Day 3
        logger.info("CalculateTotal called for: {}", email);
        return BigDecimal.ZERO;
    }

    public void clearCart(String email) {
        // TODO Day 3
        logger.info("ClearCart called for: {}", email);
    }

    public int getCartItemCount(String email) {
        // TODO Day 3
        logger.info("GetCartItemCount called for: {}", email);
        return 0;
    }
}