package com.vulncollab.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupSafetyValidator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupSafetyValidator.class);

    private final AppProperties appProperties;
    private final DeploymentProperties deploymentProperties;
    private final LabSafetyProperties labSafetyProperties;

    public StartupSafetyValidator(
            AppProperties appProperties,
            DeploymentProperties deploymentProperties,
            LabSafetyProperties labSafetyProperties
    ) {
        this.appProperties = appProperties;
        this.deploymentProperties = deploymentProperties;
        this.labSafetyProperties = labSafetyProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String environment = deploymentProperties.environment();
        boolean prod = "prod".equalsIgnoreCase(environment);

        if (prod && appProperties.vulnerableMode()) {
            throw new IllegalStateException("Refusing to start prod with app.vulnerable-mode=true");
        }

        if (prod && deploymentProperties.exposeVulnerableLab()) {
            throw new IllegalStateException("Refusing to start prod with deployment.expose-vulnerable-lab=true");
        }

        if (appProperties.vulnerableMode() && labSafetyProperties.accessRestrictionRequired()) {
            log.warn("Vulnerable mode is enabled. Ensure lab access restriction is enforced before public exposure.");
        }

        log.info(
                "VulnCollab startup safety: environment={}, vulnerableMode={}, exposeVulnerableLab={}, labWarningEnabled={}",
                environment,
                appProperties.vulnerableMode(),
                deploymentProperties.exposeVulnerableLab(),
                appProperties.labWarningEnabled()
        );
    }
}
