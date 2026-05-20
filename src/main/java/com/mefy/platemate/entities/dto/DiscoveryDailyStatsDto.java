package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryDailyStatsDto implements IDto {
    private Long todaySearchCount;
    private Long todayReviewCount;
    private Long todayReportCount;
}
