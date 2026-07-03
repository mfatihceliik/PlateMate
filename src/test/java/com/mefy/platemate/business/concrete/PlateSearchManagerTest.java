package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeTranslationDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import com.mefy.platemate.entities.concrete.City;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateSearchManagerTest {

    @Mock
    private IPlateDao plateDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IPlateSearchEventDao plateSearchEventDao;
    @Mock
    private ICityDao cityDao;
    @Mock
    private IPlateReportDao plateReportDao;
    @Mock
    private IPlateReportTypeTranslationDao translationDao;
    @Mock
    private IPlateValidator plateValidator;
    @Mock
    private TrPlateCityResolver plateCityResolver;
    @Mock
    private IMessageService messageService;

    private PlateSearchManager plateSearchManager;

    @BeforeEach
    void setUp() {
        plateSearchManager = new PlateSearchManager(
                plateDao,
                plateReviewDao,
                plateSearchEventDao,
                cityDao,
                plateReportDao,
                translationDao,
                plateValidator,
                plateCityResolver,
                messageService
        );
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
        when(plateCityResolver.resolveCityId("34ABC123")).thenReturn(Optional.of(34));
        City city = new City();
        city.setId(34);
        city.setName("Istanbul");
        when(cityDao.findById(34)).thenReturn(Optional.of(city));
        when(plateDao.save(any(Plate.class))).thenReturn(saved);

        when(plateReviewDao.getRatingDistribution(eq(1L), eq(PlateReviewStatus.APPROVED.getId())))
                .thenReturn(List.of());
        when(plateReportDao.findByPlateIdWithReportType(eq(1L), eq(PlateReviewStatus.APPROVED.getId()))).thenReturn(List.of());
        when(plateReviewDao.findRecentByPlateCodeWithUserProfile(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(List.of());
        when(messageService.getMessage(Messages.PLATE_FOUND)).thenReturn("plate-found");

        DataResult<PlateDetailDto> result = plateSearchManager.searchByPlateCode("34 ABC 123", 42L);

        assertTrue(result.isSuccess());
        assertEquals("plate-found", result.getMessage());
        assertEquals("34ABC123", result.getData().getPlateCode());
        assertEquals(0.0, result.getData().getRatingAverage());
        assertEquals(0, result.getData().getReviewCount());
        assertEquals(0L, result.getData().getTotalRatingSum());
        assertTrue(result.getData().getRecentReviews().isEmpty());
        assertTrue(result.getData().getTagSummary().isEmpty());

        ArgumentCaptor<PlateSearchEvent> eventCaptor = ArgumentCaptor.forClass(PlateSearchEvent.class);
        verify(plateSearchEventDao).save(eventCaptor.capture());
        assertEquals(42L, eventCaptor.getValue().getUserId());
    }

    @Test
    void searchByPlateCodeReturnsErrorForInvalidPlate() {
        when(plateValidator.isValid("XX")).thenReturn(false);
        when(messageService.getMessage(Messages.PLATE_INVALID)).thenReturn("invalid-format");

        DataResult<PlateDetailDto> result = plateSearchManager.searchByPlateCode("XX", 42L);

        assertFalse(result.isSuccess());
        assertEquals("invalid-format", result.getMessage());
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

        User reportUser = new User();
        reportUser.setId(99L);

        PlateReport report = new PlateReport();
        report.setId(10L);
        report.setPlate(plate);
        report.setUser(reportUser);
        report.setReportType(reportType);
        report.setActive(true);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.getRatingDistribution(eq(55L), eq(PlateReviewStatus.APPROVED.getId())))
                .thenReturn(List.of(
                        new Object[]{5, 6L},
                        new Object[]{4, 2L},
                        new Object[]{3, 1L},
                        new Object[]{2, 1L}
                ));
        when(plateReportDao.findByPlateIdWithReportType(eq(55L), eq(PlateReviewStatus.APPROVED.getId()))).thenReturn(List.of(report));
        when(plateReviewDao.findRecentByPlateCodeWithUserProfile(eq("34ABC123"), eq(PlateReviewStatus.APPROVED.getId()), any(Pageable.class)))
                .thenReturn(List.of());

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

        DataResult<PlateDetailDto> result = plateSearchManager.searchByPlateCode("34ABC123", 123L);

        assertTrue(result.isSuccess());
        assertEquals(6L, result.getData().getTotalSearchCount());
        assertEquals(2L, result.getData().getTotalReviewCount());
        assertEquals(1L, result.getData().getTotalReportCount());
        assertEquals(5L, result.getData().getTotalWeightedReportScore());
        assertEquals(25.0, result.getData().getScore());
        assertEquals(lastReport, result.getData().getLastActivityAt());

        assertEquals(1, result.getData().getTagSummary().size());
        assertEquals("RED_LIGHT_VIOLATION", result.getData().getTagSummary().get(0).getCode());
        assertEquals(1L, result.getData().getTagSummary().get(0).getCount());

        assertEquals(5, result.getData().getRatingDistribution().size());
        assertEquals(5, result.getData().getRatingDistribution().get(0).getRating());
        assertEquals(6L, result.getData().getRatingDistribution().get(0).getCount());

        verify(plateSearchEventDao).save(any(PlateSearchEvent.class));
    }
}
