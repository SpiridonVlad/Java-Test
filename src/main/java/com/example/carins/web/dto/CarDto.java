package com.example.carins.web.dto;

public record CarDto(
        Long id,
        String vin,
        String make,
        String model,
        int yearOfManufacture,
        OwnerDto owner
) {
    public record OwnerDto(Long id, String name, String email) {
    }
}
