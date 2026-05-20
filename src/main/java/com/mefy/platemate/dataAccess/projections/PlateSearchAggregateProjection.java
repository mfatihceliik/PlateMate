package com.mefy.platemate.dataAccess.projections;

import java.time.LocalDateTime;

public interface PlateSearchAggregateProjection {
    Long getPlateId();

    Long getSearchCount();

    LocalDateTime getLastSearchedAt();
}
