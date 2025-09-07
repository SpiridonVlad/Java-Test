package com.example.carins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "car")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 5, max = 32)
    private String vin;

    private String make;
    private String model;

    @Column(name = "year_of_manufacture")
    private int yearOfManufacture;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cars"})
    private Owner owner;

    public Car(String vin, String make, String model, int yearOfManufacture, Owner owner) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.yearOfManufacture = yearOfManufacture;
        this.owner = owner;
    }
}
