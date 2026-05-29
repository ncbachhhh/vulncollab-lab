package com.vulncollab.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        @NotNull
        boolean enabled,
        @NotEmpty
        List<String> allowedOrigins,
        @NotEmpty
        List<String> allowedMethods,
        @NotEmpty
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        long maxAgeSeconds
) {
}
