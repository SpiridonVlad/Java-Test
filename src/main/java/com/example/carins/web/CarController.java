package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.service.CarService;
import com.example.carins.service.ClaimService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.CarHistoryDto;
import com.example.carins.web.dto.ClaimCreateDto;
import com.example.carins.web.dto.ClaimResponseDto;
import com.example.carins.web.dto.CreateCarDto;
import com.example.carins.web.dto.UpdateCarDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Car Management", description = "APIs for managing cars, insurance policies, and claims")
public class CarController {


    private final CarService carService;
    private final ClaimService claimService;

    @GetMapping("/cars")
    @Operation(summary = "Get all cars", description = "Retrieve a list of all cars in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cars")
    public List<CarDto> getCars() {
        log.info("Fetching all cars");
        return carService.listCars().stream().map(this::toDto).toList();
    }

    @PostMapping("/cars")
    @Operation(summary = "Create a new car", description = "Create a new car in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Car created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid car data or VIN already exists"),
        @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    public ResponseEntity<CarDto> createCar(
            @Parameter(description = "Car details", required = true) @Valid @RequestBody CreateCarDto createCarDto) {
        log.info("Creating new car with VIN: {}", createCarDto.vin());
        
        Car createdCar = carService.createCar(createCarDto);
        CarDto carDto = toDto(createdCar);
        
        URI location = URI.create("/api/cars/" + createdCar.getId());
        return ResponseEntity.created(location).body(carDto);
    }

    @GetMapping("/cars/{carId}")
    @Operation(summary = "Get car by ID", description = "Retrieve a specific car by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved car"),
        @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<CarDto> getCarById(
            @Parameter(description = "Car ID", required = true) @PathVariable Long carId) {
        log.info("Fetching car with id: {}", carId);
        
        Car car = carService.getCarById(carId);
        return ResponseEntity.ok(toDto(car));
    }

    @PutMapping("/cars/{carId}")
    @Operation(summary = "Update a car", description = "Update an existing car. All insurance policies will be deleted when updating.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Car updated successfully"),
        @ApiResponse(responseCode = "404", description = "Car or owner not found"),
        @ApiResponse(responseCode = "400", description = "Invalid car data or VIN already exists")
    })
    public ResponseEntity<CarDto> updateCar(
            @Parameter(description = "Car ID", required = true) @PathVariable Long carId,
            @Parameter(description = "Updated car details", required = true) @Valid @RequestBody UpdateCarDto updateCarDto) {
        log.info("Updating car with id: {}", carId);
        
        Car updatedCar = carService.updateCar(carId, updateCarDto);
        return ResponseEntity.ok(toDto(updatedCar));
    }

    @DeleteMapping("/cars/{carId}")
    @Operation(summary = "Delete a car", description = "Delete a car and all its related insurance policies and claims")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Car deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<Void> deleteCar(
            @Parameter(description = "Car ID", required = true) @PathVariable Long carId) {
        log.info("Deleting car with id: {}", carId);
        
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    @Operation(summary = "Check insurance validity", description = "Check if a car's insurance is valid on a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked insurance validity"),
        @ApiResponse(responseCode = "404", description = "Car not found"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<InsuranceValidityResponse> isInsuranceValid(
            @Parameter(description = "Car ID", required = true) @PathVariable Long carId,
            @Parameter(description = "Date in YYYY-MM-DD format", required = true) @RequestParam String date) {
        log.info("Checking insurance validity for car: {} on date: {}", carId, date);

        boolean valid = carService.isInsuranceValid(carId, date);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, date, valid));
    }

    @PostMapping("/cars/{carId}/claims")
    @Operation(summary = "Create a new claim", description = "Create a new insurance claim for a specific car")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Claim created successfully"),
        @ApiResponse(responseCode = "404", description = "Car not found"),
        @ApiResponse(responseCode = "400", description = "Invalid claim data")
    })
    public ResponseEntity<ClaimResponseDto> createClaim(
            @Parameter(description = "Car ID", required = true) @PathVariable Long carId,
            @Parameter(description = "Claim details", required = true) @Valid @RequestBody ClaimCreateDto claimCreateDto) {
        log.info("Creating claim for car: {}", carId);

        ClaimResponseDto createdClaim = claimService.createClaim(carId, claimCreateDto);

        URI location = URI.create("/api/cars/" + carId + "/claims/" + createdClaim.id());
        return ResponseEntity.created(location).body(createdClaim);
    }

    @GetMapping("/cars/{carId}/claims")
    public List<ClaimResponseDto> getClaimsByCarId(@PathVariable Long carId) {
        log.info("Fetching claims for car: {}", carId);
        return claimService.getClaimsByCarId(carId);
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<CarHistoryDto> getCarHistory(@PathVariable Long carId) {
        log.info("Fetching history for car: {}", carId);

        CarHistoryDto history = carService.getCarHistory(carId);
        return ResponseEntity.ok(history);
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        var ownerDto = o != null ? new CarDto.OwnerDto(o.getId(), o.getName(), o.getEmail()) : null;
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(), ownerDto);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {
    }
}
