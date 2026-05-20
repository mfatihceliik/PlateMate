package com.mefy.platemate.dataAccess.projections;

import java.time.LocalDateTime;

public interface PlateDailyReviewProjection {
    Long getPlateId();

    Long getReviewCount();

    LocalDateTime getLastReviewAt();
}
