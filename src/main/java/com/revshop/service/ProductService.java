package com.revshop.service;

import com.revshop.dto.ProductDTO;
import com.revshop.entity.Product;
import com.revshop.repository.CategoryRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public Product addProduct(ProductDTO productDTO, String sellerEmail) {
        // TODO Day 3
        logger.info("AddProduct called by seller: {}", sellerEmail);
        return null;
    }

    public Product updateProduct(Long productId, ProductDTO productDTO, String sellerEmail) {
        // TODO Day 3
        logger.info("UpdateProduct called for productId: {}", productId);
        return null;
    }

    public void deleteProduct(Long productId, String sellerEmail) {
        // TODO Day 3
        logger.info("DeleteProduct called for productId: {}", productId);
    }

    public List<ProductDTO> getAllActiveProducts() {
        // TODO Day 3
        logger.info("GetAllActiveProducts called");
        return null;
    }

    public List<ProductDTO> getProductsBySeller(String sellerEmail) {
        // TODO Day 3
        logger.info("GetProductsBySeller called for: {}", sellerEmail);
        return null;
    }

    public ProductDTO getProductById(Long productId) {
        // TODO Day 3
        logger.info("GetProductById called for productId: {}", productId);
        return null;
    }

    public List<ProductDTO> searchProducts(String keyword) {
        // TODO Day 3
        logger.info("SearchProducts called with keyword: {}", keyword);
        return null;
    }

    public List<ProductDTO> filterByCategory(Long categoryId) {
        // TODO Day 3
        logger.info("FilterByCategory called for categoryId: {}", categoryId);
        return null;
    }

    public void toggleProductVisibility(Long productId, String sellerEmail) {
        // TODO Day 3
        logger.info("ToggleVisibility called for productId: {}", productId);
    }
}
