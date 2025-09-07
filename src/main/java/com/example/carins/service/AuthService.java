package com.example.carins.service;

import com.example.carins.config.JwtUtil;
import com.example.carins.model.User;
import com.example.carins.web.dto.LoginDto;
import com.example.carins.web.dto.RegisterDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    private static final String JWT_COOKIE_NAME = "jwt-token";
    private static final int JWT_COOKIE_EXPIRY = 24 * 60 * 60;

    public Map<String, Object> register(RegisterDto registerDto) {
        log.info("Processing registration for username: {}", registerDto.username());
        
        User user = userService.register(registerDto);
        
        return Map.of(
                "message", "User registered successfully",
                "userId", user.getId(),
                "username", user.getUsername()
        );
    }

    public Map<String, Object> login(LoginDto loginDto, HttpServletResponse response) {
        log.info("Processing login for username: {}", loginDto.username());
        
        User user = userService.login(loginDto);
        String jwtToken = jwtUtil.generateToken(user);
        
        setJwtCookie(response, jwtToken);
        
        return Map.of(
                "message", "Login successful",
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name()
        );
    }

    public Map<String, String> logout(HttpServletResponse response) {
        log.info("Processing logout");
        
        clearJwtCookie(response);
        SecurityContextHolder.clearContext();
        
        return Map.of("message", "Logout successful");
    }

    public Map<String, Object> verifyAuthentication() {
        log.info("Verifying authentication");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (isValidAuthentication(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            
            log.debug("User authenticated: {}", currentUser.getUsername());
            return Map.of(
                    "authenticated", true,
                    "userId", currentUser.getId(),
                    "username", currentUser.getUsername(),
                    "role", currentUser.getRole().name()
            );
        }
        
        log.debug("Authentication validation failed");
        return Map.of("authenticated", false);
    }

    private boolean isValidAuthentication(Authentication authentication) {
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal()) &&
               authentication.getPrincipal() instanceof User;
    }

    private void setJwtCookie(HttpServletResponse response, String jwtToken) {
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(JWT_COOKIE_EXPIRY);
        response.addCookie(jwtCookie);
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
    }
}
