package com.revshop.service;

import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.entity.Product;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.CartItemRepository;
import com.revshop.repository.CartRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // FIX: keeps session open for all reads
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

    @Transactional
    public Cart getOrCreateCart(String email) {
        logger.info("GetOrCreateCart called for: {}", email);
        User user = getUserByEmail(email);
        // FIX: use plain findByUser here — we only need the Cart shell, not items
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    logger.info("New cart created for: {}", email);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void addToCart(String email, Long productId, Integer quantity) {
        logger.info("AddToCart called for email: {} productId: {} qty: {}", email, productId, quantity);

        if (quantity <= 0) {
            logger.warn("Invalid quantity: {} for email: {}", quantity, email);
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // FIX: plain findById is fine here — we only read scalar fields (stockQuantity, price)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Product not found: {}", productId);
                    return new ResourceNotFoundException("Product not found: " + productId);
                });

        if (product.getStockQuantity() <= 0) {
            logger.warn("Product out of stock: {}", productId);
            throw new BadRequestException("Product is out of stock");
        }

        if (quantity > product.getStockQuantity()) {
            logger.warn("Requested qty {} exceeds stock {} for product: {}", quantity, product.getStockQuantity(), productId);
            throw new BadRequestException("Only " + product.getStockQuantity() + " items available");
        }

        Cart cart = getOrCreateCart(email);
        Optional<CartItem> existing = cartItemRepository.findByCartAndProduct(cart, product);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
            logger.info("Cart item quantity updated for productId: {}", productId);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(item);
            logger.info("New cart item added for productId: {}", productId);
        }
    }

    @Transactional
    public void removeFromCart(String email, Long cartItemId) {
        logger.info("RemoveFromCart called for email: {} cartItemId: {}", email, cartItemId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> {
                    logger.warn("CartItem not found: {}", cartItemId);
                    return new ResourceNotFoundException("Cart item not found: " + cartItemId);
                });
        cartItemRepository.delete(item);
        logger.info("Cart item removed: {}", cartItemId);
    }

    @Transactional
    public void updateQuantity(String email, Long cartItemId, Integer quantity) {
        logger.info("UpdateQuantity called for cartItemId: {} qty: {}", cartItemId, quantity);

        if (quantity <= 0) {
            logger.warn("Invalid quantity: {}", quantity);
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // FIX: use findByCartWithProduct so item.getProduct().getStockQuantity() doesn't crash
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        // item.getProduct() is still in session because of class-level @Transactional(readOnly=true)
        if (quantity > item.getProduct().getStockQuantity()) {
            throw new BadRequestException("Only " + item.getProduct().getStockQuantity() + " items available");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        logger.info("Cart item quantity updated to: {} for cartItemId: {}", quantity, cartItemId);
    }

    @Transactional
    public List<CartItem> getCartItems(String email) {
        logger.info("GetCartItems called for: {}", email);
        Cart cart = getOrCreateCart(email);

        // FIX: was findByCartWithProduct — already correct, kept as-is
        return cartItemRepository.findByCartWithProduct(cart);
    }

    public BigDecimal calculateTotal(String email) {
        logger.info("CalculateTotal called for: {}", email);
        List<CartItem> items = getCartItems(email);

        if (items.isEmpty()) {
            logger.warn("Cart is empty for: {}", email);
            return BigDecimal.ZERO;
        }

        // SAFE: product is JOIN FETCHed by findByCartWithProduct
        BigDecimal total = items.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("Cart total calculated: {} for: {}", total, email);
        return total;
    }

    @Transactional
    public void clearCart(String email) {
        logger.info("ClearCart called for: {}", email);
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteByCart(cart);
        logger.info("Cart cleared for: {}", email);
    }

    public int getCartItemCount(String email) {
        logger.info("GetCartItemCount called for: {}", email);
        return getCartItems(email).size();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}