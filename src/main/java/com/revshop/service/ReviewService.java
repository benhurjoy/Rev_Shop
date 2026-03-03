package com.revshop.service;
import com.revshop.dto.ReviewDTO;
import com.revshop.repository.ProductRepository;
import com.revshop.repository.ReviewRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ReviewService {
    private static final Logger logger = LogManager.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
}
