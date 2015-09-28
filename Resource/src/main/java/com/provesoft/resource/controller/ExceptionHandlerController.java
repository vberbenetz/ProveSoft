package com.provesoft.resource.controller;

import com.provesoft.resource.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(
            value = {
                    Exception.class,
                    RuntimeException.class
            }
    )
    public ResponseEntity handleExceptions(Exception e) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("error", e.getMessage());

        if (e instanceof ResourceNotFoundException) {
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
        if (e instanceof ForbiddenException) {
            return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
        }
        if (e instanceof ConflictException) {
            return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
        }
        if (e instanceof InternalServerErrorException) {
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (e instanceof BadRequestException) {
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
