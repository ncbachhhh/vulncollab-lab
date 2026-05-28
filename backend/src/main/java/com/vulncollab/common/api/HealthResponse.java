package com.vulncollab.common.api;

import java.time.Instant;
import java.util.List;

public record HealthResponse(
        String status,
        String service,
        Instant timestamp,
        List<String> profiles,
        String environment,
        String publicDomain,
        boolean vulnerableMode,
        boolean labWarningEnabled,
        boolean challengeSubmitEnabled,
        boolean secureModeBannerEnabled,
        boolean labAccessRestrictionRequired,
        boolean labNoindexRequired,
        boolean labRobotsDisallowRequired
) {
}
