package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateService;
import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.PlateMapper;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.SyncPlateReportsRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlateManager implements IPlateService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IPlateSearchEventDao plateSearchEventDao;
    private final IUserDao userDao;
    private final ICityDao cityDao;
    private final IPlateReportService plateReportService;
    private final com.mefy.platemate.dataAccess.abstracts.IPlateReportDao plateReportDao;
    private final PlateMapper plateMapper;
    private final PlateReviewMapper plateReviewMapper;
    private final IPlateValidator plateValidator;
    private final TrPlateCityResolver plateCityResolver;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<PlateDto> searchByPlateCode(String plateCode) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Plate plate = getOrCreatePlate(normalizedPlate);
        recordSearchEvent(plate);
        PlateDto dto = plateMapper.entityToDto(plate);
        if (dto.getCityName() == null) {
            dto.setCityName(plateCityResolver.resolveCityName(normalizedPlate).orElse(null));
        }

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PLATE_FOUND));
    }

    @Override
    @Transactional(jakarta.transaction.Transactional.TxType.SUPPORTS)
    public DataResult<com.mefy.platemate.entities.dto.PlateDetailDto> getPlateDetail(String plateCode) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Plate plate = plateDao.findByPlateCode(normalizedPlate).orElse(null);
        if (plate == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PLATE_INVALID));
        }

        com.mefy.platemate.entities.dto.PlateDetailDto dto = new com.mefy.platemate.entities.dto.PlateDetailDto();
        dto.setId(plate.getId());
        dto.setPlateCode(plate.getPlateCode());
        dto.setCityName(plate.getCity() != null ? plate.getCity().getName() : null);
        dto.setRatingAverage(plate.getRatingAverage() == null ? 0.0 : plate.getRatingAverage());
        dto.setReviewCount(plate.getReviewCount() == null ? 0 : plate.getReviewCount());
        dto.setTotalRatingSum(plate.getTotalRatingSum() == null ? 0L : plate.getTotalRatingSum());

        // Fetch max 20 reviews
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        java.util.List<com.mefy.platemate.entities.dto.PlateReviewDto> reviews = plateReviewDao
                .findByPlatePlateCode(normalizedPlate, pageable)
                .map(plateReviewMapper::entityToDto)
                .getContent();
        dto.setRecentReviews(reviews);

        // Fetch active report types
        java.util.List<com.mefy.platemate.entities.concrete.PlateReport> reports = plateReportDao
                .findByPlateIdInAndActiveTrue(java.util.List.of(plate.getId()));
        
        com.mefy.platemate.core.utilities.mappers.PlateReportTypeMapper plateReportTypeMapper = new com.mefy.platemate.core.utilities.mappers.PlateReportTypeMapper();
        java.util.List<com.mefy.platemate.entities.dto.PlateReportTypeDto> reportTypes = reports.stream()
                .map(r -> plateReportTypeMapper.entityToDto(r.getReportType()))
                .distinct()
                .toList();
        dto.setRecentReportTypes(reportTypes);

        populateTodayMetrics(plate, dto);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PLATE_FOUND));
    }

    @Override
    public DataResult<PagedData<PlateReviewDto>> getReviewsByPlateCode(
            String plateCode,
            PaginationRequest paginationRequest
    ) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Pageable pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var page = plateReviewDao.findByPlatePlateCode(normalizedPlate, pageable)
                .map(plateReviewMapper::entityToDto);
        PagedData<PlateReviewDto> reviews = PaginationMapper.fromPage(page);

        return new SuccessDataResult<>(reviews, messageService.getMessage(Messages.REVIEWS_LISTED));
    }

    @Override
    @Transactional
    public Result addReview(String plateCode, Long currentUserId, AddPlateReviewRequest request) {
        String normalizedPlate = normalizePlate(plateCode);
        User user = userDao.findById(currentUserId).orElse(null);

        Result result = BusinessRules.run(
                checkIfPlateValid(normalizedPlate),
                checkIfUserExists(user)
        );
        if (result != null) return result;

        Plate plate = getOrCreatePlate(normalizedPlate);
        if (request.getReportTypeCodes() != null) {
            Result syncResult = plateReportService.syncReportsForUserAndPlate(plate, currentUserId, request.getReportTypeCodes());
            if (!syncResult.isSuccess()) return syncResult;
        }
        PlateReview existingReview = plateReviewDao.findByPlateIdAndUserId(plate.getId(), currentUserId).orElse(null);

        if (existingReview != null) {
            existingReview.setRating(request.getRating());
            existingReview.setComment(request.getComment().trim());
            existingReview.setUpdatedAt(LocalDateTime.now());
            plateReviewDao.save(existingReview);
            refreshPlateStatistics(plate);
            return new SuccessResult(messageService.getMessage(Messages.REVIEW_UPDATED));
        }

        PlateReview review = new PlateReview();
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
        refreshPlateStatistics(plate);

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_ADDED));
    }

    @Override
    @Transactional
    public Result updateReview(Long reviewId, Long currentUserId, UpdatePlateReviewRequest request) {
        PlateReview review = plateReviewDao.findById(reviewId).orElse(null);

        Result result = BusinessRules.run(
                checkIfReviewExists(review),
                checkIfReviewOwner(review, currentUserId)
        );
        if (result != null) return result;

        if (request.getReportTypeCodes() != null) {
            Result syncResult = plateReportService.syncReportsForUserAndPlate(
                    review.getPlate(),
                    currentUserId,
                    request.getReportTypeCodes()
            );
            if (!syncResult.isSuccess()) return syncResult;
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setUpdatedAt(LocalDateTime.now());
        plateReviewDao.save(review);
        refreshPlateStatistics(review.getPlate());

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_UPDATED));
    }

    @Override
    @Transactional
    public Result deleteReview(Long reviewId, Long currentUserId) {
        PlateReview review = plateReviewDao.findById(reviewId).orElse(null);

        Result result = BusinessRules.run(
                checkIfReviewExists(review),
                checkIfReviewOwner(review, currentUserId)
        );
        if (result != null) return result;

        Plate plate = review.getPlate();
        plateReviewDao.delete(review);
        refreshPlateStatistics(plate);

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_DELETED));
    }

    @Override
    @Transactional
    public Result syncReports(String plateCode, Long currentUserId, SyncPlateReportsRequest request) {
        String normalizedPlate = normalizePlate(plateCode);
        User user = userDao.findById(currentUserId).orElse(null);

        Result result = BusinessRules.run(
                checkIfPlateValid(normalizedPlate),
                checkIfUserExists(user)
        );
        if (result != null) return result;

        Plate plate = getOrCreatePlate(normalizedPlate);
        return plateReportService.syncReportsForUserAndPlate(plate, currentUserId, request.getReportTypeCodes());
    }

    private Plate getOrCreatePlate(String normalizedPlate) {
        return plateDao.findByPlateCode(normalizedPlate).orElseGet(() -> plateDao.save(createPlate(normalizedPlate)));
    }

    private Plate createPlate(String normalizedPlate) {
        Plate plate = new Plate();
        plate.setPlateCode(normalizedPlate);
        plate.setRatingAverage(0.0);
        plate.setReviewCount(0);
        plate.setTotalRatingSum(0L);
        plate.setCreatedAt(LocalDateTime.now());
        plate.setUpdatedAt(LocalDateTime.now());
        plateCityResolver.resolveCityId(normalizedPlate).flatMap(cityDao::findById).ifPresent(plate::setCity);
        return plate;
    }

    private void refreshPlateStatistics(Plate plate) {
        if (plate == null || plate.getId() == null) return;

        long reviewCountLong = plateReviewDao.countByPlateId(plate.getId());
        int reviewCount = reviewCountLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) reviewCountLong;
        long totalRatingSum = plateReviewDao.sumRatingByPlateId(plate.getId());

        plate.setReviewCount(reviewCount);
        plate.setTotalRatingSum(totalRatingSum);
        plate.setRatingAverage(reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
    }

    private void recordSearchEvent(Plate plate) {
        if (plate == null || plate.getId() == null) return;
        LocalDateTime now = LocalDateTime.now();
        PlateSearchEvent event = new PlateSearchEvent();
        event.setPlate(plate);
        event.setSearchedAt(now);
        event.setCreatedAt(now);
        plateSearchEventDao.save(event);
    }

    private void populateTodayMetrics(Plate plate, com.mefy.platemate.entities.dto.PlateDetailDto dto) {
        if (plate == null || plate.getId() == null) {
            dto.setTodaySearchCount(0L);
            dto.setTodayReviewCount(0L);
            dto.setTodayReportCount(0L);
            dto.setTodayWeightedReportScore(0L);
            dto.setScore(0.0);
            dto.setLastActivityAt(null);
            return;
        }

        LocalDateTime start = LocalDate.now(com.mefy.platemate.business.utilities.time.TimeWindowService.TURKEY_ZONE)
                .atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long todaySearchCount = plateSearchEventDao.countByPlateIdAndSearchedAtGreaterThanEqualAndSearchedAtLessThan(
                plate.getId(), start, end
        );
        long todayReviewCount = plateReviewDao.countByPlateIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                plate.getId(), start, end
        );
        long todayReportCount = plateReportDao.countByPlateIdAndActiveTrueAndLastReportedAtGreaterThanEqualAndLastReportedAtLessThan(
                plate.getId(), start, end
        );
        long todayWeightedReportScore = safeLong(plateReportDao.getWeightedScoreByPlateIdAndWindow(
                plate.getId(), start, end
        ));

        LocalDateTime lastActivityAt = maxDate(
                plateSearchEventDao.findLastSearchedAtByPlateIdAndWindow(plate.getId(), start, end),
                plateReviewDao.findLastReviewAtByPlateIdAndWindow(plate.getId(), start, end),
                plateReportDao.findLastReportedAtByPlateIdAndWindow(plate.getId(), start, end),
                plate.getUpdatedAt()
        );

        double score = todaySearchCount
                + (todayReviewCount * 2.0)
                + (todayWeightedReportScore * 3.0);

        dto.setTodaySearchCount(todaySearchCount);
        dto.setTodayReviewCount(todayReviewCount);
        dto.setTodayReportCount(todayReportCount);
        dto.setTodayWeightedReportScore(todayWeightedReportScore);
        dto.setScore(score);
        dto.setLastActivityAt(lastActivityAt);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private LocalDateTime maxDate(LocalDateTime... values) {
        LocalDateTime max = null;
        for (LocalDateTime value : values) {
            if (value == null) {
                continue;
            }
            if (max == null || value.isAfter(max)) {
                max = value;
            }
        }
        return max;
    }

    private String normalizePlate(String plateCode) {
        if (plateCode == null) return "";
        return plateCode.replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private Result checkIfPlateValid(String plateCode) {
        if (!plateValidator.isValid(plateCode)) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_INVALID));
        }
        return new SuccessResult();
    }

    private Result checkIfUserExists(User user) {
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfReviewExists(PlateReview review) {
        if (review == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfReviewOwner(PlateReview review, Long currentUserId) {
        if (review == null) return new SuccessResult();
        if (!review.getUser().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage("review.delete.unauthorized"));
        }
        return new SuccessResult();
    }
}
