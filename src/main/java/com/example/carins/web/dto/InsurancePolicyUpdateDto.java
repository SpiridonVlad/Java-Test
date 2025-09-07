package com.example.carins.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Data transfer object for updating an existing insurance policy")
public record InsurancePolicyUpdateDto(
        @Schema(description = "ID of the car to insure", example = "1")
        Long carId,
        
        @Schema(description = "Insurance provider name", example = "Updated Insurance Co.")
        String provider,
        
        @Schema(description = "Policy start date", example = "2024-02-01", required = true)
        @NotNull LocalDate startDate,
        
        @Schema(description = "Policy end date", example = "2025-02-01", required = true)
        @NotNull LocalDate endDate
) {
}
