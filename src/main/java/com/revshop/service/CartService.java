package com.revshop.service;

import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.entity.Product;
import com.revshop.entity.ProductVariant;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.CartItemRepository;
import com.revshop.repository.CartRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.ProductVariantRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private static final Logger logger = LogManager.getLogger(CartService.class);

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Cart getOrCreateCart(String email) {
        logger.info("GetOrCreateCart called for: {}", email);
        User user = getUserByEmail(email);
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    logger.info("New cart created for: {}", email);
                    return cartRepository.save(newCart);
                });
    }

    // FIX: old signature kept for backward compatibility (wishlist move-to-cart etc.)
    @Transactional
    public void addToCart(String email, Long productId, Integer quantity) {
        addToCart(email, productId, null, quantity);
    }

    // FIX: new variant-aware method.
    // variantId = null  → no-variant product (plain stock)
    // variantId = <id>  → specific size/color — creates a SEPARATE cart row
    //                     from other variants of the same product.
    @Transactional
    public void addToCart(String email, Long productId, Long variantId, Integer quantity) {
        logger.info("AddToCart email:{} productId:{} variantId:{} qty:{}",
                email, productId, variantId, quantity);

        if (quantity <= 0) throw new BadRequestException("Quantity must be greater than 0");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // Resolve the variant (may be null for non-variant products)
        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

            // Stock check against the specific variant
            if (variant.getStockQuantity() <= 0)
                throw new BadRequestException("This variant is out of stock");
            if (quantity > variant.getStockQuantity())
                throw new BadRequestException("Only " + variant.getStockQuantity() + " items available for this variant");
        } else {
            // Stock check against the product total
            if (product.getStockQuantity() <= 0)
                throw new BadRequestException("Product is out of stock");
            if (quantity > product.getStockQuantity())
                throw new BadRequestException("Only " + product.getStockQuantity() + " items available");
        }

        Cart cart = getOrCreateCart(email);

        // FIX: look up existing item by (cart + product + variant).
        // Previously findByCartAndProduct matched ANY variant of the same product,
        // so adding Size S and Size M would just increment quantity on one row.
        // Now each (product, variant) combination gets its own distinct cart row.
        Optional<CartItem> existing =
                cartItemRepository.findByCartAndProductAndVariant(cart, product, variant);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;

            // Re-validate combined quantity against stock
            int availableStock = (variant != null)
                    ? variant.getStockQuantity()
                    : product.getStockQuantity();
            if (newQty > availableStock)
                throw new BadRequestException("Only " + availableStock + " items available");

            item.setQuantity(newQty);
            cartItemRepository.save(item);
            logger.info("Cart item quantity updated for productId:{} variantId:{}", productId, variantId);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(item);
            logger.info("New cart item added for productId:{} variantId:{}", productId, variantId);
        }
    }

    @Transactional
    public void removeFromCart(String email, Long cartItemId) {
        logger.info("RemoveFromCart email:{} cartItemId:{}", email, cartItemId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void updateQuantity(String email, Long cartItemId, Integer quantity) {
        logger.info("UpdateQuantity cartItemId:{} qty:{}", cartItemId, quantity);

        if (quantity <= 0) throw new BadRequestException("Quantity must be greater than 0");

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        // FIX: validate against variant stock when a variant is present
        int availableStock = (item.getVariant() != null)
                ? item.getVariant().getStockQuantity()
                : item.getProduct().getStockQuantity();

        if (quantity > availableStock)
            throw new BadRequestException("Only " + availableStock + " items available");

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public List<CartItem> getCartItems(String email) {
        logger.info("GetCartItems called for: {}", email);
        Cart cart = getOrCreateCart(email);
        return cartItemRepository.findByCartWithProduct(cart);
    }

    @Transactional
    public BigDecimal calculateTotal(String email) {
        logger.info("CalculateTotal called for: {}", email);
        List<CartItem> items = getCartItems(email);
        if (items.isEmpty()) return BigDecimal.ZERO;

        return items.stream()
                .map(item -> {
                    // FIX: use variant price override if present, else base product price
                    BigDecimal unitPrice = (item.getVariant() != null
                            && item.getVariant().getPriceOverride() != null)
                            ? item.getVariant().getPriceOverride()
                            : item.getProduct().getPrice();
                    return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void clearCart(String email) {
        logger.info("ClearCart called for: {}", email);
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteByCart(cart);
    }

    @Transactional
    public int getCartItemCount(String email) {
        return getCartItems(email).size();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}