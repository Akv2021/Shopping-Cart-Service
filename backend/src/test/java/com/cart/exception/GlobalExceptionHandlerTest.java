package com.cart.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleCartException() {
        CartException ex = new CartException("Cart error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleCartException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cart error", response.getBody().getMessage());
        assertEquals("error", response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleVersionConflict() {
        CartException ex = new CartException("Version conflict", HttpStatus.CONFLICT);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleCartException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Version conflict", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleGenericException() {
        RuntimeException ex = new RuntimeException("Unexpected error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void errorResponseShouldHaveTimestamp() {
        CartException ex = new CartException("Test error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleCartException(ex);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timestamp = response.getBody().getTimestamp();

        assertNotNull(timestamp);
        assertTrue(timestamp.isEqual(now) || timestamp.isBefore(now));
    }
}
