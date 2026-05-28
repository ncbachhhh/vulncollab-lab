package com.vulncollab.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "lab.safety")
public record LabSafetyProperties(
        boolean warningBannerRequired,
        boolean accessRestrictionRequired,
        boolean basicAuthRecommended,
        boolean ipAllowlistRecommended,
        boolean vpnRecommended,
        boolean noindexRequired,
        boolean robotsDisallowRequired,
        @NotBlank
        String legalWarning
) {
}
