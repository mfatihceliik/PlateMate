package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserSettingsDao extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUserId(Long userId);
}
