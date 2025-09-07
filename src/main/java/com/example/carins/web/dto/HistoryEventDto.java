package com.example.carins.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HistoryEventDto(
        String type,
        LocalDate date,
        String description,
        LocalDateTime timestamp
) {
}
