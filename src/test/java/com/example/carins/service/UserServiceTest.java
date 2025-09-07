package com.example.carins.service;

import com.example.carins.exception.AuthenticationException;
import com.example.carins.exception.UserAlreadyExistsException;
import com.example.carins.model.User;
import com.example.carins.repo.UserRepository;
import com.example.carins.web.dto.LoginDto;
import com.example.carins.web.dto.RegisterDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, authenticationManager);
    }


    @Test
    void register_ValidUser_Success() {
        RegisterDto registerDto = new RegisterDto("testuser", "password123", "test@example.com");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User("testuser", "encodedPassword", "test@example.com", User.Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.register(registerDto);

        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        RegisterDto registerDto = new RegisterDto("existinguser", "password123", "test@example.com");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(registerDto));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        RegisterDto registerDto = new RegisterDto("testuser", "password123", "existing@example.com");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(registerDto));
    }

    @Test
    void login_ValidCredentials_Success() {
        LoginDto loginDto = new LoginDto("testuser", "password123");
        User user = new User("testuser", "encodedPassword", "test@example.com", User.Role.USER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.login(loginDto);

        assertEquals("testuser", result.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginDto loginDto = new LoginDto("testuser", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationException.class,
                () -> userService.login(loginDto));
    }
}
