package com.revshop.config;

import com.revshop.entity.Category;
import com.revshop.entity.User;
import com.revshop.repository.CategoryRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminIfAbsent();
        createDefaultCategoriesIfAbsent();
    }

    private void createAdminIfAbsent() {
        if (userRepository.existsByEmail("admin@revshop.com")) {
            logger.info("DataInitializer: Admin already exists, skipping.");
            return;
        }

        User admin = User.builder()
                .firstName("Admin")
                .lastName("RevShop")
                .email("admin@revshop.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("9999999999")
                .role(User.Role.ADMIN)
                .enabled(true)
                .blocked(false)
                .build();

        userRepository.save(admin);
        logger.info("DataInitializer: Admin user created — admin@revshop.com / admin123");
    }

    private void createDefaultCategoriesIfAbsent() {
        if (categoryRepository.count() > 0) {
            logger.info("DataInitializer: Categories already exist, skipping.");
            return;
        }

        List<Category> categories = List.of(
                Category.builder().name("Electronics").description("Phones, laptops, gadgets and accessories").build(),
                Category.builder().name("Fashion").description("Men, women and kids clothing and accessories").build(),
                Category.builder().name("Home & Kitchen").description("Furniture, appliances and home decor").build(),
                Category.builder().name("Books").description("Fiction, non-fiction, academic and more").build(),
                Category.builder().name("Sports & Fitness").description("Equipment, gear and activewear").build(),
                Category.builder().name("Beauty & Personal Care").description("Skincare, haircare and grooming").build()
        );

        categoryRepository.saveAll(categories);
        logger.info("DataInitializer: {} default categories created.", categories.size());
    }
}