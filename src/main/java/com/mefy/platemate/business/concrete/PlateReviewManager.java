package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.abstracts.IPlateReviewService;
import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.abstracts.IPlateModerationService;
import com.mefy.platemate.business.utilities.moderation.ContentModerationResult;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.ReviewResponseDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlateReviewManager implements IPlateReviewService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateReportDao plateReportDao;
    private final IUserDao userDao;
    private final IPlateReportService plateReportService;
    private final PlateReviewMapper plateReviewMapper;
    private final IPlateModerationService plateModerationService;
    private final IMessageService messageService;
    private final IPlateSearchService plateSearchService;

    @Value("${moderation.accepted-responsibility-legacy-fallback:true}")
    private boolean acceptedResponsibilityLegacyFallback = true;

    @Override
    public DataResult<PagedData<PlateReviewDto>> getReviewsByPlateCode(
            String plateCode,
            PaginationRequest paginationRequest
    ) {
        String normalizedPlate = plateSearchService.normalizePlate(plateCode);
        Result result = BusinessRules.run(plateSearchService.checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Plate plate = plateDao.findByPlateCode(normalizedPlate).orElse(null);
        Result visibilityResult = plateSearchService.checkIfPlatePubliclyVisible(plate);
        if (!visibilityResult.isSuccess()) {
            return new ErrorDataResult<>(visibilityResult.getMessage());
        }

        Pageable pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = plateReviewDao.findByPlatePlateCodeAndStatusId(
                        normalizedPlate,
                        approvedStatusId(),
                        pageable
                )
                .map(plateReviewMapper::entityToDto);
        PagedData<PlateReviewDto> reviews = PaginationMapper.fromPage(page);

        return new SuccessDataResult<>(reviews, messageService.getMessage(Messages.REVIEWS_LISTED));
    }

    @Override
    public DataResult<PagedData<PlateReviewDto>> getMyReviews(
            Long userId,
            String statusCode,
            String query,
            PaginationRequest paginationRequest
    ) {
        PlateReviewStatus status = PlateReviewStatus.fromCode(statusCode);
        if (statusCode != null && status == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.INVALID_REQUEST_PARAM));
        }

        String search = (query == null || query.isBlank()) ? null : query.trim();
        Pageable pageable = PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize());
        var page = plateReviewDao.searchByUserId(
                        userId,
                        status == null ? null : status.getId(),
                        search,
                        pageable
                )
                .map(plateReviewMapper::entityToDto);
        PagedData<PlateReviewDto> reviews = PaginationMapper.fromPage(page);

        return new SuccessDataResult<>(reviews, messageService.getMessage(Messages.REVIEWS_LISTED));
    }

    @Override
    public DataResult<PlateReviewDto> getReviewById(Long reviewId) {
        PlateReview review = plateReviewDao.findById(reviewId).orElse(null);
        Result existsResult = checkIfReviewExists(review);
        if (!existsResult.isSuccess()) {
            return new ErrorDataResult<>(existsResult.getMessage());
        }

        return new SuccessDataResult<>(plateReviewMapper.entityToDto(review), messageService.getMessage(Messages.REVIEWS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<ReviewResponseDto> addReview(String plateCode, Long currentUserId, AddPlateReviewRequest request) {
        String normalizedPlate = plateSearchService.normalizePlate(plateCode);
        User user = userDao.findByIdAndActiveTrue(currentUserId).orElse(null);

        Result validationResult = validateAddReviewInput(normalizedPlate, user, request);
        if (validationResult != null) return new ErrorDataResult<>(validationResult.getMessage());

        String normalizedComment = normalizeComment(request.getComment());
        Result submissionRulesResult = validateSubmissionRules(user, normalizedComment, request.getReportTypeCodes());
        if (submissionRulesResult != null) return new ErrorDataResult<>(submissionRulesResult.getMessage());

        ContentModerationResult moderation = plateModerationService.resolveModeration(user, normalizedComment);
        if (!moderation.isAllowed()) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.REVIEW_CONTENT_NOT_ALLOWED));
        }

        Plate plate = plateSearchService.getOrCreatePlate(normalizedPlate);
        Result visibilityResult = plateSearchService.checkIfPlatePubliclyVisible(plate);
        if (!visibilityResult.isSuccess()) return new ErrorDataResult<>(visibilityResult.getMessage());

        PlateReview existingReview = plateReviewDao.findByPlateIdAndUserId(plate.getId(), currentUserId).orElse(null);
        Result existingReviewCheck = checkExistingReviewStatus(existingReview);
        if (!existingReviewCheck.isSuccess()) return new ErrorDataResult<>(existingReviewCheck.getMessage());

        if (request.getReportTypeCodes() != null) {
            Result syncResult = plateReportService.syncReportsForUserAndPlate(plate.getId(), currentUserId, request.getReportTypeCodes());
            if (!syncResult.isSuccess()) return new ErrorDataResult<>(syncResult.getMessage());
        }

        PlateReview savedReview;
        if (existingReview != null) {
            savedReview = resubmitRejectedReview(existingReview, request, moderation, currentUserId, plate);
        } else {
            savedReview = submitNewReview(plate, user, request, moderation, currentUserId);
        }

        ReviewResponseDto responseDto = buildReviewResponse(savedReview, plate, user);
        return new SuccessDataResult<>(responseDto, resolveReviewSuccessMessage(existingReview != null, savedReview.getStatus()));
    }

    @Override
    @Transactional
    public Result updateReview(Long reviewId, Long currentUserId, UpdatePlateReviewRequest request) {
        PlateReview review = plateReviewDao.findById(reviewId).orElse(null);
        String normalizedComment = normalizeComment(request.getComment());

        Result validationResult = validateUpdateReview(review, currentUserId, request, normalizedComment);
        if (validationResult != null) return validationResult;

        ContentModerationResult moderation = plateModerationService.resolveModeration(review.getUser(), normalizedComment);
        if (!moderation.isAllowed()) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_CONTENT_NOT_ALLOWED));
        }

        if (request.getReportTypeCodes() != null) {
            Result syncResult = plateReportService.syncReportsForUserAndPlate(
                    review.getPlate().getId(),
                    currentUserId,
                    request.getReportTypeCodes()
            );
            if (!syncResult.isSuccess()) return syncResult;
        }

        PlateReviewStatus previousStatus = review.getStatus();
        applyReviewMutation(
                review,
                request.getRating(),
                moderation,
                resolveResponsibilityAcceptance(request.getAcceptedResponsibility()),
                request.getResponsibilityPolicyVersion()
        );
        plateReviewDao.save(review);
        plateModerationService.logReviewSubmitted(
                review,
                previousStatus == null ? null : previousStatus.getId(),
                currentUserId,
                com.mefy.platemate.business.utilities.constants.ModerationConstants.USER_UPDATED_REVIEW
        );
        refreshPlateStatistics(review.getPlate());

        return new SuccessResult(resolveReviewSuccessMessage(true, review.getStatus()));
    }

    private Result validateUpdateReview(
            PlateReview review,
            Long currentUserId,
            UpdatePlateReviewRequest request,
            String normalizedComment
    ) {
        Result result = BusinessRules.run(
                checkIfReviewExists(review),
                checkIfReviewOwner(review, currentUserId),
                checkIfResponsibilityAccepted(request.getAcceptedResponsibility())
        );
        if (result != null) return result;

        Result visibilityResult = plateSearchService.checkIfPlatePubliclyVisible(review.getPlate());
        if (!visibilityResult.isSuccess()) return visibilityResult;

        Result submissionRulesResult = validateSubmissionRules(review.getUser(), normalizedComment, request.getReportTypeCodes());
        if (submissionRulesResult != null) return submissionRulesResult;

        return null;
    }

    @Override
    @Transactional
    public Result deleteReview(Long reviewId, Long currentUserId) {
        PlateReview review = plateReviewDao.findById(reviewId).orElse(null);

        Result result = BusinessRules.run(
                checkIfReviewExists(review),
                checkIfReviewOwner(review, currentUserId)
        );
        if (result != null) return result;

        Plate plate = review.getPlate();
        PlateReviewStatus previousStatus = review.getStatus();
        review.setStatus(PlateReviewStatus.REMOVED_BY_USER);
        review.setDeletedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
        plateModerationService.logReviewRemovedByUser(
                review,
                previousStatus == null ? null : previousStatus.getId(),
                currentUserId
        );
        refreshPlateStatistics(plate);

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_DELETED));
    }

    private void refreshPlateStatistics(Plate plate) {
        if (plate == null || plate.getId() == null) return;

        long reviewCountLong = plateReviewDao.countByPlateIdAndStatusId(plate.getId(), approvedStatusId());
        int reviewCount = reviewCountLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) reviewCountLong;
        Long ratingSum = plateReviewDao.sumRatingByPlateIdAndStatus(plate.getId(), approvedStatusId());
        long totalRatingSum = ratingSum == null ? 0L : ratingSum;

        plate.setReviewCount(reviewCount);
        plate.setTotalRatingSum(totalRatingSum);
        plate.setRatingAverage(reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
    }

    private Result checkIfUserExists(User user) {
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfResponsibilityAccepted(Boolean acceptedResponsibility) {
        if (Boolean.FALSE.equals(acceptedResponsibility)) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_RESPONSIBILITY_REQUIRED));
        }
        if (acceptedResponsibility == null && !acceptedResponsibilityLegacyFallback) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_RESPONSIBILITY_REQUIRED));
        }
        return new SuccessResult();
    }

    private Result checkIfReviewExists(PlateReview review) {
        if (review == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfReviewOwner(PlateReview review, Long currentUserId) {
        if (review == null) return new SuccessResult();
        if (!review.getUser().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_DELETE_UNAUTHORIZED));
        }
        return new SuccessResult();
    }

    private boolean resolveResponsibilityAcceptance(Boolean acceptedResponsibility) {
        if (acceptedResponsibility != null) {
            return acceptedResponsibility;
        }
        return acceptedResponsibilityLegacyFallback;
    }

    private String resolveResponsibilityPolicyVersion(String responsibilityPolicyVersion) {
        if (responsibilityPolicyVersion == null || responsibilityPolicyVersion.isBlank()) {
            return "v1";
        }
        return responsibilityPolicyVersion.trim();
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return "";
        }
        return comment.trim();
    }

    private Result validateSubmissionRules(User user, String normalizedComment, List<String> reportTypeCodes) {
        if (user != null && user.isPremiumActive()) {
            return null;
        }

        if (normalizedComment != null && !normalizedComment.isBlank()) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_COMMENT_PREMIUM_REQUIRED));
        }

        if (reportTypeCodes == null || reportTypeCodes.isEmpty()) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_REPORT_TYPE_REQUIRED_FOR_NON_PREMIUM));
        }

        return null;
    }

    private void applyReviewMutation(
            PlateReview review,
            Integer rating,
            ContentModerationResult moderation,
            boolean acceptedResponsibility,
            String responsibilityPolicyVersion
    ) {
        LocalDateTime now = LocalDateTime.now();
        review.setRating(rating);
        review.setDeletedAt(null);
        review.setUserAcceptedResponsibility(acceptedResponsibility);
        review.setUserAcceptedResponsibilityAt(acceptedResponsibility ? now : null);
        review.setUpdatedAt(now);
        
        plateModerationService.applyModerationMetadata(review, moderation, responsibilityPolicyVersion);
    }

    private String resolveReviewSuccessMessage(boolean isUpdate, PlateReviewStatus status) {
        if (status == PlateReviewStatus.PENDING_REVIEW) {
            return messageService.getMessage(Messages.REVIEW_PENDING_REVIEW);
        }
        return messageService.getMessage(isUpdate ? Messages.REVIEW_UPDATED : Messages.REVIEW_ADDED);
    }

    private Long approvedStatusId() {
        return PlateReviewStatus.APPROVED.getId();
    }

    private Result validateAddReviewInput(String normalizedPlate, User user, AddPlateReviewRequest request) {
        return BusinessRules.run(
                plateSearchService.checkIfPlateValid(normalizedPlate),
                checkIfUserExists(user),
                checkIfResponsibilityAccepted(request.getAcceptedResponsibility())
        );
    }

    private Result checkExistingReviewStatus(PlateReview existingReview) {
        if (existingReview != null && existingReview.getStatus() != PlateReviewStatus.REJECTED) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE));
        }
        return new SuccessResult();
    }

    private PlateReview resubmitRejectedReview(
            PlateReview existingReview,
            AddPlateReviewRequest request,
            ContentModerationResult moderation,
            Long currentUserId,
            Plate plate
    ) {
        PlateReviewStatus previousStatus = existingReview.getStatus();
        applyReviewMutation(
                existingReview,
                request.getRating(),
                moderation,
                resolveResponsibilityAcceptance(request.getAcceptedResponsibility()),
                request.getResponsibilityPolicyVersion()
        );
        plateReviewDao.save(existingReview);
        plateModerationService.logReviewSubmitted(
                existingReview,
                previousStatus == null ? null : previousStatus.getId(),
                currentUserId,
                com.mefy.platemate.business.utilities.constants.ModerationConstants.USER_RESUBMITTED_REJECTED_REVIEW
        );
        refreshPlateStatistics(plate);
        return existingReview;
    }

    private PlateReview submitNewReview(
            Plate plate,
            User user,
            AddPlateReviewRequest request,
            ContentModerationResult moderation,
            Long currentUserId
    ) {
        PlateReview review = new PlateReview();
        review.setPlate(plate);
        review.setUser(user);
        applyReviewMutation(
                review,
                request.getRating(),
                moderation,
                resolveResponsibilityAcceptance(request.getAcceptedResponsibility()),
                request.getResponsibilityPolicyVersion()
        );
        review.setCreatedAt(LocalDateTime.now());
        PlateReview savedReview = plateReviewDao.save(review);
        PlateReview persistedReview = savedReview == null ? review : savedReview;
        plateModerationService.logReviewSubmitted(
                persistedReview,
                null,
                currentUserId,
                com.mefy.platemate.business.utilities.constants.ModerationConstants.USER_SUBMITTED_REVIEW
        );
        refreshPlateStatistics(plate);
        return persistedReview;
    }

    private ReviewResponseDto buildReviewResponse(PlateReview review, Plate plate, User user) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setReviewId(review.getId());
        dto.setPlateCode(plate.getPlateCode());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setStatus(review.getStatusCode());
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        UserProfile profile = user.getProfile();
        if (profile != null) {
            dto.setDisplayName(profile.getDisplayName());
            dto.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        }

        List<PlateReport> activeReports = plateReportDao.findByPlateIdAndUserIdAndActiveTrue(plate.getId(), user.getId());
        List<String> reportCodes = activeReports.stream()
                .map(r -> r.getReportType().getCode())
                .toList();
        dto.setReportTypeCodes(reportCodes);

        return dto;
    }
}

