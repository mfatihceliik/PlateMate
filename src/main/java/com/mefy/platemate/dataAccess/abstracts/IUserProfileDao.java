package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IUserProfileDao extends JpaRepository<UserProfile, Long> {
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.socialMediaLinks sm LEFT JOIN FETCH sm.platformRef WHERE up.id = :id")
    Optional<UserProfile> findByIdWithSocialMediaLinks(Long id);
}
