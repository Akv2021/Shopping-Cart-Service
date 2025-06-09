package com.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CartException extends RuntimeException {
    private final HttpStatus status;

    public CartException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public CartException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
