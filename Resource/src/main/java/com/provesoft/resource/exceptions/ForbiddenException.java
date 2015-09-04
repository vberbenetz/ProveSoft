package com.provesoft.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception is when User attempts to access a route they are not allowed to (403).
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("Access Denied");
    }
}
