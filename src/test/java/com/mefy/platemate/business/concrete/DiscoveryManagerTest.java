package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.discovery.DiscoveryActivityService;
import com.mefy.platemate.business.discovery.DiscoveryAggregationService;
import com.mefy.platemate.business.discovery.DiscoveryQueryValidator;
import com.mefy.platemate.business.discovery.DiscoveryTabService;
import com.mefy.platemate.business.discovery.DiscoveryTimeWindowService;
import com.mefy.platemate.business.discovery.model.DiscoveryTimeWindow;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryTabsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryManagerTest {

    @Mock
    private ICityDao cityDao;
    @Mock
    private IMessageService messageService;
    @Mock
    private DiscoveryQueryValidator discoveryQueryValidator;
    @Mock
    private DiscoveryTimeWindowService discoveryTimeWindowService;
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
                discoveryQueryValidator,
                discoveryTimeWindowService,
                discoveryAggregationService,
                discoveryTabService,
                discoveryActivityService
        );
    }

    @Test
    void getHomeReturnsSuccessWhenValidationPasses() {
        DiscoveryTimeWindow today = new DiscoveryTimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());

        when(discoveryQueryValidator.validateLimits(8, 5, 20)).thenReturn(new SuccessResult());
        when(discoveryTimeWindowService.todayWindow()).thenReturn(today);
        when(discoveryAggregationService.getDailyStats(today)).thenReturn(new DiscoveryDailyStatsDto(1L, 2L, 3L));
        when(discoveryTabService.buildTabs(8)).thenReturn(new DiscoveryTabsDto(List.of(), List.of(), List.of(), List.of()));
        when(discoveryAggregationService.getTopCityStats(today, 5)).thenReturn(List.of());
        when(discoveryActivityService.buildRecentActivities(20)).thenReturn(List.of());
        when(messageService.getMessage(Messages.DISCOVERY_HOME_FOUND)).thenReturn("ok");

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(8,  5, 20);

        assertTrue(result.isSuccess());
        assertEquals("ok", result.getMessage());
        assertEquals(1L, result.getData().getDailyStats().getTodaySearchCount());
    }

    @Test
    void getHomeReturnsErrorWhenValidationFails() {
        when(discoveryQueryValidator.validateLimits(anyInt(), anyInt(), anyInt())).thenReturn(new ErrorResult("invalid"));

        DataResult<DiscoveryHomeDto> result = discoveryManager.getHome(0, 5,20);

        assertFalse(result.isSuccess());
        assertEquals("invalid", result.getMessage());
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
        DiscoveryTimeWindow today = new DiscoveryTimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        CityPlateActivityDto row = new CityPlateActivityDto("34ABC123", 2L, 1L, LocalDateTime.now(), 4.5, 10);

        when(cityDao.existsById(34)).thenReturn(true);
        when(discoveryTimeWindowService.todayWindow()).thenReturn(today);
        when(discoveryAggregationService.buildCityDailyMetrics(34, today)).thenReturn(Map.of(1L, new com.mefy.platemate.business.discovery.model.PlateDailyMetrics()));
        when(discoveryAggregationService.toCityPlateActivityRows(org.mockito.ArgumentMatchers.anyMap())).thenReturn(List.of(row));
        when(messageService.getMessage(Messages.DISCOVERY_CITY_PLATES_LISTED)).thenReturn("listed");

        DataResult<PagedData<CityPlateActivityDto>> result = discoveryManager.getCityPlates(34, PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals("listed", result.getMessage());
        assertEquals(1, result.getData().getItems().size());
    }
}
