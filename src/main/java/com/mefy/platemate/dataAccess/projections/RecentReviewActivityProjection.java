package com.mefy.platemate.dataAccess.projections;

import java.time.LocalDateTime;

public interface RecentReviewActivityProjection {
    Long getReviewId();

    String getUsername();

    String getPlateCode();

    Integer getRating();

    String getComment();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
