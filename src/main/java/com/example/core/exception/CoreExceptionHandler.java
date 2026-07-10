package com.example.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

// Restrict this advice ONLY to the core controller package
@RestControllerAdvice(basePackages = "com.example.core.controller")
public class CoreExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
            HandlerMethod handlerMethod) {

        // Extract the method name safely
        String methodName = (handlerMethod != null) ? handlerMethod.getMethod().getName() : "UnknownMethod";
        // Optional: Extract the controller class name too
        String className = (handlerMethod != null) ? handlerMethod.getBeanType().getSimpleName() : "UnknownClass";

        ErrorResponse errorDetails = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                String.format("Error in %s.%s(): %s", className, methodName, ex.getMessage()));

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Generic handler for unexpected runtime errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, HandlerMethod handlerMethod) {

        // Extract the method name safely
        String methodName = (handlerMethod != null) ? handlerMethod.getMethod().getName() : "UnknownMethod";
        // Optional: Extract the controller class name too
        String className = (handlerMethod != null) ? handlerMethod.getBeanType().getSimpleName() : "UnknownClass";

        ErrorResponse errorDetails = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getClass().getSimpleName(),
                String.format("Error in %s.%s(): %s ", className, methodName, ex.getMessage()));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
