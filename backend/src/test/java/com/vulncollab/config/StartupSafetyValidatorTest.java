package com.vulncollab.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StartupSafetyValidatorTest {
    private static final LabSafetyProperties LAB_SAFETY = new LabSafetyProperties(
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            "Authorized lab use only."
    );

    @Test
    void allowsSecureProductionStartup() {
        StartupSafetyValidator validator = new StartupSafetyValidator(
                app(false),
                deployment("prod", false),
                LAB_SAFETY
        );

        assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments()));
    }

    @Test
    void rejectsVulnerableModeInProduction() {
        StartupSafetyValidator validator = new StartupSafetyValidator(
                app(true),
                deployment("prod", false),
                LAB_SAFETY
        );

        assertThrows(IllegalStateException.class, () -> validator.run(new DefaultApplicationArguments()));
    }

    @Test
    void rejectsExposedVulnerableLabInProduction() {
        StartupSafetyValidator validator = new StartupSafetyValidator(
                app(false),
                deployment("prod", true),
                LAB_SAFETY
        );

        assertThrows(IllegalStateException.class, () -> validator.run(new DefaultApplicationArguments()));
    }

    private static AppProperties app(boolean vulnerableMode) {
        return new AppProperties(
                vulnerableMode,
                "lab.example.com",
                true,
                true,
                true
        );
    }

    private static DeploymentProperties deployment(String environment, boolean exposeVulnerableLab) {
        return new DeploymentProperties(environment, exposeVulnerableLab);
    }
}
