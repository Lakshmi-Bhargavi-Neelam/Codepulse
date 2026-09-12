package com.codepulse.schedulerservice.exception;

/** Thrown when a call from WorkerServiceClient to worker-service fails. */
public class WorkerServiceException extends RuntimeException {

    public WorkerServiceException(String message) {
        super(message);
    }

    public WorkerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
