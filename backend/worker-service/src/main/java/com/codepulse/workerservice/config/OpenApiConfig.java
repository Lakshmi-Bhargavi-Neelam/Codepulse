package com.codepulse.workerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/** Swagger/OpenAPI metadata, served at /v3/api-docs and /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workerServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodePulse Worker Service API")
                        .version("v1"));
    }
}
