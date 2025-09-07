package com.example.carins.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String field, String value) {
        super("User already exists with " + field + ": " + value);
    }
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
