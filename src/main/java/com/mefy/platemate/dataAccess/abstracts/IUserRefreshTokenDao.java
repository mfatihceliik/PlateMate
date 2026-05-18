package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRefreshTokenDao extends JpaRepository<UserRefreshToken, Long> {

    Optional<UserRefreshToken> findByTokenHash(String tokenHash);

    Optional<UserRefreshToken> findByJti(String jti);
}
