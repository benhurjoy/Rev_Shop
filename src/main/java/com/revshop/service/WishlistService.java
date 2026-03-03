package com.revshop.service;

import com.revshop.entity.Product;
import com.revshop.entity.User;
import com.revshop.entity.Wishlist;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.UserRepository;
import com.revshop.repository.WishlistRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Wishlist getOrCreateWishlist(String email) {
        logger.info("GetOrCreateWishlist called for: {}", email);
        User user = getUserByEmail(email);
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist w = Wishlist.builder().user(user).build();
                    logger.info("New wishlist created for: {}", email);
                    return wishlistRepository.save(w);
                });
    }

    @Transactional
    public void addToWishlist(String email, Long productId) {
        logger.info("AddToWishlist called for email: {} productId: {}", email, productId);
        Product product = getProductById(productId);
        Wishlist wishlist = getOrCreateWishlist(email);

        if (wishlist.getProducts().contains(product)) {
            logger.warn("Product already in wishlist: {} for: {}", productId, email);
            throw new BadRequestException("Product already in wishlist");
        }

        wishlist.getProducts().add(product);
        wishlistRepository.save(wishlist);
        logger.info("Product added to wishlist: {} for: {}", productId, email);
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        logger.info("RemoveFromWishlist called for email: {} productId: {}", email, productId);
        Product product = getProductById(productId);
        Wishlist wishlist = getOrCreateWishlist(email);
        wishlist.getProducts().remove(product);
        wishlistRepository.save(wishlist);
        logger.info("Product removed from wishlist: {} for: {}", productId, email);
    }

    @Transactional
    public void moveToCart(String email, Long productId) {
        logger.info("MoveToCart called for email: {} productId: {}", email, productId);
        removeFromWishlist(email, productId);
        cartService.addToCart(email, productId, 1);
        logger.info("Product moved to cart: {} for: {}", productId, email);
    }

    public boolean isInWishlist(String email, Long productId) {
        logger.info("IsInWishlist called for email: {} productId: {}", email, productId);
        Wishlist wishlist = getOrCreateWishlist(email);
        return wishlist.getProducts().stream()
                .anyMatch(p -> p.getId().equals(productId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }
}