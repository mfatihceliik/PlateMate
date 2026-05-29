package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IRefreshTokenService;
import com.mefy.platemate.business.exceptions.RefreshTokenServiceException;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.dataAccess.abstracts.IUserRefreshTokenDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRefreshToken;
import com.mefy.platemate.entities.dto.AuthTokensDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenManagerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private IUserRefreshTokenDao userRefreshTokenDao;

    private RefreshTokenManager manager;

    @BeforeEach
    void setUp() {
        manager = new RefreshTokenManager(jwtTokenProvider, userRefreshTokenDao);
    }

    @Test
    void issueTokensCreatesAccessAndRefreshTokens() {
        User user = buildUser(10L, "fatih");

        when(jwtTokenProvider.generateAccessToken(10L, "fatih")).thenReturn("access-10");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh-10");
        when(jwtTokenProvider.getRefreshTokenExpiryFromNow()).thenReturn(Instant.now().plusSeconds(3600));
        when(userRefreshTokenDao.save(any(UserRefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthTokensDto result = manager.issueTokens(user);

        assertEquals("access-10", result.getAccessToken());
        assertEquals("refresh-10", result.getRefreshToken());
        verify(userRefreshTokenDao).save(any(UserRefreshToken.class));
    }

    @Test
    void refreshTokensReturnsRevokedWhenRecordAlreadyRevoked() {
        User user = buildUser(3L, "mert");
        UserRefreshToken tokenRecord = buildTokenRecord(user, "jti-3", LocalDateTime.now().plusMinutes(15));
        tokenRecord.setRevokedAt(LocalDateTime.now());

        when(jwtTokenProvider.validateAndGetRefreshClaims("old-refresh"))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(
                        3L, "mert", "jti-3", Instant.now().plusSeconds(300), false
                ));
        when(userRefreshTokenDao.findByTokenHash(anyString())).thenReturn(Optional.of(tokenRecord));

        RefreshTokenServiceException ex = assertThrows(
                RefreshTokenServiceException.class,
                () -> manager.refreshTokens("old-refresh")
        );

        assertEquals(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_REVOKED, ex.getCode());
    }

    @Test
    void refreshTokensRotatesTokenAndRevokesOldToken() {
        User user = buildUser(6L, "emine");
        UserRefreshToken tokenRecord = buildTokenRecord(user, "jti-old", LocalDateTime.now().plusMinutes(30));

        when(jwtTokenProvider.validateAndGetRefreshClaims("old-refresh"))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(
                        6L, "emine", "jti-old", Instant.now().plusSeconds(600), false
                ));
        when(jwtTokenProvider.generateAccessToken(6L, "emine")).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("new-refresh");
        when(jwtTokenProvider.getRefreshTokenExpiryFromNow()).thenReturn(Instant.now().plusSeconds(3600));
        when(userRefreshTokenDao.findByTokenHash(anyString())).thenReturn(Optional.of(tokenRecord));
        when(userRefreshTokenDao.save(any(UserRefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthTokensDto result = manager.refreshTokens("old-refresh");

        assertEquals("new-access", result.getAccessToken());
        assertEquals("new-refresh", result.getRefreshToken());
        assertNotNull(tokenRecord.getRevokedAt());
        assertNotNull(tokenRecord.getReplacedByTokenHash());
        verify(userRefreshTokenDao, atLeast(2)).save(any(UserRefreshToken.class));
    }

    @Test
    void refreshTokensReturnsExpiredAndRevokesRecordWhenJwtExpired() {
        User user = buildUser(8L, "bora");
        UserRefreshToken tokenRecord = buildTokenRecord(user, "jti-8", LocalDateTime.now().plusMinutes(30));

        when(jwtTokenProvider.validateAndGetRefreshClaims("expired-refresh"))
                .thenReturn(new JwtTokenProvider.RefreshTokenClaims(
                        8L, "bora", "jti-8", Instant.now().minusSeconds(30), true
                ));
        when(userRefreshTokenDao.findByTokenHash(anyString())).thenReturn(Optional.of(tokenRecord));
        when(userRefreshTokenDao.save(any(UserRefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenServiceException ex = assertThrows(
                RefreshTokenServiceException.class,
                () -> manager.refreshTokens("expired-refresh")
        );

        assertEquals(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_EXPIRED, ex.getCode());
        assertNotNull(tokenRecord.getRevokedAt());
        verify(userRefreshTokenDao).save(tokenRecord);
    }

    @Test
    void revokeIsIdempotentWhenTokenRecordDoesNotExist() {
        when(userRefreshTokenDao.findByTokenHash(anyString())).thenReturn(Optional.empty());

        manager.revoke("missing-refresh");

        verify(userRefreshTokenDao, never()).save(any(UserRefreshToken.class));
    }

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private UserRefreshToken buildTokenRecord(User user, String jti, LocalDateTime expiresAt) {
        UserRefreshToken token = new UserRefreshToken();
        token.setUser(user);
        token.setJti(jti);
        token.setTokenHash("hash-" + jti);
        token.setExpiresAt(expiresAt);
        return token;
    }
}
