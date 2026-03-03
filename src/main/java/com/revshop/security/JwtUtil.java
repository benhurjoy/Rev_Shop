package com.revshop.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LogManager.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Day 2 - Skeleton only
    // Full implementation on Day 3

    public String generateToken(String email, String role) {
        // TODO Day 3: implement token generation
        return null;
    }

    public String extractEmail(String token) {
        // TODO Day 3: implement email extraction
        return null;
    }

    public String extractRole(String token) {
        // TODO Day 3: implement role extraction
        return null;
    }

    public boolean validateToken(String token) {
        // TODO Day 3: implement token validation
        return false;
    }

    public boolean isTokenExpired(String token) {
        // TODO Day 3: implement expiry check
        return true;
    }

    private Key getSigningKey() {
        // TODO Day 3: implement signing key
        return null;
    }
}