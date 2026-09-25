package com.dsalearner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorResponse(int status, String error, Object message, Instant timestamp) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                        (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Validation failed", errors, Instant.now()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Conflict", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not found", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(429, "Too Many Requests", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(com.dsalearner.pipeline.exception.InvalidTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
            com.dsalearner.pipeline.exception.InvalidTransitionException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(422, "Invalid state transition", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(com.dsalearner.pipeline.exception.PromptNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePromptNotFound(
            com.dsalearner.pipeline.exception.PromptNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Prompt not found", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(com.dsalearner.pipeline.exception.ModelConfigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleModelConfigNotFound(
            com.dsalearner.pipeline.exception.ModelConfigNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Model config not found", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(com.dsalearner.pipeline.exception.BudgetExceededException.class)
    public ResponseEntity<ErrorResponse> handleBudgetExceeded(
            com.dsalearner.pipeline.exception.BudgetExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(429, "Budget exceeded", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(com.dsalearner.pipeline.exception.DomainNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDomainNotFound(
            com.dsalearner.pipeline.exception.DomainNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Domain not found", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error", ex.getMessage(), Instant.now()));
    }
}
