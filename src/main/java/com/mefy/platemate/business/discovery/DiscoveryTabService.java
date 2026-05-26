package com.mefy.platemate.business.discovery;

import com.mefy.platemate.business.discovery.model.PlateDailyMetrics;
import com.mefy.platemate.business.discovery.model.ScoredDiscoveryPlate;
import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.dataAccess.projections.PlateReportAggregateProjection;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.core.utilities.mappers.PlateReportTypeMapper;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.DiscoveryTabsDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiscoveryTabService {

    private static final int MIN_GOOD_DRIVER_REVIEW_COUNT = 3;
    private static final int GOOD_DRIVER_CANDIDATE_LIMIT = 200;

    private final IPlateDao plateDao;
    private final IPlateReportDao plateReportDao;
    private final DiscoveryAggregationService discoveryAggregationService;
    private final TimeWindowService timeWindowService;
    private final PlateReportTypeMapper plateReportTypeMapper;

    public DiscoveryTabsDto buildTabs(int limit) {
        List<ScoredDiscoveryPlate> trendScored = buildTrendTabScored(limit, timeWindowService.todayWindow());
        List<ScoredDiscoveryPlate> attentionScored = buildDangerousTabScored(limit, timeWindowService.lastDaysWindow(7));
        List<ScoredDiscoveryPlate> goodDriverScored = buildGoodDriverTabScored(limit, timeWindowService.lastDaysWindow(30));
        List<ScoredDiscoveryPlate> newScored = buildNewTabScored(limit);

        Set<Long> allPlateIds = new HashSet<>();
        trendScored.forEach(p -> allPlateIds.add(p.getPlate().getId()));
        attentionScored.forEach(p -> allPlateIds.add(p.getPlate().getId()));
        goodDriverScored.forEach(p -> allPlateIds.add(p.getPlate().getId()));
        newScored.forEach(p -> allPlateIds.add(p.getPlate().getId()));

        Map<Long, List<PlateReportTypeDto>> trendPlatesMap = buildTrendPlatesMap(allPlateIds);

        return new DiscoveryTabsDto(
                trendScored.stream().map(s -> toPlateCard(s, trendPlatesMap)).toList(),
                attentionScored.stream().map(s -> toPlateCard(s, trendPlatesMap)).toList(),
                goodDriverScored.stream().map(s -> toPlateCard(s, trendPlatesMap)).toList(),
                newScored.stream().map(s -> toPlateCard(s, trendPlatesMap)).toList()
        );
    }

    private Map<Long, List<PlateReportTypeDto>> buildTrendPlatesMap(Set<Long> plateIds) {
        Map<Long, List<PlateReportTypeDto>> map = new HashMap<>();
        if (plateIds.isEmpty()) return map;

        List<PlateReport> activeReports = plateReportDao.findByPlateIdInAndActiveTrue(plateIds);
        Map<Long, Map<Long, Long>> reportFrequencyByPlate = new HashMap<>();
        Map<Long, PlateReportTypeDto> dtoCache = new HashMap<>();

        for (PlateReport report : activeReports) {
            Long plateId = report.getPlate().getId();
            Long typeId = report.getReportType().getId();
            reportFrequencyByPlate.computeIfAbsent(plateId, k -> new HashMap<>())
                    .merge(typeId, 1L, Long::sum);
            dtoCache.putIfAbsent(typeId, plateReportTypeMapper.entityToDto(report.getReportType()));
        }

        for (Map.Entry<Long, Map<Long, Long>> entry : reportFrequencyByPlate.entrySet()) {
            List<PlateReportTypeDto> top2 = entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(2)
                    .map(e -> dtoCache.get(e.getKey()))
                    .toList();
            map.put(entry.getKey(), top2);
        }
        return map;
    }

    private List<ScoredDiscoveryPlate> buildTrendTabScored(int limit, TimeWindow todayWindow) {
        Map<Long, PlateDailyMetrics> metricsByPlateId = discoveryAggregationService.buildDailyMetrics(todayWindow);
        if (metricsByPlateId.isEmpty()) {
            return List.of();
        }

        Map<Long, Plate> plateById = plateDao.findAllById(metricsByPlateId.keySet()).stream()
                .collect(Collectors.toMap(Plate::getId, plate -> plate));

        List<ScoredDiscoveryPlate> scoredPlates = new ArrayList<>();
        for (Map.Entry<Long, PlateDailyMetrics> entry : metricsByPlateId.entrySet()) {
            Plate plate = plateById.get(entry.getKey());
            if (plate == null || plate.getStatus() != PlateStatus.ACTIVE) continue;

            PlateDailyMetrics metrics = entry.getValue();
            double score = metrics.getTodaySearchCount()
                    + (metrics.getTodayReviewCount() * 2.0)
                    + (metrics.getTodayWeightedReportScore() * 3.0);
            scoredPlates.add(new ScoredDiscoveryPlate(plate, metrics, score));
        }

        scoredPlates.sort(Comparator.comparing(ScoredDiscoveryPlate::getScore).reversed()
                .thenComparing(item -> item.getMetrics().getLastActivityAt(), Comparator.nullsLast(Comparator.reverseOrder())));

        return scoredPlates.stream()
                .limit(limit)
                .toList();
    }

    private List<ScoredDiscoveryPlate> buildDangerousTabScored(int limit, TimeWindow window) {
        List<PlateReportAggregateProjection> projections =
                plateReportDao.getActiveReportAggregates(window.getStart(), window.getEnd());
        if (projections.isEmpty()) {
            return List.of();
        }

        Set<Long> plateIds = new HashSet<>();
        projections.forEach(item -> plateIds.add(item.getPlateId()));
        Map<Long, Plate> plateById = plateDao.findAllById(plateIds).stream()
                .collect(Collectors.toMap(Plate::getId, plate -> plate));

        List<ScoredDiscoveryPlate> scoredPlates = new ArrayList<>();
        for (PlateReportAggregateProjection projection : projections) {
            Plate plate = plateById.get(projection.getPlateId());
            if (plate == null || plate.getStatus() != PlateStatus.ACTIVE) continue;

            PlateDailyMetrics metrics = new PlateDailyMetrics();
            metrics.addTodayReportCount(safeLong(projection.getReportCount()));
            metrics.addTodayWeightedReportScore(safeLong(projection.getWeightedScore()));
            metrics.mergeLastActivityAt(projection.getLastReportedAt());

            double score = (metrics.getTodayWeightedReportScore() * 2.0) + metrics.getTodayReportCount();
            scoredPlates.add(new ScoredDiscoveryPlate(plate, metrics, score));
        }

        scoredPlates.sort(Comparator.comparing(ScoredDiscoveryPlate::getScore).reversed()
                .thenComparing(item -> item.getMetrics().getLastActivityAt(), Comparator.nullsLast(Comparator.reverseOrder())));

        return scoredPlates.stream()
                .limit(limit)
                .toList();
    }

    private List<ScoredDiscoveryPlate> buildGoodDriverTabScored(int limit, TimeWindow penaltyWindow) {
        List<Plate> candidates = plateDao.findByReviewCountGreaterThanEqual(
                MIN_GOOD_DRIVER_REVIEW_COUNT,
                PageRequest.of(0, GOOD_DRIVER_CANDIDATE_LIMIT, Sort.by("ratingAverage").descending())
        );
        if (candidates.isEmpty()) {
            return List.of();
        }

        Set<Long> candidateIds = new HashSet<>();
        for (Plate plate : candidates) {
            if (plate.getId() != null) {
                candidateIds.add(plate.getId());
            }
        }

        Map<Long, Long> weightedPenaltyByPlate = new HashMap<>();
        if (!candidateIds.isEmpty()) {
            plateReportDao.getWeightedScoresByPlateIds(candidateIds, penaltyWindow.getStart(), penaltyWindow.getEnd())
                    .forEach(projection -> weightedPenaltyByPlate.put(projection.getPlateId(), safeLong(projection.getWeightedScore())));
        }

        List<ScoredDiscoveryPlate> scoredPlates = new ArrayList<>();
        for (Plate plate : candidates) {
            if (plate.getStatus() != PlateStatus.ACTIVE) {
                continue;
            }
            PlateDailyMetrics metrics = new PlateDailyMetrics();
            metrics.addTodayWeightedReportScore(weightedPenaltyByPlate.getOrDefault(plate.getId(), 0L));
            double score = (safeDouble(plate.getRatingAverage()) * 20.0)
                    + Math.min(safeInt(plate.getReviewCount()), 40)
                    - (metrics.getTodayWeightedReportScore() * 2.0);
            scoredPlates.add(new ScoredDiscoveryPlate(plate, metrics, score));
        }

        scoredPlates.sort(Comparator.comparing(ScoredDiscoveryPlate::getScore).reversed());
        return scoredPlates.stream()
                .limit(limit)
                .toList();
    }

    private List<ScoredDiscoveryPlate> buildNewTabScored(int limit) {
        List<Plate> newest = plateDao.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending())).getContent();
        List<ScoredDiscoveryPlate> data = new ArrayList<>();

        for (Plate plate : newest) {
            if (plate.getStatus() != PlateStatus.ACTIVE) {
                continue;
            }
            PlateDailyMetrics metrics = new PlateDailyMetrics();
            metrics.mergeLastActivityAt(plate.getCreatedAt());
            double score = plate.getCreatedAt() == null ? 0.0
                    : plate.getCreatedAt().atZone(TimeWindowService.TURKEY_ZONE).toEpochSecond();
            data.add(new ScoredDiscoveryPlate(plate, metrics, score));
        }

        return data;
    }

    private DiscoveryPlateCardDto toPlateCard(ScoredDiscoveryPlate scored, Map<Long, List<PlateReportTypeDto>> trendPlatesMap) {
        Plate plate = scored.getPlate();
        PlateDailyMetrics metrics = scored.getMetrics();
        return new DiscoveryPlateCardDto(
                plate.getPlateCode(),
                plate.getCity() != null ? plate.getCity().getName() : null,
                safeDouble(plate.getRatingAverage()),
                safeInt(plate.getReviewCount()),
                metrics.getTodaySearchCount(),
                metrics.getTodayReviewCount(),
                metrics.getTodayReportCount(),
                metrics.getTodayWeightedReportScore(),
                scored.getScore(),
                metrics.getLastActivityAt(),
                trendPlatesMap.getOrDefault(plate.getId(), List.of())
        );
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
