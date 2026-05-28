package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IModerationAdminService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.PlateAdminDto;
import com.mefy.platemate.entities.dto.PlateReviewAdminDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModerationAdminManager implements IModerationAdminService {

    private final IPlateReviewDao plateReviewDao;
    private final IPlateDao plateDao;
    private final IPlateReportDao plateReportDao;
    private final PlateReviewModerationEventService moderationEventService;
    private final IMessageService messageService;

    @Override
    public DataResult<PagedData<PlateReviewAdminDto>> getPendingComments(PaginationRequest paginationRequest) {
        Pageable pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = plateReviewDao
                .findByStatusIdOrderByCreatedAtDesc(PlateReviewStatus.PENDING_REVIEW.getId(), pageable)
                .map(this::toAdminDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage("admin.comments.pending.listed")
        );
    }

    @Override
    @Transactional
    public Result approveComment(Long commentId, Long adminUserId) {
        PlateReview review = plateReviewDao.findById(commentId).orElse(null);
        Result existenceCheck = checkCommentExists(review);
        if (existenceCheck != null) return existenceCheck;

        PlateReviewStatus previousStatus = review.getStatus();
        applyApproveMutation(review);
        logModerationEventAndRefresh(review, previousStatus, PlateReviewModerationActionType.APPROVED_BY_ADMIN, adminUserId, "APPROVED_BY_ADMIN");

        return new SuccessResult(messageService.getMessage("admin.comment.approved"));
    }

    @Override
    @Transactional
    public Result rejectComment(Long commentId, Long adminUserId, String reason) {
        PlateReview review = plateReviewDao.findById(commentId).orElse(null);
        Result existenceCheck = checkCommentExists(review);
        if (existenceCheck != null) return existenceCheck;

        PlateReviewStatus previousStatus = review.getStatus();
        applyRejectMutation(review, reason);
        logModerationEventAndRefresh(review, previousStatus, PlateReviewModerationActionType.REJECTED_BY_ADMIN, adminUserId, reason);

        return new SuccessResult(messageService.getMessage("admin.comment.rejected"));
    }

    @Override
    @Transactional
    public Result removeComment(Long commentId, Long adminUserId, String reason) {
        PlateReview review = plateReviewDao.findById(commentId).orElse(null);
        Result existenceCheck = checkCommentExists(review);
        if (existenceCheck != null) return existenceCheck;

        PlateReviewStatus previousStatus = review.getStatus();
        applyRemoveMutation(review, reason);
        logModerationEventAndRefresh(review, previousStatus, PlateReviewModerationActionType.REMOVED_BY_MODERATOR, adminUserId, reason);

        return new SuccessResult(messageService.getMessage("admin.comment.removed"));
    }

    private Result checkCommentExists(PlateReview review) {
        if (review == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }
        return null;
    }

    private void applyApproveMutation(PlateReview review) {
        review.setStatus(PlateReviewStatus.APPROVED);
        review.setModerationReason("APPROVED_BY_ADMIN");
        review.setDeletedAt(null);
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
    }

    private void applyRejectMutation(PlateReview review, String reason) {
        review.setStatus(PlateReviewStatus.REJECTED);
        review.setModerationReason(resolveReason("REJECTED_BY_ADMIN", reason));
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
    }

    private void applyRemoveMutation(PlateReview review, String reason) {
        review.setStatus(PlateReviewStatus.REMOVED_BY_MODERATOR);
        review.setModerationReason(resolveReason("REMOVED_BY_MODERATOR", reason));
        review.setDeletedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
    }

    private void logModerationEventAndRefresh(
            PlateReview review,
            PlateReviewStatus previousStatus,
            PlateReviewModerationActionType actionType,
            Long adminUserId,
            String details
    ) {
        moderationEventService.logEvent(
                review,
                previousStatus == null ? null : previousStatus.getId(),
                review.getStatusId(),
                actionType,
                adminUserId,
                details
        );
        refreshPlateStatistics(review.getPlate());
    }

    @Override
    public DataResult<PagedData<PlateAdminDto>> getHiddenPlates(PaginationRequest paginationRequest) {
        Pageable pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("updatedAt").descending()
        );
        List<PlateStatus> statuses = List.of(
                PlateStatus.HIDDEN_BY_REQUEST,
                PlateStatus.BLOCKED,
                PlateStatus.DELETED
        );
        var page = plateDao.findByStatusIn(statuses, pageable).map(this::toPlateAdminDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage("admin.plates.hidden.listed")
        );
    }

    @Override
    @Transactional
    public Result hidePlate(Long plateId, Long adminUserId, String reason) {
        Plate plate = plateDao.findById(plateId).orElse(null);
        if (plate == null) {
            return new ErrorResult(messageService.getMessage("plate.not.found"));
        }

        plate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);
        plate.setHiddenReason(reason);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
        return new SuccessResult(messageService.getMessage("admin.plate.hidden"));
    }

    @Override
    @Transactional
    public Result restorePlate(Long plateId, Long adminUserId) {
        Plate plate = plateDao.findById(plateId).orElse(null);
        if (plate == null) {
            return new ErrorResult(messageService.getMessage("plate.not.found"));
        }

        plate.setStatus(PlateStatus.ACTIVE);
        plate.setHiddenReason(null);
        plate.setDeletedAt(null);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
        return new SuccessResult(messageService.getMessage("admin.plate.restored"));
    }

    private PlateReviewAdminDto toAdminDto(PlateReview review) {
        return new PlateReviewAdminDto(
                review.getId(),
                review.getPlate() == null ? null : review.getPlate().getPlateCode(),
                review.getUser() == null ? null : review.getUser().getId(),
                review.getUser() == null ? null : review.getUser().getUsername(),
                review.getRating(),
                review.getComment(),
                review.getStatusId(),
                review.getStatusCode(),
                review.getModerationReason(),
                review.getReportCount(),
                review.getUserAcceptedResponsibility(),
                review.getResponsibilityPolicyVersion(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getDeletedAt()
        );
    }

    private PlateAdminDto toPlateAdminDto(Plate plate) {
        int reportCount = plate == null || plate.getId() == null
                ? 0
                : toSafeInt(plateReportDao.countByPlateIdAndActiveTrue(plate.getId()));
        return new PlateAdminDto(
                plate.getId(),
                plate.getPlateCode(),
                plate.getStatusId(),
                plate.getStatusCode(),
                plate.getHiddenReason(),
                plate.getReviewCount(),
                reportCount,
                plate.getCreatedAt(),
                plate.getUpdatedAt(),
                plate.getDeletedAt()
        );
    }

    private String resolveReason(String base, String reason) {
        if (reason == null || reason.isBlank()) return base;
        return base + ":" + reason.trim();
    }

    private void refreshPlateStatistics(Plate plate) {
        if (plate == null || plate.getId() == null) return;

        long reviewCountLong = plateReviewDao.countByPlateIdAndStatusId(plate.getId(), PlateReviewStatus.APPROVED.getId());
        int reviewCount = reviewCountLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) reviewCountLong;
        long totalRatingSum = safeLong(plateReviewDao.sumRatingByPlateIdAndStatus(plate.getId(), PlateReviewStatus.APPROVED.getId()));

        plate.setReviewCount(reviewCount);
        plate.setTotalRatingSum(totalRatingSum);
        plate.setRatingAverage(reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int toSafeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
