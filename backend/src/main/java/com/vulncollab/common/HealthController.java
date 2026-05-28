package com.vulncollab.common;

import com.vulncollab.common.api.HealthResponse;
import com.vulncollab.config.AppProperties;
import com.vulncollab.config.DeploymentProperties;
import com.vulncollab.config.LabSafetyProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final AppProperties appProperties;
    private final DeploymentProperties deploymentProperties;
    private final LabSafetyProperties labSafetyProperties;
    private final Environment environment;

    public HealthController(
            AppProperties appProperties,
            DeploymentProperties deploymentProperties,
            LabSafetyProperties labSafetyProperties,
            Environment environment
    ) {
        this.appProperties = appProperties;
        this.deploymentProperties = deploymentProperties;
        this.labSafetyProperties = labSafetyProperties;
        this.environment = environment;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(
                "UP",
                "vulncollab-backend",
                Instant.now(),
                Arrays.asList(environment.getActiveProfiles()),
                deploymentProperties.environment(),
                appProperties.publicDomain(),
                appProperties.vulnerableMode(),
                appProperties.labWarningEnabled(),
                appProperties.challengeSubmitEnabled(),
                appProperties.secureModeBannerEnabled(),
                labSafetyProperties.accessRestrictionRequired(),
                labSafetyProperties.noindexRequired(),
                labSafetyProperties.robotsDisallowRequired()
        );
    }
}
