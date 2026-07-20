package com.example.ToyProject_Board.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        HttpStatus status = switch (e.getCode()) {
            case "SIGNUP_PENDING", "SIGNUP_REJECTED" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.UNAUTHORIZED;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(e.getCode(), e.getMessage()));
    }
}
