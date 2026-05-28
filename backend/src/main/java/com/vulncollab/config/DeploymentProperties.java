package com.vulncollab.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "deployment")
public record DeploymentProperties(
        @NotBlank
        String environment,
        boolean exposeVulnerableLab
) {
}
