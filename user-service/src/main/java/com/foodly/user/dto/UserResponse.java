package com.foodly.user.dto;

import com.foodly.user.domain.Role;
import com.foodly.user.domain.User;

import java.time.Instant;
import java.util.UUID;

/** Public projection of a {@link User} — never exposes the password hash. */
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Role role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
