package com.example.carins.exception;

public class CarNotFoundException extends RuntimeException {
    public CarNotFoundException(Long carId) {
        super("Car not found with id: " + carId);
    }
}
