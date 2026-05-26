package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.UserProfileReviewPageDto;
import com.mefy.platemate.entities.dto.UserProfileReviewPageMetaDto;
import com.mefy.platemate.entities.dto.UserReviewEvaluationTotalsDto;
import com.mefy.platemate.entities.dto.UserReviewStatusCountsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileManager implements IUserProfileService {

    private final IUserProfileDao userProfileDao;
    private final IPlateReviewDao plateReviewDao;
    private final UserProfileMapper userProfileMapper;
    private final PlateReviewMapper plateReviewMapper;
    private final IUserSettingsService userSettingsService;
    private final IMessageService messageService;

    @Override
    public DataResult<UserProfileDto> getByUserId(Long userId, Long requesterUserId, PaginationRequest paginationRequest) {
        UserProfile profile = userProfileDao.findById(userId).orElse(null);
        
        Result result = BusinessRules.run(checkIfProfileExists(profile));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        UserProfileDto dto = userProfileMapper.entityToDto(profile);

        var pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        boolean selfViewer = requesterUserId != null && requesterUserId.equals(userId);
        var plateReviews = (selfViewer
                ? plateReviewDao.findByUserId(userId, pageable)
                : plateReviewDao.findByUserIdAndStatus(userId, PlateReviewStatus.APPROVED, pageable))
                .map(plateReviewMapper::entityToDto);
        long reviewCountLong = plateReviewDao.countByUserId(userId);
        int reviewCount = toSafeInt(reviewCountLong);
        long totalRatingSum = safeLong(plateReviewDao.sumRatingByUserId(userId));
        double averageGivenRating = reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0;
        UserReviewStatusCountsDto fullStatusCounts = buildFullReviewStatusCounts(userId);
        UserReviewStatusCountsDto statusCounts = buildViewerStatusCounts(fullStatusCounts, selfViewer);
        UserReviewEvaluationTotalsDto evaluationTotals = toEvaluationTotals(fullStatusCounts);

        dto.setPlateReviews(toReviewPageDto(plateReviews, evaluationTotals));
        dto.setReviewStatusCounts(statusCounts);
        dto.setReviewCount(reviewCount);
        dto.setAverageGivenRating(averageGivenRating);

        if (profile.getUser() != null) {
            dto.setJoinedAt(profile.getUser().getCreatedAt());
            dto.setPremiumActive(profile.getUser().isPremiumActive());
            dto.setPremiumUntil(profile.getUser().getPremiumUntil());
        }

        if (selfViewer) {
            dto.setUserSettings(userSettingsService.getByUserId(userId).getData());
        }

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PROFILE_FOUND));
    }

    /// ----- BUSINESS RULES -----

    private Result checkIfProfileExists(UserProfile profile) {
        if (profile == null) {
            return new ErrorResult(messageService.getMessage(Messages.PROFILE_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private UserReviewStatusCountsDto buildFullReviewStatusCounts(Long userId) {
        return new UserReviewStatusCountsDto(
                toSafeInt(plateReviewDao.countByUserIdAndStatus(userId, PlateReviewStatus.APPROVED)),
                toSafeInt(plateReviewDao.countByUserIdAndStatus(userId, PlateReviewStatus.PENDING_REVIEW)),
                toSafeInt(plateReviewDao.countByUserIdAndStatus(userId, PlateReviewStatus.REJECTED)),
                toSafeInt(plateReviewDao.countByUserIdAndStatus(userId, PlateReviewStatus.REMOVED_BY_USER)),
                toSafeInt(plateReviewDao.countByUserIdAndStatus(userId, PlateReviewStatus.REMOVED_BY_MODERATOR)),
                toSafeInt(plateReviewDao.countByUserIdAndStatus(userId, PlateReviewStatus.REMOVED_BY_LEGAL_REQUEST))
        );
    }

    private UserReviewStatusCountsDto buildViewerStatusCounts(
            UserReviewStatusCountsDto fullStatusCounts,
            boolean selfViewer
    ) {
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

    private UserProfileReviewPageDto toReviewPageDto(
            Page<PlateReviewDto> page,
            UserReviewEvaluationTotalsDto evaluationTotals
    ) {
        UserProfileReviewPageMetaDto meta = new UserProfileReviewPageMetaDto(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                evaluationTotals
        );
        return new UserProfileReviewPageDto(page.getContent(), meta);
    }

    private int toSafeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
