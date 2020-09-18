package com.thoughtworks.rslist.exception;

import com.thoughtworks.rslist.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotValidException.class,
            RsEventNotValidException.class,
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<Error> handleException(Exception e) {

        String message;

        if (e instanceof MethodArgumentNotValidException) {
            message = "invalid param";
        } else {
            message = e.getMessage();
        }

        return ResponseEntity.badRequest().body(new Error(message));
    }

}
