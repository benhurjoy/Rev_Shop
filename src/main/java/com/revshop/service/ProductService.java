package com.revshop.service;

import com.revshop.dto.ProductDTO;
import com.revshop.dto.ProductVariantDTO;
import com.revshop.entity.Category;
import com.revshop.entity.Product;
import com.revshop.entity.ProductVariant;
import com.revshop.entity.User;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.CategoryRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.ProductVariantRepository;
import com.revshop.repository.ReviewRepository;
import com.revshop.repository.UserRepository;
import com.revshop.repository.WishlistRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);
    private static final int LOW_STOCK_THRESHOLD = 5;

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private NotificationService notificationService;

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
                .discountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0)
                .stockQuantity(0)
                .imageUrl(dto.getImageUrl())
                .active(true)
                .deleted(false)
                .category(category)
                .seller(seller)
                .build();

        // Attach variants
        if (dto.getVariants() != null) {
            for (ProductVariantDTO vDto : dto.getVariants()) {
                ProductVariant v = mapVariantFromDTO(vDto, product);
                product.getVariants().add(v);
            }
        }

        product.syncStockFromVariants();
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
        product.setDiscountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0);
        product.setCategory(category);

        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            product.setImageUrl(dto.getImageUrl());
        }

        // Replace variants: clear old ones, add new ones
        product.getVariants().clear();
        if (dto.getVariants() != null) {
            for (ProductVariantDTO vDto : dto.getVariants()) {
                ProductVariant v = mapVariantFromDTO(vDto, product);
                product.getVariants().add(v);
            }
        }

        product.syncStockFromVariants();
        Product updated = productRepository.save(product);
        logger.info("Product updated successfully: {}", productId);
        return updated;
    }

    // ───────────────── DELETE ─────────────────
    @Transactional
    public void deleteProduct(Long productId, String sellerEmail) {
        deleteProduct(productId);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        product.setDeleted(true);
        product.setActive(false);
        productRepository.save(product);
    }

    // ───────────────── QUERIES ─────────────────
    public List<ProductDTO> getAllActiveProducts() {
        return productRepository.findAllActiveWithDetails()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsBySeller(String sellerEmail) {
        User seller = getUserByEmail(sellerEmail);
        return productRepository.findBySellerNotDeletedWithDetails(seller)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return mapToDTO(product);
    }

    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrueWithDetails(keyword)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> filterByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrueWithDetails(categoryId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void toggleProductVisibility(Long productId, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    // ───────────────── REDUCE STOCK (variant-aware) ─────────────────
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        reduceStock(productId, null, quantity);
    }

    @Transactional
    public void reduceStock(Long productId, Long variantId, int quantity) {
        logger.info("ReduceStock called for productId: {} variantId: {}", productId, variantId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (variantId != null) {
            // Reduce specific variant stock
            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
            if (variant.getStockQuantity() < quantity) throw new RuntimeException("Insufficient stock");
            variant.setStockQuantity(variant.getStockQuantity() - quantity);
            variantRepository.save(variant);
        } else {
            // No variant — reduce base product stock
            if (product.getStockQuantity() < quantity) throw new RuntimeException("Insufficient stock");
        }

        // Keep product-level stock in sync
        product.syncStockFromVariants();
        int updatedStock = product.getStockQuantity();
        productRepository.save(product);

        if (updatedStock <= LOW_STOCK_THRESHOLD) {
            try {
                notificationService.sendLowStockAlert(
                        product.getSeller().getEmail(), product.getName(), updatedStock);
            } catch (Exception e) {
                logger.error("Failed to send low stock alert: {}", e.getMessage());
            }
            try {
                wishlistRepository.findAllByProductsContaining(product).forEach(wishlist -> {
                    try {
                        notificationService.sendWishlistLowStockAlert(
                                wishlist.getUser().getEmail(), product.getName(), updatedStock);
                    } catch (Exception ex) {
                        logger.error("Failed to notify wishlist buyer: {}", ex.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to fetch wishlist buyers: {}", e.getMessage());
            }
        }
    }

    // ───────────────── MAPPERS ─────────────────
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
            dto.setImageUrl("/uploads/products/" + Paths.get(raw).getFileName());
        }

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategoryHasColors(product.getCategory().isHasColors());
            dto.setCategoryHasSizes(product.getCategory().isHasSizes());
        }

        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName());
        }

        Double avg = reviewRepository.findAverageRatingByProduct(product);
        dto.setAverageRating(avg != null ? avg : 0.0);
        dto.setTotalReviews(reviewRepository.countByProduct(product));

        // Map variants
        if (product.getVariants() != null) {
            List<ProductVariantDTO> variantDTOs = product.getVariants().stream()
                    .filter(ProductVariant::isActive)
                    .map(v -> mapVariantToDTO(v, product.getPrice()))
                    .collect(Collectors.toList());
            dto.setVariants(variantDTOs);
        }

        return dto;
    }

    private ProductVariantDTO mapVariantToDTO(ProductVariant v, BigDecimal basePrice) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(v.getId());
        dto.setColor(v.getColor());
        dto.setSize(v.getSize());
        dto.setStockQuantity(v.getStockQuantity());
        dto.setPriceOverride(v.getPriceOverride());
        dto.setSkuCode(v.getSkuCode());
        dto.setActive(v.isActive());
        dto.setResolvedPrice(v.getPriceOverride() != null ? v.getPriceOverride() : basePrice);

        String raw = v.getImageUrl();
        if (raw != null && !raw.isBlank()) {
            dto.setImageUrl("/uploads/products/" + Paths.get(raw).getFileName());
        }
        return dto;
    }

    private ProductVariant mapVariantFromDTO(ProductVariantDTO dto, Product product) {
        return ProductVariant.builder()
                .product(product)
                .color(dto.getColor())
                .size(dto.getSize())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .priceOverride(dto.getPriceOverride())
                .imageUrl(dto.getImageUrl())
                .skuCode(dto.getSkuCode())
                .active(true)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}