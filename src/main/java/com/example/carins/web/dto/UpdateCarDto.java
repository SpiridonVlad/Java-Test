package com.example.carins.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateCarDto(
        @Size(min = 5, max = 32, message = "VIN must be between 5 and 32 characters")
        String vin,

        String make,

        String model,

        @Min(value = 1900, message = "Year of manufacture must be 1900 or later")
        @Max(value = 2030, message = "Year of manufacture must be 2030 or earlier")
        Integer yearOfManufacture,

        Long ownerId
) {
}
