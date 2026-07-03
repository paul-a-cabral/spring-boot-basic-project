package com.example.core.data;

/**
 * Checked exception for employee-related errors.
 */
public class MyEmployeeException extends Exception {
    private static final long serialVersionUID = 1L;

    public MyEmployeeException() {
        super();
    }

    public MyEmployeeException(String message) {
        super(message);
    }

    public MyEmployeeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyEmployeeException(Throwable cause) {
        super(cause);
    }
}
