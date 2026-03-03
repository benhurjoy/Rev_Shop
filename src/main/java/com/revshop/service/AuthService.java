package com.revshop.service;

import com.revshop.dto.LoginDTO;
import com.revshop.dto.RegisterDTO;
import com.revshop.entity.OtpVerification;
import com.revshop.entity.User;
import com.revshop.exception.UserAlreadyExistsException;
import com.revshop.repository.OtpVerificationRepository;
import com.revshop.repository.UserRepository;
import com.revshop.security.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void register(RegisterDTO dto) {
        logger.info("Register attempt for email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .enabled(false)
                .blocked(false)
                .build();

        userRepository.save(user);
        logger.info("User registered successfully: {}", dto.getEmail());

        sendOtp(dto.getEmail());
    }

    public String login(LoginDTO dto) {
        logger.info("Login attempt for email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", dto.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (user.isBlocked()) {
            logger.warn("Login failed - account blocked: {}", dto.getEmail());
            throw new BadCredentialsException("Your account has been blocked");
        }

        if (!user.isEnabled()) {
            logger.warn("Login failed - account not verified: {}", dto.getEmail());
            throw new BadCredentialsException("Please verify your email first");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            logger.warn("Login failed - wrong password for: {}", dto.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        logger.info("Login successful for: {}", dto.getEmail());
        return token;
    }

    @Transactional
    public void sendOtp(String email) {
        logger.info("Sending OTP for email: {}", email);

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(email, otp);
        logger.info("OTP sent to: {}", email);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        logger.info("Verifying OTP for email: {}", email);

        OtpVerification otpVerification = otpVerificationRepository
                .findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> {
                    logger.warn("OTP verification failed - invalid OTP for: {}", email);
                    return new BadCredentialsException("Invalid or expired OTP");
                });

        if (otpVerification.isExpired()) {
            logger.warn("OTP expired for: {}", email);
            throw new BadCredentialsException("OTP has expired. Please request a new one");
        }

        otpVerification.setUsed(true);
        otpVerificationRepository.save(otpVerification);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        logger.info("OTP verified successfully for: {}", email);
        return true;
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        logger.info("Password reset request for: {}", email);

        userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Password reset failed - user not found: {}", email);
                    return new BadCredentialsException("No account found with this email");
                });

        sendOtp(email);
        logger.info("Password reset OTP sent to: {}", email);
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        logger.info("Resetting password for: {}", email);

        verifyOtp(email, otp);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password reset successful for: {}", email);
    }
}