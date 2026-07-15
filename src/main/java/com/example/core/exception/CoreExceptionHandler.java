package com.example.core.exception;

import com.example.core.interceptor.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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

  private static final String ERROR_MSG_PREFIX = "[Core Error Handler]";

  // Handles completely missing parameters
  // This is a common issue when the client forgets to include a required query
  // parameter in a GET request.
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    errors.put(
        "error",
        String.format(
            "%s Required query parameter '%s' is missing",
            ERROR_MSG_PREFIX, ex.getParameterName()));

    if (RequestContext.isDetailedError()) {
      return addErrorDetails(errors, ex, request);
    }
    return errors;
  }

  // Handles missing body or broken JSON
  // This is a common issue when the client sends a POST or PUT request without a
  // body or with malformed JSON.
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMissingOrMalformedBody(
      HttpMessageNotReadableException ex, HttpServletRequest request) throws IOException {
    Map<String, String> errors = new HashMap<>();

    // You can check the error message to see if it was completely empty or just
    // malformed
    if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
      errors.put(
          "error",
          String.format(
              "%s The required request body payload is entirely missing.", ERROR_MSG_PREFIX));
    } else {
      errors.put(
          "error",
          String.format(
              "%s The request body is malformed or contains invalid JSON syntax.",
              ERROR_MSG_PREFIX));
    }

    if (RequestContext.isDetailedError()) {
      return addErrorDetails(errors, ex, request);
    }
    return errors;
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
                "%s Error in %s.%s(): %s",
                ERROR_MSG_PREFIX, className, methodName, ex.getMessage()));

    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
  }

  // Catches @RequestBody validation failures
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleRequestBodyError(
      MethodArgumentNotValidException ex, HttpServletRequest request) throws IOException {
    Map<String, String> errors = new HashMap<>();
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

    errors.put(
        "error",
        String.format("%s Validation failed for fields: %s", ERROR_MSG_PREFIX, fieldErrors));

    if (RequestContext.isDetailedError()) {
      errors.put("fieldErrors", fieldErrors.toString());
      return addErrorDetails(errors, ex, request);
    }
    return errors;
  }

  // Catches @RequestParam / @PathVariable validation failures
  // Handles Validation Violations (e.g., -5 passed to a @Min(1) Long)
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleParameterError(
      ConstraintViolationException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    Map<String, String> paramErrors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation -> {
              // Extracts the parameter name from the property path
              String paramName = violation.getPropertyPath().toString();
              paramErrors.put(paramName, violation.getMessage());
            });

    errors.put(
        "error",
        String.format("%s Validation failed for parameters: %s", ERROR_MSG_PREFIX, paramErrors));

    if (RequestContext.isDetailedError()) {
      errors.put("validationErrors", paramErrors.toString());
      return addErrorDetails(errors, ex, request);
    }
    return errors;
  }

  // Handles Type Mismatches (e.g., "abc" passed to a Long)
  // That is, bad data types (e.g., text passed instead of a number)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    errors.put(
        "error",
        String.format(
            "%s Parameter '%s' must be of type %s",
            ERROR_MSG_PREFIX, ex.getName(), ex.getRequiredType().getSimpleName()));

    if (RequestContext.isDetailedError()) {
      return addErrorDetails(errors, ex, request);
    }

    return errors;
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Map<String, String> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", String.format("%s Access denied: %s", ERROR_MSG_PREFIX, ex.getMessage()));

    if (RequestContext.isDetailedError()) {
      return addErrorDetails(errors, ex, request);
    }
    return errors;
  }

  /** Generic handler for unexpected runtime errors. */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, String> handleGeneralException(
      Exception ex, HandlerMethod handlerMethod, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    // Extract the method name safely
    String methodName =
        (handlerMethod != null) ? handlerMethod.getMethod().getName() : "UnknownMethod";
    // Optional: Extract the controller class name too
    String className =
        (handlerMethod != null) ? handlerMethod.getBeanType().getSimpleName() : "UnknownClass";
    errors.put(
        "error",
        String.format(
            "%s An unexpected error occurred in %s.%s(): %s",
            ERROR_MSG_PREFIX, className, methodName, ex.getMessage()));

    if (RequestContext.isDetailedError()) {
      return addErrorDetails(errors, ex, request, true);
    }

    return errors;
  }

  private String getCustomStackTrace(Exception ex) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    return sw.toString();
  }

  private String getPayload(HttpServletRequest request) throws IOException {
    try (BufferedReader reader = request.getReader()) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  private String flattenParameterMap(Map<String, String[]> paramMap) {
    return paramMap.entrySet().stream()
        .map(entry -> entry.getKey() + "=[" + String.join(", ", entry.getValue()) + "]")
        .collect(Collectors.joining(", ", "{", "}"));
  }

  private Map<String, String> addErrorDetails(
      Map<String, String> details, Exception ex, HttpServletRequest request) {
    return addErrorDetails(details, ex, request, false);
  }

  private Map<String, String> addErrorDetails(
      Map<String, String> details,
      Exception ex,
      HttpServletRequest request,
      boolean withStackTrace) {
    details.put("exception", ex.getClass().getSimpleName());
    details.put(
        "status",
        String.format(
            "%d (%s)", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()));
    details.put("message", ex.getMessage());

    details.put("uri", request.getRequestURI());
    details.put("url", request.getRequestURL().toString());
    details.put("queryString", request.getQueryString());
    details.put("method", request.getMethod());
    details.put("remoteAddr", request.getRemoteAddr());
    details.put("remoteHost", request.getRemoteHost());
    details.put("remotePort", String.valueOf(request.getRemotePort()));
    details.put("localAddr", request.getLocalAddr());
    details.put("localName", request.getLocalName());
    details.put("localPort", String.valueOf(request.getLocalPort()));
    details.put("userAgent", request.getHeader("User-Agent"));
    details.put("parameterMap", flattenParameterMap(request.getParameterMap()));
    try {
      details.put(
          "body",
          request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual));
      details.put("payload", getPayload(request));
    } catch (IOException e) {
    }

    if (withStackTrace) {
      details.put("stackTrace", getCustomStackTrace(ex));
    }

    return new TreeMap<>(details);
  }
}
