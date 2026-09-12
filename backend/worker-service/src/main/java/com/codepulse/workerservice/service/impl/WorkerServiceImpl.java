package com.codepulse.workerservice.service.impl;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codepulse.workerservice.client.IncidentServiceClient;
import com.codepulse.workerservice.dto.HealthCheckResponse;
import com.codepulse.workerservice.dto.IncidentRequest;
import com.codepulse.workerservice.dto.MonitorConfig;
import com.codepulse.workerservice.entity.HealthCheckLog;
import com.codepulse.workerservice.entity.MonitorHealth;
import com.codepulse.workerservice.entity.enums.HealthStatus;
import com.codepulse.workerservice.exception.ResourceNotFoundException;
import com.codepulse.workerservice.repository.HealthCheckLogRepository;
import com.codepulse.workerservice.repository.MonitorHealthRepository;
import com.codepulse.workerservice.service.WorkerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes the actual HTTP probe against a monitor's URL, persists the
 * result, and notifies incident-service when the status flips.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService {

    private static final int DEFAULT_TIMEOUT_MS = 10_000;

    private final RestClient.Builder restClientBuilder;
    private final MonitorHealthRepository monitorHealthRepository;
    private final HealthCheckLogRepository healthCheckLogRepository;
    private final IncidentServiceClient incidentServiceClient;

    @Override
    @Transactional
    public HealthCheckResponse executeHealthCheck(MonitorConfig monitor) {
        log.info("Health check received for monitor {} ({})", monitor.getId(), monitor.getName());
        log.info("Checking URL: {} [{}]", monitor.getUrl(), monitor.getHttpMethod());

        Instant start = Instant.now();
        Integer statusCode = null;
        String errorMessage = null;
        HealthStatus status;

        try {
            RestClient probeClient = buildProbeClient(monitor.getTimeoutMs());

            var response = probeClient
                    .method(resolveMethod(monitor.getHttpMethod()))
                    .uri(monitor.getUrl())
                    .retrieve()
                    .toBodilessEntity();

            statusCode = response.getStatusCode().value();
            log.info("Response status code: {}", statusCode);

            status = (monitor.getExpectedStatusCode() != null
                    && statusCode.equals(monitor.getExpectedStatusCode()))
                    || (monitor.getExpectedStatusCode() == null && response.getStatusCode().is2xxSuccessful())
                    ? HealthStatus.UP
                    : HealthStatus.DOWN;

        } catch (RestClientException ex) {
            // Covers connection failures, timeouts, and non-2xx responses raised as exceptions.
            status = HealthStatus.DOWN;
            errorMessage = ex.getMessage();
            log.warn("Health check failed for monitor {}: {}", monitor.getId(), errorMessage);
        }

        long responseTimeMs = Duration.between(start, Instant.now()).toMillis();
        Instant checkedAt = Instant.now();

        log.info("Response time: {} ms | Health status: {}", responseTimeMs, status);

        String previousStatus = persistLatestStatus(monitor, status, statusCode, responseTimeMs, checkedAt, errorMessage);
        persistHistoryEntry(monitor, status, statusCode, responseTimeMs, checkedAt, errorMessage);

        if (previousStatus != null && !previousStatus.equals(status.name())) {
            log.info("Monitor {} changed status: {} -> {}. Notifying incident-service.",
                    monitor.getId(), previousStatus, status);
            notifyIncidentService(monitor, previousStatus, status, responseTimeMs, checkedAt, errorMessage);
        }

        return HealthCheckResponse.builder()
                .monitorId(monitor.getId())
                .status(status)
                .statusCode(statusCode)
                .responseTimeMs(responseTimeMs)
                .checkedAt(checkedAt)
                .errorMessage(errorMessage)
                .build();
    }

    @Override
    public HealthCheckResponse getLatestHealth(UUID monitorId) {
        MonitorHealth latest = monitorHealthRepository.findByMonitorId(monitorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No health data found for monitor " + monitorId));

        return HealthCheckResponse.builder()
                .monitorId(latest.getMonitorId())
                .status(latest.getStatus())
                .statusCode(latest.getStatusCode())
                .responseTimeMs(latest.getResponseTimeMs())
                .checkedAt(latest.getCheckedAt())
                .errorMessage(latest.getErrorMessage())
                .build();
    }

    @Override
    public List<HealthCheckLog> getHealthHistory(UUID monitorId) {
        return healthCheckLogRepository.findTop20ByMonitorIdOrderByCheckedAtDesc(monitorId);
    }

    /**
     * Builds a one-off RestClient for this specific probe, honoring the
     * monitor's own timeoutMs (falling back to a sane default) rather than
     * the shared config timeouts, since every monitor can define its own.
     */
    private RestClient buildProbeClient(Integer timeoutMs) {
        int timeout = timeoutMs != null ? timeoutMs : DEFAULT_TIMEOUT_MS;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(timeout));

        return restClientBuilder.build().mutate().requestFactory(requestFactory).build();
    }

    private HttpMethod resolveMethod(String httpMethod) {
        return HttpMethod.valueOf(httpMethod == null ? "GET" : httpMethod.toUpperCase());
    }

    /**
     * Saves/updates the single MonitorHealth row for this monitor and
     * returns the status it held *before* this update (null if this is the
     * monitor's first-ever check), so the caller can detect a flip.
     */
    private String persistLatestStatus(MonitorConfig monitor, HealthStatus status, Integer statusCode,
                                        long responseTimeMs, Instant checkedAt, String errorMessage) {

        Optional<MonitorHealth> existing = monitorHealthRepository.findByMonitorId(monitor.getId());
        String previousStatus = existing.map(mh -> mh.getStatus().name()).orElse(null);

        MonitorHealth monitorHealth = existing.orElseGet(MonitorHealth::new);
        monitorHealth.setMonitorId(monitor.getId());
        monitorHealth.setProjectId(monitor.getProjectId());
        monitorHealth.setMonitorName(monitor.getName());
        monitorHealth.setUrl(monitor.getUrl());
        monitorHealth.setStatus(status);
        monitorHealth.setStatusCode(statusCode);
        monitorHealth.setResponseTimeMs(responseTimeMs);
        monitorHealth.setCheckedAt(checkedAt);
        monitorHealth.setErrorMessage(errorMessage);

        monitorHealthRepository.save(monitorHealth);
        log.info("Persisted latest status for monitor {}: {}", monitor.getId(), status);

        return previousStatus;
    }

    private void persistHistoryEntry(MonitorConfig monitor, HealthStatus status, Integer statusCode,
                                      long responseTimeMs, Instant checkedAt, String errorMessage) {

        HealthCheckLog logEntry = new HealthCheckLog();
        logEntry.setMonitorId(monitor.getId());
        logEntry.setProjectId(monitor.getProjectId());
        logEntry.setStatus(status);
        logEntry.setStatusCode(statusCode);
        logEntry.setResponseTimeMs(responseTimeMs);
        logEntry.setCheckedAt(checkedAt);
        logEntry.setErrorMessage(errorMessage);

        healthCheckLogRepository.save(logEntry);
        log.info("Recorded health check log entry for monitor {}", monitor.getId());
    }

    private void notifyIncidentService(MonitorConfig monitor, String previousStatus, HealthStatus currentStatus,
                                        long responseTimeMs, Instant checkedAt, String errorMessage) {

        IncidentRequest request = IncidentRequest.builder()
                .monitorId(monitor.getId())
                .projectId(monitor.getProjectId())
                .monitorName(monitor.getName())
                .url(monitor.getUrl())
                .previousStatus(previousStatus)
                .currentStatus(currentStatus.name())
                .responseTimeMs(responseTimeMs)
                .checkedAt(checkedAt)
                .errorMessage(errorMessage)
                .build();

        incidentServiceClient.notifyStatusChange(request);
    }
}
