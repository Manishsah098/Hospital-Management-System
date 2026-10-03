package com.smartcare.exception;

/**
 * Base custom runtime exception for SmartCare system errors.
 */
public class SmartCareException extends RuntimeException {
    public SmartCareException(String message) {
        super(message);
    }

    public SmartCareException(String message, Throwable cause) {
        super(message, cause);
    }
}
