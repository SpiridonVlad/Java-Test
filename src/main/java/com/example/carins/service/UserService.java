package com.example.carins.service;

import com.example.carins.exception.AuthenticationException;
import com.example.carins.exception.UserAlreadyExistsException;
import com.example.carins.model.User;
import com.example.carins.repo.UserRepository;
import com.example.carins.web.dto.LoginDto;
import com.example.carins.web.dto.RegisterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;


    public User register(RegisterDto registerDto) {
        log.info("Attempting to register user: {}", registerDto.username());

        if (userRepository.existsByUsername(registerDto.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + registerDto.username());
        }

        if (userRepository.existsByEmail(registerDto.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + registerDto.email());
        }

        User user = new User(
                registerDto.username(),
                passwordEncoder.encode(registerDto.password()),
                registerDto.email(),
                User.Role.USER
        );

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {}", savedUser.getUsername());
        return savedUser;
    }

    public User login(LoginDto loginDto) {
        log.info("Attempting to login user: {}", loginDto.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(loginDto.username())
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            log.info("Successfully logged in user: {}", user.getUsername());
            return user;
        } catch (Exception e) {
            log.warn("Failed login attempt for user: {}", loginDto.username());
            throw new AuthenticationException("Invalid username or password");
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Current user not found"));
    }
}
