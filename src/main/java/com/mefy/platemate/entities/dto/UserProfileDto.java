package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto implements IDto {
    private Long id;
    private String username;
    private Double averageGivenRating;
    private Integer reviewCount;
    private LocalDateTime joinedAt;
    private Boolean premiumActive;
    private LocalDateTime premiumUntil;
    private UserSettingsDto userSettings;
    private UserReviewStatusCountsDto reviewStatusCounts;
    private List<SocialMediaLinkDto> socialMediaLinks;
    private UserProfileReviewPageDto plateReviews;
}
