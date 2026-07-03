package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateFollowService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeTranslationDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.PlateAdminDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationAdminManagerTest {

    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IPlateDao plateDao;
    @Mock
    private IPlateReportDao plateReportDao;
    @Mock
    private IPlateReportTypeTranslationDao translationDao;
    @Mock
    private PlateReviewModerationEventService moderationEventService;
    @Mock
    private IMessageService messageService;
    @Mock
    private IPlateFollowService plateFollowService;

    private ModerationAdminManager manager;

    @BeforeEach
    void setUp() {
        manager = new ModerationAdminManager(
                plateReviewDao,
                plateDao,
                plateReportDao,
                translationDao,
                moderationEventService,
                messageService,
                plateFollowService
        );
    }

    @Test
    void approveCommentUpdatesStatusAndLogsEvent() {
        Plate plate = new Plate();
        plate.setId(10L);
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview review = new PlateReview();
        review.setId(90L);
        review.setPlate(plate);
        review.setStatus(PlateReviewStatus.PENDING_REVIEW);

        when(plateReviewDao.findById(90L)).thenReturn(Optional.of(review));
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(5L);
        when(messageService.getMessage("admin.comment.approved")).thenReturn("approved");

        Result result = manager.approveComment(90L, 7L);

        assertTrue(result.isSuccess());
        assertEquals("approved", result.getMessage());

        ArgumentCaptor<PlateReview> reviewCaptor = ArgumentCaptor.forClass(PlateReview.class);
        verify(plateReviewDao).save(reviewCaptor.capture());
        assertEquals(PlateReviewStatus.APPROVED, reviewCaptor.getValue().getStatus());

        verify(moderationEventService).logEvent(
                eq(review),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewStatus.APPROVED.getId()),
                eq(PlateReviewModerationActionType.APPROVED_BY_ADMIN),
                eq(7L),
                eq("APPROVED_BY_ADMIN")
        );
    }

    @Test
    void rejectCommentUpdatesStatusAndLogsEvent() {
        Plate plate = new Plate();
        plate.setId(10L);
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview review = new PlateReview();
        review.setId(91L);
        review.setPlate(plate);
        review.setStatus(PlateReviewStatus.PENDING_REVIEW);

        when(plateReviewDao.findById(91L)).thenReturn(Optional.of(review));
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(messageService.getMessage("admin.comment.rejected")).thenReturn("rejected");

        Result result = manager.rejectComment(91L, 8L, "icerik kurali");

        assertTrue(result.isSuccess());
        assertEquals("rejected", result.getMessage());
        assertEquals(PlateReviewStatus.REJECTED, review.getStatus());
        verify(moderationEventService).logEvent(
                eq(review),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewStatus.REJECTED.getId()),
                eq(PlateReviewModerationActionType.REJECTED_BY_ADMIN),
                eq(8L),
                eq("icerik kurali")
        );
    }

    @Test
    void removeCommentUpdatesStatusAndLogsEvent() {
        Plate plate = new Plate();
        plate.setId(10L);
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview review = new PlateReview();
        review.setId(92L);
        review.setPlate(plate);
        review.setStatus(PlateReviewStatus.PENDING_REVIEW);

        when(plateReviewDao.findById(92L)).thenReturn(Optional.of(review));
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(messageService.getMessage("admin.comment.removed")).thenReturn("removed");

        Result result = manager.removeComment(92L, 9L, "hakaret");

        assertTrue(result.isSuccess());
        assertEquals("removed", result.getMessage());
        assertEquals(PlateReviewStatus.REMOVED_BY_MODERATOR, review.getStatus());
        verify(moderationEventService).logEvent(
                eq(review),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewStatus.REMOVED_BY_MODERATOR.getId()),
                eq(PlateReviewModerationActionType.REMOVED_BY_MODERATOR),
                eq(9L),
                eq("hakaret")
        );
    }

    @Test
    void getHiddenPlatesCalculatesReportCountFromReportDao() {
        Plate plate = new Plate();
        plate.setId(101L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);

        when(plateDao.findByStatusIn(any(), any()))
                .thenReturn(new PageImpl<>(List.of(plate), PageRequest.of(0, 20), 1));
        when(plateReportDao.countByPlateIdAndActiveTrue(101L)).thenReturn(3L);
        when(messageService.getMessage("admin.plates.hidden.listed")).thenReturn("listed");

        DataResult<com.mefy.platemate.core.utilities.pagination.PagedData<PlateAdminDto>> result =
                manager.getHiddenPlates(PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getItems().size());
        assertEquals(3, result.getData().getItems().get(0).getReportCount());
    }
}




