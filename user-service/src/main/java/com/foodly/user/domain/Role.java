package com.foodly.user.domain;

/**
 * Coarse-grained account roles. Persisted as {@code VARCHAR} (the enum name) and
 * mirrored into the {@code roles} JWT claim as {@code ROLE_<name>} authorities by
 * the security layer.
 */
public enum Role {
    CUSTOMER,
    RESTAURANT_OWNER,
    DELIVERY_AGENT,
    ADMIN
}
