package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ICommentReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.CommentReport;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.dto.CommentReportDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportRequest;
import com.mefy.platemate.entities.dto.request.ReviewCommentReportRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentReportManager implements ICommentReportService {

    private final ICommentReportDao commentReportDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateDao plateDao;
    private final IUserDao userDao;
    private final PlateReviewModerationEventService moderationEventService;
    private final com.mefy.platemate.core.utilities.messages.IMessageService messageService;

    @Value("${moderation.comment-report-threshold:3}")
    private int commentReportThreshold = 3;

    @Override
    @Transactional
    public Result addReport(Long commentId, Long reporterUserId, AddCommentReportRequest request) {
        if (commentId == null || reporterUserId == null || request == null || request.getReason() == null) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_TYPE_INVALID));
        }
        if (userDao.findByIdAndActiveTrue(reporterUserId).isEmpty()) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        PlateReview comment = plateReviewDao.findById(commentId).orElse(null);
        if (comment == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }

        if (commentReportDao.existsByCommentIdAndReporterUserId(commentId, reporterUserId)) {
            return new ErrorResult(messageService.getMessage("comment.report.duplicate"));
        }

        LocalDateTime now = LocalDateTime.now();
        CommentReport report = new CommentReport();
        report.setComment(comment);
        report.setReporterUserId(reporterUserId);
        report.setReason(request.getReason());
        report.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        report.setStatus(CommentReportStatus.OPEN);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        commentReportDao.save(report);

        int currentCount = comment.getReportCount() == null ? 0 : comment.getReportCount();
        comment.setReportCount(currentCount + 1);
        if (comment.getReportCount() >= commentReportThreshold && comment.getStatus() == PlateReviewStatus.APPROVED) {
            PlateReviewStatus previousStatus = comment.getStatus();
            comment.setStatus(PlateReviewStatus.PENDING_REVIEW);
            String existing = comment.getModerationReason();
            String appended = "AUTO_PENDING_BY_REPORT_THRESHOLD";
            comment.setModerationReason(existing == null || existing.isBlank() ? appended : existing + "," + appended);
            moderationEventService.logEvent(
                    comment,
                    previousStatus,
                    comment.getStatus(),
                    PlateReviewModerationActionType.AUTO_PENDING_BY_REPORT_THRESHOLD,
                    reporterUserId,
                    appended
            );
            refreshPlateStatistics(comment.getPlate());
        }
        plateReviewDao.save(comment);

        return new SuccessResult(messageService.getMessage("comment.report.created"));
    }

    @Override
    public DataResult<PagedData<CommentReportDto>> getReports(PaginationRequest paginationRequest) {
        var pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = commentReportDao.findAll(pageable).map(this::toDto);
        return new SuccessDataResult<>(
                PaginationMapper.fromPage(page),
                messageService.getMessage("comment.reports.listed")
        );
    }

    @Override
    @Transactional
    public Result reviewReport(Long reportId, Long reviewerUserId, ReviewCommentReportRequest request) {
        CommentReport report = commentReportDao.findById(reportId).orElse(null);
        if (report == null) {
            return new ErrorResult(messageService.getMessage("comment.report.not.found"));
        }

        if (request.getStatus() == CommentReportStatus.OPEN) {
            return new ErrorResult(messageService.getMessage("comment.report.review.invalid.status"));
        }

        LocalDateTime now = LocalDateTime.now();
        report.setStatus(request.getStatus());
        report.setAdminNote(request.getAdminNote() == null ? null : request.getAdminNote().trim());
        report.setReviewedBy(reviewerUserId);
        report.setReviewedAt(now);
        report.setUpdatedAt(now);
        commentReportDao.save(report);

        if (request.getStatus() == CommentReportStatus.ACCEPTED) {
            PlateReview comment = report.getComment();
            if (comment != null) {
                PlateReviewStatus previousStatus = comment.getStatus();
                comment.setStatus(PlateReviewStatus.REMOVED_BY_MODERATOR);
                comment.setDeletedAt(now);
                String existing = comment.getModerationReason();
                String appended = "REMOVED_BY_ACCEPTED_REPORT";
                comment.setModerationReason(existing == null || existing.isBlank() ? appended : existing + "," + appended);
                comment.setUpdatedAt(now);
                plateReviewDao.save(comment);
                moderationEventService.logEvent(
                        comment,
                        previousStatus,
                        comment.getStatus(),
                        PlateReviewModerationActionType.REMOVED_BY_MODERATOR,
                        reviewerUserId,
                        appended
                );
                refreshPlateStatistics(comment.getPlate());
            }
        }

        return new SuccessResult(messageService.getMessage("comment.report.reviewed"));
    }

    private CommentReportDto toDto(CommentReport report) {
        String plateCode = null;
        if (report.getComment() != null
                && report.getComment().getPlate() != null) {
            plateCode = report.getComment().getPlate().getPlateCode();
        }
        return new CommentReportDto(
                report.getId(),
                report.getComment() != null ? report.getComment().getId() : null,
                report.getReporterUserId(),
                plateCode,
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getAdminNote(),
                report.getCreatedAt(),
                report.getReviewedAt(),
                report.getReviewedBy()
        );
    }

    private void refreshPlateStatistics(Plate plate) {
        if (plate == null || plate.getId() == null) return;

        long reviewCountLong = plateReviewDao.countByPlateIdAndStatus(plate.getId(), PlateReviewStatus.APPROVED);
        int reviewCount = reviewCountLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) reviewCountLong;
        long totalRatingSum = safeLong(plateReviewDao.sumRatingByPlateIdAndStatus(plate.getId(), PlateReviewStatus.APPROVED));

        plate.setReviewCount(reviewCount);
        plate.setTotalRatingSum(totalRatingSum);
        plate.setRatingAverage(reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
