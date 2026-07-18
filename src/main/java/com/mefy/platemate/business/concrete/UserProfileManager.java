package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFollowService;
import com.mefy.platemate.business.abstracts.IFriendshipService;
import com.mefy.platemate.business.abstracts.ISocialPlatformService;
import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFriendshipDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.Friendship;
import com.mefy.platemate.entities.concrete.FriendshipRequestStatusCodes;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.request.UpdateProfileRequest;
import com.mefy.platemate.entities.dto.FriendshipDto;
import com.mefy.platemate.entities.dto.SocialPlatformDto;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.UserProfileFriendRequestDto;
import com.mefy.platemate.entities.dto.UserProfilePageDto;
import com.mefy.platemate.entities.dto.UserProfileReviewDto;
import com.mefy.platemate.entities.dto.UserReviewEvaluationTotalsDto;
import com.mefy.platemate.entities.dto.UserReviewStatusCountsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileManager implements IUserProfileService {

    private static final int PROFILE_RECENT_LIMIT = 10;

    private final IUserProfileDao userProfileDao;
    private final IUserDao userDao;
    private final IFriendshipDao friendshipDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateReportDao plateReportDao;
    private final IFollowService followService;
    private final IFriendshipService friendshipService;
    private final ISocialPlatformService socialPlatformService;
    private final UserProfileMapper userProfileMapper;
    private final IUserSettingsService userSettingsService;
    private final IMessageService messageService;

    @Override
    public DataResult<UserProfileDto> getByUserId(Long userId, Long requesterUserId) {
        UserProfile profile = userProfileDao.findByIdWithSocialMediaLinks(userId).orElse(null);

        Result result = BusinessRules.run(checkIfProfileExists(profile));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        UserProfileDto dto = userProfileMapper.entityToDto(profile);
        boolean selfViewer = requesterUserId != null && requesterUserId.equals(userId);

        List<UserProfileReviewDto> plateReviews = fetchRecentProfileReviews(userId, selfViewer);
        List<UserProfileFriendRequestDto> friendRequests = fetchRecentFriendRequests(userId, selfViewer);

        Long approvedStatusId = PlateReviewStatus.APPROVED.getId();
        long reviewCountLong = plateReviewDao.countByUserIdAndStatusId(userId, approvedStatusId);
        long totalFriendCountsLong = friendshipDao.countByUserIdAndStatusId(userId, FriendshipRequestStatusCodes.ACCEPTED_ID);
        int reviewCount = toSafeInt(reviewCountLong);
        int totalFriendCounts = toSafeInt(totalFriendCountsLong);
        long totalRatingSum = safeLong(plateReviewDao.sumRatingByUserIdAndStatus(userId, approvedStatusId));
        double averageGivenRating = reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0;

        UserReviewStatusCountsDto fullStatusCounts = buildFullReviewStatusCounts(userId);
        UserReviewStatusCountsDto statusCounts = buildViewerStatusCounts(fullStatusCounts, selfViewer);
        UserReviewEvaluationTotalsDto evaluationTotals = toEvaluationTotals(fullStatusCounts);

        dto.setPlateReviews(plateReviews);
        dto.setFriendRequests(friendRequests);
        dto.setReviewStatusCounts(statusCounts);
        dto.setEvaluationTotals(evaluationTotals);
        dto.setTotalFriendCounts(totalFriendCounts);
        dto.setReviewCount(reviewCount);
        dto.setAverageGivenRating(averageGivenRating);

        populateFollowData(dto, userId, requesterUserId);
        populateFriendshipData(dto, userId, requesterUserId);
        populateProfileDetails(dto, profile, userId, selfViewer);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PROFILE_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResult<UserProfilePageDto> getPageByUserId(Long userId, Long requesterUserId) {
        DataResult<UserProfileDto> profileResult = getByUserId(userId, requesterUserId);
        if (!profileResult.isSuccess()) {
            return new ErrorDataResult<>(profileResult.getMessage());
        }

        boolean selfViewer = requesterUserId != null && requesterUserId.equals(userId);
        // Bekleyen arkadaşlık istekleri sadece kendi profilini görüntülerken anlamlı (başkasının
        // bekleyen isteklerini görmek gizlilik ihlali olur).
        List<FriendshipDto> pendingFriendRequests = selfViewer
                ? friendshipService.getPendingRequests(requesterUserId).getData()
                : List.of();
        List<SocialPlatformDto> socialPlatforms = socialPlatformService.getActivePlatforms().getData();

        UserProfilePageDto page = new UserProfilePageDto(profileResult.getData(), pendingFriendRequests, socialPlatforms);
        return new SuccessDataResult<>(page, messageService.getMessage(Messages.PROFILE_FOUND));
    }

    @Override
    @Transactional
    public Result updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileDao.findByIdWithSocialMediaLinks(userId).orElse(null);

        Result validation = BusinessRules.run(
                checkIfProfileExists(profile),
                checkUsernameUpdate(request.getUsername(), profile)
        );
        if (validation != null) {
            return validation;
        }

        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getProfilePhotoUrl() != null) {
            profile.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }

        if (request.getUsername() != null && profile.getUser() != null) {
            profile.getUser().setUsername(request.getUsername());
        }

        userProfileDao.save(profile);
        return new SuccessResult(messageService.getMessage(Messages.PROFILE_UPDATED));
    }

    private Result checkUsernameUpdate(String newUsername, UserProfile profile) {
        if (newUsername == null || profile == null || profile.getUser() == null) {
            return new SuccessResult();
        }
        if (newUsername.equals(profile.getUser().getUsername())) {
            return new SuccessResult();
        }
        if (userDao.existsByUsername(newUsername)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_USERNAME_EXISTS));
        }
        return new SuccessResult();
    }

    private List<UserProfileReviewDto> fetchRecentProfileReviews(Long userId, boolean selfViewer) {
        var pageable = PageRequest.of(0, PROFILE_RECENT_LIMIT);

        List<PlateReview> reviews = selfViewer
                ? plateReviewDao.findRecentByUserIdWithPlateAndCity(userId, pageable)
                : plateReviewDao.findRecentByUserIdAndStatusIdWithPlateAndCity(
                        userId, PlateReviewStatus.APPROVED.getId(), pageable);

        if (reviews.isEmpty()) {
            return List.of();
        }

        List<Long> plateIds = reviews.stream()
                .map(r -> r.getPlate().getId())
                .toList();

        Map<Long, List<String>> tagsByPlateId = buildReportTagMap(userId, plateIds);

        return reviews.stream()
                .map(review -> toProfileReviewDto(review, tagsByPlateId))
                .toList();
    }

    private Map<Long, List<String>> buildReportTagMap(Long userId, Collection<Long> plateIds) {
        List<PlateReport> reports = plateReportDao.findByUserIdAndPlateIdInAndActiveTrue(
                userId, plateIds, PlateReviewStatus.APPROVED.getId());

        return reports.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getPlate().getId(),
                        Collectors.mapping(
                                r -> r.getReportType().getLabel(),
                                Collectors.toList()
                        )
                ));
    }

    private UserProfileReviewDto toProfileReviewDto(PlateReview review, Map<Long, List<String>> tagsByPlateId) {
        UserProfileReviewDto dto = new UserProfileReviewDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewStatusId(review.getStatusId());
        dto.setReviewStatusCode(review.getStatusCode());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getPlate() != null) {
            dto.setPlateCode(review.getPlate().getPlateCode());
            if (review.getPlate().getCity() != null) {
                dto.setCityName(review.getPlate().getCity().getName());
            }
        }

        Long plateId = review.getPlate() != null ? review.getPlate().getId() : null;
        dto.setReportTags(tagsByPlateId.getOrDefault(plateId, Collections.emptyList()));

        return dto;
    }

    private List<UserProfileFriendRequestDto> fetchRecentFriendRequests(Long userId, boolean selfViewer) {
        return selfViewer
                ? friendshipDao.findRecentByUserId(userId, PageRequest.of(0, PROFILE_RECENT_LIMIT)).stream()
                    .map(this::toUserProfileFriendRequestDto)
                    .toList()
                : List.of();
    }

    private void populateFollowData(UserProfileDto dto, Long profileUserId, Long requesterUserId) {
        dto.setFollowerCount(toSafeInt(followService.countFollowers(profileUserId)));
        dto.setFollowingCount(toSafeInt(followService.countFollowing(profileUserId)));
        dto.setIsFollowing(followService.isFollowing(requesterUserId, profileUserId));
    }

    private void populateFriendshipData(UserProfileDto dto, Long profileUserId, Long requesterUserId) {
        if (requesterUserId == null || requesterUserId.equals(profileUserId)) {
            dto.setFriendshipStatus("NONE");
            return;
        }
        
        java.util.Optional<Friendship> friendshipOpt = friendshipDao.findLatestActiveBetweenUsers(
                requesterUserId, profileUserId, FriendshipRequestStatusCodes.ACTIVE_IDS);
        
        if (friendshipOpt.isPresent()) {
            Friendship f = friendshipOpt.get();
            dto.setFriendshipId(f.getId());
            if (f.getStatusId().equals(FriendshipRequestStatusCodes.ACCEPTED_ID)) {
                dto.setFriendshipStatus("FRIENDS");
            } else if (f.getStatusId().equals(FriendshipRequestStatusCodes.REQUESTED_ID)) {
                if (f.getRequester().getId().equals(requesterUserId)) {
                    dto.setFriendshipStatus("PENDING_SENT");
                } else {
                    dto.setFriendshipStatus("PENDING_RECEIVED");
                }
            }
        } else {
            dto.setFriendshipStatus("NONE");
        }
    }

    private void populateProfileDetails(UserProfileDto dto, UserProfile profile, Long userId, boolean selfViewer) {
        if (profile.getUser() != null) {
            dto.setJoinedAt(profile.getUser().getCreatedAt());
            dto.setPremiumActive(profile.getUser().isPremiumActive());
        }

        if (selfViewer) {
            dto.setUserSettings(userSettingsService.getByUserId(userId).getData());
        }
    }

    private UserProfileFriendRequestDto toUserProfileFriendRequestDto(Friendship friendship) {
        LocalDateTime lastActionAt = friendship.getRespondedAt() != null
                ? friendship.getRespondedAt()
                : friendship.getCreatedAt();

        return new UserProfileFriendRequestDto(
                friendship.getId(),
                friendship.getRequester() == null ? null : friendship.getRequester().getId(),
                friendship.getRequester() == null ? null : friendship.getRequester().getUsername(),
                friendship.getAddressee() == null ? null : friendship.getAddressee().getId(),
                friendship.getAddressee() == null ? null : friendship.getAddressee().getUsername(),
                friendship.getStatusId(),
                friendship.getStatusCode(),
                friendship.getCreatedAt(),
                friendship.getRespondedAt(),
                lastActionAt
        );
    }

    private Result checkIfProfileExists(UserProfile profile) {
        if (profile == null) {
            return new ErrorResult(messageService.getMessage(Messages.PROFILE_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private UserReviewStatusCountsDto buildFullReviewStatusCounts(Long userId) {
        return new UserReviewStatusCountsDto(
                toSafeInt(plateReviewDao.countByUserIdAndStatusId(userId, PlateReviewStatus.APPROVED.getId())),
                toSafeInt(plateReviewDao.countByUserIdAndStatusId(userId, PlateReviewStatus.PENDING_REVIEW.getId())),
                toSafeInt(plateReviewDao.countByUserIdAndStatusId(userId, PlateReviewStatus.REJECTED.getId())),
                toSafeInt(plateReviewDao.countByUserIdAndStatusId(userId, PlateReviewStatus.REMOVED_BY_USER.getId())),
                toSafeInt(plateReviewDao.countByUserIdAndStatusId(userId, PlateReviewStatus.REMOVED_BY_MODERATOR.getId())),
                toSafeInt(plateReviewDao.countByUserIdAndStatusId(userId, PlateReviewStatus.REMOVED_BY_LEGAL_REQUEST.getId()))
        );
    }

    private UserReviewStatusCountsDto buildViewerStatusCounts(UserReviewStatusCountsDto fullStatusCounts, boolean selfViewer) {
        if (selfViewer) {
            return fullStatusCounts;
        }

        return new UserReviewStatusCountsDto(
                fullStatusCounts.getApproved(),
                0,
                0,
                0,
                0,
                0
        );
    }

    private UserReviewEvaluationTotalsDto toEvaluationTotals(UserReviewStatusCountsDto fullStatusCounts) {
        return new UserReviewEvaluationTotalsDto(
                fullStatusCounts.getApproved(),
                fullStatusCounts.getPendingReview(),
                fullStatusCounts.getRejected(),
                fullStatusCounts.getRemovedByUser(),
                fullStatusCounts.getRemovedByModerator(),
                fullStatusCounts.getRemovedByLegalRequest()
        );
    }

    private int toSafeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}

