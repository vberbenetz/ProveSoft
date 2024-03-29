package com.provesoft.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException() { super("Bad Request"); }

    public BadRequestException(String errorMessage) {
        super(errorMessage);
    }
}
