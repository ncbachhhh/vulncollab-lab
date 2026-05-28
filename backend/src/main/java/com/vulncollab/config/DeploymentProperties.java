package com.vulncollab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deployment")
public record DeploymentProperties(
        String environment,
        boolean exposeVulnerableLab
) {
}
