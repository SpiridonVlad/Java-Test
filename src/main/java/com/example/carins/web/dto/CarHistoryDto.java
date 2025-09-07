package com.example.carins.web.dto;

import java.util.List;

public record CarHistoryDto(
        Long carId,
        String vin,
        String make,
        String model,
        int yearOfManufacture,
        List<HistoryEventDto> events
) {
}
