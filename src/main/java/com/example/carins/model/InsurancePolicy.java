package com.example.carins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "insurancepolicy")
@Schema(description = "Insurance policy entity representing a car insurance policy")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InsurancePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the insurance policy", example = "1")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    @Schema(description = "The car associated with this insurance policy")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "policies"})
    private Car car;

    @Schema(description = "Insurance provider company name", example = "State Farm")
    private String provider;

    @NotNull
    @Column(name = "start_date")
    @Schema(description = "Policy start date", example = "2024-01-01")
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date")
    @Schema(description = "Policy end date", example = "2025-01-01")
    private LocalDate endDate;

    public InsurancePolicy(Car car, String provider, LocalDate startDate, LocalDate endDate) {
        this.car = car;
        this.provider = provider;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
