package com.example.carins.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Data transfer object representing an insurance policy")
public record InsurancePolicyDto(
        @Schema(description = "Unique identifier of the insurance policy", example = "1")
        Long id,

        @Schema(description = "ID of the insured car", example = "1")
        Long carId,

        @Schema(description = "Insurance provider name", example = "State Farm")
        String provider,

        @Schema(description = "Policy start date", example = "2024-01-01")
        LocalDate startDate,

        @Schema(description = "Policy end date", example = "2025-01-01")
        LocalDate endDate
) {
}
