package com.revshop.service;


import com.revshop.entity.Category;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.CategoryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LogManager.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    // ── Original 2-arg method — kept so nothing else breaks ───
    @Transactional
    public Category addCategory(String name, String description) {
        return addCategory(name, description, false, false);
    }

    // ── NEW 4-arg method used by AdminController ───────────────
    @Transactional
    public Category addCategory(String name, String description,
                                boolean hasColors, boolean hasSizes) {
        logger.info("AddCategory called for: {}", name);
        if (categoryRepository.existsByName(name)) {
            logger.warn("Category already exists: {}", name);
            throw new RuntimeException("Category already exists: " + name);
        }
        Category category = Category.builder()
                .name(name)
                .description(description)
                .hasColors(hasColors)
                .hasSizes(hasSizes)
                .build();
        Category saved = categoryRepository.save(category);
        logger.info("Category added successfully: {}", saved.getId());
        return saved;
    }

    // ── NEW: update variant flags on an existing category ──────
    @Transactional
    public void updateCategoryVariants(Long id, boolean hasColors, boolean hasSizes) {
        logger.info("UpdateCategoryVariants called for id: {} hasColors={} hasSizes={}",
                id, hasColors, hasSizes);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        category.setHasColors(hasColors);
        category.setHasSizes(hasSizes);
        categoryRepository.save(category);
        logger.info("Category variants updated for id: {}", id);
    }

    public List<Category> getAllCategories() {
        logger.info("GetAllCategories called");
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        logger.info("GetCategoryById called for id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Category not found: {}", id);
                    return new ResourceNotFoundException("Category not found: " + id);
                });
    }

    @Transactional
    public void deleteCategory(Long id) {
        logger.info("DeleteCategory called for id: {}", id);
        if (!categoryRepository.existsById(id)) {
            logger.warn("Category not found for delete: {}", id);
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
        logger.info("Category deleted: {}", id);
    }
}