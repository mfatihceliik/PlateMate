package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.PlateDetailMapper;
import com.mefy.platemate.business.utilities.mappers.PlateDetailReviewMapper;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateFollowDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.concrete.PlateReportTypeTranslation;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateDetailReviewDto;
import com.mefy.platemate.entities.dto.PlateTagSummaryDto;
import com.mefy.platemate.entities.dto.RatingDistributionDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.mefy.platemate.business.utilities.plate.ReportTypeTranslationResolver;

@Service
@RequiredArgsConstructor
public class PlateSearchManager implements IPlateSearchService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateSearchEventDao plateSearchEventDao;
    private final ICityDao cityDao;
    private final IPlateReportDao plateReportDao;
    private final IPlateFollowDao plateFollowDao;
    private final IPlateValidator plateValidator;
    private final TrPlateCityResolver plateCityResolver;
    private final IMessageService messageService;
    private final ReportTypeTranslationResolver translationResolver;
    private final PlateDetailMapper plateDetailMapper;
    private final PlateDetailReviewMapper plateDetailReviewMapper;

    @Override
    @Transactional
    public DataResult<PlateDetailDto> searchByPlateCode(String plateCode, Long currentUserId) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Plate plate = getOrCreatePlate(normalizedPlate);
        Result visibilityResult = checkIfPlatePubliclyVisible(plate);
        if (!visibilityResult.isSuccess()) {
            return new ErrorDataResult<>(visibilityResult.getMessage());
        }

        recordSearchEvent(plate, currentUserId);
        PlateDetailDto dto = buildPlateDetailDto(plate, normalizedPlate, currentUserId);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PLATE_FOUND));
    }

    @Override
    public Plate getOrCreatePlate(String normalizedPlate) {
        return plateDao.findByPlateCode(normalizedPlate).orElseGet(() -> createPlateSafely(normalizedPlate));
    }

    @Override
    public String normalizePlate(String plateCode) {
        if (plateCode == null) return "";
        return plateCode.replaceAll("\\s+", "").toUpperCase(new Locale("tr", "TR"));
    }

    @Override
    public Result checkIfPlateValid(String normalizedPlate) {
        if (!plateValidator.isValid(normalizedPlate)) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_INVALID));
        }
        return new SuccessResult();
    }

    @Override
    public Result checkIfPlatePubliclyVisible(Plate plate) {
        if (plate != null && plate.getStatus() != PlateStatus.ACTIVE) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Plate createPlateSafely(String normalizedPlate) {
        try {
            Plate plate = createNewPlate(normalizedPlate);
            return plateDao.save(plate);
        } catch (DataIntegrityViolationException e) {
            return plateDao.findByPlateCode(normalizedPlate)
                    .orElseThrow(() -> new IllegalStateException("Could not retrieve plate after concurrent insert"));
        }
    }

    private Plate createNewPlate(String normalizedPlate) {
        Plate plate = new Plate();
        plate.setPlateCode(normalizedPlate);
        plate.setStatus(PlateStatus.ACTIVE);
        plate.setRatingAverage(0.0);
        plate.setReviewCount(0);
        plate.setTotalRatingSum(0L);
        plate.setCreatedAt(LocalDateTime.now());
        plate.setUpdatedAt(LocalDateTime.now());
        plateCityResolver.resolveCityId(normalizedPlate).flatMap(cityDao::findById).ifPresent(plate::setCity);
        return plate;
    }

    private void recordSearchEvent(Plate plate, Long userId) {
        if (plate == null || plate.getId() == null) return;
        LocalDateTime now = LocalDateTime.now();
        PlateSearchEvent event = new PlateSearchEvent();
        event.setPlate(plate);
        event.setUserId(userId);
        event.setSearchedAt(now);
        event.setCreatedAt(now);
        plateSearchEventDao.save(event);
    }

    private PlateDetailDto buildPlateDetailDto(Plate plate, String normalizedPlate, Long currentUserId) {
        PlateDetailDto dto = plateDetailMapper.entityToDto(plate);
        dto.setFollowing(currentUserId != null && plate.getId() != null
                && plateFollowDao.existsByUserIdAndPlateId(currentUserId, plate.getId()));
        if (dto.getCityName() == null) {
            dto.setCityName(plateCityResolver.resolveCityName(normalizedPlate).orElse(null));
        }

        Long approvedStatusId = PlateReviewStatus.APPROVED.getId();

        dto.setRatingDistribution(buildRatingDistribution(plate.getId(), approvedStatusId, dto.getReviewCount()));

        List<PlateReport> allReports = plateReportDao.findByPlateIdWithReportType(plate.getId(), approvedStatusId);
        Map<Long, PlateReportTypeTranslation> translationMap = loadTranslations();
        dto.setTagSummary(buildTagSummary(allReports, translationMap));

        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        List<PlateReview> reviews = plateReviewDao
                .findRecentByPlateCodeWithUserProfile(normalizedPlate, approvedStatusId, pageable);

        Map<Long, List<String>> userTagMap = buildUserTagMap(allReports, translationMap);
        dto.setRecentReviews(reviews.stream()
                .map(r -> toDetailReviewDto(r, userTagMap))
                .toList());

        populateTotalMetrics(plate, dto);
        return dto;
    }

    private List<RatingDistributionDto> buildRatingDistribution(Long plateId, Long statusId, int totalReviewCount) {
        List<Object[]> rows = plateReviewDao.getRatingDistribution(plateId, statusId);
        Map<Integer, Long> countByRating = new LinkedHashMap<>();
        for (Object[] row : rows) {
            countByRating.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        List<RatingDistributionDto> result = new ArrayList<>();
        for (int star = 5; star >= 1; star--) {
            long count = countByRating.getOrDefault(star, 0L);
            double pct = totalReviewCount > 0 ? (count * 100.0 / totalReviewCount) : 0.0;
            result.add(new RatingDistributionDto(star, count, Math.round(pct * 10.0) / 10.0));
        }
        return result;
    }

    private List<PlateTagSummaryDto> buildTagSummary(List<PlateReport> reports, Map<Long, PlateReportTypeTranslation> translationMap) {
        Map<Long, PlateReportType> typeById = new LinkedHashMap<>();
        Map<Long, Long> countByTypeId = new LinkedHashMap<>();

        for (PlateReport report : reports) {
            PlateReportType type = report.getReportType();
            typeById.putIfAbsent(type.getId(), type);
            countByTypeId.merge(type.getId(), 1L, Long::sum);
        }

        return countByTypeId.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> {
                    PlateReportType type = typeById.get(entry.getKey());
                    return new PlateTagSummaryDto(
                            type.getCode(),
                            resolveLabel(type, translationMap),
                            type.getIconKey(),
                            type.getColorHex(),
                            entry.getValue()
                    );
                })
                .toList();
    }

    private Map<Long, List<String>> buildUserTagMap(List<PlateReport> reports, Map<Long, PlateReportTypeTranslation> translationMap) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getUser().getId(),
                        Collectors.mapping(r -> resolveLabel(r.getReportType(), translationMap), Collectors.toList())
                ));
    }

    private String resolveLabel(PlateReportType type, Map<Long, PlateReportTypeTranslation> translationMap) {
        return translationResolver.resolveLabel(type, translationMap);
    }

    private Map<Long, PlateReportTypeTranslation> loadTranslations() {
        return translationResolver.loadTranslations();
    }

    private PlateDetailReviewDto toDetailReviewDto(PlateReview review, Map<Long, List<String>> userTagMap) {
        PlateDetailReviewDto dto = plateDetailReviewMapper.entityToDto(review);

        if (review.getUser() != null) {
            dto.setReportTags(userTagMap.getOrDefault(review.getUser().getId(), Collections.emptyList()));
        }

        return dto;
    }

    private void populateTotalMetrics(Plate plate, PlateDetailDto dto) {
        if (plate == null || plate.getId() == null) {
            dto.setTotalSearchCount(0L);
            dto.setTotalReviewCount(0L);
            dto.setTotalReportCount(0L);
            dto.setTotalWeightedReportScore(0L);
            dto.setScore(0.0);
            dto.setLastActivityAt(null);
            return;
        }

        long totalSearchCount = plateSearchEventDao.countByPlateId(plate.getId());
        long totalReviewCount = plateReviewDao.countByPlateIdAndStatusId(plate.getId(), PlateReviewStatus.APPROVED.getId());
        long totalReportCount = plateReportDao.countByPlateIdAndActiveTrue(plate.getId());
        long totalWeightedReportScore = safeLong(plateReportDao.getWeightedScoreByPlateId(plate.getId()));

        LocalDateTime lastActivityAt = maxDate(
                plateSearchEventDao.findLastSearchedAtByPlateId(plate.getId()),
                plateReviewDao.findLastReviewAtByPlateIdAndStatus(plate.getId(), PlateReviewStatus.APPROVED.getId()),
                plateReportDao.findLastReportedAtByPlateId(plate.getId()),
                plate.getUpdatedAt()
        );

        double score = totalSearchCount
                + (totalReviewCount * 2.0)
                + (totalWeightedReportScore * 3.0);

        dto.setTotalSearchCount(totalSearchCount);
        dto.setTotalReviewCount(totalReviewCount);
        dto.setTotalReportCount(totalReportCount);
        dto.setTotalWeightedReportScore(totalWeightedReportScore);
        dto.setScore(score);
        dto.setLastActivityAt(lastActivityAt);
    }

    private long safeLong(Long val) {
        return val == null ? 0L : val;
    }

    private LocalDateTime maxDate(LocalDateTime... dates) {
        LocalDateTime max = null;
        for (LocalDateTime date : dates) {
            if (date != null) {
                if (max == null || date.isAfter(max)) {
                    max = date;
                }
            }
        }
        return max;
    }
}
