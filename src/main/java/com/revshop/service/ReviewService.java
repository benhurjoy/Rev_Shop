package com.revshop.service;

import com.revshop.dto.ReviewDTO;
import com.revshop.entity.Product;
import com.revshop.entity.Review;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
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
public class ReviewService {

    private static final Logger logger = LogManager.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ReviewDTO addReview(String email, ReviewDTO dto) {
        logger.info("AddReview called by: {} for productId: {}", email, dto.getProductId());

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + dto.getProductId()));

        if (reviewRepository.existsByProductAndBuyer(product, buyer)) {
            logger.warn("Duplicate review attempt by: {} for productId: {}", email, dto.getProductId());
            throw new BadRequestException("You have already reviewed this product");
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
        return mapToDTO(saved);
    }

    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        logger.info("GetReviewsByProduct called for productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return reviewRepository.findByProductOrderByCreatedAtDesc(product)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        logger.info("DeleteReview called for reviewId: {}", reviewId);
        if (!reviewRepository.existsById(reviewId)) {
            logger.warn("Review not found for delete: {}", reviewId);
            throw new ResourceNotFoundException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
        logger.info("Review deleted: {}", reviewId);
    }

    public Double getAverageRating(Long productId) {
        logger.info("GetAverageRating called for productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Double avg = reviewRepository.findAverageRatingByProduct(product);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public boolean hasAlreadyReviewed(String email, Long productId) {
        logger.info("HasAlreadyReviewed called for: {} productId: {}", email, productId);
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return reviewRepository.existsByProductAndBuyer(product, buyer);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setBuyerId(review.getBuyer().getId());
        dto.setBuyerName(review.getBuyer().getFirstName()
                + " " + review.getBuyer().getLastName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}

