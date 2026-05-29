package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.abstracts.IPlateService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.moderation.ContentModerationResult;
import com.mefy.platemate.business.utilities.moderation.ContentModerationService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.security.HashingService;
import com.mefy.platemate.core.utilities.mappers.PlateReportTypeMapper;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
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
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.SyncPlateReportsRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlateManager implements IPlateService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateSearchEventDao plateSearchEventDao;
    private final IUserDao userDao;
    private final ICityDao cityDao;
    private final IPlateReportService plateReportService;
    private final IPlateReportDao plateReportDao;
    private final PlateReportTypeMapper plateReportTypeMapper;
    private final PlateReviewMapper plateReviewMapper;
    private final ContentModerationService contentModerationService;
    private final HashingService hashingService;
    private final IPlateValidator plateValidator;
    private final TrPlateCityResolver plateCityResolver;
    private final PlateReviewModerationEventService moderationEventService;
    private final IMessageService messageService;

    @Value("${moderation.accepted-responsibility-legacy-fallback:true}")
    private boolean acceptedResponsibilityLegacyFallback = true;

    @Override
    @Transactional
    public DataResult<PlateDetailDto> searchByPlateCode(String plateCode, Long currentUserId) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Plate plate = getOrCreatePlate(normalizedPlate);
        Result visibilityResult = checkIfPlatePubliclyVisible(plate);
        if (!visibilityResult.isSuccess()) {
            return new ErrorDataResult<>(visibilityResult.getMessage());
        }

        recordSearchEvent(plate, currentUserId);
        PlateDetailDto dto = buildPlateDetailDto(plate, normalizedPlate);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PLATE_FOUND));
    }

    @Override
    public DataResult<PagedData<PlateReviewDto>> getReviewsByPlateCode(
            String plateCode,
            PaginationRequest paginationRequest
    ) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Plate plate = plateDao.findByPlateCode(normalizedPlate).orElse(null);
        Result visibilityResult = checkIfPlatePubliclyVisible(plate);
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
    @Transactional
    public Result addReview(String plateCode, Long currentUserId, AddPlateReviewRequest request) {
        String normalizedPlate = normalizePlate(plateCode);
        User user = userDao.findByIdAndActiveTrue(currentUserId).orElse(null);

        Result validationResult = validateAddReviewInput(normalizedPlate, user, request);
        if (validationResult != null) return validationResult;

        String normalizedComment = normalizeComment(request.getComment());
        Result submissionRulesResult = validateSubmissionRules(user, normalizedComment, request.getReportTypeCodes());
        if (submissionRulesResult != null) return submissionRulesResult;

        ContentModerationResult moderation = resolveModeration(user, normalizedComment);
        if (!moderation.isAllowed()) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_CONTENT_NOT_ALLOWED));
        }

        Plate plate = getOrCreatePlate(normalizedPlate);
        Result visibilityResult = checkIfPlatePubliclyVisible(plate);
        if (!visibilityResult.isSuccess()) return visibilityResult;

        PlateReview existingReview = plateReviewDao.findByPlateIdAndUserId(plate.getId(), currentUserId).orElse(null);
        Result existingReviewCheck = checkExistingReviewStatus(existingReview);
        if (!existingReviewCheck.isSuccess()) return existingReviewCheck;

        if (request.getReportTypeCodes() != null) {
            Result syncResult = plateReportService.syncReportsForUserAndPlate(plate.getId(), currentUserId, request.getReportTypeCodes());
            if (!syncResult.isSuccess()) return syncResult;
        }

        if (existingReview != null) {
            return resubmitRejectedReview(existingReview, request, moderation, currentUserId, plate);
        }

        return submitNewReview(plate, user, request, moderation, currentUserId);
    }

    @Override
    @Transactional
    public Result updateReview(Long reviewId, Long currentUserId, UpdatePlateReviewRequest request) {
        PlateReview review = plateReviewDao.findById(reviewId).orElse(null);
        String normalizedComment = normalizeComment(request.getComment());

        Result validationResult = validateUpdateReview(review, currentUserId, request, normalizedComment);
        if (validationResult != null) return validationResult;

        ContentModerationResult moderation = resolveModeration(review.getUser(), normalizedComment);
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
        moderationEventService.logEvent(
                review,
                previousStatus == null ? null : previousStatus.getId(),
                review.getStatusId(),
                PlateReviewModerationActionType.SUBMITTED_FOR_REVIEW,
                currentUserId,
                "USER_UPDATED_REVIEW"
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

        Result visibilityResult = checkIfPlatePubliclyVisible(review.getPlate());
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
        moderationEventService.logEvent(
                review,
                previousStatus == null ? null : previousStatus.getId(),
                review.getStatusId(),
                PlateReviewModerationActionType.REMOVED_BY_USER,
                currentUserId,
                "USER_REMOVED_REVIEW"
        );
        refreshPlateStatistics(plate);

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_DELETED));
    }

    @Override
    @Transactional
    public Result syncReports(String plateCode, Long currentUserId, SyncPlateReportsRequest request) {
        String normalizedPlate = normalizePlate(plateCode);
        User user = userDao.findByIdAndActiveTrue(currentUserId).orElse(null);

        Result result = BusinessRules.run(
                checkIfPlateValid(normalizedPlate),
                checkIfUserExists(user)
        );
        if (result != null) return result;

        Plate plate = getOrCreatePlate(normalizedPlate);
        Result visibilityResult = checkIfPlatePubliclyVisible(plate);
        if (!visibilityResult.isSuccess()) return visibilityResult;
        return plateReportService.syncReportsForUserAndPlate(plate.getId(), currentUserId, request.getReportTypeCodes());
    }

    private Plate getOrCreatePlate(String normalizedPlate) {
        return plateDao.findByPlateCode(normalizedPlate).orElseGet(() -> createPlateSafely(normalizedPlate));
    }

    private Plate createPlateSafely(String normalizedPlate) {
        try {
            return plateDao.save(createPlate(normalizedPlate));
        } catch (DataIntegrityViolationException ex) {
            return plateDao.findByPlateCode(normalizedPlate).orElseThrow(() -> ex);
        }
    }

    private Plate createPlate(String normalizedPlate) {
        Plate plate = new Plate();
        plate.setPlateCode(normalizedPlate);
        plate.setStatus(PlateStatus.ACTIVE);
        plate.setRatingAverage(0.0);
        plate.setReviewCount(0);
        plate.setTotalRatingSum(0L);
        plate.setCreatedAt(LocalDateTime.now());
        plate.setUpdatedAt(LocalDateTime.now());
        plateCityResolver.resolveCityId(normalizedPlate).flatMap(cityDao::findById).ifPresent(plate::setCity);
        return plate;
    }

    private void refreshPlateStatistics(Plate plate) {
        if (plate == null || plate.getId() == null) return;

        long reviewCountLong = plateReviewDao.countByPlateIdAndStatusId(plate.getId(), approvedStatusId());
        int reviewCount = reviewCountLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) reviewCountLong;
        long totalRatingSum = safeLong(plateReviewDao.sumRatingByPlateIdAndStatus(plate.getId(), approvedStatusId()));

        plate.setReviewCount(reviewCount);
        plate.setTotalRatingSum(totalRatingSum);
        plate.setRatingAverage(reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
    }

    private void recordSearchEvent(Plate plate, Long userId) {
        if (plate == null || plate.getId() == null) return;
        LocalDateTime now = LocalDateTime.now();
        PlateSearchEvent event = new PlateSearchEvent();
        event.setPlate(plate);
        event.setUserId(userId);
        event.setSearchedAt(now);
        event.setCreatedAt(now);
        plateSearchEventDao.save(event);
    }

    private PlateDetailDto buildPlateDetailDto(Plate plate, String normalizedPlate) {
        PlateDetailDto dto = new PlateDetailDto();
        dto.setId(plate.getId());
        dto.setPlateCode(plate.getPlateCode());
        dto.setCityName(
                plate.getCity() != null
                        ? plate.getCity().getName()
                        : plateCityResolver.resolveCityName(normalizedPlate).orElse(null)
        );
        dto.setRatingAverage(plate.getRatingAverage() == null ? 0.0 : plate.getRatingAverage());
        dto.setReviewCount(plate.getReviewCount() == null ? 0 : plate.getReviewCount());
        dto.setTotalRatingSum(plate.getTotalRatingSum() == null ? 0L : plate.getTotalRatingSum());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        List<PlateReviewDto> reviews = plateReviewDao
                .findByPlatePlateCodeAndStatusId(normalizedPlate, approvedStatusId(), pageable)
                .map(plateReviewMapper::entityToDto)
                .getContent();
        dto.setRecentReviews(reviews);

        List<PlateReport> reports = plateReportDao
                .findByPlateIdInAndActiveTrue(java.util.List.of(plate.getId()));
        List<PlateReportTypeDto> reportTypes = reports.stream()
                .map(r -> plateReportTypeMapper.entityToDto(r.getReportType()))
                .distinct()
                .toList();
        dto.setRecentReportTypes(reportTypes);

        populateTotalMetrics(plate, dto);
        return dto;
    }

    private void populateTotalMetrics(Plate plate, PlateDetailDto dto) {
        if (plate == null || plate.getId() == null) {
            dto.setTotalSearchCount(0L);
            dto.setTotalReviewCount(0L);
            dto.setTotalReportCount(0L);
            dto.setTotalWeightedReportScore(0L);
            dto.setScore(0.0);
            dto.setLastActivityAt(null);
            return;
        }

        long totalSearchCount = plateSearchEventDao.countByPlateId(plate.getId());
        long totalReviewCount = plateReviewDao.countByPlateIdAndStatusId(plate.getId(), approvedStatusId());
        long totalReportCount = plateReportDao.countByPlateIdAndActiveTrue(plate.getId());
        long totalWeightedReportScore = safeLong(plateReportDao.getWeightedScoreByPlateId(plate.getId()));

        LocalDateTime lastActivityAt = maxDate(
                plateSearchEventDao.findLastSearchedAtByPlateId(plate.getId()),
                plateReviewDao.findLastReviewAtByPlateIdAndStatus(plate.getId(), approvedStatusId()),
                plateReportDao.findLastReportedAtByPlateId(plate.getId()),
                plate.getUpdatedAt()
        );

        double score = totalSearchCount
                + (totalReviewCount * 2.0)
                + (totalWeightedReportScore * 3.0);

        dto.setTotalSearchCount(totalSearchCount);
        dto.setTotalReviewCount(totalReviewCount);
        dto.setTotalReportCount(totalReportCount);
        dto.setTotalWeightedReportScore(totalWeightedReportScore);
        dto.setScore(score);
        dto.setLastActivityAt(lastActivityAt);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private LocalDateTime maxDate(LocalDateTime... values) {
        LocalDateTime max = null;
        for (LocalDateTime value : values) {
            if (value == null) {
                continue;
            }
            if (max == null || value.isAfter(max)) {
                max = value;
            }
        }
        return max;
    }

    private String normalizePlate(String plateCode) {
        if (plateCode == null) return "";
        return plateCode.replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private Result checkIfPlateValid(String plateCode) {
        if (!plateValidator.isValid(plateCode)) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_INVALID));
        }
        return new SuccessResult();
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

    private Result checkIfPlatePubliclyVisible(Plate plate) {
        if (plate == null || plate.getStatus() != PlateStatus.ACTIVE) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_NOT_AVAILABLE));
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

    private ContentModerationResult resolveModeration(User user, String normalizedComment) {
        if (normalizedComment == null || normalizedComment.isBlank()) {
            return new ContentModerationResult(true, false, List.of(), "");
        }

        if (user != null && user.isPremiumActive()) {
            return contentModerationService.moderate(normalizedComment);
        }

        return new ContentModerationResult(false, false, List.of("NON_PREMIUM_TEXT_COMMENT_NOT_ALLOWED"), normalizedComment);
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
        review.setComment(moderation.getSanitizedText());
        review.setStatus(resolveModerationStatus(moderation));
        review.setModerationReason(moderation.isRequiresReview()
                ? String.join(",", moderation.getReasons())
                : null);
        review.setDeletedAt(null);
        review.setUserAcceptedResponsibility(acceptedResponsibility);
        review.setUserAcceptedResponsibilityAt(acceptedResponsibility ? now : null);
        review.setResponsibilityPolicyVersion(resolveResponsibilityPolicyVersion(responsibilityPolicyVersion));
        RequestMetadata requestMetadata = resolveRequestMetadata();
        review.setIpHash(requestMetadata.ipHash());
        review.setUserAgentHash(requestMetadata.userAgentHash());
        review.setUpdatedAt(now);
    }

    private PlateReviewStatus resolveModerationStatus(ContentModerationResult moderation) {
        return PlateReviewStatus.PENDING_REVIEW;
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

    private RequestMetadata resolveRequestMetadata() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return new RequestMetadata(null, null);
        }
        HttpServletRequest request = requestAttributes.getRequest();
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return new RequestMetadata(hashingService.sha256(clientIp), hashingService.sha256(userAgent));
    }

    private Result validateAddReviewInput(String normalizedPlate, User user, AddPlateReviewRequest request) {
        return BusinessRules.run(
                checkIfPlateValid(normalizedPlate),
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

    private Result resubmitRejectedReview(
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
        moderationEventService.logEvent(
                existingReview,
                previousStatus == null ? null : previousStatus.getId(),
                existingReview.getStatusId(),
                PlateReviewModerationActionType.SUBMITTED_FOR_REVIEW,
                currentUserId,
                "USER_RESUBMITTED_REJECTED_REVIEW"
        );
        refreshPlateStatistics(plate);
        return new SuccessResult(resolveReviewSuccessMessage(true, existingReview.getStatus()));
    }

    private Result submitNewReview(
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
        moderationEventService.logEvent(
                persistedReview,
                null,
                persistedReview.getStatusId(),
                PlateReviewModerationActionType.SUBMITTED_FOR_REVIEW,
                currentUserId,
                "USER_SUBMITTED_REVIEW"
        );
        refreshPlateStatistics(plate);

        return new SuccessResult(resolveReviewSuccessMessage(false, review.getStatus()));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RequestMetadata(String ipHash, String userAgentHash) {
    }
}
