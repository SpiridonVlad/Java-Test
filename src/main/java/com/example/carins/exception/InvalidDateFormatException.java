package com.example.carins.exception;

public class InvalidDateFormatException extends RuntimeException {
    public InvalidDateFormatException(String date) {
        super("Invalid date format: " + date + ". Expected format: YYYY-MM-DD");
    }
}
