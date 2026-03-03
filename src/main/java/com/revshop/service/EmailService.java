package com.revshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String to, String otp) {
        logger.info("Sending OTP email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("RevShop – Your OTP Verification Code");
            helper.setText(buildOtpEmailBody(otp), true);
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to: {} - {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String otp) {
        logger.info("Sending password reset email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("RevShop – Password Reset Request");
            helper.setText(buildPasswordResetEmailBody(otp), true);
            mailSender.send(message);
            logger.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {} - {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        logger.info("Sending welcome email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to RevShop!");
            helper.setText(buildWelcomeEmailBody(name), true);
            mailSender.send(message);
            logger.info("Welcome email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send welcome email to: {} - {}", to, e.getMessage());
        }
    }

    private String buildOtpEmailBody(String otp) {
        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#1B3A6B;'>RevShop Email Verification</h2>"
                + "<p>Your OTP verification code is:</p>"
                + "<h1 style='color:#2E5FA3;letter-spacing:8px;'>" + otp + "</h1>"
                + "<p>This OTP is valid for <strong>10 minutes</strong>.</p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "<hr/><p style='color:#888;font-size:12px;'>RevShop Team</p>"
                + "</div>";
    }

    private String buildPasswordResetEmailBody(String otp) {
        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#1B3A6B;'>RevShop Password Reset</h2>"
                + "<p>Your password reset OTP is:</p>"
                + "<h1 style='color:#2E5FA3;letter-spacing:8px;'>" + otp + "</h1>"
                + "<p>This OTP is valid for <strong>10 minutes</strong>.</p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "<hr/><p style='color:#888;font-size:12px;'>RevShop Team</p>"
                + "</div>";
    }

    private String buildWelcomeEmailBody(String name) {
        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#1B3A6B;'>Welcome to RevShop, " + name + "!</h2>"
                + "<p>Your account has been verified successfully.</p>"
                + "<p>Start shopping or selling today!</p>"
                + "<hr/><p style='color:#888;font-size:12px;'>RevShop Team</p>"
                + "</div>";
    }
}