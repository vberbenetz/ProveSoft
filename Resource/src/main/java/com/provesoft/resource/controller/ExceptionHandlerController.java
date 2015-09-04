package com.provesoft.resource.controller;

import com.provesoft.resource.exceptions.ForbiddenException;
import com.provesoft.resource.exceptions.InternalServerErrorException;
import com.provesoft.resource.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(
            value = {
                    Exception.class,
                    RuntimeException.class
            }
    )
    public ResponseEntity handleExceptions(Exception e) {
        if (e instanceof ResourceNotFoundException) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (e instanceof ForbiddenException) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        if (e instanceof InternalServerErrorException) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
