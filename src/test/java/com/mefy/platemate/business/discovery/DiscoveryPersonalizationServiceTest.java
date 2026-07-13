package com.mefy.platemate.business.discovery;

import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.dataAccess.abstracts.IPlateFollowDao;
import com.mefy.platemate.dataAccess.abstracts.ISavedPlateDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateFollow;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.concrete.SavedPlate;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryForYouDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.DiscoveryRecentActivityDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryPersonalizationServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private IPlateFollowDao plateFollowDao;
    @Mock
    private ISavedPlateDao savedPlateDao;
    @Mock
    private DiscoveryAggregationService discoveryAggregationService;
    @Mock
    private DiscoveryTabService discoveryTabService;
    @Mock
    private TimeWindowService timeWindowService;

    private DiscoveryPersonalizationService personalizationService;

    @BeforeEach
    void setUp() {
        personalizationService = new DiscoveryPersonalizationService(
                plateFollowDao,
                savedPlateDao,
                discoveryAggregationService,
                discoveryTabService,
                timeWindowService
        );
    }

    @Test
    void buildForYouCollectsFollowedAndSavedPlatesAndFiltersActivities() {
        TimeWindow today = new TimeWindow(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        TimeWindow week = new TimeWindow(LocalDateTime.now().minusDays(7), LocalDateTime.now());
        TimeWindow previousWeek = new TimeWindow(LocalDateTime.now().minusDays(14), LocalDateTime.now().minusDays(7));

        Plate followedPlate = mock(Plate.class);
        when(followedPlate.getId()).thenReturn(10L);
        when(followedPlate.getStatus()).thenReturn(PlateStatus.ACTIVE);
        when(followedPlate.getPlateCode()).thenReturn("34ABC123");
        PlateFollow follow = mock(PlateFollow.class);
        when(follow.getPlate()).thenReturn(followedPlate);

        Plate savedPlateEntity = mock(Plate.class);
        when(savedPlateEntity.getId()).thenReturn(20L);
        when(savedPlateEntity.getStatus()).thenReturn(PlateStatus.ACTIVE);
        SavedPlate saved = mock(SavedPlate.class);
        when(saved.getPlate()).thenReturn(savedPlateEntity);

        when(plateFollowDao.findByUserIdWithPlate(USER_ID)).thenReturn(List.of(follow));
        when(savedPlateDao.findByUserIdWithPlate(USER_ID)).thenReturn(List.of(saved));
        when(discoveryAggregationService.buildDailyMetrics(today)).thenReturn(Map.of());
        when(discoveryTabService.buildTopReportTypesMap(anySet())).thenReturn(Map.of());
        when(discoveryTabService.toPlateCard(any(), anyMap())).thenReturn(new DiscoveryPlateCardDto());
        when(timeWindowService.lastDaysWindow(7)).thenReturn(week);
        when(timeWindowService.previousDaysWindow(7)).thenReturn(previousWeek);
        when(discoveryAggregationService.getDailyStats(week)).thenReturn(new DiscoveryDailyStatsDto(10L, 4L, 2L));
        when(discoveryAggregationService.getDailyStats(previousWeek)).thenReturn(new DiscoveryDailyStatsDto(5L, 4L, 0L));

        DiscoveryRecentActivityDto followedActivity = new DiscoveryRecentActivityDto(
                "fatih", "34ABC123", null, LocalDateTime.now(), 5, "iyi", null, null);
        DiscoveryRecentActivityDto otherActivity = new DiscoveryRecentActivityDto(
                "ali", "06XYZ987", null, LocalDateTime.now(), 3, "", null, null);

        DiscoveryForYouDto result = personalizationService.buildForYou(
                USER_ID, today, List.of(followedActivity, otherActivity));

        assertEquals(1, result.getFollowedPlates().size());
        assertEquals(1, result.getSavedPlates().size());
        assertEquals(1, result.getFollowedPlateActivities().size());
        assertEquals("34ABC123", result.getFollowedPlateActivities().get(0).getPlateCode());
        assertNotNull(result.getPremiumStats());
        assertEquals(100.0, result.getPremiumStats().getWeeklySearchDeltaPercent());
        assertEquals(0.0, result.getPremiumStats().getWeeklyReviewDeltaPercent());
        assertEquals(200.0, result.getPremiumStats().getWeeklyReportDeltaPercent());
    }
}
