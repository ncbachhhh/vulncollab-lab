package com.vulncollab.common;

import com.vulncollab.config.AppProperties;
import com.vulncollab.config.DeploymentProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final AppProperties appProperties;
    private final DeploymentProperties deploymentProperties;
    private final Environment environment;

    public HealthController(
            AppProperties appProperties,
            DeploymentProperties deploymentProperties,
            Environment environment
    ) {
        this.appProperties = appProperties;
        this.deploymentProperties = deploymentProperties;
        this.environment = environment;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "vulncollab-backend",
                "timestamp", Instant.now().toString(),
                "profiles", Arrays.asList(environment.getActiveProfiles()),
                "environment", deploymentProperties.environment(),
                "vulnerableMode", appProperties.vulnerableMode(),
                "labWarningEnabled", appProperties.labWarningEnabled(),
                "challengeSubmitEnabled", appProperties.challengeSubmitEnabled()
        );
    }
}
