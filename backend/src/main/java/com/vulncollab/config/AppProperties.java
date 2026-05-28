package com.vulncollab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        boolean vulnerableMode,
        String publicDomain,
        boolean labWarningEnabled,
        boolean challengeSubmitEnabled,
        boolean secureModeBannerEnabled
) {
}
