package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryExtendedStatsDto implements IDto {
    private Long yesterdaySearchCount;
    private Long yesterdayReviewCount;
    private Long yesterdayReportCount;
    private Double searchDeltaPercent;
    private Double reviewDeltaPercent;
    private Double reportDeltaPercent;
    private List<DiscoveryReportTypeCountDto> topReportTypesToday;
}
