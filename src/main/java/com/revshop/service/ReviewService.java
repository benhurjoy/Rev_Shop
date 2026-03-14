package com.revshop.service;

import com.revshop.dto.ReviewDTO;
import com.revshop.dto.SellerReviewDTO;
import com.revshop.entity.Notification;
import com.revshop.entity.Product;
import com.revshop.entity.Review;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.OrderRepository;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.ReviewRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private static final Logger logger = LogManager.getLogger(ReviewService.class);

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public ReviewDTO addReview(String email, ReviewDTO dto) {
        logger.info("AddReview called by: {} for productId: {}", email, dto.getProductId());

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + dto.getProductId()));

        if (!orderRepository.existsByBuyerAndProductDelivered(buyer, dto.getProductId())) {
            logger.warn("Review blocked - no delivered purchase: {} for productId: {}", email, dto.getProductId());
            throw new BadRequestException("You can only review products you have purchased and received.");
        }

        if (reviewRepository.existsByProductAndBuyer(product, buyer)) {
            logger.warn("Duplicate review attempt by: {} for productId: {}", email, dto.getProductId());
            throw new BadRequestException("You have already reviewed this product.");
        }

        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .product(product)
                .buyer(buyer)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        logger.info("Review added successfully: {} by: {}", saved.getId(), email);

        try {
            String sellerEmail = product.getSeller().getEmail();
            String title = "New Review on Your Product";
            String message = "\"" + product.getName() + "\" received a " + dto.getRating()
                    + "-star review from " + buyer.getFirstName() + " " + buyer.getLastName() + ".";
            notificationService.sendNotification(
                    sellerEmail, title, message, Notification.NotificationType.REVIEW_ADDED);
        } catch (Exception e) {
            logger.error("Failed to notify seller for productId: {} - {}", dto.getProductId(), e.getMessage());
        }

        return mapToDTO(saved);
    }

    // ── Edit buyer's own review ───────────────────────────────
    @Transactional
    public void editReview(Long reviewId, String email, int rating, String comment) {
        logger.info("EditReview called for reviewId: {} by: {}", reviewId, email);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        // Ensure the buyer owns this review
        if (!review.getBuyer().getEmail().equals(email)) {
            throw new BadRequestException("You can only edit your own reviews.");
        }

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
        logger.info("Review updated: {} by: {}", reviewId, email);
    }

    // ── Delete by buyer (owns it) ─────────────────────────────
    @Transactional
    public void deleteReviewByBuyer(Long reviewId, String email) {
        logger.info("DeleteReviewByBuyer called for reviewId: {} by: {}", reviewId, email);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getBuyer().getEmail().equals(email)) {
            throw new BadRequestException("You can only delete your own reviews.");
        }

        reviewRepository.deleteById(reviewId);
        logger.info("Review deleted: {} by buyer: {}", reviewId, email);
    }

    // ── Delete by admin (no ownership check) ─────────────────
    @Transactional
    public void deleteReview(Long reviewId) {
        logger.info("DeleteReview (admin) called for reviewId: {}", reviewId);
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
        logger.info("Review deleted (admin): {}", reviewId);
    }

    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        logger.info("GetReviewsByProduct called for productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return reviewRepository.findByProductWithBuyer(product)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SellerReviewDTO> getReviewsBySeller(String email, String productFilter, Integer ratingFilter) {
        logger.info("GetReviewsBySeller called for seller: {}", email);
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return reviewRepository.findByProduct_Seller(seller)
                .stream()
                .filter(r -> productFilter == null || productFilter.isBlank() ||
                        r.getProduct().getName().toLowerCase().contains(productFilter.toLowerCase()))
                .filter(r -> ratingFilter == null || r.getRating() == ratingFilter)
                .map(r -> {
                    SellerReviewDTO dto = new SellerReviewDTO();
                    dto.setReviewId(r.getId());
                    dto.setProductId(r.getProduct().getId());
                    dto.setProductName(r.getProduct().getName());
                    dto.setProductImageUrl(r.getProduct().getImageUrl());
                    dto.setBuyerName(r.getBuyer().getFirstName() + " " + r.getBuyer().getLastName());
                    dto.setBuyerEmail(r.getBuyer().getEmail());
                    dto.setRating(r.getRating());
                    dto.setComment(r.getComment());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    public Double getAverageRating(Long productId) {
        logger.info("GetAverageRating called for productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Double avg = reviewRepository.findAverageRatingByProduct(product);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public boolean hasAlreadyReviewed(String email, Long productId) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return reviewRepository.existsByProductAndBuyer(product, buyer);
    }

    public boolean hasPurchasedProduct(String email, Long productId) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return orderRepository.existsByBuyerAndProductDelivered(buyer, productId);
    }

    public ReviewDTO getReviewByBuyer(String email, Long productId) {
        logger.info("GetReviewByBuyer called for: {} productId: {}", email, productId);
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return reviewRepository.findByProductAndBuyer(product, buyer)
                .map(this::mapToDTO)
                .orElse(null);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setBuyerId(review.getBuyer().getId());
        dto.setBuyerName(review.getBuyer().getFirstName() + " " + review.getBuyer().getLastName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}