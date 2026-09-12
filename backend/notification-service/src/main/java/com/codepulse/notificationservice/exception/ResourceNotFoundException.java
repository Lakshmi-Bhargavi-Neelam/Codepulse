package com.codepulse.notificationservice.exception;

/** Thrown when a requested resource (e.g. project delivery history) does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
