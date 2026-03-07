package com.revshop.service;

import com.revshop.dto.ProductDTO;
import com.revshop.entity.Category;
import com.revshop.entity.Product;
import com.revshop.entity.User;
import com.revshop.repository.CategoryRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.ReviewRepository;
import com.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;  // needed by mapToDTO

    @InjectMocks
    private ProductService productService;

    private User mockSeller;
    private Category mockCategory;
    private Product mockProduct;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        mockSeller = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("seller@test.com")
                .role(User.Role.SELLER)
                .enabled(true)
                .build();

        mockCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Test Phone")
                .price(new BigDecimal("15000"))
                .mrp(new BigDecimal("18000"))
                .discountPercent(17)
                .stockQuantity(50)
                .active(true)
                .category(mockCategory)
                .seller(mockSeller)
                .build();

        productDTO = new ProductDTO();
        productDTO.setName("Test Phone");
        productDTO.setPrice(new BigDecimal("15000"));
        productDTO.setMrp(new BigDecimal("18000"));
        productDTO.setDiscountPercent(17);
        productDTO.setStockQuantity(50);
        productDTO.setCategoryId(1L);
    }

    @Test
    void addProduct_ValidData_ShouldSaveProduct() {
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(mockSeller));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Product saved = productService.addProduct(productDTO, "seller@test.com");

        assertNotNull(saved);
        assertEquals("Test Phone", saved.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProduct_SellerNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> productService.addProduct(productDTO, "unknown@test.com"));

        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_CategoryNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(mockSeller));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> productService.addProduct(productDTO, "seller@test.com"));
    }

    @Test
    void getAllActiveProducts_ShouldReturnList() {
        when(productRepository.findAllActiveWithDetails()).thenReturn(List.of(mockProduct));
        when(reviewRepository.findAverageRatingByProduct(mockProduct)).thenReturn(4.0);
        when(reviewRepository.countByProduct(mockProduct)).thenReturn(5L);

        List<ProductDTO> result = productService.getAllActiveProducts();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getProductById_ValidId_ShouldReturnProduct() {
        when(productRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.findAverageRatingByProduct(mockProduct)).thenReturn(4.0);
        when(reviewRepository.countByProduct(mockProduct)).thenReturn(5L);

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Phone", result.getName());
    }

    @Test
    void getProductById_InvalidId_ShouldThrowException() {
        when(productRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> productService.getProductById(99L));
    }

    @Test
    void deleteProduct_OwnProduct_ShouldDeactivate() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenReturn(mockProduct);

        assertDoesNotThrow(() -> productService.deleteProduct(1L, "seller@test.com"));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void searchProducts_MatchingKeyword_ShouldReturnResults() {
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrueWithDetails("Phone"))
                .thenReturn(List.of(mockProduct));
        when(reviewRepository.findAverageRatingByProduct(mockProduct)).thenReturn(4.0);
        when(reviewRepository.countByProduct(mockProduct)).thenReturn(5L);

        List<ProductDTO> results = productService.searchProducts("Phone");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }
}