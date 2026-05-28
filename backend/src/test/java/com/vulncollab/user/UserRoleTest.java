package com.vulncollab.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {
    @Test
    void registrationDefaultsToUserRole() {
        assertThat(UserRole.defaultRegistrationRole()).isEqualTo(UserRole.USER);
    }
}
