package com.vulncollab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "upload")
public record UploadProperties(
        String dir,
        String maxSize
) {
}
