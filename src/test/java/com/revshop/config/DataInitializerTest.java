package com.revshop.config;

import com.revshop.entity.Category;
import com.revshop.entity.User;
import com.revshop.repository.CategoryRepository;
import com.revshop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_AdminNotExist_ShouldCreateAdmin() throws Exception {
        when(userRepository.existsByEmail("admin@revshop.com")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedAdmin123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.count()).thenReturn(6L); // categories already exist

        dataInitializer.run();

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("admin@revshop.com") &&
                        user.getRole() == User.Role.ADMIN &&
                        user.isEnabled() &&
                        !user.isBlocked()
        ));
    }

    @Test
    void run_AdminAlreadyExists_ShouldNotCreateDuplicate() throws Exception {
        when(userRepository.existsByEmail("admin@revshop.com")).thenReturn(true);
        when(categoryRepository.count()).thenReturn(6L); // categories already exist

        dataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_CategoriesNotExist_ShouldCreateSixCategories() throws Exception {
        when(userRepository.existsByEmail("admin@revshop.com")).thenReturn(true);
        when(categoryRepository.count()).thenReturn(0L);
        when(categoryRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        dataInitializer.run();

        verify(categoryRepository).saveAll(argThat((List<Category> list) -> list.size() == 6));
    }

    @Test
    void run_CategoriesAlreadyExist_ShouldNotDuplicate() throws Exception {
        when(userRepository.existsByEmail("admin@revshop.com")).thenReturn(true);
        when(categoryRepository.count()).thenReturn(6L);

        dataInitializer.run();

        verify(categoryRepository, never()).saveAll(any());
        verify(categoryRepository, never()).save(any());
    }
}
