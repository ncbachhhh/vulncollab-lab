package com.vulncollab.lab;

import com.vulncollab.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class LabModeService {
    private final AppProperties appProperties;

    public LabModeService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean isVulnerableMode() {
        return appProperties.vulnerableMode();
    }
}
