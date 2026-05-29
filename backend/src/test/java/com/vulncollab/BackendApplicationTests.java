package com.vulncollab;

import com.vulncollab.user.UserRepository;
import com.vulncollab.auth.RefreshTokenRepository;
import com.vulncollab.workspace.WorkspaceMemberRepository;
import com.vulncollab.workspace.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class BackendApplicationTests {
	@Autowired
	private CorsConfigurationSource corsConfigurationSource;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RefreshTokenRepository refreshTokenRepository;

	@MockBean
	private WorkspaceRepository workspaceRepository;

	@MockBean
	private WorkspaceMemberRepository workspaceMemberRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void corsAllowsLocalFrontendOriginForApiRoutes() {
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/health");
		request.addHeader("Origin", "http://localhost:5173");
		request.addHeader("Access-Control-Request-Method", "GET");

		var configuration = corsConfigurationSource.getCorsConfiguration(request);

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins()).contains("http://localhost:5173");
		assertThat(configuration.getAllowedHeaders()).contains("Authorization", "Content-Type");
		assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "PATCH", "OPTIONS");
	}

}
