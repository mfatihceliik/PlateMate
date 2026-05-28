package com.mefy.platemate.config.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = "test-only-jwt-secret-for-unit-tests-2026";

    @Test
    void validateAndGetRefreshClaimsReturnsClaimsForValidRefreshToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 900000, 2592000000L);

        String refreshToken = provider.generateRefreshToken(7L, "fatih", "jti-7");
        JwtTokenProvider.RefreshTokenClaims claims = provider.validateAndGetRefreshClaims(refreshToken);

        assertEquals(7L, claims.userId());
        assertEquals("fatih", claims.username());
        assertEquals("jti-7", claims.jti());
        assertFalse(claims.expired());
    }

    @Test
    void validateAndGetRefreshClaimsRejectsAccessToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 900000, 2592000000L);

        String accessToken = provider.generateAccessToken(5L, "alice");

        JwtTokenProvider.RefreshTokenJwtException ex = assertThrows(
                JwtTokenProvider.RefreshTokenJwtException.class,
                () -> provider.validateAndGetRefreshClaims(accessToken)
        );

        assertEquals(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_INVALID, ex.getCode());
    }

    @Test
    void validateAccessTokenRejectsRefreshToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 900000, 2592000000L);

        String refreshToken = provider.generateRefreshToken(2L, "mert", "jti-2");

        assertFalse(provider.validateAccessToken(refreshToken));
    }

    @Test
    void validateAndGetRefreshClaimsMarksExpiredRefreshToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 900000, -1);

        String refreshToken = provider.generateRefreshToken(11L, "old-user", "jti-old");
        JwtTokenProvider.RefreshTokenClaims claims = provider.validateAndGetRefreshClaims(refreshToken);

        assertTrue(claims.expired());
    }
}
