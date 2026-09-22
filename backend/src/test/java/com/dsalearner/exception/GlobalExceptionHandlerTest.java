package com.dsalearner.exception;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler h=new GlobalExceptionHandler();

  @Test void handlesValidation() {
    var ex=mock(MethodArgumentNotValidException.class); var br=mock(BindingResult.class); var f=mock(FieldError.class);
    when(ex.getBindingResult()).thenReturn(br); when(br.getFieldErrors()).thenReturn(List.of(f));
    when(f.getField()).thenReturn("email"); when(f.getDefaultMessage()).thenReturn("required");
    var r=h.handleValidation(ex); assertEquals(400,r.getStatusCode().value()); assertEquals("Validation failed",r.getBody().error());
  }
  @Test void handlesKnownExceptions() {
    assertEquals(401,h.handleBadCredentials(new BadCredentialsException("bad")).getStatusCode().value());
    assertEquals(401,h.handleUnauthorized(new UnauthorizedException("bad")).getStatusCode().value());
    assertEquals(409,h.handleConflict(new ConflictException("bad")).getStatusCode().value());
    assertEquals(404,h.handleNotFound(new NotFoundException("bad")).getStatusCode().value());
    assertEquals(429,h.handleTooManyRequests(new TooManyRequestsException("bad")).getStatusCode().value());
    assertEquals(500,h.handleGeneric(new RuntimeException("bad")).getStatusCode().value());
  }
}
