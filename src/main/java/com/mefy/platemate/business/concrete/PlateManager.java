package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.PlateMapper;
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
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlateManager implements IPlateService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;
    private final IUserDao userDao;
    private final ICityDao cityDao;
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
        PlateDto dto = plateMapper.entityToDto(plate);
        if (dto.getCityName() == null) {
            dto.setCityName(plateCityResolver.resolveCityName(normalizedPlate).orElse(null));
        }

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PLATE_FOUND));
    }

    @Override
    public DataResult<Page<PlateReviewDto>> getReviewsByPlateCode(String plateCode, int page, int size) {
        String normalizedPlate = normalizePlate(plateCode);
        Result result = BusinessRules.run(checkIfPlateValid(normalizedPlate));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PlateReviewDto> reviews = plateReviewDao.findByPlatePlateCode(normalizedPlate, pageable)
                .map(plateReviewMapper::entityToDto);

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
