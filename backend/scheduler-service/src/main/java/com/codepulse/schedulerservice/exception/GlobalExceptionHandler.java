package com.codepulse.schedulerservice.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.codepulse.schedulerservice.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Translates exceptions raised anywhere in scheduler-service into a
 * consistent JSON error body: timestamp, status, error, message, path.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiServiceException.class)
    public ResponseEntity<ErrorResponse> handleApiServiceException(ApiServiceException ex, WebRequest request) {
        log.error("api-service call failed: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(WorkerServiceException.class)
    public ResponseEntity<ErrorResponse> handleWorkerServiceException(WorkerServiceException ex, WebRequest request) {
        log.error("worker-service call failed: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
