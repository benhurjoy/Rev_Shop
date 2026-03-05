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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private WishlistService wishlistService;

    private User mockBuyer;
    private Product mockProduct;
    private Wishlist mockWishlist;

    @BeforeEach
    void setUp() {
        mockBuyer = User.builder()
                .id(1L)
                .email("buyer@test.com")
                .role(User.Role.BUYER)
                .build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Test Phone")
                .price(new BigDecimal("15000"))
                .active(true)
                .stockQuantity(5)
                .build();

        mockWishlist = Wishlist.builder()
                .id(1L)
                .user(mockBuyer)
                .products(new ArrayList<>())
                .build();
    }

    @Test
    void getOrCreateWishlist_Existing_ShouldReturn() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(wishlistRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockWishlist));

        Wishlist result = wishlistService.getOrCreateWishlist("buyer@test.com");

        assertNotNull(result);
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void addToWishlist_ProductNotInList_ShouldAdd() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(wishlistRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockWishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertDoesNotThrow(() -> wishlistService.addToWishlist("buyer@test.com", 1L));

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void removeFromWishlist_ProductInList_ShouldRemove() {
        mockWishlist.getProducts().add(mockProduct);
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(wishlistRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockWishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertDoesNotThrow(() -> wishlistService.removeFromWishlist("buyer@test.com", 1L));

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void isInWishlist_ProductPresent_ShouldReturnTrue() {
        mockWishlist.getProducts().add(mockProduct);
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(wishlistRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockWishlist));

        boolean result = wishlistService.isInWishlist("buyer@test.com", 1L);

        assertTrue(result);
    }

    @Test
    void isInWishlist_ProductAbsent_ShouldReturnFalse() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(wishlistRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockWishlist));

        boolean result = wishlistService.isInWishlist("buyer@test.com", 1L);

        assertFalse(result);
    }

    @Test
    void moveToCart_ShouldRemoveFromWishlistAndAddToCart() {
        mockWishlist.getProducts().add(mockProduct);
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(wishlistRepository.findByUser(mockBuyer)).thenReturn(Optional.of(mockWishlist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        assertDoesNotThrow(() -> wishlistService.moveToCart("buyer@test.com", 1L));

        verify(cartService).addToCart("buyer@test.com", 1L, 1);
        verify(wishlistRepository).save(any(Wishlist.class));
    }
}