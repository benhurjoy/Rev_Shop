package com.revshop.service;

import com.revshop.entity.Category;
import com.revshop.repository.CategoryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LogManager.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public Category addCategory(String name, String description) {
        // TODO Day 3
        logger.info("AddCategory called for: {}", name);
        return null;
    }

    public List<Category> getAllCategories() {
        // TODO Day 3
        logger.info("GetAllCategories called");
        return null;
    }

    public Category getCategoryById(Long id) {
        // TODO Day 3
        logger.info("GetCategoryById called for id: {}", id);
        return null;
    }

    public void deleteCategory(Long id) {
        // TODO Day 3
        logger.info("DeleteCategory called for id: {}", id);
    }
}
