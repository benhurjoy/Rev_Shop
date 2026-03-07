package com.revshop.service;

import com.revshop.dto.LoginDTO;
import com.revshop.dto.RegisterDTO;
import com.revshop.entity.User;
import com.revshop.repository.OtpVerificationRepository;
import com.revshop.repository.UserRepository;
import com.revshop.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .password("encodedPassword")
                .phone("9876543210")
                .role(User.Role.BUYER)
                .enabled(true)
                .blocked(false)
                .build();

        registerDTO = new RegisterDTO();
        registerDTO.setFirstName("John");
        registerDTO.setLastName("Doe");
        registerDTO.setEmail("john@test.com");
        registerDTO.setPassword("password123");
        registerDTO.setPhone("9876543210");
        registerDTO.setRole(User.Role.BUYER);

        loginDTO = new LoginDTO();
        loginDTO.setEmail("john@test.com");
        loginDTO.setPassword("password123");
    }

    @Test
    void register_NewUser_ShouldSaveSuccessfully() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(otpVerificationRepository.save(any())).thenReturn(null);

        assertDoesNotThrow(() -> authService.register(registerDTO));

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_DuplicateEmail_ShouldThrowException() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(Exception.class, () -> authService.register(registerDTO));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock.jwt.token");

        String token = authService.login(loginDTO);

        assertNotNull(token);
        assertEquals("mock.jwt.token", token);
    }

    @Test
    void login_WrongPassword_ShouldThrowException() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(Exception.class, () -> authService.login(loginDTO));
    }

    @Test
    void login_BlockedUser_ShouldThrowException() {
        // Service checks isBlocked() BEFORE passwordEncoder.matches() — don't stub matcher
        mockUser.setBlocked(true);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(Exception.class, () -> authService.login(loginDTO));
    }

    @Test
    void login_UserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        loginDTO.setEmail("notfound@test.com");
        assertThrows(Exception.class, () -> authService.login(loginDTO));
    }

    @Test
    void sendOtp_ValidEmail_ShouldSaveOtpAndSendEmail() {
        // sendOtp() does NOT check existsByEmail — it just saves OTP and sends email directly
        when(otpVerificationRepository.save(any())).thenReturn(null);

        assertDoesNotThrow(() -> authService.sendOtp("john@test.com"));

        verify(emailService).sendOtpEmail(eq("john@test.com"), anyString());
    }
}