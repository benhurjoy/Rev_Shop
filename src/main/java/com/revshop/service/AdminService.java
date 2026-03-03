package com.revshop.service;

import com.revshop.dto.DashboardDTO;
import com.revshop.dto.UserDTO;
import com.revshop.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public List<UserDTO> getAllUsers() {
        // TODO Day 3
        logger.info("GetAllUsers called");
        return null;
    }

    public void blockUser(Long userId) {
        // TODO Day 3
        logger.info("BlockUser called for userId: {}", userId);
    }

    public void unblockUser(Long userId) {
        // TODO Day 3
        logger.info("UnblockUser called for userId: {}", userId);
    }

    public void removeProduct(Long productId) {
        // TODO Day 3
        logger.info("RemoveProduct called for productId: {}", productId);
    }

    public void removeReview(Long reviewId) {
        // TODO Day 3
        logger.info("RemoveReview called for reviewId: {}", reviewId);
    }

    public DashboardDTO getDashboardStats() {
        // TODO Day 3
        logger.info("GetDashboardStats called");
        return null;
    }
}