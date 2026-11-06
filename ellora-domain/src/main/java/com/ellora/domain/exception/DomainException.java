package com.ellora.domain.exception;

/**
 * Base exception for all domain-specific validation and business rule failures.
 * Pure Java, zero framework dependencies.
 */
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message) {
        this(null, message);
    }

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
