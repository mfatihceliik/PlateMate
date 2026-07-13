package com.mefy.platemate.business.discovery;

import com.mefy.platemate.business.discovery.model.PlateDailyMetrics;
import com.mefy.platemate.business.discovery.model.ScoredDiscoveryPlate;
import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.dataAccess.abstracts.IPlateFollowDao;
import com.mefy.platemate.dataAccess.abstracts.ISavedPlateDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryForYouDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.DiscoveryPremiumStatsDto;
import com.mefy.platemate.entities.dto.DiscoveryRecentActivityDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Premium kullanicilar icin kisisellestirilmis "Senin Icin" bolumunu uretir:
 * takip edilen ve kaydedilen plakalarin gunluk metrikleri, bu plakalardaki
 * son aktiviteler ve haftalik delta istatistikleri.
 */
@Component
@RequiredArgsConstructor
public class DiscoveryPersonalizationService {

    private static final int FOR_YOU_PLATE_LIMIT = 10;
    private static final int FOR_YOU_ACTIVITY_LIMIT = 10;
    private static final int WEEKLY_WINDOW_DAYS = 7;

    private static final Comparator<ScoredDiscoveryPlate> BY_LAST_ACTIVITY_DESC =
            Comparator.comparing((ScoredDiscoveryPlate item) -> item.getMetrics().getLastActivityAt(),
                    Comparator.nullsLast(Comparator.reverseOrder()));

    private final IPlateFollowDao plateFollowDao;
    private final ISavedPlateDao savedPlateDao;
    private final DiscoveryAggregationService discoveryAggregationService;
    private final DiscoveryTabService discoveryTabService;
    private final TimeWindowService timeWindowService;

    public DiscoveryForYouDto buildForYou(
            Long userId,
            TimeWindow today,
            List<DiscoveryRecentActivityDto> recentActivities
    ) {
        List<Plate> followedPlates = plateFollowDao.findByUserIdWithPlate(userId).stream()
                .map(follow -> follow.getPlate())
                .toList();
        List<Plate> savedPlates = savedPlateDao.findByUserIdWithPlate(userId).stream()
                .map(saved -> saved.getPlate())
                .toList();

        Map<Long, PlateDailyMetrics> metricsByPlateId = discoveryAggregationService.buildDailyMetrics(today);

        Set<Long> plateIds = new HashSet<>();
        followedPlates.forEach(plate -> plateIds.add(plate.getId()));
        savedPlates.forEach(plate -> plateIds.add(plate.getId()));
        Map<Long, List<PlateReportTypeDto>> topReportTypesMap = discoveryTabService.buildTopReportTypesMap(plateIds);

        Set<String> followedPlateCodes = new HashSet<>();
        followedPlates.forEach(plate -> followedPlateCodes.add(plate.getPlateCode()));

        List<DiscoveryRecentActivityDto> followedActivities = recentActivities.stream()
                .filter(activity -> followedPlateCodes.contains(activity.getPlateCode()))
                .limit(FOR_YOU_ACTIVITY_LIMIT)
                .toList();

        return new DiscoveryForYouDto(
                toCards(followedPlates, metricsByPlateId, topReportTypesMap),
                toCards(savedPlates, metricsByPlateId, topReportTypesMap),
                followedActivities,
                buildPremiumStats()
        );
    }

    private List<DiscoveryPlateCardDto> toCards(
            List<Plate> plates,
            Map<Long, PlateDailyMetrics> metricsByPlateId,
            Map<Long, List<PlateReportTypeDto>> topReportTypesMap
    ) {
        return plates.stream()
                .filter(plate -> plate.getStatus() == PlateStatus.ACTIVE)
                .map(plate -> {
                    PlateDailyMetrics metrics =
                            metricsByPlateId.getOrDefault(plate.getId(), new PlateDailyMetrics());
                    return new ScoredDiscoveryPlate(plate, metrics, 0.0);
                })
                .sorted(BY_LAST_ACTIVITY_DESC)
                .limit(FOR_YOU_PLATE_LIMIT)
                .map(item -> discoveryTabService.toPlateCard(item, topReportTypesMap))
                .toList();
    }

    private DiscoveryPremiumStatsDto buildPremiumStats() {
        DiscoveryDailyStatsDto currentWeek =
                discoveryAggregationService.getDailyStats(timeWindowService.lastDaysWindow(WEEKLY_WINDOW_DAYS));
        DiscoveryDailyStatsDto previousWeek =
                discoveryAggregationService.getDailyStats(timeWindowService.previousDaysWindow(WEEKLY_WINDOW_DAYS));

        return new DiscoveryPremiumStatsDto(
                currentWeek.getTodaySearchCount(),
                currentWeek.getTodayReviewCount(),
                currentWeek.getTodayReportCount(),
                deltaPercent(currentWeek.getTodaySearchCount(), previousWeek.getTodaySearchCount()),
                deltaPercent(currentWeek.getTodayReviewCount(), previousWeek.getTodayReviewCount()),
                deltaPercent(currentWeek.getTodayReportCount(), previousWeek.getTodayReportCount())
        );
    }

    private Double deltaPercent(Long currentValue, Long previousValue) {
        long current = currentValue == null ? 0L : currentValue;
        long previous = previousValue == null ? 0L : previousValue;
        return (current - previous) * 100.0 / Math.max(previous, 1L);
    }
}
