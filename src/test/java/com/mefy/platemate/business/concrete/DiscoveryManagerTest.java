package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IDiscoveryTabOptionService;
import com.mefy.platemate.business.discovery.DiscoveryActivityService;
import com.mefy.platemate.business.discovery.DiscoveryAggregationService;
import com.mefy.platemate.business.discovery.DiscoveryPersonalizationService;
import com.mefy.platemate.business.discovery.DiscoveryTabService;
import com.mefy.platemate.business.utilities.LimitValidator;
import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.business.discovery.model.PlateDailyMetrics;
import com.mefy.platemate.business.discovery.model.ScoredDiscoveryPlate;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryActivityActionType;
import com.mefy.platemate.entities.dto.DiscoveryCityStatDto;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryFeedType;
import com.mefy.platemate.entities.dto.DiscoveryForYouDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.DiscoveryRecentActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryTabType;
import com.mefy.platemate.entities.dto.DiscoveryTabsDto;
import com.mefy.platemate.entities.dto.request.DiscoveryTabFeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryManagerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private ICityDao cityDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private IMessageService messageService;
    @Mock
    private LimitValidator limitValidator;
    @Mock
    private TimeWindowService timeWindowService;
    @Mock
    private DiscoveryAggregationService discoveryAggregationService;
    @Mock
    private DiscoveryTabService discoveryTabService;
    @Mock
    private DiscoveryActivityService discoveryActivityService;
    @Mock
    private DiscoveryPersonalizationService discoveryPersonalizationService;
    @Mock
    private IDiscoveryTabOptionService discoveryTabOptionService;

    private DiscoveryManager discoveryManager;

    @BeforeEach
    void setUp() {
        discoveryManager = new DiscoveryManager(
                cityDao,
                userDao,
                messageService,
                limitValidator,
                timeWindowService,
                discoveryAggregationService,
                discoveryTabService,
                discoveryActivityService,
                discoveryPersonalizationService,
                discoveryTabOptionService
        );
        // getHome basarili yol her testte cagirmaz (ör. validation-hatasi erken donuslerde); lenient
        // ile strict-stubbing UnnecessaryStubbingException'i onlenir.
        lenient().when(discoveryTabOptionService.getActiveTabOptions())
                .thenReturn(new SuccessDataResult<>(List.of(), "ok"));
    }

    @Test
    void getHomeReturnsSuccessWhenValidationPasses() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        TimeWindow last7Days = new TimeWindow(LocalDateTime.now().minusDays(6), LocalDateTime.now());
        DiscoveryRecentActivityDto activity = new DiscoveryRecentActivityDto(
                "user",
                "34ABC123",
                DiscoveryActivityActionType.REVIEW_ADDED,
                LocalDateTime.now(),
                5,
                "comment",
                null,
                null
        );

        TimeWindow yesterday = new TimeWindow(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2));

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(timeWindowService.previousDayWindow()).thenReturn(yesterday);
        when(userDao.findByIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty());
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(1L, 2L, 3L));
        when(discoveryAggregationService.getDailyStats(yesterday)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryAggregationService.getTopReportTypeCounts(today, 3)).thenReturn(List.of());
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of(activity));
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(USER_ID, 8,  5, 20);

        assertTrue(result.isSuccess());
        assertEquals("ok", result.getMessage());
        assertEquals(1L, result.getData().getDailyStats().getTodaySearchCount());
        assertEquals(1, result.getData().getRecentActivities().size());
        assertEquals(DiscoveryFeedType.FREE.name(), result.getData().getFeedType());
    }

    @Test
    void getHomeReturnsErrorWhenValidationFails() {
        when(limitValidator.validateLimits(anyInt(), anyInt(), anyInt())).thenReturn(new ErrorResult("invalid"));

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(USER_ID, 0, 5,20);

        assertFalse(result.isSuccess());
        assertEquals("invalid", result.getMessage());
    }

    @Test
    void getHomeFallsBackToLast7DaysWhenTodayIsEmpty() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        TimeWindow last7Days = new TimeWindow(LocalDateTime.now().minusDays(6), LocalDateTime.now());
        DiscoveryCityStatDto city = new DiscoveryCityStatDto(34, "Istanbul", 6L);
        CityPlateActivityDto row = new CityPlateActivityDto("34ABC123", 2L, 1L, LocalDateTime.now(), 4.5, 10);
        DiscoveryRecentActivityDto activity = new DiscoveryRecentActivityDto(
                "user2",
                "34XYZ999",
                DiscoveryActivityActionType.REPORT_SUBMITTED,
                LocalDateTime.now(),
                null,
                null,
                "TRAFFIC_RULE_VIOLATION",
                "Traffic Rule Violation"
        );

        TimeWindow yesterday = new TimeWindow(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2));

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(timeWindowService.previousDayWindow()).thenReturn(yesterday);
        when(userDao.findByIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty());
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(1L, 2L, 3L));
        when(discoveryAggregationService.getDailyStats(yesterday)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryAggregationService.getTopReportTypeCounts(today, 3)).thenReturn(List.of());
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of(city));
        when(discoveryAggregationService.buildCityDailyMetrics(34, last7Days))
                .thenReturn(Map.of(1L, new com.mefy.platemate.business.discovery.model.PlateDailyMetrics()));
        when(discoveryAggregationService.toCityPlateActivityRows(anyMap())).thenReturn(new ArrayList<>(List.of(row)));
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, last7Days)).thenReturn(List.of(activity));
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(USER_ID, 8, 5, 20);

        assertTrue(result.isSuccess());
        assertEquals(1L, result.getData().getDailyStats().getTodaySearchCount());
        assertEquals(1, result.getData().getCityStats().size());
        assertEquals("Istanbul", result.getData().getCityStats().get(0).getCityName());
        assertEquals(1, result.getData().getTopCityPlates().size());
        assertEquals(1, result.getData().getRecentActivities().size());
        verify(discoveryAggregationService, times(1)).getTopCityStats(today, 5);
        verify(discoveryAggregationService, times(1)).getTopCityStats(last7Days, 5);
        verify(discoveryActivityService, times(1)).buildRecentActivities(20, today);
        verify(discoveryActivityService, times(1)).buildRecentActivities(20, last7Days);
    }

    @Test
    void getHomeReturnsEmptyListsWhenTodayAndFallbackAreEmpty() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        TimeWindow last7Days = new TimeWindow(LocalDateTime.now().minusDays(6), LocalDateTime.now());

        TimeWindow yesterday = new TimeWindow(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2));

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(timeWindowService.previousDayWindow()).thenReturn(yesterday);
        when(userDao.findByIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty());
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryAggregationService.getDailyStats(yesterday)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryAggregationService.getTopReportTypeCounts(today, 3)).thenReturn(List.of());
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, last7Days)).thenReturn(List.of());
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(USER_ID, 8, 5, 20);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().getCityStats().isEmpty());
        assertTrue(result.getData().getTopCityPlates().isEmpty());
        assertTrue(result.getData().getRecentActivities().isEmpty());
    }

    @Test
    void getHomeMarksFeedTypePremiumForPremiumUser() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        TimeWindow last7Days = new TimeWindow(LocalDateTime.now().minusDays(6), LocalDateTime.now());
        TimeWindow yesterday = new TimeWindow(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2));
        User premiumUser = mock(User.class);
        when(premiumUser.isPremiumActive()).thenReturn(true);

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(timeWindowService.previousDayWindow()).thenReturn(yesterday);
        when(userDao.findByIdAndActiveTrue(USER_ID)).thenReturn(Optional.of(premiumUser));
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryAggregationService.getDailyStats(yesterday)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryAggregationService.getTopReportTypeCounts(today, 3)).thenReturn(List.of());
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, last7Days)).thenReturn(List.of());
        when(discoveryPersonalizationService.buildForYou(USER_ID, today, List.of()))
                .thenReturn(new DiscoveryForYouDto(List.of(), List.of(), List.of(), null));
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(USER_ID, 8, 5, 20);

        assertTrue(result.isSuccess());
        assertEquals(DiscoveryFeedType.PREMIUM.name(), result.getData().getFeedType());
        assertTrue(result.getData().getForYou() != null);
    }

    @Test
    void getHomeComputesExtendedStatsDeltas() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        TimeWindow last7Days = new TimeWindow(LocalDateTime.now().minusDays(6), LocalDateTime.now());
        TimeWindow yesterday = new TimeWindow(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2));

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(timeWindowService.previousDayWindow()).thenReturn(yesterday);
        when(userDao.findByIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty());
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(4L, 6L, 3L));
        when(discoveryAggregationService.getDailyStats(yesterday)).thenReturn(new DiscoveryDailyStatsDto(2L, 3L, 0L));
        when(discoveryAggregationService.getTopReportTypeCounts(today, 3)).thenReturn(List.of());
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, last7Days)).thenReturn(List.of());
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(USER_ID, 8, 5, 20);

        assertTrue(result.isSuccess());
        assertEquals(2L, result.getData().getExtendedStats().getYesterdaySearchCount());
        assertEquals(100.0, result.getData().getExtendedStats().getSearchDeltaPercent());
        assertEquals(100.0, result.getData().getExtendedStats().getReviewDeltaPercent());
        assertEquals(300.0, result.getData().getExtendedStats().getReportDeltaPercent());
    }

    @Test
    void getTabPlatesReturnsErrorForInvalidTabType() {
        when(messageService.getMessage(Messages.DISCOVERY_TAB_INVALID)).thenReturn("invalid-tab");

        DataResult<PagedData<DiscoveryPlateCardDto>> result = discoveryManager.getTabPlates(
                USER_ID, "NOT_A_TAB", new DiscoveryTabFeedRequest(null, null, null, null), PaginationRequest.of(0, 20));

        assertFalse(result.isSuccess());
        assertEquals("invalid-tab", result.getMessage());
    }

    @Test
    void getTabPlatesReturnsErrorForInvalidWindowDays() {
        when(messageService.getMessage(Messages.DISCOVERY_FILTER_INVALID)).thenReturn("invalid-filter");

        DataResult<PagedData<DiscoveryPlateCardDto>> result = discoveryManager.getTabPlates(
                USER_ID, "TREND", new DiscoveryTabFeedRequest(null, null, null, 400), PaginationRequest.of(0, 20));

        assertFalse(result.isSuccess());
        assertEquals("invalid-filter", result.getMessage());
    }

    @Test
    void getTabPlatesRejectsPremiumFiltersForFreeUser() {
        when(userDao.findByIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(Messages.DISCOVERY_FILTER_PREMIUM_REQUIRED)).thenReturn("premium-required");

        DataResult<PagedData<DiscoveryPlateCardDto>> result = discoveryManager.getTabPlates(
                USER_ID, "TREND", new DiscoveryTabFeedRequest(null, "DANGEROUS_DRIVING", null, null), PaginationRequest.of(0, 20));

        assertFalse(result.isSuccess());
        assertEquals("premium-required", result.getMessage());
    }

    @Test
    void getTabPlatesReturnsPagedCards() {
        TimeWindow window = new TimeWindow(LocalDateTime.now().minusDays(7), LocalDateTime.now());
        when(timeWindowService.lastDaysWindow(7)).thenReturn(window);

        List<ScoredDiscoveryPlate> candidates = new ArrayList<>();
        for (long plateId = 1L; plateId <= 3L; plateId++) {
            Plate plate = mock(Plate.class);
            when(plate.getId()).thenReturn(plateId);
            candidates.add(new ScoredDiscoveryPlate(plate, new PlateDailyMetrics(), 10.0 - plateId));
        }
        when(discoveryTabService.buildTabScored(DiscoveryTabType.TREND, 500, window)).thenReturn(candidates);
        when(discoveryTabService.buildTopReportTypesMap(anySet())).thenReturn(Map.of());
        when(discoveryTabService.toPlateCard(org.mockito.ArgumentMatchers.any(), anyMap()))
                .thenReturn(new DiscoveryPlateCardDto());
        when(messageService.getMessage(Messages.DISCOVERY_TAB_LISTED)).thenReturn("listed");

        DataResult<PagedData<DiscoveryPlateCardDto>> result = discoveryManager.getTabPlates(
                USER_ID, "TREND", new DiscoveryTabFeedRequest(null, null, null, null), PaginationRequest.of(0, 2));

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().getItems().size());
        assertEquals(3L, result.getData().getMeta().getTotalElements());
        assertTrue(result.getData().getMeta().isHasNext());
    }

    @Test
    void getCityPlatesReturnsBadRequestWhenCityNotFound() {
        when(cityDao.existsById(34)).thenReturn(false);
        when(messageService.getMessage(Messages.CITY_NOT_FOUND)).thenReturn("city-not-found");

        DataResult<PagedData<CityPlateActivityDto>> result = discoveryManager.getCityPlates(34, PaginationRequest.of(0, 20));

        assertFalse(result.isSuccess());
        assertEquals("city-not-found", result.getMessage());
    }

    @Test
    void getCityPlatesReturnsPagedDataWhenCityExists() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        CityPlateActivityDto row = new CityPlateActivityDto("34ABC123", 2L, 1L, LocalDateTime.now(), 4.5, 10);

        when(cityDao.existsById(34)).thenReturn(true);
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(discoveryAggregationService.buildCityDailyMetrics(34, today)).thenReturn(Map.of(1L, new com.mefy.platemate.business.discovery.model.PlateDailyMetrics()));
        when(discoveryAggregationService.toCityPlateActivityRows(org.mockito.ArgumentMatchers.anyMap())).thenReturn(new ArrayList<>(List.of(row)));
        when(messageService.getMessage(Messages.DISCOVERY_CITY_PLATES_LISTED)).thenReturn("listed");

        DataResult<PagedData<CityPlateActivityDto>> result = discoveryManager.getCityPlates(34, PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals("listed", result.getMessage());
        assertEquals(1, result.getData().getItems().size());
    }
}
