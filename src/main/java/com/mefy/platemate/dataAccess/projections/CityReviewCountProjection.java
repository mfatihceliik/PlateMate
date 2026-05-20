package com.mefy.platemate.dataAccess.projections;

public interface CityReviewCountProjection {
    Integer getCityId();

    String getCityName();

    Long getReviewCount();
}
