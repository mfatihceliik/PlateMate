package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IRefreshTokenService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.dataAccess.abstracts.IUserRefreshTokenDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRefreshToken;
import com.mefy.platemate.entities.dto.AuthTokensDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenManager implements IRefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final IUserRefreshTokenDao userRefreshTokenDao;

    @Override
    @Transactional
    public AuthTokensDto issueTokens(User user) {
        return generateAndPersistTokens(user, null);
    }

    @Override
    @Transactional
    public AuthTokensDto refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenServiceException(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_INVALID);
        }

        JwtTokenProvider.RefreshTokenClaims claims = parseRefreshClaims(refreshToken);
        String tokenHash = hashToken(refreshToken);

        UserRefreshToken existing = userRefreshTokenDao.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RefreshTokenServiceException(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_INVALID));

        if (existing.isRevoked()) {
            throw new RefreshTokenServiceException(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_REVOKED);
        }

        if (!existing.getJti().equals(claims.jti()) || !existing.getUser().getId().equals(claims.userId())) {
            throw new RefreshTokenServiceException(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_INVALID);
        }

        if (claims.expired() || existing.isExpired()) {
            existing.setRevokedAt(LocalDateTime.now());
            existing.setLastUsedAt(LocalDateTime.now());
            userRefreshTokenDao.save(existing);
            throw new RefreshTokenServiceException(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_EXPIRED);
        }

        existing.setLastUsedAt(LocalDateTime.now());
        return generateAndPersistTokens(existing.getUser(), existing);
    }

    @Override
    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(refreshToken);
        userRefreshTokenDao.findByTokenHash(tokenHash).ifPresent(existing -> {
            if (!existing.isRevoked()) {
                existing.setRevokedAt(LocalDateTime.now());
            }
            existing.setLastUsedAt(LocalDateTime.now());
            userRefreshTokenDao.save(existing);
        });
    }

    private AuthTokensDto generateAndPersistTokens(User user, UserRefreshToken replacedToken) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());

        String newJti = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), newJti);
        String refreshTokenHash = hashToken(refreshToken);

        UserRefreshToken tokenRecord = new UserRefreshToken();
        tokenRecord.setUser(user);
        tokenRecord.setJti(newJti);
        tokenRecord.setTokenHash(refreshTokenHash);
        tokenRecord.setExpiresAt(LocalDateTime.ofInstant(
                jwtTokenProvider.getRefreshTokenExpiryFromNow(),
                ZoneId.systemDefault()
        ));
        tokenRecord.setLastUsedAt(LocalDateTime.now());
        userRefreshTokenDao.save(tokenRecord);

        if (replacedToken != null) {
            replacedToken.setRevokedAt(LocalDateTime.now());
            replacedToken.setLastUsedAt(LocalDateTime.now());
            replacedToken.setReplacedByTokenHash(refreshTokenHash);
            userRefreshTokenDao.save(replacedToken);
        }

        return new AuthTokensDto(accessToken, refreshToken);
    }

    private JwtTokenProvider.RefreshTokenClaims parseRefreshClaims(String refreshToken) {
        try {
            return jwtTokenProvider.validateAndGetRefreshClaims(refreshToken);
        } catch (JwtTokenProvider.RefreshTokenJwtException e) {
            throw new RefreshTokenServiceException(e.getCode());
        }
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexBuilder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available.", e);
        }
    }
}
