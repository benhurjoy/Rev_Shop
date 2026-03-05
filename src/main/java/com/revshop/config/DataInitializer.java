package com.revshop.config;

import com.revshop.entity.User;
import com.revshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        if (!userRepository.existsByEmail("admin@revshop.com")) {

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("RevShop")
                    .email("admin@revshop.com")
                    .phone("9999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)       // ← Role is inside User
                    .enabled(true)
                    .blocked(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin user created: admin@revshop.com / admin123");
        } else {
            System.out.println("ℹ️  Admin user already exists.");
        }
    }
}