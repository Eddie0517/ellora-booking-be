package com.ellora.domain.exception;

/**
 * Exception thrown when a domain business rule invariant is violated.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
