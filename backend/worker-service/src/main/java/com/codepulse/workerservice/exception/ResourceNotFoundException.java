package com.codepulse.workerservice.exception;

/** Thrown when a requested monitor's health data does not exist yet. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
