package com.revshop.service;

import com.revshop.dto.DashboardDTO;
import com.revshop.dto.UserDTO;
import com.revshop.entity.User;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger logger = LogManager.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public List<UserDTO> getAllUsers() {
        logger.info("Admin fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockUser(Long userId) {
        logger.info("Admin blocking userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("BlockUser failed - user not found: {}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
        user.setBlocked(true);
        userRepository.save(user);
        logger.info("User blocked successfully: {}", userId);
    }

    @Transactional
    public void unblockUser(Long userId) {
        logger.info("Admin unblocking userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("UnblockUser failed - user not found: {}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
        user.setBlocked(false);
        userRepository.save(user);
        logger.info("User unblocked successfully: {}", userId);
    }

    @Transactional
    public void removeProduct(Long productId) {
        logger.info("Admin removing productId: {}", productId);
        if (!productRepository.existsById(productId)) {
            logger.warn("RemoveProduct failed - product not found: {}", productId);
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        productRepository.deleteById(productId);
        logger.info("Product removed successfully: {}", productId);
    }

    @Transactional
    public void removeReview(Long reviewId) {
        logger.info("Admin removing reviewId: {}", reviewId);
        if (!reviewRepository.existsById(reviewId)) {
            logger.warn("RemoveReview failed - review not found: {}", reviewId);
            throw new ResourceNotFoundException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
        logger.info("Review removed successfully: {}", reviewId);
    }

    public DashboardDTO getDashboardStats() {
        logger.info("Admin fetching dashboard stats");

        long totalUsers = userRepository.count();
        long totalBuyers = userRepository.findByRole(User.Role.BUYER).size();
        long totalSellers = userRepository.findByRole(User.Role.SELLER).size();
        long blockedUsers = userRepository.findByBlocked(true).size();
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.findByActiveTrue().size();
        long outOfStock = productRepository.findByStockQuantityEquals(0).size();
        long totalOrders = orderRepository.count();
        long totalReviews = reviewRepository.count();

        Double revenue = orderRepository.sumTotalRevenue();

        return DashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalBuyers(totalBuyers)
                .totalSellers(totalSellers)
                .blockedUsers(blockedUsers)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .outOfStockProducts(outOfStock)
                .totalOrders(totalOrders)
                .totalRevenue(revenue != null ? revenue : 0.0)
                .totalReviews(totalReviews)
                .build();
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setBlocked(user.isBlocked());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}