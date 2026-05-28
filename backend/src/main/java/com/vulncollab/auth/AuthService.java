package com.vulncollab.auth;

import com.vulncollab.auth.dto.AuthResponse;
import com.vulncollab.auth.dto.LoginRequest;
import com.vulncollab.auth.dto.RegisterRequest;
import com.vulncollab.auth.dto.UserSummary;
import com.vulncollab.common.error.ConflictException;
import com.vulncollab.common.error.UnauthorizedException;
import com.vulncollab.security.JwtService;
import com.vulncollab.security.UserPrincipal;
import com.vulncollab.user.User;
import com.vulncollab.user.UserRepository;
import com.vulncollab.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("EMAIL_ALREADY_REGISTERED", "Email is already registered");
        }

        User user = new User(
                generatePublicId(),
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                UserRole.defaultRegistrationRole()
        );

        User saved = userRepository.save(user);
        return authResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "Email or password is invalid"));

        return authResponse(user);
    }

    public UserSummary currentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "Authentication is required");
        }

        return UserSummary.from(principal.user());
    }

    private AuthResponse authResponse(User user) {
        Instant expiresAt = jwtService.accessTokenExpiresAt();
        String token = jwtService.createAccessToken(user, expiresAt);
        return new AuthResponse(token, expiresAt, UserSummary.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generatePublicId() {
        return UUID.randomUUID().toString();
    }
}
