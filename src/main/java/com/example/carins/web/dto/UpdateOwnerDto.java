package com.example.carins.web.dto;

import jakarta.validation.constraints.Email;

public record UpdateOwnerDto(
        String name,
        
        @Email(message = "Email must be valid")
        String email
) {
}
