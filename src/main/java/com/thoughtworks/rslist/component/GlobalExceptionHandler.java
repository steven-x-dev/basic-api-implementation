package com.thoughtworks.rslist.component;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.exception.Err;
import com.thoughtworks.rslist.exception.RsEventNotValidException;
import com.thoughtworks.rslist.exception.UserNameOccupiedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            RsEventNotValidException.class,
            UserNameOccupiedException.class,
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<Err> handleException(Exception e) {

        String message;

        if (e instanceof RsEventNotValidException) {
            message = e.getMessage();
        } else if (e instanceof UserNameOccupiedException) {
            message = e.getMessage();
        } else if (e instanceof MethodArgumentNotValidException) {
            Object target = ((MethodArgumentNotValidException) e).getBindingResult().getTarget();
            if (target != null && target.getClass() == User.class) {
                message = "invalid user";
            } else {
                message = "invalid param";
            }
        } else {
            message = "unknown error";
        }

        return ResponseEntity.badRequest().body(new Err(message));
    }

}
