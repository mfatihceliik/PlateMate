package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.mappers.SocialMediaLinkMapper;
import com.mefy.platemate.core.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileManagerTest {

    @Mock
    private IUserProfileDao userProfileDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IUserSettingsService userSettingsService;
    @Mock
    private IMessageService messageService;

    private UserProfileManager userProfileManager;

    @BeforeEach
    void setUp() {
        userProfileManager = new UserProfileManager(
                userProfileDao,
                plateReviewDao,
                new UserProfileMapper(new SocialMediaLinkMapper()),
                new PlateReviewMapper(),
                userSettingsService,
                messageService
        );
    }

    @Test
    void getByUserIdReturnsEnrichedProfileForSelfViewer() {
        User user = new User();
        user.setId(3L);
        user.setUsername("fatih");
        user.setCreatedAt(LocalDateTime.of(2026, 1, 10, 10, 0));
        user.setPremiumUntil(LocalDateTime.of(2026, 12, 31, 23, 59));

        UserProfile profile = new UserProfile();
        profile.setId(3L);
        profile.setUser(user);

        Plate plate = new Plate();
        plate.setId(8L);
        plate.setPlateCode("34ABC123");

        PlateReview review = new PlateReview();
        review.setId(11L);
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(4);
        review.setComment("iyi surucu");
        review.setStatus(PlateReviewStatus.PENDING_REVIEW);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(userProfileDao.findById(3L)).thenReturn(Optional.of(profile));
        when(plateReviewDao.findByUserId(eq(3L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 20), 1));
        when(plateReviewDao.countByUserId(3L)).thenReturn(1L);
        when(plateReviewDao.sumRatingByUserId(3L)).thenReturn(4L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.APPROVED)).thenReturn(1L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.PENDING_REVIEW)).thenReturn(2L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REJECTED)).thenReturn(3L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REMOVED_BY_USER)).thenReturn(4L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REMOVED_BY_MODERATOR)).thenReturn(5L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REMOVED_BY_LEGAL_REQUEST)).thenReturn(6L);
        when(userSettingsService.getByUserId(3L))
                .thenReturn(new SuccessDataResult<>(new UserSettingsDto(true, true, true, true), "settings"));
        when(messageService.getMessage(Messages.PROFILE_FOUND)).thenReturn("profile-found");

        DataResult<UserProfileDto> result = userProfileManager.getByUserId(3L, 3L, PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals("profile-found", result.getMessage());
        assertEquals("fatih", result.getData().getUsername());
        assertEquals(1, result.getData().getPlateReviews().getItems().size());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, result.getData().getPlateReviews().getItems().get(0).getReviewStatus());
        assertEquals(0, result.getData().getPlateReviews().getMeta().getPage());
        assertEquals(20, result.getData().getPlateReviews().getMeta().getSize());
        assertEquals(1L, result.getData().getPlateReviews().getMeta().getTotalElements());
        assertEquals(4.0, result.getData().getAverageGivenRating());
        assertEquals(1, result.getData().getReviewCount());
        assertNotNull(result.getData().getReviewStatusCounts());
        assertEquals(1, result.getData().getReviewStatusCounts().getApproved());
        assertEquals(2, result.getData().getReviewStatusCounts().getPendingReview());
        assertEquals(3, result.getData().getReviewStatusCounts().getRejected());
        assertEquals(4, result.getData().getReviewStatusCounts().getRemovedByUser());
        assertEquals(5, result.getData().getReviewStatusCounts().getRemovedByModerator());
        assertEquals(6, result.getData().getReviewStatusCounts().getRemovedByLegalRequest());
        assertNotNull(result.getData().getPlateReviews().getMeta().getEvaluationTotals());
        assertEquals(1, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalApproved());
        assertEquals(2, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalPendingReview());
        assertEquals(3, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRejected());
        assertEquals(4, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRemovedByUser());
        assertEquals(5, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRemovedByModerator());
        assertEquals(6, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRemovedByLegalRequest());
        assertNotNull(result.getData().getUserSettings());
        assertNotNull(result.getData().getJoinedAt());
        assertEquals(user.getPremiumUntil(), result.getData().getPremiumUntil());
    }

    @Test
    void getByUserIdReturnsNullSettingsForNonSelfViewer() {
        User user = new User();
        user.setId(3L);
        user.setUsername("fatih");

        UserProfile profile = new UserProfile();
        profile.setId(3L);
        profile.setUser(user);

        Plate plate = new Plate();
        plate.setId(8L);
        plate.setPlateCode("34ABC123");

        PlateReview approvedReview = new PlateReview();
        approvedReview.setId(11L);
        approvedReview.setPlate(plate);
        approvedReview.setUser(user);
        approvedReview.setRating(5);
        approvedReview.setComment("temiz");
        approvedReview.setStatus(PlateReviewStatus.APPROVED);
        approvedReview.setCreatedAt(LocalDateTime.now());
        approvedReview.setUpdatedAt(LocalDateTime.now());

        when(userProfileDao.findById(3L)).thenReturn(Optional.of(profile));
        when(plateReviewDao.findByUserIdAndStatus(eq(3L), eq(PlateReviewStatus.APPROVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(approvedReview), PageRequest.of(0, 20), 1));
        when(plateReviewDao.countByUserId(3L)).thenReturn(0L);
        when(plateReviewDao.sumRatingByUserId(3L)).thenReturn(0L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.APPROVED)).thenReturn(1L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.PENDING_REVIEW)).thenReturn(2L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REJECTED)).thenReturn(3L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REMOVED_BY_USER)).thenReturn(4L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REMOVED_BY_MODERATOR)).thenReturn(5L);
        when(plateReviewDao.countByUserIdAndStatus(3L, PlateReviewStatus.REMOVED_BY_LEGAL_REQUEST)).thenReturn(6L);
        when(messageService.getMessage(Messages.PROFILE_FOUND)).thenReturn("profile-found");

        DataResult<UserProfileDto> result = userProfileManager.getByUserId(3L, 99L, PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertNull(result.getData().getUserSettings());
        assertEquals(1, result.getData().getPlateReviews().getItems().size());
        assertEquals(PlateReviewStatus.APPROVED, result.getData().getPlateReviews().getItems().get(0).getReviewStatus());
        assertNotNull(result.getData().getReviewStatusCounts());
        assertEquals(1, result.getData().getReviewStatusCounts().getApproved());
        assertEquals(0, result.getData().getReviewStatusCounts().getPendingReview());
        assertEquals(0, result.getData().getReviewStatusCounts().getRejected());
        assertEquals(0, result.getData().getReviewStatusCounts().getRemovedByUser());
        assertEquals(0, result.getData().getReviewStatusCounts().getRemovedByModerator());
        assertEquals(0, result.getData().getReviewStatusCounts().getRemovedByLegalRequest());
        assertNotNull(result.getData().getPlateReviews().getMeta().getEvaluationTotals());
        assertEquals(1, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalApproved());
        assertEquals(2, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalPendingReview());
        assertEquals(3, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRejected());
        assertEquals(4, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRemovedByUser());
        assertEquals(5, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRemovedByModerator());
        assertEquals(6, result.getData().getPlateReviews().getMeta().getEvaluationTotals().getTotalRemovedByLegalRequest());
    }
}
