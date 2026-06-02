package com.foodly.user.service;

import com.foodly.common.exception.ConflictException;
import com.foodly.common.exception.UnauthorizedException;
import com.foodly.user.domain.Role;
import com.foodly.user.domain.User;
import com.foodly.user.dto.LoginRequest;
import com.foodly.user.dto.SignupRequest;
import com.foodly.user.dto.TokenPair;
import com.foodly.user.dto.UserResponse;
import com.foodly.user.repository.UserRepository;
import com.foodly.user.security.JwtIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw ConflictException.duplicate("User", "email", email);
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(request.role() == null ? Role.CUSTOMER : request.role())
                .enabled(true)
                .build();
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public TokenPair login(LoginRequest request) {
        User user = users.findByEmail(normalize(request.email()))
                .orElseThrow(UnauthorizedException::invalidCredentials);
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw UnauthorizedException.invalidCredentials();
        }
        return jwtIssuer.issue(user);
    }

    @Transactional(readOnly = true)
    public TokenPair refresh(String refreshToken) {
        UUID userId = UUID.fromString(jwtIssuer.parseRefreshSubject(refreshToken));
        User user = users.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new UnauthorizedException("Account no longer active"));
        return jwtIssuer.issue(user);
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
