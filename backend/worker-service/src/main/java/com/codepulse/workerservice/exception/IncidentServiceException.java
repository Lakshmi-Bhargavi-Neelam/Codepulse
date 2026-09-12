package com.codepulse.workerservice.exception;

/** Thrown when the call from IncidentServiceClient to incident-service fails. */
public class IncidentServiceException extends RuntimeException {

    public IncidentServiceException(String message) {
        super(message);
    }

    public IncidentServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
