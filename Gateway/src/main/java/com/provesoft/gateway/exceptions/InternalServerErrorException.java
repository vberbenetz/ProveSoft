package com.provesoft.gateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception is thrown when there is an issue with processing a user request (500).
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException() {
        super("Server Error");
    }

    public InternalServerErrorException(String errorMessage) {
        super(errorMessage);
    }
}
