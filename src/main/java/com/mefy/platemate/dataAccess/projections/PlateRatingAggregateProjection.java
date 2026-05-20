package com.mefy.platemate.dataAccess.projections;

public interface PlateRatingAggregateProjection {
    Long getPlateId();

    Long getReviewCount();

    Long getTotalRatingSum();
}
