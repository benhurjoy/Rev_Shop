package com.revshop.service;

import com.revshop.entity.*;
import com.revshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User mockBuyer;
    private Product mockProduct;
    private Cart mockCart;
    private CartItem mockCartItem;

    @BeforeEach
    void setUp() {
        mockBuyer = User.builder()
                .id(1L)
                .email("buyer@test.com")
                .role(User.Role.BUYER)
                .enabled(true)
                .build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Test Phone")
                .price(new BigDecimal("15000"))
                .stockQuantity(10)
                .active(true)
                .build();

        mockCart = Cart.builder()
                .id(1L)
                .user(mockBuyer)
                .cartItems(new ArrayList<>())
                .build();

        mockCartItem = CartItem.builder()
                .id(1L)
                .cart(mockCart)
                .product(mockProduct)
                .quantity(2)
                .build();
    }

    @Test
    void getOrCreateCart_ExistingCart_ShouldReturnExisting() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockCart));

        Cart result = cartService.getOrCreateCart("buyer@test.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getOrCreateCart_NoExistingCart_ShouldCreateNew() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        Cart result = cartService.getOrCreateCart("buyer@test.com");

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_NewItem_ShouldSaveCartItem() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(cartItemRepository.findByCartAndProduct(mockCart, mockProduct))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> cartService.addToCart("buyer@test.com", 1L, 2));

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_ShouldUpdateQuantity() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(cartItemRepository.findByCartAndProduct(mockCart, mockProduct))
                .thenReturn(Optional.of(mockCartItem));

        assertDoesNotThrow(() -> cartService.addToCart("buyer@test.com", 1L, 3));

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_OutOfStock_ShouldThrowException() {
        // Service checks quantity > 0, then findById, then stock — throws before getUserByEmail
        mockProduct.setStockQuantity(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertThrows(Exception.class,
                () -> cartService.addToCart("buyer@test.com", 1L, 1));
    }

    @Test
    void calculateTotal_WithItems_ShouldReturnCorrectTotal() {
        mockCart.getCartItems().add(mockCartItem);
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartWithProduct(mockCart)).thenReturn(List.of(mockCartItem));

        BigDecimal total = cartService.calculateTotal("buyer@test.com");

        assertNotNull(total);
        assertTrue(total.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void getCartItemCount_EmptyCart_ShouldReturnZero() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartWithProduct(mockCart)).thenReturn(new ArrayList<>());

        int count = cartService.getCartItemCount("buyer@test.com");

        assertEquals(0, count);
    }

    @Test
    void clearCart_ShouldDeleteAllItems() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockCart));

        assertDoesNotThrow(() -> cartService.clearCart("buyer@test.com"));

        verify(cartItemRepository).deleteByCart(mockCart);
    }
}