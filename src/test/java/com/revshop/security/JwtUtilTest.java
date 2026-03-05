package com.revshop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_for_unit_testing_only_32chars");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_ValidInput_ShouldReturnToken() {
        String token = jwtUtil.generateToken("john@test.com", "BUYER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_ValidToken_ShouldReturnEmail() {
        String token = jwtUtil.generateToken("john@test.com", "BUYER");
        String email = jwtUtil.extractEmail(token);

        assertEquals("john@test.com", email);
    }

    @Test
    void extractRole_ValidToken_ShouldReturnRole() {
        String token = jwtUtil.generateToken("john@test.com", "SELLER");
        String role = jwtUtil.extractRole(token);

        assertEquals("SELLER", role);
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken("john@test.com", "BUYER");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_TamperedToken_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("invalid.tampered.token"));
    }

    @Test
    void isTokenExpired_FreshToken_ShouldReturnFalse() {
        String token = jwtUtil.generateToken("john@test.com", "BUYER");

        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void generateToken_DifferentRoles_ShouldEncodeCorrectly() {
        String buyerToken = jwtUtil.generateToken("buyer@test.com", "BUYER");
        String sellerToken = jwtUtil.generateToken("seller@test.com", "SELLER");
        String adminToken = jwtUtil.generateToken("admin@test.com", "ADMIN");

        assertEquals("BUYER", jwtUtil.extractRole(buyerToken));
        assertEquals("SELLER", jwtUtil.extractRole(sellerToken));
        assertEquals("ADMIN", jwtUtil.extractRole(adminToken));
    }
}