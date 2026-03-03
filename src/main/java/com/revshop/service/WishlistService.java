package com.revshop.service;

import com.revshop.entity.Wishlist;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.UserRepository;
import com.revshop.repository.WishlistRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WishlistService {

    private static final Logger logger = LogManager.getLogger(WishlistService.class);

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public Wishlist getOrCreateWishlist(String email) {
        // TODO Day 3
        logger.info("GetOrCreateWishlist called for: {}", email);
        return null;
    }

    public void addToWishlist(String email, Long productId) {
        // TODO Day 3
        logger.info("AddToWishlist called for email: {} productId: {}", email, productId);
    }

    public void removeFromWishlist(String email, Long productId) {
        // TODO Day 3
        logger.info("RemoveFromWishlist called for email: {} productId: {}", email, productId);
    }

    public void moveToCart(String email, Long productId) {
        // TODO Day 3
        logger.info("MoveToCart called for email: {} productId: {}", email, productId);
    }

    public boolean isInWishlist(String email, Long productId) {
        // TODO Day 3
        logger.info("IsInWishlist called for email: {} productId: {}", email, productId);
        return false;
    }
}