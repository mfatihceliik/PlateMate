package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.SocialMediaLink;
import com.mefy.platemate.entities.concrete.SocialPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISocialMediaLinkDao extends JpaRepository<SocialMediaLink, Long> {
    boolean existsByUserProfileIdAndPlatformId(Long userProfileId, Long platformId);

    default boolean existsByUserProfileIdAndPlatform(Long userProfileId, SocialPlatform platform) {
        return existsByUserProfileIdAndPlatformId(userProfileId, platform == null ? null : platform.getId());
    }
}
