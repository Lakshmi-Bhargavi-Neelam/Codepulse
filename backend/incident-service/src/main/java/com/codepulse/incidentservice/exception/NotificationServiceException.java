package com.codepulse.incidentservice.exception;

/** Thrown when the call from NotificationServiceClient to notification-service fails. */
public class NotificationServiceException extends RuntimeException {

    public NotificationServiceException(String message) {
        super(message);
    }

    public NotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
