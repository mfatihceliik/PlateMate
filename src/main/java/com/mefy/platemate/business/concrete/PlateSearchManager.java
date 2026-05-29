package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.PlateReportTypeMapper;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlateSearchManager implements IPlateSearchService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateSearchEventDao plateSearchEventDao;
    private final ICityDao cityDao;
    private final IPlateReportDao plateReportDao;
    private final PlateReportTypeMapper plateReportTypeMapper;
    private final PlateReviewMapper plateReviewMapper;
    private final IPlateValidator plateValidator;
    private final TrPlateCityResolver plateCityResolver;
    private final IMessageService messageService;

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
        PlateDetailDto dto = buildPlateDetailDto(plate, normalizedPlate);

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

    private PlateDetailDto buildPlateDetailDto(Plate plate, String normalizedPlate) {
        PlateDetailDto dto = new PlateDetailDto();
        dto.setId(plate.getId());
        dto.setPlateCode(plate.getPlateCode());
        dto.setCityName(
                plate.getCity() != null
                        ? plate.getCity().getName()
                        : plateCityResolver.resolveCityName(normalizedPlate).orElse(null)
        );
        dto.setRatingAverage(plate.getRatingAverage() == null ? 0.0 : plate.getRatingAverage());
        dto.setReviewCount(plate.getReviewCount() == null ? 0 : plate.getReviewCount());
        dto.setTotalRatingSum(plate.getTotalRatingSum() == null ? 0L : plate.getTotalRatingSum());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        List<PlateReviewDto> reviews = plateReviewDao
                .findByPlatePlateCodeAndStatusId(normalizedPlate, PlateReviewStatus.APPROVED.getId(), pageable)
                .map(plateReviewMapper::entityToDto)
                .getContent();
        dto.setRecentReviews(reviews);

        List<PlateReport> reports = plateReportDao
                .findByPlateIdInAndActiveTrue(java.util.List.of(plate.getId()));
        List<PlateReportTypeDto> reportTypes = reports.stream()
                .map(r -> plateReportTypeMapper.entityToDto(r.getReportType()))
                .distinct()
                .toList();
        dto.setRecentReportTypes(reportTypes);

        populateTotalMetrics(plate, dto);
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
