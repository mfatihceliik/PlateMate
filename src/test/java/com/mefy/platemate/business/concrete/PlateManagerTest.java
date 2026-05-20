package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
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
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
                new PlateReviewMapper(),
                plateValidator,
                new TrPlateCityResolver(),
                messageService
        );
    }

    @Test
    void searchByPlateCodeCreatesPlateWhenNotFound() {
        Plate saved = new Plate();
        saved.setId(1L);
        saved.setPlateCode("34ABC123");
        saved.setRatingAverage(0.0);
        saved.setReviewCount(0);
        saved.setTotalRatingSum(0L);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.empty());
        when(plateDao.save(any(Plate.class))).thenReturn(saved);
        when(plateReviewDao.findByPlatePlateCode(eq("34ABC123"), any(Pageable.class)))
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
        User user = new User();
        user.setId(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findById(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateId(10L)).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateId(10L)).thenReturn(5L);
        when(messageService.getMessage(Messages.REVIEW_ADDED)).thenReturn("review-added");

        Result result = plateManager.addReview("34 ABC 123", 99L, new AddPlateReviewRequest(5, "iyi", null));

        assertTrue(result.isSuccess());
        assertEquals("review-added", result.getMessage());
        assertEquals(1, plate.getReviewCount());
        assertEquals(5L, plate.getTotalRatingSum());
        assertEquals(5.0, plate.getRatingAverage());
        verify(plateDao).save(plate);
        verify(plateReportService, never()).syncReportsForUserAndPlate(any(), any(), any());
    }

    @Test
    void addReviewSyncsReportsWhenReportCodesProvided() {
        User user = new User();
        user.setId(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findById(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateId(10L)).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateId(10L)).thenReturn(5L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate), eq(99L), eq(List.of("RED_LIGHT_VIOLATION"))))
                .thenReturn(new SuccessResult("synced"));
        when(messageService.getMessage(Messages.REVIEW_ADDED)).thenReturn("review-added");

        Result result = plateManager.addReview(
                "34 ABC 123",
                99L,
                new AddPlateReviewRequest(5, "iyi", List.of("RED_LIGHT_VIOLATION"))
        );

        assertTrue(result.isSuccess());
        verify(plateReportService).syncReportsForUserAndPlate(eq(plate), eq(99L), eq(List.of("RED_LIGHT_VIOLATION")));
    }

    @Test
    void addReviewSyncsReportsWhenReportCodesEmptyListProvided() {
        User user = new User();
        user.setId(77L);

        Plate plate = new Plate();
        plate.setId(88L);
        plate.setPlateCode("34ABC123");

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findById(77L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(88L, 77L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateId(88L)).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateId(88L)).thenReturn(4L);
        when(plateReportService.syncReportsForUserAndPlate(eq(plate), eq(77L), eq(List.of())))
                .thenReturn(new SuccessResult("synced"));
        when(messageService.getMessage(Messages.REVIEW_ADDED)).thenReturn("review-added");

        Result result = plateManager.addReview("34ABC123", 77L, new AddPlateReviewRequest(4, "iyi", List.of()));

        assertTrue(result.isSuccess());
        verify(plateReportService).syncReportsForUserAndPlate(eq(plate), eq(77L), eq(List.of()));
    }

    @Test
    void getReviewsByPlateCodeReturnsPagedDataMeta() {
        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");

        User user = new User();
        user.setId(1L);
        user.setUsername("fatih");

        PlateReview review = new PlateReview();
        review.setId(9L);
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(5);
        review.setComment("cok iyi");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateReviewDao.findByPlatePlateCode(eq("34ABC123"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 20), 1));
        when(messageService.getMessage(Messages.REVIEWS_LISTED)).thenReturn("reviews-listed");

        DataResult<PagedData<PlateReviewDto>> result =
                plateManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals("reviews-listed", result.getMessage());
        assertEquals(1, result.getData().getItems().size());
        assertEquals(0, result.getData().getMeta().getPage());
        assertEquals(20, result.getData().getMeta().getSize());
        assertEquals(1L, result.getData().getMeta().getTotalElements());
        assertEquals(1, result.getData().getMeta().getTotalPages());
        assertFalse(result.getData().getMeta().isHasNext());
        assertFalse(result.getData().getMeta().isHasPrevious());
    }

    @Test
    void getReviewsByPlateCodeUsesDescendingCreatedAtSortAndRequestedPage() {
        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateReviewDao.findByPlatePlateCode(eq("34ABC123"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 20), 21));
        when(messageService.getMessage(Messages.REVIEWS_LISTED)).thenReturn("reviews-listed");

        DataResult<PagedData<PlateReviewDto>> result =
                plateManager.getReviewsByPlateCode("34ABC123", PaginationRequest.of(1, 20));

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getMeta().getPage());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(plateReviewDao).findByPlatePlateCode(eq("34ABC123"), pageableCaptor.capture());
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
        plate.setRatingAverage(4.5);
        plate.setReviewCount(10);
        plate.setTotalRatingSum(45L);

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
        when(plateReviewDao.findByPlatePlateCode(eq("34ABC123"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(plateReportDao.findByPlateIdInAndActiveTrue(List.of(55L))).thenReturn(List.of(report));

        when(plateSearchEventDao.countByPlateId(55L)).thenReturn(6L);
        when(plateReviewDao.countByPlateId(55L)).thenReturn(2L);
        when(plateReportDao.countByPlateIdAndActiveTrue(55L)).thenReturn(1L);
        when(plateReportDao.getWeightedScoreByPlateId(55L)).thenReturn(5L);

        LocalDateTime lastSearch = LocalDateTime.now().minusHours(3);
        LocalDateTime lastReview = LocalDateTime.now().minusHours(2);
        LocalDateTime lastReport = LocalDateTime.now().minusHours(1);
        when(plateSearchEventDao.findLastSearchedAtByPlateId(55L)).thenReturn(lastSearch);
        when(plateReviewDao.findLastReviewAtByPlateId(55L)).thenReturn(lastReview);
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
}
