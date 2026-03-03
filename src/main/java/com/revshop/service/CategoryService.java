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

    @Transactional
    public Category addCategory(String name, String description) {
        logger.info("AddCategory called for: {}", name);
        if (categoryRepository.existsByName(name)) {
            logger.warn("Category already exists: {}", name);
            throw new RuntimeException("Category already exists: " + name);
        }
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();
        Category saved = categoryRepository.save(category);
        logger.info("Category added successfully: {}", saved.getId());
        return saved;
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
