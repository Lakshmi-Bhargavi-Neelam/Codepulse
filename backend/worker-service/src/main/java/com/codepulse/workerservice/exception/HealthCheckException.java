package com.codepulse.workerservice.exception;

/** Thrown when executing the outbound HTTP probe against a monitor's URL fails unrecoverably. */
public class HealthCheckException extends RuntimeException {

    public HealthCheckException(String message) {
        super(message);
    }

    public HealthCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
