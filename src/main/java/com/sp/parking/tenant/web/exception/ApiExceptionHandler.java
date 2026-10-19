package com.sp.parking.tenant.web.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Map;

/** Devuelve los errores con un campo "message" legible para el frontend. */
@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> responseStatus(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(body(ex.getStatusCode(), ex.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(error -> error.getDefaultMessage())
      .orElse("Datos inválidos");
    return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> generic(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el servidor"));
  }

  private Map<String, Object> body(HttpStatusCode status, String message) {
    return Map.of(
      "timestamp", Instant.now().toString(),
      "status", status.value(),
      "error", status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Error",
      "message", message == null ? "Error inesperado" : message
    );
  }
}
