package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.abstracts.IPlateModerationService;
import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.utilities.moderation.ContentModerationResult;

import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateReviewManagerTest {

    @Mock
    private IPlateDao plateDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IPlateReportDao plateReportDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private IPlateReportService plateReportService;
    @Mock
    private IPlateSearchService plateSearchService;
    @Mock
    private IPlateModerationService plateModerationService;
    @Mock
    private IMessageService messageService;

    private PlateReviewManager plateReviewManager;

    @BeforeEach
    void setUp() {
        plateReviewManager = new PlateReviewManager(
                plateDao,
                plateReviewDao,
                plateReportDao,
                userDao,
                plateReportService,
                new PlateReviewMapper(),
                plateModerationService,
                messageService,
                plateSearchService
        );
        ReflectionTestUtils.setField(plateReviewManager, "acceptedResponsibilityLegacyFallback", true);

        org.mockito.Mockito.lenient().when(plateModerationService.resolveModeration(any(), any()))
            .thenReturn(new ContentModerationResult(true, false, List.of(), ""));

        org.mockito.Mockito.lenient().doAnswer(invocation -> {
            PlateReview r = invocation.getArgument(0);
            ContentModerationResult mod = invocation.getArgument(1);
            r.setComment(mod.getSanitizedText());
            r.setStatus(PlateReviewStatus.PENDING_REVIEW);
            r.setModerationReason(mod.isRequiresReview() ? String.join(",", mod.getReasons()) : null);
            return null;
        }).when(plateModerationService).applyModerationMetadata(any(), any(), any());
    }



    @Test
    void addReviewUpdatesAggregateValuesFromPlateReviews() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(5L);
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 99L)).thenReturn(List.of());
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        DataResult result = plateReviewManager.addReview("34 ABC 123", 99L, new AddPlateReviewRequest(5, "iyi", null, true));

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        assertEquals(1, plate.getReviewCount());
        assertEquals(5L, plate.getTotalRatingSum());
        assertEquals(5.0, plate.getRatingAverage());
        verify(plateDao).save(plate);
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
    }

    @Test
    void addReviewSyncsReportsWhenReportCodesProvided() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(5L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate.getId()), eq(99L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 99L)).thenReturn(List.of());
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        DataResult result = plateReviewManager.addReview(
                "34 ABC 123",
                99L,
                new AddPlateReviewRequest(5, "iyi", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertTrue(result.isSuccess());
        verify(plateReportService).syncReportsForUserAndPlate(eq(plate.getId()), eq(99L), eq(List.of("RED_LIGHT_VIOLATION")));
    }

    @Test
    void addReviewSyncsReportsWhenReportCodesEmptyListProvided() {
        User user = createPremiumUser(77L);

        Plate plate = new Plate();
        plate.setId(88L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(77L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(88L, 77L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(88L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(88L, PlateReviewStatus.APPROVED.getId())).thenReturn(4L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate.getId()), eq(77L), eq(List.of())))
                .thenReturn(new SuccessResult("synced"));
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(88L, 77L)).thenReturn(List.of());
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        DataResult result = plateReviewManager.addReview("34ABC123", 77L, new AddPlateReviewRequest(4, "iyi", List.of(), true));

        assertTrue(result.isSuccess());
        verify(plateReportService).syncReportsForUserAndPlate(eq(plate.getId()), eq(77L), eq(List.of()));
    }

    @Test
    void addReviewReturnsErrorWhenUserAlreadyHasPendingReviewForSamePlate() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview existingReview = new PlateReview();
        existingReview.setId(100L);
        existingReview.setPlate(plate);
        existingReview.setUser(user);
        existingReview.setRating(4);
        existingReview.setComment("mevcut");
        existingReview.setStatus(PlateReviewStatus.PENDING_REVIEW);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE)).thenReturn("already-exists");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeni yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("already-exists", result.getMessage());
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
        verify(plateReviewDao, never()).save(any(PlateReview.class));
        verify(plateModerationService, never()).logReviewSubmitted(any(), any(), any(), any());
        verify(plateDao, never()).save(any(Plate.class));
    }

    @Test
    void addReviewResubmitsRejectedReviewAsPending() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview existingReview = new PlateReview();
        existingReview.setId(101L);
        existingReview.setPlate(plate);
        existingReview.setUser(user);
        existingReview.setRating(2);
        existingReview.setComment("eski");
        existingReview.setStatus(PlateReviewStatus.REJECTED);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(plateReportService.syncReportsForUserAndPlate(eq(plate.getId()), eq(99L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 99L)).thenReturn(List.of());
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeniden yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, existingReview.getStatus());
        assertEquals(5, existingReview.getRating());
        verify(plateReviewDao).save(eq(existingReview));
        verify(plateModerationService).logReviewSubmitted(any(), any(), any(), any());
    }

    @Test
    void addReviewReturnsErrorWhenUserAlreadyHasApprovedReviewForSamePlate() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview existingReview = new PlateReview();
        existingReview.setId(102L);
        existingReview.setPlate(plate);
        existingReview.setUser(user);
        existingReview.setStatus(PlateReviewStatus.APPROVED);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE)).thenReturn("already-exists");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeni yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("already-exists", result.getMessage());
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
        verify(plateReviewDao, never()).save(any(PlateReview.class));
        verify(plateModerationService, never()).logReviewSubmitted(any(), any(), any(), any());
    }

    @Test
    void addReviewReturnsErrorWhenUserAlreadyHasRemovedReviewForSamePlate() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview existingReview = new PlateReview();
        existingReview.setId(103L);
        existingReview.setPlate(plate);
        existingReview.setUser(user);
        existingReview.setStatus(PlateReviewStatus.REMOVED_BY_USER);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE)).thenReturn("already-exists");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeni yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("already-exists", result.getMessage());
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
        verify(plateReviewDao, never()).save(any(PlateReview.class));
        verify(plateModerationService, never()).logReviewSubmitted(any(), any(), any(), any());
    }

    @Test
    void getReviewsByPlateCodeReturnsPagedDataMeta() {
        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        User user = new User();
        user.setId(1L);
        user.setUsername("fatih");

        PlateReview review = new PlateReview();
        review.setId(9L);
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(5);
        review.setComment("cok iyi");
        review.setStatus(PlateReviewStatus.APPROVED);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 20), 1));
        when(messageService.getMessage(Messages.REVIEWS_LISTED)).thenReturn("reviews-listed");

        DataResult<PagedData<PlateReviewDto>> result =
                plateReviewManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals("reviews-listed", result.getMessage());
        assertEquals(1, result.getData().getItems().size());
        assertEquals(PlateReviewStatus.APPROVED.getId(), result.getData().getItems().get(0).getReviewStatusId());
        assertEquals(PlateReviewStatus.APPROVED.getCode(), result.getData().getItems().get(0).getReviewStatusCode());
        assertEquals(0, result.getData().getMeta().getPage());
        assertEquals(20, result.getData().getMeta().getSize());
        assertEquals(1L, result.getData().getMeta().getTotalElements());
        assertEquals(1, result.getData().getMeta().getTotalPages());
        assertFalse(result.getData().getMeta().isHasNext());
        assertFalse(result.getData().getMeta().isHasPrevious());
    }

    @Test
    void getReviewsByPlateCodeUsesDescendingCreatedAtSortAndRequestedPage() {
        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 20), 21));
        when(messageService.getMessage(Messages.REVIEWS_LISTED)).thenReturn("reviews-listed");

        DataResult<PagedData<PlateReviewDto>> result =
                plateReviewManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(1, 20));

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getMeta().getPage());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(plateReviewDao).findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("createdAt").isDescending());
    }



    @Test
    void addReviewReturnsErrorWhenResponsibilityIsNotAccepted() {
        User user = new User();
        user.setId(99L);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(messageService.getMessage(Messages.REVIEW_RESPONSIBILITY_REQUIRED))
                .thenReturn("responsibility-required");

        DataResult result = plateReviewManager.addReview("34ABC123", 99L, new AddPlateReviewRequest(5, "iyi", null, false));

        assertFalse(result.isSuccess());
        assertEquals("responsibility-required", result.getMessage());
        verify(plateDao, never()).save(any());
    }

    @Test
    void addReviewSendsRiskyContentToPendingReview() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 99L)).thenReturn(List.of());
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "Bu adam hirsiz", null, true)
        );

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        ArgumentCaptor<PlateReview> reviewCaptor = ArgumentCaptor.forClass(PlateReview.class);
        verify(plateReviewDao).save(reviewCaptor.capture());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, reviewCaptor.getValue().getStatus());
    }

    @Test
    void updateReviewMovesStatusToPendingAndLogsModerationEvent() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        PlateReview review = new PlateReview();
        review.setId(55L);
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(5);
        review.setComment("onceki yorum");
        review.setStatus(PlateReviewStatus.APPROVED);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(plateReviewDao.findById(55L)).thenReturn(Optional.of(review));
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateReviewManager.updateReview(
                55L,
                99L,
                new UpdatePlateReviewRequest(4, "guncel yorum", null, true)
        );

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, review.getStatus());
        verify(plateModerationService).logReviewSubmitted(any(), any(), any(), any());
    }

    @Test
    void addReviewRejectsFreeTextForNonPremiumUser() {
        User user = new User();
        user.setId(90L);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(90L)).thenReturn(Optional.of(user));
        when(messageService.getMessage(Messages.REVIEW_COMMENT_PREMIUM_REQUIRED)).thenReturn("premium-required");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                90L,
                new AddPlateReviewRequest(5, "yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("premium-required", result.getMessage());
    }

    @Test
    void addReviewRejectsMissingTagsForNonPremiumUser() {
        User user = new User();
        user.setId(91L);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(91L)).thenReturn(Optional.of(user));
        when(messageService.getMessage(Messages.REVIEW_REPORT_TYPE_REQUIRED_FOR_NON_PREMIUM)).thenReturn("tag-required");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                91L,
                new AddPlateReviewRequest(5, "", List.of(), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("tag-required", result.getMessage());
    }

    @Test
    void addReviewAcceptsRatingAndTagsForNonPremiumUser() {
        User user = new User();
        user.setId(92L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(userDao.findByIdAndActiveTrue(92L)).thenReturn(Optional.of(user));
        when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);
        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateReviewDao.findByPlateIdAndUserId(10L, 92L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate.getId()), eq(92L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 92L)).thenReturn(List.of());
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        DataResult result = plateReviewManager.addReview(
                "34ABC123",
                92L,
                new AddPlateReviewRequest(5, "", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        ArgumentCaptor<PlateReview> reviewCaptor = ArgumentCaptor.forClass(PlateReview.class);
        verify(plateReviewDao).save(reviewCaptor.capture());
        assertEquals("", reviewCaptor.getValue().getComment());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, reviewCaptor.getValue().getStatus());
    }

    @Test
    void getReviewsByPlateCodeReturnsErrorWhenPlateIsHidden() {
        Plate hiddenPlate = new Plate();
        hiddenPlate.setId(99L);
        hiddenPlate.setPlateCode("34ABC123");
        hiddenPlate.setStatus(PlateStatus.HIDDEN_BY_REQUEST);

        when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));
        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(hiddenPlate));
        when(plateSearchService.checkIfPlatePubliclyVisible(hiddenPlate)).thenReturn(new com.mefy.platemate.core.utilities.results.ErrorResult("plate-not-available"));

        DataResult<PagedData<PlateReviewDto>> result =
                plateReviewManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(0, 20));

        assertFalse(result.isSuccess());
        assertEquals("plate-not-available", result.getMessage());
        verify(plateReviewDao, never()).findByPlatePlateCodeAndStatusId(any(), any(), any());
    }

    private User createPremiumUser(Long userId) {
        UserRole role = new UserRole();
        role.setCode(UserRoleCode.PREMIUM);

        User user = new User();
        user.setId(userId);
        user.setRole(role);
        user.setPremiumUntil(LocalDateTime.now().plusDays(30));
        return user;
    }
}




