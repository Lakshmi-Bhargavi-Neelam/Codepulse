package com.codepulse.schedulerservice.exception;

/** Thrown when a call from ApiServiceClient to api-service fails. */
public class ApiServiceException extends RuntimeException {

    public ApiServiceException(String message) {
        super(message);
    }

    public ApiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
