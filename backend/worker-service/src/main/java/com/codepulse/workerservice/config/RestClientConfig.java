package com.codepulse.workerservice.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Provides a shared RestClient.Builder pre-configured with connect/read
 * timeouts from application.yml (rest-client.connect-timeout / read-timeout).
 *
 * IncidentServiceClient injects this builder and applies its own baseUrl
 * (incident-service.base-url) on top of it. The outbound probe against a
 * monitor's own URL uses these same default timeouts unless the monitor's
 * MonitorConfig.timeoutMs overrides them for that single check (see
 * WorkerServiceImpl).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder(
            @Value("${rest-client.connect-timeout}") long connectTimeoutMs,
            @Value("${rest-client.read-timeout}") long readTimeoutMs) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder().requestFactory(requestFactory);
    }
}
