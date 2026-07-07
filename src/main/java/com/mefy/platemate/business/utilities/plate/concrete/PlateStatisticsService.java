package com.mefy.platemate.business.utilities.plate.concrete;

import com.mefy.platemate.business.utilities.Numbers;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Recomputes a plate's denormalized review aggregates (reviewCount, totalRatingSum,
 * ratingAverage) from its APPROVED reviews and persists the plate. Extracted from the review
 * and moderation managers, where this block was duplicated verbatim.
 */
@Service
@RequiredArgsConstructor
public class PlateStatisticsService {

    private final IPlateDao plateDao;
    private final IPlateReviewDao plateReviewDao;

    public void refresh(Plate plate) {
        if (plate == null || plate.getId() == null) {
            return;
        }

        Long approvedStatusId = PlateReviewStatus.APPROVED.getId();
        int reviewCount = Numbers.toSafeInt(plateReviewDao.countByPlateIdAndStatusId(plate.getId(), approvedStatusId));
        long totalRatingSum = Numbers.safeLong(plateReviewDao.sumRatingByPlateIdAndStatus(plate.getId(), approvedStatusId));

        plate.setReviewCount(reviewCount);
        plate.setTotalRatingSum(totalRatingSum);
        plate.setRatingAverage(reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0);
        plate.setUpdatedAt(LocalDateTime.now());
        plateDao.save(plate);
    }
}
