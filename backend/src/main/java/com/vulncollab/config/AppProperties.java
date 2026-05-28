package com.vulncollab.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        boolean vulnerableMode,
        @NotBlank
        String publicDomain,
        boolean labWarningEnabled,
        boolean challengeSubmitEnabled,
        boolean secureModeBannerEnabled
) {
}
