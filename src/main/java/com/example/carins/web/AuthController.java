package com.example.carins.web;

import com.example.carins.service.AuthService;
import com.example.carins.web.dto.LoginDto;
import com.example.carins.web.dto.RegisterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<Map<String, Object>> register(
            @Parameter(description = "Registration details", required = true) @Valid @RequestBody RegisterDto registerDto) {
        log.info("Registration request for username: {}", registerDto.username());

        Map<String, Object> response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and create JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid login data")
    })
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Login credentials", required = true) @Valid @RequestBody LoginDto loginDto,
            HttpServletResponse response) {
        log.info("Login request for username: {}", loginDto.username());

        Map<String, Object> responseBody = authService.login(loginDto, response);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and clear JWT token")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        log.info("Logout request");

        Map<String, String> responseBody = authService.logout(response);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify authentication", description = "Check if user is authenticated")
    public ResponseEntity<Map<String, Object>> verify() {
        log.info("Verify request");

        Map<String, Object> response = authService.verifyAuthentication();
        return ResponseEntity.ok(response);
    }
}
