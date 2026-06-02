package com.foodly.user.dto;

import com.foodly.user.domain.User;

import java.util.UUID;

/**
 * Minimal cross-service view of a user — what order/delivery services need to label
 * a customer without pulling the full profile. Kept in user-service for now; promote
 * to common-lib once a second service consumes it.
 */
public record UserSummary(
        UUID id,
        String fullName,
        String role
) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getFullName(), user.getRole().name());
    }
}
