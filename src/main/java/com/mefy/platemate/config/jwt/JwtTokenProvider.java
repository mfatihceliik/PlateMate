package com.mefy.platemate.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms:${jwt.expiration-ms:900000}}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms:2592000000}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // Backward-compatible alias for existing call sites.
    public String generateToken(Long userId, String username) {
        return generateAccessToken(userId, username);
    }

    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, String jti) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("username", String.class);
    }

    public boolean validateToken(String token) {
        return validateAccessToken(token);
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return tokenType == null || ACCESS_TOKEN_TYPE.equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public RefreshTokenClaims validateAndGetRefreshClaims(String token) {
        try {
            Claims claims = parseClaims(token);
            ensureRefreshTokenType(claims);
            return toRefreshTokenClaims(claims, false);
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            if (claims == null) {
                throw new RefreshTokenJwtException(RefreshTokenErrorCode.REFRESH_INVALID);
            }
            ensureRefreshTokenType(claims);
            return toRefreshTokenClaims(claims, true);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RefreshTokenJwtException(RefreshTokenErrorCode.REFRESH_INVALID);
        }
    }

    public Instant getRefreshTokenExpiryFromNow() {
        return Instant.now().plusMillis(refreshExpirationMs);
    }

    private RefreshTokenClaims toRefreshTokenClaims(Claims claims, boolean expired) {
        try {
            Long userId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (jti == null || jti.isBlank() || expiration == null) {
                throw new RefreshTokenJwtException(RefreshTokenErrorCode.REFRESH_INVALID);
            }

            return new RefreshTokenClaims(userId, username, jti, expiration.toInstant(), expired);
        } catch (NumberFormatException e) {
            throw new RefreshTokenJwtException(RefreshTokenErrorCode.REFRESH_INVALID);
        }
    }

    private void ensureRefreshTokenType(Claims claims) {
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw new RefreshTokenJwtException(RefreshTokenErrorCode.REFRESH_INVALID);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record RefreshTokenClaims(
            Long userId,
            String username,
            String jti,
            Instant expiresAt,
            boolean expired
    ) {
    }

    public enum RefreshTokenErrorCode {
        REFRESH_EXPIRED,
        REFRESH_REVOKED,
        REFRESH_INVALID
    }

    public static class RefreshTokenJwtException extends RuntimeException {
        private final RefreshTokenErrorCode code;

        public RefreshTokenJwtException(RefreshTokenErrorCode code) {
            super(code.name());
            this.code = code;
        }

        public RefreshTokenErrorCode getCode() {
            return code;
        }
    }
}
