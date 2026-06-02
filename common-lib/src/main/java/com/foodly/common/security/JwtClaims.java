package com.foodly.common.security;

/**
 * Shared JWT claim names and role identifiers used across Foodly services.
 *
 * <p>The {@code user-service} signs tokens using these keys and every downstream
 * service reads them back via {@link JwtVerifier} or a Spring Security resource
 * server. Keeping the names in one place avoids silent drift between the issuer
 * and the verifiers.
 */
public final class JwtClaims {

    private JwtClaims() {
    }

    /** Standard subject claim — carries the user's UUID. */
    public static final String SUBJECT = "sub";

    /** Granted roles, serialised as a JSON array of role names (without the {@code ROLE_} prefix). */
    public static final String ROLES = "roles";

    /** Distinguishes access tokens from refresh tokens. */
    public static final String TOKEN_TYPE = "tokenType";

    /** Convenience claim carrying the user's email. */
    public static final String EMAIL = "email";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_RESTAURANT_OWNER = "RESTAURANT_OWNER";
    public static final String ROLE_DELIVERY_AGENT = "DELIVERY_AGENT";
    public static final String ROLE_ADMIN = "ADMIN";
}
