package com.revshop.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidationUtil {

    private static final Logger logger = LogManager.getLogger(ValidationUtil.class);

    // Validate email format
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        boolean valid = email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        if (!valid) logger.warn("Invalid email format detected");
        return valid;
    }

    // Validate phone number (10 digits)
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        boolean valid = phone.matches("^[0-9]{10}$");
        if (!valid) logger.warn("Invalid phone number detected");
        return valid;
    }

    // Validate OTP (6 digits)
    public static boolean isValidOtp(String otp) {
        if (otp == null || otp.isBlank()) return false;
        return otp.matches("^[0-9]{6}$");
    }

    // Validate pincode (6 digits)
    public static boolean isValidPincode(String pincode) {
        if (pincode == null || pincode.isBlank()) return false;
        return pincode.matches("^[0-9]{6}$");
    }

    // Validate rating (1-5)
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    // Validate stock quantity is positive
    public static boolean isValidQuantity(int quantity) {
        return quantity > 0;
    }

    // Validate price is positive
    public static boolean isValidPrice(double price) {
        return price > 0;
    }

    // Sanitize string — strips HTML tags
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("<[^>]*>", "");
    }

    // Capitalize first letter
    public static String capitalize(String input) {
        if (input == null || input.isBlank()) return "";
        return input.substring(0, 1).toUpperCase()
                + input.substring(1).toLowerCase();
    }
}

