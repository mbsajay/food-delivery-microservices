package com.foodly.user.dto;

/**
 * Access + refresh token pair returned by login/refresh. {@code expiresIn} is the
 * access token lifetime in seconds; {@code tokenType} is always {@code Bearer}.
 */
public record TokenPair(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public static TokenPair bearer(String accessToken, String refreshToken, long expiresIn) {
        return new TokenPair(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
