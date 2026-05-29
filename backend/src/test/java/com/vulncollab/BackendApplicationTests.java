package com.vulncollab;

import com.vulncollab.user.UserRepository;
import com.vulncollab.auth.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class BackendApplicationTests {
	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RefreshTokenRepository refreshTokenRepository;

	@Test
	void contextLoads() {
	}

}
