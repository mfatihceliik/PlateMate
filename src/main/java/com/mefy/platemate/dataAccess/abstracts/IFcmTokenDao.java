package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFcmTokenDao extends JpaRepository<UserFcmToken, Long> {
    List<UserFcmToken> findByUserId(Long userId);
    Optional<UserFcmToken> findByToken(String token);
    void deleteByToken(String token);
}
