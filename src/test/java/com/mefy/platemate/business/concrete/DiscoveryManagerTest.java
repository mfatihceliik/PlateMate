package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.discovery.DiscoveryActivityService;
import com.mefy.platemate.business.discovery.DiscoveryAggregationService;
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
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryActivityActionType;
import com.mefy.platemate.entities.dto.DiscoveryCityStatDto;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryRecentActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryTabsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryManagerTest {

    @Mock
    private ICityDao cityDao;
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

    private DiscoveryManager discoveryManager;

    @BeforeEach
    void setUp() {
        discoveryManager = new DiscoveryManager(
                cityDao,
                messageService,
                limitValidator,
                timeWindowService,
                discoveryAggregationService,
                discoveryTabService,
                discoveryActivityService
        );
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

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(1L, 2L, 3L));
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of(activity));
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(8,  5, 20);

        assertTrue(result.isSuccess());
        assertEquals("ok", result.getMessage());
        assertEquals(1L, result.getData().getDailyStats().getTodaySearchCount());
        assertEquals(1, result.getData().getRecentActivities().size());
    }

    @Test
    void getHomeReturnsErrorWhenValidationFails() {
        when(limitValidator.validateLimits(anyInt(), anyInt(), anyInt())).thenReturn(new ErrorResult("invalid"));

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(0, 5,20);

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

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(1L, 2L, 3L));
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of(city));
        when(discoveryAggregationService.buildCityDailyMetrics(34, last7Days))
                .thenReturn(Map.of(1L, new com.mefy.platemate.business.discovery.model.PlateDailyMetrics()));
        when(discoveryAggregationService.toCityPlateActivityRows(anyMap())).thenReturn(new ArrayList<>(List.of(row)));
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, last7Days)).thenReturn(List.of(activity));
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(8, 5, 20);

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

        when(limitValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(timeWindowService.todayWindow()).thenReturn(today);
        when(timeWindowService.lastDaysWindow(7)).thenReturn(last7Days);
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(0L, 0L, 0L));
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryAggregationService.getTopCityStats(last7Days, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, today)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20, last7Days)).thenReturn(List.of());
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(8, 5, 20);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().getCityStats().isEmpty());
        assertTrue(result.getData().getTopCityPlates().isEmpty());
        assertTrue(result.getData().getRecentActivities().isEmpty());
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
