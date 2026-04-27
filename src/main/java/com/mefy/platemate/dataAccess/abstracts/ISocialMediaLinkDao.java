package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.SocialMediaLink;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mefy.platemate.entities.concrete.SocialPlatform;

public interface ISocialMediaLinkDao extends JpaRepository<SocialMediaLink, Long> {
    boolean existsByUserProfileIdAndPlatform(Long userProfileId, SocialPlatform platform);
}
