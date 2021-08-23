package com.unipi.giguniverse.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionResponse extends ResponseEntityExceptionHandler {

    //TODO: Check BasicErrorController class
    @ExceptionHandler({ApplicationException.class})
    public ResponseEntity<String> handleExceptions(ApplicationException applicationException){
        return new ResponseEntity(applicationException.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
