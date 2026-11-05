package com.ellora.domain.exception;

/**
 * Base exception for all domain-specific validation and business rule failures.
 * Pure Java, zero framework dependencies.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
