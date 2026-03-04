package com.revshop.controller;

import com.revshop.dto.LoginDTO;
import com.revshop.dto.RegisterDTO;
import com.revshop.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    // ── Register ──────────────────────────────────────────────
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterDTO registerDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(registerDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Check your email for OTP.");
            redirectAttributes.addFlashAttribute("email", registerDTO.getEmail());
            logger.info("Registration successful for: {}", registerDTO.getEmail());
            return "redirect:/auth/verify-otp";
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    // ── OTP Verification ──────────────────────────────────────
    @GetMapping("/verify-otp")
    public String showVerifyOtpPage() {
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            authService.verifyOtp(email, otp);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Email verified! Please login.");
            logger.info("OTP verified for: {}", email);
            return "redirect:/auth/login";
        } catch (Exception e) {
            logger.error("OTP verification failed for: {}", email);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            authService.sendOtp(email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "OTP resent to " + email);
            redirectAttributes.addFlashAttribute("email", email);
            logger.info("OTP resent for: {}", email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auth/verify-otp";
    }

    // ── Login ────────────────────────────────────────────────
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute LoginDTO loginDTO,
            BindingResult result,
            HttpServletResponse response,
            Model model) {

        if (result.hasErrors()) {
            return "auth/login";
        }
        try {
            String token = authService.login(loginDTO);

            // Store JWT in HttpOnly cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 hours
            response.addCookie(cookie);

            // Redirect based on role
            String role = authService.getRoleFromToken(token);
            logger.info("Login successful for: {} role: {}", loginDTO.getEmail(), role);

            return switch (role) {
                case "ADMIN"  -> "redirect:/admin/dashboard";
                case "SELLER" -> "redirect:/seller/dashboard";
                default       -> "redirect:/buyer/home";
            };

        } catch (Exception e) {
            logger.error("Login failed for: {}", loginDTO.getEmail());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }

    // ── Logout ───────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        // Clear JWT cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        redirectAttributes.addFlashAttribute("successMessage",
                "Logged out successfully.");
        logger.info("User logged out");
        return "redirect:/auth/login";
    }

    // ── Forgot Password ───────────────────────────────────────
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            authService.sendPasswordResetEmail(email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "OTP sent to your email.");
            redirectAttributes.addFlashAttribute("email", email);
            logger.info("Password reset OTP sent to: {}", email);
            return "redirect:/auth/reset-password";
        } catch (Exception e) {
            logger.error("Forgot password failed for: {}", email);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    // ── Reset Password ────────────────────────────────────────
    @GetMapping("/reset-password")
    public String showResetPasswordPage() {
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            authService.resetPassword(email, otp, newPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset successfully! Please login.");
            logger.info("Password reset successful for: {}", email);
            return "redirect:/auth/login";
        } catch (Exception e) {
            logger.error("Password reset failed for: {}", email);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/auth/reset-password";
        }
    }
}