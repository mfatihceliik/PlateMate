package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.plate.concrete.PlateStatisticsService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportReasonDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.concrete.CommentReportReason;
import com.mefy.platemate.entities.concrete.CommentReport;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.request.AddCommentReportRequest;
import com.mefy.platemate.entities.dto.request.ReviewCommentReportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentReportManagerTest {

    @Mock
    private ICommentReportDao commentReportDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private PlateStatisticsService plateStatisticsService;
    @Mock
    private PlateReviewModerationEventService moderationEventService;
    @Mock
    private IAppSettingsService appSettingsService;
    @Mock
    private IMessageService messageService;
    @Mock
    private com.mefy.platemate.business.utilities.mappers.CommentReportMapper commentReportMapper;
    @Mock
    private ICommentReportReasonDao commentReportReasonDao;

    private CommentReportManager manager;

    @BeforeEach
    void setUp() {
        manager = new CommentReportManager(
                commentReportDao,
                plateReviewDao,
                userDao,
                plateStatisticsService,
                moderationEventService,
                appSettingsService,
                commentReportMapper,
                messageService,
                commentReportReasonDao
        );
    }

    private CommentReportReason reason(Long id, String code, boolean active) {
        CommentReportReason reason = new CommentReportReason();
        reason.setId(id);
        reason.setCode(code);
        reason.setLabel(code);
        reason.setSortOrder(1);
        reason.setActive(active);
        return reason;
    }

    @Test
    void addReportRejectsDuplicateReportFromSameUser() {
        PlateReview review = new PlateReview();
        review.setId(11L);

        when(commentReportReasonDao.findByCode("SPAM")).thenReturn(Optional.of(reason(6L, "SPAM", true)));
        when(userDao.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(new com.mefy.platemate.entities.concrete.User()));
        when(plateReviewDao.findById(11L)).thenReturn(Optional.of(review));
        when(commentReportDao.existsByCommentIdAndReporterUserId(11L, 5L)).thenReturn(true);
        when(messageService.getMessage("comment.report.duplicate")).thenReturn("duplicate");

        Result result = manager.addReport(11L, 5L, new AddCommentReportRequest(null, "SPAM", "x"));

        assertFalse(result.isSuccess());
        assertEquals("duplicate", result.getMessage());
        verify(commentReportDao, never()).save(any());
    }

    @Test
    void addReportRejectsInactiveReason() {
        PlateReview review = new PlateReview();
        review.setId(11L);

        when(commentReportReasonDao.findByCode("SPAM")).thenReturn(Optional.of(reason(6L, "SPAM", false)));
        when(plateReviewDao.findById(11L)).thenReturn(Optional.of(review));
        when(messageService.getMessage("report.type.invalid")).thenReturn("invalid");

        Result result = manager.addReport(11L, 5L, new AddCommentReportRequest(null, "SPAM", "x"));

        assertFalse(result.isSuccess());
        assertEquals("invalid", result.getMessage());
        verify(commentReportDao, never()).save(any());
    }

    @Test
    void addReportAutoMovesCommentToPendingWhenThresholdReached() {
        Plate plate = new Plate();
        plate.setId(90L);
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview review = new PlateReview();
        review.setId(12L);
        review.setPlate(plate);
        review.setStatus(PlateReviewStatus.APPROVED);
        review.setReportCount(2);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(commentReportReasonDao.findByCode("INSULT")).thenReturn(Optional.of(reason(2L, "INSULT", true)));
        when(userDao.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(new com.mefy.platemate.entities.concrete.User()));
        when(plateReviewDao.findById(12L)).thenReturn(Optional.of(review));
        when(commentReportDao.existsByCommentIdAndReporterUserId(12L, 7L)).thenReturn(false);
        when(appSettingsService.getInt(AppSettingKey.COMMENT_REPORT_THRESHOLD)).thenReturn(3);
        when(messageService.getMessage("comment.report.created")).thenReturn("created");

        Result result = manager.addReport(12L, 7L, new AddCommentReportRequest(null, "INSULT", "x"));

        assertTrue(result.isSuccess());
        ArgumentCaptor<CommentReport> reportCaptor = ArgumentCaptor.forClass(CommentReport.class);
        verify(commentReportDao).save(reportCaptor.capture());
        assertEquals(2L, reportCaptor.getValue().getReasonId());
        ArgumentCaptor<PlateReview> reviewCaptor = ArgumentCaptor.forClass(PlateReview.class);
        verify(plateReviewDao).save(reviewCaptor.capture());
        assertEquals(3, reviewCaptor.getValue().getReportCount());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, reviewCaptor.getValue().getStatus());
        verify(moderationEventService).logEvent(
                eq(review),
                eq(PlateReviewStatus.APPROVED.getId()),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewModerationActionType.AUTO_PENDING_BY_REPORT_THRESHOLD),
                eq(7L),
                eq("AUTO_PENDING_BY_REPORT_THRESHOLD")
        );
        verify(plateStatisticsService).refresh(eq(plate));
    }

    @Test
    void reviewReportAcceptedRemovesCommentAndLogsModerationEvent() {
        Plate plate = new Plate();
        plate.setId(90L);
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview comment = new PlateReview();
        comment.setId(77L);
        comment.setPlate(plate);
        comment.setStatus(PlateReviewStatus.PENDING_REVIEW);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        CommentReport report = new CommentReport();
        report.setId(21L);
        report.setComment(comment);
        report.setReporterUserId(7L);
        report.setReasonId(6L);
        report.setStatus(CommentReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        when(commentReportDao.findById(21L)).thenReturn(Optional.of(report));
        when(messageService.getMessage("comment.report.reviewed")).thenReturn("reviewed");

        Result result = manager.reviewReport(
                21L,
                100L,
                new ReviewCommentReportRequest(CommentReportStatus.ACCEPTED, "uygunsuz")
        );

        assertTrue(result.isSuccess());
        assertEquals("reviewed", result.getMessage());
        assertEquals(PlateReviewStatus.REMOVED_BY_MODERATOR, comment.getStatus());
        verify(moderationEventService).logEvent(
                eq(comment),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewStatus.REMOVED_BY_MODERATOR.getId()),
                eq(PlateReviewModerationActionType.REMOVED_BY_MODERATOR),
                eq(100L),
                eq("REMOVED_BY_ACCEPTED_REPORT")
        );
        verify(plateStatisticsService).refresh(eq(plate));
    }
}
