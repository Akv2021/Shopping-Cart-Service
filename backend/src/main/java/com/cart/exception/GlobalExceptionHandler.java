package com.cart.exception;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(CartException ex) {
        log.error("Cart error: {}", ex.getMessage());
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage()),
            ex.getStatus()
        );
    }

    @ExceptionHandler(VersionConflictException.class)
    public ResponseEntity<ErrorResponse> handleVersionConflict(VersionConflictException ex) {
        log.error("Version conflict: {}", ex.getMessage());
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage()),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return new ResponseEntity<>(
            new ErrorResponse(ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return new ResponseEntity<>(
            new ErrorResponse("An unexpected error occurred"),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private String status = "error";
        private LocalDateTime timestamp = LocalDateTime.now();

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
