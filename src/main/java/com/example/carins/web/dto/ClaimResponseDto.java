package com.example.carins.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponseDto(
        Long id,
        Long carId,
        LocalDate claimDate,
        String description,
        BigDecimal amount,
        LocalDateTime createdAt
) {
}
