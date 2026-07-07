package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IModerationAdminService;
import com.mefy.platemate.business.abstracts.IPlateFollowService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.concrete.PlateStatisticsService;
import com.mefy.platemate.business.utilities.mappers.PlateReviewAdminMapper;
import com.mefy.platemate.business.utilities.mappers.PlateAdminMapper;
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
    private final PlateReviewModerationEventService moderationEventService;
    private final IMessageService messageService;
    private final IPlateFollowService plateFollowService;
    private final PlateStatisticsService plateStatisticsService;
    private final PlateReviewAdminMapper plateReviewAdminMapper;
    private final PlateAdminMapper plateAdminMapper;

    @Override
    public DataResult<PagedData<PlateReviewAdminDto>> getPendingComments(PaginationRequest paginationRequest) {
        Pageable pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = plateReviewDao
                .findByStatusIdOrderByCreatedAtDesc(PlateReviewStatus.PENDING_REVIEW.getId(), pageable)
                .map(plateReviewAdminMapper::entityToDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage(Messages.ADMIN_COMMENTS_PENDING_LISTED)
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
        logModerationEventAndRefresh(review, previousStatus, PlateReviewModerationActionType.APPROVED_BY_ADMIN, adminUserId, com.mefy.platemate.business.utilities.constants.ModerationConstants.APPROVED_BY_ADMIN);

        if (previousStatus != PlateReviewStatus.APPROVED) {
            plateFollowService.notifyReviewApproved(review);
        }

        return new SuccessResult(messageService.getMessage(Messages.ADMIN_COMMENT_APPROVED));
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

        return new SuccessResult(messageService.getMessage(Messages.ADMIN_COMMENT_REJECTED));
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

        return new SuccessResult(messageService.getMessage(Messages.ADMIN_COMMENT_REMOVED));
    }

    private Result checkCommentExists(PlateReview review) {
        if (review == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }
        return null;
    }

    private void applyApproveMutation(PlateReview review) {
        review.setStatus(PlateReviewStatus.APPROVED);
        review.setModerationReason(com.mefy.platemate.business.utilities.constants.ModerationConstants.APPROVED_BY_ADMIN);
        review.setDeletedAt(null);
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
    }

    private void applyRejectMutation(PlateReview review, String reason) {
        review.setStatus(PlateReviewStatus.REJECTED);
        review.setModerationReason(resolveReason(com.mefy.platemate.business.utilities.constants.ModerationConstants.REJECTED_BY_ADMIN, reason));
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
    }

    private void applyRemoveMutation(PlateReview review, String reason) {
        review.setStatus(PlateReviewStatus.REMOVED_BY_MODERATOR);
        review.setModerationReason(resolveReason(com.mefy.platemate.business.utilities.constants.ModerationConstants.REMOVED_BY_MODERATOR, reason));
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
        plateStatisticsService.refresh(review.getPlate());
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
        var page = plateDao.findByStatusIn(statuses, pageable).map(plateAdminMapper::entityToDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage(Messages.ADMIN_PLATES_HIDDEN_LISTED)
        );
    }

    @Override
    @Transactional
    public Result hidePlate(Long plateId, Long adminUserId, String reason) {
        Plate plate = plateDao.findById(plateId).orElse(null);
        if (plate == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_NOT_FOUND));
        }

        plate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);
        plate.setHiddenReason(reason);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
        return new SuccessResult(messageService.getMessage(Messages.ADMIN_PLATE_HIDDEN));
    }

    @Override
    @Transactional
    public Result restorePlate(Long plateId, Long adminUserId) {
        Plate plate = plateDao.findById(plateId).orElse(null);
        if (plate == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_NOT_FOUND));
        }

        plate.setStatus(PlateStatus.ACTIVE);
        plate.setHiddenReason(null);
        plate.setDeletedAt(null);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
        return new SuccessResult(messageService.getMessage(Messages.ADMIN_PLATE_RESTORED));
    }

    private String resolveReason(String base, String reason) {
        if (reason == null || reason.isBlank()) return base;
        return base + ":" + reason.trim();
    }
}
