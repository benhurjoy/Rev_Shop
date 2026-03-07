package com.revshop.service;

import com.revshop.entity.Category;
import com.revshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory;

    @BeforeEach
    void setUp() {
        mockCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic items")
                .build();
    }

    @Test
    void addCategory_NewCategory_ShouldSave() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

        Category result = categoryService.addCategory("Electronics", "Electronic items");

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void addCategory_DuplicateName_ShouldThrowException() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThrows(Exception.class,
                () -> categoryService.addCategory("Electronics", "desc"));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));

        List<Category> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void getCategoryById_ValidId_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));

        Category result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void getCategoryById_InvalidId_ShouldThrowException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void deleteCategory_ValidId_ShouldDelete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository).deleteById(1L);
    }
}
