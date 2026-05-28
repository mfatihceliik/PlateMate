package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.moderation.ContentModerationService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.business.utilities.security.HashingService;
import com.mefy.platemate.core.utilities.mappers.PlateReportTypeMapper;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateManagerTest {

    @Mock
    private IPlateDao plateDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IPlateSearchEventDao plateSearchEventDao;
    @Mock
    private IPlateReportDao plateReportDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private ICityDao cityDao;
    @Mock
    private IPlateReportService plateReportService;
    @Mock
    private IPlateValidator plateValidator;
    @Mock
    private PlateReviewModerationEventService moderationEventService;
    @Mock
    private IMessageService messageService;

    private PlateManager plateManager;

    @BeforeEach
    void setUp() {
        plateManager = new PlateManager(
                plateDao,
                plateReviewDao,
                plateSearchEventDao,
                userDao,
                cityDao,
                plateReportService,
                plateReportDao,
                new PlateReportTypeMapper(new PlateReportTypePolicyService()),
                new PlateReviewMapper(),
                new ContentModerationService(),
                new HashingService(),
                plateValidator,
                new TrPlateCityResolver(),
                moderationEventService,
                messageService
        );
        ReflectionTestUtils.setField(plateManager, "acceptedResponsibilityLegacyFallback", true);
    }

    @Test
    void searchByPlateCodeCreatesPlateWhenNotFound() {
        Plate saved = new Plate();
        saved.setId(1L);
        saved.setPlateCode("34ABC123");
        saved.setStatus(PlateStatus.ACTIVE);
        saved.setRatingAverage(0.0);
        saved.setReviewCount(0);
        saved.setTotalRatingSum(0L);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.empty());
        when(plateDao.save(any(Plate.class))).thenReturn(saved);
        when(plateReviewDao.findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(plateReportDao.findByPlateIdInAndActiveTrue(List.of(1L))).thenReturn(List.of());
        when(messageService.getMessage(Messages.PLATE_FOUND)).thenReturn("plate-found");

        DataResult<PlateDetailDto> result = plateManager.searchByPlateCode("34 ABC 123", 42L);

        assertTrue(result.isSuccess());
        assertEquals("plate-found", result.getMessage());
        assertEquals("34ABC123", result.getData().getPlateCode());
        assertEquals(0.0, result.getData().getRatingAverage());
        assertEquals(0, result.getData().getReviewCount());
        assertEquals(0L, result.getData().getTotalRatingSum());
        assertEquals("Istanbul", result.getData().getCityName());
        assertTrue(result.getData().getRecentReviews().isEmpty());
        assertTrue(result.getData().getRecentReportTypes().isEmpty());

        ArgumentCaptor<PlateSearchEvent> eventCaptor = ArgumentCaptor.forClass(PlateSearchEvent.class);
        verify(plateSearchEventDao).save(eventCaptor.capture());
        assertEquals(42L, eventCaptor.getValue().getUserId());
    }

    @Test
    void searchByPlateCodeReturnsErrorForInvalidPlate() {
        when(plateValidator.isValid("XX")).thenReturn(false);
        when(messageService.getMessage(Messages.PLATE_INVALID)).thenReturn("invalid-plate");

        DataResult<PlateDetailDto> result = plateManager.searchByPlateCode("XX", 42L);

        assertTrue(!result.isSuccess());
        assertEquals("invalid-plate", result.getMessage());
    }

    @Test
    void addReviewUpdatesAggregateValuesFromPlateReviews() {
        User user = createPremiumUser(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(5L);
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.addReview("34 ABC 123", 99L, new AddPlateReviewRequest(5, "iyi", null, true));

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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(5L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate), eq(99L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.addReview(
                "34 ABC 123",
                99L,
                new AddPlateReviewRequest(5, "iyi", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertTrue(result.isSuccess());
        verify(plateReportService).syncReportsForUserAndPlate(eq(plate), eq(99L), eq(List.of("RED_LIGHT_VIOLATION")));
    }

    @Test
    void addReviewSyncsReportsWhenReportCodesEmptyListProvided() {
        User user = createPremiumUser(77L);

        Plate plate = new Plate();
        plate.setId(88L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(77L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(88L, 77L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(88L, PlateReviewStatus.APPROVED.getId())).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(88L, PlateReviewStatus.APPROVED.getId())).thenReturn(4L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate), eq(77L), eq(List.of())))
                .thenReturn(new SuccessResult("synced"));
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.addReview("34ABC123", 77L, new AddPlateReviewRequest(4, "iyi", List.of(), true));

        assertTrue(result.isSuccess());
        verify(plateReportService).syncReportsForUserAndPlate(eq(plate), eq(77L), eq(List.of()));
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE)).thenReturn("already-exists");

        Result result = plateManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeni yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("already-exists", result.getMessage());
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
        verify(plateReviewDao, never()).save(any(PlateReview.class));
        verify(moderationEventService, never()).logEvent(any(), any(), any(), any(), any(), any());
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(plateReportService.syncReportsForUserAndPlate(eq(plate), eq(99L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeniden yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, existingReview.getStatus());
        assertEquals(5, existingReview.getRating());
        verify(plateReviewDao).save(eq(existingReview));
        verify(moderationEventService).logEvent(
                eq(existingReview),
                eq(PlateReviewStatus.REJECTED.getId()),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewModerationActionType.SUBMITTED_FOR_REVIEW),
                eq(99L),
                eq("USER_RESUBMITTED_REJECTED_REVIEW")
        );
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE)).thenReturn("already-exists");

        Result result = plateManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeni yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("already-exists", result.getMessage());
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
        verify(plateReviewDao, never()).save(any(PlateReview.class));
        verify(moderationEventService, never()).logEvent(any(), any(), any(), any(), any(), any());
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.of(existingReview));
        when(messageService.getMessage(Messages.REVIEW_ALREADY_EXISTS_FOR_PLATE)).thenReturn("already-exists");

        Result result = plateManager.addReview(
                "34ABC123",
                99L,
                new AddPlateReviewRequest(5, "yeni yorum", List.of("RED_LIGHT_VIOLATION"), true)
        );

        assertFalse(result.isSuccess());
        assertEquals("already-exists", result.getMessage());
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
        verify(plateReviewDao, never()).save(any(PlateReview.class));
        verify(moderationEventService, never()).logEvent(any(), any(), any(), any(), any(), any());
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 20), 1));
        when(messageService.getMessage(Messages.REVIEWS_LISTED)).thenReturn("reviews-listed");

        DataResult<PagedData<PlateReviewDto>> result =
                plateManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(0, 20));

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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 20), 21));
        when(messageService.getMessage(Messages.REVIEWS_LISTED)).thenReturn("reviews-listed");

        DataResult<PagedData<PlateReviewDto>> result =
                plateManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(1, 20));

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
    void searchByPlateCodePopulatesDetailAndTotalMetricsFromRepositories() {
        Plate plate = new Plate();
        plate.setId(55L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);
        plate.setRatingAverage(4.5);
        plate.setReviewCount(10);
        plate.setTotalRatingSum(45L);
        plate.setUpdatedAt(LocalDateTime.now().minusHours(4));

        PlateReportType reportType = new PlateReportType();
        reportType.setId(1L);
        reportType.setCode("RED_LIGHT_VIOLATION");
        reportType.setLabel("Kirmizi Isik");
        reportType.setDescription("desc");
        reportType.setIconKey("icon");
        reportType.setSeverity(PlateReportSeverity.RED);
        reportType.setColorHex("#E53935");
        reportType.setWeight(5);
        reportType.setSortOrder(1);
        reportType.setActive(true);

        PlateReport report = new PlateReport();
        report.setId(10L);
        report.setPlate(plate);
        report.setReportType(reportType);
        report.setActive(true);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlatePlateCodeAndStatusId(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(plateReportDao.findByPlateIdInAndActiveTrue(List.of(55L))).thenReturn(List.of(report));

        when(plateSearchEventDao.countByPlateId(55L)).thenReturn(6L);
        when(plateReviewDao.countByPlateIdAndStatusId(55L, PlateReviewStatus.APPROVED.getId())).thenReturn(2L);
        when(plateReportDao.countByPlateIdAndActiveTrue(55L)).thenReturn(1L);
        when(plateReportDao.getWeightedScoreByPlateId(55L)).thenReturn(5L);

        LocalDateTime lastSearch = LocalDateTime.now().minusHours(3);
        LocalDateTime lastReview = LocalDateTime.now().minusHours(2);
        LocalDateTime lastReport = LocalDateTime.now().minusHours(1);
        when(plateSearchEventDao.findLastSearchedAtByPlateId(55L)).thenReturn(lastSearch);
        when(plateReviewDao.findLastReviewAtByPlateIdAndStatus(55L, PlateReviewStatus.APPROVED.getId())).thenReturn(lastReview);
        when(plateReportDao.findLastReportedAtByPlateId(55L)).thenReturn(lastReport);

        when(messageService.getMessage(Messages.PLATE_FOUND)).thenReturn("plate-found");

        DataResult<PlateDetailDto> result = plateManager.searchByPlateCode("34ABC123", 123L);

        assertTrue(result.isSuccess());
        assertEquals(6L, result.getData().getTotalSearchCount());
        assertEquals(2L, result.getData().getTotalReviewCount());
        assertEquals(1L, result.getData().getTotalReportCount());
        assertEquals(5L, result.getData().getTotalWeightedReportScore());
        assertEquals(25.0, result.getData().getScore());
        assertEquals(lastReport, result.getData().getLastActivityAt());
        verify(plateSearchEventDao).save(any(PlateSearchEvent.class));
    }

    @Test
    void addReviewReturnsErrorWhenResponsibilityIsNotAccepted() {
        User user = new User();
        user.setId(99L);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(messageService.getMessage(Messages.REVIEW_RESPONSIBILITY_REQUIRED))
                .thenReturn("responsibility-required");

        Result result = plateManager.addReview("34ABC123", 99L, new AddPlateReviewRequest(5, "iyi", null, false));

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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.addReview(
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
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.updateReview(
                55L,
                99L,
                new UpdatePlateReviewRequest(4, "guncel yorum", null, true)
        );

        assertTrue(result.isSuccess());
        assertEquals("pending-review", result.getMessage());
        assertEquals(PlateReviewStatus.PENDING_REVIEW, review.getStatus());
        verify(moderationEventService).logEvent(
                eq(review),
                eq(PlateReviewStatus.APPROVED.getId()),
                eq(PlateReviewStatus.PENDING_REVIEW.getId()),
                eq(PlateReviewModerationActionType.SUBMITTED_FOR_REVIEW),
                eq(99L),
                eq("USER_UPDATED_REVIEW")
        );
    }

    @Test
    void addReviewRejectsFreeTextForNonPremiumUser() {
        User user = new User();
        user.setId(90L);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(90L)).thenReturn(Optional.of(user));
        when(messageService.getMessage(Messages.REVIEW_COMMENT_PREMIUM_REQUIRED)).thenReturn("premium-required");

        Result result = plateManager.addReview(
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(91L)).thenReturn(Optional.of(user));
        when(messageService.getMessage(Messages.REVIEW_REPORT_TYPE_REQUIRED_FOR_NON_PREMIUM)).thenReturn("tag-required");

        Result result = plateManager.addReview(
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findByIdAndActiveTrue(92L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 92L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateIdAndStatusId(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReviewDao.sumRatingByPlateIdAndStatus(10L, PlateReviewStatus.APPROVED.getId())).thenReturn(0L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate), eq(92L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(messageService.getMessage(Messages.REVIEW_PENDING_REVIEW)).thenReturn("pending-review");

        Result result = plateManager.addReview(
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

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(hiddenPlate));
        when(messageService.getMessage(Messages.PLATE_NOT_AVAILABLE)).thenReturn("plate-not-available");

        DataResult<PagedData<PlateReviewDto>> result =
                plateManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(0, 20));

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




