package com.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class VersionConflictException extends RuntimeException {
    public VersionConflictException(long serverVersion) {
        super("Version conflict. Server version: " + serverVersion);
    }
}
