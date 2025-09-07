package com.example.carins.web.dto;

import jakarta.validation.constraints.*;

public record CreateCarDto(
        @NotBlank(message = "VIN is required")
        @Size(min = 5, max = 32, message = "VIN must be between 5 and 32 characters")
        String vin,

        @NotBlank(message = "Make is required")
        String make,

        @NotBlank(message = "Model is required")
        String model,

        @NotNull(message = "Year of manufacture is required")
        @Min(value = 1900, message = "Year of manufacture must be 1900 or later")
        @Max(value = 2030, message = "Year of manufacture must be 2030 or earlier")
        Integer yearOfManufacture,

        @NotNull(message = "Owner ID is required")
        Long ownerId
) {
}
