package com.foodly.user.security;

import com.foodly.common.exception.UnauthorizedException;
import com.foodly.common.security.JwtClaims;
import com.foodly.user.domain.User;
import com.foodly.user.dto.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Mints and validates RS256 access/refresh tokens. The access token carries the
 * subject, email and roles claims (read by every downstream resource server); the
 * refresh token is minimal and only ever exchanged back here for a fresh pair.
 */
@Component
public class JwtIssuer {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final JwtProperties properties;

    public JwtIssuer(JwtProperties properties) {
        this.properties = properties;
        this.privateKey = properties.rsaPrivateKey();
        this.publicKey = properties.rsaPublicKey();
    }

    public TokenPair issue(User user) {
        Instant now = Instant.now();
        String access = accessToken(user, now);
        String refresh = refreshToken(user, now);
        return TokenPair.bearer(access, refresh, properties.getAccessTokenTtl().toSeconds());
    }

    private String accessToken(User user, Instant now) {
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.getId().toString())
                .claim(JwtClaims.EMAIL, user.getEmail())
                .claim(JwtClaims.ROLES, List.of(user.getRole().name()))
                .claim(JwtClaims.TOKEN_TYPE, JwtClaims.TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTokenTtl())))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private String refreshToken(User user, Instant now) {
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.getId().toString())
                .claim(JwtClaims.TOKEN_TYPE, JwtClaims.TOKEN_TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getRefreshTokenTtl())))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Validates a refresh token and returns its subject (user id), or 401 on failure. */
    public String parseRefreshSubject(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            if (!JwtClaims.TOKEN_TYPE_REFRESH.equals(claims.get(JwtClaims.TOKEN_TYPE))) {
                throw new UnauthorizedException("Not a refresh token");
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }
}
