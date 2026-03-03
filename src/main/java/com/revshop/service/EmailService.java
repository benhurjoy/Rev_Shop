package com.revshop.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    @Async
    public void sendOtpEmail(String to, String otp) {
        // TODO Day 3
        logger.info("SendOtpEmail called for: {}", to);
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        // TODO Day 3
        logger.info("SendPasswordResetEmail called for: {}", to);
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        // TODO Day 3
        logger.info("SendWelcomeEmail called for: {}", to);
    }
}