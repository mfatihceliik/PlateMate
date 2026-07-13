package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.abstracts.ICommentReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.CommentReportMapper;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.plate.concrete.PlateStatisticsService;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportReasonDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.concrete.CommentReport;
import com.mefy.platemate.entities.concrete.CommentReportReason;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.dto.CommentReportDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportRequest;
import com.mefy.platemate.entities.dto.request.ReviewCommentReportRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentReportManager implements ICommentReportService {

    private final ICommentReportDao commentReportDao;
    private final IPlateReviewDao plateReviewDao;
    private final IUserDao userDao;
    private final PlateStatisticsService plateStatisticsService;
    private final PlateReviewModerationEventService moderationEventService;
    private final IAppSettingsService appSettingsService;
    private final CommentReportMapper commentReportMapper;
    private final IMessageService messageService;
    private final ICommentReportReasonDao commentReportReasonDao;



    @Override
    @Transactional
    public Result addReport(Long commentId, Long reporterUserId, AddCommentReportRequest request) {
        CommentReportReason reason = resolveActiveReason(request);
        PlateReview comment = plateReviewDao.findById(commentId).orElse(null);

        Result validationResult = BusinessRules.run(
                validateAddReportInput(commentId, reporterUserId, request, reason),
                checkCommentExists(comment),
                checkDuplicateReport(commentId, reporterUserId)
        );
        if (validationResult != null) return validationResult;

        createAndSaveReport(comment, reporterUserId, reason, request.getDescription());
        handleReportThresholdAndSave(comment, reporterUserId);

        return new SuccessResult(messageService.getMessage(Messages.COMMENT_REPORT_CREATED));
    }

    private CommentReportReason resolveActiveReason(AddCommentReportRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getReasonId() != null) {
            return commentReportReasonDao.findById(request.getReasonId())
                    .filter(CommentReportReason::isActive)
                    .orElse(null);
        }
        if (request.getReasonCode() != null) {
            return commentReportReasonDao.findByCode(request.getReasonCode())
                    .filter(CommentReportReason::isActive)
                    .orElse(null);
        }
        return null;
    }

    private Result checkCommentExists(PlateReview comment) {
        if (comment == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }
        return new SuccessResult();
    }

    @Override
    @Transactional
    public DataResult<PagedData<CommentReportDto>> getReports(PaginationRequest paginationRequest) {
        var pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = commentReportDao.findAll(pageable).map(commentReportMapper::entityToDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage(Messages.COMMENT_REPORTS_LISTED)
        );
    }

    @Override
    @Transactional
    public Result reviewReport(Long reportId, Long reviewerUserId, ReviewCommentReportRequest request) {
        CommentReport report = commentReportDao.findById(reportId).orElse(null);
        CommentReportStatus nextStatus = request == null ? null : CommentReportStatus.resolve(request.getStatusId(), request.getStatusCode());

        Result validationResult = validateReviewReportRequest(report, nextStatus);
        if (validationResult != null) return validationResult;

        updateReportStatus(report, nextStatus, request.getAdminNote(), reviewerUserId);

        if (nextStatus == CommentReportStatus.ACCEPTED) {
            removeReportedComment(report.getComment(), reviewerUserId);
        }

        return new SuccessResult(messageService.getMessage(Messages.COMMENT_REPORT_REVIEWED));
    }

    private Result validateAddReportInput(Long commentId, Long reporterUserId, AddCommentReportRequest request, CommentReportReason reason) {
        if (commentId == null || reporterUserId == null || request == null || reason == null) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_TYPE_INVALID));
        }
        if (userDao.findByIdAndActiveTrue(reporterUserId).isEmpty()) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkDuplicateReport(Long commentId, Long reporterUserId) {
        if (commentReportDao.existsByCommentIdAndReporterUserId(commentId, reporterUserId)) {
            return new ErrorResult(messageService.getMessage(Messages.COMMENT_REPORT_DUPLICATE));
        }
        return new SuccessResult();
    }

    private void createAndSaveReport(PlateReview comment, Long reporterUserId, CommentReportReason reason, String description) {
        LocalDateTime now = LocalDateTime.now();
        CommentReport report = new CommentReport();
        report.setComment(comment);
        report.setReporterUserId(reporterUserId);
        report.setReasonId(reason.getId());
        report.setDescription(description == null ? null : description.trim());
        report.setStatus(CommentReportStatus.OPEN);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        commentReportDao.save(report);
    }

    private void handleReportThresholdAndSave(PlateReview comment, Long reporterUserId) {
        int currentCount = comment.getReportCount() == null ? 0 : comment.getReportCount();
        comment.setReportCount(currentCount + 1);
        int commentReportThreshold = appSettingsService.getInt(AppSettingKey.COMMENT_REPORT_THRESHOLD);
        if (comment.getReportCount() >= commentReportThreshold
                && PlateReviewStatus.APPROVED.getId().equals(comment.getStatusId())) {
            PlateReviewStatus previousStatus = comment.getStatus();
            comment.setStatus(PlateReviewStatus.PENDING_REVIEW);
            String existing = comment.getModerationReason();
            String appended = com.mefy.platemate.business.utilities.constants.ModerationConstants.AUTO_PENDING_BY_REPORT_THRESHOLD;
            comment.setModerationReason(existing == null || existing.isBlank() ? appended : existing + "," + appended);
            moderationEventService.logEvent(
                    comment,
                    previousStatus == null ? null : previousStatus.getId(),
                    comment.getStatusId(),
                    PlateReviewModerationActionType.AUTO_PENDING_BY_REPORT_THRESHOLD,
                    reporterUserId,
                    appended
            );
            plateStatisticsService.refresh(comment.getPlate());
        }
        plateReviewDao.save(comment);
    }

    private Result validateReviewReportRequest(CommentReport report, CommentReportStatus nextStatus) {
        if (report == null) {
            return new ErrorResult(messageService.getMessage(Messages.COMMENT_REPORT_NOT_FOUND));
        }
        if (nextStatus == null || nextStatus == CommentReportStatus.OPEN) {
            return new ErrorResult(messageService.getMessage(Messages.COMMENT_REPORT_REVIEW_INVALID_STATUS));
        }
        return null;
    }

    private void updateReportStatus(CommentReport report, CommentReportStatus nextStatus, String adminNote, Long reviewerUserId) {
        LocalDateTime now = LocalDateTime.now();
        report.setStatus(nextStatus);
        report.setAdminNote(adminNote == null ? null : adminNote.trim());
        report.setReviewedBy(reviewerUserId);
        report.setReviewedAt(now);
        report.setUpdatedAt(now);
        commentReportDao.save(report);
    }

    private void removeReportedComment(PlateReview comment, Long reviewerUserId) {
        if (comment == null) return;
        LocalDateTime now = LocalDateTime.now();
        PlateReviewStatus previousStatus = comment.getStatus();
        comment.setStatus(PlateReviewStatus.REMOVED_BY_MODERATOR);
        comment.setDeletedAt(now);
        String existing = comment.getModerationReason();
        String appended = com.mefy.platemate.business.utilities.constants.ModerationConstants.REMOVED_BY_ACCEPTED_REPORT;
        comment.setModerationReason(existing == null || existing.isBlank() ? appended : existing + "," + appended);
        comment.setUpdatedAt(now);
        plateReviewDao.save(comment);
        moderationEventService.logEvent(
                comment,
                previousStatus == null ? null : previousStatus.getId(),
                comment.getStatusId(),
                PlateReviewModerationActionType.REMOVED_BY_MODERATOR,
                reviewerUserId,
                appended
        );
        plateStatisticsService.refresh(comment.getPlate());
    }
}
