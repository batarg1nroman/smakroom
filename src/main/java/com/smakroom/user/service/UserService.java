package com.smakroom.user.service;

import com.smakroom.exception.BusinessException;
import com.smakroom.user.dto.ProfileDto;
import com.smakroom.user.dto.RegisterDto;
import com.smakroom.user.entity.Role;
import com.smakroom.user.entity.User;
import com.smakroom.user.entity.UserProfile;
import com.smakroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email уже зарегистрирован");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Пароли не совпадают");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        UserProfile profile = new UserProfile();
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        user.setProfile(profile);

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());
        return saved;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsernameWithProfile(username)
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));
    }

    @Transactional
    public void updateProfile(String username, ProfileDto dto) {
        User user = userRepository.findByUsernameWithProfile(username)
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            user.setProfile(profile);
        }
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhone(dto.getPhone());

        userRepository.save(user);
        log.info("Updated profile for user: {}", username);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}
