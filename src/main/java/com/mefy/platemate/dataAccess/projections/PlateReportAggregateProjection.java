package com.mefy.platemate.dataAccess.projections;

import java.time.LocalDateTime;

public interface PlateReportAggregateProjection {
    Long getPlateId();

    Long getReportCount();

    Long getWeightedScore();

    LocalDateTime getLastReportedAt();
}
