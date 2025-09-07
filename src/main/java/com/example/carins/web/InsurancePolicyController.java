package com.example.carins.web;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.InsurancePolicyService;
import com.example.carins.web.dto.InsurancePolicyCreateDto;
import com.example.carins.web.dto.InsurancePolicyUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/policies")
@Tag(name = "Insurance Policy Management", description = "APIs for managing insurance policies")
public class InsurancePolicyController {


    private final InsurancePolicyService policyService;

    @Operation(
            summary = "Create a new insurance policy",
            description = "Creates a new insurance policy for a car. The policy must have an end date.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Insurance policy creation data",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicyCreateDto.class),
                            examples = @ExampleObject(
                                    name = "Valid Policy",
                                    value = """
                                            {
                                              "carId": 1,
                                              "provider": "State Farm",
                                              "startDate": "2024-01-01",
                                              "endDate": "2025-01-01"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Insurance policy created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicy.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - missing required fields or invalid data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing End Date",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "Validation Error",
                                                      "message": "Validation failed: {endDate=must not be null}",
                                                      "timestamp": "2024-01-01T10:00:00"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Car not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Car Not Found",
                                    value = """
                                            {
                                              "status": 404,
                                              "error": "Resource Not Found",
                                              "message": "Car not found with id: 999",
                                              "timestamp": "2024-01-01T10:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<InsurancePolicy> createPolicy(@Valid @RequestBody InsurancePolicyCreateDto dto) {
        log.info("Creating insurance policy for car: {}", dto.carId());

        InsurancePolicy policy = policyService.createPolicy(dto);

        URI location = URI.create("/api/policies/" + policy.getId());
        return ResponseEntity.created(location).body(policy);
    }

    @Operation(
            summary = "Get insurance policy by ID",
            description = "Retrieves a specific insurance policy by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Insurance policy found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicy.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Insurance policy not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Policy Not Found",
                                    value = """
                                            {
                                              "status": 404,
                                              "error": "Resource Not Found",
                                              "message": "Insurance policy not found with id: 999",
                                              "timestamp": "2024-01-01T10:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<InsurancePolicy> getPolicy(
            @Parameter(description = "Insurance policy ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Retrieving insurance policy with id: {}", id);

        InsurancePolicy policy = policyService.getPolicy(id);
        return ResponseEntity.ok(policy);
    }

    @Operation(
            summary = "Get all insurance policies",
            description = "Retrieves a list of all insurance policies in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of insurance policies retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicy.class, type = "array")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<InsurancePolicy>> getAllPolicies() {
        log.info("Retrieving all insurance policies");

        List<InsurancePolicy> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    @Operation(
            summary = "Get insurance policies by car ID",
            description = "Retrieves all insurance policies associated with a specific car"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of insurance policies for the car retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicy.class, type = "array")
                    )
            )
    })
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<InsurancePolicy>> getPoliciesByCarId(
            @Parameter(description = "Car ID to filter policies", required = true, example = "1")
            @PathVariable Long carId) {
        log.info("Retrieving insurance policies for car: {}", carId);

        List<InsurancePolicy> policies = policyService.getPoliciesByCarId(carId);
        return ResponseEntity.ok(policies);
    }

    @Operation(
            summary = "Update an insurance policy",
            description = "Updates an existing insurance policy. All fields in the request body are required, including endDate.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Insurance policy update data",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicyUpdateDto.class),
                            examples = @ExampleObject(
                                    name = "Valid Update",
                                    value = """
                                            {
                                              "carId": 1,
                                              "provider": "Updated Insurance Co.",
                                              "startDate": "2024-02-01",
                                              "endDate": "2025-02-01"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Insurance policy updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InsurancePolicy.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - missing required fields or invalid data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing End Date",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "Validation Error",
                                                      "message": "Validation failed: {endDate=must not be null}",
                                                      "timestamp": "2024-01-01T10:00:00"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Insurance policy or associated car not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Policy Not Found",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "Resource Not Found",
                                                      "message": "Insurance policy not found with id: 999",
                                                      "timestamp": "2024-01-01T10:00:00"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Car Not Found",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "Resource Not Found",
                                                      "message": "Car not found with id: 999",
                                                      "timestamp": "2024-01-01T10:00:00"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<InsurancePolicy> updatePolicy(
            @Parameter(description = "Insurance policy ID to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody InsurancePolicyUpdateDto dto) {
        log.info("Updating insurance policy with id: {}", id);

        InsurancePolicy updatedPolicy = policyService.updatePolicy(id, dto);
        return ResponseEntity.ok(updatedPolicy);
    }

    @Operation(
            summary = "Delete an insurance policy",
            description = "Permanently deletes an insurance policy from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Insurance policy deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Insurance policy not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Policy Not Found",
                                    value = """
                                            {
                                              "status": 404,
                                              "error": "Resource Not Found",
                                              "message": "Insurance policy not found with id: 999",
                                              "timestamp": "2024-01-01T10:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(
            @Parameter(description = "Insurance policy ID to delete", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Deleting insurance policy with id: {}", id);

        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Fix open-ended policies",
            description = "Fixes insurance policies that don't have an end date by setting a default end date (1 year from start date)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Open-ended policies fixed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "message": "Open-ended policies have been fixed"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/fix-open-ended")
    public ResponseEntity<Map<String, String>> fixOpenEndedPolicies() {
        log.info("Fixing open-ended policies");

        policyService.fixOpenEndedPolicies();

        Map<String, String> response = Map.of("message", "Open-ended policies have been fixed");
        return ResponseEntity.ok(response);
    }
}
