package com.mefy.platemate.dataAccess.projections;

import java.time.LocalDateTime;

public interface RecentReportActivityProjection {
    Long getReportId();

    String getUsername();

    String getPlateCode();

    String getReportTypeCode();

    String getReportTypeLabel();

    Long getReportTypeId();

    LocalDateTime getOccurredAt();
}
