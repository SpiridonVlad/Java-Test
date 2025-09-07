package com.example.carins.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOwnerDto(
        @NotBlank(message = "Name is required")
        String name,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
