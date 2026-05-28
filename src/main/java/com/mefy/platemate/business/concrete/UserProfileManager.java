package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFriendshipDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.Friendship;
import com.mefy.platemate.entities.concrete.FriendshipRequestStatusCodes;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.UserProfileFriendRequestDto;
import com.mefy.platemate.entities.dto.UserReviewEvaluationTotalsDto;
import com.mefy.platemate.entities.dto.UserReviewStatusCountsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileManager implements IUserProfileService {

    private static final int PROFILE_RECENT_LIMIT = 10;

    private final IUserProfileDao userProfileDao;
    private final IFriendshipDao friendshipDao;
    private final IPlateReviewDao plateReviewDao;
    private final UserProfileMapper userProfileMapper;
    private final PlateReviewMapper plateReviewMapper;
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

        List<PlateReviewDto> plateReviews = fetchRecentReviews(userId, selfViewer);
        List<UserProfileFriendRequestDto> friendRequests = fetchRecentFriendRequests(userId, selfViewer);

        long reviewCountLong = plateReviewDao.countByUserId(userId);
        long totalFriendCountsLong = friendshipDao.countByUserIdAndStatusId(userId, FriendshipRequestStatusCodes.ACCEPTED_ID);
        int reviewCount = toSafeInt(reviewCountLong);
        int totalFriendCounts = toSafeInt(totalFriendCountsLong);
        long totalRatingSum = safeLong(plateReviewDao.sumRatingByUserId(userId));
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

        populateProfileDetails(dto, profile, userId, selfViewer);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PROFILE_FOUND));
    }

    private List<PlateReviewDto> fetchRecentReviews(Long userId, boolean selfViewer) {
        var reviewPageable = PageRequest.of(
                0,
                PROFILE_RECENT_LIMIT,
                Sort.by("createdAt").descending()
        );
        return (selfViewer
                ? plateReviewDao.findByUserId(userId, reviewPageable)
                : plateReviewDao.findByUserIdAndStatusId(userId, PlateReviewStatus.APPROVED.getId(), reviewPageable))
                .map(plateReviewMapper::entityToDto)
                .getContent();
    }

    private List<UserProfileFriendRequestDto> fetchRecentFriendRequests(Long userId, boolean selfViewer) {
        return selfViewer
                ? friendshipDao.findRecentByUserId(userId, PageRequest.of(0, PROFILE_RECENT_LIMIT)).stream()
                    .map(this::toUserProfileFriendRequestDto)
                    .toList()
                : List.of();
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
