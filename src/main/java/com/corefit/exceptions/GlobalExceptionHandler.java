package com.corefit.exceptions;

import com.corefit.dto.response.GeneralResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom general exceptions
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<GeneralResponse<?>> handleGeneralException(GeneralException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GeneralResponse<>("Error: " + ex.getMessage()));
    }

    /**
     * Handles entity not found exceptions
     */
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<GeneralResponse<?>> handleEntityNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GeneralResponse<>("Error: " + ex.getMessage()));
    }

    /**
     * Handles validation errors for request body fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GeneralResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new GeneralResponse<>("Validation failed", errors));
    }

    /**
     * Handles validation errors for request parameters and path variables
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GeneralResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(field, violation.getMessage());
        });
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new GeneralResponse<>("Validation failed", errors));
    }

    /**
     * Handles missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GeneralResponse<?>> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GeneralResponse<>("Error: Missing parameter - " + ex.getParameterName()));
    }

    /**
     * Handles type mismatch for request parameters
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GeneralResponse<?>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = ex.getName() + " should be of type " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GeneralResponse<>("Error: " + message));
    }

    /**
     * Handles errors for invalid JSON request bodies
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GeneralResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GeneralResponse<>("Error: Invalid request body format",ex.getMessage()));
    }

    /**
     * Handles authentication exceptions
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<GeneralResponse<?>> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new GeneralResponse<>("Error: " + ex.getMessage()));
    }

    /**
     * Handles access denied exceptions
     */
    @ExceptionHandler({AccessDeniedException.class, java.nio.file.AccessDeniedException.class})
    public ResponseEntity<GeneralResponse<?>> handleAccessDeniedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new GeneralResponse<>("Error: Access denied - " + ex.getMessage()));
    }

    /**
     * Handles database constraint violations (like unique constraints)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GeneralResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message.contains("unique constraint") || message.contains("Duplicate entry")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new GeneralResponse<>("Error: Resource already exists"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GeneralResponse<>("Error: Database constraint violation"));
    }

    /**
     * Handles database access exceptions
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<GeneralResponse<?>> handleDataAccessException(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GeneralResponse<>("Error: Database access error - " + ex.getMessage()));
    }

    /**
     * Handles unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GeneralResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new GeneralResponse<>("Error: " + ex.getMessage()));
    }

    /**
     * Handles unsupported media types
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<GeneralResponse<?>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new GeneralResponse<>("Error: " + ex.getMessage()));
    }

    /**
     * Handles file upload size limit exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GeneralResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new GeneralResponse<>("Error: File size exceeds the maximum allowed limit"));
    }

    /**
     * Handles all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralResponse<?>> handleGenericException(Exception ex) {
        // Log the exception
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GeneralResponse<>("Error: An unexpected error occurred - " + ex.getMessage()));
    }
}