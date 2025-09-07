package com.example.carins.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimCreateDto(
        @NotNull LocalDate claimDate,
        @NotBlank @Size(max = 1000) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
) {
}
