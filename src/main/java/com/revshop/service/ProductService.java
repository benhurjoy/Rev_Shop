package com.revshop.service;

import com.revshop.dto.ProductDTO;
import com.revshop.entity.Category;
import com.revshop.entity.Product;
import com.revshop.entity.User;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.CategoryRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.ReviewRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // ───────────────── ADD PRODUCT ─────────────────
    @Transactional
    public Product addProduct(ProductDTO dto, String sellerEmail) {
        logger.info("AddProduct called by seller: {}", sellerEmail);

        User seller = getUserByEmail(sellerEmail);
        Category category = getCategoryById(dto.getCategoryId());

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .mrp(dto.getMrp())
                .discountPercent(dto.getDiscountPercent())
                .stockQuantity(dto.getStockQuantity())
                .imageUrl(dto.getImageUrl())   // already a clean web path from controller
                .active(true)
                .category(category)
                .seller(seller)
                .build();

        Product saved = productRepository.save(product);
        logger.info("Product added successfully: {}", saved.getId());
        return saved;
    }

    // ───────────────── UPDATE PRODUCT ─────────────────
    @Transactional
    public Product updateProduct(Long productId, ProductDTO dto, String sellerEmail) {
        logger.info("UpdateProduct called for productId: {}", productId);

        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Category category = getCategoryById(dto.getCategoryId());

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setMrp(dto.getMrp());
        product.setDiscountPercent(dto.getDiscountPercent());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(category);

        // Only update image if a new one was uploaded (dto.imageUrl will be set by controller)
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            product.setImageUrl(dto.getImageUrl());
        }

        Product updated = productRepository.save(product);
        logger.info("Product updated successfully: {}", productId);
        return updated;
    }

    // ───────────────── DELETE PRODUCT (SOFT DELETE) ─────────────────
    @Transactional
    public void deleteProduct(Long productId, String sellerEmail) {
        logger.info("DeleteProduct called for productId: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setActive(false);
        productRepository.save(product);
        logger.info("Product soft deleted: {}", productId);
    }

    // ───────────────── GET ALL PRODUCTS ─────────────────
    public List<ProductDTO> getAllActiveProducts() {
        logger.info("GetAllActiveProducts called");

        return productRepository.findAllActiveWithDetails()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ───────────────── GET PRODUCTS BY SELLER ─────────────────
    public List<ProductDTO> getProductsBySeller(String sellerEmail) {
        logger.info("GetProductsBySeller called for: {}", sellerEmail);

        User seller = getUserByEmail(sellerEmail);

        return productRepository.findBySellerWithDetails(seller)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ───────────────── GET PRODUCT BY ID ─────────────────
    public ProductDTO getProductById(Long productId) {
        logger.info("GetProductById called for: {}", productId);

        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        return mapToDTO(product);
    }

    // ───────────────── SEARCH PRODUCTS ─────────────────
    public List<ProductDTO> searchProducts(String keyword) {
        logger.info("SearchProducts called with keyword: {}", keyword);

        return productRepository.findByNameContainingIgnoreCaseAndActiveTrueWithDetails(keyword)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ───────────────── FILTER BY CATEGORY ─────────────────
    public List<ProductDTO> filterByCategory(Long categoryId) {
        logger.info("FilterByCategory called for categoryId: {}", categoryId);

        return productRepository.findByCategoryIdAndActiveTrueWithDetails(categoryId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ───────────────── TOGGLE VISIBILITY ─────────────────
    @Transactional
    public void toggleProductVisibility(Long productId, String sellerEmail) {
        logger.info("ToggleVisibility called for productId: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setActive(!product.isActive());
        productRepository.save(product);
        logger.info("Visibility toggled: {}", product.isActive());
    }

    // ───────────────── REDUCE STOCK ─────────────────
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        logger.info("ReduceStock called for productId: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    // ───────────────── DTO MAPPER ─────────────────
    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setMrp(product.getMrp());
        dto.setDiscountPercent(product.getDiscountPercent());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setActive(product.isActive());


        String raw = product.getImageUrl();
        if (raw != null && !raw.isBlank()) {
            String filename = Paths.get(raw).getFileName().toString();
            dto.setImageUrl("/uploads/products/" + filename);
        } else {
            dto.setImageUrl(null);
        }

        // SAFE: category and seller are always JOIN FETCHed by the new query methods
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName());
        }

        Double avgRating = reviewRepository.findAverageRatingByProduct(product);
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        dto.setTotalReviews(reviewRepository.countByProduct(product));

        return dto;
    }

    // ───────────────── HELPER METHODS ─────────────────
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}