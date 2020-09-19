package com.thoughtworks.rslist.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ResourceExistsException.class,
            UserNotValidException.class,
            RsEventNotValidException.class,
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<Error> handleNotValidException(Exception e) {

        String message;

        if (e instanceof MethodArgumentNotValidException) {
            message = "invalid param";
        } else {
            message = e.getMessage();
        }

        return ResponseEntity.badRequest().body(new Error(message));
    }

    @ExceptionHandler({ ResourceNotExistsException.class })
    public ResponseEntity<Error> handleResourceNotExistsException(ResourceNotExistsException e) {
        return ResponseEntity.notFound().header("message", e.getMessage()).build();
    }

}
