package com.revshop.service;

import com.revshop.dto.LoginDTO;
import com.revshop.dto.RegisterDTO;
import com.revshop.repository.UserRepository;
import com.revshop.security.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public void register(RegisterDTO registerDTO) {
        // TODO Day 3
        logger.info("Register method called for email: {}", registerDTO.getEmail());
    }

    public String login(LoginDTO loginDTO) {
        // TODO Day 3
        logger.info("Login method called for email: {}", loginDTO.getEmail());
        return null;
    }

    public void sendOtp(String email) {
        // TODO Day 3
        logger.info("SendOtp called for email: {}", email);
    }

    public boolean verifyOtp(String email, String otp) {
        // TODO Day 3
        logger.info("VerifyOtp called for email: {}", email);
        return false;
    }

    public void sendPasswordResetEmail(String email) {
        // TODO Day 3
        logger.info("SendPasswordResetEmail called for: {}", email);
    }

    public void resetPassword(String email, String newPassword) {
        // TODO Day 3
        logger.info("ResetPassword called for: {}", email);
    }
}