package com.mefy.platemate.business.discovery;

import com.mefy.platemate.business.discovery.model.PlateDailyMetrics;
import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryCityStatDto;
import com.mefy.platemate.entities.dto.DiscoveryDailyStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiscoveryAggregationService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateReportDao plateReportDao;
    private final IPlateSearchEventDao plateSearchEventDao;

    public DiscoveryDailyStatsDto getDailyStats(TimeWindow window) {
        return new DiscoveryDailyStatsDto(
                plateSearchEventDao.countBySearchedAtGreaterThanEqualAndSearchedAtLessThan(window.getStart(), window.getEnd()),
                plateReviewDao.countApprovedByCreatedAtWindow(window.getStart(), window.getEnd()),
                plateReportDao.countActiveByReportedAtWindow(window.getStart(), window.getEnd())
        );
    }

    public Map<Long, PlateDailyMetrics> buildDailyMetrics(TimeWindow window) {
        Map<Long, PlateDailyMetrics> metricsByPlateId = new HashMap<>();

        plateSearchEventDao.getDailySearchAggregates(window.getStart(), window.getEnd()).forEach(projection -> {
            PlateDailyMetrics metrics = metricsByPlateId.computeIfAbsent(projection.getPlateId(), key -> new PlateDailyMetrics());
            metrics.addTodaySearchCount(safeLong(projection.getSearchCount()));
            metrics.mergeLastActivityAt(projection.getLastSearchedAt());
        });

        plateReviewDao.getDailyReviewAggregates(window.getStart(), window.getEnd()).forEach(projection -> {
            PlateDailyMetrics metrics = metricsByPlateId.computeIfAbsent(projection.getPlateId(), key -> new PlateDailyMetrics());
            metrics.addTodayReviewCount(safeLong(projection.getReviewCount()));
            metrics.mergeLastActivityAt(projection.getLastReviewAt());
        });

        plateReportDao.getActiveReportAggregates(window.getStart(), window.getEnd()).forEach(projection -> {
            PlateDailyMetrics metrics = metricsByPlateId.computeIfAbsent(projection.getPlateId(), key -> new PlateDailyMetrics());
            metrics.addTodayReportCount(safeLong(projection.getReportCount()));
            metrics.addTodayWeightedReportScore(safeLong(projection.getWeightedScore()));
            metrics.mergeLastActivityAt(projection.getLastReportedAt());
        });

        return metricsByPlateId;
    }

    public Map<Long, PlateDailyMetrics> buildCityDailyMetrics(Integer cityId, TimeWindow window) {
        Map<Long, PlateDailyMetrics> metricsByPlateId = new HashMap<>();

        plateReviewDao.getCityDailyReviewAggregates(cityId, window.getStart(), window.getEnd()).forEach(projection -> {
            PlateDailyMetrics metrics = metricsByPlateId.computeIfAbsent(projection.getPlateId(), key -> new PlateDailyMetrics());
            metrics.addTodayReviewCount(safeLong(projection.getReviewCount()));
            metrics.mergeLastActivityAt(projection.getLastReviewAt());
        });

        plateReportDao.getCityActiveReportAggregates(cityId, window.getStart(), window.getEnd()).forEach(projection -> {
            PlateDailyMetrics metrics = metricsByPlateId.computeIfAbsent(projection.getPlateId(), key -> new PlateDailyMetrics());
            metrics.addTodayReportCount(safeLong(projection.getReportCount()));
            metrics.addTodayWeightedReportScore(safeLong(projection.getWeightedScore()));
            metrics.mergeLastActivityAt(projection.getLastReportedAt());
        });

        return metricsByPlateId;
    }

    public List<DiscoveryCityStatDto> getTopCityStats(TimeWindow window, int cityLimit) {
        return plateReviewDao.getTopCityReviewCounts(
                        window.getStart(),
                        window.getEnd(),
                        PageRequest.of(0, cityLimit)
                ).stream()
                .map(item -> new DiscoveryCityStatDto(item.getCityId(), item.getCityName(), item.getReviewCount()))
                .toList();
    }

    public List<CityPlateActivityDto> toCityPlateActivityRows(Map<Long, PlateDailyMetrics> metricsByPlateId) {
        if (metricsByPlateId.isEmpty()) {
            return List.of();
        }

        Map<Long, Plate> plateById = plateDao.findAllById(metricsByPlateId.keySet()).stream()
                .collect(Collectors.toMap(Plate::getId, plate -> plate));

        List<CityPlateActivityDto> rows = new ArrayList<>();
        for (Map.Entry<Long, PlateDailyMetrics> entry : metricsByPlateId.entrySet()) {
            Plate plate = plateById.get(entry.getKey());
            if (plate == null || plate.getStatus() != PlateStatus.ACTIVE) continue;

            PlateDailyMetrics metrics = entry.getValue();
            rows.add(new CityPlateActivityDto(
                    plate.getPlateCode(),
                    metrics.getTodayReviewCount(),
                    metrics.getTodayReportCount(),
                    metrics.getLastActivityAt(),
                    safeDouble(plate.getRatingAverage()),
                    safeInt(plate.getReviewCount())
            ));
        }
        return rows;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
