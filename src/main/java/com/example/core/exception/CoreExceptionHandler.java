package com.example.core.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Restrict this advice ONLY to the core controller package
@RestControllerAdvice(basePackages = "com.example.core.controller")
public class CoreExceptionHandler {

  // Handles completely missing parameters
  // This is a common issue when the client forgets to include a required query
  // parameter in a GET request.
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMissingParam(MissingServletRequestParameterException ex) {
    Map<String, String> error = new HashMap<>();
    error.put(
        "error",
        String.format(
            "[Core Error Handler] Required query parameter '%s' is missing",
            ex.getParameterName()));
    return error;
  }

  // Handles missing body or broken JSON
  // This is a common issue when the client sends a POST or PUT request without a
  // body or with malformed JSON.
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMissingOrMalformedBody(HttpMessageNotReadableException ex) {
    Map<String, String> error = new HashMap<>();

    // You can check the error message to see if it was completely empty or just
    // malformed
    if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
      error.put(
          "error", "[Core Error Handler] The required request body payload is entirely missing.");
    } else {
      error.put(
          "error",
          "[Core Error Handler] The request body is malformed or contains invalid JSON syntax.");
    }

    return error;
  }

  @ExceptionHandler(EmployeeNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      EmployeeNotFoundException ex, HandlerMethod handlerMethod) {

    // Extract the method name safely
    String methodName =
        (handlerMethod != null) ? handlerMethod.getMethod().getName() : "UnknownMethod";
    // Optional: Extract the controller class name too
    String className =
        (handlerMethod != null) ? handlerMethod.getBeanType().getSimpleName() : "UnknownClass";

    ErrorResponse errorDetails =
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            String.format(
                "[Core Error Handler] Error in %s.%s(): %s",
                className, methodName, ex.getMessage()));

    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
  }

  // Catches @RequestBody validation failures
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleRequestBodyError(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return errors;
  }

  // Catches @RequestParam / @PathVariable validation failures
  // Handles Validation Violations (e.g., -5 passed to a @Min(1) Long)
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleParameterError(ConstraintViolationException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation -> {
              // Extracts the parameter name from the property path
              String paramName = violation.getPropertyPath().toString();
              errors.put(paramName, violation.getMessage());
            });
    return errors;
  }

  // Handles Type Mismatches (e.g., "abc" passed to a Long)
  // That is, bad data types (e.g., text passed instead of a number)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    Map<String, String> error = new HashMap<>();
    error.put(
        "error",
        String.format(
            "[Core Error Handler] Parameter '%s' must be of type %s",
            ex.getName(), ex.getRequiredType().getSimpleName()));
    return error;
  }

  /** Generic handler for unexpected runtime errors. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGeneralException(Exception ex, HandlerMethod handlerMethod) {

    // Extract the method name safely
    String methodName =
        (handlerMethod != null) ? handlerMethod.getMethod().getName() : "UnknownMethod";
    // Optional: Extract the controller class name too
    String className =
        (handlerMethod != null) ? handlerMethod.getBeanType().getSimpleName() : "UnknownClass";

    ErrorResponse errorDetails =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getClass().getSimpleName(),
            String.format(
                "[Core Error Handler] Error in %s.%s(): %s ",
                className, methodName, ex.getMessage()));

    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
