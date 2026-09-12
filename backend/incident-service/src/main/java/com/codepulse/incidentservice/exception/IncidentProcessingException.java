package com.codepulse.incidentservice.exception;

/** Thrown when the incident state machine encounters an invalid or unprocessable status change. */
public class IncidentProcessingException extends RuntimeException {

    public IncidentProcessingException(String message) {
        super(message);
    }

    public IncidentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
