
package com.workboard.workboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolations(
            ConstraintViolationException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getConstraintViolations().forEach(violation ->
            errors.put("error", violation.getMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }
    
    @org.springframework.web.bind.annotation.ExceptionHandler(
            IllegalArgumentException.class
    )
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>>
    handleIllegalArgumentException(IllegalArgumentException ex) {

        return org.springframework.http.ResponseEntity
                .status(org.springframework.http.HttpStatus.CONFLICT)
                .body(java.util.Map.of("error", ex.getMessage()));
    }

}
