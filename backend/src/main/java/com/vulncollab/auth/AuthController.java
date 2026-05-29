package com.vulncollab.auth;

import com.vulncollab.auth.dto.AuthResponse;
import com.vulncollab.auth.dto.LoginRequest;
import com.vulncollab.auth.dto.LogoutRequest;
import com.vulncollab.auth.dto.LogoutResponse;
import com.vulncollab.auth.dto.RefreshRequest;
import com.vulncollab.auth.dto.RegisterRequest;
import com.vulncollab.auth.dto.UserSummary;
import com.vulncollab.common.api.ApiResponse;
import com.vulncollab.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.register(request, clientIp(servletRequest), userAgent(servletRequest)));
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.login(request, clientIp(servletRequest), userAgent(servletRequest)));
    }

    @PostMapping("/refresh")
    ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.refresh(request, clientIp(servletRequest), userAgent(servletRequest)));
    }

    @PostMapping("/logout")
    ApiResponse<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.success(new LogoutResponse(true));
    }

    @GetMapping("/me")
    ApiResponse<UserSummary> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(authService.currentUser(principal));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Nginx will provide the original client first when the app runs behind a proxy.
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
