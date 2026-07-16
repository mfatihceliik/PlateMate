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
    private String displayName;
    private String bio;
    private String profilePhotoUrl;
    private Boolean verified;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean isFollowing;
    private String friendshipStatus;
    private Long friendshipId;
    private Integer totalFriendCounts;
    private Double averageGivenRating;
    private Integer reviewCount;
    private LocalDateTime joinedAt;
    private Boolean premiumActive;
    private UserSettingsDto userSettings;
    private UserReviewStatusCountsDto reviewStatusCounts;
    private UserReviewEvaluationTotalsDto evaluationTotals;
    private List<SocialMediaLinkDto> socialMediaLinks;
    private List<UserProfileReviewDto> plateReviews;
    private List<UserProfileFriendRequestDto> friendRequests;
}
