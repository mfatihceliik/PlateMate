package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryPremiumStatsDto implements IDto {
    private Long weeklySearchCount;
    private Long weeklyReviewCount;
    private Long weeklyReportCount;
    private Double weeklySearchDeltaPercent;
    private Double weeklyReviewDeltaPercent;
    private Double weeklyReportDeltaPercent;
}
