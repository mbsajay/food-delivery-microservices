package com.foodly.common.security;

import com.foodly.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import java.security.PublicKey;
import java.util.List;

/**
 * Stateless RS256 token verifier for downstream services that do not run a full
 * Spring Security resource server. Construct it once with the platform public key
 * (published by {@code config-server}) and reuse it — the underlying parser is
 * thread-safe.
 *
 * <p>Verification failures (bad signature, expired, malformed) are normalised to
 * {@link UnauthorizedException} so a service's {@code @RestControllerAdvice} can
 * translate them to the standard {@code ApiResponse.fail(...)} envelope.
 */
public class JwtVerifier {

    private final PublicKey publicKey;

    public JwtVerifier(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /** Verifies the signature and expiry and returns the parsed claims. */
    public Claims verify(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid or expired authentication token");
        }
    }

    /** The subject (user id) of a verified token. */
    public String subject(String token) {
        return verify(token).getSubject();
    }

    /** The roles claim of a verified token, or an empty list when absent. */
    @SuppressWarnings("unchecked")
    public List<String> roles(String token) {
        Object roles = verify(token).get(JwtClaims.ROLES);
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }
}
