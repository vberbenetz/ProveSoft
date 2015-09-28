package com.provesoft.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception is thrown when a user passes in the wrong parameters for a method (404).
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Path does not exist");
    }

    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
