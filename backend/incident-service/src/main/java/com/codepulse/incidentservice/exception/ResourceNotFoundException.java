package com.codepulse.incidentservice.exception;

/** Thrown when a requested incident does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
