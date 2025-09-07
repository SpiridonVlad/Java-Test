package com.example.carins.web;

import com.example.carins.model.Owner;
import com.example.carins.service.OwnerService;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CreateOwnerDto;
import com.example.carins.web.dto.OwnerDto;
import com.example.carins.web.dto.UpdateOwnerDto;
import com.example.carins.web.dto.CarDto;
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
@Tag(name = "Owner Management", description = "APIs for managing car owners")
public class OwnerController {

    private final OwnerService ownerService;
    private final CarService carService;

    @GetMapping("/owners")
    @Operation(summary = "Get all owners", description = "Retrieve a list of all owners in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of owners")
    public List<OwnerDto> getOwners() {
        log.info("Fetching all owners");
        return ownerService.listOwners().stream().map(this::toDto).toList();
    }

    @GetMapping("/owners/{ownerId}")
    @Operation(summary = "Get owner by ID", description = "Retrieve a specific owner by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved owner"),
        @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    public ResponseEntity<OwnerDto> getOwnerById(
            @Parameter(description = "Owner ID", required = true) @PathVariable Long ownerId) {
        log.info("Fetching owner with id: {}", ownerId);
        
        Owner owner = ownerService.getOwnerById(ownerId);
        return ResponseEntity.ok(toDto(owner));
    }

    @PostMapping("/owners")
    @Operation(summary = "Create a new owner", description = "Create a new owner in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Owner created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid owner data or email already exists")
    })
    public ResponseEntity<OwnerDto> createOwner(
            @Parameter(description = "Owner details", required = true) @Valid @RequestBody CreateOwnerDto createOwnerDto) {
        log.info("Creating new owner with email: {}", createOwnerDto.email());
        
        Owner createdOwner = ownerService.createOwner(createOwnerDto);
        OwnerDto ownerDto = toDto(createdOwner);
        
        URI location = URI.create("/api/owners/" + createdOwner.getId());
        return ResponseEntity.created(location).body(ownerDto);
    }

    @PutMapping("/owners/{ownerId}")
    @Operation(summary = "Update an owner", description = "Update an existing owner's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Owner updated successfully"),
        @ApiResponse(responseCode = "404", description = "Owner not found"),
        @ApiResponse(responseCode = "400", description = "Invalid owner data or email already exists")
    })
    public ResponseEntity<OwnerDto> updateOwner(
            @Parameter(description = "Owner ID", required = true) @PathVariable Long ownerId,
            @Parameter(description = "Updated owner details", required = true) @Valid @RequestBody UpdateOwnerDto updateOwnerDto) {
        log.info("Updating owner with id: {}", ownerId);
        
        Owner updatedOwner = ownerService.updateOwner(ownerId, updateOwnerDto);
        return ResponseEntity.ok(toDto(updatedOwner));
    }

    @DeleteMapping("/owners/{ownerId}")
    @Operation(summary = "Delete an owner", description = "Delete an owner from the system. Owner cannot be deleted if they own any cars.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Owner deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Owner not found"),
        @ApiResponse(responseCode = "400", description = "Owner cannot be deleted because they own cars")
    })
    public ResponseEntity<Void> deleteOwner(
            @Parameter(description = "Owner ID", required = true) @PathVariable Long ownerId) {
        log.info("Deleting owner with id: {}", ownerId);
        
        ownerService.deleteOwner(ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owners/{ownerId}/cars")
    @Operation(summary = "Get cars by owner", description = "Retrieve all cars owned by a specific owner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved owner's cars"),
        @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    public ResponseEntity<List<CarDto>> getCarsByOwner(
            @Parameter(description = "Owner ID", required = true) @PathVariable Long ownerId) {
        log.info("Fetching cars for owner with id: {}", ownerId);

        ownerService.getOwnerById(ownerId);
        
        List<CarDto> cars = carService.getCarsByOwnerId(ownerId);
        return ResponseEntity.ok(cars);
    }

    private OwnerDto toDto(Owner owner) {
        return new OwnerDto(owner.getId(), owner.getName(), owner.getEmail());
    }

    private CarDto toCarDto(com.example.carins.model.Car c) {
        var o = c.getOwner();
        var ownerDto = o != null ? new CarDto.OwnerDto(o.getId(), o.getName(), o.getEmail()) : null;
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(), ownerDto);
    }
}
