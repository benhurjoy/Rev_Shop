package com.revshop.service;

import java.util.Optional;

import com.revshop.dto.ReviewDTO;
import com.revshop.entity.*;
import com.revshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User mockBuyer;
    private Product mockProduct;
    private Review mockReview;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        mockBuyer = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("buyer@test.com")
                .role(User.Role.BUYER)
                .build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Test Phone")
                .price(new BigDecimal("15000"))
                .active(true)
                .build();

        mockReview = Review.builder()
                .id(1L)
                .buyer(mockBuyer)
                .product(mockProduct)
                .rating(4)
                .comment("Great product!")
                .build();

        reviewDTO = new ReviewDTO();
        reviewDTO.setProductId(1L);
        reviewDTO.setRating(4);
        reviewDTO.setComment("Great product!");
    }

    @Test
    void addReview_FirstTimeReview_ShouldSave() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.existsByProductAndBuyer(mockProduct, mockBuyer)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);

        ReviewDTO result = reviewService.addReview("buyer@test.com", reviewDTO);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReview_AlreadyReviewed_ShouldThrowException() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.existsByProductAndBuyer(mockProduct, mockBuyer)).thenReturn(true);

        assertThrows(Exception.class,
                () -> reviewService.addReview("buyer@test.com", reviewDTO));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_InvalidRating_ShouldThrowException() {
        reviewDTO.setRating(6); // out of range
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.existsByProductAndBuyer(mockProduct, mockBuyer)).thenReturn(false);

        assertThrows(Exception.class,
                () -> reviewService.addReview("buyer@test.com", reviewDTO));
    }

    @Test
    void getReviewsByProduct_ValidProduct_ShouldReturnList() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.findByProductOrderByCreatedAtDesc(mockProduct))
                .thenReturn(List.of(mockReview));

        List<ReviewDTO> result = reviewService.getReviewsByProduct(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getRating());
    }

    @Test
    void getAverageRating_WithReviews_ShouldReturnAverage() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.findAverageRatingByProduct(mockProduct)).thenReturn(4.0);

        Double avg = reviewService.getAverageRating(1L);

        assertEquals(4.0, avg);
    }

    @Test
    void getAverageRating_NoReviews_ShouldReturnZero() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.findAverageRatingByProduct(mockProduct)).thenReturn(null);

        Double avg = reviewService.getAverageRating(1L);

        assertEquals(0.0, avg);
    }

    @Test
    void hasAlreadyReviewed_Reviewed_ShouldReturnTrue() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.existsByProductAndBuyer(mockProduct, mockBuyer)).thenReturn(true);

        assertTrue(reviewService.hasAlreadyReviewed("buyer@test.com", 1L));
    }

    @Test
    void deleteReview_ValidId_ShouldDelete() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> reviewService.deleteReview(1L));

        verify(reviewRepository).deleteById(1L);
    }
}

