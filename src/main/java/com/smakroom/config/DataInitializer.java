package com.smakroom.config;

import com.smakroom.product.entity.Category;
import com.smakroom.product.repository.CategoryRepository;
import com.smakroom.user.entity.Role;
import com.smakroom.user.entity.User;
import com.smakroom.user.entity.UserProfile;
import com.smakroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initAdmin();
        initCategories();
    }

    private void initAdmin() {
        if (userRepository.existsByUsername("admin")) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@smakroom.ru");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);

        UserProfile profile = new UserProfile();
        profile.setFirstName("Администратор");
        profile.setLastName("SmakRoom");
        admin.setProfile(profile);

        userRepository.save(admin);
        log.info("Admin user created: admin / admin123");
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) return;

        List<Category> categories = List.of(
            new Category("Торты", "Красивые и вкусные торты на любой праздник"),
            new Category("Пирожные", "Мини-десерты: эклеры, макаронс, тарталетки"),
            new Category("Печенье", "Авторское печенье и сладкие наборы"),
            new Category("Зефир", "Домашний зефир в ассортименте"),
            new Category("Шоколад", "Конфеты и шоколадные изделия ручной работы")
        );
        categoryRepository.saveAll(categories);
        log.info("Default categories created");
    }
}
