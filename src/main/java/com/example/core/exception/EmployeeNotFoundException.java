package com.example.core.exception;

public class EmployeeNotFoundException extends RuntimeException {
  public EmployeeNotFoundException(String message) {
    super(message);
  }
}
