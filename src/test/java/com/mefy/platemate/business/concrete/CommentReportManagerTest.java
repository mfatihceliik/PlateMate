package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    private IPlateDao plateDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private PlateReviewModerationEventService moderationEventService;
    @Mock
    private IMessageService messageService;

    private CommentReportManager manager;

    @BeforeEach
    void setUp() {
        manager = new CommentReportManager(
                commentReportDao,
                plateReviewDao,
                plateDao,
                userDao,
                moderationEventService,
                messageService
        );
        ReflectionTestUtils.setField(manager, "commentReportThreshold", 3);
    }

    @Test
    void addReportRejectsDuplicateReportFromSameUser() {
        PlateReview review = new PlateReview();
        review.setId(11L);

        when(userDao.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(new com.mefy.platemate.entities.concrete.User()));
        when(plateReviewDao.findById(11L)).thenReturn(Optional.of(review));
        when(commentReportDao.existsByCommentIdAndReporterUserId(11L, 5L)).thenReturn(true);
        when(messageService.getMessage("comment.report.duplicate")).thenReturn("duplicate");

        Result result = manager.addReport(11L, 5L, new AddCommentReportRequest(CommentReportReason.SPAM, "x"));

        assertFalse(result.isSuccess());
        assertEquals("duplicate", result.getMessage());
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

        when(userDao.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(new com.mefy.platemate.entities.concrete.User()));
        when(plateReviewDao.findById(12L)).thenReturn(Optional.of(review));
        when(commentReportDao.existsByCommentIdAndReporterUserId(12L, 7L)).thenReturn(false);
        when(plateReviewDao.countByPlateIdAndStatus(90L, PlateReviewStatus.APPROVED)).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(90L, PlateReviewStatus.APPROVED)).thenReturn(0L);
        when(messageService.getMessage("comment.report.created")).thenReturn("created");

        Result result = manager.addReport(12L, 7L, new AddCommentReportRequest(CommentReportReason.INSULT, "x"));

        assertTrue(result.isSuccess());
        ArgumentCaptor<PlateReview> reviewCaptor = ArgumentCaptor.forClass(PlateReview.class);
        verify(plateReviewDao).save(reviewCaptor.capture());
        assertEquals(3, reviewCaptor.getValue().getReportCount());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, reviewCaptor.getValue().getStatus());
        verify(moderationEventService).logEvent(
                eq(review),
                eq(PlateReviewStatus.APPROVED),
                eq(PlateReviewStatus.PENDING_REVIEW),
                eq(PlateReviewModerationActionType.AUTO_PENDING_BY_REPORT_THRESHOLD),
                eq(7L),
                eq("AUTO_PENDING_BY_REPORT_THRESHOLD")
        );
        verify(plateDao).save(eq(plate));
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
        report.setReason(CommentReportReason.SPAM);
        report.setStatus(CommentReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        when(commentReportDao.findById(21L)).thenReturn(Optional.of(report));
        when(plateReviewDao.countByPlateIdAndStatus(90L, PlateReviewStatus.APPROVED)).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(90L, PlateReviewStatus.APPROVED)).thenReturn(0L);
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
                eq(PlateReviewStatus.PENDING_REVIEW),
                eq(PlateReviewStatus.REMOVED_BY_MODERATOR),
                eq(PlateReviewModerationActionType.REMOVED_BY_MODERATOR),
                eq(100L),
                eq("REMOVED_BY_ACCEPTED_REPORT")
        );
        verify(plateDao).save(eq(plate));
    }
}
